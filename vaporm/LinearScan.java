package vaporm;

import cs132.vapor.ast.*;

import java.util.*;

public class LinearScan {
    private RegisterMap pool;
    private List<Interval> active;
    private Map<String, Register> register;
    private Set<String> unusedParams;
    private Set<String> stack;

    public LinearScanMap computeAllocation(List<Interval> ci, VVarRef.Local[] params) {
        pool = RegisterMap.CreateGlobalPool();
        active = new ArrayList<>();
        register = new LinkedHashMap<>();
        unusedParams = new HashSet<>();
        stack = new LinkedHashSet<>();

        List<Interval> intervals = new ArrayList<>(ci);
        // Sort by increasing start point
        intervals.sort(Comparator.comparingInt(Interval::getStart));

        // Map params to registers (in a0~a3 and `in` stack)
        for (int i = 0; i < params.length; i++) {
            String arg = params[i].ident;
            // If parameter is used during the function
            if (intervals.stream().map(Interval::getVar).anyMatch(o -> o.equals(arg))) {
                if (pool.hasFree()) {
                    // For those args that are not able to be put into registers,
                    // we move them into `local` stack later (by spilling them).
                    register.put(arg, pool.acquire());
                    unusedParams.add(arg);
                }
            }
        }

        for (Interval i : intervals) {
            expireOldInterval(i);

            // No need to allocate registers for the first parameters
            if (i.getStart() > 0 || !unusedParams.contains(i.getVar())) {
                if (!pool.hasFree()) {
                    spillAtInterval(i);
                } else {
                    register.put(i.getVar(), pool.acquire());
                    active.add(i);
                }
            }
        }

        return new LinearScanMap(new LinkedHashMap<>(register),
                stack.toArray(new String[stack.size()]));
    }

    private void expireOldInterval(Interval interval) {
        // Sort by increasing end point
        active.sort(Comparator.comparingInt(Interval::getEnd));

        for (Iterator<Interval> iter = active.iterator(); iter.hasNext(); ) {
            Interval i = iter.next();
            if (i.getEnd() >= interval.getStart())
                return;

            iter.remove();
            pool.release(register.get(i.getVar()));

            // release the interval of first parameters
            if (unusedParams.contains(i.getVar()))
                unusedParams.remove(i.getVar());
        }
    }

