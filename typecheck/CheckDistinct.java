package typecheck;

import java.util.*;

import syntaxtree.*;

public class CheckDistinct {

    // Used in determining type literals 
    public static String NULL = "";
    public static String INT_ARRAY = "int[]";
    public static String BOOLEAN = "boolean";
    public static String INT = "int";

    public static MethodTable methodType(MethodDeclaration n) {
        MethodTable mt;
        NodeOptional noNode = n.f4; // ( FormalParameterList() )?
        LinkedList<String> params = new LinkedList<>();
        boolean b = noNode.present();

        if ( b ) {
            FormalParameterList node = (FormalParameterList)noNode.node;
            FormalParameter firstParam = node.f0; // FormalParameter()
            NodeListInterface restParam = node.f1; // ( FormalParameterRest() )*
            Type tNode = firstParam.f0;

            params.add( CheckDistinct.determExpr(tNode).getType() );
            for (Enumeration<Node> e = restParam.elements(); e.hasMoreElements(); ) {
                Type fpCast = ((FormalParameterRest)e.nextElement() ).f1.f0;

                String type = CheckDistinct.determExpr(fpCast).getType(); // FormalParameter() -> Type()
                params.add(type);
            }
        }
        Type tNode = n.f1; // Type()
        mt = new MethodTable( params, CheckDistinct.determExpr(tNode) );
        return mt;
    }

    public static String getParams(FormalParameterList n){
	    String params = CheckDistinct.NULL;
        Identifier id = n.f0.f1; // FormalParameter() -> Identifier()

	    params += identifierId(id);
	    return params;
    }

    // Distinct Functions
    public static boolean distinctParams(FormalParameterList n) {
        
        LinkedList<String> paramList = new LinkedList<>();
        FormalParameter firstParam = n.f0; // FormalParameter()
        NodeListInterface restParam = n.f1; // ( FormalParameterRest() )*
        Identifier iNode = firstParam.f1; // FormalParameter() -> Identifier()

        paramList.add(CheckDistinct.identifierId(iNode));

        for (Enumeration<Node> e = restParam.elements(); e.hasMoreElements(); ) {
            Identifier fprCast = ((FormalParameterRest) e.nextElement()).f1.f1;

            String id = CheckDistinct.identifierId(fprCast); // FormalParameter() -> Identifier()
            boolean b = paramList.contains(id);

            if (b){
                return false;
            }
            else{
                paramList.add(id);
            }
        }
        return true;
    }

    public static boolean distinctVar(NodeListInterface n) {
        
        LinkedList<String> varList = new LinkedList<>();

        for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
            Identifier varCast = ((VarDeclaration)e.nextElement()).f1;

            String id = CheckDistinct.identifierId( varCast );
            boolean b = varList.contains(id);
            if ( b ){
                return false;
            }
            else{
                varList.add(id);
            }
        }
        return true;
    }

    public static boolean distinctMethod(NodeListInterface n) {
        
        LinkedList<String> methodList = new LinkedList<>();

        for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
           
            String id = CheckDistinct.methodId(e.nextElement());
            boolean b = methodList.contains(id);
            
            if ( b ){
                return false;
            }
            else{
                methodList.add(id);
            }
        }
        return true;
    }

    public static boolean distinctClass(NodeListInterface n) {
        
        LinkedList<String> classList = new LinkedList<>();

        for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
            String id;
            Node m = e.nextElement();
            boolean b1 = (m instanceof MainClass);

            if(b1) {
                id = CheckDistinct.classId(m);
            }
            else {
                Node tdCast = ((TypeDeclaration)m).f0.choice;
                id = CheckDistinct.classId( tdCast ); // ClassDeclaration() | ClassExtendsDeclaration()
            }
            
            boolean b2 = classList.contains(id);

            if ( b2 ){
                return false;
            }
            else{
                classList.add(id);
            }
        }
        return true;
    }

    // Determines Param or Var, and Expressions
    public static Type determParamVar(Node n) {
        boolean b = (n instanceof FormalParameter);
        
        if (b) {
            Type fpCast = ( (FormalParameter)n ).f0;

            return fpCast;
        } 
        else {
            Type varCast = ( (VarDeclaration)n ).f0;

            return varCast;
        }
    }

    public static ExpressionTable determExpr(Type t) {
        ExpressionTable et;
        String returnType;
        NodeChoice nc = t.f0; // ArrayType() | BooleanType() | IntegerType() | Identifier()

        if (nc.which == 0){
            returnType = CheckDistinct.INT_ARRAY;
        }
        else if (nc.which == 1){
            returnType = CheckDistinct.BOOLEAN;
        }
        else if (nc.which == 2){
            returnType = CheckDistinct.INT;
        }
        else{
            returnType = CheckDistinct.identifierId( (Identifier)nc.choice );
        }
        et = new ExpressionTable(nc.choice, returnType);
        return et;
    }

    // Checks for primitive data types
    public static boolean ifPrimitive(String type) {
        boolean b1 = type.equals(CheckDistinct.INT_ARRAY);
        boolean b2 = type.equals(CheckDistinct.BOOLEAN);
        boolean b3 = type.equals(CheckDistinct.INT);
        boolean b = (b1 || b2 || b3);
        
        return b;
    }

    // Obtain Id of class, method, and identifier
    public static String classId(Node n) {
        String id; 
        boolean b1 = ( n instanceof MainClass );
        boolean b2 = ( n instanceof ClassDeclaration );

        if ( b1 ) {
            Identifier mcCast = ((MainClass)n).f1;
            id = CheckDistinct.identifierId( mcCast );

            return id;
        }
        else if (b2) {
            Identifier cdCast = ((ClassDeclaration)n).f1;
            id = CheckDistinct.identifierId( cdCast );

            return id;
        }
        else {
            Identifier cedCast = ((ClassExtendsDeclaration)n).f1;
            id = CheckDistinct.identifierId( cedCast );

            return id;
        }
    }

    public static String methodId(Node n) {
        Identifier mdCast = ((MethodDeclaration)n).f2;
        String id = CheckDistinct.identifierId( mdCast );

        return id;
    }

    public static String identifierId(Identifier n) {
        String id = n.f0.tokenImage; // <IDENTIFIER>
        return id;
    }
} // End of Check Distinct
