public class JugadorsEquip {
    int MAX_PORTERS = 1;
    int MAX_DEFENSES = 3;
    int MAX_MIGCAMPISTES = 2;
    int MAX_DAVANTERS = 1;
    int MAX_JUGADORS = MAX_PORTERS + MAX_DEFENSES + MAX_MIGCAMPISTES + MAX_DAVANTERS;

    int numPorters = 0;
    int numDefenses = 0;
    int numMigcampistes = 0;
    int numDavanters = 0;

    public Jugador[] porters = new Jugador[MAX_PORTERS];
    public Jugador[] defenses = new Jugador[MAX_DEFENSES];
    public Jugador[] migcampistes = new Jugador[MAX_MIGCAMPISTES];
    public Jugador[] davanters = new Jugador[MAX_DAVANTERS];
    public Jugador[] jugadors = new Jugador[MAX_JUGADORS];

    public JugadorsEquip () {}

    // Adds a player
    public void addPlayer(Jugador jugador) {
        switch (jugador.posicio) {
            case Porter ->  {
                for (int i = 0; i < porters.length; i++) {
                    if (porters[i] == null) {
                        porters[i] = jugador;
                        numPorters++;
                        break;
                    }
                }
            }
            case Defensa ->  {
                for (int i = 0; i < defenses.length; i++) {
                    if (defenses[i] == null) {
                        defenses[i] = jugador;
                        numDefenses++;
                        break;
                    }
                }
            }
            case Migcampista ->  {
                for (int i = 0; i < migcampistes.length; i++) {
                    if (migcampistes[i] == null) {
                        migcampistes[i] = jugador;
                        numMigcampistes++;
                        break;
                    }
                }
            }
            case Davanter ->  {
                for (int i = 0; i < davanters.length; i++) {
                    if (davanters[i] == null) {
                        davanters[i] = jugador;
                        numDavanters++;
                        break;
                    }
                }
            }
        }
    }

    // Copies all the diferent player's array into an array together
    public void getJugadors() {
        System.arraycopy(porters, 0, jugadors, 0, porters.length);
        System.arraycopy(defenses, 0, jugadors, porters.length, defenses.length);
        System.arraycopy(migcampistes, 0, jugadors, defenses.length, migcampistes.length);
        System.arraycopy(davanters, 0, jugadors, migcampistes.length, davanters.length);
    }

    // Gets a player
    public Jugador getPlayer(int index) {
        getJugadors();
        return jugadors[index];
    }

    public JugadorsEquip copy(){
        JugadorsEquip new_jugadors = new JugadorsEquip();
        for (Jugador porter : porters) {
            if (porter != null) {
                new_jugadors.addPlayer(porter);
            }
        }
        for (Jugador defensa : defenses) {
            if (defensa != null) {
                new_jugadors.addPlayer(defensa);
            }
        }
        for (Jugador migcamp : migcampistes) {
            if (migcamp != null) {
                new_jugadors.addPlayer(migcamp);
            }
        }
        for (Jugador davanter : davanters) {
            if (davanter != null) {
                new_jugadors.addPlayer(davanter);
            }
        }
        return new_jugadors;
    }
}