package typecheck;

import java.util.*;

public class SubtypeTable {
    // Data Structure
    private static HashMap< SymbolTable, List<SymbolTable> > subRelation = new HashMap<>();

    // Methods
    public static void addSub(SymbolTable root, SymbolTable child) {
        subRelation.putIfAbsent(root, new LinkedList<>());
        subRelation.get(root).add(child);

        // Typecheck if subtyping is transitive
        for (Iterator<SymbolTable> it = subRelation.keySet().iterator(); it.hasNext(); ) {
            SymbolTable st = it.next();
            boolean b1 = st.equals(root);

            if (b1) {
                List<SymbolTable> rootChild = subRelation.get(st);
                boolean b2 = rootChild.contains(root);

                if( b2 ){
                    rootChild.add(child);
                }

            }
        } // end of for loop

    } // end of addSub

    public static boolean isSub(SymbolTable child, SymbolTable base) {
        boolean b1 = subRelation.containsKey(base);

        if (b1){
            boolean b2 = subRelation.get(base).contains(child);
            
            return b2;
        }
        else{
            return false;
        }
    }

    public static boolean hasSub(SymbolTable c) {
        boolean b = subRelation.containsKey(c);
        return b;
    }

} // End of SubtypeTable Class
