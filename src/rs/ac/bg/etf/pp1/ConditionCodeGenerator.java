package rs.ac.bg.etf.pp1;

import java.util.ArrayList;
import java.util.List;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;

/**Code generator for conditions, used in if and for statements
 */
public class ConditionCodeGenerator extends VisitorAdaptor {
	
	public static int MatchedIf = 0, UnmatchedIf = 1, UnmatchedElse = 2, 
			MatchedFor = 3, UnmatchedFor = 4, MatchedForeach = 5, UnmatchedForeach = 6;
	
	private int numOfFactors = 1;
	
	private List<Integer> exprStartAddress = new ArrayList<Integer> ();
	
	/** list of address fixups for conditional expressions (all false jumps put)
	 */
	private List<Integer> conditionalFactorsJumpAddresses = new ArrayList<Integer> ();
	
	/** list of address fixups for conditional factors (jump if true)
	 */
	private List<Integer> numOfFactorsInTerms = new ArrayList<Integer> ();

	/**
	 * @return the numOfFactorsInTerms
	 */
	public List<Integer> getNumOfFactorsInTerms() {
		return numOfFactorsInTerms;
	}
	
	/**
	 * @return the exprStartAddress
	 */
	public List<Integer> getExprStartAddress() {
		return exprStartAddress;
	}
	
	/**
	 * @return the conditionalFactorsJumpAddresses
	 */
	public List<Integer> getConditionalFactorsJumpAddresses() {
		return conditionalFactorsJumpAddresses;
	}
	
	/*
	 * expression code generator
	 */
	
	/**SingleExprFact; generate expression
	 * <br> load 1 and put not_equal jump
	 */
	public void visit(SingleExprFact sef) {
		
		exprStartAddress.add(Code.pc);
		
		if (sef.getExpr() instanceof ExprWithoutAssign) {
			
			ExpressionLeftAssocCodeGenerator exprGenerator = new ExpressionLeftAssocCodeGenerator (0);
			
			sef.getExpr().traverseBottomUp(exprGenerator);
			
		}
		
		else if (sef.getExpr() instanceof ExprWithAssign) {
			
			ExpressionRightAssocCodeGenerator exprGenerator = new ExpressionRightAssocCodeGenerator ();
			
			sef.getExpr().traverseBottomUp(exprGenerator);
			
		}
		
		// put jump
		Code.loadConst(1);
		Code.putFalseJump(Code.eq, 0);
		//conditionalJumpAddressesList.add(Code.pc - 2);
		conditionalFactorsJumpAddresses.add(Code.pc - 2);
		
	}
	
	/** First expression of relational operator
	 */
	public void visit(FirstExpr fe) {
		
		exprStartAddress.add(Code.pc);
		
		if (fe.getExpr() instanceof ExprWithoutAssign) {
			
			ExpressionLeftAssocCodeGenerator exprGenerator = new ExpressionLeftAssocCodeGenerator (0);
			
			fe.getExpr().traverseBottomUp(exprGenerator);
			
		}
		
		else if (fe.getExpr() instanceof ExprWithAssign) {
			
			ExpressionRightAssocCodeGenerator exprGenerator = new ExpressionRightAssocCodeGenerator ();
			
			fe.getExpr().traverseBottomUp(exprGenerator);
			
		}
		
	}
	
	/** Second expression of relational operator
	 */
	public void visit(SecondExpr se) {
		
		if (se.getExpr() instanceof ExprWithoutAssign) {
			
			ExpressionLeftAssocCodeGenerator exprGenerator = new ExpressionLeftAssocCodeGenerator (0);
			
			se.getExpr().traverseBottomUp(exprGenerator);
			
		}
		
		else if (se.getExpr() instanceof ExprWithAssign) {
			
			ExpressionRightAssocCodeGenerator exprGenerator = new ExpressionRightAssocCodeGenerator ();
			
			se.getExpr().traverseBottomUp(exprGenerator);
			
		}
		
	}
	
	/*
	 * conditional factor
	 */
	
	/**MultipleExprFact;
	 * <br> both expressions loaded; put jump based on relational operator
	 */
	public void visit(MultipleExprFact mef) {
		
		if (mef.getRelop() instanceof Equals)
			Code.putFalseJump(Code.eq, 0);
		
		else if (mef.getRelop() instanceof NotEquals)
			Code.putFalseJump(Code.ne, 0);
		
		else if (mef.getRelop() instanceof GreaterThan)
			Code.putFalseJump(Code.gt, 0);
		
		else if (mef.getRelop() instanceof GreaterThanEquals)
			Code.putFalseJump(Code.ge, 0);
		
		else if (mef.getRelop() instanceof LessThan)
			Code.putFalseJump(Code.lt, 0);
		
		else if (mef.getRelop() instanceof LessThanEquals)
			Code.putFalseJump(Code.le, 0);
		
		conditionalFactorsJumpAddresses.add(Code.pc - 2);
		
	}
	
	/*
	 * conditional term
	 */
	
	public void visit(And a) {
		
		numOfFactors++;
		
	}
	
	public void visit(Or o) {
		
		numOfFactorsInTerms.add(numOfFactors);
		
		numOfFactors = 1;
		
	}

}
