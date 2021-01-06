/* ---------------------------------------------------------------
Práctica 2.
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
        Manfutc.addMessage("\033[38;2;254;75;15mID de l'equip: " + this.id);
        Manfutc.addMessage("Cost total: " + this.cost);
        Manfutc.addMessage("Valor: " + this.valor);
        Manfutc.addMessage("\nPORTERS: ");
        for (Jugador porter : jugadorsEquip.porters) {
            if (porter != null) {
                Manfutc.addMessage("   - " + porter.nom);
            }
        }
        Manfutc.addMessage("\nDEFENSES: ");
        for (Jugador defensa : jugadorsEquip.defenses) {
            if (defensa != null) {
                Manfutc.addMessage("   - " + defensa.nom);
            }
        }
        Manfutc.addMessage("\nCENTRES: ");
        for (Jugador migcampista : jugadorsEquip.migcampistes) {
            if (migcampista != null) {
                Manfutc.addMessage("   - " + migcampista.nom);
            }
        }
        Manfutc.addMessage("\nDAVANTERS: ");
        for (Jugador davanter : jugadorsEquip.davanters) {
            if (davanter != null) {
                Manfutc.addMessage("   - " + davanter.nom);
            }
        }
    }
}