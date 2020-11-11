public class Equip {
    public int id;
    public int valor;
    public int cost;
    public int pressupost;
    public JugadorsEquip jugadorsEquip;

    public Equip (int id, int valor, int cost, int pressupost, JugadorsEquip jugadorsEquip) {
        this.id = id;
        this.valor = valor;
        this.cost = cost;
        this.pressupost = pressupost;
        this.jugadorsEquip = jugadorsEquip;
    }

    // Checks if a player can fit in a team
    public boolean fitPlayer(Jugador jugador) {
        if (jugador.preu > pressupost) {
            return false;
        } else {
            switch (jugador.posicio) {
                case Porter -> {
                    return jugadorsEquip.porters.length < jugadorsEquip.MAX_PORTERS;
                }
                case Defensa -> {
                    return jugadorsEquip.defenses.length < jugadorsEquip.MAX_DEFENSES;
                }
                case Migcampista -> {
                    return jugadorsEquip.migcampistes.length < jugadorsEquip.MAX_MIGCAMPISTES;
                }
                case Davanter -> {
                    return jugadorsEquip.davanters.length < jugadorsEquip.MAX_DAVANTERS;
                }
                default -> {
                    return false;
                }
            }
        }
    }

    /***
    // Checks if there's a repeated player
    public boolean repeatPlayer(Jugador jugador) {
        for (Jugador porter : jugadorsEquip.porters) {
            return porter == jugador;
        }
        for (Jugador defensa : jugadorsEquip.defenses) {
            return defensa == jugador;
        }
        for (Jugador migcampista : jugadorsEquip.migcampistes) {
            return migcampista == jugador;
        }
        for (Jugador davanter : jugadorsEquip.davanters) {
            return davanter == jugador;
        }
        return false;
    }
     ***/

    /***
    // Calculates the cost of the team
    public int getCost() {
        int cost = 0;
        int i;
        for (i = 0; i < jugadorsEquip.porters.length; i++) {
            cost += jugadorsEquip.porters[i].preu;
        }
        for (i = 0; i < jugadorsEquip.defenses.length; i++) {
            cost += jugadorsEquip.defenses[i].preu;
        }
        for (i = 0; i < jugadorsEquip.migcampistes.length; i++) {
            cost += jugadorsEquip.migcampistes[i].preu;
        }
        for (i = 0; i < jugadorsEquip.davanters.length; i++) {
            cost += jugadorsEquip.davanters[i].preu;
        }
        return cost;
    }
     ***/

    /***
    // Calculates the value of the team
    public int getValue() {
        int value = 0;
        int i;
        for (i = 0; i < jugadorsEquip.porters.length; i++) {
            value += jugadorsEquip.porters[i].valor;
        }
        for (i = 0; i < jugadorsEquip.defenses.length; i++) {
            value += jugadorsEquip.defenses[i].valor;
        }
        for (i = 0; i < jugadorsEquip.migcampistes.length; i++) {
            value += jugadorsEquip.migcampistes[i].valor;
        }
        for (i = 0; i < jugadorsEquip.davanters.length; i++) {
            value += jugadorsEquip.davanters[i].valor;
        }
        return value;
    }
     ***/

    // Prints the players of the team
    public void printTeam() {
        System.out.println("Porters: ");
        for (Jugador porter : jugadorsEquip.porters) {
            System.out.println(porter.toString());
        }
        System.out.println("Defenses: ");
        for (Jugador defensa : jugadorsEquip.defenses) {
            System.out.println(defensa.toString());
        }
        System.out.println("Migcampistes: ");
        for (Jugador migcampista : jugadorsEquip.migcampistes) {
            System.out.println(migcampista.toString());
        }
        System.out.println("Davanters: ");
        for (Jugador davanter : jugadorsEquip.davanters) {
            System.out.println(davanter.toString());
        }
        System.out.println("Cost: " + cost);
        System.out.println("Puntuació: " + valor);
    }
}