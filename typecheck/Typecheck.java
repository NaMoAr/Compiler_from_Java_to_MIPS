/* Comment this for auto-grader */
package typecheck;
import parser.*;

import syntaxtree.*;
import typecheck.*;
import java.io.*;


public class Typecheck {
    public static void main(String[] args) throws FileNotFoundException {
       
        try {
            // For Local Machine Test
            //InputStream stream = new FileInputStream("");
            //new MiniJavaParser(stream);

            // For Auto-Grader Machine Test
            new MiniJavaParser(System.in);

            // Visitor Extend Objects
            FirstPassVisitor traverse = new FirstPassVisitor();
            SecondPassVisitor check = new SecondPassVisitor();

            // Generate Big Symbol Table, Table
            Goal program = MiniJavaParser.Goal();
            Table scope = new Table(program);

            // Scan through the program as inputs
            program.accept(traverse, scope);
            if (ProgramError.foundError()) {
                System.out.println("Type error");
                System.exit(1);
            } 
            else {

                // Typecheck the return types of the program
                program.accept(check, scope);
                if (ProgramError.foundError()) {
                    System.out.println("Type error");
                    System.exit(1);
                } 
                else {
                    System.out.println("Program type checked successfully");
                } // end of inner if-else

            } // end of outer if-else

        }

        catch (ParseException e) {
            System.out.println("Type error");
        } // end of try-catch
    }
}