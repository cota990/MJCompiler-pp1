package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;

/**
 * Class used for generating code for combined operators expressions
 */
public class ExpressionRightAssocCodeGenerator extends VisitorAdaptor {
	
	/** ExprWithAssign;
	 * <br> generate code for destination; expr stack : ..., -> ...,val|adr,val|adr,ind,val
	 * <br> check if expr instanceof ExprWithoutAssign;
	 * <br> if true, start generating code for left association expressions: expr stack : ..., -> ...,new_val
	 * <br> if false, create new right association expression code generator
	 * <br> perform arithmetic operation from combined operator; val operand new_val = store_val; expr stack : ...,val, new_val -> ...,store_val
	 * <br> check destination type
	 * <br> if SimpleDesign: dup -> store; expr_stack: ...,store_val -> ..., store_val
	 * <br> if ClassDesign: dup_x1 -> store; expr_stack: ...,adr,store_val -> ...,store_val 
	 * <br> if ArrayDesign: dup_x2 -> store; expr_stack: ...,adr,ind,store_val -> ...,store_val
	 */
	public void visit(ExprWithAssign ewa) {
		
		DestinationCodeGenerator destGenerator = new DestinationCodeGenerator ();
		
		ewa.getDestination().traverseBottomUp(destGenerator);
		
		if (ewa.getExpr() instanceof ExprWithoutAssign) {
			
			ExpressionLeftAssocCodeGenerator exprGenerator = new ExpressionLeftAssocCodeGenerator ();
			
			ewa.getExpr().traverseBottomUp(exprGenerator);
			
		}
		
		else if (ewa.getExpr() instanceof ExprWithAssign) {
			
			ExpressionRightAssocCodeGenerator exprGenerator = new ExpressionRightAssocCodeGenerator ();
			
			ewa.getExpr().traverseBottomUp(exprGenerator);
			
		}
		
		if (ewa.getAssignop() instanceof Assign) {
			
			Code.put(Code.dup_x1); Code.put(Code.pop); Code.put(Code.pop);
		}
		
		else if (ewa.getAssignop() instanceof AddopAssign) {
			
			if ((
					(AddopAssign) ewa.getAssignop()
				)
					.getAddopRight() instanceof PlusAssign)
				Code.put(Code.add);
			
			else if ((
					(AddopAssign) ewa.getAssignop()
				)
					.getAddopRight() instanceof MinusAssign)
				Code.put(Code.sub);
			
		}
		
		else if (ewa.getAssignop() instanceof MulopAssign) {
			
			if ((
					(MulopAssign) ewa.getAssignop()
				)
					.getMulopRight() instanceof MulAssign)
				Code.put(Code.mul);
			
			else if ((
					(MulopAssign) ewa.getAssignop()
				)
					.getMulopRight() instanceof DivAssign)
				Code.put(Code.div);
			
			else if ((
					(MulopAssign) ewa.getAssignop()
				)
					.getMulopRight() instanceof ModAssign)
				Code.put(Code.rem);
			
		}
		
		if (destGenerator.getType() == DestinationCodeGenerator.SimpleDesign)
			Code.put(Code.dup);
		
		else if (destGenerator.getType() == DestinationCodeGenerator.ClassDesign)
			Code.put(Code.dup_x1);
		
		else if (destGenerator.getType() == DestinationCodeGenerator.ArrayDesign)
			Code.put(Code.dup_x2);
		
		Code.store (ewa.getDestination().myobjimpl);
		
	}

}
