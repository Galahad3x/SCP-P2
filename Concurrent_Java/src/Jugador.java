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

    // Set Player's id
    public void setId(int id) { this.id = id; }

    // Set Player's name
    public void setNom(String nom) { this.nom = nom; }

    // Set Player's position
    public void setPosicio(TJugador posicio) { this.posicio = posicio; }

    // Set Player's price
    public void setPreu(int preu) { this.preu = preu; }

    // Set Player's team
    public void setEquip(String equip) { this.equip = equip; }

    // Set Player's value
    public void setValor(int valor) {
        this.valor = valor;
    }

    // Prints the players of the team
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