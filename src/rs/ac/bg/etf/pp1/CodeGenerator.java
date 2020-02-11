package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Struct;

public class CodeGenerator extends VisitorAdaptor {
	
	Logger log = Logger.getLogger(CodeGenerator.class);
	
	private int mainPc;
	
	private int nVars;
	
	private MyDumpSymbolTableVisitor stv = new MyDumpSymbolTableVisitor ();
	
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
			
			log.info("konstanta tru");
			Code.loadConst(1);
		}
		
		else {
			
			log.info("konstanta false");
			Code.loadConst(0);
		}
	
	}
	
	/* designators */
	
	public void visit (SimpleDesignator SimpleDesignator) {
		
		SyntaxNode parent = SimpleDesignator.getParent();
		
		while (parent != null) {
			
			log.info(parent.getClass());
			if (parent instanceof ReadStatement || parent instanceof Destination) {
				
				log.info("should return");
				return;
			}
			if (parent instanceof ArrayDesignator && parent.getParent() != null && parent.getParent() instanceof Destination) {
				
				log.info("should break");
				break;
			}
			parent = parent.getParent();
			
		}
    	
		Code.load (SimpleDesignator.obj);
    	
    }
	
	public void visit (ArrayDesignator ArrayDesignator) {
		
		SyntaxNode parent = ArrayDesignator.getParent();
		
		while (parent != null) {
			
			log.info(parent.getClass());
			if (parent instanceof ReadStatement || parent instanceof Destination) return;
			parent = parent.getParent();
			
		}
    	
		Code.load (ArrayDesignator.obj);
    	
    }
	
	public void visit (DeclDesignator DeclDesignator) {
		
		if (DeclDesignator.getDesignator() instanceof ArrayDesignator) {
			
			SyntaxNode parent = DeclDesignator.getParent();
			
			while (parent != null) {
				
				if (parent instanceof Source || parent instanceof PrintStatement || parent instanceof ReadStatement) return;
				
				parent = parent.getParent();
				
			}
			
			if (DeclDesignator.struct == Tab.charType) Code.put(Code.baload);
			else
				Code.put(Code.aload);
			
		}
	}
	
	/* main */
	
	public void visit (MethodName MethodName) {
		
		if("main".equalsIgnoreCase(MethodName.getMethodName())){
			mainPc = Code.pc;
		}
		MethodName.obj.setAdr(Code.pc);
		
		Code.put(Code.enter);
		Code.put(MethodName.obj.getLevel());
		Code.put(MethodName.obj.getLocalSymbols().size());
		
	}
	
	public void visit (MethodDeclSuccess MethodDeclSuccess) {
		
		Code.put(Code.exit); Code.put(Code.return_); 
	}

}
