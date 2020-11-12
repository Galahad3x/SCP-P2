import java.io.*;

public class Manfutc {

    public static int pressupost;
    public static int n_threads;
    public static Mercat mercat;
    public static Equip equip;
    static int id_equips = 0;

    public static void main(String[] argvs) {
        if (argvs.length != 3) {
            throw new IllegalArgumentException("Error while introduce the arguments: <pressupost>, <nom_mercat>, <n_threads>.");
        }

        pressupost = Integer.parseInt(argvs[0]);
        n_threads = Integer.parseInt(argvs[2]);
        mercat = new Mercat();
        equip = new Equip(0, 0, 0, pressupost, null);

        //Reads the file (mercatXj.csv)
        try {
            System.out.println("---------- Llegint el fitxer: " + argvs[1] + " ----------");
            mercat = LlegirFitxerJugadors(argvs[1]);
        } catch (Exception e) {
            System.out.println("ERROR: Error reading the file.");
        }

        // Calculates the best team
        System.out.println("---------- Calculant l'equip òptim ----------");
        equip = calcularEquipOptim(mercat, equip, (mercat.NJugadors - 1));
        System.out.println("---------- MILLOR EQUIP OBTINGUT ----------");
        equip.printTeam();
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
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            br.readLine();

            mercat.NJugadors = mercat.NPorters = mercat.NDefensors = mercat.NMigcampistes = mercat.NDavanters = 0;

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
                        mercat.NPorters++;
                    }
                    case "Defensa" -> {
                        jugador.setPosicio(TJugador.Defensa);
                        mercat.NDefensors++;
                    }
                    case "Medio" -> {
                        jugador.setPosicio(TJugador.Migcampista);
                        mercat.NMigcampistes++;
                    }
                    case "Delantero" -> {
                        jugador.setPosicio(TJugador.Davanter);
                        mercat.NDavanters++;
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
        if (index == 0) {
            if (equip.fitPlayer(mercat.getJugador(index))) {
                equip.jugadorsEquip.addPlayer(mercat.getJugador(index));
                equip.id = id_equips;
                id_equips++;
            }
        } else {
            int val_no_agafar, val_agafar = 0;
            JugadorsEquip no_agafar = new JugadorsEquip();
            JugadorsEquip agafar = new JugadorsEquip();
            Equip no_agafar_equip = new Equip(0, 0, 0, 0, null);
            Equip agafar_equip = new Equip(0, 0, 0, 0, null);


            for (int i = 0; i < (equip.jugadorsEquip.MAX_PORTERS + equip.jugadorsEquip.MAX_DEFENSES + equip.jugadorsEquip.MAX_MIGCAMPISTES + equip.jugadorsEquip.MAX_DAVANTERS); i++) {
                no_agafar.addPlayer(equip.jugadorsEquip.getPlayer(i));
            }
            no_agafar_equip.valor = equip.valor;
            no_agafar_equip.cost = equip.cost;
            no_agafar_equip.pressupost = equip.pressupost;
            no_agafar_equip.jugadorsEquip = no_agafar;
            val_no_agafar = calcularEquipOptim(mercat, no_agafar_equip, index - 1).valor;
        }
        return equip;
    }
}
