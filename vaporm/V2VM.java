package vaporm;

import cs132.util.*;
import cs132.vapor.parser.*;
import cs132.vapor.ast.*;
import cs132.vapor.ast.VBuiltIn.Op;

import java.io.*;
import java.util.*;

public class V2VM {
    public static VaporProgram parseVapor(InputStream in) throws ProblemException, IOException {
        Op[] ops = {
                Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
                Op.PrintIntS, Op.HeapAllocZ, Op.Error,
        };

        boolean allowLocals = true;
        String[] registers = null;
        boolean allowStack = false;

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
    public static void main(String[] args) throws IOException, ProblemException {
        LinearScan ls = new LinearScan();
        VaporToVaporm vtvm = new VaporToVaporm();

        InputStream stream = new FileInputStream("C:\\Users\\Najmeh\\Desktop\\Factorial.vapor");             
        VaporProgram program = parseVapor(stream);

        //VaporProgram program = parseVapor(System.in);

        vtvm.outputConstSegment(program.dataSegments);
        for (VFunction func : program.functions) {
            CFG flowGraph = LinearScan.generateFlowGraph(func);
            Liveness liveness = flowGraph.computLiveness();

            // Register allocation is applied to ech function separately.
            List<Interval> intervals = LinearScan.generateLiveIntervals(flowGraph, liveness);
            LinearScanMap map = ls.computeAllocation(intervals, func.params);
            vtvm.outputFunction(func, map, liveness);
            vtvm.appendLine();
        }
    }
}
