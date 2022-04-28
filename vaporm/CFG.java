package vaporm;

import cs132.vapor.ast.*;

import java.util.*;
import java.util.stream.Collectors;

public class CFG {
    
    private ArrayList<CFGNode> nodes = new ArrayList<>();
    private Map<CFGNode, Set<CFGNode>> edges = new HashMap<>();

    public CFGNode newNode(VInstr instr, Set<String> def, Set<String> use) {
        CFGNode gn = new CFGNode(this, nodes.size(), instr, def, use);
        nodes.add(gn);
        return gn;
    }

    public CFGNode getNode(int index) {
        return nodes.get(index);
    }

    public int getIndex(CFGNode node) {
        return nodes.indexOf(node);
    }

    public List<CFGNode> getNodes() {
        return new ArrayList<>(nodes);
    }

    public int nodesCount() {
        return nodes.size();
    }

    public void addEdge(CFGNode from, CFGNode to) {
        if (from != null && to != null && from != to && nodes.contains(from) && nodes.contains(to)) {
            edges.computeIfAbsent(from, k -> new HashSet<>()).add(to);
            from.addSuccessor(to);
            to.addPredecessor(from);
        }
    }

    public Liveness computLiveness() {
        Map<CFGNode, Set<String>> in = new LinkedHashMap<>();
        Map<CFGNode, Set<String>> out = new LinkedHashMap<>();
        boolean updated;

        for (CFGNode n : nodes) {
            in.put(n, new HashSet<>());
            out.put(n, new HashSet<>());
        }

        do {
            updated = false;

            for (CFGNode n : nodes) {
                Set<String> oldin = new HashSet<>(in.get(n));
                Set<String> oldout = new HashSet<>(out.get(n));

                // in[n] = use[n]\/(out[n]-def[n])
                Set<String> newin = new HashSet<>(n.getUse());
                Set<String> diff = new HashSet<>(oldout);
                diff.removeAll(n.getDef());
                newin.addAll(diff);

                // out[n] = \/(s in succ[n]) in[s]
                Set<String> newout = new HashSet<>();
                for (CFGNode s : n.getSucc())
                    newout.addAll(in.get(s));

                in.put(n, newin);
                out.put(n, newout);

                if (!newin.equals(oldin) || !newout.equals(oldout))
                    updated = true;
            }
        } while (updated);

        return new Liveness(new ArrayList<>(in.values()), new ArrayList<>(out.values()),
                nodes.stream().map(CFGNode::getDef).collect(Collectors.toList()),
                nodes.stream().map(CFGNode::getUse).collect(Collectors.toList()));
    }
}