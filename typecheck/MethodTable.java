package typecheck;

import java.util.*;

public class MethodTable {
    // Data Structure
    private final LinkedList<String> params;
    
    // Fields
    private final ExpressionTable returnType;

    // Constructor
    MethodTable(LinkedList<String> p, ExpressionTable et) {
        params = new LinkedList<>(p);
        returnType = et;
    }

    // Methods
    public LinkedList<String> getParams() {
        return params;
    }

    public ExpressionTable getReturnType() {
        return returnType;
    }

    @Override
    public boolean equals(Object obj) {
        boolean b1 = (obj == null);
        boolean b2 = !(obj instanceof MethodTable);
        boolean bx = (b1 || b2);

        if (bx){
            return false;
        }
        MethodTable rhs = (MethodTable)obj;
        boolean b3 = rhs.params.equals(this.params);
        boolean b4 = rhs.returnType.equals(this.returnType);
        boolean by = (b3 && b4);

        return by;
    }
} // End of Method Table
