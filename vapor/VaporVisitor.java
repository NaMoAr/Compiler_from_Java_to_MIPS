package vapor;
import syntaxtree.*;
import visitor.*;
import typecheck.*;
import java.util.*;

public class VaporVisitor extends GJDepthFirst<Variable, TranslateTable>{

    /*  Object Variable Glossary
        Translator translate
        - recieve classrecord and vtable info from translator w/ their labels 
          and return as appended string

        Variable tVar
        - obtains translate/translation variable [t.x]

        Variable temp
        - obtain temp variable from tVar

        Variable nullPtr
        - check when if label goes to null
        
        Jump jump
        - get the jump label for goto
    */
    
    private boolean allocFlag = false;
  
    @Override
    public Variable visit(Goal n, TranslateTable tt) {
        Translator translate = tt.getTranslator();
       
        for (Iterator<Collector> it = tt.getTable().tableIterator(); it.hasNext(); ) {
            Collector c = it.next();

            boolean b = !(c.getTypeNode() instanceof MainClass);

            if (b) {
                translate.getClassRecord(c);
                translate.appendVTable(c);
                translate.appendLine();
            }
        }

        MainClass mcNode = n.f0;
        NodeListOptional noNode = n.f1; 
        
        mcNode.accept(this, tt);
        noNode.accept(this, tt);

        if(allocFlag){
            translate.appendLine();
            translate.appendAlloc();
        }
        return null;
    }
   
    @Override
    public Variable visit(MainClass n, TranslateTable tt) {
        Translator translate = tt.getTranslator();
        SymbolTable id = SymbolTable.setTypeId( CheckDistinct.classId(n) );
        Collector c = tt.getTable().find(id);
        Table table = c.getTable();

        translate.appendMethod(c);
        translate.incrIndent();
        translate.getVar().countMethodVar();

        NodeListOptional noNode = n.f15;
        TranslateTable ttp = new TranslateTable(table, translate);

        noNode.accept(this, ttp);

        translate.appendReturn();
        translate.decrIndent();
        return null;
    }

    @Override
    public Variable visit(ClassDeclaration n, TranslateTable tt) {
        SymbolTable id = SymbolTable.setTypeId( CheckDistinct.classId(n) );
        Collector c = tt.getTable().find(id);

        NodeListOptional nloNode = n.f4;
        TranslateTable ttp = new TranslateTable( c.getTable(), tt.getTranslator() );

        nloNode.accept(this, ttp);
        return null;
    }
    
    @Override
    public Variable visit(ClassExtendsDeclaration n, TranslateTable tt) {
        SymbolTable id = SymbolTable.setTypeId( CheckDistinct.classId(n) );
        Collector c = tt.getTable().find(id);

        NodeListOptional nloNode = n.f6;
        TranslateTable ttp = new TranslateTable( c.getTable(), tt.getTranslator() );

        nloNode.accept(this, ttp);
        return null;
    }

    @Override
    public Variable visit(MethodDeclaration n, TranslateTable tt) {
        Translator translate = tt.getTranslator();
        SymbolTable id = SymbolTable.setTypeId( CheckDistinct.methodId(n) );
        Collector c = tt.getTable().find(id);
        Table table = c.getTable();

        translate.appendLine();
        translate.appendMethod(c);
        translate.incrIndent();
        translate.getVar().countMethodVar();

        TranslateTable ttp = new TranslateTable(table, translate);
        NodeListOptional nloNode = n.f8;

        nloNode.accept(this, ttp);

        Expression eNode = n.f10;
        Variable ret = VaporCheck.MemoryCall( eNode.accept(this, ttp), ttp );
        translate.appendReturn(ret);

        translate.decrIndent();
        return null;
    }

