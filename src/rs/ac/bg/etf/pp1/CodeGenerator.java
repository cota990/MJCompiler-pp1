//package rs.ac.bg.etf.pp1;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.apache.log4j.Logger;
//
//import rs.ac.bg.etf.pp1.ast.*;
//import rs.etf.pp1.mj.runtime.Code;
//import rs.etf.pp1.symboltable.Tab;
//import rs.etf.pp1.symboltable.concepts.Obj;
//
//public class CodeGenerator extends VisitorAdaptor {
//	
//	Logger log = Logger.getLogger(CodeGenerator.class);
//	
//	private int mainPc;
//	
//	private int nVars = 0;
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
//	/**
//	 * @return the mainPc
//	 */
//	public int getMainPc() {
//		return mainPc;
//	}
//
//	/**
//	 * @return the nVars
//	 */
//	public int getnVars() {
//		return nVars;
//	}
//	
//	/* global variables */
//	
//	public void visit(SingleVarDeclSuccess SingleVarDeclSuccess) {
//		
//		this.nVars++;
//	}
//	
//	/* print and read statements */
//	
//	// read var: expr stack: prev_value, new_value; store -> pop
//	// read elem: expr stack: arr_adr, arr_ind, prev_value, new_value;  dup_x1 -> pop -> pop -> store
//	// read fld: expr stack: class_adr, prev_valie, new_value; dup_x1 -> pop -> pop -> store
//	public void visit(ReadStatement ReadStatement) {
//		
//		if (ReadStatement.getDesignator().obj.getType() == Tab.intType) {
//			
//			Code.put(Code.read);
//			
//			if (ReadStatement.getDesignator().obj.getKind() == Obj.Elem ||
//					ReadStatement.getDesignator().obj.getKind() == Obj.Fld) {
//				
//				Code.put(Code.dup_x1);
//				Code.put(Code.pop);
//				Code.put(Code.pop);
//				
//			}
//			Code.store (ReadStatement.getDesignator().obj);
//			
//		}
//		
//		else if (ReadStatement.getDesignator().obj.getType() == Tab.charType) {
//			
//			Code.put(Code.bread);
//			
//			if (ReadStatement.getDesignator().obj.getKind() == Obj.Elem ||
//					ReadStatement.getDesignator().obj.getKind() == Obj.Fld) {
//				
//				Code.put(Code.dup_x1);
//				Code.put(Code.pop);
//				Code.put(Code.pop);
//				
//			}
//			Code.store (ReadStatement.getDesignator().obj);
//		
//		}
//		
//		else if (ReadStatement.getDesignator().obj.getType() == MyTabImpl.boolType) {
//			
//			Code.put(Code.bread);
//			Code.put(Code.dup);
//			Code.loadConst(116);
//			Code.putFalseJump(Code.eq, 0);
//			
//			int adr1 = Code.pc - 2;
//			
//			Code.put(Code.pop);
//			Code.put(Code.bread);			
//			Code.loadConst(114);
//			Code.putFalseJump(Code.eq, 0);
//			
//			int adr2 = Code.pc - 2;
//			
//			Code.put(Code.bread);			
//			Code.loadConst(117);
//			Code.putFalseJump(Code.eq, 0);
//			
//			int adr3 = Code.pc - 2;
//			
//			Code.put(Code.bread);			
//			Code.loadConst(101);
//			Code.putFalseJump(Code.eq, 0);
//			
//			int adr4 = Code.pc - 2;
//			
//			Code.loadConst(1);
//			if (ReadStatement.getDesignator().obj.getKind() == Obj.Elem ||
//					ReadStatement.getDesignator().obj.getKind() == Obj.Fld) {
//				
//				Code.put(Code.dup_x1);
//				Code.put(Code.pop);
//				Code.put(Code.pop);
//				
//			}
//			Code.store (ReadStatement.getDesignator().obj);
//			Code.putJump(0);
//			
//			int adr5 = Code.pc - 2;
//			
//			Code.fixup(adr1); Code.fixup(adr2); Code.fixup(adr3); Code.fixup(adr4);
//			
//			Code.loadConst(102);
//			Code.putFalseJump(Code.eq, 0);
//			
//			int adr6 = Code.pc - 2;
//			
//			Code.put(Code.bread);			
//			Code.loadConst(97);
//			Code.putFalseJump(Code.eq, 0);
//			
//			int adr7 = Code.pc - 2;
//			
//			Code.put(Code.bread);			
//			Code.loadConst(108);
//			Code.putFalseJump(Code.eq, 0);
//			
//			int adr8 = Code.pc - 2;
//			
//			Code.put(Code.bread);			
//			Code.loadConst(115);
//			Code.putFalseJump(Code.eq, 0);
//			
//			int adr9 = Code.pc - 2;
//			
//			Code.put(Code.bread);			
//			Code.loadConst(101);
//			Code.putFalseJump(Code.eq, 0);
//			
//			int adr10 = Code.pc - 2;
//			
//			Code.loadConst(0);
//			if (ReadStatement.getDesignator().obj.getKind() == Obj.Elem ||
//					ReadStatement.getDesignator().obj.getKind() == Obj.Fld) {
//				
//				Code.put(Code.dup_x1);
//				Code.put(Code.pop);
//				Code.put(Code.pop);
//				
//			}
//			Code.store (ReadStatement.getDesignator().obj);
//			
//			Code.fixup(adr6); Code.fixup(adr7); Code.fixup(adr8); Code.fixup(adr9); Code.fixup(adr10);
//			
//			//TODO da li prijaviti gresku
//			/*Code.loadConst(5); // da kazemo da je to I/O error
//			Code.put(Code.trap); */ 
//			
//			Code.fixup(adr5); //Code.fixup(adr11);
//			
//		}
//		
//		// remove previously loaded value of var
//		if (ReadStatement.getDesignator().obj.getKind() == Obj.Var) Code.put(Code.pop);
//		
//	}
//	
//	// everything removed from expr stack
//	
//	public void visit(PrintStatement PrintStatement) {
//		
//		rightAssociationFixup() ;
//		
//		if (PrintStatement.getPrintOption() instanceof NoPrintArg)
//			Code.loadConst(5);
//		
//		if (PrintStatement.getExpr().obj.getType() == Tab.intType) Code.put(Code.print);
//		
//		else if (PrintStatement.getExpr().obj.getType() == Tab.charType) Code.put(Code.bprint);
//		
//		else if (PrintStatement.getExpr().obj.getType() == MyTabImpl.boolType) {
//			
//			Code.put(Code.dup_x1);
//			Code.put(Code.pop);
//			Code.loadConst(1);
//			Code.putFalseJump(Code.eq, 0);
//			
//			int adr1 = Code.pc - 2;
//			
//			Code.loadConst(116);
//			Code.put(Code.dup_x1);
//			Code.put(Code.pop);
//			Code.put(Code.bprint);
//			Code.loadConst(114);
//			Code.loadConst(1);
//			Code.put(Code.bprint);
//			Code.loadConst(117);
//			Code.loadConst(1);
//			Code.put(Code.bprint);
//			Code.loadConst(101);
//			Code.loadConst(1);
//			Code.put(Code.bprint);
//			
//			Code.putJump(0);
//			
//			int adr2 = Code.pc - 2;
//			
//			Code.fixup(adr1);
//			
//			Code.loadConst(102);
//			Code.put(Code.dup_x1);
//			Code.put(Code.pop);
//			Code.put(Code.bprint);
//			Code.loadConst(97);
//			Code.loadConst(1);
//			Code.put(Code.bprint);
//			Code.loadConst(108);
//			Code.loadConst(1);
//			Code.put(Code.bprint);
//			Code.loadConst(115);
//			Code.loadConst(1);
//			Code.put(Code.bprint);
//			Code.loadConst(101);
//			Code.loadConst(1);
//			Code.put(Code.bprint);
//			
//			Code.fixup(adr2);
//			
//		}
//		
//	}
//	
//	public void visit (PrintArg PrintArg) {
//		
//		Code.loadConst(PrintArg.getN1());
//		
//	}
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
//	/* main and other methods*/
//	
//	public void visit (MethodName MethodName) {
//		
//		if("main".equalsIgnoreCase(MethodName.getMethodName())){
//			mainPc = Code.pc;
//		}
//		MethodName.obj.setAdr(Code.pc);
//		
//		Code.put(Code.enter);
//		Code.put(MethodName.obj.getLevel());
//		Code.put(MethodName.obj.getLocalSymbols().size());
//		
//	}
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
//}
