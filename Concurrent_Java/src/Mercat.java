public class Mercat {
    int NJugadors;
    int NPorters;
    int NDefensors;
    int NMigcampistes;
    int NDavanters;
    Jugador[] jugadors;

    public Mercat() {
        this.jugadors = new Jugador[50];
    }

    // Gets a player in a specific position
    public Jugador getJugador(int index) {
        return this.jugadors[index];
    }
}