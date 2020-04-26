package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;

public class CodeGenerator extends VisitorAdaptor {
	
	Logger log = Logger.getLogger(CodeGenerator.class);
	
	private int mainPc;
	
	private int nVars = 0;
	
	private MyObjImpl foreachIdent;
//	
//	private List<List<Integer>> rightAssociationOperators = new ArrayList<List<Integer>> ();
//	
//	private List<List<Obj>> rightAssociationObjects = new ArrayList <List<Obj>> ();
//	
//	private void rightAssociationFixup () {
//		
//		if (!rightAssociationOperators.isEmpty()) {
//	    	
//    		List<Integer> operators = rightAssociationOperators.get(rightAssociationOperators.size() - 1);
//			List<Obj> designators = rightAssociationObjects.get(rightAssociationObjects.size() - 1);
//			
//			while (!designators.isEmpty()) {
//				
//				Code.put(operators.get(operators.size() - 1));
//				
//				//if (designators.get(designators.size() - 1).getKind() == Obj.Elem)
//					//Code.put(Code.dup_x2);
//				//else
//					//Code.put(Code.dup);
//				Code.store(designators.get(designators.size() - 1));
//				
//				operators.remove(operators.size() - 1);
//				designators.remove(designators.size() - 1);
//				
//			}
//			
//			rightAssociationObjects.remove(rightAssociationObjects.size() - 1);
//			rightAssociationOperators.remove(rightAssociationOperators.size() - 1);
//		
//    	}
//		
//	}
//	
	/**
	 * @return the mainPc
	 */
	public int getMainPc() {
		return mainPc;
	}

	/**
	 * @return the nVars
	 */
	public int getnVars() {
		return nVars;
	}
	
	/*
	 * global variables
	 */
	
	/**SingleVarDecl;
	 * <br> number of global variables is needed for generating obj file
	 */
	public void visit(SingleVarDecl svd) {
		
		nVars++;
		
	}
	
	/*
	 * main and other methods
	 */
	
	/**MethodName;
	 * <br> generates code for entering function (ENTER command, number of formal parameters, number of formal parameters + local variables); 
	 * collects main address
	 * TODO this pointer as formal parameter; so far only global methods
	 */
	public void visit (MethodName mn) {
		
		if("main".equalsIgnoreCase(mn.getMethodName())){
			
			mainPc = Code.pc;
		
		}

		mn.myobjimpl.setAdr(Code.pc);
		
		Code.put(Code.enter);
		Code.put(mn.myobjimpl.getLevel());
		Code.put(mn.myobjimpl.getLocalSymbols().size());
		
	}
	
	/**ReturnStatement;
	 * <br> generates code for return from function (value is already loaded, if non-void) : EXIT command, RETURN command
	 */
	public void visit (ReturnStatement rs) {

		Code.put(Code.exit); Code.put(Code.return_);
	
	}
	
	/** Global method end;
	 * <br> generate code for return from function if void (EXIT command, RETURN command)
	 * <br> throws runtime error if method is non void
	 */
	public void visit (MethodDecl mds) {
		
		if (mds.getMethodName().myobjimpl.getType() == MyTabImpl.noType) {
			
			Code.put(Code.exit); Code.put(Code.return_);
			
		}
		
		else {
			
			Code.put(Code.trap); Code.put(1);
			
		}
			
	}
	
	/*
	 * statement designators and expressions
	 */
	
	/**Destination generator; used in assign, increment, decrement and read statements
	 */
	public void visit(Destination d) {
		
		DestinationCodeGenerator destinationCodeGenerator = new DestinationCodeGenerator();
		
		d.traverseBottomUp(destinationCodeGenerator);
		
	}
	
	/**Source generator; used in assign statement
	 */
	public void visit(SourceSuccess ss) {
		
		if (ss.getExpr() instanceof ExprWithAssign) {
			
			ExpressionRightAssocCodeGenerator sourceExprGenerator = new ExpressionRightAssocCodeGenerator();
			
			ss.getExpr().traverseBottomUp(sourceExprGenerator);
			
		}
		
		else if (ss.getExpr() instanceof ExprWithoutAssign) {
			
			ExpressionLeftAssocCodeGenerator sourceExprGenerator = new ExpressionLeftAssocCodeGenerator();
			
			ss.getExpr().traverseBottomUp(sourceExprGenerator);
			
		}
		
	}
	
