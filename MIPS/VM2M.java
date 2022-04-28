package MIPS;

import java.io.FileInputStream;
import java.io.InputStream;

import cs132.vapor.ast.VFunction;
import cs132.vapor.ast.VaporProgram;

import cs132.util.ProblemException;
import cs132.vapor.parser.VaporParser;
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.ast.VBuiltIn.Op;

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class VM2M {
    public static void main(String[] args) {
        try {



            InputStream stream = new FileInputStream("C:\\Users\\Najmeh\\Desktop\\Factorial.vaporm");    

            VaporProgram tree = parseVapor(stream);



            //VaporProgram tree = ParseVapor.parseVapor(System.in);

            // Translate the data section
            System.out.println(".data");
            for (int i = 0; i < tree.dataSegments.length; i++) {
                System.out.println(tree.dataSegments[i].ident + ":");
                for (int j = 0; j < tree.dataSegments[i].values.length; j++) {
                    String label = tree.dataSegments[i].values[j].toString().replace(":","");
                    System.out.println("    " + label);
                }
            }
            System.out.println("");

            // Translate the text section
            System.out.println(".text");

            // Jump-and-link to Main
            System.out.println("jal Main\nli $v0 10\nsyscall\n");

            // Translate function bodies
            VaporVisitor<Exception> vaporVisitor = new VaporVisitor<>();
            for (int i = 0; i < tree.functions.length; i++) {
                VFunction currFunction = tree.functions[i];
                vaporVisitor.setData(currFunction);

                // Translate function header
                System.out.println(currFunction.ident + ":");

                // For each line in each function in the program
                for (int j = 0; j < currFunction.body.length; j++) {
                    currFunction.body[j].accept(vaporVisitor);
                }

                vaporVisitor.printBuffer();

                System.out.println(); // newline for readability
            }

            // Translate useful syscalls and data
            System.out.println("\n_print:\nli $v0 1\nsyscall\nla $a0 _newline\nli $v0 4\nsyscall\njr $ra");
            System.out.println("\n_error:\nli $v0 4\nsyscall\nli $v0 10\nsyscall");
            System.out.println("\n_heapAlloc:\nli $v0 9\nsyscall\njr $ra");
            System.out.println("\n.data");
            System.out.println(".align 0");
            System.out.println("_newline: .asciiz \"\\n\"");
            System.out.println("_str0: .asciiz \"null pointer\\n\"");
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace(System.out);
        }
    }






    public static VaporProgram parseVapor(InputStream in) throws IOException {
        Op[] ops = {
                Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
                Op.PrintIntS, Op.HeapAllocZ, Op.Error,
        };

        boolean allowLocals = false;
        String[] registers = {
                "v0", "v1",
                "a0", "a1", "a2", "a3",
                "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
                "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
                "t8",
        };
        boolean allowStack = true;

        VaporProgram tree;
        try {
            tree = VaporParser.run(new InputStreamReader(in), 1, 1,
                    java.util.Arrays.asList(ops),
                    allowLocals, registers, allowStack);
        }
        catch (ProblemException ex) {
            System.out.println(ex.getMessage());
            return null;
        }

        return tree;
    }
}
   
