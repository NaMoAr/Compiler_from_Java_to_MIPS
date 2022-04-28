package vaporm;

import java.io.PrintStream;

import cs132.vapor.ast.*;

import java.util.*;
import java.util.stream.Collectors;

public class VaporToVaporm {
    //private Print out = new Print(System.out);;
    private RegisterMap localPool = RegisterMap.CreateLocalPool();

    // public Print getOutput() {
    //     return out;
    // }

    public void outputConstSegment(VDataSegment[] segments) {
        // Treat all data segment as const segment
        for (VDataSegment seg : segments) {
            appendNewLine("const " + seg.ident);
             incrIndent();
            for (VOperand.Static label : seg.values) {
                 appendNewLine(label.toString());
            }
             decrIndent();
             appendLine();
        }
    }

    public void outputAssignment(String lhs, String rhs) {
         appendNewLine(lhs + " = " + rhs);
    }

    private void outputFunctionSignature(String func, int inStack, int outStack, int localStack) {
         appendLine("func " + func + " ");
         appendLine("[in " + Integer.toString(inStack) + ", ");
         appendLine("out " + Integer.toString(outStack) + ", ");
         appendNewLine("local " + Integer.toString(localStack) + "]");
    }

    private Register loadVariable(LinearScanMap map, String var, boolean dst) {
        Register reg = map.lookupRegister(var);
        if (reg != null) { // var in register
            return reg;
        } else { // var on `local` stack
            int offset = map.lookupStack(var);
            Register load = localPool.acquire();
            if (!dst) // for dest's, they only want a register.
                outputAssignment(load.toString(), LinearScan.local(offset));
            return load;
        }
    }

    private void writeVariable(Register reg, LinearScanMap map, String var) {
        int offset = map.lookupStack(var);
        if (offset != -1) {
            outputAssignment(LinearScan.local(offset), reg.toString());
        }
    }

    private void releaseLocalRegister(Register reg) {
        if (localPool.contains(reg))
            localPool.release(reg);
    }