    @Override
    public Variable visit(AssignmentStatement n, TranslateTable tt){
        Translator translate = tt.getTranslator();
        Identifier iNode = n.f0;
        SymbolTable id = SymbolTable.setTypeId( CheckDistinct.identifierId(iNode) );
       
        Variable label = VaporCheck.idLabel(id, tt);

        Expression eNode = n.f2;
        Variable expr = eNode.accept(this, tt);
       
        boolean b1 = expr.isCall() || expr.isMemory();
        boolean b2 = label.isMemory() && b1;

        if(b2){ expr = VaporCheck.MemoryCall(expr, tt); }
        translate.appendAssignment(label, expr);
        return null;
    }

    public Variable visit(ArrayAssignmentStatement n, TranslateTable tt) {
        Translator translate = tt.getTranslator();
        Identifier iNode = n.f0;

        SymbolTable id = SymbolTable.setTypeId( CheckDistinct.identifierId(iNode) );
        Variable idLabel = VaporCheck.idLabel(id, tt);
        Variable nullPtr = VaporCheck.nullPointer(idLabel, tt); 

        Expression eNode1 = n.f2;
        Variable indentLabel = VaporCheck.MemoryCall( eNode1.accept(this, tt), tt ); 
        Variable arrElement = VaporCheck.outBounds(nullPtr, indentLabel, tt);

        Expression eNode2 = n.f5;
        Variable expr = eNode2.accept(this, tt);

        boolean b = expr.isCall() || expr.isMemory();

        if (b) {
            expr = VaporCheck.MemoryCall(expr, tt);
        }
        translate.appendAssignment(arrElement, expr);
        return null;
    }

    public Variable visit(IfStatement n, TranslateTable tt) {
        Translator translate = tt.getTranslator();
        Jump jump = translate.getJump();
        Jump elseLabel = jump.appendIfElse();
        Jump endLabel = jump.appendIfEnd();

        Expression eNode = n.f2;
        Variable varCheck = VaporCheck.MemoryCall( eNode.accept(this, tt), tt );
        translate.appendIfZero(varCheck, elseLabel);
        translate. incrIndent();

        Statement sNode1 = n.f4;
        sNode1.accept(this, tt);
        translate.appendGoto(endLabel);
        translate.decrIndent();

        translate.appendJump(elseLabel);
        translate.incrIndent();

        Statement sNode2 = n.f6;
        sNode2.accept(this, tt);
        translate.decrIndent();

        translate.appendJump(endLabel);
        return null;
    }

    public Variable visit(WhileStatement n, TranslateTable tt) {
        Translator translate = tt.getTranslator();
        Jump jump = translate.getJump();
        Jump topLabel = jump.appendWhileTop();
        Jump endLabel = jump.appendWhileEnd();

        translate.appendJump(topLabel);

        Expression eNode = n.f2;
        Variable varCheck = VaporCheck.MemoryCall( eNode.accept(this, tt), tt );
        translate.appendIfZero(varCheck, endLabel);
        translate.incrIndent();

        Statement sNode = n.f4;
        sNode.accept(this, tt);
        translate.appendGoto(topLabel);
        translate.decrIndent();

        translate.appendJump(endLabel);
        return null;
    }

    public Variable visit(PrintStatement n, TranslateTable tt) {
        Translator translate = tt.getTranslator();
        Expression eNode = n.f2;
        Variable expr = VaporCheck.MemoryCall( eNode.accept(this, tt), tt) ;
        translate.appendPrintIntS(expr);
        return null;
    }

    public Variable visit(Expression n, TranslateTable tt) {
        NodeChoice ncNode = n.f0;
        return ncNode.accept(this, tt);
    }

    public Variable visit(AndExpression n, TranslateTable tt) {
        Translator translate = tt.getTranslator();
        Jump jump = translate.getJump();
        Jump elseLabel = jump.appendAndElse();
        Jump endLabel = jump.appendAndEnd();
        Variable tVar = translate.getVar();
     
        Variable temp = tVar.appendTempVar();

        PrimaryExpression peNode1 = n.f0;
        Variable cond1 = VaporCheck.MemoryCall( peNode1.accept(this, tt), tt );
        translate.appendIfZero(cond1, elseLabel);
        translate.incrIndent();

        PrimaryExpression peNode2 = n.f2;
        Variable cond2 = peNode2.accept(this, tt);
        translate.appendAssignment(temp, cond2);
        translate.appendGoto(endLabel);
        translate.decrIndent();

        translate.appendJump(elseLabel);
        translate.incrIndent();
        translate.appendAssignment(temp, "0");
        translate.decrIndent();

        translate.appendJump(endLabel);
        return temp;
    }

