import java.io.*;

public class Manfutc {

    public static int id_equips = 0;
    public static int n_threads;

    public static JugadorsEquip no_agafar = new JugadorsEquip();
    public static Equip no_agafar_equip = new Equip(0, 0, 0, 0, no_agafar);
    public static JugadorsEquip agafar = new JugadorsEquip();
    public static Equip agafar_equip = new Equip(0, 0, 0, 0, agafar);

    public static ManfutcThreads[] threads_arr;
    public static boolean[] threads_act;

    public static final Object lock = new Object();

    public static Equip[] thread_return;

    public static void main(String[] argvs) throws InterruptedException {
        if (argvs.length != 3) {
            throw new IllegalArgumentException("Error while introduce the arguments: <pressupost>, <nom_mercat>, <n_threads>.");
        }
        int pressupost = Integer.parseInt(argvs[0]);
        n_threads = Integer.parseInt(argvs[2]);
        threads_arr = new ManfutcThreads[n_threads];
        threads_act = new boolean[n_threads];
        thread_return = new Equip[n_threads];

        Mercat mercat = new Mercat();
        JugadorsEquip jugadorsEquip = new JugadorsEquip();
        Equip equip = new Equip(id_equips, 0, 0, pressupost, jugadorsEquip);
        Equip equipOptim;

        //Reads the file (mercatXj.csv)
        try {
            System.out.println("----------\nLlegint el fitxer: " + argvs[1]);
            System.out.println("----------\nLlista de jugadors del mercat:");
            mercat = LlegirFitxerJugadors(argvs[1]);
        } catch (Exception e) {
            System.out.println("ERROR: Error reading the file.");
        }
        // Calculates the best team
        System.out.println("----------\nCalculant l'equip òptim...");
        equipOptim = calcularEquipOptim(mercat, equip, (mercat.NJugadors - 1));
        System.out.println("---------- MILLOR EQUIP OBTINGUT ----------");
        equipOptim.printTeam();
    }

    //Reads the file
    public static Mercat LlegirFitxerJugadors(String fitxer) {
        Mercat mercat = new Mercat();
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(fitxer);

        } catch (FileNotFoundException e) {
            System.err.println("ERROR: File not found.");
        }

        try {
            assert fis != null;
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            br.readLine();

            mercat.NJugadors = 0;

            String line;
            while ((line = br.readLine()) != null) {
                String[] field = line.split(";");

                // Creating player
                Jugador jugador = new Jugador();

                // Setting player's id
                jugador.setId(Integer.parseInt(field[0]));

                // Setting player's name
                jugador.setNom(field[1]);

                // Setting player's position
                switch (field[2]) {
                    case "Portero" -> jugador.setPosicio(TJugador.Porter);
                    case "Defensa" -> jugador.setPosicio(TJugador.Defensa);
                    case "Medio" -> jugador.setPosicio(TJugador.Migcampista);
                    case "Delantero" -> jugador.setPosicio(TJugador.Davanter);
                    default -> System.err.println("ERROR: Invalid player type.");
                }
                // Setting player's price
                jugador.setPreu(Integer.parseInt(field[3]));

                // Setting player's team
                jugador.setEquip(field[4]);

                // Setting player's value
                jugador.setValor(Integer.parseInt(field[5]));

                mercat.jugadors[mercat.NJugadors] = jugador;
                mercat.NJugadors++;
                System.out.println(jugador.printPlayer());
            }
            br.close();
        } catch (IOException e) {
            System.err.println("ERROR: Error doing I/O.");
        }
        return mercat;
    }

    // Calculates the best team
    public static Equip calcularEquipOptim(Mercat mercat, Equip equip, int index) throws InterruptedException {
        if (index == 0) {
            if (equip.playerFits(mercat.getJugador(index)) && equip.isRepeated(mercat.getJugador(index))) {
                equip.jugadorsEquip.addPlayer(mercat.getJugador(index));
                equip.id = id_equips;
                id_equips++;
            }
        } else {

            int val_agafar = 0;

            int t_index = -1;

            // In the case we pick the player
            if (equip.playerFits(mercat.getJugador(index)) && equip.isRepeated(mercat.getJugador(index))) {
                synchronized (lock) {
                    for (int i = 0; i < n_threads; i++) {
                        if (!threads_act[i]) {
                            t_index = i;
                            break;
                        }
                    }
                    if (t_index != -1) {
                        threads_act[t_index] = true;
                    }
                }
                if (t_index != -1) {
                    threads_arr[t_index] = new ManfutcThreads(mercat, equip, index, t_index);
                    threads_arr[t_index].start();
                } else {
                    agafar_equip.id = equip.id;
                    agafar_equip.valor = equip.valor + (mercat.getJugador(index).valor);
                    agafar_equip.cost = equip.cost + (mercat.getJugador(index).preu);
                    agafar_equip.pressupost = equip.pressupost - (mercat.getJugador(index).preu);
                    agafar_equip.jugadorsEquip = equip.jugadorsEquip;
                    agafar_equip.jugadorsEquip.addPlayer(mercat.getJugador(index));
                    agafar_equip = calcularEquipOptim(mercat, agafar_equip, index - 1);
                    val_agafar = agafar_equip.valor;
                }
            }

            // In the case we don't pick the player
            no_agafar_equip.id = equip.id;
            no_agafar_equip.valor = equip.valor;
            no_agafar_equip.cost = equip.cost;
            no_agafar_equip.pressupost = equip.pressupost;
            no_agafar_equip.jugadorsEquip = equip.jugadorsEquip;
            no_agafar_equip = calcularEquipOptim(mercat, no_agafar_equip, index - 1);

            if (t_index != -1) {
                threads_arr[t_index].join();
                agafar_equip = thread_return[t_index];
                threads_act[t_index] = false;
            }

            // We check which equip fits better
            if (val_agafar == 0 || no_agafar_equip.valor > agafar_equip.valor) {
                equip.id = no_agafar_equip.id;
                equip.valor = no_agafar_equip.valor;
                equip.cost = no_agafar_equip.cost;
                equip.pressupost = no_agafar_equip.pressupost;
                equip.jugadorsEquip = no_agafar_equip.jugadorsEquip;
            } else {
                equip.id = agafar_equip.id;
                equip.valor = agafar_equip.valor;
                equip.cost = agafar_equip.cost;
                equip.pressupost = agafar_equip.pressupost;
                equip.jugadorsEquip = agafar_equip.jugadorsEquip;
            }
        }
        return equip;
    }

    public static class ManfutcThreads extends Thread{
        public Mercat mercat;
        public Equip equip;
        public int index;
        public int t_index;

        public ManfutcThreads(Mercat mercat, Equip equip, int index, int t_index) {
            this.mercat = mercat;
            this.equip = equip;
            this.index = index;
            this.t_index = t_index;
        }

        @Override
        public void run() {
            agafar_equip.id = equip.id;
            agafar_equip.valor = equip.valor + (mercat.getJugador(index).valor);
            agafar_equip.cost = equip.cost + (mercat.getJugador(index).preu);
            agafar_equip.pressupost = equip.pressupost - (mercat.getJugador(index).preu);
            agafar_equip.jugadorsEquip = equip.jugadorsEquip;
            agafar_equip.jugadorsEquip.addPlayer(mercat.getJugador(index));
            try {
                agafar_equip = calcularEquipOptim(mercat, agafar_equip, index - 1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread_return[t_index] = agafar_equip;
        }
    }
}