package vaporm;

import java.util.*;

public class Liveness {
    private final ArrayList< Set<String> > in; // variables going into block
    private final ArrayList< Set<String> > out; // variables going out of block
    private final List< Set<String> > define; // variables on LHS of assignment
    private final List< Set<String> > use; // variables on RHS of assignment

    public Liveness(ArrayList< Set<String> > inIn, ArrayList< Set<String> > inOut, 
    List<Set<String>> list, List<Set<String>> list2) {
        in = inIn;
        out = inOut;
        define = list;
        use = list2;
    }

    public ArrayList< Set<String> > getIn() {
        ArrayList< Set<String> > IN = new ArrayList<>(in);
        return IN;
    }

    public ArrayList< Set<String> > getOut() {
        ArrayList< Set<String> > OUT = new ArrayList<>(out);
        return OUT;
    }

    public ArrayList< Set<String> > getDefine() {
        ArrayList< Set<String> > DEFINE = new ArrayList<>(define);
        return DEFINE;
    }

    public ArrayList< Set<String> > getUse() {
        ArrayList< Set<String> > USE = new ArrayList<>(use);
        return USE;
    }

}