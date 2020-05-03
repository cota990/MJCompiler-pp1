package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

/**
 * Class used for generating code for expressions with operators with left associativity
 */
public class ExpressionLeftAssocCodeGenerator extends VisitorAdaptor {
	
	Logger log = Logger.getLogger(ExpressionLeftAssocCodeGenerator.class);
	
	private int depth;
	
	/**Counter which is used to determine whether new expression should be calculated;
	 * if > 0 nothing should be done, as that expression was calculated in separate generator;
	 * <br> default value is false
	 */
	protected int anotherExprStarted = 0;
	
	/**
	 * @param depth
	 */
	public ExpressionLeftAssocCodeGenerator(int depth) {
		this.depth = depth;
	}

	/*
	 * expression starts
	 */
	
	/** Left Parenthesis exprssion started; sets anotherExprStarted flag; used in method calls and composite factors
	 * <br> for methods, it should start new expression for every actual parameter; ends generator when parameter is calculated
	 * <br> for composites, starts new expression generator, ends generator when expression is calculated
	 */
	public void visit(LeftParenthesis lp) {
		
		if (anotherExprStarted++ == 0) {
			
			log.info("LeftParenthesis " + depth);
		
			SyntaxNode parent = lp.getParent();
			
			if (parent instanceof MethodCallFactor) {
				
				MethodCallFactor methodCallFactor = (MethodCallFactor) parent;
				
				if (methodCallFactor.getActParamsOption() instanceof ActualParameters) {
					
					//anotherExprStarted++;
					
					ActualParameters actualParameters = (ActualParameters) methodCallFactor.getActParamsOption();
					
					SyntaxNode actualPars = actualParameters.getActPars();
					
					while (actualPars instanceof MultipleActualParameters) {
						actualPars = ((MultipleActualParameters) actualPars).getActPars();
					}
					
					SingleActPar sap = ((SingleActualParameter) actualPars).getSingleActPar();
					
					ActualParametersCodeGenerator actualParametersCodeGenerator = new ActualParametersCodeGenerator (depth + 1);
					sap.traverseBottomUp(actualParametersCodeGenerator);
					
					actualPars = sap.getParent().getParent();
					
					while (actualPars instanceof MultipleActualParameters) {
						
						SingleActPar parentSap = ((MultipleActualParameters) actualPars).getSingleActPar();
						
						actualPars = actualPars.getParent();
						
						parentSap.traverseBottomUp(actualParametersCodeGenerator);
						
					}
					
					/*ExpressionLeftAssocCodeGenerator actualParametersCodeGenerator = new ExpressionLeftAssocCodeGenerator (depth + 1);
					
					actualParameters.traverseBottomUp(actualParametersCodeGenerator);*/
					
				}
				
			}
			
			else if (parent instanceof CompositeFactor) {
				
				CompositeFactor compositeFactor = (CompositeFactor) parent;
				
				if (compositeFactor.getExpr() instanceof ExprWithAssign) {
					
					ExpressionRightAssocCodeGenerator parameterGenerator = new ExpressionRightAssocCodeGenerator();
					
					compositeFactor.getExpr().traverseBottomUp(parameterGenerator);
					
				}
				
				else if (compositeFactor.getExpr() instanceof ExprWithoutAssign) {
					
					ExpressionLeftAssocCodeGenerator parameterGenerator = new ExpressionLeftAssocCodeGenerator(depth + 1);
					
					compositeFactor.getExpr().traverseBottomUp(parameterGenerator);
					
				}
			
			}
		
		}
		
	}
	
