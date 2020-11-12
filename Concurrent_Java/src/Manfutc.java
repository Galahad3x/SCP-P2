import java.io.*;

public class Manfutc {
    public static void main(String[] argvs) {
        if (argvs.length != 3) {
            throw new IllegalArgumentException("Error while introduce the arguments: <pressupost>, <nom_mercat>, <n_threads>.");
        }

        int pressupost = Integer.parseInt(argvs[0]);
        int n_threads = Integer.parseInt(argvs[2]);
        Mercat mercat = new Mercat();
        Equip equip = new Equip(0, 0, 0, pressupost, null);

        //Reads the file (mercatXj.csv)
        try {
            System.out.println("---------- Llegint el fitxer: " + argvs[1] + " ----------");
            mercat = LlegirFitxerJugadors(argvs[1]);
        } catch (Exception e) {
            System.out.println("ERROR: Error reading the file.");
        }

        // Calculates the best team
        System.out.println("---------- Calculant l'equip òptim ----------");
        equip = calcularEquipOptim(pressupost, n_threads, mercat, equip, (mercat.NJugadors - 1));
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
    public static Equip calcularEquipOptim(int pressupost, int n_threads, Mercat mercat, Equip equip, int index) {
        if (index == 0) {
            if (equip.fitPlayer(mercat.getJugador(index))) {
                equip.jugadorsEquip.addPlayer(mercat.getJugador(index));
            }
        } else {
            
        }
        return equip;
    }
}
