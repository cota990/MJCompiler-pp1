package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.concepts.Obj;

/**
 * Class used for generating code for expressions with operators with left associativity
 */
public class ExpressionLeftAssocCodeGenerator extends VisitorAdaptor {
	
	//Logger log = Logger.getLogger(ExpressionLeftAssocCodeGenerator.class);
	
	/**Boolean which is used to determine whether new expression should be calculated;
	 * if true nothing should be done, as that expression was calculated in separate generator;
	 * <br> default value is false
	 */
	private boolean anotherExprStarted = false;
	
	/*
	 * expression starts
	 */
	
	/** Left Parenthesis exprssion started; sets anotherExprStarted flag; used in method calls and composite factors
	 * <br> for methods, it should start new expression for every actual parameter; ends generator when parameter is calculated
	 * <br> for composites, starts new expression generator, ends generator when expression is calculated
	 */
	public void visit(LeftParenthesis lp) {
		
		if (!anotherExprStarted) {
		
			anotherExprStarted = true;
			SyntaxNode parent = lp.getParent();
			
			if (parent instanceof MethodDesignator) {
				
				MethodDesignator methodDesignator = (MethodDesignator) parent;
				
				if (methodDesignator.getActParamsOption() instanceof ActualParameters) {
					
					ActualParameters actualParameters = (ActualParameters) methodDesignator.getActParamsOption();
					
					ActualParametersCodeGenerator actualParametersCodeGenerator = new ActualParametersCodeGenerator ();
					
					actualParameters.traverseBottomUp(actualParametersCodeGenerator);
					
				}
				
			}
			
			else if (parent instanceof CompositeFactor) {
				
				CompositeFactor compositeFactor = (CompositeFactor) parent;
				
				if (compositeFactor.getExpr() instanceof ExprWithAssign) {
					
					ExpressionRightAssocCodeGenerator parameterGenerator = new ExpressionRightAssocCodeGenerator();
					
					compositeFactor.getExpr().traverseBottomUp(parameterGenerator);
					
				}
				
				else if (compositeFactor.getExpr() instanceof ExprWithoutAssign) {
					
					ExpressionLeftAssocCodeGenerator parameterGenerator = new ExpressionLeftAssocCodeGenerator();
					
					compositeFactor.getExpr().traverseBottomUp(parameterGenerator);
					
				}
			
			}
		
		}
		
	}
	
	/**Left Bracket expression started; sets anotherExprStarted flag; used in ArrayDesignator and New arrays
	 * <br> for ArrayDesignator, starts new expression generator, ends generator when expression is calculated
	 * <br> for New arrays, starts new expression generator, ends generator when expression is calculated
	 */
	public void visit(LeftBracket lb) {
		
		if (!anotherExprStarted) {
			
			anotherExprStarted = true;
			SyntaxNode parent = lb.getParent();
			
			if (parent instanceof ArrayDesignator) {
				
				ArrayDesignator arrayDesignator = (ArrayDesignator) parent;
				
				if (arrayDesignator.getExpr() instanceof ExprWithAssign) {
					
					ExpressionRightAssocCodeGenerator parameterGenerator = new ExpressionRightAssocCodeGenerator();
					
					arrayDesignator.getExpr().traverseBottomUp(parameterGenerator);
					
				}
				
				else if (arrayDesignator.getExpr() instanceof ExprWithoutAssign) {
					
					ExpressionLeftAssocCodeGenerator parameterGenerator = new ExpressionLeftAssocCodeGenerator();
					
					arrayDesignator.getExpr().traverseBottomUp(parameterGenerator);
					
				}
				
			}
			
			else if (parent instanceof NewArrayFactor) {
				
				NewArrayFactor newArrayFactor = (NewArrayFactor) parent;
				
				if (newArrayFactor.getExpr() instanceof ExprWithAssign) {
					
					ExpressionRightAssocCodeGenerator parameterGenerator = new ExpressionRightAssocCodeGenerator();
					
					newArrayFactor.getExpr().traverseBottomUp(parameterGenerator);
					
				}
				
				else if (newArrayFactor.getExpr() instanceof ExprWithoutAssign) {
					
					ExpressionLeftAssocCodeGenerator parameterGenerator = new ExpressionLeftAssocCodeGenerator();
					
					newArrayFactor.getExpr().traverseBottomUp(parameterGenerator);
					
				}
				
			}
			
		}
		
	}
	
	/*
	 * expression ends
	 */
	
