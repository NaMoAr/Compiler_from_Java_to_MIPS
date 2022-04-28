package typecheck;

import java.util.*;
import syntaxtree.*;
import visitor.*;

public class SecondPassVisitor extends GJDepthFirst<ExpressionTable, Table> {
/*
   Second pass type checks return types of the declared methods from their class
   The methods also check their local variables and parameters.
   In addition, checks expressions, assignments, and statements
   Referring the to MiniJavaTypeSystem for their Type Rules
*/   
    @Override
    public ExpressionTable visit(MainClass n, Table t) {
        SymbolTable id = SymbolTable.setTypeId(CheckDistinct.classId(n));
        Collector b = t.find(id);
        NodeListOptional node = n.f15; // ( Statement() )*

        node.accept(this, b.getTable());
        return null;
    }

    @Override
    public ExpressionTable visit(ClassDeclaration n, Table t) {
        SymbolTable id = SymbolTable.setTypeId(CheckDistinct.classId(n));
        Collector b = t.find(id);
        NodeListOptional node = n.f4; // ( MethodDeclaration() )*

        node.accept(this, b.getTable());
        return null;
    }
   
    @Override
    public ExpressionTable visit(ClassExtendsDeclaration n, Table t) {
        SymbolTable id = SymbolTable.setTypeId(CheckDistinct.classId(n));
        Collector b = t.find(id);
        NodeListOptional node = n.f6; // ( MethodDeclaration() )*

        node.accept(this, b.getTable());
        return null;
    }

    @Override
    public ExpressionTable visit(VarDeclaration n, Table t) {
        Type node = n.f0; // Type()
        ExpressionTable rt = CheckDistinct.determExpr(node);
        boolean b1 = !CheckDistinct.ifPrimitive(rt.getType());
        boolean b2 = !SubtypeTable.hasSub(SymbolTable.setTypeId(rt.getType()));

        if ( b1 ) {
            if ( b2 ) {
                ProgramError.detectError();
            }
        }
        return null;
    }

    @Override
    public ExpressionTable visit(MethodDeclaration n, Table t) {
        SymbolTable id = SymbolTable.setTypeId(CheckDistinct.methodId(n));
        Collector c = t.find(id);
        Table tt = c.getTable();

        n.f8.accept(this, tt);

        Expression node = n.f10; // Expression()
        ExpressionTable retType = node.accept(this, tt);
        
        // in type A, if inside class C, expr e has type t
        // Type Judgement (6)
        // A, C |= e : t
        Type tnode = n.f1; 
        boolean b = !CheckDistinct.determExpr(tnode).equals(retType);

        // checks for the method's return type value
        if ( b ) {
            ProgramError.detectError();
        }
        return null;
    }
    
    @Override
    public ExpressionTable visit(FormalParameter n, Table t) {
        Type node = n.f0;
        ExpressionTable rt = CheckDistinct.determExpr(node);
        boolean b1 = !CheckDistinct.ifPrimitive(rt.getType());
        boolean b2 = !SubtypeTable.hasSub(SymbolTable.setTypeId(rt.getType()));

        if ( b1 ) {
            if ( b2 ) {
                ProgramError.detectError();
            }
        }
        return null;
    }

    @Override
    public ExpressionTable visit(AssignmentStatement n, Table t) {
        Identifier node = n.f0; // Identifier()
        Expression enode = n.f2; // Expression()
        SymbolTable id = SymbolTable.setTypeId(CheckDistinct.identifierId(node));
        Collector c = t.find(id);
        
        // 7.6 Statement Type Rule (23)
        // A(id) = t_1
        if (c != null) {
            Type dpv = CheckDistinct.determParamVar(c.getTypeNode());
            ExpressionTable idType = CheckDistinct.determExpr(dpv);
            ExpressionTable et = enode.accept(this, t);

            boolean b1 = !et.getType().equals(CheckDistinct.NULL); // A,C |- e : t_2 
            boolean b2 = !idType.equals(et);                        // t_2 <= t_1
            boolean b3 = (idType.getNode() instanceof Identifier);  // identifier type check is from class id
            boolean b4 = (et.getNode() instanceof Identifier);      // expression type check is from class id
            boolean b = ( b3 && b4 ); // if b3 and b4 share class id

            SymbolTable setType = SymbolTable.setTypeId( idType.getType() );

            boolean b5 = !SubtypeTable.isSub(SymbolTable.setTypeId( et.getType() ), setType );
            
            if ( b1 ) {
                if ( b2 ) {
                    if ( b ) {
                        if ( b5 ) {
                            ProgramError.detectError();
                        }
                    } 
                    else {
                        ProgramError.detectError();
                    }
                }
            } 
            else {
                ProgramError.detectError();
            }
        } 
        else {
            ProgramError.detectError();
        }
        return null;
    }

