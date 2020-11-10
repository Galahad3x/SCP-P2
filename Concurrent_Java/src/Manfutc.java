public class Manfutc {
    //1- pressupost, 2- mercat, 3- n_threads
    public static void main(String[] argvs) {
        if (argvs.length != 3) {
            throw new IllegalArgumentException("Error while introduce the arguments: <pressupost>, <nom_mercat>, <n_threads>.");
        }

        int pressupost = Integer.parseInt(argvs[0]);
        Mercat mercat = new Mercat();
        int n_treads = Integer.parseInt(argvs[2]);

        //Read file (mercatXj.csv)
        try {
            mercat.LlegirFitxerJugadors(argvs[1]);

        } catch (Exception e) {
            System.out.println("ERROR: Error reading the file.");
        }

        // Calculate best team
        mercat.calcularEquipOptim(pressupost);
    }
}
