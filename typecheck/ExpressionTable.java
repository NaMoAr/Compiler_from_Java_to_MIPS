package typecheck;

import syntaxtree.*;

public class ExpressionTable {
    // Fields
    private final Node node;
    private final String type;

    // Constructor
    ExpressionTable(Node n, String t) {
        node = n;
        type = t;
    }

    // Methods
    public Node getNode() {
        return node;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        boolean b1 = (obj == null);
        boolean b2 = !(obj instanceof ExpressionTable);
        
        if (b1 || b2){
            return false;
        }

        ExpressionTable rhs = (ExpressionTable)obj;
        boolean b3 = rhs.type.equals(this.type);
        
        return b3;
    }
} // End of Expression Table
