package vaporm;

import cs132.vapor.ast.*;

import java.util.*;

public class CFGNode {
    private final CFG graph;
    private final int index;

    private final VInstr instr;
    private final Set<String> def;
    private final Set<String> use;

    private Set<CFGNode> succ = new HashSet<>();
    private Set<CFGNode> pred = new HashSet<>();

    public CFGNode(CFG g, int idx, VInstr vi, Set<String> d, Set<String> u) {
        graph = g;
        index = idx;
        instr = vi;
        def = d;
        use = u;
    }

    public CFG getGraph() {
        return graph;
    }

    public int getIndex() {
        return index;
    }

    public Set<CFGNode> getSucc() {
        return new HashSet<>(succ);
    }

    public Set<CFGNode> getPred() {
        return new HashSet<>(pred);
    }

    public VInstr getInstr() {
        return instr;
    }

    public Set<String> getDef() {
        return new HashSet<>(def);
    }

    public Set<String> getUse() {
        return new HashSet<>(use);
    }

    public void addSuccessor(CFGNode gn) {
        if (gn != null && this != gn)
            succ.add(gn);
    }

    public void addPredecessor(CFGNode gn) {
        if (gn != null && this != gn)
            pred.add(gn);
    }
}