    @Override
    public ExpressionTable visit(ArrayAssignmentStatement n, Table t) {
        Identifier iNode = n.f0; // Identifier()
        Expression eNode1 = n.f2; // Expression()
        Expression eNode2 = n.f5; // Expression()
        String identId = CheckDistinct.identifierId(iNode);
        SymbolTable id = SymbolTable.setTypeId(identId);
        Collector c = t.find(id);
        
        // 7.6 Statement Type Rule (24)
        // A(id) = int[]
        if (c != null) {
            VarDeclaration varDec = (VarDeclaration)c.getTypeNode();
            Type tnode = varDec.f0; // Type()

            ExpressionTable idType = CheckDistinct.determExpr(tnode);

            boolean b1 = idType.getType().equals(CheckDistinct.INT_ARRAY);

            if (b1) {
                ExpressionTable indexType = eNode1.accept(this, t);

                // A,C |- e_1 : int
                boolean b2 = !indexType.getType().equals(CheckDistinct.NULL);

                if(b2){

                    boolean b3 = indexType.getType().equals(CheckDistinct.INT);

                    if(b3) {
                        ExpressionTable et = eNode2.accept(this, t);

                        // A,C |- e_2 : int
                        boolean b4 = !et.getType().equals(CheckDistinct.NULL);
                        boolean b5 = !et.getType().equals(CheckDistinct.INT);

                        if(b4) {
                            if(b5) {
                                ProgramError.detectError();
                            }
                        } 
                        else {
                            ProgramError.detectError();
                        } // end of if else

                    } 
                    else {
                        ProgramError.detectError();
                    } // end of if else

                } // end of if else 

                else {
                    ProgramError.detectError();
                }
            } 
            else {
                ProgramError.detectError();
            } // end of if else

        } 
        else {
            ProgramError.detectError();
        } // end of if else

        return null;
    }
    
    public ExpressionTable visit(IfStatement n, Table t) {
        Expression eNode = n.f2; // Expression()
        ExpressionTable condtional = eNode.accept(this, t);

        // 7.6 Statement Type Rule (25)
        // A,C |- e : boolean
        boolean b = condtional.getType().equals(CheckDistinct.BOOLEAN);

        if (b) {
            Statement sNode1 = n.f4; // Statement()
            Statement sNode2 = n.f6; // Statement()

            // A,C |- s_1
            sNode1.accept(this, t);

            // A,C |- s_2
            sNode2.accept(this, t);
        } 
        else {
            ProgramError.detectError();
        }
        return null;
    }
    
    @Override
    public ExpressionTable visit(WhileStatement n, Table t) {
        Expression node = n.f2; // Expression()
        ExpressionTable condtional = node.accept(this, t);

        // 7.6 Statement Type Rule (26)
        // A,C |- e : boolean
        boolean b = condtional.getType().equals(CheckDistinct.BOOLEAN);
        
        if (b) {
            Statement snode = n.f4; // Statement()
            snode.accept(this, t); // A,C |- s
        } 
        else {
            ProgramError.detectError();
        }
        return null;
    }

    @Override
    public ExpressionTable visit(PrintStatement n, Table t) {
        Expression node = n.f2; // Expression()
        ExpressionTable printS = node.accept(this, t);

        // 7.6 Statement Type Rule (27)
        // A,C |- e : int
        boolean b = !printS.getType().equals(CheckDistinct.INT);
        
        if (b) {
            ProgramError.detectError();
        }
        return null;
    }

    @Override
    public ExpressionTable visit(Expression n, Table t) {
        NodeChoice nc = n.f0; // The grammar of which expression occurs here

        return nc.accept(this, t);
    }
    
