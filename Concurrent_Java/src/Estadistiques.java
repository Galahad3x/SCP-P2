/* ---------------------------------------------------------------
Práctica 2.
Código fuente: Estadistiques.java
Grau Informàtica
48051307Y Joel Aumedes Serrano
78103400T Joel Farré Cortés
---------------------------------------------------------------*/

public class Estadistiques {
    public int combinacions_valides;
    public int combinacions_evaluades;
    public int combinacions_no_valides;
    public int cost_total_valides;
    public int puntuacio_total_valides;
    public int millor_puntuacio;
    public int pitjor_puntuacio;
    public int etapa;

    // Increments the number of valid combinations
    public void inc_combinacions_valides() {
        this.combinacions_valides += 1;
    }

    // Increments the number of combinations evaluated
    public void inc_combinacions_evaluades() {
        this.combinacions_evaluades += 1;
    }

    // Increments the number of non-valid combinations
    public void inc_combinacions_no_valides() {
        this.combinacions_no_valides += 1;
    }

    // Adds the cost of a combination to the total cost
    public void inc_cost_total_valides(int cost) {
        this.cost_total_valides += cost;
    }

    // Adds the value of a combination to the total value
    public void inc_puntuacio_total_valides(int valor) {
        this.puntuacio_total_valides += valor;
    }

    // Sets the best combination's value
    public void inc_millor_puntuacio(int valor) {
        millor_puntuacio = valor;
    }

    // Sets the worst combination's value
    public void inc_pitjor_puntuacio(int valor) {
        pitjor_puntuacio = valor;
    }

    // Increases the number of the stage
    public void inc_etapa() {
        this.etapa++;
    }

    // Prints the stats, globals or each thread stats, depending on the thread slot value
    public void printStats(int thread_slot) {
        if ( thread_slot < 0){
            Manfutc.addMessage("\033[38;2;52;246;119m============= Parcials Globals =============" + "\nVàlides totals: " + combinacions_valides +
                    " No vàlides totals: " + combinacions_no_valides
                    + " Totals: " + combinacions_evaluades + "\nMillor puntuació: " + millor_puntuacio +
                    " Pitjor puntuació: " + pitjor_puntuacio + "\nCost mitjà: " + (cost_total_valides / combinacions_valides)
                    + " Puntuació mitjana: " + (puntuacio_total_valides / combinacions_valides) + "\n-------------------------------------------");
        }else{
            Manfutc.addMessage("\033[38;2;123;246;2m============= Parcials Slot " + thread_slot + " =============" + "\nVàlides totals: " + combinacions_valides +
                    " No vàlides totals: " + combinacions_no_valides
                    + " Totals: " + combinacions_evaluades + "\nMillor puntuació: " + millor_puntuacio +
                    " Pitjor puntuació: " + pitjor_puntuacio + "\nCost mitjà: " + (cost_total_valides / combinacions_valides)
                    + " Puntuació mitjana: " + (puntuacio_total_valides / combinacions_valides) + "\n-------------------------------------------");
        }
    }
}