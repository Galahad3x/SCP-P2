public class JugadorsEquip {
    int MAX_PORTERS = 1;
    int MAX_DEFENSES = 3;
    int MAX_MIGCAMPISTES = 2;
    int MAX_DAVANTERS = 1;

    public Jugador[] porters = new Jugador[MAX_PORTERS];
    public Jugador[] defenses = new Jugador[MAX_DEFENSES];
    public Jugador[] migcampistes = new Jugador[MAX_MIGCAMPISTES];
    public Jugador[] davanters = new Jugador[MAX_DAVANTERS];

    public JugadorsEquip () {}

    // Adds a player
    public void addPlayer(Jugador jugador) {
        switch (jugador.posicio) {
            case Porter ->  {
                for (int i = 0; i < porters.length; i++) {
                    if (porters[i] == null) {
                        porters[i] = jugador;
                    }
                }
            }
            case Defensa ->  {
                for (int i = 0; i < defenses.length; i++) {
                    if (defenses[i] == null) {
                        defenses[i] = jugador;
                    }
                }
            }case Migcampista ->  {
                for (int i = 0; i < migcampistes.length; i++) {
                    if (migcampistes[i] == null) {
                        migcampistes[i] = jugador;
                    }
                }
            }
            case Davanter ->  {
                for (int i = 0; i < davanters.length; i++) {
                    if (davanters[i] == null) {
                        davanters[i] = jugador;
                    }
                }
            }
        }
    }

    // Gets all the goalkeepers
    public Jugador[] getPorters() { return this.porters; }

    // Gets all the defenders
    public Jugador[] getDefenses() { return this.defenses; }

    // Gets all the midfielders
    public Jugador[] getMigcampistes() { return this.migcampistes; }

    // Gets all the attackers
    public Jugador[] getDavanters() { return this.davanters; }
}