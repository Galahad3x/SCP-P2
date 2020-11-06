enum TJugador {Porter, Defensa, Migcampista, Delanter}

public class Jugador {
    public String nom;
    public int id;
    public int preu;
    public int valor;
    public TJugador posicio;

    public Jugador (String nom,int id, int preu, int valor, TJugador posicio) {
        this.nom = nom;
        this.id = id;
        this.preu = preu;
        this.valor = valor;
        this.posicio = posicio;
    }

    public String getName() {
        return this.nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPreu() {
        return this.preu;
    }

    public void setPreu(int preu) {
        this.preu = preu;
    }

    public int getValor() {
        return this.valor;
    }

    public void setValor(int valor) {
        this.valor = valor;
    }

    public TJugador getPosicio() {
        return this.posicio;
    }

    public void setPosicio(TJugador posicio) {
        this.posicio = posicio;
    }
}
