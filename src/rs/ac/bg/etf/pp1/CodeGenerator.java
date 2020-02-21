package rs.ac.bg.etf.pp1;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;

public class CodeGenerator extends VisitorAdaptor {
	
	Logger log = Logger.getLogger(CodeGenerator.class);
	
	private int mainPc;
	
	private int nVars = 0;
	
	private MyDumpSymbolTableVisitor stv = new MyDumpSymbolTableVisitor ();
	
	private List<List<Integer>> conditionalAndForJumps = new ArrayList<List<Integer>> (); // lista koja sluzi za skakanje na naredbe u if-else i for petlji
	
	private List<List<Integer>> breakStatements = new ArrayList<List<Integer>> ();
	
	private List<List<Integer>> continueStatements = new ArrayList<List<Integer>> ();
	
	private List<Integer> condFactJumpAdrs = new ArrayList<Integer> ();
	
	private List<Integer> condTermJumpAdrs = new ArrayList<Integer> ();
	
	private List<Integer> conditionJumpAdrs = new ArrayList<Integer> ();
	
	private List<List<Integer>> rightAssociationOperators = new ArrayList<List<Integer>> ();
	
	private List<List<Obj>> rightAssociationObjects = new ArrayList <List<Obj>> ();
	
	private Obj foreachIdent;
	
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
	
	/* global variables */
	
	public void visit(SingleVarDeclSuccess SingleVarDeclSuccess) {
		
		this.nVars++;
	}
	
	/* print and read statements */
	
	public void visit (ReadStatement ReadStatement) {
		
		if (ReadStatement.getDesignator().obj.getKind() == Obj.Elem) {
			
			Code.put(Code.dup_x1);
			Code.put(Code.pop);
			Code.put(Code.pop);
			
		}
		
		if (ReadStatement.getDesignator().obj.getType() == Tab.intType) {
			
			Code.put(Code.read);
			Code.store (ReadStatement.getDesignator().obj);
			
		}
		else if (ReadStatement.getDesignator().obj.getType() == Tab.charType) {
			
			Code.put(Code.bread);
			Code.store (ReadStatement.getDesignator().obj);
		
		}
		
		else if (ReadStatement.getDesignator().obj.getType() == MyTabImpl.boolType) {
			
			Code.put(Code.bread);
			Code.put(Code.dup);
			Code.loadConst(116);
			Code.putFalseJump(Code.eq, 0);
			
			int adr1 = Code.pc - 2;
			
			Code.put(Code.pop);
			Code.put(Code.bread);			
			Code.loadConst(114);
			Code.putFalseJump(Code.eq, 0);
			
			int adr2 = Code.pc - 2;
			
			Code.put(Code.bread);			
			Code.loadConst(117);
			Code.putFalseJump(Code.eq, 0);
			
			int adr3 = Code.pc - 2;
			
			Code.put(Code.bread);			
			Code.loadConst(101);
			Code.putFalseJump(Code.eq, 0);
			
			int adr4 = Code.pc - 2;
			
			Code.loadConst(1);
			Code.store (ReadStatement.getDesignator().obj);
			Code.putJump(0);
			
			int adr5 = Code.pc - 2;
			
			Code.fixup(adr1); Code.fixup(adr2); Code.fixup(adr3); Code.fixup(adr4);
			
			Code.loadConst(102);
			Code.putFalseJump(Code.eq, 0);
			
			int adr6 = Code.pc - 2;
			
			Code.put(Code.bread);			
			Code.loadConst(97);
			Code.putFalseJump(Code.eq, 0);
			
			int adr7 = Code.pc - 2;
			
			Code.put(Code.bread);			
			Code.loadConst(108);
			Code.putFalseJump(Code.eq, 0);
			
			int adr8 = Code.pc - 2;
			
			Code.put(Code.bread);			
			Code.loadConst(115);
			Code.putFalseJump(Code.eq, 0);
			
			int adr9 = Code.pc - 2;
			
			Code.put(Code.bread);			
			Code.loadConst(101);
			Code.putFalseJump(Code.eq, 0);
			
			int adr10 = Code.pc - 2;
			
			Code.loadConst(0);
			Code.store (ReadStatement.getDesignator().obj);
			
			Code.fixup(adr6); Code.fixup(adr7); Code.fixup(adr8); Code.fixup(adr9); Code.fixup(adr10);
			
			/*Code.loadConst(5); // da kazemo da je to I/O error
			Code.put(Code.trap); */ 
			
			Code.fixup(adr5); //Code.fixup(adr11);
			
		}
		
		if (ReadStatement.getDesignator().obj.getKind() != Obj.Elem) 
			Code.put(Code.pop);
		
	}

