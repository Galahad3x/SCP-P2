enum TJugador {Porter, Defensa, Migcampista, Davanter}

public class Jugador {
    public String nom;
    public int id;
    public int preu;
    public int valor;
    public TJugador posicio;

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

    public int getValor() { return this.valor; }

    public void setValor(int valor) {
        this.valor = valor;
    }

    public TJugador getPosicio() {
        return this.posicio;
    }

    public void setPosicio(TJugador posicio) {
        this.posicio = posicio;
    }

    public String toString() {
        return "Nom: " + this.nom +
                "\nId: " + this.id +
                "\nPreu: " + this.preu +
                "\nValor: " + this.valor +
                "\nPosició: " + this.posicio;
    }
}
