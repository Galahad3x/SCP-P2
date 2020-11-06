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
        jugadors = new ArrayList<Jugador>();
    }

    /***
    Jugador GetJugador (int i) { return jugadors.get(i); }
    Jugador GetPorter (int i) { return jugadors.get(i); }
    Jugador GetDefensor (int i) { return jugadors.get(i); }
    Jugador GetMigcampista (int i) { return jugadors.get(i); }
    Jugador GetDavanter (int i) {return jugadors.get(i); }
     ***/

    public void LlegirFitxerJugadors(String fitxer) {
        long Offset = 0;
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
            String line = null;
            while ((line = br.readLine()) != null) {
                String field[] = line.split(";");
                Jugador jugador = new Jugador();

                jugador.setId(Integer.parseInt(field[0]));

                jugador.setNom(field[1]);

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

                jugador.setPreu(Integer.parseInt(field[3]));

                jugador.setValor(Integer.parseInt(field[4]));

                NJugadors++;
                jugadors.add(jugador);
            }
            br.close();

        } catch (IOException e) {
            System.err.println("ERROR: Error doing I/O.");
        }
    }

}
