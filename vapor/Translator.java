package vapor;
import syntaxtree.*;
import typecheck.*;
import java.util.*;

public class Translator {
    private ClassRecord cr = new ClassRecord();
    private VTable vt = new VTable();
    private Jump jump = new Jump();
    private Variable varLabel = new Variable();

    private String INDENT = "  ";
    private String indent = "";

    private boolean newLineFlag = true;

    public ClassRecord getClassRecord() { return cr; }

    public VTable getVTable() { return vt; }

    public Jump getJump() { return jump; }

    public Variable getVar() { return varLabel; }

    public void getClassRecord(Collector c) {
        Stack<Table> clazzRecord = new Stack<>();

        // Obtain previously calculated methods in ClassRecord
        LinkedHashMap<String, String> method = new LinkedHashMap<>();

        for( Table it = c.getTable(); it != null; it = it.getRoot() ) {
            clazzRecord.push(it);
        }

        // Pop classRecords until reaches root of the class, the superclass or mainclass
        clazzRecord.pop();
   
        // cannot resort boolean <- !clazzRecord.empty due to while-loop 
        while ( !clazzRecord.empty() ) { // There exists classes in the stack
            Table t = clazzRecord.pop();
            String classId = CheckDistinct.classId( t.getNode() );

            for (Iterator<Collector> it = t.tableIterator(); it.hasNext(); ) {
                Collector co = it.next();

                boolean b2 = (co.getTypeNode() instanceof MethodDeclaration);
                if( b2 ) {
                    String methodId = co.getSymbol().toString();
                    method.put(methodId, classId + "." + methodId);
                } 
                else{ 
                    String Clazz = c.getSymbol().toString();
                    String getMethod = co.getSymbol().toString();

                    // Store variable tied to their class and method
                    cr.setVar(Clazz, getMethod);
                }
            }
        }

        String Clazz = c.getSymbol().toString();

        // Store method in vtable from class
        vt.setMethod(Clazz, method);
    }

    public void appendError(String e) { appendNewLine("Error(\"" + e + "\")"); }

    public void appendVTable(Collector c) {
        String Clazz = c.getSymbol().toString();
        appendNewLine("const vmt_" + Clazz); // Append class at start of VTable
        incrIndent();

        // Retreive data segments for every method in the VTable
        String getMethod = c.getSymbol().toString();
        for(Iterator<String> it = vt.methodIterator(getMethod); it.hasNext();){
            appendNewLine( ":" + it.next() );
        }
         decrIndent();
    }

    public void appendMethod(Collector c) {
        Node n = c.getTypeNode();

        boolean b1 = (n instanceof MainClass);

        if (b1) { appendNewLine("func Main()"); } 
        else { // n is a Method
            Table t = c.getTable();
            String Clazz = CheckDistinct.classId( t.getRoot().getNode() );

            appendLine("func ");
            appendLine( Clazz + "." + CheckDistinct.methodId(n) );
            appendLine("(this");

            // Scan through parameters called in the method
            for (Iterator<Collector> it = t.tableIterator(); it.hasNext(); ) {
                Collector bind = it.next();

                boolean b2 = (bind.getTypeNode() instanceof FormalParameter);
                String getParam = bind.getSymbol().toString();
                if (b2) { appendLine( " " + getParam ); }
            }

            appendNewLine(")");
        } // end of if else
    }

    public void appendPrintIntS(Variable v) {
        String Value = v.toString();
        appendNewLine("PrintIntS(" + Value + ")"); 
    }

    public void appendAssignment(Variable Location, Variable rhs) {
        String Value = rhs.toString();
        appendAssignment( Location, Value ); 
    }

    public void appendAssignment(Variable lhs, String Value) {
        String Location = lhs.toString();
        appendNewLine(Location + " = " + Value); 
    }

    public void appendIf(Variable v, Jump j){
        String Value =  v.toString();
        String CodeLabel = j.toString();
        appendNewLine( "if " + Value + " goto :" + CodeLabel ); 
    }

    public void appendIfZero(Variable v, Jump j){
        String Value =  v.toString();
        String CodeLabel = j.toString();
        appendNewLine( "if0 " + Value + " goto :" + CodeLabel ); 
    }

    public void appendGoto(Jump j) {
        String CodeLabel = j.toString();
        appendNewLine( "goto :" + CodeLabel ); 
    }

    public void appendJump(Jump j) {
        String CodeLabel = j.toString();
        appendNewLine(CodeLabel + ":"); 
    }

    public void appendReturn() { appendNewLine("ret"); }

    public void appendReturn(Variable v) {
        String Value = v.toString();
        appendNewLine( "ret " + Value ); 
    }

    public void appendAlloc() {
        appendNewLine("func AllocArray(size)");
        incrIndent();
        appendNewLine("bytes = MulS(size 4)");
        appendNewLine("bytes = Add(bytes 4)");
        appendNewLine("v = HeapAllocZ(bytes)");
        appendNewLine("[v] = size");
        appendNewLine("ret v");
        decrIndent();
    }

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