    public Variable visit(CompareExpression n, TranslateTable tt) {
        Translator translate = tt.getTranslator();
        Variable tVar = translate.getVar();

        PrimaryExpression peNode1 = n.f0;
        PrimaryExpression peNode2 = n.f2;

        Variable a = VaporCheck.MemoryCall( peNode1.accept(this, tt), tt );
        Variable b = VaporCheck.MemoryCall( peNode2.accept(this, tt), tt );

        String Param = a.toString();
        String Value = b.toString();
        Variable cmp = tVar.appendFuncCall( BuiltInOps.LtS(Param, Value) );
        return cmp;
    }

    public Variable visit(PlusExpression n, TranslateTable tt) {
        Translator translate = tt.getTranslator();
        Variable tVar = translate.getVar();

        PrimaryExpression peNode1 = n.f0;
        PrimaryExpression peNode2 = n.f2;

        Variable a = VaporCheck.MemoryCall( peNode1.accept(this, tt), tt );
        Variable b = VaporCheck.MemoryCall( peNode2.accept(this, tt), tt );

        String Param = a.toString();
        String Value = b.toString();
        Variable plus = tVar.appendFuncCall( BuiltInOps.Add(Param, Value) );
        return plus;
    }
  
    public Variable visit(MinusExpression n, TranslateTable tt) {
        Translator translate = tt.getTranslator();
        Variable tVar = translate.getVar();

        PrimaryExpression peNode1 = n.f0;
        PrimaryExpression peNode2 = n.f2;

        Variable a = VaporCheck.MemoryCall( peNode1.accept(this, tt), tt );
        Variable b = VaporCheck.MemoryCall( peNode2.accept(this, tt), tt );

        String Param = a.toString();
        String Value = b.toString();
        Variable sub = tVar.appendFuncCall( BuiltInOps.Sub(Param, Value) );
        return sub;
    }

    public Variable visit(TimesExpression n, TranslateTable tt) {
        Translator translate = tt.getTranslator();
        Variable tVar = translate.getVar();

        PrimaryExpression peNode1 = n.f0;
        PrimaryExpression peNode2 = n.f2;

        Variable a = VaporCheck.MemoryCall(peNode1.accept(this, tt), tt);
        Variable b = VaporCheck.MemoryCall(peNode2.accept(this, tt), tt);

        String Param = a.toString();
        String Value = b.toString();
        Variable mul = tVar.appendFuncCall( BuiltInOps.MulS( Param, Value ) );
        return mul;
    }
    
    public Variable visit(ArrayLookup n, TranslateTable tt) {
        PrimaryExpression peNode1 = n.f0;
        Variable array = peNode1.accept(this, tt);
        Variable nullPtr = VaporCheck.nullPointer(array, tt);
        PrimaryExpression peNode2 = n.f2;
        Variable indentLabel = peNode2.accept(this, tt);

        return VaporCheck.outBounds(nullPtr, indentLabel, tt);
    }

    public Variable visit(ArrayLength n, TranslateTable tt) {
        PrimaryExpression peNode = n.f0;
        Variable array = peNode.accept(this, tt);
        Variable nullPtr = VaporCheck.nullPointer(array, tt);

        return nullPtr.memVar();
    }
   