	public void visit (PrintStatement PrintStatement) {
		
		List<Integer> operators = rightAssociationOperators.get(rightAssociationOperators.size() - 1);
		List<Obj> designators = rightAssociationObjects.get(rightAssociationObjects.size() - 1);
		
		while (!designators.isEmpty()) {
			
			Code.put(operators.get(operators.size() - 1));
			
			if (designators.get(designators.size() - 1).getKind() == Obj.Elem)
				Code.put(Code.dup_x2);
			else
				Code.put(Code.dup);
			Code.store(designators.get(designators.size() - 1));
			
			operators.remove(operators.size() - 1);
			designators.remove(designators.size() - 1);
			
		}
		
		rightAssociationObjects.remove(rightAssociationObjects.size() - 1);
		rightAssociationOperators.remove(rightAssociationOperators.size() - 1);
		
		if (PrintStatement.getPrintOption() instanceof NoPrintArg)
			Code.loadConst(5);
		
		if (PrintStatement.getExpr().obj.getType() == Tab.intType) Code.put(Code.print);
		
		else if (PrintStatement.getExpr().obj.getType() == Tab.charType) Code.put(Code.bprint);
		
		else if (PrintStatement.getExpr().obj.getType() == MyTabImpl.boolType) {
			
			int printArg = Code.get(Code.pc - 1);
			Code.put(Code.pop);
			Code.loadConst(1);
			Code.putFalseJump(Code.eq, 0);
			
			int adr1 = Code.pc - 2;
			
			Code.loadConst(116);
			Code.put(printArg);
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
			
			Code.loadConst(102);
			Code.put(printArg);
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
	
	public void visit (PrintArg PrintArg) {
		
		Code.loadConst(PrintArg.getN1());
		
	}
	
	/* combined operators starters */
	
	public void visit (Destination Destination) {
		
		rightAssociationObjects.add(new ArrayList<Obj> ());
		rightAssociationOperators.add(new ArrayList<Integer> ());
		
	}
	
	public void visit (ActPar ActPar) {
		
		log.info("ActPar");
		log.info(ActPar.getExpr().obj != null ? ActPar.getExpr().obj.getKind() : "obj je null");
		log.info(ActPar.getExpr().obj != null ? ActPar.getExpr().obj.getName() : "obj je null");
		
		List<Integer> operators = rightAssociationOperators.get(rightAssociationOperators.size() - 1);
		List<Obj> designators = rightAssociationObjects.get(rightAssociationObjects.size() - 1);
		
		while (!designators.isEmpty()) {
			
			Code.put(operators.get(operators.size() - 1));
			
			if (designators.get(designators.size() - 1).getKind() == Obj.Elem)
				Code.put(Code.dup_x2);
			else
				Code.put(Code.dup);
			Code.store(designators.get(designators.size() - 1));
			
			operators.remove(operators.size() - 1);
			designators.remove(designators.size() - 1);
			
		}
		
		rightAssociationObjects.remove(rightAssociationObjects.size() - 1);
		rightAssociationOperators.remove(rightAssociationOperators.size() - 1);
		
		rightAssociationObjects.add(new ArrayList<Obj> ());
		rightAssociationOperators.add(new ArrayList<Integer> ());
		
	}
	
	public void visit (LeftBracketExpr LeftBracketExpr) {
    	
		rightAssociationObjects.add(new ArrayList<Obj> ());
		rightAssociationOperators.add(new ArrayList<Integer> ());
    	
    }
    
    public void visit (LeftParenthesisExpr LeftParenthesisExpr) {
    	
    	rightAssociationObjects.add(new ArrayList<Obj> ());
		rightAssociationOperators.add(new ArrayList<Integer> ());
    	
    }
    
    public void visit (Return Return) {
    	
    	rightAssociationObjects.add(new ArrayList<Obj> ());
		rightAssociationOperators.add(new ArrayList<Integer> ());
    	
    }
    
    public void visit (FirstExpr FirstExpr) {
    	
    	List<Integer> operators = rightAssociationOperators.get(rightAssociationOperators.size() - 1);
		List<Obj> designators = rightAssociationObjects.get(rightAssociationObjects.size() - 1);
		
		while (!designators.isEmpty()) {
			
			Code.put(operators.get(operators.size() - 1));
			
			if (designators.get(designators.size() - 1).getKind() == Obj.Elem)
				Code.put(Code.dup_x2);
			else
				Code.put(Code.dup);
			Code.store(designators.get(designators.size() - 1));
			
			operators.remove(operators.size() - 1);
			designators.remove(designators.size() - 1);
			
		}
		
		rightAssociationObjects.remove(rightAssociationObjects.size() - 1);
		rightAssociationOperators.remove(rightAssociationOperators.size() - 1);
    			
    	rightAssociationObjects.add(new ArrayList<Obj> ());
		rightAssociationOperators.add(new ArrayList<Integer> ());
    	
    }
    
    public void visit (SecondExpr SecondExpr) {
    	
    	List<Integer> operators = rightAssociationOperators.get(rightAssociationOperators.size() - 1);
		List<Obj> designators = rightAssociationObjects.get(rightAssociationObjects.size() - 1);
		
		while (!designators.isEmpty()) {
			
			Code.put(operators.get(operators.size() - 1));
			
			if (designators.get(designators.size() - 1).getKind() == Obj.Elem)
				Code.put(Code.dup_x2);
			else
				Code.put(Code.dup);
			Code.store(designators.get(designators.size() - 1));
			
			operators.remove(operators.size() - 1);
			designators.remove(designators.size() - 1);
			
		}
		
		rightAssociationObjects.remove(rightAssociationObjects.size() - 1);
		rightAssociationOperators.remove(rightAssociationOperators.size() - 1);			
    	
    }
    
    public void visit (Source Source) {
    	
    	List<Integer> operators = rightAssociationOperators.get(rightAssociationOperators.size() - 1);
		List<Obj> designators = rightAssociationObjects.get(rightAssociationObjects.size() - 1);
		
		while (!designators.isEmpty()) {
			
			Code.put(operators.get(operators.size() - 1));
			
			if (designators.get(designators.size() - 1).getKind() == Obj.Elem)
				Code.put(Code.dup_x2);
			else
				Code.put(Code.dup);
			Code.store(designators.get(designators.size() - 1));
			
			operators.remove(operators.size() - 1);
			designators.remove(designators.size() - 1);
			
		}
		
		rightAssociationObjects.remove(rightAssociationObjects.size() - 1);
		rightAssociationOperators.remove(rightAssociationOperators.size() - 1);
    	
    }
    
    public void visit (CompositeFactor CompositeFactor) {
    	
    	List<Integer> operators = rightAssociationOperators.get(rightAssociationOperators.size() - 1);
		List<Obj> designators = rightAssociationObjects.get(rightAssociationObjects.size() - 1);
		
		while (!designators.isEmpty()) {
			
			Code.put(operators.get(operators.size() - 1));
			
			if (designators.get(designators.size() - 1).getKind() == Obj.Elem)
				Code.put(Code.dup_x2);
			else
				Code.put(Code.dup);
			Code.store(designators.get(designators.size() - 1));
			
			operators.remove(operators.size() - 1);
			designators.remove(designators.size() - 1);
			
		}
		
		rightAssociationObjects.remove(rightAssociationObjects.size() - 1);
		rightAssociationOperators.remove(rightAssociationOperators.size() - 1);
    			
    }
	
	/* assignment, increment and decrement statements */
	
	public void visit (AssignStatementSuccess AssignStatementSuccess) {
		
		if (AssignStatementSuccess.getAssignop() instanceof Assign) {
			
			log.info("Assign");
			
			if (AssignStatementSuccess.getDestination().obj.getKind() == Obj.Elem) {
				
				Code.put(Code.dup_x1);
				Code.put(Code.pop);
				Code.put(Code.pop);
				
			}
			
					
			Code.store (AssignStatementSuccess.getDestination().obj);
			
			log.info(AssignStatementSuccess.getDestination().obj.getKind());
			
			if (AssignStatementSuccess.getDestination().obj.getKind() != Obj.Elem)
				Code.put(Code.pop);
		
		}
		
		else if (AssignStatementSuccess.getAssignop() instanceof AddopRightAssign) {
			
			log.info("AddopRightAssign");
			//Code.store (AssignStatementSuccess.getDestination().obj);
			
			AddopRightAssign addop = (AddopRightAssign) AssignStatementSuccess.getAssignop();
			
			//Code.load(AssignStatementSuccess.getDestination().obj);
			
			//Code.put(Code.dup_x1); Code.put(Code.pop);
			
			if (addop.getAddopRight() instanceof PlusAssign) {
				
				Code.put(Code.add);
				Code.store (AssignStatementSuccess.getDestination().obj);
				
			}
			
			else if (addop.getAddopRight() instanceof MinusAssign) {
				
				Code.put(Code.sub);
				Code.store (AssignStatementSuccess.getDestination().obj);
				
			}
			
		}
		else if (AssignStatementSuccess.getAssignop() instanceof MulopRightAssign) {
			
			log.info("MulopRightAssign");
			
			MulopRightAssign mulop = (MulopRightAssign) AssignStatementSuccess.getAssignop();
			
			//Code.load(AssignStatementSuccess.getDestination().obj);
			
			//Code.put(Code.dup_x1); Code.put(Code.pop);
			
			if (mulop.getMulopRight() instanceof MulAssign) {
				
				Code.put(Code.mul);
				Code.store (AssignStatementSuccess.getDestination().obj);
				
			}
			
			else if (mulop.getMulopRight() instanceof DivAssign) {
				
				Code.put(Code.div);
				Code.store (AssignStatementSuccess.getDestination().obj);
				
			}
			
			else if (mulop.getMulopRight() instanceof ModAssign) {
				
				Code.put(Code.rem);
				Code.store (AssignStatementSuccess.getDestination().obj);
				
			}
			
		}
		
		
		
	}
	
	public void visit (IncrementStatement IncrementStatement) {
		
		Code.load(IncrementStatement.getDestination().obj);
		Code.loadConst(1);
		Code.put(Code.add);
		Code.store (IncrementStatement.getDestination().obj);
		
	}
	
	public void visit (DecrementStatement DecrementStatement) {
		
		Code.load(DecrementStatement.getDestination().obj);
		Code.loadConst(1);
		Code.put(Code.sub);
		Code.store (DecrementStatement.getDestination().obj);
		
	}
	
	/* new */
	
	public void visit (NewArrayFactor NewArrayFactor) {
		
		List<Integer> operators = rightAssociationOperators.get(rightAssociationOperators.size() - 1);
		List<Obj> designators = rightAssociationObjects.get(rightAssociationObjects.size() - 1);
		
		while (!designators.isEmpty()) {
			
			Code.put(operators.get(operators.size() - 1));
			
			if (designators.get(designators.size() - 1).getKind() == Obj.Elem)
				Code.put(Code.dup_x2);
			else
				Code.put(Code.dup);
			Code.store(designators.get(designators.size() - 1));
			
			operators.remove(operators.size() - 1);
			designators.remove(designators.size() - 1);
			
		}
		
		rightAssociationObjects.remove(rightAssociationObjects.size() - 1);
		rightAssociationOperators.remove(rightAssociationOperators.size() - 1);
    	
    	Code.put(Code.newarray);
    	Code.put(NewArrayFactor.getType().struct == Tab.charType ? 0 : 1);
    
    }
	
	/* method call */
	
	public void visit (MethodDesignator MethodDesignator) {
		
		if (!(MethodDesignator.getDesignator().obj.getName().equals("ord")
				|| MethodDesignator.getDesignator().obj.getName().equals("chr")
				|| MethodDesignator.getDesignator().obj.getName().equals("len") )) {
		
			int destAdr = MethodDesignator.getDesignator().obj.getAdr() - Code.pc;
			Code.put(Code.call);
			Code.put2(destAdr);
			
		}
		
		else {
			
			if (MethodDesignator.getDesignator().obj.getName().equals("len")) {
				
				Code.put (Code.arraylength);
				
			}
		}
	
	}
	
	public void visit (MethodCallStatement MethodCallStatement) {
		
		if (!(MethodCallStatement.getDestination().obj.getName().equals("ord")
				|| MethodCallStatement.getDestination().obj.getName().equals("chr")
				|| MethodCallStatement.getDestination().obj.getName().equals("len") )) {
		
			int destAdr = MethodCallStatement.getDestination().obj.getAdr() - Code.pc;
			Code.put(Code.call);
			Code.put2(destAdr);
			
		}
		
		else {
			
			if ( MethodCallStatement.getDestination().obj.getName().equals("len")) {
				
				Code.put (Code.arraylength);
				
			}
		}
		
	}
	
	/* arithmetic operations */
	
	public void visit (MultipleFactorTerm MultipleFactorTerm) {
		
		if (MultipleFactorTerm.getMulopLeft() instanceof Mul) {
			
			Code.put(Code.mul);
			log.info("Mul");
			
		}
		
		else if (MultipleFactorTerm.getMulopLeft() instanceof Div) {
			
			Code.put(Code.div);
			log.info("Div");
			
		}
		
		else if (MultipleFactorTerm.getMulopLeft() instanceof Mod) {
			
			Code.put(Code.rem);
			log.info("Mod");
			
		}
		
	}
	
	public void visit (MultipleFactorTermAssign MultipleFactorTermAssign) {
		
		if (MultipleFactorTermAssign.getMulopRight() instanceof MulAssign) {
			
			//Code.put(Code.mul);
			log.info("MulAssign");
			
			rightAssociationOperators.get(rightAssociationOperators.size() - 1).add(Code.mul);
			rightAssociationObjects.get(rightAssociationObjects.size() - 1).add(MultipleFactorTermAssign.obj);
			
		}
		
		else if (MultipleFactorTermAssign.getMulopRight() instanceof DivAssign) {
			
			//Code.put(Code.div);
			log.info("DivAssign");
			
			rightAssociationOperators.get(rightAssociationOperators.size() - 1).add(Code.div);
			rightAssociationObjects.get(rightAssociationObjects.size() - 1).add(MultipleFactorTermAssign.obj);
			
		}
		
		else if (MultipleFactorTermAssign.getMulopRight() instanceof ModAssign) {
			
			//Code.put(Code.rem);
			log.info("ModAssign");
			
			rightAssociationOperators.get(rightAssociationOperators.size() - 1).add(Code.rem);
			rightAssociationObjects.get(rightAssociationObjects.size() - 1).add(MultipleFactorTermAssign.obj);
			
		}
		
	}
	
	public void visit (MultipleTermExpr MultipleTermExpr) {
		
		if (MultipleTermExpr.getAddopLeft() instanceof Plus) {
			
			Code.put(Code.add);
			log.info("Plus");
			
		}
		
		else if (MultipleTermExpr.getAddopLeft() instanceof Minus) {
			
			Code.put(Code.sub);
			log.info("Minus");
			
		}
		
	}
	
	public void visit (MultipleTermExprAssign MultipleTermExprAssign) {
		
		if (MultipleTermExprAssign.getAddopRight() instanceof PlusAssign) {
			
			//Code.put(Code.add);
			log.info("PlusAssign");
			
			rightAssociationOperators.get(rightAssociationOperators.size() - 1).add(Code.add);
			rightAssociationObjects.get(rightAssociationObjects.size() - 1).add(MultipleTermExprAssign.obj);
			
		}
		
		else if (MultipleTermExprAssign.getAddopRight() instanceof MinusAssign) {
			
			//Code.put(Code.sub);
			log.info("MinusAssign");
			
			rightAssociationOperators.get(rightAssociationOperators.size() - 1).add(Code.sub);
			rightAssociationObjects.get(rightAssociationObjects.size() - 1).add(MultipleTermExprAssign.obj);
			
		}
		
	}
	
	public void visit (MinusSingleTermExpr MinusSingleTermExpr) {
		
		Code.put(Code.neg);
		
	}
	
	/* numeric, char and boolean constants */
	
	public void visit (NumFactor NumFactor) {
		
		Code.loadConst(NumFactor.getNumberValue());
		
	}
	
	public void visit (CharFactor CharFactor) {
		
		Code.loadConst(Integer.valueOf(CharFactor.getCharValue()));
		
	}
	
	public void visit (BoolFactor BoolFactor) {
		
		if (BoolFactor.getBoolValue()) {
			
			//log.info("konstanta tru");
			Code.loadConst(1);
		}
		
		else {
			
			//log.info("konstanta false");
			Code.loadConst(0);
		}
	
	}
	
	/* designators */
	
	public void visit (SimpleDesignator SimpleDesignator) {
		
		log.info("SimpleDesignator");
		log.info(SimpleDesignator.obj != null ? SimpleDesignator.obj.getName() : "obj je null");
		
		/*SyntaxNode parent = SimpleDesignator.getParent();
		
		while (parent != null) {
			
			//log.info(parent.getClass());
			//if (parent instanceof ReadStatement || parent instanceof Destination) {
				
				//log.info("should return");
				//return;
			//}
			if (parent instanceof ArrayDesignator && parent.getParent() != null && parent.getParent() instanceof Destination) {
				
				//log.info("should break");
				break;
			}
			parent = parent.getParent();
			
		}*/
    	
		if (SimpleDesignator.obj.getKind() == Obj.Elem ||
				SimpleDesignator.obj.getKind() == Obj.Var ||
				SimpleDesignator.obj.getKind() == Obj.Fld)
		Code.load (SimpleDesignator.obj);
    	
    }
	
	public void visit (ArrayDesignator ArrayDesignator) {
		
		List<Integer> operators = rightAssociationOperators.get(rightAssociationOperators.size() - 1);
		List<Obj> designators = rightAssociationObjects.get(rightAssociationObjects.size() - 1);
		
		while (!designators.isEmpty()) {
			
			Code.put(operators.get(operators.size() - 1));
			
			if (designators.get(designators.size() - 1).getKind() == Obj.Elem)
				Code.put(Code.dup_x2);
			else
				Code.put(Code.dup);
			Code.store(designators.get(designators.size() - 1));
			
			operators.remove(operators.size() - 1);
			designators.remove(designators.size() - 1);
			
		}
		
		rightAssociationObjects.remove(rightAssociationObjects.size() - 1);
		rightAssociationOperators.remove(rightAssociationOperators.size() - 1);
		
		SyntaxNode parent = ArrayDesignator.getParent();
		
		SyntaxNode child = ArrayDesignator;
		
		while (parent != null) {
			
			log.info(parent.getClass());
			if (parent instanceof ReadStatement || parent instanceof Destination 
					|| parent instanceof MultipleFactorTermAssign || parent instanceof MultipleTermExprAssign) {
				
				if (parent instanceof MultipleFactorTermAssign || parent instanceof MultipleTermExprAssign) {
					
					if (parent instanceof MultipleFactorTermAssign && child instanceof DeclDesignator) {
						
						log.info("ISPUNJENO: parent MultipleFactorTermAssign child DeclDesignator");
						break;
						
					}
					
					else if (parent instanceof MultipleTermExprAssign && 
							(child instanceof SingleFactorTerm || child instanceof MultipleFactorTerm)) {
						
						log.info("ISPUNJENO: parent MultipleTermExprAssign child SingleFactorTerm | MultipleFactorTerm");
						break;
						
					}
					
					
										
				}	
				
				Code.put(Code.dup2);
				break;
				
			};
			
			child = parent;
			parent = parent.getParent();
			
		}
		
		log.info("ArrayDesignator");
		log.info(ArrayDesignator.obj != null ? ArrayDesignator.obj.getName() : "obj je null");
    	
		Code.load (ArrayDesignator.obj);
    	
    }
	
	public void visit (DeclDesignator DeclDesignator) {
		
		log.info("DeclDesignator");
		
		if (DeclDesignator.getDesignator() instanceof ArrayDesignator) {
			
			SyntaxNode parent = DeclDesignator.getParent();
			
			while (parent != null) {
				
				if (parent instanceof Source || parent instanceof PrintStatement || parent instanceof ReadStatement || parent instanceof ActPar) return;
				
				parent = parent.getParent();
				
			}
			
			log.info("DeclDesignator");
			log.info(DeclDesignator.obj != null ? DeclDesignator.obj.getName() : "obj je null");
			
			if (DeclDesignator.obj.getType() == Tab.charType) Code.put(Code.baload);
			else
				Code.put(Code.aload);
			
		}
	}
	
	/* main and other methods*/
	
	public void visit (MethodName MethodName) {
		
		if("main".equalsIgnoreCase(MethodName.getMethodName())){
			mainPc = Code.pc;
		}
		MethodName.obj.setAdr(Code.pc);
		
		Code.put(Code.enter);
		Code.put(MethodName.obj.getLevel());
		Code.put(MethodName.obj.getLocalSymbols().size());
		
	}
	
	public void visit (ReturnStatement ReturnStatement) {
		
		List<Integer> operators = rightAssociationOperators.get(rightAssociationOperators.size() - 1);
		List<Obj> designators = rightAssociationObjects.get(rightAssociationObjects.size() - 1);
		
		while (!designators.isEmpty()) {
			
			Code.put(operators.get(operators.size() - 1));
			
			if (designators.get(designators.size() - 1).getKind() == Obj.Elem)
				Code.put(Code.dup_x2);
			else
				Code.put(Code.dup);
			Code.store(designators.get(designators.size() - 1));
			
			operators.remove(operators.size() - 1);
			designators.remove(designators.size() - 1);
			
		}
		
		rightAssociationObjects.remove(rightAssociationObjects.size() - 1);
		rightAssociationOperators.remove(rightAssociationOperators.size() - 1);
		
		Code.put(Code.exit); Code.put(Code.return_);
		
	}
	
	public void visit (MethodDeclSuccess MethodDeclSuccess) {
		
		if (MethodDeclSuccess.getMethodName().obj.getType() == Tab.noType) {
			
			Code.put(Code.exit); Code.put(Code.return_);
			
		}
		
		else {
			
			Code.put(Code.trap); Code.put(1);
			
		}
			
	}
	
	/* if and for statements */
	
	public void visit (For For) {
		
		log.info("For");
		conditionalAndForJumps.add(new ArrayList<Integer> ());
		breakStatements.add(new ArrayList<Integer> ());
		continueStatements.add(new ArrayList<Integer> ());
		
	}
	
	public void visit (FirstForDesignatorStatement FirstForDesignatorStatement) {
		
		log.info("FirstForDesignatorStatement");
		conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).add(Code.pc); // adresa uslova
		
	}
	
	public void visit (NoFirstForDesignatorStatement NoFirstForDesignatorStatement) {
		
		log.info("NoFirstForDesignatorStatement");
		conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).add(Code.pc); // adresa uslova
		
	}
	
