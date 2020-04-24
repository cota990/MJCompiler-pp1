package rs.ac.bg.etf.pp1.semantic;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.MyObjImpl;
import rs.ac.bg.etf.pp1.MyStructImpl;
import rs.ac.bg.etf.pp1.MyTabImpl;
import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.report.MyDumpSymbolTableVisitor;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class MethodParametersSemanticVisitor extends VisitorAdaptor {
	
	Logger log = Logger.getLogger(MethodParametersSemanticVisitor.class);
	
	private Boolean semanticErrorFound = false;
	
	private MyDumpSymbolTableVisitor stv = new MyDumpSymbolTableVisitor ();
	
	private int formParCounter = 0;
	
	private int localVarCounter = 0;
	
	private int actParCounter = 0;
	
	private MyObjImpl methodProcessed;
	
	private TypeVisitor typeVisitor = new TypeVisitor();

	/**
	 * @param methodProcessed the methodProcessed to set
	 */
	public void setMethodProcessed(MyObjImpl methodProcessed) {
		this.methodProcessed = methodProcessed;
	}

	/**
	 * @return the semanticErrorFound
	 */
	public Boolean getSemanticErrorFound() {
		return semanticErrorFound;
	}

	/**
	 * @return the formParCounter
	 */
	public int getFormParCounter() {
		return formParCounter;
	}

	/**
	 * @return the localVarCounter
	 */
	public int getLocalVarCounter() {
		return localVarCounter;
	}
	
	/**
	 * @return the actParCounter
	 */
	public int getActParCounter() {
		return actParCounter;
	}

	/*
     * formal paramters
     */
    /**Formal parameter type processing, done in TypeVisitor class, pass result in mystructimpl field
     * @see rs.ac.bg.etf.pp1.ast.VisitorAdaptor#visit(rs.ac.bg.etf.pp1.ast.FormParType)
     * @see rs.ac.bg.etf.pp1.semantic.TypeVisitor
     */
    public void visit(FormParType fpt) {
    	
    	log.info("Usao u FormParType");
    	
    	fpt.getType().accept(typeVisitor);
    	
    	fpt.mystructimpl = fpt.getType().mystructimpl;
    	
    	if (typeVisitor.getSemanticErrorFound())
			semanticErrorFound = true;
    	
    }
    
    /**Formal parameter declaration;
     * already checked if declared;
     * same as global variable declaration in {@link GlobalDeclarationsSemanticVisitor#visit(SingleVarDecl)};
     * @see rs.ac.bg.etf.pp1.ast.VisitorAdaptor#visit(rs.ac.bg.etf.pp1.ast.SingleFormParSuccess)
     */
    public void visit (SingleFormParSuccess sfp) {
    	
    	log.info("Usao u SingleFormParSuccess");
    	
    	MyStructImpl type = sfp.getFormParType().mystructimpl;
				
		if (type != null) {
			
			if (sfp.getArrayOption() instanceof ArrayVariable)
				type = new MyStructImpl (new Struct (Struct.Array, type));
			
			MyObjImpl formPar = MyTabImpl.insert(Obj.Var, sfp.getFormParName().getFormParName(), type);
			formPar.setFpPos(formParCounter++);
			
			stv.reportSemanticDetection("Declared formal parameter: " + sfp.getFormParName().getFormParName(), sfp.getFormParName(), formPar);
			
		}
    	
    }
    
    /*
     * local variables
     */
    
    /**Local variable type processing, done in TypeVisitor class, pass result in mystructimpl field
     * @see rs.ac.bg.etf.pp1.ast.VisitorAdaptor#visit(rs.ac.bg.etf.pp1.ast.LocalVarDeclType)
     * @see rs.ac.bg.etf.pp1.semantic.TypeVisitor
     */
    public void visit(LocalVarDeclType lvtype) {
    	
    	lvtype.getType().accept(typeVisitor);
    	
    	lvtype.mystructimpl = lvtype.getType().mystructimpl;
    	
    	if (typeVisitor.getSemanticErrorFound())
			semanticErrorFound = true;
    	
    }
    
    /**Local variable declaration;
     * must check if declared;
     * same as global variable declaration in {@link GlobalDeclarationsSemanticVisitor#visit(SingleVarDecl)};
     * @see rs.ac.bg.etf.pp1.ast.VisitorAdaptor#visit(rs.ac.bg.etf.pp1.ast.SingleLocalVarDeclSuccess)
     */
    public void visit(SingleLocalVarDeclSuccess slvds) {
    	
    	MyObjImpl objFound = MyTabImpl.findInCurrent(slvds.getLocalVarName().getLocalVarName());
    	
    	if (objFound == null) {
    	
	    	SyntaxNode typeNode = slvds.getParent();
			
			while (typeNode.getClass() != LocalVarDeclSuccess.class) typeNode = typeNode.getParent();
			
			MyStructImpl type = ((LocalVarDeclSuccess) typeNode).getLocalVarDeclType().mystructimpl;
			
			if (type != null) {
				
				if (slvds.getArrayOption() instanceof ArrayVariable)
					type = new MyStructImpl (new Struct (Struct.Array, type));
				
				MyObjImpl localVar = MyTabImpl.insert(Obj.Var, slvds.getLocalVarName().getLocalVarName(), type);
				localVar.setFpPos(-1);
				
				localVarCounter++;
				
				stv.reportSemanticDetection("Declared local variable: " + slvds.getLocalVarName().getLocalVarName(), slvds.getLocalVarName(), localVar);
				
			}
		
    	}
    	
    	else {
			
			stv.reportSemanticError(slvds.getLocalVarName().getLocalVarName() + " already declared!", slvds.getLocalVarName());
			semanticErrorFound = true;
			
		}
    	
    }
    
    /*
     * actual parameters
     */
    //TODO actual parameters

}