    public void outputFunction(VFunction func, LinearScanMap map, Liveness liveness) {
        List<Register> callee = map.usedCalleeRegister();

        // Map instrIndex to a label
        Map<Integer, Set<String>> labels = new HashMap<>();
        for (VCodeLabel l : func.labels)
            labels.computeIfAbsent(l.instrIndex, k -> new LinkedHashSet<>()).add(l.ident);

        int inStack = Math.max(func.params.length - 4, 0);
        int outStack = 0; // calculated later
        int localStack = map.stackSize();

        for (int i = 0; i < func.body.length; i++) {
            VInstr instr = func.body[i];
            if (instr instanceof VCall) {
                VCall call = (VCall) instr;
                outStack = Math.max(call.args.length - 4, outStack);

                // Only save those live-out but not def in this node.
                Set<String> liveOut = liveness.getOut().get(i);
                liveOut.removeAll(liveness.getDefine().get(i));
                // For saving $t before function call.
                // $t are saved on the high address of local stack.
                int saves = (int) liveOut.stream().map(map::lookupRegister).filter(o -> o != null
                        && o.isCallerSaved()).distinct().count();
                localStack = Math.max(localStack, map.stackSize() + saves);
            }
        }

        outputFunctionSignature(func.ident, inStack, outStack, localStack);
         incrIndent();

        // Save all $s registers
        for (int i = 0; i < callee.size(); i++) {
            outputAssignment(LinearScan.local(i), callee.get(i).toString());
        }

        // Load parameters into register or `local` statck
        Register[] argregs = { Register.a[0], Register.a[1], Register.a[2], Register.a[3] };
        for (int i = 0; i < func.params.length; i++) {
            Register dst = map.lookupRegister(func.params[i].ident);
            if (dst != null) {
                if (i < 4) { // Params passed by registers
                    outputAssignment(dst.toString(), argregs[i].toString());
                } else { // Params passed by `in` stack
                    outputAssignment(dst.toString(), LinearScan.in(i - 4));
                }
            } else {
                int offset = map.lookupStack(func.params[i].ident);
                if (offset != -1) { // some parameters may never be used
                    // Move the remaining parameters into `local` stack
                    Register load = localPool.acquire();
                    outputAssignment(load.toString(), LinearScan.in(i - 4));
                    outputAssignment(LinearScan.local(offset), load.toString());
                    localPool.release(load);
                }
            }
        }

        for (int i = 0; i < func.body.length; i++) {
            // Only save those live-out but not def in this node.
            final Set<String> liveOut = liveness.getOut().get(i);
            liveOut.removeAll(liveness.getDefine().get(i));

            // Output labels
            if (labels.containsKey(i)) {
                 decrIndent();
                labels.get(i).forEach(l ->  appendNewLine(l + ":"));
                 incrIndent();
            }

            func.body[i].accept(new VInstr.Visitor<RuntimeException>() {
                @Override
                public void visit(VAssign vAssign) {
                    Register dst = loadVariable(map, vAssign.dest.toString(), true);

                    if (vAssign.source instanceof VVarRef) {
                        Register src = loadVariable(map, vAssign.source.toString(), false);
                        outputAssignment(dst.toString(), src.toString());
                        releaseLocalRegister(src);
                    } else {
                        outputAssignment(dst.toString(), vAssign.source.toString());
                    }

                    writeVariable(dst, map, vAssign.dest.toString());
                    releaseLocalRegister(dst);
                }

                @Override
                public void visit(VCall vCall) {
                    List<Register> save = liveOut.stream().map(map::lookupRegister).filter(o -> o != null
                            && o.isCallerSaved()).distinct().collect(Collectors.toList());
                    save.sort(Comparator.comparing(Register::toString));

                    // Save all $t registers
                    for (int i = 0; i < save.size(); i++) {
                        outputAssignment(LinearScan.local(map.stackSize() + i), save.get(i).toString());
                    }

                    Register[] argregs = { Register.a[0], Register.a[1], Register.a[2], Register.a[3] };
                    for (int i = 0; i < vCall.args.length; i++) {
                        String var = vCall.args[i].toString();
                        if (vCall.args[i] instanceof VVarRef) {
                            if (i < 4) { // into registers
                                Register reg = map.lookupRegister(var);
                                if (reg != null) {
                                    outputAssignment(argregs[i].toString(), reg.toString());
                                } else {
                                    int offset = map.lookupStack(var);
                                    outputAssignment(argregs[i].toString(), LinearScan.local(offset));
                                }
                            } else { // into `out` stack
                                Register reg = loadVariable(map, var, false);
                                outputAssignment(LinearScan.out(i - 4), reg.toString());
                                releaseLocalRegister(reg);
                            }
                        } else {
                            if (i < 4) { // store into $a0~$a3
                                outputAssignment(argregs[i].toString(), var);
                            } else { // store into `out` stack
                                outputAssignment(LinearScan.out(i - 4), var);
                            }
                        }
                    }

                    if (vCall.addr instanceof VAddr.Label) {
                         appendNewLine("call " + vCall.addr.toString());
                    } else {
                        Register addr = loadVariable(map, vCall.addr.toString(), false);
                         appendNewLine("call " + addr.toString());
                        releaseLocalRegister(addr);
                    }

                    Register dst = loadVariable(map, vCall.dest.toString(), true);
                    if (dst != Register.v[0])
                        outputAssignment(dst.toString(), Register.v[0].toString());
                    writeVariable(dst, map, vCall.dest.toString());
                    releaseLocalRegister(dst);

                    // Restore all $t registers
                    for (int i = 0; i < save.size(); i++) {
                        outputAssignment(save.get(i).toString(), LinearScan.local(map.stackSize() + i));
                    }
                }

                @Override
                public void visit(VBuiltIn vBuiltIn) {
                    StringBuilder rhs = new StringBuilder(vBuiltIn.op.name + "(");
                    List<Register> srcregs = new ArrayList<>();
                    for (VOperand arg : vBuiltIn.args) {
                        if (arg instanceof VVarRef) {
                            Register src = loadVariable(map, arg.toString(), false);
                            srcregs.add(src);

                            rhs.append(src.toString());
                            rhs.append(" ");
                        } else {
                            rhs.append(arg.toString());
                            rhs.append(" ");
                        }
                    }
                    rhs.deleteCharAt(rhs.length() - 1);
                    rhs.append(")");

                    for (Register src : srcregs)
                        releaseLocalRegister(src);

                    if (vBuiltIn.dest == null) { // no return value
                         appendNewLine(rhs.toString());
                    } else {
                        Register dst = loadVariable(map, vBuiltIn.dest.toString(), true);
                        outputAssignment(dst.toString(), rhs.toString());

                        writeVariable(dst, map, vBuiltIn.dest.toString());
                        releaseLocalRegister(dst);
                    }
                }

                @Override
                public void visit(VMemWrite vMemWrite) {
                    VMemRef.Global ref = (VMemRef.Global) vMemWrite.dest;
                    Register base = loadVariable(map, ref.base.toString(), false);

                    if (vMemWrite.source instanceof VVarRef) {
                        Register src = loadVariable(map, vMemWrite.source.toString(), false);
                        outputAssignment(LinearScan.memoryReference(base, ref.byteOffset), src.toString());
                        releaseLocalRegister(src);
                    } else {
                        outputAssignment(LinearScan.memoryReference(base, ref.byteOffset), vMemWrite.source.toString());
                    }

                    releaseLocalRegister(base);
                }

                @Override
                public void visit(VMemRead vMemRead) {
                    Register dst = loadVariable(map, vMemRead.dest.toString(), true);

                    VMemRef.Global ref = (VMemRef.Global) vMemRead.source;
                    Register src = loadVariable(map, ref.base.toString(), false);
                    outputAssignment(dst.toString(), LinearScan.memoryReference(src, ref.byteOffset));
                    releaseLocalRegister(src);

                    writeVariable(dst, map, vMemRead.dest.toString());
                    releaseLocalRegister(dst);
                }

                @Override
                public void visit(VBranch vBranch) {
                    String cond = vBranch.value.toString();
                    if (vBranch.value instanceof VVarRef) {
                        Register src = loadVariable(map, vBranch.value.toString(), false);
                        cond = src.toString();
                        releaseLocalRegister(src);
                    }

                     appendLine(vBranch.positive ? "if" : "if0");
                     appendLine(" " + cond);
                     appendNewLine(" goto " + vBranch.target);
                }

                @Override
                public void visit(VGoto vGoto) {
                     appendNewLine("goto " + vGoto.target.toString());
                }

                @Override
                public void visit(VReturn vReturn) {
                    if (vReturn.value != null) {
                        if (vReturn.value instanceof VVarRef) {
                            Register src = loadVariable(map, vReturn.value.toString(), false);
                            if (src != Register.v[0])
                                outputAssignment(Register.v[0].toString(), src.toString());
                            releaseLocalRegister(src);
                        } else {
                            outputAssignment(Register.v[0].toString(), vReturn.value.toString());
                        }
                    }

                    // Restore all $s registers
                    for (int i = 0; i < callee.size(); i++) {
                        outputAssignment(callee.get(i).toString(), LinearScan.local(i));
                    }

                     appendNewLine("ret");
                }
            });
        }

         decrIndent();
    }







        private static final String INDENT = "  ";
        private String indent = "";
        private boolean newLineFlag = true;
    
      
        public void appendLine() { System.out.println(); }
    
        public void appendLine(String s) {
            String output = "";
            if(newLineFlag){ output = indent; }
            System.out.print(output + s);
             newLineFlag = false;
        }
    
        public void appendNewLine(String s) {
            String output = "";
            if(newLineFlag){ output = indent; }
            System.out.println(output + s);
            newLineFlag = true;
        }
    
        public void incrIndent() { indent += INDENT; }
    
        public void decrIndent() {
            int currIndent = indent.length();
            int prevIndent = INDENT.length();
            indent = indent.substring(0, currIndent - prevIndent ); 
        }


}

