package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.ast.*;

public class ActualParametersCodeGenerator extends VisitorAdaptor {
	
	private int depth;
	
	private int actParsStarted = 0;
	
	/**
	 * @param depth
	 */
	public ActualParametersCodeGenerator(int depth) {
		this.depth = depth;
	}



	/** For each actual parameter start new generator
	 */
	public void visit(SingleActPar sap) {
		
		if (actParsStarted == 0) {
		
			if (sap.getExpr() instanceof ExprWithAssign) {
				
				ExpressionRightAssocCodeGenerator parameterGenerator = new ExpressionRightAssocCodeGenerator();
				
				sap.getExpr().traverseBottomUp(parameterGenerator);
				
			}
			
			else if (sap.getExpr() instanceof ExprWithoutAssign) {
				
				ExpressionLeftAssocCodeGenerator parameterGenerator = new ExpressionLeftAssocCodeGenerator(depth);
				
				sap.getExpr().traverseBottomUp(parameterGenerator);
				
			}
		
		}
		
	}
	
	/**Method designator; if gets here, it means atgument of this method is another method, so skip generating code until its done
	 */
	public void visit(MethodDesignator lp) {
		
		SyntaxNode parent = lp.getParent();
			
		if (parent instanceof MethodCallFactor) {
			
			actParsStarted++;
			
		}
		
	}
	
	/** method called, continue with generating
	 */
	public void visit(MethodCallFactor mcf) {
		
		actParsStarted--;
		
	}

}