	/** Starts new generator for each next actual parameter
	 */
	/*public void visit (Comma c) {
		
		anotherExprStarted++;
		
	}*/
	
	
	/**Left Bracket expression started; sets anotherExprStarted flag; used in ArrayDesignator and New arrays
	 * <br> for ArrayDesignator, starts new expression generator, ends generator when expression is calculated
	 * <br> for New arrays, starts new expression generator, ends generator when expression is calculated
	 */
	public void visit(LeftBracket lb) {
		
		if (anotherExprStarted++ == 0) {
			
			log.info("LeftBracket " + depth);
			
			SyntaxNode parent = lb.getParent();
			
			if (parent instanceof ArrayDesignator) {
				
				ArrayDesignator arrayDesignator = (ArrayDesignator) parent;
				
				if (arrayDesignator.getExpr() instanceof ExprWithAssign) {
					
					ExpressionRightAssocCodeGenerator parameterGenerator = new ExpressionRightAssocCodeGenerator();
					
					arrayDesignator.getExpr().traverseBottomUp(parameterGenerator);
					
				}
				
				else if (arrayDesignator.getExpr() instanceof ExprWithoutAssign) {
					
					ExpressionLeftAssocCodeGenerator parameterGenerator = new ExpressionLeftAssocCodeGenerator(depth + 1);
					
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
					
					ExpressionLeftAssocCodeGenerator parameterGenerator = new ExpressionLeftAssocCodeGenerator(depth + 1);
					
					newArrayFactor.getExpr().traverseBottomUp(parameterGenerator);
					
				}
				
			}
			
		}
		
	}
	
	/*
	 * expression ends
	 */
	
	/** CompositeFactor; expression in parenthesis calculated, reset anotherExprStarted flag
	 */
	public void visit(CompositeFactor cf) {
		
		log.info("CompositeFactor " + depth);
		anotherExprStarted--;
		
	}
	
	/**Array designator; reset anotherExprStarted flag
	 * loads elem value; <br>expr stack: ... -> ..., val(adr if struct class or array)
	 */
	public void visit(ArrayDesignator ad) {
		
		if (anotherExprStarted-- == 1) {
			
			log.info("ArrayDesignator " + depth);
			Code.load(ad.myobjimpl);
			
		}
		
	}
	
	/**NewArrayFactor; allocate memory for array and reset anotherExprStarted flag
	 */
	public void visit(NewArrayFactor naf) {
		
		if (anotherExprStarted-- == 1) {
			
			log.info("NewArrayFactor " + depth);
			Code.put(Code.newarray);
	    	Code.put(naf.getType().mystructimpl == MyTabImpl.charType ? 0 : 1);			
			
		}
		
	}
	
	/** Actual parameter done, decrement counter
	 */
	public void visit(ActualParameters ap) {
		
		if (anotherExprStarted -- == 1) {
		log.info("ActualParameters " + depth);
		}
		
	}
	
	/** Actual parameter done, decrement counter
	 */
	public void visit(NoActualParameters nap) {
		
		if (anotherExprStarted -- == 1) {
		log.info("NoActualParameters " + depth);
		}
		
	}
	/*
	 * designators
	 */
	
