import java.io.*;

public class Manfutc {

    public static int pressupost;
    public static int n_threads;
    public static Mercat mercat = new Mercat();
    public static JugadorsEquip jugadorsEquip = new JugadorsEquip();
    public static Equip equip;
    public static Equip equipOptim = new Equip(0, 0, 0, 0, jugadorsEquip);

    static int id_equips = 0;
    public static int val_no_agafar, val_agafar = 0;
    public static JugadorsEquip no_agafar = new JugadorsEquip();
    public static Equip no_agafar_equip = new Equip(0, 0, 0, 0, jugadorsEquip);
    public static JugadorsEquip agafar = new JugadorsEquip();
    public static Equip agafar_equip = new Equip(0, 0, 0, 0, jugadorsEquip);

    public static void main(String[] argvs) {
        if (argvs.length != 3) {
            throw new IllegalArgumentException("Error while introduce the arguments: <pressupost>, <nom_mercat>, <n_threads>.");
        }
        pressupost = Integer.parseInt(argvs[0]);
        n_threads = Integer.parseInt(argvs[2]);

        //Reads the file (mercatXj.csv)
        try {
            System.out.println("---------- Llegint el fitxer: " + argvs[1] + " ----------");
            mercat = LlegirFitxerJugadors(argvs[1]);
        } catch (Exception e) {
            System.out.println("ERROR: Error reading the file.");
        }
        // Calculates the best team
        System.out.println("---------- Calculant l'equip òptim ----------");
        equip = new Equip(0, 0, 0, pressupost, jugadorsEquip);
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
                    case "Portero" -> {
                        jugador.setPosicio(TJugador.Porter);
                    }
                    case "Defensa" -> {
                        jugador.setPosicio(TJugador.Defensa);
                    }
                    case "Medio" -> {
                        jugador.setPosicio(TJugador.Migcampista);
                    }
                    case "Delantero" -> {
                        jugador.setPosicio(TJugador.Davanter);
                    }
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
    public static Equip calcularEquipOptim(Mercat mercat, Equip equip, int index) {
        System.out.println("Jugador: " + mercat.getJugador(index).nom + "\n");
        if (index == 0) {
            if (equip.playerFits(mercat.getJugador(index))) {
                equip.jugadorsEquip.addPlayer(mercat.getJugador(index));
                equip.id = id_equips;
                id_equips++;
            }
        } else {
            // In the case we don't pick the player
            System.out.println("No agafem el jugador. \n");
            no_agafar_equip.id = equip.id;
            no_agafar_equip.valor = equip.valor;
            no_agafar_equip.cost = equip.cost;
            no_agafar_equip.pressupost = equip.pressupost;
            no_agafar_equip.jugadorsEquip = no_agafar;
            val_no_agafar = calcularEquipOptim(mercat, no_agafar_equip, index - 1).valor;

            // In the case we pick the player
            if (equip.playerFits(mercat.getJugador(index)) && !equip.isRepeated(mercat.getJugador(index))) {
                System.out.println("Agafem el jugador \n");
                agafar_equip.id = equip.id;
                agafar_equip.valor = equip.valor + mercat.getJugador(index).valor;
                agafar_equip.cost = equip.cost + mercat.getJugador(index).preu;
                agafar_equip.pressupost = (equip.pressupost - mercat.getJugador(index).preu);
                agafar_equip.jugadorsEquip.addPlayer(mercat.getJugador(index));
                val_agafar = calcularEquipOptim(mercat, agafar_equip, index - 1).valor;
            }

            // We check which equip fits better
            if (val_agafar == 0 || val_no_agafar > val_agafar) {
                equip.valor = no_agafar_equip.valor;
                equip.cost = no_agafar_equip.cost;
                equip.pressupost = no_agafar_equip.pressupost;
                equip.jugadorsEquip = no_agafar_equip.jugadorsEquip;
            } else {
                equip.valor = agafar_equip.valor;
                equip.cost = agafar_equip.cost;
                equip.pressupost = agafar_equip.pressupost;
                equip.jugadorsEquip = agafar_equip.jugadorsEquip;
            }
        }
        return equip;
    }

    public static class ManfutcThreads extends Thread{
        public int index;

        public ManfutcThreads(int index) {
            this.index = index;
        }

        @Override
        public void run() {

        }
    }
}