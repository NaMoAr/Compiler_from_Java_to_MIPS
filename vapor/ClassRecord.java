package vapor;

import java.util.*;

public class ClassRecord {
    // Create Hashmap to record classes and their field names to offset
    private LinkedHashMap<String, LinkedList<String> > classRecord = new LinkedHashMap<>();
   
    public int getVarOffset(String c, String v) {
        // Receive last recorded values of the field offset from VTable
        int offsetVar = classRecord.get(c).lastIndexOf(v);
        return offsetVar * 4 + 4;
    }

    public int getClassSize(String c) {
        boolean b = classRecord.containsKey(c);
        
        if( b ){ // When class keys exist in the hashmap
            int clazzSize = classRecord.get(c).size();
            return clazzSize * 4 + 4;
        }
        else{ // Initially there is nothing in the hashmap
            return 4;
        }
    }

    public void setVar(String c, String v) {
        classRecord.computeIfAbsent( c, k -> new LinkedList<>() ).add(v);
    }

    public Iterator<String> varIterator(String c) {
        return classRecord.get(c).iterator();
    }

} // ENDOF ClassRecord
