package vapor;
import parser.*;

import syntaxtree.*;
import typecheck.*;
import vapor.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;




public class J2V {
    public static void main(String args[]) throws FileNotFoundException, ParseException {
        
        try {

            InputStream stream = new FileInputStream("C:\\Users\\Najmeh\\Desktop\\BinaryTree.java");       
            new MiniJavaParser(stream);

            // For Auto-Grader Machine Test
            // new MiniJavaParser(System.in);

            FirstPassVisitor traverse = new FirstPassVisitor();
    
            Goal program = MiniJavaParser.Goal();
            Table scope = new Table(program);
            Translator t = new Translator();
            VaporVisitor vv = new VaporVisitor();        
            TranslateTable tt = new TranslateTable(scope, t);

            program.accept(traverse, scope);
            program.accept(vv, tt);            
         

            } catch (FileNotFoundException | ParseException e) {
                System.out.println("Vapor error");
         }
    }
}
