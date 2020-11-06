enum TJugador {Porter, Defensa, Migcampista, Delanter}

public class Jugador {
    public String nom;
    public int preu;
    public int valor;
    public TJugador posicio;

    public Jugador (String nom, int preu, int valor, TJugador posicio) {
        this.nom = nom;
        this.preu = preu;
        this.valor = valor;
        this.posicio = posicio;
    }
}