    @Override
    public ExpressionTable visit(AndExpression n, Table t) {
        ExpressionTable et;
        PrimaryExpression peNode1 = n.f0; // PrimaryExpression()
        PrimaryExpression peNode2 = n.f2; // PrimaryExpression()
        ExpressionTable lhs = peNode1.accept(this, t);
        ExpressionTable rhs = peNode2.accept(this, t);

        // 7.7 Expressions and Primary Expressions Type Rule (28)
        // A,C |- p1 : boolean
        boolean b1 = !lhs.getType().equals(CheckDistinct.BOOLEAN);

        // A,C |- p_2 : boolean
        boolean b2 = !rhs.getType().equals(CheckDistinct.BOOLEAN);
        boolean b = (b1 || b2);

        if (b) {
            ProgramError.detectError();
        }

        // A,C |- (p_1 && p_2) : boolean
        et = new ExpressionTable(n, CheckDistinct.BOOLEAN);
        return et;
    }
    
    @Override
    public ExpressionTable visit(CompareExpression n, Table t) {
        ExpressionTable et; 
        PrimaryExpression peNode1 = n.f0; // PrimaryExpression()
        PrimaryExpression peNode2 = n.f2; // PrimaryExpression()
        ExpressionTable lhs = peNode1.accept(this, t);
        ExpressionTable rhs = peNode2.accept(this, t);

        // 7.7 Expressions and Primary Expressions Type Rule (29)
        // A,C |- p_1: int
        boolean b1 = !lhs.getType().equals(CheckDistinct.INT);

        // A,C |- p_2 : int
        boolean b2 = !rhs.getType().equals(CheckDistinct.INT);
        boolean b = (b1 || b2);

        if (b) {
            ProgramError.detectError();
        }

        // A,C |- (p_1 < p_2) : boolean
        et = new ExpressionTable(n, CheckDistinct.BOOLEAN);
        return et;
    }

    @Override
    public ExpressionTable visit(PlusExpression n, Table t) {
        ExpressionTable et; 
        PrimaryExpression peNode1 = n.f0; // PrimaryExpression()
        PrimaryExpression peNode2 = n.f2; // PrimaryExpression()
        ExpressionTable lhs = peNode1.accept(this, t);
        ExpressionTable rhs = peNode2.accept(this, t);

        // 7.7 Expressions and Primary Expressions Rule (30)
        boolean b1 = !lhs.getType().equals(CheckDistinct.INT);
        boolean b2 = !rhs.getType().equals(CheckDistinct.INT);
        boolean b = (b1 || b2);

        if (b) {
            ProgramError.detectError();
        }
        et = new ExpressionTable(n, CheckDistinct.INT);
        return et;
    }

    @Override
    public ExpressionTable visit(MinusExpression n, Table t) {
        ExpressionTable et;
        PrimaryExpression peNode1 = n.f0; // PrimaryExpression()
        PrimaryExpression peNode2 = n.f2; // PrimaryExpression()
        ExpressionTable lhs = peNode1.accept(this, t);
        ExpressionTable rhs = peNode2.accept(this, t);
        
        // 7.7 Expressions and Primary Expressions Type Rule (31)
        boolean b1 = !lhs.getType().equals(CheckDistinct.INT);
        boolean b2 = !rhs.getType().equals(CheckDistinct.INT);
        boolean b = (b1 || b2);

        if (b) {
            ProgramError.detectError();
        }
        et = new ExpressionTable(n, CheckDistinct.INT);
        return et;
    }

    @Override
    public ExpressionTable visit(TimesExpression n, Table t) {
        ExpressionTable et;
        PrimaryExpression peNode1 = n.f0; // PrimaryExpression()
        PrimaryExpression peNode2 = n.f2; // PrimaryExpression()
        ExpressionTable lhs = peNode1.accept(this, t);
        ExpressionTable rhs = peNode2.accept(this, t);

        // 7.7 Expressions and Primary Expressions Type Rule (32)
        boolean b1 = !lhs.getType().equals(CheckDistinct.INT);
        boolean b2 = !rhs.getType().equals(CheckDistinct.INT);
        boolean b = (b1 || b2);

        if (b) {
            ProgramError.detectError();
        }
        et = new ExpressionTable(n, CheckDistinct.INT);
        return et;
    }

