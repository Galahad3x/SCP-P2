public class Equip {
    public int id;
    public int valor;
    public int cost;
    public JugadorsEquip jugadorsEquip;

    public Equip (int id, int valor, int cost, JugadorsEquip jugadorsEquip) {
        this.id = id;
        this.valor = valor;
        this.cost = cost;
        this.jugadorsEquip = jugadorsEquip;
    }
}
