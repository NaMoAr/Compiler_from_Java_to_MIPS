package vaporm;

import java.util.*;

public class RegisterMap {
    private Set<Register> all = new LinkedHashSet<>();
    private Set<Register> use = new HashSet<>();

    private RegisterMap(Register[] regs) {
        Collections.addAll(all, regs);
    }

    // We only use t0~t7 and s0~s7. a0~a3, v0 and v1 are reserved.
    public static RegisterMap CreateGlobalPool() {
        Register[] regs = {
                // Caller-saved
                Register.t[0], Register.t[1], Register.t[2], Register.t[3],
                Register.t[4], Register.t[5], Register.t[6], Register.t[7],
                Register.t[8],
                // Callee-saved
                Register.s[0], Register.s[1], Register.s[2], Register.s[3],
                Register.s[4], Register.s[5], Register.s[6], Register.s[7]
        };

        return new RegisterMap(regs);
    }

    // Local pool used for retrieving values from `local` stack
    public static RegisterMap CreateLocalPool() {
        Register[] regs = {
                Register.v[0], Register.v[1],
                Register.a[0], Register.a[1], Register.a[2], Register.a[3]
        };

        return new RegisterMap(regs);
    }

    public boolean contains(Register reg) {
        return all.contains(reg);
    }

    public boolean inUse(Register reg) {
        return use.contains(reg);
    }

    public boolean hasFree() {
        return all.size() > use.size();
    }

    public Register acquire() {
        Register ret = null;
        Set<Register> diff = new LinkedHashSet<>(all);
        diff.removeAll(use);

        if (!diff.isEmpty()) {
            ret = diff.iterator().next();
            use.add(ret);
        }
        return ret;
    }

    public void release(Register reg) {
        use.remove(reg);
    }
}
