package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.concepts.Obj;

/** Class used for generating code for assign, increment, decrement and read destination designators;
 * 
 */
public class DestinationCodeGenerator extends VisitorAdaptor {
	
	/**Boolean which is used to determine whether expression inside [] is being calculated;
	 * if true nothing should be loaded, as those designators are loaded in expression generators;
	 * <br> default value is false
	 */
	private boolean arrayExprStarted = false;
	
	public static int SimpleDesign = 0, ClassDesign = 1, ArrayDesign = 2;
	
	/** determines type of designator
	 */
	private int type;
	
	/*
	 * simple designator
	 */
	
	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**SimpleDesignator;
	 * loads obj; <br>expr stack: ... -> ..., val(adr if struct class or array)
	 */
	public void visit(SimpleDesignator sd) {
		
		if (!arrayExprStarted) {
			
			if (sd.myobjimpl.getKind() == Obj.Fld) {
				
				MyObjImpl thisPointer = null;
				
				SyntaxNode parent = sd.getParent();
				
				while (parent.getClass() != ClassMethodDecl.class) parent = parent.getParent();
				
				ClassMethodDecl cmd = (ClassMethodDecl) parent;
				
				for (Obj local : cmd.getClassMethodName().myobjimpl.getLocalSymbols()) {
					
					if (local.getName().equals("this")) {
						
						thisPointer = (MyObjImpl) local;
						break;
						
					}
					
				}
				
				Code.load(thisPointer); Code.put(Code.dup);
				
			}
			
			Code.load(sd.myobjimpl);
			type = SimpleDesign;
			
		}
		
	}
	
	/**Class designator;
	 * loads field value, keeps class address; class instance already processed
	 * <br> if parent designator instanceof SimpleDesignator: do nothing; expr stack: ...,adr -> ...,adr
	 * <br> if parent designator instanceof ClassDesignator: dup_x1 -> pop -> pop; expr stack: ...,par_adr, adr -> ...,adr
	 * <br> if parent designator instanceof ArrayDesignator: ; dup_x2 -> pop -> pop -> pop; expr stack: ...,par_adr, par_ind, adr -> ...,adr
	 * <br> finally, load field value: dup -> load; expr stack: ...,adr -> ...,adr,val
	 */
	public void visit(ClassDesignator cd) {
		
		if (!arrayExprStarted) {
		
			if (cd.getDesignator() instanceof ClassDesignator) {
				
				Code.put(Code.dup_x1); Code.put(Code.pop); Code.put(Code.pop);
			}
			
			else if (cd.getDesignator() instanceof ArrayDesignator) {
				
				Code.put(Code.dup_x2); Code.put(Code.pop); Code.put(Code.pop); Code.put(Code.pop);
				
			}
			
			Code.put(Code.dup); Code.load(cd.myobjimpl);
			
			type = ClassDesign;
		
		}
		
	}
	
	/**Array designator;
	 * loads elem value, keeps array address and element index; array designator already processed, must process expr
	 * after expr process, ind is put on expr stack: ...,adr -> ...,adr,ind
	 * <br> if parent designator instanceof SimpleDesignator: do nothing; expr stack: ...,adr,ind -> ...,adr,ind
	 * <br> if parent designator instanceof ClassDesignator: dup_x2 -> pop -> dup_x2 -> pop -> pop; expr stack: ...,par_adr, adr,ind -> ...,adr,ind
	 * <br> if parent designator instanceof ArrayDesignator: dup_x2 -> pop -> dup_x2 -> pop -> pop -> dup_x2 -> pop -> dup_x2 -> pop -> pop ; expr stack: ...,par_adr, par_ind, adr, ind -> ...,adr
	 * <br> finally, load elem value: dup2 -> load; expr stack: ...,adr,ind -> ...,adr,ind,val
	 * <br> reset arrayExprStarted flag
	 */
	public void visit(ArrayDesignator ad) {
		
		if (arrayExprStarted) {
		
			if (ad.getDesignator() instanceof ClassDesignator) {
				
				Code.put(Code.dup_x2); Code.put(Code.pop); 
				Code.put(Code.dup_x2); Code.put(Code.pop); Code.put(Code.pop);
				
			}
			
			else if (ad.getDesignator() instanceof ArrayDesignator) {
				
				Code.put(Code.dup_x2); Code.put(Code.pop); 
				Code.put(Code.dup_x2); Code.put(Code.pop); Code.put(Code.pop);
				
				Code.put(Code.dup_x2); Code.put(Code.pop); 
				Code.put(Code.dup_x2); Code.put(Code.pop); Code.put(Code.pop);
				
			}
			
			Code.put(Code.dup2); Code.load(ad.myobjimpl);
			arrayExprStarted = false;
			
			type = ArrayDesign;
		
		}
		
	}
	
	/** LeftBracket;
	 * <br> begins generating code for array expression; sets arrayExprStarted flag, and while flag is set, does nothing;
	 */
	public void visit(LeftBracket lb) {
		
		if (!arrayExprStarted) {
			
			SyntaxNode parent = lb.getParent();
			
			if (parent instanceof ArrayDesignator) {
				
				arrayExprStarted = true;
				
				ArrayDesignator parentDesignator = (ArrayDesignator) parent;
				
				if (parentDesignator.getExpr() instanceof ExprWithAssign) {
					
					ExpressionRightAssocCodeGenerator exprGenerator = new ExpressionRightAssocCodeGenerator ();
					
					parentDesignator.getExpr().traverseBottomUp(exprGenerator);
					
					
				}
				
				else if (parentDesignator.getExpr() instanceof ExprWithoutAssign) {
					
					ExpressionLeftAssocCodeGenerator exprGenerator = new ExpressionLeftAssocCodeGenerator (0);
					
					parentDesignator.getExpr().traverseBottomUp(exprGenerator);
					
				}
				
			}
			
		}
		
	}

}
