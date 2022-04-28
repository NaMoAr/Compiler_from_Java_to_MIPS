package typecheck;

import syntaxtree.*;

public class Collector {
    // Fields
    private SymbolTable st;
    private Node n;
    private Table t;

    // Constructor
    Collector(SymbolTable symboltable, Node node, Table table) {
        st = symboltable;
        n = node;
        t = table;
    }

    // Methods
    public SymbolTable getSymbol() {
        return st;
    }

    public Node getTypeNode() {
        return n;
    }

    public Table getTable() {
        return t;
    }
} // End of Collector
