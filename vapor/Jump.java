package vapor;

public class Jump {

    private final String jumpStr;
    private final int count;

    // Count is incremented globally, starts at 1
    private int jumpCount[] = {1,1,1,1,1}; // {null, bounds, if, while, and}

    Jump() {
        jumpStr = "";
        count = 0;
    }

    private Jump(String j, int n) {
        jumpStr = j;
        count = n;
    }

    public Jump appendNull() {
        Jump j = new Jump("null%d", jumpCount[0]++);
        return j;
    }

    public Jump appendBounds() {
        Jump j = new Jump("bounds%d", jumpCount[1]++);
        return j;
    }

    // Return these after initializing the jump label chain
    public Jump appendIfEnd() {
        Jump j = new Jump("if%d_end", jumpCount[2]++);
        return j;
    }

    public Jump appendWhileEnd() {
        Jump j = new Jump("while%d_end", jumpCount[3]++);
        return j;
    }

    public Jump appendAndEnd() {
        Jump j = new Jump("ss%d_end", jumpCount[4]++);
        return j;
    }

    // Initialize these labels to begin jump label chain
    public Jump appendIfElse() {        
        Jump j = new Jump("if%d_else", jumpCount[2]);
        return j;
    }

    public Jump appendWhileTop() {
        Jump j = new Jump("while%d_top", jumpCount[3]);
        return j;
    }
    
    public Jump appendAndElse() {
        Jump j = new Jump("ss%d_else", jumpCount[4]);
        return j;
    }

    @Override
    public String toString() {
        String jmpString = String.format(jumpStr, count);
        return jmpString;
    }
} // ENDOF Jump
