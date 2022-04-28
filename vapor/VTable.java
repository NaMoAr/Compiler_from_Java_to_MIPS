    
package vapor;

import java.util.*;

public class VTable {
    
    // Create Hashmap to record classes and their vtable
    // each entry in vtable has class mapped with their own method and offsets
    private LinkedHashMap<String, LinkedHashMap<String, String> > vtable = new LinkedHashMap<>();

    public int getMethodOffset(String c, String vt) {
        int offsetMethod = new LinkedList<>(vtable.get(c).keySet()).indexOf(vt);

        return offsetMethod * 4;
    }
    
    public void setMethod(String c, HashMap<String, String> vt) {
        vtable.computeIfAbsent(c, k -> new LinkedHashMap<>()).putAll(vt);
    }

    public Iterator<String> methodIterator(String c) {
        return vtable.get(c).values().iterator();
    }

}