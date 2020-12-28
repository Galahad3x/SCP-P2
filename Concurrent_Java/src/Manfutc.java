/* ---------------------------------------------------------------
Práctica 1.
Código fuente: Manfutc.java
Grau Informàtica
48051307Y Joel Aumedes Serrano
78103400T Joel Farré Cortés
---------------------------------------------------------------*/

import java.io.*;

public class Manfutc {

    public static int id_equips = 0;
    public static int n_threads;

    public static Mercat mercat;

    public static ManfutcThreads[] threads_arr;
    public static boolean[] threads_act;
    public static Equip[] thread_return;

    public static int lock = 0;

    // Main function
    public static void main(String[] argvs) throws InterruptedException {
        if (argvs.length != 3) {
            throw new IllegalArgumentException("Error while introducing the arguments: <pressupost>, <nom_mercat>, <n_threads>.");
        }
        int pressupost = Integer.parseInt(argvs[0]);
        n_threads = Integer.parseInt(argvs[2]);
        if(n_threads == 1){
            System.out.println("Passat 1 thread, serà com fer-ho seqüencial.");
        }else if(n_threads == 0){
            System.out.println("Passats 0 threads, es farà amb 1.");
            n_threads = 1;
        }
        n_threads -= 1;

        threads_arr = new ManfutcThreads[n_threads];
        threads_act = new boolean[n_threads];
        thread_return = new Equip[n_threads];

        for (int i = 0; i < n_threads; i++) {
            threads_arr[i] = null;
            threads_act[i] = false;
            thread_return[i] = null;
        }

        mercat = new Mercat();
        JugadorsEquip jugadorsEquip = new JugadorsEquip();
        Equip equip = new Equip(id_equips, 0, 0, pressupost, jugadorsEquip);
        Equip equipOptim;

        // Reads the file (mercatXj.csv)
        try {
            System.out.println("----------\nLlegint el fitxer: " + argvs[1]);
            // System.out.println("----------\nLlista de jugadors del mercat:");
            mercat = LlegirFitxerJugadors(argvs[1]);
        } catch (Exception e) {
            System.out.println("ERROR: Error reading the file.");
        }
        // Calculates the best team
        System.out.println("----------\nCalculant l'equip òptim...");
        equipOptim = calcularEquipOptim(equip, (mercat.NJugadors) - 1);
        System.out.println("---------- MILLOR EQUIP OBTINGUT ----------");
        equipOptim.printTeam();
    }

    // Reads the file entered by parameter
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

                // Creates a new player to fill it
                Jugador jugador = new Jugador();

                // Sets the player's id
                jugador.setId(Integer.parseInt(field[0]));

                // Sets the player's name
                jugador.setNom(field[1]);

                // Sets the player's position
                if (field[2].equals("Portero")) {
                    jugador.setPosicio(TJugador.Porter);
                } else if(field[2].equals("Defensa")) {
                    jugador.setPosicio(TJugador.Defensa);
                } else if(field[2].equals("Medio")) {
                    jugador.setPosicio(TJugador.Migcampista);
                } else {
                    jugador.setPosicio(TJugador.Davanter);
                }
                // Sets the player's price
                jugador.setPreu(Integer.parseInt(field[3]));

                // Sets the player's team
                jugador.setEquip(field[4]);

                // Sets the player's value
                jugador.setValor(Integer.parseInt(field[5]));

                // Adds the readed player into the market
                mercat.jugadors[mercat.NJugadors] = jugador;
                mercat.NJugadors++;
                // System.out.println(jugador.printPlayer());
            }
            br.close();
        } catch (IOException e) {
            System.err.println("ERROR: Error doing I/O.");
        }
        return mercat;
    }

    // Calculates the best team
    public static Equip calcularEquipOptim(Equip equip, int index) throws InterruptedException {
        if (index == 0) {
            if (equip.playerFits(mercat.getJugador(index)) && !equip.isRepeated(mercat.getJugador(index))) {
                equip.valor = equip.valor + (mercat.getJugador(index).valor);
                equip.cost = equip.cost + (mercat.getJugador(index).preu);
                equip.pressupost = equip.pressupost - (mercat.getJugador(index).preu);
                equip.jugadorsEquip.addPlayer(mercat.getJugador(index));
                equip.id = id_equips;
                id_equips++;
            }
        } else {
            JugadorsEquip no_agafar = equip.jugadorsEquip.copy();
            Equip no_agafar_equip = new Equip(equip.id, equip.valor, equip.cost, equip.pressupost, no_agafar);
            JugadorsEquip agafar = equip.jugadorsEquip.copy();
            Equip agafar_equip = new Equip(equip.id, equip.valor, equip.cost, equip.pressupost, agafar);

            int val_agafar = 0;

            int t_index = -1;

            // In the case we pick the player
            if (equip.playerFits(mercat.getJugador(index)) && !equip.isRepeated(mercat.getJugador(index))) {
                while (lock == 1) {
                    Thread.sleep(200);
                }
                lock = 1;
                for (int i = 0; i < n_threads; i++) {
                    if (!threads_act[i]) {
                        t_index = i;
                        break;
                    }
                }
                if (t_index != -1) {
                    threads_act[t_index] = true;
                }
                lock = 0;
                if (t_index != -1) {
                    threads_arr[t_index] = new ManfutcThreads(equip, index, t_index);
                    threads_arr[t_index].setDaemon(true);
                    threads_arr[t_index].start();
                } else {
                    agafar_equip.id = equip.id;
                    agafar_equip.valor = equip.valor + (mercat.getJugador(index).valor);
                    agafar_equip.cost = equip.cost + (mercat.getJugador(index).preu);
                    agafar_equip.pressupost = equip.pressupost - (mercat.getJugador(index).preu);
                    agafar_equip.jugadorsEquip.addPlayer(mercat.getJugador(index));
                    // CalcularEquipOptim modifies agafar_equip
                    val_agafar = calcularEquipOptim(agafar_equip, index - 1).valor;
                }
            }

            // In the case we don't pick the player
            no_agafar_equip.id = equip.id;
            no_agafar_equip.valor = equip.valor;
            no_agafar_equip.cost = equip.cost;
            no_agafar_equip.pressupost = equip.pressupost;
            calcularEquipOptim(no_agafar_equip, index - 1);

            if (t_index != -1) {
                threads_arr[t_index].join();
                agafar_equip = thread_return[t_index].copy();
                val_agafar = agafar_equip.valor;
                threads_arr[t_index] = null;
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

    // Thread's class
    public static class ManfutcThreads extends Thread{
        public Equip equip;
        public int index;
        public int t_index;

        public ManfutcThreads(Equip equip, int index, int t_index) {
            this.equip = equip;
            this.index = index;
            this.t_index = t_index;
        }

        // We override the thread's run function
        @Override
        public void run() {
            equip.id = equip.id;
            equip.valor = equip.valor + (mercat.getJugador(index).valor);
            equip.cost = equip.cost + (mercat.getJugador(index).preu);
            equip.pressupost = equip.pressupost - (mercat.getJugador(index).preu);
            equip.jugadorsEquip.addPlayer(mercat.getJugador(index));
            try {
                calcularEquipOptim(equip, index - 1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread_return[t_index] = equip;
        }
    }
}