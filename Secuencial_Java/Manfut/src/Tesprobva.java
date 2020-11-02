import java.util.BitSet;

public class Tesprobva {
    public static void main(String[] argvs){
        BitSet st = new BitSet();
        st.set(0);
        st.set(1);
        System.out.println(st.toString());
        st.set(3);
        st.clear(1);
        System.out.println(st.length());
    }
}