	/** ForeachArray; load address of array used in foreach statement
	 */
	public void visit(ForeachArray fa) {
		
		ExpressionLeftAssocCodeGenerator foreachArrayGenerator = new ExpressionLeftAssocCodeGenerator();
		
		fa.traverseBottomUp(foreachArrayGenerator);
		
	}
	
	/** IteratorName; will be used in foreach statement code generation
	 */
	public void visit(IteratorName in) {
		
		foreachIdent = in.myobjimpl;
		
	}
	
	/** Return statement starter; generate return expr if needed
	 */
	public void visit(Return r) {
		
		ReturnStatement returnStatement = (ReturnStatement) r.getParent();
		
		if (returnStatement.getReturnExprOption() instanceof ReturnExpr) {
			
			ReturnExpr returnExpr = (ReturnExpr) returnStatement.getReturnExprOption();
			
			if (returnExpr.getExpr() instanceof ExprWithAssign) {
				
				ExpressionRightAssocCodeGenerator returnExprGenerator = new ExpressionRightAssocCodeGenerator();
				
				returnExpr.getExpr().traverseBottomUp(returnExprGenerator);
				
			}
			
			else if (returnExpr.getExpr() instanceof ExprWithoutAssign) {
				
				ExpressionLeftAssocCodeGenerator returnExprGenerator = new ExpressionLeftAssocCodeGenerator();
				
				returnExpr.getExpr().traverseBottomUp(returnExprGenerator);
				
			}
			
		}
		
	}
	
	/** Print statement starter; generate print expr and number constant if needed
	 */
	public void visit(Print p) {
		
		PrintStatement printStatement = (PrintStatement) p.getParent();
		
		if (printStatement.getExpr() instanceof ExprWithAssign) {
			
			ExpressionRightAssocCodeGenerator printExprGenerator = new ExpressionRightAssocCodeGenerator();
			
			printStatement.getExpr().traverseBottomUp(printExprGenerator);
			
		}
		
		else if (printStatement.getExpr() instanceof ExprWithoutAssign) {
			
			ExpressionLeftAssocCodeGenerator printExprGenerator = new ExpressionLeftAssocCodeGenerator();
			
			printStatement.getExpr().traverseBottomUp(printExprGenerator);
			
		}
		
		if (printStatement.getPrintOption() instanceof PrintArg) {
			
			PrintArg printArg = (PrintArg) printStatement.getPrintOption();
			
			ExpressionLeftAssocCodeGenerator printArgGenerator = new ExpressionLeftAssocCodeGenerator();
			
			printArg.traverseBottomUp(printArgGenerator);
			
		}
		
		else if (printStatement.getPrintOption() instanceof NoPrintArg)
			Code.loadConst(5);
		
	}
	
	/*
	 *  print and read statements
	 */
	
