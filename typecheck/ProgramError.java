package typecheck;

public class ProgramError {
    // Fields
    private static boolean errorFlag = false;

    // Methods
    public static boolean foundError() {
        return errorFlag;
    }

    public static void detectError() {
        errorFlag = true;
    }

} // End of Program Error
