package rs.ac.bg.etf.pp1.semantic;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.*;
import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.report.MyDumpSymbolTableVisitor;
import rs.ac.bg.etf.pp1.semantic.leftassocexpr.GlobalMethodsLeftAssociationExpresionsSemanticVisitor;
import rs.etf.pp1.symboltable.concepts.Obj;

public class GlobalMethodsSemanticVisitor extends VisitorAdaptor {
	
	Logger log = Logger.getLogger(GlobalMethodsSemanticVisitor.class);
	
	private Boolean semanticErrorFound = false;
	
	private MyDumpSymbolTableVisitor stv = new MyDumpSymbolTableVisitor ();
	
	private TypeVisitor typeVisitor = new TypeVisitor();
	
	private MyObjImpl currMethod = null;
	
	private boolean mainFound = false;
	
	private MethodParametersSemanticVisitor 
		parsAndVarsVisitor = new MethodParametersSemanticVisitor ();

	/**
	 * @return the semanticErrorFound
	 */
	public Boolean getSemanticErrorFound() {
		return semanticErrorFound;
	}

	/**
	 * @return the currMethod
	 */
	public MyObjImpl getCurrMethod() {
		return currMethod;
	}
	
	/**
	 * @return the mainFound
	 */
	public boolean isMainFound() {
		return mainFound;
	}
	
	/*
	 * declaration
	 */

	/**Global method declaration;
	 * must check if declared;
	 * type was previously checked, so parent instance of MethodDecl must be fetched;
	 * if type is null then do nothing;
	 * else new Meth Obj is created and inserted in Symbol Table; new scope is opened;
	 * @see rs.ac.bg.etf.pp1.ast.VisitorAdaptor#visit(rs.ac.bg.etf.pp1.ast.MethodName)
	 */
	public void visit(MethodName mn) {
		
		MyObjImpl objFound = MyTabImpl.find(mn.getMethodName());
		
		if (objFound == null) {
			
			SyntaxNode methDecl = mn.getParent();
			
			while (methDecl.getClass() != MethodDecl.class) methDecl = methDecl.getParent();
			
			MyStructImpl type = ((MethodDecl) methDecl).getReturnType().mystructimpl;
			
			if (type != null) {
				
				currMethod = MyTabImpl.insert(Obj.Meth, mn.getMethodName(), type);
				currMethod.setAbstract(false);
				currMethod.setReturnFound(false);
				
				stv.reportSemanticDetection("Declared global method: " + mn.getMethodName(), mn, currMethod);
				
			}
			
		}
		
		else {
			
			stv.reportSemanticError(mn.getMethodName() + " already declared!", mn);
			semanticErrorFound = true;
			
		}
		
		if (currMethod != null)
			MyTabImpl.openScope();
		
	}
	
	/*
	 * finishing declaration
	 */
	
	/**Global method declaration finnish;
	 * statements are processed;
	 * number of formal parameters is set, symbols are chained and scope is closed;
	 * must check for return statement in non-void methods;
	 * store method in myobjimpl field;
	 * check if method is main (if void and without parameters)
	 * @see rs.ac.bg.etf.pp1.ast.VisitorAdaptor#visit(rs.ac.bg.etf.pp1.ast.MethodDecl)
	 */
	public void visit(MethodDecl md) {
		
		if (currMethod != null) {
			
			GlobalMethodsLeftAssociationExpresionsSemanticVisitor statementVisitor = new GlobalMethodsLeftAssociationExpresionsSemanticVisitor ();
			md.getStatementList().traverseBottomUp(statementVisitor);
			
			if (statementVisitor.getSemanticErrorFound())
				semanticErrorFound = true;
			
			currMethod.setLevel(parsAndVarsVisitor.getFormParCounter());
			currMethod.setNumOfParsAndVars(parsAndVarsVisitor.getFormParCounter() + parsAndVarsVisitor.getLocalVarCounter());
			
			if (parsAndVarsVisitor.getSemanticErrorFound())
				semanticErrorFound = true;
			
			if (currMethod.getType() != MyTabImpl.noType
					&& !currMethod.isReturnFound()) {
				
				stv.reportSemanticError("no return statement found in non-void method " + md.getMethodName().getMethodName(), null);
				semanticErrorFound = true;
				
			}
			
			MyTabImpl.chainLocalSymbols(currMethod);
			MyTabImpl.closeScope();
			
			stv.reportSemanticDetection("Finnished declaring global method: " + md.getMethodName().getMethodName(), null, currMethod);
			
			if (currMethod.getName().equals("main")) {
				
				mainFound = true;
				
				if (currMethod.getType() != MyTabImpl.noType) {
					
					stv.reportSemanticError("main must be declared as void method", md.getReturnType());
					semanticErrorFound = true;
					
				}
				
				if (currMethod.getLevel() != 0) {
					
					stv.reportSemanticError("main must be declared without formal parameters", md.getFormParsOption());
					semanticErrorFound = true;
					
				}
			}
			
		}
		
		md.getMethodName().myobjimpl = currMethod;
		
	}
	
