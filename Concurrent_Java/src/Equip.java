/* ---------------------------------------------------------------
Práctica 1.
Código fuente: Equip.java
Grau Informàtica
48051307Y Joel Aumedes Serrano
78103400T Joel Farré Cortés
---------------------------------------------------------------*/

public class Equip {
    public int id;
    public int valor;
    public int cost;
    public int pressupost;
    public JugadorsEquip jugadorsEquip;

    // Team's constructor
    public Equip(int id, int valor, int cost, int pressupost, JugadorsEquip jugadorsEquip) {
        this.id = id;
        this.valor = valor;
        this.cost = cost;
        this.pressupost = pressupost;
        this.jugadorsEquip = jugadorsEquip;
    }

    // Checks if a player can fit in a team
    public boolean playerFits(Jugador jugador) {
        if (jugador.preu > pressupost) {
            return false;
        } else {
            switch (jugador.posicio) {
                case Porter:
                    return jugadorsEquip.numPorters < jugadorsEquip.MAX_PORTERS;
                case Defensa:
                    return jugadorsEquip.numDefenses < jugadorsEquip.MAX_DEFENSES;
                case Migcampista:
                    return jugadorsEquip.numMigcampistes < jugadorsEquip.MAX_MIGCAMPISTES;
                case Davanter:
                    return jugadorsEquip.numDavanters < jugadorsEquip.MAX_DAVANTERS;
                default:
                    return false;
            }
        }
    }

    // Checks if there's a repeated player in a team
    public boolean isRepeated(Jugador jugador) {
        if (jugador != null) {
            for (int i = 0; i < jugadorsEquip.MAX_JUGADORS; i++) {
                if (jugador == jugadorsEquip.getPlayer(i)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Creates a deep copy of the team, including its JugadorsEquip
    public Equip copy(){
        return new Equip(this.id, this.valor, this.cost, this.pressupost, this.jugadorsEquip.copy());
    }

    // Prints the parameters of the team
    public void printTeam() {
        System.out.println("ID de l'equip: " + this.id);
        System.out.println("Valor de l'equip: " + this.valor);
        System.out.println("Cost de l'equip: " + this.cost);
        System.out.println("Jugadors de l'equip òpim: ");
        System.out.println("Porters: ");
        for (Jugador porter : jugadorsEquip.porters) {
            if (porter != null) {
                System.out.println("   - " + porter.nom);
            }
        }
        System.out.println("Defenses: ");
        for (Jugador defensa : jugadorsEquip.defenses) {
            if (defensa != null) {
                System.out.println("   - " + defensa.nom);
            }
        }
        System.out.println("Migcampistes: ");
        for (Jugador migcampista : jugadorsEquip.migcampistes) {
            if (migcampista != null) {
                System.out.println("   - " + migcampista.nom);
            }
        }
        System.out.println("Davanters: ");
        for (Jugador davanter : jugadorsEquip.davanters) {
            if (davanter != null) {
                System.out.println("   - " + davanter.nom);
            }
        }
    }
}