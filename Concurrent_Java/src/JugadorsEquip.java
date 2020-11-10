import java.util.ArrayList;

public class JugadorsEquip {
    public ArrayList<Jugador> porters;
    public ArrayList<Jugador> defenses;
    public ArrayList<Jugador> migcampistes;
    public ArrayList<Jugador> davanters;

    public JugadorsEquip (ArrayList<Jugador> porters, ArrayList<Jugador> defenses, ArrayList<Jugador> migcampistes, ArrayList<Jugador> davanters) {
        this.porters = porters;
        this.defenses = defenses;
        this.migcampistes = migcampistes;
        this.davanters = davanters;
    }

    // Get & Set Goalkeepers
    public ArrayList<Jugador> getPorters() { return this.porters; }
    public void setPorters(Jugador porter) { this.porters.add(porter); }

    // Get & Set Defenders
    public ArrayList<Jugador> getDefenses() { return this.defenses; }
    public void setDefenses(Jugador defensa) { this.defenses.add(defensa); }

    // Get & Set Midfielders
    public ArrayList<Jugador> getMigcampistes() { return this.migcampistes; }
    public void setMigcampistes(Jugador migcampista) { this.migcampistes.add(migcampista); }

    // Get & Set Attackers
    public ArrayList<Jugador> getDavanters() { return this.davanters; }
    public void setDavanters(Jugador davanter) { this.davanters.add(davanter); }

    // Checks if there's a repeated player
    public boolean repeatPlayer(Jugador jugador) {
        return  (porters.contains(jugador) || defenses.contains(jugador) || migcampistes.contains(jugador) || davanters.contains(jugador));
    }

    // Calculates the cost of the team
    public int getCost() {
        int cost = 0;
        int i;
        for (i = 0; i < porters.size(); i++) {
            cost += porters.get(i).preu;
        }
        for (i = 0; i < defenses.size(); i++) {
            cost += defenses.get(i).preu;
        }
        for (i = 0; i < migcampistes.size(); i++) {
            cost += migcampistes.get(i).preu;
        }
        for (i = 0; i < davanters.size(); i++) {
            cost += davanters.get(i).preu;
        }
        return cost;
    }

    // Calculates the value of the team
    public int getValue() {
        int value = 0;
        int i;
        for (i = 0; i < porters.size(); i++) {
            value += porters.get(i).valor;
        }
        for (i = 0; i < defenses.size(); i++) {
            value += defenses.get(i).valor;
        }
        for (i = 0; i < migcampistes.size(); i++) {
            value += migcampistes.get(i).valor;
        }
        for (i = 0; i < davanters.size(); i++) {
            value += davanters.get(i).valor;
        }
        return value;
    }

    // toString() function
    public void printTeam() {
        System.out.println("Porters: ");
        for (Jugador porter : porters) {
            System.out.println(porter.toString());
        }
        System.out.println("Defenses: ");
        for (Jugador defensa : defenses) {
            System.out.println(defensa.toString());
        }
        System.out.println("Migcampistes: ");
        for (Jugador migcampista : migcampistes) {
            System.out.println(migcampista.toString());
        }
        System.out.println("Davanters: ");
        for (Jugador davanter : davanters) {
            System.out.println(davanter.toString());
        }
    }

}
