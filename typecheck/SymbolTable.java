package typecheck;

import java.util.*;

public class SymbolTable {
    // Data Structure
    private static HashMap<String, SymbolTable> symboltable = new HashMap<>();

    // Fields
    private String type_id;

    private SymbolTable(String n) {
        type_id = n;
    }

    // Methods
    @Override
    public String toString() {
        return type_id;
    }

    public static SymbolTable setTypeId(String n) {
        // provide new key, the type id, if absent in the SymbolTable
        return symboltable.computeIfAbsent(n, k -> new SymbolTable(k));
    }

    @Override
    public boolean equals(Object obj) {
        boolean b1 = (obj == null);
        boolean b2 = !(obj instanceof SymbolTable);

        if (b1 || b2){
            return false;
        }

        SymbolTable rhs = (SymbolTable)obj;
        
        boolean b3 = rhs.type_id.equals(this.type_id);
        return b3;
    }

    @Override
    public int hashCode() {
        return type_id.hashCode();
    }

} // End of SymbolTable Class
