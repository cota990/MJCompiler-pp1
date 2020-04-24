/**
 * 
 */
package rs.ac.bg.etf.pp1.semantic;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.MyObjImpl;
import rs.ac.bg.etf.pp1.MyStructImpl;
import rs.ac.bg.etf.pp1.MyTabImpl;
import rs.ac.bg.etf.pp1.ast.Type;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;
import rs.ac.bg.etf.pp1.report.MyDumpSymbolTableVisitor;
import rs.etf.pp1.symboltable.concepts.Obj;

/**
 *Class used for semantic analysis of types
 *
 */
public class TypeVisitor extends VisitorAdaptor {
	
	Logger log = Logger.getLogger(TypeVisitor.class);
	
	private Boolean semanticErrorFound = false;
	
	private MyDumpSymbolTableVisitor stv = new MyDumpSymbolTableVisitor ();
	
	/**
	 * @return the semanticErrorFound
	 */
	public Boolean getSemanticErrorFound() {
		return semanticErrorFound;
	}

	/** (non-Javadoc)
	 * @see rs.ac.bg.etf.pp1.ast.VisitorAdaptor#visit(rs.ac.bg.etf.pp1.ast.Type)
	 * Type processing; 
	 * must be checked if previously declared
	 * context check: Type = IDENT
	 * 		 Obj with name IDENT must be Type
	 * set myobjimpl field of Type to found object if semantic check is passed, else set myobjimpl field to null
	 * 
	 */
	public void visit(Type type) { 
		
		MyObjImpl typeNode = MyTabImpl.find(type.getTypeName());
		
		if (typeNode == null) {
			
			//log.error ("Semantic error on line " + type.getLine() + ": " + type.getTypeName() + " is not declared!");
			stv.reportSemanticError(type.getTypeName() + " is not declared!", type);
    		semanticErrorFound = true;
    		type.mystructimpl = null;
    		
		}
		
		else if (typeNode.getKind() != Obj.Type) {
			
			//log.error ("Semantic error on line " + type.getLine() + ": " + type.getTypeName() + " must be declared as type!");
			stv.reportSemanticError(type.getTypeName() + " must be declared as type!", type);
    		semanticErrorFound = true;
    		type.mystructimpl = null;
    		
		}
		
		else {
			
			type.mystructimpl = (MyStructImpl) typeNode.getType();
			
			// if needed for report
			// stv.reportSemanticDetection("Usage of type: " + type.getTypeName(), type, typeNode); 
			
		}
		
	}

}