	/** Read statement; 
	 * <br>read designator already processed and put on expr stack; pop value;
	 * <br> check designator type:
	 * <br> if designator int: put read operation
	 * <br> if designator char: put bread operation
	 * <br> if designator bool: read char by char, check if "true" or "false"; if none generate error?
	 * <br> store designator
	 */
	public void visit(ReadStatement rs) {
		
		Code.put(Code.pop);
		
		if (rs.getDestination().myobjimpl.getType() == MyTabImpl.intType)
			Code.put(Code.read);
		
		else if (rs.getDestination().myobjimpl.getType() == MyTabImpl.charType)
			Code.put(Code.bread);
		
		else if (rs.getDestination().myobjimpl.getType() == MyTabImpl.boolType) {
			
			// read first character; dup value for next check; check if 't'; if not jump to false check
			Code.put(Code.bread);
			Code.put(Code.dup);
			Code.loadConst(116);
			Code.putFalseJump(Code.eq, 0);
			
			int adr1 = Code.pc - 2;
			
			// pop dupped value; read next character; check if 'r'; if not jump to error 
			Code.put(Code.pop);
			Code.put(Code.bread);			
			Code.loadConst(114);
			Code.putFalseJump(Code.eq, 0);
			
			int adr2 = Code.pc - 2;
			
			// read next character; check if 'u'; if not jump to error
			Code.put(Code.bread);			
			Code.loadConst(117);
			Code.putFalseJump(Code.eq, 0);
			
			int adr3 = Code.pc - 2;
			
			// read next character; check if 'e'; if yes load 1 and finish; if not jump to error
			Code.put(Code.bread);			
			Code.loadConst(101);
			Code.putFalseJump(Code.eq, 0);
			
			int adr4 = Code.pc - 2;
			
			Code.loadConst(1);
			Code.putJump(0);
			
			int adr5 = Code.pc - 2;
			
			// fix first jump, and check if read character 'f'; if not jump to error
			Code.fixup(adr1);
			
			Code.loadConst(102);
			Code.putFalseJump(Code.eq, 0);
			
			int adr6 = Code.pc - 2;
			
			// read next character; check if 'a'; if not jump to error
			Code.put(Code.bread);			
			Code.loadConst(97);
			Code.putFalseJump(Code.eq, 0);
			
			int adr7 = Code.pc - 2;
			
			// read next character; check if 'l'; if not jump to error
			Code.put(Code.bread);			
			Code.loadConst(108);
			Code.putFalseJump(Code.eq, 0);
			
			int adr8 = Code.pc - 2;
			
			// read next character; check if 's'; if not jump to error
			Code.put(Code.bread);			
			Code.loadConst(115);
			Code.putFalseJump(Code.eq, 0);
			
			int adr9 = Code.pc - 2;
			
			// read next character; check if 'e'; if not jump to error
			Code.put(Code.bread);			
			Code.loadConst(101);
			Code.putFalseJump(Code.eq, 0);
			
			int adr10 = Code.pc - 2;
			
			Code.loadConst(0);
			Code.putJump(0);
			
			int adr11 = Code.pc - 2;
			
			// generate error
			Code.fixup(adr2); Code.fixup(adr3); Code.fixup(adr4); Code.fixup(adr6); 
			Code.fixup(adr7); Code.fixup(adr8); Code.fixup(adr9); Code.fixup(adr10);
			
			// TODO generate error
			
			// here it's jumped if bool is read
			Code.fixup(adr5);Code.fixup(adr11);
			
		}
		
		Code.store (rs.getDestination().myobjimpl);
		
	}
	
