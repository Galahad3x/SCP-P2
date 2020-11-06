import java.io.*;
import java.util.ArrayList;

public class Mercat {
    ArrayList<Jugador> jugadors;

    public Mercat() {
        jugadors = new ArrayList<Jugador>();
    }

    public void LlegirFitxerJugadors(String fitxer) {
        long Offset = 0;
        FileInputStream fis;

        try {
            fis = new FileInputStream(fitxer);

        } catch (FileNotFoundException e) {
            System.err.println("ERROR: File not found");
        }

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            br.readLine();

        } catch (IOException e) {
            System.err.println("ERROR: Error doing I/O");
        }
    }

}
