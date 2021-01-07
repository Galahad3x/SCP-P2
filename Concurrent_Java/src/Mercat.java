/* ---------------------------------------------------------------
Práctica 2.
Código fuente: Mercat.java
Grau Informàtica
48051307Y Joel Aumedes Serrano
78103400T Joel Farré Cortés
---------------------------------------------------------------*/

public class Mercat {
    int NJugadors;
    Jugador[] jugadors;

    // Mercat's constructor
    public Mercat() {
        this.jugadors = new Jugador[100];
    }

    // Gets a player from the market in a specific position
    public Jugador getJugador(int index) {
        return this.jugadors[index];
    }
}