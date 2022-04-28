package typecheck;

import java.util.*;

import syntaxtree.*;

public class Table {
    // Data Structure
    private HashMap<SymbolTable, Collector> table = new LinkedHashMap<>();

    // Fields
    private Table root;
    private Node node;

    // Constructor
    public Table(Node n) {
        root = null;
        node = n;
    }

    public Table(Table t, Node n) {
        root = t;
        node = n;
    }

    // Methods

    public HashMap<SymbolTable, Collector> getTable(){
        return table;
    }

    public void addScope(SymbolTable st, Node n, Table t) {
        // Put the program into the big symbol table, table
        table.put(st, new Collector(st, n, t));
    }

    public Collector find(SymbolTable st) {
        Collector c = table.get(st);
        boolean b1 = (c != null);
        boolean b2 = (root != null);
        
        if (b1){
            return c;
        }

       
        else if (b2){
            return root.find(st);
        }

        else{
            return null;
        }
    }

    public Collector findLocal(SymbolTable st) {
        Collector c = table.get(st);
        boolean b1 = (c != null);
        
        if (b1){
            return c;
        }

        else{
            return null;
        }
    }




    public Table getRoot() {
        return root;
    }

    public Node getNode() {
        return node;
    }

    public Iterator<Collector> tableIterator() {
        return table.values().iterator();
    }

} // End of Table 
