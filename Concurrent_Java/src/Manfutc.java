/* ---------------------------------------------------------------
Práctica 2.
Código fuente: Manfutc.java
Grau Informàtica
48051307Y Joel Aumedes Serrano
78103400T Joel Farré Cortés
---------------------------------------------------------------*/

import java.io.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class Manfutc {

    public static int id_equips = 0;
    public static int n_threads;
    public static int M = 25000;

    public static Mercat mercat;

    public static ManfutcThreads[] threads_arr;
    public static boolean[] threads_act;
    public static Equip[] thread_return;

    public static final Object lock = new Object();

    public static Estadistiques stats;
    public static Estadistiques[] stats_arr;

    public static Semaphore semafor;
    public static Semaphore[] semafor_arr;

    public static Queue<String> message_list = new LinkedList<>();

    // Main function
    public static void main(String[] argvs) throws InterruptedException {

        MessageThreads message_thread = new MessageThreads();
        message_thread.missatge_alive = 1;
        // message_thread.setDaemon(true);
        message_thread.start();

        if (argvs.length < 3) {
            throw new IllegalArgumentException("Error while introducing the arguments: <pressupost>, <nom_mercat>, <n_threads>, <M>.");
        }

        if (argvs.length == 4) {
            M = Integer.parseInt(argvs[3]);
        }

        int pressupost = Integer.parseInt(argvs[0]);

        n_threads = Integer.parseInt(argvs[2]);
        if (n_threads == 1) {
            addMessage("S'ha passat 1 thread, serà com fer-ho de forma seqüencial.");
        } else if (n_threads == 0) {
            addMessage("S'ha passat 0 threads, es farà amb 1.");
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

        stats_arr = new Estadistiques[n_threads + 1];
        for (int i = 0; i < n_threads + 1; i++) {
            stats_arr[i] = new Estadistiques();
            stats_arr[i].combinacions_valides = 0;
            stats_arr[i].combinacions_evaluades = 0;
            stats_arr[i].combinacions_no_valides = 0;
            stats_arr[i].cost_total_valides = 0;
            stats_arr[i].puntuacio_total_valides = 0;
            stats_arr[i].millor_puntuacio = 0;
            stats_arr[i].pitjor_puntuacio = 999999;
        }

        stats = new Estadistiques();
        stats.combinacions_valides = 0;
        stats.combinacions_evaluades = 0;
        stats.combinacions_no_valides = 0;
        stats.cost_total_valides = 0;
        stats.puntuacio_total_valides = 0;
        stats.millor_puntuacio = 0;
        stats.pitjor_puntuacio = 999999;

        semafor = new Semaphore(1);
        semafor_arr = new Semaphore[n_threads + 1];

        mercat = new Mercat();
        JugadorsEquip jugadorsEquip = new JugadorsEquip();
        Equip equip = new Equip(id_equips, 0, 0, pressupost, jugadorsEquip);
        Equip equipOptim;

        // Reads the file (mercatXj.csv)
        try {
            //addMessage("----------\nLlegint el fitxer: " + argvs[1]);
            mercat = LlegirFitxerJugadors(argvs[1]);
        } catch (Exception e) {
            addMessage("ERROR: Error reading the file.");
        }

        // Calculates the best team
        equipOptim = calcularEquipOptim(equip, (mercat.NJugadors) - 1, 0);
        equipOptim.printTeam();

        message_thread.missatge_alive = 0;
        synchronized (lock) {
            lock.notify();
        }
        message_thread.join();
    }

    // Reads the file entered by parameter
    public static Mercat LlegirFitxerJugadors(String fitxer) {
        Mercat mercat = new Mercat();
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(fitxer);

        } catch (FileNotFoundException e) {
            addMessage("ERROR: File not found.");
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
            }
            br.close();
        } catch (IOException e) {
            addMessage("ERROR: Error doing I/O.");
        }
        return mercat;
    }

    // Calculates the best team
    public static Equip calcularEquipOptim(Equip equip, int index, int thread_slot) throws InterruptedException {
        if (index == 0) {
            if (equip.playerFits(mercat.getJugador(index)) && !equip.isRepeated(mercat.getJugador(index))) {
                equip.valor = equip.valor + (mercat.getJugador(index).valor);
                equip.cost = equip.cost + (mercat.getJugador(index).preu);
                equip.pressupost = equip.pressupost - (mercat.getJugador(index).preu);
                equip.jugadorsEquip.addPlayer(mercat.getJugador(index));
                equip.id = id_equips;
                id_equips++;
            } else {
                stats_arr[thread_slot].inc_combinacions_no_valides();
                stats_arr[thread_slot].inc_combinacions_evaluades();

                if ((stats_arr[thread_slot].combinacions_evaluades % M) == 0) {
                    stats_arr[thread_slot].inc_etapa();
                }

                semafor.acquire();
                stats.inc_combinacions_no_valides();
                stats.inc_combinacions_evaluades();
                if (needToPrint()) {
                    printAllStates();
                }
                semafor.release();
            }

            if (equip.jugadorsEquip.numPorters + equip.jugadorsEquip.numDefenses + equip.jugadorsEquip.numMigcampistes + equip.jugadorsEquip.numDavanters == 7) {
                semafor.acquire();
                stats.inc_combinacions_valides();
                stats.inc_combinacions_evaluades();
                stats.inc_cost_total_valides(equip.cost);
                stats.inc_puntuacio_total_valides(equip.valor);
                if (equip.valor > stats.millor_puntuacio) {
                    stats.inc_millor_puntuacio(equip.valor);
                } else if (equip.valor < stats.pitjor_puntuacio) {
                    stats.inc_pitjor_puntuacio(equip.valor);
                }
                if (needToPrint()) {
                    printAllStates();
                }
                semafor.release();

                stats_arr[thread_slot].inc_combinacions_valides();
                stats_arr[thread_slot].inc_combinacions_evaluades();

                if ((stats_arr[thread_slot].combinacions_evaluades % M) == 0) {
                    stats_arr[thread_slot].inc_etapa();
                }

                stats_arr[thread_slot].inc_cost_total_valides(equip.cost);
                stats_arr[thread_slot].inc_puntuacio_total_valides(equip.valor);

                if (equip.valor > stats_arr[thread_slot].millor_puntuacio) {
                    stats_arr[thread_slot].inc_millor_puntuacio(equip.valor);
                } else if (equip.valor < stats_arr[thread_slot].pitjor_puntuacio) {
                    stats_arr[thread_slot].inc_pitjor_puntuacio(equip.valor);
                }
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
                    threads_arr[t_index] = new ManfutcThreads(equip, index, t_index, t_index + 1);
                    threads_arr[t_index].setDaemon(true);
                    semafor_arr[t_index] = new Semaphore(0);
                    threads_arr[t_index].start();
                } else {
                    agafar_equip.id = equip.id;
                    agafar_equip.valor = equip.valor + (mercat.getJugador(index).valor);
                    agafar_equip.cost = equip.cost + (mercat.getJugador(index).preu);
                    agafar_equip.pressupost = equip.pressupost - (mercat.getJugador(index).preu);
                    agafar_equip.jugadorsEquip.addPlayer(mercat.getJugador(index));
                    val_agafar = calcularEquipOptim(agafar_equip, index - 1, thread_slot).valor;
                }
            } else {
                stats_arr[thread_slot].inc_combinacions_no_valides();
                stats_arr[thread_slot].inc_combinacions_evaluades();

                if ((stats_arr[thread_slot].combinacions_evaluades % M) == 0) {
                    stats_arr[thread_slot].inc_etapa();
                }

                semafor.acquire();
                stats.inc_combinacions_no_valides();
                stats.inc_combinacions_evaluades();
                if (needToPrint()) {
                    printAllStates();
                }
                semafor.release();
            }

            // In the case we don't pick the player
            no_agafar_equip.id = equip.id;
            no_agafar_equip.valor = equip.valor;
            no_agafar_equip.cost = equip.cost;
            no_agafar_equip.pressupost = equip.pressupost;
            calcularEquipOptim(no_agafar_equip, index - 1, thread_slot);

            if (t_index != -1) {
                semafor_arr[t_index].acquire();
                agafar_equip = thread_return[t_index].copy();
                val_agafar = agafar_equip.valor;
                threads_arr[t_index].join();
                threads_act[t_index] = false;
                threads_arr[t_index] = null;
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
        public int thread_slot;

        public ManfutcThreads(Equip equip, int index, int t_index, int thread_slot) {
            this.equip = equip;
            this.index = index;
            this.t_index = t_index;
            this.thread_slot = thread_slot;
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
                calcularEquipOptim(equip, index - 1, thread_slot);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread_return[t_index] = equip;
            semafor_arr[t_index].release();
        }
    }

    // Message thread's class
    public static class MessageThreads extends Thread{
        // Thread's flag
        int missatge_alive;

        // We override the thread's run function
        @Override
        public void run() {
            while (missatge_alive == 1) {
                synchronized (lock) {
                    while (missatge_alive == 1 && message_list.size() < 100) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    for (int i = 0; i < 100; i++) {
                        if (message_list.isEmpty()) {
                            break;
                        }
                        String element = message_list.remove();
                        if (element != null) {
                                System.out.println(element);
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }

    // Function that adds a message to the queue
    public static void addMessage(String message) {
        synchronized (lock) {
            message_list.add(message);
            lock.notify();
        }
    }

    // Function that prints the stats depending on each thread slot
    public static void printStats(int thread_slot) {
        if (thread_slot < 0) {
            stats.printStats(-1);
        } else {
            stats_arr[thread_slot].printStats(thread_slot);
        }
    }

    // Function that checks if the "etapa" equals one or higher, and that means that the thread has evaluated more than M combinations
    public static boolean needToPrint() {
        for (int i = 0; i < n_threads + 1; i++) {
            if (stats_arr[i].etapa < 1) {
                return false;
            }
        }
        return true;
    }

    // Function that prints all the stats, both thread stats and the global stats
    public static void printAllStates() {
        printStats(-1);
        for (int i = 0; i < n_threads + 1; i++) {
            printStats(i);
            stats_arr[i].etapa--;
        }
        stats.etapa++;
    }
}