    @Override
    public ExpressionTable visit(ArrayLookup n, Table t) {
        ExpressionTable et;
        PrimaryExpression peNode1 = n.f0; // PrimaryExpression()
        PrimaryExpression peNode2 = n.f2; // PrimaryExpression()
        ExpressionTable array = peNode1.accept(this, t);
        ExpressionTable index = peNode2.accept(this, t);

        // 7.7 Expressions and Primary Expressions Type Rule (33)
        boolean b1 = !array.getType().equals(CheckDistinct.INT_ARRAY);
        boolean b2 = !index.getType().equals(CheckDistinct.INT);
        boolean b = (b1 || b2);

        if (b) {
            ProgramError.detectError();
        }
        
        et = new ExpressionTable(n, CheckDistinct.INT);
        return et;
    }

    @Override
    public ExpressionTable visit(ArrayLength n, Table t) {
        ExpressionTable et;
        PrimaryExpression peNode = n.f0; // PrimaryExpression()
        ExpressionTable array = peNode.accept(this, t);
        
        // 7.7 Expressions and Primary Expressions Type Rule (34)
        boolean b = !array.getType().equals(CheckDistinct.INT_ARRAY);

        if (b) {
            ProgramError.detectError();
        }
        et = new ExpressionTable(n, CheckDistinct.INT);
        return et;
    }
    
    @Override
    public ExpressionTable visit(MessageSend n, Table t) {
        ExpressionTable et;
        PrimaryExpression peNode = n.f0; // PrimaryExpression()
        ExpressionTable p = peNode.accept(this, t);
        String type;

        // 7.7 Expressions and Primary Expressions Type Rule (35)
        boolean b0 = CheckDistinct.ifPrimitive(p.getType());
        boolean b1 = p.getType().equals(CheckDistinct.NULL);

        if ( b0 ) {
            type = CheckDistinct.NULL;
            ProgramError.detectError();
        }

        else if( b1 ) {
            type = CheckDistinct.NULL;
            ProgramError.detectError();
        }

        // THE MOST CRITICAL TYPE CHECK, THE METHOD CALL IS CONTAINED BELOW

        // p is an identifier of some class D
        else { 
            Collector c = t.find( SymbolTable.setTypeId(p.getType()) );
            // A,C |- p : D
            if (c != null) {
                Table tt = c.getTable();
                Identifier iNode = n.f2;
                SymbolTable m = SymbolTable.setTypeId(CheckDistinct.identifierId(iNode));
                Collector cc = tt.find(m);

                // methodtype(D, id) = (t_1', ..., t_n') --> t
                if (cc != null) { // non empty method type--has some list of types
                    MethodDeclaration mdCast = (MethodDeclaration)cc.getTypeNode();
                    MethodTable mt = CheckDistinct.methodType(mdCast);
                    NodeOptional noNode = n.f4;
                    boolean b2 = noNode.present();

                    // A,C |- e_i : t_i
                    if ( b2 ) {
                        LinkedList<String> referenceParams = mt.getParams(); // t_i
                        LinkedList<String> actualParams = new LinkedList<>(); // t_i'
                        ExpressionList el = (ExpressionList)n.f4.node;

                        actualParams.add(el.f0.accept(this, t).getType()); // Expression()

                        for ( Enumeration<Node> e = el.f1.elements(); e.hasMoreElements(); ) {
                            ExpressionRest er = (ExpressionRest) e.nextElement();
                            actualParams.add(er.f1.accept(this, t).getType());
                        }
                        boolean b3 = ( referenceParams.size() == actualParams.size() );

                        // A,C |- e_i : t_i
                        if ( b3 ) {
                            for (int i = 0; i < referenceParams.size(); i++) {
                                String reference = referenceParams.get(i); // t_i
                                String actual = actualParams.get(i);       // t_i'
                                boolean b4 = !reference.equals(actual);
                                
                                if ( b4 ) {
                                    // Type mismatch occurs
                                    boolean b5 = !CheckDistinct.ifPrimitive(reference); // t_i
                                    boolean b6 = !CheckDistinct.ifPrimitive(actual);    // t_i'
                                    boolean b = ( b5 && b6 );

                                    if ( b ) {
                                        // t_i <= t_i'
                                        SymbolTable stAct = SymbolTable.setTypeId(actual);
                                        SymbolTable stRef = SymbolTable.setTypeId(reference);
                                        boolean b7 = !SubtypeTable.isSub(stAct, stRef);

                                        if ( b7 ) {
                                            ProgramError.detectError();
                                        }
                                    } 
                                    else {
                                        ProgramError.detectError();
                                    }
                                }
                            }
                        } 
                        else {
                            ProgramError.detectError();
                        }
                    }
                    // A,C |- p .id(e_1, ..., e_n) : t
                    type = mt.getReturnType().getType(); // p is most definitely the receiver 
                } 
                else {
                    type = CheckDistinct.NULL;
                    ProgramError.detectError();
                }
            } 
            else {
                type = CheckDistinct.NULL;
                ProgramError.detectError();
            }
        }
        et = new ExpressionTable(n, type); // Method call type checking is complete
        return et;
    }