	/*
	 *  return types
	 */

	/**Void return;
	 * returns MyTabImpl.noType as mystructimpl
	 * @see rs.ac.bg.etf.pp1.ast.VisitorAdaptor#visit(rs.ac.bg.etf.pp1.ast.VoidReturn)
	 */
	public void visit(VoidReturn vr) {
    	
    	vr.mystructimpl = MyTabImpl.noType;
    	
    }
    
    /**Type return processing, done in TypeVisitor class, pass result in mystructimpl field
     * @see rs.ac.bg.etf.pp1.ast.VisitorAdaptor#visit(rs.ac.bg.etf.pp1.ast.NoVoidReturn)
     * @see rs.ac.bg.etf.pp1.semantic.TypeVisitor
     */
    public void visit(NoVoidReturn nvr) {
    	
    	nvr.getType().accept(typeVisitor);
		
		nvr.mystructimpl = nvr.getType().mystructimpl;
		
		if (typeVisitor.getSemanticErrorFound())
			semanticErrorFound = true;
    	
    }
    
    /*
     * formal paramters
     */
    
    /**Formal parameter declaration;
     * check if already declared; if not pass it to parsAndVarsVisitor
     * @see rs.ac.bg.etf.pp1.ast.VisitorAdaptor#visit(rs.ac.bg.etf.pp1.ast.SingleFormParSuccess)
     */
    public void visit (SingleFormParSuccess sfp) {
    	
    	if (currMethod != null) {
    		
    		MyObjImpl objFound = MyTabImpl.findInCurrent(sfp.getFormParName().getFormParName());
			
			if (objFound == null)
				sfp.traverseBottomUp(parsAndVarsVisitor);
			
			else {
				
				stv.reportSemanticError(sfp.getFormParName().getFormParName() + " already declared!", sfp.getFormParName());
				semanticErrorFound = true;
				
			}
    	
    	}
    	
    }
    
    /*
     *  local variables
     */
    
    /**Local variable declaration;
     * check if type object is already set; 
     * if not, fetch parent instanceof LocalVarDeclSuccess, and pass it to FormalParameterAndLocalVariableDeclarationSemanticVisitor
     * all variables in that declaration will be processed
     * @see rs.ac.bg.etf.pp1.ast.VisitorAdaptor#visit(rs.ac.bg.etf.pp1.ast.SingleLocalVarDeclSuccess)
     */
    public void visit(SingleLocalVarDeclSuccess slvds) {
    	
    	if (currMethod != null) {
    		
    		SyntaxNode localVarDecl = slvds.getParent();
			while (localVarDecl.getClass() != LocalVarDeclSuccess.class) localVarDecl = localVarDecl.getParent();
			
			if (((LocalVarDeclSuccess) localVarDecl).getLocalVarDeclType().mystructimpl == null)
				localVarDecl.traverseBottomUp(parsAndVarsVisitor);
				
		}
    	
    }

}
