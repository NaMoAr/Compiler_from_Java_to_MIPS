package typecheck;

import java.util.*;
import syntaxtree.*;
import visitor.*;

public class FirstPassVisitor extends GJDepthFirst<Table, Table> {
/*
    First pass scans through the input program and builds the symbol table.
    As per required, checks for overloading parameters
    In addition, determines if passed in parameters, class, method, and variable names are distinct
    Referring the to the MiniJavaTypeSystem for their type rules.
*/
    
    @Override
    public Table visit(Goal n, Table t) {
        
        // Initiate reading the input program
        // at the root node, main class id and its type
        MainClass mcNode = n.f0; // MainClass()
        NodeListOptional nloNode = n.f1; // ( TypeDeclaration() )*
        NodeList node = new NodeList(mcNode);
        for (Enumeration<Node> e = nloNode.elements(); e.hasMoreElements(); ) {
            node.addNode(e.nextElement()); // add node of each class instantiated
        }

        // 7.3 Main Class Type Rule (18)
        boolean b = CheckDistinct.distinctClass(node);

        if (b) {
            mcNode.accept(this, t);
            nloNode.accept(this, t);
        } else {
            ProgramError.detectError();
        }
        return null;
    }
   
    @Override
    public Table visit(MainClass n, Table t) {
        SymbolTable id = SymbolTable.setTypeId(CheckDistinct.classId(n));
        SubtypeTable.addSub(id, id); // throw id in case class is subclass or superclass

        // generate big symbol table
        Table tt = new Table(t, n);
        t.addScope(id, n, tt); // add main class id into big symbol table

        // Checks if each variable id are distinct
        NodeListOptional nloNode1 = n.f14; // ( VarDeclaration() )*
        boolean b = CheckDistinct.distinctVar(nloNode1);

        if(b) {
            nloNode1.accept(this, tt);
        } 
        else {
            ProgramError.detectError();
        }
        return null;
    }

    @Override
    public Table visit(ClassDeclaration n, Table t) {
        SymbolTable id = SymbolTable.setTypeId(CheckDistinct.classId(n));
        SubtypeTable.addSub(id, id);

        Table tt = new Table(t, n);
        t.addScope(id, n, tt);
        
        NodeListOptional nloNode1 = n.f3; // ( VarDeclaration() )*
        NodeListOptional nloNode2 = n.f4; // ( MethodDeclaration() )*

        boolean b1 = CheckDistinct.distinctVar(nloNode1);
        boolean b2 = CheckDistinct.distinctMethod(nloNode2);

        if (b1) {
            nloNode1.accept(this, tt);

            if (b2){
                nloNode2.accept(this, tt);
            }
            else{
                ProgramError.detectError();
            }
        } 
        else {
            ProgramError.detectError();
        }
        return null;
    }

   
    @Override
    public Table visit(ClassExtendsDeclaration n, Table t) {
        SymbolTable id = SymbolTable.setTypeId( CheckDistinct.classId(n) );
        SubtypeTable.addSub(id, id);

        Identifier iNode = n.f3; // Identifier()
        String identId = CheckDistinct.identifierId(iNode);
        SymbolTable base = SymbolTable.setTypeId( identId );
        Collector b = t.find(base);

        if (b != null) {
            SubtypeTable.addSub(base, id);

            Table tt = new Table(b.getTable(), n);
            t.addScope(id, n, tt);

            NodeListOptional nloNode1 = n.f5; // ( VarDeclaration() )*
            NodeListOptional nloNode2 = n.f6; // ( MethodDeclaration() )*
            boolean b1 = CheckDistinct.distinctVar(nloNode1);
            boolean b2 = CheckDistinct.distinctMethod(nloNode2);

            if (b1) {
                nloNode1.accept(this, tt);

                if ( b2 ){
                    nloNode2.accept(this, tt);
                }
                else{
                    ProgramError.detectError();
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
    public Table visit(VarDeclaration n, Table t) {
        Identifier iNode = n.f1; // Identifier()
        String identId = CheckDistinct.identifierId(iNode);
        SymbolTable id = SymbolTable.setTypeId( identId );
        t.addScope(id, n, t); // Add to second pass visitor
        return null;
    }
    
    @Override
    public Table visit(MethodDeclaration n, Table t) {
        SymbolTable id = SymbolTable.setTypeId(CheckDistinct.methodId(n));
        Collector c = t.find(id);
        
        if (c != null) {
            MethodDeclaration mdCast = (MethodDeclaration)c.getTypeNode();
            MethodTable root = CheckDistinct.methodType( mdCast );
            MethodTable child = CheckDistinct.methodType(n);
            
            // 7.4 Type Declarations Rule (20)
            // for no Overloading
            boolean b = !root.equals(child);

            // If overriding methods exist
            if (b) {
                ProgramError.detectError();
            }
        }

        Table tt = new Table(t, n);
        t.addScope(id, n, tt);
        boolean b1 = n.f4.present();

        if (b1) {
            // Checks for distinct parameters
            Node nNode = n.f4.node; // ( FormalParameterList() )? -> ( A )? or [ A ]
            FormalParameterList fplCast = (FormalParameterList)nNode;
            boolean b2 = CheckDistinct.distinctParams( fplCast );
            
            if ( b2 ) {
                n.f4.accept(this, tt);
            }
            else {
                ProgramError.detectError();
            }
        }

        NodeListOptional nloNode = n.f7; // ( VarDeclaration() )*
        boolean b3 = CheckDistinct.distinctVar(nloNode);
        
        if ( b3 ) {
            n.f7.accept(this, tt);
        } 
        else {
            ProgramError.detectError();
        }
        return null;
    }

    @Override
    public Table visit(FormalParameter n, Table t) {
        Identifier iNode = n.f1; // Identifier()
        String identId = CheckDistinct.identifierId(iNode);
        SymbolTable id = SymbolTable.setTypeId( identId );

        t.addScope(id, n, t);   // Add to second pass visitor
        return null;
    }
    
} // End of First Pass Visitor
