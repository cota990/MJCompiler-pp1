package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.ast.ExprWithAssign;
import rs.ac.bg.etf.pp1.ast.ExprWithoutAssign;
import rs.ac.bg.etf.pp1.ast.SingleActPar;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;

public class ActualParametersCodeGenerator extends VisitorAdaptor {
	
	/** For each actual parameter start new generator
	 */
	public void visit(SingleActPar sap) {
		
		if (sap.getExpr() instanceof ExprWithAssign) {
			
			ExpressionRightAssocCodeGenerator parameterGenerator = new ExpressionRightAssocCodeGenerator();
			
			sap.getExpr().traverseBottomUp(parameterGenerator);
			
		}
		
		else if (sap.getExpr() instanceof ExprWithoutAssign) {
			
			ExpressionLeftAssocCodeGenerator parameterGenerator = new ExpressionLeftAssocCodeGenerator();
			
			sap.getExpr().traverseBottomUp(parameterGenerator);
			
		}
	}

}
