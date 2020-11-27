/* ---------------------------------------------------------------
Práctica 1.
Código fuente: Jugador.java
Grau Informàtica
48051307Y Joel Aumedes Serrano
78103400T Joel Farré Cortés
---------------------------------------------------------------*/

enum TJugador {Porter, Defensa, Migcampista, Davanter}

public class Jugador {
    public int id;
    public String nom;
    public TJugador posicio;
    public int preu;
    public String equip;
    public int valor;

    // Sets Player's id
    public void setId(int id) { this.id = id; }

    // Sets Player's name
    public void setNom(String nom) { this.nom = nom; }

    // Sets Player's position
    public void setPosicio(TJugador posicio) { this.posicio = posicio; }

    // Sets Player's price
    public void setPreu(int preu) { this.preu = preu; }

    // Sets Player's team
    public void setEquip(String equip) { this.equip = equip; }

    // Sets Player's value
    public void setValor(int valor) {
        this.valor = valor;
    }

    // Prints a player
    public String printPlayer() {
        return "---------- JUGADOR ----------" +
                "\nId: " + this.id +
                "\nNom: " + this.nom +
                "\nPosició: " + this.posicio +
                "\nPreu: " + this.preu +
                "\nEquip: " + this.equip +
                "\nValor: " + this.valor;
    }
}