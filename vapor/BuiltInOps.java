package vapor;

public class BuiltInOps {

    public static String Add(String a, String b) {
        String addStr = "Add(" + a + " " + b + ")";
        return addStr;
    }

    public static String Sub(String a, String b) {
        String subStr = "Sub(" + a + " " + b + ")"; 
        return subStr;
    }

    public static String MulS(String a, String b) {
        String mulsStr = "MulS(" + a + " " + b + ")";
        return mulsStr;
    }

    public static String Eq(String a, String b) {
        String eqStr = "Eq(" + a + " " + b + ")";
        return eqStr;
    }

    public static String Lt(String a, String b) {
        String ltStr = "Lt(" + a + " " + b + ")";
        return ltStr;
    }

    public static String LtS(String a, String b) {
        String ltsStr = "LtS(" + a + " " + b + ")";
        return ltsStr;
    }

    public static String HeapAllocZ(int n) {
        String intLabel = Integer.toString(n);
        String heapStr = "HeapAllocZ(" + intLabel + ")";
        return heapStr;
    }

    public static String AllocArray(Variable size) {
        String intLabel = size.toString();
        String allocStr = "call :AllocArray(" + intLabel + ")";
        return allocStr;
    }

} // ENDOF BuiltInOps
