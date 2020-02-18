package rs.ac.bg.etf.pp1;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;

public class CodeGenerator extends VisitorAdaptor {
	
	Logger log = Logger.getLogger(CodeGenerator.class);
	
	private int mainPc;
	
	private int nVars = 0;
	
	private MyDumpSymbolTableVisitor stv = new MyDumpSymbolTableVisitor ();
	
	private List<List<Integer>> conditionalAndLoopJumps = new ArrayList<List<Integer>> (); // lista koja sluzi za skakanje na naredbe u if-else ili for petlji
	
	private List<Integer> condFactJumpAdrs = new ArrayList<Integer> ();
	
	private List<Integer> condTermJumpAdrs = new ArrayList<Integer> ();
	
	private List<Integer> conditionJumpAdrs = new ArrayList<Integer> ();
	
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
	}

	public void visit (PrintStatement PrintStatement) {
		
		if (PrintStatement.getPrintOption() instanceof NoPrintArg)
			Code.loadConst(5);
		
		if (PrintStatement.getExpr().struct == Tab.intType) Code.put(Code.print);
		
		else if (PrintStatement.getExpr().struct == Tab.charType) Code.put(Code.bprint);
		
		else if (PrintStatement.getExpr().struct == MyTabImpl.boolType) {
			
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
	
	/* assignment, increment and decrement statements */
	
	public void visit (AssignStatementSuccess AssignStatementSuccess) {
		
		Code.store (AssignStatementSuccess.getDestination().obj);
		
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
    	
    	Code.put(Code.newarray);
    	Code.put(NewArrayFactor.getType().struct == Tab.charType ? 0 : 1);
    
    }
	
	/* method call */
	
	public void visit (MethodDesignator MethodDesignator) {
		
		int destAdr = MethodDesignator.getDesignator().obj.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(destAdr);
	
	}
	
	public void visit (MethodCallStatement MethodCallStatement) {
		
		int destAdr = MethodCallStatement.getDestination().obj.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(destAdr);
		
	}
	
	/* arithmetic operations */
	
	public void visit (MultipleFactorTerm MultipleFactorTerm) {
		
		if (MultipleFactorTerm.getMulop() instanceof Mul)
			Code.put(Code.mul);
		
		else if (MultipleFactorTerm.getMulop() instanceof Div)
			Code.put(Code.div);
		
		else if (MultipleFactorTerm.getMulop() instanceof Mod)
			Code.put(Code.rem);
		
	}
	
	public void visit (MultipleTermExpr MultipleTermExpr) {
		
		if (MultipleTermExpr.getAddop() instanceof Plus)
			Code.put(Code.add);
		
		else if (MultipleTermExpr.getAddop() instanceof Minus)
			Code.put(Code.sub);
		
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
		
		SyntaxNode parent = SimpleDesignator.getParent();
		
		while (parent != null) {
			
			//log.info(parent.getClass());
			if (parent instanceof ReadStatement || parent instanceof Destination) {
				
				//log.info("should return");
				return;
			}
			if (parent instanceof ArrayDesignator && parent.getParent() != null && parent.getParent() instanceof Destination) {
				
				//log.info("should break");
				break;
			}
			parent = parent.getParent();
			
		}
    	
		Code.load (SimpleDesignator.obj);
    	
    }
	
	public void visit (ArrayDesignator ArrayDesignator) {
		
		SyntaxNode parent = ArrayDesignator.getParent();
		
		while (parent != null) {
			
			//log.info(parent.getClass());
			if (parent instanceof ReadStatement || parent instanceof Destination) return;
			parent = parent.getParent();
			
		}
    	
		Code.load (ArrayDesignator.obj);
    	
    }
	
	public void visit (DeclDesignator DeclDesignator) {
		
		if (DeclDesignator.getDesignator() instanceof ArrayDesignator) {
			
			SyntaxNode parent = DeclDesignator.getParent();
			
			while (parent != null) {
				
				if (parent instanceof Source || parent instanceof PrintStatement || parent instanceof ReadStatement || parent instanceof ActPar) return;
				
				parent = parent.getParent();
				
			}
			
			if (DeclDesignator.struct == Tab.charType) Code.put(Code.baload);
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
	
	public void visit (If If) {
		
		log.info("If");
		conditionalAndLoopJumps.add(new ArrayList<Integer> ());
		
	}
	
	public void visit (Else Else) {
		
		Code.putJump(0);
		
		for (Integer adr : conditionalAndLoopJumps.get(conditionalAndLoopJumps.size() - 1)) {
			
			Code.fixup(adr);
			
		}
		
		conditionalAndLoopJumps.get(conditionalAndLoopJumps.size() - 1).clear();
		
		conditionalAndLoopJumps.get(conditionalAndLoopJumps.size() - 1).add(Code.pc - 2);
		
	}
	
	public void visit (MatchedIfStatement MatchedIfStatement) {
		
		for (Integer adr : conditionalAndLoopJumps.get(conditionalAndLoopJumps.size() - 1)) {
			
			Code.fixup(adr);
			
		}
		
		conditionalAndLoopJumps.remove(conditionalAndLoopJumps.size() - 1);
		
	}
	
	public void visit (UnmatchedIfStatement UnmatchedIfStatement) {
		
		for (Integer adr : conditionalAndLoopJumps.get(conditionalAndLoopJumps.size() - 1)) {
			
			Code.fixup(adr);
			
		}
		
		conditionalAndLoopJumps.remove(conditionalAndLoopJumps.size() - 1);
		
	}
	
	/* conditions */
	
	/* prva iteracija: samo da zveknemo normalan skok pa da vidimo sta cemo */
	
	public void visit (SingleExprFact SingleExprFact) {
		
		log.info("SingleExprFact");
		
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
				log.info("MultipleFactTerm nije poslednji u uslovu");
			
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
		log.info(condTermJumpAdrs.size());
		log.info(conditionJumpAdrs.size());
		log.info(conditionalAndLoopJumps.size());
		
		for (Integer adr : condFactJumpAdrs)
			conditionalAndLoopJumps.get(conditionalAndLoopJumps.size() - 1).add(adr);
		
		condFactJumpAdrs.clear();
		
		for (Integer adr : condTermJumpAdrs)
			Code.fixup(adr);
		
		condTermJumpAdrs.clear();
		
		for (Integer adr : conditionJumpAdrs)
			Code.fixup(adr);
		
		conditionJumpAdrs.clear();
		
		log.info(Code.buf [Code.pc]);
		log.info(Code.buf [Code.pc - 1]);
		log.info(Code.buf [Code.pc - 2]);
		log.info(Code.buf [Code.pc - 3]);
		
		conditionalAndLoopJumps.get(conditionalAndLoopJumps.size() - 1).add(Code.pc - 2);
		
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