public class Mercat {
    int NJugadors;
    Jugador[] jugadors;

    public Mercat() {
        this.jugadors = new Jugador[50];
    }

    // Gets a player in a specific position
    public Jugador getJugador(int index) {
        return this.jugadors[index];
    }
}