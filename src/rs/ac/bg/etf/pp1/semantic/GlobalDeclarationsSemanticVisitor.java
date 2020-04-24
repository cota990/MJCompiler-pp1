package rs.ac.bg.etf.pp1.semantic;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.MyObjImpl;
import rs.ac.bg.etf.pp1.MyStructImpl;
import rs.ac.bg.etf.pp1.MyTabImpl;
import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.report.MyDumpSymbolTableVisitor;
import rs.ac.bg.etf.pp1.semantic.classdeclaration.AbstractClassDeclarationSemanticVisitor;
import rs.ac.bg.etf.pp1.semantic.classdeclaration.NonAbstractClassDeclarationSemanticVisitor;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

/**
 * Class used for semantic analysis of global constants and variables declarations,
 * also for starters of class and abstract class declarations
 *
 */
public class GlobalDeclarationsSemanticVisitor extends VisitorAdaptor {
	
	Logger log = Logger.getLogger(GlobalDeclarationsSemanticVisitor.class);
	
	private Boolean semanticErrorFound = false;
	
	private MyDumpSymbolTableVisitor stv = new MyDumpSymbolTableVisitor ();
	
	private TypeVisitor typeVisitor = new TypeVisitor();
	
	/**
	 * @return the semanticErrorFound
	 */
	public Boolean getSemanticErrorFound() {
		return semanticErrorFound;
	}
	
	/**Const Type processing; done in TypeVisitor class, pass result in mystructimpl field
	 * @see rs.ac.bg.etf.pp1.ast.VisitorAdaptor#visit(rs.ac.bg.etf.pp1.ast.Type)
	 * @see rs.ac.bg.etf.pp1.semantic.TypeVisitor
	 */
	public void visit(ConstType constType) {
		
		constType.getType().accept(typeVisitor);
		
		constType.mystructimpl = constType.getType().mystructimpl;
		
		if (typeVisitor.getSemanticErrorFound())
			semanticErrorFound = true;
		
	}
	
	/**Var Type processing; done in TypeVisitor class, pass result in mystructimpl field
	 * @see rs.ac.bg.etf.pp1.ast.VisitorAdaptor#visit(rs.ac.bg.etf.pp1.ast.Type)
	 * @see rs.ac.bg.etf.pp1.semantic.TypeVisitor
	 */
	public void visit(VarType varType) {
		
		varType.getType().accept(typeVisitor);
		
		varType.mystructimpl = varType.getType().mystructimpl;
		
		if (typeVisitor.getSemanticErrorFound())
			semanticErrorFound = true;
		
	}
	
	/**
	 * 
	 * symbolic constants; 
	 * numbers, printable characters or boolean constants
	 * report detection, return created Obj 
	 * 
	 */
	
	/**Numbers processing; return created constant
	 * @see rs.ac.bg.etf.pp1.ast.VisitorAdaptor#visit(rs.ac.bg.etf.pp1.ast.NumberConst)
	 */
	public void visit(NumberConst numberConst) {
    	
    	numberConst.myobjimpl = new MyObjImpl (Obj.Con, numberConst.getNumberConst().toString(),  MyTabImpl.intType, numberConst.getNumberConst(), 0);
		
    	stv.reportSemanticDetection("Detected usage of symbol (symbolic constant): " + numberConst.getNumberConst(), numberConst, numberConst.myobjimpl);
    	
    }
    
    /**Printable characters processing; return created constant
     * @see rs.ac.bg.etf.pp1.ast.VisitorAdaptor#visit(rs.ac.bg.etf.pp1.ast.CharConst)
     */
    public void visit(CharConst charConst) {
    	
    	charConst.myobjimpl = new MyObjImpl (Obj.Con, "'" + charConst.getCharConst() + "'",  MyTabImpl.charType, Integer.valueOf (charConst.getCharConst()), 0);
    	
    	stv.reportSemanticDetection("Detected usage of symbol (symbolic constant): '" + charConst.getCharConst() + "'", charConst, charConst.myobjimpl);
		
    }
    
    /**Bool constants (true; false) processing; return created constant
     * @see rs.ac.bg.etf.pp1.ast.VisitorAdaptor#visit(rs.ac.bg.etf.pp1.ast.BoolConst)
     */
    public void visit(BoolConst boolConst) {
    	
    	boolConst.myobjimpl = new MyObjImpl (Obj.Con, boolConst.getBoolConst() ? "true" : "false",  MyTabImpl.boolType, boolConst.getBoolConst() ? 1 : 0, 0);
    	
    	stv.reportSemanticDetection("Detected usage of symbol (symbolic constant): " + boolConst.getBoolConst(), boolConst, boolConst.myobjimpl);
		
    }
	