	public void visit (NoForCondition NoForCondition) {
		
		log.info("NoForCondition");
		
		// ubacuje true
		Code.loadConst(1);
		Code.loadConst(1);
		Code.putFalseJump(Code.eq, 0);
		conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).add(Code.pc - 2); // skok izvan tela kad nije ispunjen uslov
		
		Code.putJump(0);
		conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).add(Code.pc - 2); // fixup za skok na petlju
		conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).add(Code.pc); // adresa poslednje naredbe
		
		continueStatements.get(continueStatements.size() - 1).add(Code.pc);
		
	}
	
	public void visit (ForCondition ForCondition) {
		
		log.info("ForCondition");
				
		Code.putJump(0);
		conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).add(Code.pc - 2); // fixup za skok na petlju
		conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).add(Code.pc); // adresa poslednje naredbe
		
		continueStatements.get(continueStatements.size() - 1).add(Code.pc);
		
	}
	
	public void visit (SecondForDesignatorStatement SecondForDesignatorStatement) {
		
		log.info("SecondForDesignatorStatement");
		Code.putJump(conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).get(0)); // skok na uslov (prvi element liste)
		Code.fixup(conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).get(2)); // ovde skace na telo petlje
		
		log.info(conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).size());
	
	}
	
	public void visit (NoSecondForDesignatorStatement NoSecondForDesignatorStatement) {
		
		log.info("NoSecondForDesignatorStatement");
		Code.putJump(conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).get(0)); // skok na uslov (prvi element liste)
		Code.fixup(conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).get(2)); // ovde skace na telo petlje
		
		log.info(conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).size());
	
	}
	
	public void visit (BreakStatement BreakStatement) {
		
		log.info("BreakStatement");
		Code.putJump(0);
		breakStatements.get(breakStatements.size() - 1).add(Code.pc - 2);
		
	}
	
	public void visit (ContinueStatement ContinueStatement) {
		
		log.info("ContinueStatement");
		Code.putJump(continueStatements.get(continueStatements.size() - 1).get(0)); // skok na zadnju naredbu
		//continueStatements.get(continueStatements.size() - 1).add(Code.pc - 2);
		
	}
	
	public void visit (UnmatchedForStatement UnmatchedForStatement) {
		
		log.info("UnmatchedForStatement");
		log.info(conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).size());
		for (Integer i : conditionalAndForJumps.get(conditionalAndForJumps.size() - 1))
			log.info(i);
		Code.putJump(conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).get(3)); // skok na zadnju naredbu
		Code.fixup(conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).get(1)); // zavrsetak petlje
		
		for (Integer adr : breakStatements.get(breakStatements.size() - 1))
			Code.fixup (adr);
		
		breakStatements.remove(breakStatements.size() - 1);
		
		continueStatements.remove(continueStatements.size() - 1);
		
		conditionalAndForJumps.remove(conditionalAndForJumps.size() - 1);
		
	}
	
	public void visit (MatchedForStatement MatchedForStatement) {
		
		log.info("MatchedForStatement");
		log.info(conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).size());
		for (Integer i : conditionalAndForJumps.get(conditionalAndForJumps.size() - 1))
			log.info(i);
		Code.putJump(conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).get(3)); // skok na zadnju naredbu
		Code.fixup(conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).get(1)); // zavrsetak petlje
				
		for (Integer adr : breakStatements.get(breakStatements.size() - 1))
			Code.fixup (adr);
		
		breakStatements.remove(breakStatements.size() - 1);
		
		continueStatements.remove(continueStatements.size() - 1);
		
		conditionalAndForJumps.remove(conditionalAndForJumps.size() - 1);
		
	}
	
	public void visit (Foreach Foreach) {
		
		log.info("Foreach");
		conditionalAndForJumps.add(new ArrayList<Integer> ());
		breakStatements.add(new ArrayList<Integer> ());
		continueStatements.add(new ArrayList<Integer> ());
		
	}
	
	public void visit (IteratorName IteratorName) {
		
		log.info(IteratorName.obj.getName());
		
		foreachIdent = IteratorName.obj;
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see rs.ac.bg.etf.pp1.ast.VisitorAdaptor#visit(ForeachArray)
	 * 
	 * Ideja: ovde je ucitan designator niza; ucitamo -1 (ako to moze), pamtimo adresu i odatle krece iteriranje
	 * ucitavamo 1, dodamo na trenutni index sacuvan:  expr stack: niz, 0
	 * radimo dup_x1 i pop da im obrnemo redosled, pa dup2, da ih dupliramo: expr stack: 0, niz, 0, niz (imamo sacuvan index i adresu niza)
	 * arraylength za proveru uslova: expr stack: 0, niz, 0, len(niz)
	 * skok ako je index veci ili jednak duzini niza: expr stack: 0, niz
	 * opet im obrcemo redosled (dup_x1 + pop) i duplamo: tako imamo zapamcen niz, trenutni index, i mozemo da ucitamo element; expr stack: niz, 0, niz, 0
	 * ucitavamo element: expr stack: niz, 0, niz[0]
	 */
	public void visit (ForeachArray ForeachArray) {
		
		Code.loadConst(-1);
		
		conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).add(Code.pc); // adresa provere uslova iteracije
		continueStatements.get(continueStatements.size() - 1).add(Code.pc); // ovde ce skakati za continue
		
		Code.loadConst(1);
		Code.put(Code.add);
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
		Code.put(Code.dup2);
		Code.put(Code.arraylength);
		
		Code.putFalseJump(Code.lt, 0);
		
		conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).add(Code.pc - 2); // adresa za fixup za izlaz iz petlje
		
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
		Code.put(Code.dup2);
		
		log.info(foreachIdent.getName());
		
		if (foreachIdent.getType() == MyTabImpl.charType)
			Code.put(Code.baload);
		else
			Code.put(Code.aload);
		
		Code.store(foreachIdent);
		
		conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).add(Code.pc); // adresa tela petlje
		
	}
	
	public void visit (MatchedForEachStatement MatchedForEachStatement) {
		
		Code.putJump(conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).get(0)); // skok na uslov
		
		Code.fixup(conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).get(1)); // izlaz
		
		for (Integer adr : breakStatements.get(breakStatements.size() - 1)) // izlaz za break
			Code.fixup (adr);
		
		Code.put(Code.pop);
		Code.put(Code.pop); // skida sa expr steka
		
		breakStatements.remove(breakStatements.size() - 1);
		
		continueStatements.remove(continueStatements.size() - 1);
		
		conditionalAndForJumps.remove(conditionalAndForJumps.size() - 1);
		
	}
	
	public void visit (UnmatchedForEachStatement UnmatchedForEachStatement) {
		
		Code.putJump(conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).get(0)); // skok na uslov
		
		Code.fixup(conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).get(1)); // izlaz
		
		for (Integer adr : breakStatements.get(breakStatements.size() - 1)) // izlaz za break
			Code.fixup (adr);
		
		Code.put(Code.pop);
		Code.put(Code.pop); // skida sa expr steka
		
		breakStatements.remove(breakStatements.size() - 1);
		
		continueStatements.remove(continueStatements.size() - 1);
		
		conditionalAndForJumps.remove(conditionalAndForJumps.size() - 1);
		
	}
	
	public void visit (If If) {
		
		log.info("If");
		conditionalAndForJumps.add(new ArrayList<Integer> ());
		
	}
	
	public void visit (Else Else) {
		
		Code.putJump(0);
		
		for (Integer adr : conditionalAndForJumps.get(conditionalAndForJumps.size() - 1)) {
			
			Code.fixup(adr);
			
		}
		
		conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).clear();
		
		conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).add(Code.pc - 2);
		
	}
	
	public void visit (MatchedIfStatement MatchedIfStatement) {
		
		for (Integer adr : conditionalAndForJumps.get(conditionalAndForJumps.size() - 1)) {
			
			Code.fixup(adr);
			
		}
		
		conditionalAndForJumps.remove(conditionalAndForJumps.size() - 1);
		
	}
	
	public void visit (UnmatchedIfStatement UnmatchedIfStatement) {
		
		log.info("UnmatchedIfStatement");
		
		log.info(Code.pc);
		
		for (Integer adr : conditionalAndForJumps.get(conditionalAndForJumps.size() - 1)) {
			
			Code.fixup(adr);
			
		}
		
		conditionalAndForJumps.remove(conditionalAndForJumps.size() - 1);
		
	}
	
	/* conditions */
	
	/* prva iteracija: samo da zveknemo normalan skok pa da vidimo sta cemo */
	
	public void visit (SingleExprFact SingleExprFact) {
		
		log.info("SingleExprFact");
		
		List<Integer> operators = rightAssociationOperators.get(rightAssociationOperators.size() - 1);
		List<Obj> designators = rightAssociationObjects.get(rightAssociationObjects.size() - 1);
		
		while (!designators.isEmpty()) {
			
			Code.put(operators.get(operators.size() - 1));
			
			if (designators.get(designators.size() - 1).getKind() == Obj.Elem)
				Code.put(Code.dup_x2);
			else
				Code.put(Code.dup);
			Code.store(designators.get(designators.size() - 1));
			
			operators.remove(operators.size() - 1);
			designators.remove(designators.size() - 1);
			
		}
		
		rightAssociationObjects.remove(rightAssociationObjects.size() - 1);
		rightAssociationOperators.remove(rightAssociationOperators.size() - 1);
		
		SyntaxNode parent = SingleExprFact.getParent();
		
		boolean singleExpressionCondition = false, onlyAndExpression = false, onlyOrExpression = false;
		
		while (parent != null) {
			
			log.info(parent.getClass());
			parent = parent.getParent();
			
			//if (parent instanceof Statement) break;
			
		}
		
		Code.loadConst(1);
		Code.putFalseJump(Code.eq, 0);
		//conditionalJumpAddressesList.add(Code.pc - 2);
		condFactJumpAdrs.add(Code.pc - 2);
		
		parent = SingleExprFact.getParent();
		
		while (parent != null) {
			
			//log.info(parent.getClass());
			//parent = parent.getParent();
			
			if (parent instanceof SingleFactTerm 
					&& parent.getParent() instanceof SingleTermCondition
					&& parent.getParent().getParent() instanceof ConditionSuccess) {
				
				/*log.info("SINGLE EXPRESSION CONDITION");
				conditionalJumpAddressesList.add(Code.pc - 2);
				return; */
				
				singleExpressionCondition = true;
				break;
				
			}
			
			if (parent instanceof MultipleFactTerm) {
				
				parent = parent.getParent();
				
				while (parent != null) {
					
					if (parent instanceof SingleTermCondition && parent.getParent() instanceof ConditionSuccess ) {
						
						onlyAndExpression = true;
						break;
						
					}
					
					parent = parent.getParent();
					
				}
				
				if (onlyAndExpression) break;
				
			}
			
			if (parent != null) parent = parent.getParent();
			
		}
		
		if (singleExpressionCondition) {
			
			log.info("SINGLE EXPRESSION CONDITION");
			//Code.putFalseJump(Code.eq, 0);
			//conditionalUnsuccessfulJumpToStatements.add(Code.pc - 2);
			
		}
		
		else if (onlyAndExpression) {
			
			log.info("ONLY AND EXPRESSION CONDITION");
			//Code.putFalseJump(Code.eq, 0);
			//conditionalUnsuccessfulJumpToStatements.add(Code.pc - 2);
			
		}
		
	}
	
	public void visit (MultipleExprFact MultipleExprFact) {
		
		log.info("MultipleExprFact");
		
		SyntaxNode parent = MultipleExprFact.getParent();
		
		boolean singleExpressionCondition = false, onlyAndExpression = false;
		
		while (parent != null) {
			
			log.info(parent.getClass());
			parent = parent.getParent();
			
			//if (parent instanceof Statement) break;
			
		}
		
		if (MultipleExprFact.getRelop() instanceof Equals) {
			
			Code.putFalseJump(Code.eq, 0);
			//conditionalJumpAddressesList.add(Code.pc - 2);
			
		}
		
		else if (MultipleExprFact.getRelop() instanceof NotEquals) {
			
			Code.putFalseJump(Code.ne, 0);
			//conditionalJumpAddressesList.add(Code.pc - 2);
			
		}
		
		else if (MultipleExprFact.getRelop() instanceof GreaterThan) {
			
			Code.putFalseJump(Code.gt, 0);
			//conditionalJumpAddressesList.add(Code.pc - 2);
			
		}
		
		else if (MultipleExprFact.getRelop() instanceof GreaterThanEquals) {
			
			Code.putFalseJump(Code.ge, 0);
			//conditionalJumpAddressesList.add(Code.pc - 2);
			
		}
		
		else if (MultipleExprFact.getRelop() instanceof LessThan) {
			
			Code.putFalseJump(Code.lt, 0);
			//conditionalJumpAddressesList.add(Code.pc - 2);
			
		}
		
		else if (MultipleExprFact.getRelop() instanceof LessThanEquals) {
			
			Code.putFalseJump(Code.le, 0);
			//conditionalJumpAddressesList.add(Code.pc - 2);
			
		}
		
		condFactJumpAdrs.add(Code.pc - 2);
		
		parent = MultipleExprFact.getParent();
		
		while (parent != null) {
			
			//log.info(parent.getClass());
			//parent = parent.getParent();
			
			if (parent instanceof SingleFactTerm 
					&& parent.getParent() instanceof SingleTermCondition
					&& parent.getParent().getParent() instanceof ConditionSuccess) {
				
				singleExpressionCondition = true;
				break;
				
			}
			
			if (parent instanceof MultipleFactTerm) {
				
				parent = parent.getParent();
				
				while (parent != null) {
					
					if (parent instanceof SingleTermCondition && parent.getParent() instanceof ConditionSuccess ) {
						
						onlyAndExpression = true;
						break;
						
					}
					
					parent = parent.getParent();
					
				}
				
				if (onlyAndExpression) break;
				
			}
			
			if (parent != null) parent = parent.getParent();
			
		}
		
		if (singleExpressionCondition) {
			
			log.info("SINGLE EXPRESSION CONDITION");
			//conditionalUnsuccessfulJumpToStatements.add(Code.pc - 2);
			
		}
		
		if (onlyAndExpression) {
			
			log.info("ONLY AND EXPRESSION CONDITION");
			//conditionalUnsuccessfulJumpToStatements.add(Code.pc - 2);
			
		}
	
	}
	
	public void visit (SingleFactTerm SingleFactTerm) {
		
		log.info("SingleFactTerm");
		
		if (SingleFactTerm.getParent() instanceof SingleTermCondition 
				|| SingleFactTerm.getParent() instanceof MultipleTermCondition ) {
			
			log.info("nema nigde &&");
			log.info(Code.buf [Code.pc]);
			log.info(Code.buf [Code.pc - 1]);
			log.info(Code.buf [Code.pc - 2]);
			log.info(Code.buf [Code.pc - 3]);
			
			if (!(SingleFactTerm.getParent().getParent() instanceof ConditionSuccess))
				condFactJumpAdrs.clear();
			else
				log.info("SingleFactTerm nije poslednji u uslovu");
			
			condTermJumpAdrs.add(Code.pc - 2);
			
			switch (Code.buf [Code.pc - 3]) {
			
			case 43: Code.buf [Code.pc - 3] = 44; break;
			case 44: Code.buf [Code.pc - 3] = 43; break;
			case 45: Code.buf [Code.pc - 3] = 48; break;
			case 46: Code.buf [Code.pc - 3] = 47; break;
			case 47: Code.buf [Code.pc - 3] = 46; break;
			case 48: Code.buf [Code.pc - 3] = 45; break;
			default: break;
			
			}
		
		}
		
	}
	
	public void visit (MultipleFactTerm MultipleFactTerm) {
		
		log.info("MultipleFactTerm");
		
		if (MultipleFactTerm.getParent() instanceof SingleTermCondition
				|| MultipleFactTerm.getParent() instanceof MultipleTermCondition) {
			
			log.info("poslednja smena sa &&");
			log.info(Code.pc);
			log.info(Code.buf [Code.pc]);
			log.info(Code.buf [Code.pc - 1]);
			log.info(Code.buf [Code.pc - 2]);
			log.info(Code.buf [Code.pc - 3]);
			
			for (Integer adr : condFactJumpAdrs)
				Code.fixup(adr);
			
			if (!(MultipleFactTerm.getParent().getParent() instanceof ConditionSuccess))
				condFactJumpAdrs.clear();
			else
				log.info("MultipleFactTerm je poslednji u uslovu");
			
			log.info(Code.buf [Code.pc]);
			log.info(Code.buf [Code.pc - 1]);
			log.info(Code.buf [Code.pc - 2]);
			log.info(Code.buf [Code.pc - 3]);
			
			condTermJumpAdrs.add(Code.pc - 2);
			
			switch (Code.buf [Code.pc - 3]) {
			
			case 43: Code.buf [Code.pc - 3] = 44; break;
			case 44: Code.buf [Code.pc - 3] = 43; break;
			case 45: Code.buf [Code.pc - 3] = 48; break;
			case 46: Code.buf [Code.pc - 3] = 47; break;
			case 47: Code.buf [Code.pc - 3] = 46; break;
			case 48: Code.buf [Code.pc - 3] = 45; break;
			default: break;
			
			}
			
		}
		
	}
	
	public void visit (SingleTermCondition SingleTermCondition) {
		
		log.info("SingleTermCondition");
		
		if (SingleTermCondition.getParent() instanceof ConditionSuccess ) {
			
			log.info("nema nigde ||");
			
			condTermJumpAdrs.clear();
			log.info(Code.buf [Code.pc]);
			log.info(Code.buf [Code.pc - 1]);
			log.info(Code.buf [Code.pc - 2]);
			log.info(Code.buf [Code.pc - 3]);
			
			conditionJumpAdrs.add(Code.pc - 2);
			
			switch (Code.buf [Code.pc - 3]) {
			
			case 43: Code.buf [Code.pc - 3] = 44; break;
			case 44: Code.buf [Code.pc - 3] = 43; break;
			case 45: Code.buf [Code.pc - 3] = 48; break;
			case 46: Code.buf [Code.pc - 3] = 47; break;
			case 47: Code.buf [Code.pc - 3] = 46; break;
			case 48: Code.buf [Code.pc - 3] = 45; break;
			default: break;
			
			}
		
		}
	}
	
	public void visit (MultipleTermCondition MultipleTermCondition) {
		
		log.info("MultipleTermCondition");

		if (MultipleTermCondition.getParent() instanceof ConditionSuccess) {
			
			log.info("poslednja smena sa ||");
			log.info(Code.buf [Code.pc]);
			log.info(Code.buf [Code.pc - 1]);
			log.info(Code.buf [Code.pc - 2]);
			log.info(Code.buf [Code.pc - 3]);
			
			for (Integer adr : condTermJumpAdrs)
				Code.fixup(adr);
			
			condTermJumpAdrs.clear();
			
			log.info(Code.buf [Code.pc]);
			log.info(Code.buf [Code.pc - 1]);
			log.info(Code.buf [Code.pc - 2]);
			log.info(Code.buf [Code.pc - 3]);
			
			conditionJumpAdrs.add(Code.pc - 2);
			
			switch (Code.buf [Code.pc - 3]) {
			
			case 43: Code.buf [Code.pc - 3] = 44; break;
			case 44: Code.buf [Code.pc - 3] = 43; break;
			case 45: Code.buf [Code.pc - 3] = 48; break;
			case 46: Code.buf [Code.pc - 3] = 47; break;
			case 47: Code.buf [Code.pc - 3] = 46; break;
			case 48: Code.buf [Code.pc - 3] = 45; break;
			default: break;
			
			}
			
		}
		
	}
	
	public void visit (ConditionSuccess ConditionSuccess) {
		
		log.info("ConditionSuccess");
		
		log.info(condFactJumpAdrs.size());
		for (Integer adr : condFactJumpAdrs)
			log.info(adr);
		log.info(condTermJumpAdrs.size());
		for (Integer adr : condTermJumpAdrs)
			log.info(adr);
		log.info(conditionJumpAdrs.size());
		for (Integer adr : conditionJumpAdrs)
			log.info(adr);
		log.info(conditionalAndForJumps.size());
		
		log.info(Code.pc);
		
		//for (Integer adr : condFactJumpAdrs)
			//conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).add(adr);
		
		for (int i = 0 ; i < condFactJumpAdrs.size() - 1; i++)
			conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).add(condFactJumpAdrs.get(i));
		
		condFactJumpAdrs.clear();
		
		//for (Integer adr : condTermJumpAdrs)
			//Code.fixup(adr);
		
		//condTermJumpAdrs.clear();
		
		for (Integer adr : conditionJumpAdrs)
			Code.fixup(adr);
		
		conditionJumpAdrs.clear();
		
		log.info(Code.buf [Code.pc]);
		log.info(Code.buf [Code.pc - 1]);
		log.info(Code.buf [Code.pc - 2]);
		log.info(Code.buf [Code.pc - 3]);
		
		conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).add(Code.pc - 2);
		
		log.info(conditionalAndForJumps.get(conditionalAndForJumps.size() - 1).size());
		
		for (Integer adr : conditionalAndForJumps.get(conditionalAndForJumps.size() - 1))
			log.info(adr);
		
		/*switch (Code.buf [Code.pc - 3]) {
		
		case 43: Code.buf [Code.pc - 3] = 44; break;
		case 44: Code.buf [Code.pc - 3] = 43; break;
		case 45: Code.buf [Code.pc - 3] = 48; break;
		case 46: Code.buf [Code.pc - 3] = 47; break;
		case 47: Code.buf [Code.pc - 3] = 46; break;
		case 48: Code.buf [Code.pc - 3] = 45; break;
		default: break;
		}*/
		
	}

}
