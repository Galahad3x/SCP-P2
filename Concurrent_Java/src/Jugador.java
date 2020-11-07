enum TJugador {Porter, Defensa, Migcampista, Davanter}

public class Jugador {
    public int id;
    public String nom;
    public TJugador posicio;
    public int preu;
    public String equip;
    public int valor;

    // Get & Set Player's id
    public int getId() { return this.id; }
    public void setId(int id) { this.id = id; }

    // Get & Set Player's name
    public String getNom() {
        return this.nom;
    }
    public void setNom(String nom) { this.nom = nom; }

    // Get & Set Player's position
    public TJugador getPosicio() {
        return this.posicio;
    }
    public void setPosicio(TJugador posicio) { this.posicio = posicio; }

    // Get & Set Player's price
    public int getPreu() {
        return this.preu;
    }
    public void setPreu(int preu) { this.preu = preu; }

    // Get & Set Player's team
    public String getEquip() { return this.equip; }
    public void setEquip(String equip) { this.equip = equip; }

    // Get & Set Player's value
    public int getValor() { return this.valor; }
    public void setValor(int valor) {
        this.valor = valor;
    }

    // toString() function
    public String toString() {
        return "---------- JUGADOR ----------" +
                "\nId: " + this.id +
                "\nNom: " + this.nom +
                "\nPosició: " + this.posicio +
                "\nPreu: " + this.preu +
                "\nEquip: " + this.equip +
                "\nValor: " + this.valor;
    }
}