	/**Global constant declaration;<br>
	 * must check if name already declared;
	 * type was previously checked, so parent instance of ConstDecl must be fetched; 
	 * if type is null then do nothing; if constValue is null also do nothing;
	 * <br>context check: ConstDecl = Type ConstName EQUALS ConstValue
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Type and type of ConstValue must be equal <br>
	 * if semantic context checked, insert new node in Symbol Table and report inserting
	 * @see rs.ac.bg.etf.pp1.ast.VisitorAdaptor#visit(rs.ac.bg.etf.pp1.ast.SingleConstDecl)
	 * 
	 */
	public void visit(SingleConstDecl scd) {
		
		MyObjImpl objFound = MyTabImpl.find(scd.getConstName().getConstName());
		
		if (objFound == null) {
			
			SyntaxNode typeNode = scd.getParent();
			
			while (typeNode.getClass() != ConstDecl.class) typeNode = typeNode.getParent();
			
			MyStructImpl type = ((ConstDecl) typeNode).getConstType().mystructimpl;
			MyObjImpl constValueObj = scd.getConstValue().myobjimpl;
			
			if (type != null
					&& constValueObj != null) {
				
				if (!type.equals(constValueObj.getType())) {
					
					stv.reportSemanticError("assigned constant value type does not match declared constant type!", scd.getConstValue());
					semanticErrorFound = true;
					
				}
				
				else {
					
					objFound = MyTabImpl.insert(Obj.Con, scd.getConstName().getConstName(), type);
					objFound.setAdr(constValueObj.getAdr());
					stv.reportSemanticDetection("Declared global constant: " + scd.getConstName().getConstName(), scd.getConstName(), objFound);
					
				}
				
			}
			
		}
		
		else {
			
			stv.reportSemanticError(scd.getConstName().getConstName() + " already declared!", scd.getConstName());
			semanticErrorFound = true;
			
		}
	
	}
	
	/**Global variable declaration;<br>
	 * must check if name already declared;
	 * type was previously checked, so parent instance of VarDecl must be fetched;
	 * if type is null then do nothing;
	 * no specific semantic context to check;
	 * depending on instance of ArrayOption, insert variable as array of type or type, then report inserting
	 * @see rs.ac.bg.etf.pp1.ast.VisitorAdaptor#visit(rs.ac.bg.etf.pp1.ast.SingleVarDecl)
	 */
	public void visit(SingleVarDecl svd) {
		
		MyObjImpl objFound = MyTabImpl.find(svd.getVarName().getVarName());
		
		if (objFound == null) {
			
			SyntaxNode typeNode = svd.getParent();
			
			while (typeNode.getClass() != VarDecl.class) typeNode = typeNode.getParent();
			
			MyStructImpl type = ((VarDecl) typeNode).getVarType().mystructimpl;
			
			if (type != null) {
				
				if (svd.getArrayOption() instanceof ArrayVariable)
					type = new MyStructImpl (new Struct (Struct.Array, type));
				
				objFound = MyTabImpl.insert(Obj.Var, svd.getVarName().getVarName(), type);
				stv.reportSemanticDetection("Declared global variable: " + svd.getVarName().getVarName(), svd.getVarName(), objFound);
				
			}
			
		}
		
		else {
			
			stv.reportSemanticError(svd.getVarName().getVarName() + " already declared!", svd.getVarName());
			semanticErrorFound = true;
			
		}
		
	}
	
	 /**Abstract class declaration; if no syntax errors happened, in this node we find abstract class name;
	  * must be checked if declared;
	  * parent instance of AbstractClassDecl is passed to AbstractClassDeclarationSemanticVisitor for further analysis
	  * @see rs.ac.bg.etf.pp1.ast.VisitorAdaptor#visit(rs.ac.bg.etf.pp1.ast.AbstractClassDeclSuccess)
	  * @see rs.ac.bg.etf.pp1.semantic.classdeclaration.AbstractClassDeclarationSemanticVisitor
	 */
	public void visit(AbstractClassDeclSuccess acds) { 
	    	
    	MyObjImpl classFound = MyTabImpl.find(acds.getClassName().getClassName());
    	
    	if (classFound == null) {
			
    		AbstractClassDeclarationSemanticVisitor acdsVisitor = new AbstractClassDeclarationSemanticVisitor ();
    		
    		((AbstractClassDecl) acds.getParent()).traverseBottomUp(acdsVisitor);
    		
    		if (acdsVisitor.getSemanticErrorFound())
    			semanticErrorFound = true;
    		
    		stv.reportSemanticDetection("Finished declaring abstract class: " + acds.getClassName().getClassName(), null, acds.getClassName().myobjimpl);
    		
		}
		
    	else {
			
			stv.reportSemanticError(acds.getClassName().getClassName() + " already declared!", acds.getClassName());
			semanticErrorFound = true;
			
		}
    	
    }
	
	/**Class declaration; if no syntax errors happened, in this node we find class name;
	 * must be checked if declared;
	 * parent instance of ClassDecl is passed to ClassDeclarationSemanticVisitor for further analysis
	 * @see rs.ac.bg.etf.pp1.ast.VisitorAdaptor#visit(rs.ac.bg.etf.pp1.ast.ClassDeclSuccess)
	 * @see rs.ac.bg.etf.pp1.semantic.classdeclaration.NonAbstractClassDeclarationSemanticVisitor
	 */
	public void visit(ClassDeclSuccess cds) {
		
		MyObjImpl classFound = MyTabImpl.find(cds.getClassName().getClassName());
		
		if (classFound == null) {
			
			NonAbstractClassDeclarationSemanticVisitor cdsVisitor = new NonAbstractClassDeclarationSemanticVisitor ();
			
			cds.getParent().traverseBottomUp(cdsVisitor);
			
			if (cdsVisitor.getSemanticErrorFound())
    			semanticErrorFound = true;
    		
    		stv.reportSemanticDetection("Finished declaring class: " + cds.getClassName().getClassName(), null, cds.getClassName().myobjimpl);
			
		}
		
		else {
			
			stv.reportSemanticError(cds.getClassName().getClassName() + " already declared!", cds.getClassName());
			semanticErrorFound = true;
			
		}
		
	}

}