	/**MethodDesignator; all actual parameter expression calculated, call method (CALL statement and return address) and reset anotherExprStarted
	 */
	public void visit(MethodDesignator md) {
		
		if (anotherExprStarted) {
			
			anotherExprStarted = false;
			
			if (!(md.getDesignator().myobjimpl.getName().equals("ord")
					|| md.getDesignator().myobjimpl.getName().equals("chr")
					|| md.getDesignator().myobjimpl.getName().equals("len") )) {
			
				int destAdr = md.getDesignator().myobjimpl.getAdr() - Code.pc;
				Code.put(Code.call);
				Code.put2(destAdr);
				
			}
			
			else {
				
				if (md.getDesignator().myobjimpl.getName().equals("len"))
					Code.put (Code.arraylength);
				
			}
		
		}
		
	}
	
	/** CompositeFactor; expression in parenthesis calculated, reset anotherExprStarted flag
	 */
	public void visit(CompositeFactor cf) {
		
		if (anotherExprStarted) 
			anotherExprStarted = false;
		
	}
	
	/**Array designator; reset anotherExprStarted flag
	 * loads elem value; <br>expr stack: ... -> ..., val(adr if struct class or array)
	 */
	public void visit(ArrayDesignator ad) {
		
		if (anotherExprStarted) {
			
			anotherExprStarted = false;
			Code.load(ad.myobjimpl);
		
		}
		
	}
	
	/**NewArrayFactor; allocate memory for array and reset anotherExprStarted flag
	 */
	public void visit(NewArrayFactor naf) {
		
		if (anotherExprStarted) {
			
			anotherExprStarted = false;
			
			Code.put(Code.newarray);
	    	Code.put(naf.getType().mystructimpl == MyTabImpl.charType ? 0 : 1);			
			
		}
		
	}
	
	
	/*
	 * designators
	 */
	
	/**SimpleDesignator;
	 * loads obj; <br>expr stack: ... -> ..., val(adr if struct class or array)
	 */
	public void visit(SimpleDesignator sd) {
		
		if (!anotherExprStarted && sd.myobjimpl.getKind() != Obj.Meth)
			Code.load(sd.myobjimpl);
		
	}
	
	/**Class designator;
	 * loads field value;<br>expr stack: ... -> ..., val(adr if struct class or array)
	 */
	public void visit(ClassDesignator cd) {
		
		if (!anotherExprStarted) 
			Code.load(cd.myobjimpl);
		
	}
	
	/*
	 * symbolic constants
	 */
	
	/** number processing; load numeric constant
	 */
	public void visit(NumberConst nc) {
		
		if (!anotherExprStarted)
			Code.loadConst(nc.getNumberConst());
		
	}
	
	/** printable character processing; load character
	 */
	public void visit(CharConst cc) {
		
		if (!anotherExprStarted)
			Code.loadConst(Integer.valueOf(cc.getCharConst()));
		
	}
	
	/** bool constant processing; load 1 if true 0 if false
	 */
	public void visit(BoolConst bc) {
		
		if (!anotherExprStarted) {
			
			if (bc.getBoolConst())
				Code.loadConst(1);
			
			else
				Code.loadConst(0);
			
		}	
		
	}
	
	/*
	 * factors
	 */
	
	/**NewFactor; allocate memory for class instance
	 */
	public void visit(NewFactor nf) {
		
		if (!anotherExprStarted) {
			
			Code.put(Code.new_);
			Code.put2(nf.getType().mystructimpl.getNumberOfFields());
		
		}
		
	}
	
	/*
	 * terms
	 */
	
	/** MultipleFactorTerm; put operator in code
	 */
	public void visit(MultipleFactorTerm mft) {
		
		if (!anotherExprStarted) {
			
			if (mft.getMulopLeft() instanceof Mul)
				Code.put(Code.mul);
			
			else if (mft.getMulopLeft() instanceof Div)
				Code.put(Code.div);
			
			else if (mft.getMulopLeft() instanceof Mod)
				Code.put(Code.rem);
			
		}
		
	}
	
	/*
	 * expressions
	 */
	
	/**MultipleTermExpr; put operator in code
	 */
	public void visit(MultipleTermExpr mte) {
		
		if (!anotherExprStarted) {
			
			if (mte.getAddopLeft() instanceof Plus)
				Code.put(Code.add);
			
			else if (mte.getAddopLeft() instanceof Minus)
				Code.put(Code.sub);
			
		}
		
	}
	
	/**MinusTermExpr; negate existing operand
	 */
	public void visit(MinusTermExpr mte) {
		
		if (!anotherExprStarted)
			Code.put(Code.neg);
		
	}

}