	/**Print statement;
	 * <br>print expression and length argument loaded; expr stack: ...,val,length
	 * <br> check expr type:
	 * <br> if designator int: put print operation
	 * <br> if designator char: put bprint operation
	 * <br> if designator bool: switch places of expression and length; check if expression 1; if yes print 'true', if not print 'false'
	 * <br> store designator
	 */
	public void visit(PrintStatement ps) {
		
		if (ps.getExpr().mystructimpl == MyTabImpl.intType)
			Code.put(Code.print);
		
		else if (ps.getExpr().mystructimpl == MyTabImpl.charType)
			Code.put(Code.bprint);
		
		else if (ps.getExpr().mystructimpl == MyTabImpl.boolType) {
			
			// switch value and length places; and check if 1
			Code.put(Code.dup_x1);
			Code.put(Code.pop);
			Code.loadConst(1);
			Code.putFalseJump(Code.eq, 0);
			
			int adr1 = Code.pc - 2;
			
			//if 1, print 'true'; when load 't', switch places with length; jump to finish
			Code.loadConst(116);
			Code.put(Code.dup_x1);
			Code.put(Code.pop);
			Code.put(Code.bprint);
			Code.loadConst(114);
			Code.loadConst(1);
			Code.put(Code.bprint);
			Code.loadConst(117);
			Code.loadConst(1);
			Code.put(Code.bprint);
			Code.loadConst(101);
			Code.loadConst(1);
			Code.put(Code.bprint);
			
			Code.putJump(0);
			
			int adr2 = Code.pc - 2;
			
			Code.fixup(adr1);
			
			// print 'false'; when load 'f', switch places with length;			
			Code.loadConst(102);
			Code.put(Code.dup_x1);
			Code.put(Code.pop);
			Code.put(Code.bprint);
			Code.loadConst(97);
			Code.loadConst(1);
			Code.put(Code.bprint);
			Code.loadConst(108);
			Code.loadConst(1);
			Code.put(Code.bprint);
			Code.loadConst(115);
			Code.loadConst(1);
			Code.put(Code.bprint);
			Code.loadConst(101);
			Code.loadConst(1);
			Code.put(Code.bprint);
			
			Code.fixup(adr2);
		
		}
		
	}
	
	

//	
//	/* assignment, increment and decrement statements */
//	
//	public void visit (AssignStatementSuccess AssignStatementSuccess) {
//		
//		// assign var: expr stack: prev_value, new_value; store -> pop
//		// assign elem: expr stack: arr_adr, arr_ind, prev_value, new_value;  dup_x1 -> pop -> pop -> store
//		// assign fld: expr stack: class_adr, prev_valie, new_value; dup_x1 -> pop -> pop -> store
//		if (AssignStatementSuccess.getAssignop() instanceof Assign) {
//			
//			if (AssignStatementSuccess.getDestination().obj.getKind() == Obj.Elem ||
//					AssignStatementSuccess.getDestination().obj.getKind() == Obj.Fld) {
//				
//				Code.put(Code.dup_x1);
//				Code.put(Code.pop);
//				Code.put(Code.pop);
//				
//			}
//			
//					
//			Code.store (AssignStatementSuccess.getDestination().obj);
//			
//			// skida postavljeni designator
//			if (AssignStatementSuccess.getDestination().obj.getKind() == Obj.Var) Code.put(Code.pop);
//			
//			/*if (AssignStatementSuccess.getDestination().obj.getKind() != Obj.Elem 
//					&& AssignStatementSuccess.getDestination().obj.getKind() != Obj.Fld )
//				Code.put(Code.pop);*/
//		
//		}
//		
//		// TODO combined operators
//		// assign var: expr stack: prev_value, new_value; op -> store
//		// assign elem: expr stack: arr_adr, arr_ind, prev_value, new_value; op -> store
//		// assign fld: expr stack: class_adr, prev_value, new_value; op -> store
//		else if (AssignStatementSuccess.getAssignop() instanceof AddopRightAssign) {
//			
//			//log.info("AddopRightAssign");
//			
//			AddopRightAssign addop = (AddopRightAssign) AssignStatementSuccess.getAssignop();
//			
//			if (addop.getAddopRight() instanceof PlusAssign)
//				Code.put(Code.add);
//				
//			
//			else if (addop.getAddopRight() instanceof MinusAssign) 
//				Code.put(Code.sub);
//				
//			Code.store (AssignStatementSuccess.getDestination().obj);
//			
//		}
//		
//		else if (AssignStatementSuccess.getAssignop() instanceof MulopRightAssign) {
//			
//			//log.info("MulopRightAssign");
//			
//			MulopRightAssign mulop = (MulopRightAssign) AssignStatementSuccess.getAssignop();
//			
//			if (mulop.getMulopRight() instanceof MulAssign) 
//				Code.put(Code.mul);
//				
//			
//			else if (mulop.getMulopRight() instanceof DivAssign)
//				Code.put(Code.div);
//			
//			else if (mulop.getMulopRight() instanceof ModAssign) 
//				Code.put(Code.rem);
//				
//			Code.store (AssignStatementSuccess.getDestination().obj);
//			
//		}
//		
//	}
//	
//	public void visit (IncrementStatement IncrementStatement) {
//		
//		Code.loadConst(1);
//		Code.put(Code.add);
//		Code.store (IncrementStatement.getDestination().obj);
//		
//	}
//	
//	public void visit (DecrementStatement DecrementStatement) {
//		
//		Code.loadConst(1);
//		Code.put(Code.sub);
//		Code.store (DecrementStatement.getDestination().obj);
//		
//	}
//	
//	public void visit (Source Source) {
//    	
//    	rightAssociationFixup();
//    	
//    }
//
//	public void visit (ReturnStatement ReturnStatement) {
//		
//		rightAssociationFixup();
//		
//		Code.put(Code.exit); Code.put(Code.return_);
//		
//	}
//	
//	public void visit (MethodDeclSuccess MethodDeclSuccess) {
//		
//		if (MethodDeclSuccess.getMethodName().obj.getType() == Tab.noType) {
//			
//			Code.put(Code.exit); Code.put(Code.return_);
//			
//		}
//		
//		else {
//			
//			Code.put(Code.trap); Code.put(1);
//			
//		}
//			
//	}
//	
//	/* new */
//	
//	public void visit (NewArrayFactor NewArrayFactor) {
//		
//		rightAssociationFixup();
//		
//		Code.put(Code.newarray);
//    	Code.put(NewArrayFactor.getType().struct == Tab.charType ? 0 : 1);
//    
//    }
//	
//	public void visit (NewFactor NewFactor) {
//		
//		Code.put(Code.new_);
//		Code.put2(NewFactor.getType().struct.getNumberOfFields());
//		
//	}
//	
//	/* numeric, char and boolean constants */
//	
//	public void visit (NumFactor NumFactor) {
//		
//		Code.loadConst(NumFactor.getNumberValue());
//		
//	}
//	
//	public void visit (CharFactor CharFactor) {
//		
//		Code.loadConst(Integer.valueOf(CharFactor.getCharValue()));
//		
//	}
//	
//	public void visit (BoolFactor BoolFactor) {
//		
//		if (BoolFactor.getBoolValue()) 
//			Code.loadConst(1);
//		
//		else Code.loadConst(0);
//		
//	}
//	
//	/* designators */
//	
//	public void visit (SimpleDesignator SimpleDesignator) {
//		
//		if (SimpleDesignator.obj.getKind() == Obj.Elem ||
//				SimpleDesignator.obj.getKind() == Obj.Var ||
//				SimpleDesignator.obj.getKind() == Obj.Fld)
//		Code.load (SimpleDesignator.obj);
//    	
//    }
//	
//	// TODO zasad cemo ga ucitavati uvek i svuda; na steku su i adresa, i indeks, i vrednost; dok ne vidimo kako i sta
//	// ako mu je parent ClassDesignator; ima vec ucitanu adresu klase; treba je skloniti
//	// ako mu je parent ArrayDesignator; ima vec ucitanu adresu roditeljskog niza i index, treba ih skloniti
//	public void visit (ArrayDesignator ArrayDesignator) {
//		
//		rightAssociationFixup();
//		
//		if (ArrayDesignator.getDesignator() instanceof ClassDesignator) {
//			
//			// if ClassDesignator (ie A.B[c])
//			// expr_stack: par_adr, adr, val; switch places
//			Code.put(Code.dup_x2); Code.put(Code.pop);
//			Code.put(Code.dup_x2); Code.put(Code.pop); Code.put(Code.pop);
//			
//		}
//		
//		if (ArrayDesignator.getDesignator() instanceof ArrayDesignator) {
//			
//			log.info("ArrayDesignator");
//			
//		}
//		
//		Code.put(Code.dup2);
//		Code.load(ArrayDesignator.obj);
//		
//	}
//	
//	// TODO za polja klase isto; na steku i instanca klase i vrednost polja
//	// ako mu je child ClassDesignator: ima vec ucitanu adresu roditeljske klase; treba je skloniti
//	// TODO ako mu je child ArrayDesignator:
//	public void visit (ClassDesignator ClassDesignator) {
//		
//		Code.put(Code.dup);
//		Code.load (ClassDesignator.obj);
//		
//		if (ClassDesignator.getDesignator() instanceof ClassDesignator) {
//			
//			// if ClassDesignator (ie A.B.c)
//			// expr_stack: par_adr, adr, val; switch places
//			Code.put(Code.dup_x2); Code.put(Code.pop);
//			Code.put(Code.dup_x2); Code.put(Code.pop); Code.put(Code.pop);
//			
//		}
//		
//		if (ClassDesignator.getDesignator() instanceof ArrayDesignator) {
//			
//			// if ArrayDesignator (ie A[i].b)
//			// expr_stack: par_adr, ind, adr, val; switch places
//			Code.put(Code.dup_x2); Code.put(Code.pop);
//			Code.put(Code.dup_x2); Code.put(Code.pop); Code.put(Code.pop);
//			
//			Code.put(Code.dup_x2); Code.put(Code.pop);
//			Code.put(Code.dup_x2); Code.put(Code.pop); Code.put(Code.pop);
//		}
//		
//	}
//	
//	//TODO this means that it's used for calculation; must check for possible combined operator
//	public void visit (DeclDesignator DeclDesignator) {
//		
//		SyntaxNode parent = DeclDesignator.getParent();
//		
//		while (parent != null) {
//			
//			log.info(parent.getClass());
//			
//			if (parent instanceof MultipleTermExprAssign || parent instanceof MultipleFactorTermAssign) break;
//			
//			parent = parent.getParent();
//			
//		}
//		
//		if (parent == null) {
//			
//			// only for calculation; if array expr stack: adr, ind, val -> only val needed
//			// if class expr stack: adr, val -> only val needed
//			
//			if (DeclDesignator.getDesignator() instanceof ArrayDesignator) {
//				
//				Code.put(Code.dup_x2); Code.put(Code.pop); Code.put(Code.pop); Code.put(Code.pop);
//				
//			}
//			
//			else if (DeclDesignator.getDesignator() instanceof ClassDesignator) {
//				
//				Code.put(Code.dup_x1); Code.put(Code.pop); Code.put(Code.pop);
//				
//			}
//			
//			else {
//				
//				log.info("Greska u DeclDesign");
//				log.info(DeclDesignator.getDesignator().getClass());
//			}
//			
//		}
//				
//	}
//	
//	/* factors */
//	
//	public void visit (CompositeFactor CompositeFactor) {
//    	
//    	rightAssociationFixup();
//    			
//    }
//	
//	/* arithmetic operations */
//	
//	public void visit (MultipleFactorTerm MultipleFactorTerm) {
//		
//		if (MultipleFactorTerm.getMulopLeft() instanceof Mul) 
//			Code.put(Code.mul);
//		
//		else if (MultipleFactorTerm.getMulopLeft() instanceof Div) 
//			Code.put(Code.div);
//		
//		else if (MultipleFactorTerm.getMulopLeft() instanceof Mod) 
//			Code.put(Code.rem);
//		
//	}
//	
//	public void visit (MultipleFactorTermAssign MultipleFactorTermAssign) {
//		
//		if (MultipleFactorTermAssign.getMulopRight() instanceof MulAssign) 
//			rightAssociationOperators.get(rightAssociationOperators.size() - 1).add(Code.mul);
//		
//		else if (MultipleFactorTermAssign.getMulopRight() instanceof DivAssign)
//			rightAssociationOperators.get(rightAssociationOperators.size() - 1).add(Code.div);
//		
//		else if (MultipleFactorTermAssign.getMulopRight() instanceof ModAssign)
//			rightAssociationOperators.get(rightAssociationOperators.size() - 1).add(Code.rem);
//			
//		rightAssociationObjects.get(rightAssociationObjects.size() - 1).add(MultipleFactorTermAssign.obj);
//		
//	}
//	
//	public void visit (MultipleTermExpr MultipleTermExpr) {
//		
//		if (MultipleTermExpr.getAddopLeft() instanceof Plus)
//			Code.put(Code.add);
//		
//		else if (MultipleTermExpr.getAddopLeft() instanceof Minus)
//			Code.put(Code.sub);
//		
//	}
//	
//	public void visit (MultipleTermExprAssign MultipleTermExprAssign) {
//		
//		if (MultipleTermExprAssign.getAddopRight() instanceof PlusAssign)
//			rightAssociationOperators.get(rightAssociationOperators.size() - 1).add(Code.add);
//		
//		else if (MultipleTermExprAssign.getAddopRight() instanceof MinusAssign)
//			rightAssociationOperators.get(rightAssociationOperators.size() - 1).add(Code.sub);
//			
//		rightAssociationObjects.get(rightAssociationObjects.size() - 1).add(MultipleTermExprAssign.obj);
//		
//	}
//	
//	public void visit (MinusSingleTermExpr MinusSingleTermExpr) {
//		
//		Code.put(Code.neg);
//		
//	}
//	
//	/* combined operators starters */
//	
//	public void visit (Destination Destination) {
//		
//		rightAssociationObjects.add(new ArrayList<Obj> ());
//		rightAssociationOperators.add(new ArrayList<Integer> ());
//		
//	}
//	
//	public void visit (LeftBracketExpr LeftBracketExpr) {
//    	
//		rightAssociationObjects.add(new ArrayList<Obj> ());
//		rightAssociationOperators.add(new ArrayList<Integer> ());
//    	
//    }
//    
//    public void visit (LeftParenthesisExpr LeftParenthesisExpr) {
//    	
//    	rightAssociationObjects.add(new ArrayList<Obj> ());
//		rightAssociationOperators.add(new ArrayList<Integer> ());
//    	
//    }
//	
}