    @Override
    public ExpressionTable visit(PrimaryExpression n, Table t) {
        ExpressionTable et;
        NodeChoice nc = n.f0; 
        String type;

        if (nc.which == 0) { // 7.6 Statement Type Rule (36)
            type = CheckDistinct.INT; // A,C |- c : int
        }

        else if (nc.which == 1) { // 7.6 Statement Type Rule (37)
            type = CheckDistinct.BOOLEAN; // A,C |- true : boolean
        }

        else if (nc.which == 2) { // 7.6 Statement Type Rule (38)
            type = CheckDistinct.BOOLEAN; // A,C |- false : boolean
        }

        else if (nc.which == 3) { // 7.6 Statement Type Rule (39)
            String identId = CheckDistinct.identifierId((Identifier)nc.choice);
            SymbolTable id = SymbolTable.setTypeId(identId);
            Collector c = t.find(id); // id is defined in the domain(A) s.t. A, C |- id : A(id)

            if (c != null) {
                Type getNode = CheckDistinct.determParamVar(c.getTypeNode());
                return CheckDistinct.determExpr(getNode);
            }
            else {
                type = CheckDistinct.NULL;
                ProgramError.detectError();
            }
        }

        else if (nc.which == 4) { // 7.7 Expressions and Primary Expressions Type Rule (40)
            type = CheckDistinct.classId(t.getRoot().getNode()); // A,C |- this : C
        } 

        else if (nc.which == 5) { // 7.7 Expressions and Primary Expressions Type Rule (41)
            et = ( (ArrayAllocationExpression)nc.choice ).f3.accept(this, t); // Expression()
            boolean b = !et.getType().equals(CheckDistinct.INT);

            if (b) {
                ProgramError.detectError();
            }
            
            // A,C |- e : int --> A,C |- new int[e] : int[]
            type = CheckDistinct.INT_ARRAY;
        } 
        
        else if (nc.which == 6) { // 7.7 Expressions and Primary Expressions Type Rule (42)
            String identId = CheckDistinct.identifierId( ((AllocationExpression)nc.choice).f1);
            SymbolTable id = SymbolTable.setTypeId( identId ); // Identifier()
            boolean b = SubtypeTable.hasSub(id);

            if(b) {
                type = id.toString(); // A,C |- new id() : id
            } 
            else {
                type = CheckDistinct.NULL;
                ProgramError.detectError();
            }
        }

        else if (nc.which == 7) { // 7.7 Expressions and Primary Expressions Type Rule (43)
            et = ( (NotExpression) nc.choice ).f1.accept(this, t); // Expression()
            boolean b = !et.getType().equals(CheckDistinct.BOOLEAN);

            if ( b ) {
                ProgramError.detectError();
            }

            // A,C |- e : boolean --> A,C |- !e : boolean
            type = CheckDistinct.BOOLEAN;
        }

        else { // 7.7 Expressions and Primary Expressions Type Rule (44)
            return ( (BracketExpression)nc.choice ).f1.accept(this, t); // Expression()
        }

        // A,C |- e : t --> A,C |- (e) : t
        et = new ExpressionTable(nc.choice, type); // Primary Expression type checking complete
        return et;
    }

} // End of Second Pass Visitor
