package vapor;

public class Variable {
    private final int minusFlag = -1; // For variables that do not have incrementing lables
    private final String root;
    private final int number;

    private boolean memCallFlag[] = {false, false};
    // memCallFlag[0] - variable is in memory
    // memCallFlag[1] - variable is parameter in method call

    // count is incremented locally, start from 0
    private int varCount = 0;

    Variable() {
        number = 0;
        root = "";
    }

    private Variable(int n, String b, boolean[] d) {
        number = n;
        root = b;
        this.memCallFlag = d;
    }

    private Variable(Variable get) {
        number = get.number;
        root = get.root;
        memCallFlag[0] = true;
        memCallFlag[1] = false;
    }

    // For every local variable in the method
    // increment the number of variables method contains
    public void countMethodVar() {
        varCount = 0;
    }

    public Variable memVar(){ // variables have a reference in memory
        Variable refVar = new Variable(this);
        return refVar;
    }
   
    public Variable appendFuncCall(String fc) {
        boolean[] flag = {false, true};
        Variable funcCall = new Variable(minusFlag, fc, flag);
        return funcCall;
    }

    public Variable appendTempVar() {
        boolean[] flag = {false, false};
        Variable tempVar = new Variable(varCount++, null, flag);
        return tempVar;
    }

    public Variable appendConstant(String c) {
        boolean[] flag = {false, false};
        Variable constVar = new Variable(minusFlag, c, flag);
        return constVar;
    }

    // Methods in Vapor contain "this" as a reference to the original variable id (this...) or [this...]
    public Variable appendThisVar() {
        boolean[] flag = {false, false};
        Variable thisVar = new Variable(minusFlag, "this", flag);
        return thisVar;
    }

    // each variable in the method has an offset in memory [this+Offset]
    public Variable appendThisVar(int offset) {
        boolean[] flag = {false, false};
        Variable thisVar = new Variable(offset, "this", flag);
        return thisVar;
    }

    public Variable appendLocalVar(String var) {
        boolean[] flag = {false, false};
        Variable localVar = new Variable(minusFlag, var, flag);
        return localVar;
    }

    public Variable appendLocalVar(int offset, String var) {
        boolean[] flag = {false, false};
        Variable localVar = new Variable(offset, var, flag);
        return localVar;
    }

    public boolean isMemory() {
        return memCallFlag[0];
    }

    public boolean isCall() {
        return memCallFlag[1];
    }

    // Determine 
    public boolean isNullPointer() {
        boolean b1 = root != null && number < 0;
        boolean b2 = !memCallFlag[0] && !memCallFlag[1];
        boolean b3 = b1 && b2;

        boolean b4 = b3 && root.equals("this");

        // method with Func (this) must go to a nullptr
        if ( b4 ){
            return true;
        }
        else{
            return false;
        }
      
    }

    public boolean isTempVar() {
        boolean b1 = root == null && number >= 0;
        boolean b2 = !memCallFlag[0] && !memCallFlag[1];
        boolean b3 = b1 && b2;

        if(b3){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public String toString() {
        String label;

        boolean b1 = number == minusFlag;
        boolean b2 = root != null;
        boolean b3 = memCallFlag[0] && !memCallFlag[1];

        if (b1){
            label = root;
        }
        else if (b2){
            String intLabel = Integer.toString(number);
            label = root + "+" + intLabel;
        }
        else{
            String intLabel = Integer.toString(number);
            label =  "t." + intLabel;
        }

        if (b3){
            return "[" + label + "]";
        }
        else{
            return label;
        }
    }

} // ENDOF Variable
