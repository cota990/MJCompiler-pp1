package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.ast.SingleActPar;
import rs.etf.pp1.symboltable.concepts.Obj;

public class ActualParametersSemanticAnalyzer extends SemanticAnalyzer {
	
	private MyObjImpl calledMethod;
	
	private int actParsCount;
	
	private boolean countMode;

	/**
	 * @return the actParsCount
	 */
	public int getActParsCount() {
		return actParsCount;
	}
	
	/**
	 * @param calledMethod
	 * @param countMode
	 */
	public ActualParametersSemanticAnalyzer(MyObjImpl calledMethod, boolean countMode) {
		this.calledMethod = calledMethod;
		this.countMode = countMode;
		this.actParsCount = 0;
	}

	/*
	 * actual parameters
	 */
	
	/**Single actual parameter;
	 * <br> context check: ActPar = Expr
	 * <br> &nbsp;&nbsp;&nbsp;&nbsp; Expr must assignable to corresponding formal parameter (find in calledMethod locals)
	 * <br> Expr is already processed, check if != null
	 * <br> if there are more actual parameters than formal parameters, throw error
	 */
	public void visit(SingleActPar sap) {
		
		if (actParsCount >= calledMethod.getLevel()
				|| countMode)
			actParsCount++;
		
		else if (sap.getExpr().mystructimpl != null) {
			
			// search calledMethod for corresponding formal parameter
			MyObjImpl formPar = null;
			
			for (Obj objFound : calledMethod.getLocalSymbols()) {
				
				if (objFound.getFpPos() == actParsCount) {
					
					formPar = (MyObjImpl) objFound;
					break;
					
				}
				
			}
			
			if (formPar != null
					&& !sap.getExpr().mystructimpl.assignableTo(
							(MyStructImpl) formPar.getType()))
				reportSemanticError("actual parameter on position " + actParsCount + " does not match formal parameter type", sap.getExpr());
			
			actParsCount++;
			
		}
		
	}

}
