import java.io.*;
import java.util.ArrayList;

public class Mercat {
    ArrayList<Jugador> jugadors;
    int NJugadors;
    int NPorters;
    int NDefensors;
    int NMigcampistes;
    int NDavanters;

    public Mercat() {
        jugadors = new ArrayList<>();
    }

    Jugador GetJugador (int i) { return jugadors.get(i); }
    Jugador GetPorter (int i) { return jugadors.get(i); }
    Jugador GetDefensor (int i) { return jugadors.get(i + NPorters); }
    Jugador GetMigcampista (int i) { return jugadors.get(i + NPorters + NDefensors); }
    Jugador GetDavanter (int i) { return jugadors.get(i + NPorters + NDefensors + NMigcampistes); }

    public void LlegirFitxerJugadors(String fitxer) {
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(fitxer);

        } catch (FileNotFoundException e) {
            System.err.println("ERROR: File not found.");
        }

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            br.readLine();

            NJugadors = NPorters = NDefensors = NMigcampistes = NDavanters = 0;

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
                        NPorters++;
                    }
                    case "Defensa" -> {
                        jugador.setPosicio(TJugador.Defensa);
                        NDefensors++;
                    }
                    case "Medio" -> {
                        jugador.setPosicio(TJugador.Migcampista);
                        NMigcampistes++;
                    }
                    case "Delantero" -> {
                        jugador.setPosicio(TJugador.Davanter);
                        NDavanters++;
                    }
                    default -> System.err.println("ERROR: Invalid player type.");
                }
                // Setting player's price
                jugador.setPreu(Integer.parseInt(field[3]));

                // Setting player's team
                jugador.setEquip(field[4]);

                // Setting player's value
                jugador.setValor(Integer.parseInt(field[5]));

                NJugadors++;
                jugadors.add(jugador);
                System.out.println(jugador.toString());
            }
            br.close();

        } catch (IOException e) {
            System.err.println("ERROR: Error doing I/O.");
        }
    }

    public void calcularEquipOptim(int pressupost) {

    }
}