    public Variable visit(MessageSend n, TranslateTable tt) {
        Translator translator = tt.getTranslator();
        VTable vt = translator.getVTable();
        Variable tVar = translator.getVar();

        PrimaryExpression peNode = n.f0;
        Variable nullPtr = VaporCheck.nullPointer( peNode.accept(this, tt), tt );

        SecondPassVisitor secondPass = new SecondPassVisitor();
        ExpressionTable pt = peNode.accept( secondPass, tt.getTable() );

        String classId = pt.getType();

        String methodOffset = n.f2.f0.tokenImage;
        int offset = vt.getMethodOffset(classId, methodOffset);

        Variable temp = tVar.appendTempVar();
        translator.appendAssignment( temp, nullPtr.memVar() );
        String getTemp = temp.toString();
        translator.appendAssignment( temp, tVar.appendLocalVar( offset, getTemp ).memVar() );

        LinkedList<Variable> callParam = new LinkedList<>();

        NodeOptional noNode = n.f4;
        boolean b = noNode.present();

        if (b) {
            ExpressionList elCast = (ExpressionList)noNode.node;
            Expression eNode1 = elCast.f0;

            callParam.add(VaporCheck.MemoryCall( eNode1.accept(this, tt), tt) );

            NodeListOptional nloNode = elCast.f1;
            for (Enumeration<Node> e = nloNode.elements(); e.hasMoreElements(); ) {
                ExpressionRest er = (ExpressionRest)e.nextElement();
                Expression eNode2 = er.f1;
                callParam.add( VaporCheck.MemoryCall( eNode2.accept(this, tt), tt ) );
            }
        }

        Variable getCall = tVar.appendFuncCall( VaporCheck.FuncCall(temp, nullPtr, callParam) );
        return getCall;
    }

    public Variable visit(PrimaryExpression n, TranslateTable tt) {
        Translator translator = tt.getTranslator();
        ClassRecord cr = translator.getClassRecord();
        Variable tVar = translator.getVar();
        NodeChoice ncNode = n.f0;
        int ncVal = ncNode.which;
        Variable expr;

        if (ncVal == 0) { 
            IntegerLiteral ilCast = (IntegerLiteral)ncNode.choice;
            String constInt = ilCast.f0.tokenImage;
            expr = tVar.appendConstant(constInt);
        } 
        
        else if(ncVal == 1){ expr = tVar.appendConstant("1"); } 

        else if(ncVal == 2){ expr = tVar.appendConstant("0"); } 

        else if(ncVal == 3){
            Identifier identId = (Identifier)ncNode.choice;
            String typeId = CheckDistinct.identifierId(identId);
            SymbolTable id = SymbolTable.setTypeId( typeId );
            
            expr = VaporCheck.idLabel(id, tt);
        } 

        else if(ncVal == 4){ expr = tVar.appendThisVar(); } 

        else if(ncVal == 5){ 
            ArrayAllocationExpression aaeCast = (ArrayAllocationExpression)ncNode.choice;
            Expression eNode = aaeCast.f3;
            Variable size = eNode.accept(this, tt);

            allocFlag = true;
            expr = tVar.appendFuncCall( BuiltInOps.AllocArray(size) );
        } 

        else if(ncVal == 6){ 
            AllocationExpression aeCast = (AllocationExpression)ncNode.choice;
            Identifier iNode = aeCast.f1;
            String typeId = CheckDistinct.identifierId(iNode);
            SymbolTable id = SymbolTable.setTypeId( typeId );
           
            expr = tVar.appendTempVar();
            String classId = id.toString();
            int clazzSize = cr.getClassSize( classId );
            translator.appendAssignment( expr, BuiltInOps.HeapAllocZ( clazzSize ) );
            translator.appendAssignment( expr.memVar(), ":vmt_" + classId );
        } 

        else if(ncVal == 7){
            NotExpression neCast = (NotExpression)ncNode.choice;
            Expression eNode = neCast.f1;
            Variable b = eNode.accept(this, tt);

            b = VaporCheck.MemoryCall(b, tt);
            String getRHS = b.toString();
            expr = tVar.appendFuncCall( BuiltInOps.Sub( "1", getRHS ) );
        } 

        else {
            BracketExpression beCast = (BracketExpression)ncNode.choice;
            Expression eNode = beCast.f1;
            expr = eNode.accept(this, tt);
        }

        return expr;
    }

}