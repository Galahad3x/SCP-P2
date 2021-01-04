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

    public void inc_combinacions_valides() { this.combinacions_valides++; }
    public void inc_combinacions_evaluades() { this.combinacions_evaluades++; }
    public void inc_combinacions_no_valides() { this.combinacions_no_valides++; }
    public void inc_cost_total_valides(int cost) { this.cost_total_valides += cost; }
    public void inc_puntuacio_total_valides(int valor) { this.puntuacio_total_valides += valor; }
    public void inc_millor_puntuacio(int valor) { millor_puntuacio = valor; }
    public void inc_pitjor_puntuacio(int valor) { pitjor_puntuacio = valor; }

    public void printStats() {
        System.out.println("Vàlides totals: " + combinacions_valides + " No vàlides totals: " + combinacions_no_valides + " Totals: " + combinacions_evaluades);
        System.out.println("Millor puntuació: " + millor_puntuacio + " Pitjor puntuació: " + pitjor_puntuacio);
        System.out.println("Cost mitjà: " + (cost_total_valides / combinacions_valides) + " Puntuació mitjana: " + (puntuacio_total_valides / combinacions_valides));
    }
}