	/**SimpleDesignator;
	 * loads obj; <br>expr stack: ... -> ..., val(adr if struct class or array)
	 */
	public void visit(SimpleDesignator sd) {
		
		if (anotherExprStarted == 0 
				&& sd.myobjimpl.getKind() != Obj.Meth) {
			
			log.info("SimpleDesignator " + depth);
			
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
				
				Code.load(thisPointer);				
				
			}
			
			Code.load(sd.myobjimpl);
			
		}
		
	}
	
	/**Class designator;
	 * loads field value;<br>expr stack: ... -> ..., val(adr if struct class or array)
	 */
	public void visit(ClassDesignator cd) {
		
		if (anotherExprStarted == 0) {
			
			log.info("ClassDesignator " + depth);
			if (cd.myobjimpl.getKind() != Obj.Meth)
				Code.load(cd.myobjimpl);
			
		}
		
	}
	
	public void visit (MethodDesignator md) {
		
		if (anotherExprStarted == 0) {
			
			log.info("MethodDesignator " + depth);
			
			// load this if not global method
			if (!md.myobjimpl.isGlobal()) {
				
				// this is the same object as this in method
				
				log.info(md.getDesignator() instanceof SimpleDesignator);
				
				if (md.getDesignator() instanceof SimpleDesignator) {
					
					Code.put(Code.load_n + 0);
					
				}
				
			}
			
		}
	}
	
	/*
	 * symbolic constants
	 */
	
	/** number processing; load numeric constant
	 */
	public void visit(NumberConst nc) {
		
		if (anotherExprStarted == 0) {
			
			log.info("NumberConst " + depth);
			Code.loadConst(nc.getNumberConst());
			
		}
		
	}
	
	/** printable character processing; load character
	 */
	public void visit(CharConst cc) {
		
		if (anotherExprStarted == 0) {
			
			log.info("CharConst " + depth);
			Code.loadConst(Integer.valueOf(cc.getCharConst()));
			
		}
		
	}
	
	/** bool constant processing; load 1 if true 0 if false
	 */
	public void visit(BoolConst bc) {
		
		if (anotherExprStarted == 0) {
			
			log.info("BoolConst " + depth);
			
			if (bc.getBoolConst())
				Code.loadConst(1);
			
			else
				Code.loadConst(0);
			
		}	
		
	}
	
	/*
	 * factors
	 */
	
	/**MethodCallFactor; all actual parameter expression calculated; check if global'
	 * <br> if global: call method (CALL statement and return address) and reset anotherExprStarted
	 * <br> if class: invoke virtual and reset anotherExprStarted
	 */
	public void visit(MethodCallFactor mcf) {
		
		if (anotherExprStarted == 0) {
			
			log.info("MethodCallFactor " + depth);
			
			if (mcf.getMethodDesignator().myobjimpl.isGlobal()) {
			
				if (!(mcf.getMethodDesignator().getDesignator().myobjimpl.getName().equals("ord")
						|| mcf.getMethodDesignator().getDesignator().myobjimpl.getName().equals("chr")
						|| mcf.getMethodDesignator().getDesignator().myobjimpl.getName().equals("len") )) {
				
					int destAdr = mcf.getMethodDesignator().getDesignator().myobjimpl.getAdr() - Code.pc;
					Code.put(Code.call);
					Code.put2(destAdr);
					
				}
				
				else {
					
					if (mcf.getMethodDesignator().getDesignator().myobjimpl.getName().equals("len"))
						Code.put (Code.arraylength);
					
				}
			
			}
			
			else {
				
				// load vft pointer on expr stack
				
				if (mcf.getMethodDesignator().getDesignator() instanceof SimpleDesignator) {
					
					Code.put(Code.load_n + 0);
					
				}
				
				else if (mcf.getMethodDesignator().getDesignator() instanceof ClassDesignator) {
					
					ExpressionLeftAssocCodeGenerator thisGenerator = new ExpressionLeftAssocCodeGenerator(0);
					
					mcf.getMethodDesignator().traverseBottomUp(thisGenerator);
				
				}
				
				Code.put(Code.getfield);
				Code.put2(0);
				
				// invoke virtual function
				
				Code.put(Code.invokevirtual);
				
				for (int i = 0; i < mcf.getMethodDesignator().myobjimpl.getName().length(); i++)
					Code.put4( (int) mcf.getMethodDesignator().myobjimpl.getName().charAt(i));
				
				Code.put4(-1);
				
			}
		
		}
		
	}
	
	/**NewFactor; allocate memory for class instance
	 */
	public void visit(NewFactor nf) {
		
		if (anotherExprStarted == 0) {
			
			log.info("NewFactor " + depth);
			
			Code.put(Code.new_);
			Code.put2(nf.getType().mystructimpl.getNumberOfFields() * 4);
		
		}
		
	}
	
	/*
	 * terms
	 */
	
	/** MultipleFactorTerm; put operator in code
	 */
	public void visit(MultipleFactorTerm mft) {
		
		if (anotherExprStarted == 0) {
			
			log.info("MultipleFactorTerm " + depth);
			
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
		
		if (anotherExprStarted == 0) {
			
			log.info("MultipleTermExpr " + depth);
			
			if (mte.getAddopLeft() instanceof Plus)
				Code.put(Code.add);
			
			else if (mte.getAddopLeft() instanceof Minus)
				Code.put(Code.sub);
			
		}
		
	}
	
	/**MinusTermExpr; negate existing operand
	 */
	public void visit(MinusTermExpr mte) {
		
		if (anotherExprStarted == 0) {
			
			log.info("MinusTermExpr " + depth);
			Code.put(Code.neg);
			
		}
		
	}

}
