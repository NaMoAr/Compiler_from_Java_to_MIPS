package vapor;

import java.util.*;
import typecheck.*;

public class VaporCheck {

    // Function Call
    public static String FuncCall(Variable v, Variable currLabel, LinkedList<Variable> linkedLabel) {
        String Var = v.toString();
        String Args = currLabel.toString();

        // Builds function call as a sequence of strings for each func call variable and args
        StringBuilder fc = new StringBuilder( "call " + Var + "(" + Args );

        // For every parameter in the function call append here
        // call Var (Args...)
        for (Variable label : linkedLabel) {
            fc.append(" ");
            String getLabel = label.toString();
            fc.append( getLabel );
        }
        fc.append(")");

        String callStr = fc.toString();
        return callStr;
    }

    public static Variable MemoryCall(Variable v, TranslateTable tt) {
        Translator translate = tt.getTranslator();
        Variable tVar = v;

        boolean b = v.isMemory() || v.isCall();
        if(b){
            tVar = translate.getVar().appendTempVar();
            translate.appendAssignment(tVar, v);
        }
        return tVar;
    }

    public static Variable nullPointer(Variable v, TranslateTable tt) {
        Translator translate = tt.getTranslator();
        Jump jump = translate.getJump();
        Variable tVar = translate.getVar();
        Variable var = v;
        
        boolean b1 = !v.isNullPointer();
        if ( b1 ) {
            Jump thisJump = jump.appendNull();

            // when [t.X] gotos a null label
            boolean b2 = v.isMemory() || v.isCall();

            if(b2){
                var = tVar.appendTempVar();
                translate.appendAssignment(var, v);
            }
            translate.appendIf(var, thisJump);
            translate.incrIndent();
            translate.appendError("null pointer");
            translate.decrIndent();
            translate.appendJump(thisJump);
        }
        return var;
    }

    public static Variable outBounds(Variable v, Variable indent, TranslateTable tt) {
        Translator translate = tt.getTranslator();
        Jump jump = translate.getJump();

        // Jump label precedes by an "array index out of bounds"
        Jump firstLabel = jump.appendBounds(); // First label reaches to a null label
        Jump secondLabel = jump.appendBounds(); // Second label reaches to another null label
        
        Variable tVar = translate.getVar();
        Variable tempVar = tVar.appendTempVar();

        String getIndent = indent.toString();
        String getTemp = tempVar.toString();
        String getLabel = v.toString();

        v = MemoryCall(v, tt);
        translate.appendAssignment( tempVar, v.memVar() );
        translate.appendAssignment( tempVar, BuiltInOps.LtS( getIndent, getTemp ) );

        translate.appendIf(tempVar, firstLabel);
        translate.incrIndent();
        translate.appendError("array index out of bounds");
        translate.decrIndent();

        translate.appendJump(firstLabel);
        translate.appendAssignment( tempVar, BuiltInOps.LtS( "-1", getIndent ) );
        translate.appendIf(tempVar, secondLabel);
        translate.incrIndent();
        translate.appendError("array index out of bounds");
        translate.decrIndent();

        translate.appendJump(secondLabel);
        translate.appendAssignment( tempVar, BuiltInOps.MulS( getIndent, "4" ) );
        translate.appendAssignment( tempVar, BuiltInOps.Add( getTemp, getLabel ) );

        Variable localVar = tVar.appendLocalVar( 4, getTemp ).memVar();
        return localVar; // Return [t.X+4] at output translation
    }

    public static Variable idLabel(SymbolTable id, TranslateTable tt){
        Translator translate = tt.getTranslator();
        Collector c = tt.getTable().findLocal(id);
        Variable tVar;
        
        boolean b = (c != null); // check if there are local var ids in the collector

        if (b) { tVar = translate.getVar().appendLocalVar( id.toString() ); } 
        else { // no ids in the collector
            String classId = CheckDistinct.classId( tt.getTable().getRoot().getNode() );
            String getId = id.toString();
            int offset = translate.getClassRecord().getVarOffset( classId, getId );
            tVar = translate.getVar().appendThisVar(offset).memVar();
        }
        return tVar; // return [t.X+offset]
    }

} // ENDOF VaporCheck