    private void spillAtInterval(Interval interval) {
        // Sort by increasing end point
        active.sort(Comparator.comparingInt(Interval::getEnd));

        // Intervals for function parameters are marked as fixed. (They are not spilled)
        Interval spill = null;
        if (!active.isEmpty()) {
            int idx = active.size() - 1;
            do {
                spill = active.get(idx--);
            } while (idx >= 0 && unusedParams.contains(spill.getVar()));
            spill = idx < 0 ? null : spill;
        }

        if (spill != null && spill.getEnd() > interval.getEnd()) {
            register.put(interval.getVar(), register.get(spill.getVar()));
            register.remove(spill.getVar());
            stack.add(spill.getVar());
            active.remove(spill);
            active.add(interval);
        } else {
            stack.add(interval.getVar());
        }
    }
























   
        public static CFG generateFlowGraph(VFunction func) {
            CFG graph = new CFG();
            List<CFGNode> nodes = new ArrayList<>();
    
            for (VInstr instr : func.body) {
                Set<String> def = new HashSet<>();
                Set<String> use = new HashSet<>();
    
                instr.accept(new VInstr.Visitor<RuntimeException>() {
                    @Override
                    public void visit(VAssign vAssign) {
                        def.add(vAssign.dest.toString());
                        if (vAssign.source instanceof VVarRef) {
                            use.add(vAssign.source.toString());
                        }
                    }
    
                    @Override
                    public void visit(VCall vCall) {
                        def.add(vCall.dest.toString());
                        if (vCall.addr instanceof VAddr.Var) {
                            use.add(vCall.addr.toString());
                        }
                        for (VOperand arg : vCall.args) {
                            if (arg instanceof VVarRef) {
                                use.add(arg.toString());
                            }
                        }
                    }
    
                    @Override
                    public void visit(VBuiltIn vBuiltIn) {
                        if (vBuiltIn.dest != null)
                            def.add(vBuiltIn.dest.toString());
    
                        for (VOperand arg : vBuiltIn.args) {
                            if (arg instanceof VVarRef) {
                                use.add(arg.toString());
                            }
                        }
                    }
    
                    @Override
                    public void visit(VMemWrite vMemWrite) {
                        VMemRef.Global ref = (VMemRef.Global) vMemWrite.dest;
                        use.add(ref.base.toString()); // not def but use
                        if (vMemWrite.source instanceof VVarRef) {
                            use.add(vMemWrite.source.toString());
                        }
                    }
    
                    @Override
                    public void visit(VMemRead vMemRead) {
                        def.add(vMemRead.dest.toString());
    
                        VMemRef.Global ref = (VMemRef.Global) vMemRead.source;
                        use.add(ref.base.toString());
                    }
    
                    @Override
                    public void visit(VBranch vBranch) {
                        if (vBranch.value instanceof VVarRef) {
                            use.add(vBranch.value.toString());
                        }
    
                        // the branch target is label, thus no use produced.
                    }
    
                    @Override
                    public void visit(VGoto vGoto) {
                        if (vGoto.target instanceof VAddr.Var) {
                            use.add(vGoto.target.toString());
                        }
                    }
    
                    @Override
                    public void visit(VReturn vReturn) {
                        if (vReturn.value != null) {
                            if (vReturn.value instanceof VVarRef) {
                                use.add(vReturn.value.toString());
                            }
                        }
                    }
                });
    
                nodes.add(graph.newNode(instr, def, use));
            }
    
            for (int i = 0; i < func.body.length; i++) {
                VInstr instr = func.body[i];
                CFGNode prev = i > 0 ? nodes.get(i - 1) : null;
                CFGNode cur = nodes.get(i);
    
                // Edge from the prev instr to current instr.
                if (prev != null)
                    graph.addEdge(prev, cur);
    
                if (instr instanceof VBranch) {
                    VLabelRef<VCodeLabel> target = ((VBranch) instr).target;
                    CFGNode to = nodes.get(target.getTarget().instrIndex);
                    graph.addEdge(cur, to);
                } else if (instr instanceof VGoto) {
                    // For gotos, we only allow goto labels.
                    VLabelRef<VCodeLabel> target = ((VAddr.Label<VCodeLabel>) ((VGoto) instr).target).label;
                    CFGNode to = nodes.get(target.getTarget().instrIndex);
                    graph.addEdge(cur, to);
                }
            }
    
            return graph;
        }
    
        public static List<Interval> generateLiveIntervals(CFG graph, Liveness liveness) {
            Map<String, Interval> intervals = new HashMap<>();
    
            List<Set<String>> actives = new ArrayList<>();
            for (CFGNode n : graph.getNodes()) {
                // active[n] = def[n] \/ in[n]
                Set<String> active = new HashSet<>(n.getDef());
                active.addAll(liveness.getIn().get(n.getIndex()));
                actives.add(active);
            }
    
            for (int i = 0; i < actives.size(); i++) {
                for (String var : actives.get(i)) {
                    if (intervals.containsKey(var)) { // update end
                        intervals.get(var).setEnd(i);
                    } else { // create new interval
                        intervals.put(var, new Interval(var, i, i));
                    }
                }
            }
    
            return new ArrayList<>(intervals.values());
        }
    
        public static String in(int offset) {
            return "in[" + Integer.toString(offset) + "]";
        }
    
        public static String out(int offset) {
            return "out[" + Integer.toString(offset) + "]";
        }
    
        public static String local(int offset) {
            return "local[" + Integer.toString(offset) + "]";
        }
    
        public static String memoryReference(Register reg, int offset) {
            if (offset > 0)
                return "[" + reg.toString() + "+" + Integer.toString(offset) + "]";
            else
                return "[" + reg.toString() + "]";
        }
}

