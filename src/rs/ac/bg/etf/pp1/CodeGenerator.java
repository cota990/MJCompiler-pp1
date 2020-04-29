package rs.ac.bg.etf.pp1;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class CodeGenerator extends VisitorAdaptor {
	
	Logger log = Logger.getLogger(CodeGenerator.class);
	
	private int mainPc;
	
	private int nVars = 0;
	
	private List<MyObjImpl> foreachIdent = new ArrayList<MyObjImpl> ();
	
	private int ifAndForCounter = 0;
	
	private List<List<Integer>> controlStructuresExprJumpFixupAddresses = new ArrayList<List<Integer>> ();
	
	private List<Integer> controlStructuresTypes = new ArrayList<Integer> ();
	
	private List<List<Integer>> breakStatementsAddresses = new ArrayList<List<Integer>> ();
	
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
		
		SyntaxNode methodDecl = mn.getParent();
		
		if (methodDecl instanceof MethodDecl) {

			mn.myobjimpl.setAdr(Code.pc);
			
			Code.put(Code.enter);
			Code.put(mn.myobjimpl.getLevel());
			Code.put(mn.myobjimpl.getLocalSymbols().size());
		
		}
		
		else if (methodDecl instanceof ClassMethodDecl) {
			
			mn.myobjimpl.setAdr(Code.pc);
			
			Code.put(Code.enter);
			Code.put(mn.myobjimpl.getLevel() + 1);
			Code.put(mn.myobjimpl.getLocalSymbols().size());
			
		}
		
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
	
	/** Class method end;
	 * <br> generate code for return from function if void (EXIT command, RETURN command)
	 * <br> throws runtime error if method is non void
	 */
	public void visit (ClassMethodDecl cmds) {
		
		if (cmds.getMethodName().myobjimpl.getType() == MyTabImpl.noType) {
			
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
	
	/**NonDestination generator; used in increment and decrement statements
	 */
	public void visit(NonDestination nd) {
		
		ExpressionLeftAssocCodeGenerator nonDestinationCodeGenerator = new ExpressionLeftAssocCodeGenerator(0);
			
		nd.traverseBottomUp(nonDestinationCodeGenerator);
		
	}
	
	/**Source generator; used in assign statement
	 */
	public void visit(SourceSuccess ss) {
		
		if (ss.getExpr() instanceof ExprWithAssign) {
			
			ExpressionRightAssocCodeGenerator sourceExprGenerator = new ExpressionRightAssocCodeGenerator();
			
			ss.getExpr().traverseBottomUp(sourceExprGenerator);
			
		}
		
		else if (ss.getExpr() instanceof ExprWithoutAssign) {
			
			ExpressionLeftAssocCodeGenerator sourceExprGenerator = new ExpressionLeftAssocCodeGenerator(0);
			
			ss.getExpr().traverseBottomUp(sourceExprGenerator);
			
		}
		
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
				
				ExpressionLeftAssocCodeGenerator returnExprGenerator = new ExpressionLeftAssocCodeGenerator(0);
				
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
			
			ExpressionLeftAssocCodeGenerator printExprGenerator = new ExpressionLeftAssocCodeGenerator(0);
			
			printStatement.getExpr().traverseBottomUp(printExprGenerator);
			
		}
		
		if (printStatement.getPrintOption() instanceof PrintArg) {
			
			PrintArg printArg = (PrintArg) printStatement.getPrintOption();
			
			Code.loadConst(printArg.getN1());
			
		}
		
		else if (printStatement.getPrintOption() instanceof NoPrintArg)
			Code.loadConst(1);
		
	}
	
	/** Foreach iterator; used in foreach loop
	 */
	public void visit(IteratorName in) {
		
		foreachIdent.add(in.myobjimpl);
		
	}
	
	/**Logical condition; used in if and for statements
	 * <br> conditional jumps implementation:
	 * <br> current Code.pc is address to jump to if condition is true; false jump will be determined later
	 * <br> collect code position of each expression start and of each address of jumps (must be same number)
	 * <br> collect number of conditional factors in each of the terms except in last;
	 * <br> set condTermIndex to 0; set newAdrIndex to getNumOfFactorsInTerms [condTermIndex];
	 * <br> for each address to fix:
	 * <br> check if condTermIndex == getNumOfFactorsInTerms.size;
	 * <br> if true, add address to controlStructuresExprJumpFixupAddresses;
	 * <br> if false check if index + 1 == newAdrIndex
	 * <br> if true; change relation operator and put jump address to currentPc; increment condTermIndex, set newAdrIndex to newAdrIndex + getNumOfFactorsInTerms [condTermIndex]
	 * <br> if false; put jump address to exprStartAddress[newAdrIndex]
	 * <br> after for loop, add last fixup address to 
	 */
	public void visit(IfCondition c) {
		
		ConditionCodeGenerator conditionCodeGenerator = new ConditionCodeGenerator ();
		
		c.traverseBottomUp(conditionCodeGenerator);
		
		if (conditionCodeGenerator.getExprStartAddress().size() 
				== conditionCodeGenerator.getConditionalFactorsJumpAddresses().size()) {
			
			int condTermIndex = 0;
			int newAdrIndex = conditionCodeGenerator.getNumOfFactorsInTerms().isEmpty() 
								? 0
								: conditionCodeGenerator.getNumOfFactorsInTerms().get(condTermIndex);
			int i;
			
			for (i = 0; i < conditionCodeGenerator.getConditionalFactorsJumpAddresses().size() - 1; i++) {
				
				/*log.info(i);
				log.info(condTermIndex);
				log.info(newAdrIndex);*/
				
				if (condTermIndex == conditionCodeGenerator.getNumOfFactorsInTerms().size() ) {
					 
					//last term factors; each of these should jump after statement if false
					
					controlStructuresExprJumpFixupAddresses.get(ifAndForCounter - 1).add(conditionCodeGenerator.getConditionalFactorsJumpAddresses().get(i));
					
				}
				
				else {
					
					// jump if true to statement body; change operation in buffer and jump to curr pc; collect next index
					if (i + 1 == newAdrIndex) {
						
						int operationAddress = conditionCodeGenerator.getConditionalFactorsJumpAddresses().get(i) - 1;
						
						switch (Code.buf [operationAddress]) {
						
							case 43: Code.buf [operationAddress] = 44; break;
							case 44: Code.buf [operationAddress] = 43; break;
							case 45: Code.buf [operationAddress] = 48; break;
							case 46: Code.buf [operationAddress] = 47; break;
							case 47: Code.buf [operationAddress] = 46; break;
							case 48: Code.buf [operationAddress] = 45; break;
							default: break;
						
						}
						
						Code.fixup(conditionCodeGenerator.getConditionalFactorsJumpAddresses().get(i));
						
						condTermIndex++;
						
						if (condTermIndex != conditionCodeGenerator.getNumOfFactorsInTerms().size())
							newAdrIndex += conditionCodeGenerator.getNumOfFactorsInTerms().get(condTermIndex);
						
					}
					
					// jump to next term
					else {
						
						Code.put2(conditionCodeGenerator.getConditionalFactorsJumpAddresses().get(i),
								conditionCodeGenerator.getExprStartAddress().get(newAdrIndex) 
									- conditionCodeGenerator.getConditionalFactorsJumpAddresses().get(i) + 1);
						
					}
				}
				
			}
			
			controlStructuresExprJumpFixupAddresses.get(ifAndForCounter - 1).add(conditionCodeGenerator.getConditionalFactorsJumpAddresses().get(i));
			
		}
		else
			log.info("GRESKA!");
		
	}
	
	/** Code generator for for condition; same as if condition
	 */
	public void visit(ForConditionSuccess fcs) {
		
		ConditionCodeGenerator conditionCodeGenerator = new ConditionCodeGenerator ();
		
		fcs.traverseBottomUp(conditionCodeGenerator);
		
		if (conditionCodeGenerator.getExprStartAddress().size() 
				== conditionCodeGenerator.getConditionalFactorsJumpAddresses().size()) {
			
			int condTermIndex = 0;
			int newAdrIndex = conditionCodeGenerator.getNumOfFactorsInTerms().isEmpty() 
								? 0
								: conditionCodeGenerator.getNumOfFactorsInTerms().get(condTermIndex);
			int i;
			
			for (i = 0; i < conditionCodeGenerator.getConditionalFactorsJumpAddresses().size() - 1; i++) {
				
				/*log.info(i);
				log.info(condTermIndex);
				log.info(newAdrIndex);*/
				
				if (condTermIndex == conditionCodeGenerator.getNumOfFactorsInTerms().size() ) {
					 
					//last term factors; each of these should jump after statement if false
					
					controlStructuresExprJumpFixupAddresses.get(ifAndForCounter - 1).add(conditionCodeGenerator.getConditionalFactorsJumpAddresses().get(i));
					
				}
				
				else {
					
					// jump if true to statement body; change operation in buffer and jump to curr pc; collect next index
					if (i + 1 == newAdrIndex) {
						
						int operationAddress = conditionCodeGenerator.getConditionalFactorsJumpAddresses().get(i) - 1;
						
						switch (Code.buf [operationAddress]) {
						
							case 43: Code.buf [operationAddress] = 44; break;
							case 44: Code.buf [operationAddress] = 43; break;
							case 45: Code.buf [operationAddress] = 48; break;
							case 46: Code.buf [operationAddress] = 47; break;
							case 47: Code.buf [operationAddress] = 46; break;
							case 48: Code.buf [operationAddress] = 45; break;
							default: break;
						
						}
						
						Code.fixup(conditionCodeGenerator.getConditionalFactorsJumpAddresses().get(i));
						
						condTermIndex++;
						
						if (condTermIndex != conditionCodeGenerator.getNumOfFactorsInTerms().size())
							newAdrIndex += conditionCodeGenerator.getNumOfFactorsInTerms().get(condTermIndex);
						
					}
					
					// jump to next term
					else {
						
						Code.put2(conditionCodeGenerator.getConditionalFactorsJumpAddresses().get(i),
								conditionCodeGenerator.getExprStartAddress().get(newAdrIndex) 
									- conditionCodeGenerator.getConditionalFactorsJumpAddresses().get(i) + 1);
						
					}
				}
				
			}
			
			controlStructuresExprJumpFixupAddresses.get(ifAndForCounter - 1).add(conditionCodeGenerator.getConditionalFactorsJumpAddresses().get(i));
			
		}
		else
			log.info("GRESKA!");
		
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
	
	/*
	 * designator statements
	 */
	
	/**Assign statement; 
	 * <br>destination designator and source expr already loaded;
	 * <br>check Assignop
	 * <br>if Assignop instanceof Assign: dup_x1 -> pop -> pop ; expr stack: ...,prev_val,new_val -> ...,new_val
	 * <br>if Assignop instanceof AddopRight or MulopRight: put code operation ; expr stack: ...,prev_val,new_val -> ...,prev_val operator new_val
	 * <br>store designator
	 */
	public void visit(AssignStatement as) {
		
		if (as.getAssignop() instanceof Assign) {
				
			Code.put(Code.dup_x1); Code.put(Code.pop); Code.put(Code.pop);
			
		}
		
		else if (as.getAssignop() instanceof AddopAssign) {
			
			AddopAssign addop = (AddopAssign) as.getAssignop();
			
			if (addop.getAddopRight() instanceof PlusAssign)
				Code.put(Code.add);
			
			else if (addop.getAddopRight() instanceof MinusAssign)
				Code.put(Code.sub);
			
		}
		
		else if (as.getAssignop() instanceof MulopAssign) {
			
			MulopAssign mulop = (MulopAssign) as.getAssignop();
			
			if (mulop.getMulopRight() instanceof MulAssign)
				Code.put(Code.mul);
			
			else if (mulop.getMulopRight() instanceof DivAssign)
				Code.put(Code.div);
			
			else if (mulop.getMulopRight() instanceof ModAssign)
				Code.put(Code.rem);
			
		}
		
		Code.store(as.getDestination().myobjimpl);
	
	}
	
	/**Increment statement;
	 * <br>destination designator already loaded
	 * <br>load const 1, put ADD operator, store designator
	 */
	public void visit(IncrementStatement is) {
		
		Code.loadConst(1);
		Code.put(Code.add);
		Code.store(is.getDestination().myobjimpl);
	
	}
	
	/**Decrement statement;
	 * <br>destination designator already loaded; pop;
	 * <br>load const 1, put SUB operator, store designator
	 */
	public void visit(DecrementStatement ds) {
		
		Code.loadConst(1);
		Code.put(Code.sub);
		Code.store(ds.getDestination().myobjimpl);
	
	}
	
	/**Method call statement;
	 * <br> generate code for actual parameters, if any
	 * <br> call method (CALL statement and return address)
	 */
	public void visit(MethodCallStatement mcs) {
		
		if (mcs.getActParamsOption() instanceof ActualParameters) {
				
			ActualParameters actualParameters = (ActualParameters) mcs.getActParamsOption();
			
			ExpressionLeftAssocCodeGenerator actualParametersCodeGenerator = new ExpressionLeftAssocCodeGenerator (0);
			
			actualParameters.traverseBottomUp(actualParametersCodeGenerator);
			
		}
		
		if (!(mcs.getMethodDesignator().getDesignator().myobjimpl.getName().equals("ord")
				|| mcs.getMethodDesignator().getDesignator().myobjimpl.getName().equals("chr")
				|| mcs.getMethodDesignator().getDesignator().myobjimpl.getName().equals("len") )) {
		
			int destAdr = mcs.getMethodDesignator().getDesignator().myobjimpl.getAdr() - Code.pc;
			Code.put(Code.call);
			Code.put2(destAdr);
			
		}
		
		else {
			
			if (mcs.getMethodDesignator().getDesignator().myobjimpl.getName().equals("len"))
				Code.put (Code.arraylength);
			
		}
	
	}
	
	/*
	 * control structures
	 */
	
	/** If structure starter;
	 * 	<br> increment ifAndForCounter
	 */
	public void visit(If i) {
		
		ifAndForCounter++;
		
		if (i.getParent() instanceof MatchedIfStatement)
			controlStructuresTypes.add(ConditionCodeGenerator.MatchedIf);
		
		else if (i.getParent() instanceof UnmatchedIfStatement)
			controlStructuresTypes.add(ConditionCodeGenerator.UnmatchedIf);
		
		else if (i.getParent() instanceof UnmatchedElseStatement)
			controlStructuresTypes.add(ConditionCodeGenerator.UnmatchedElse);
		
		controlStructuresExprJumpFixupAddresses.add(new ArrayList<Integer> ());
		
	}
	
	/** Else statement starter;
	 * <br> fixup all condition addresses to current pc; putJump
	 */
	public void visit(Else e) {
		
		Code.putJump(0);
		
		for (Integer address : controlStructuresExprJumpFixupAddresses.get(ifAndForCounter - 1))
			Code.fixup(address);
		
		controlStructuresExprJumpFixupAddresses.get(ifAndForCounter - 1).clear();
		controlStructuresExprJumpFixupAddresses.get(ifAndForCounter - 1).add(Code.pc - 2);
		
	}
	
	/**For structure starter;
	 * <br> increment ifAndForCounter
	 */
	public void visit(For f) {
		
		ifAndForCounter++;
		
		if (f.getParent() instanceof MatchedForStatement)
			controlStructuresTypes.add(ConditionCodeGenerator.MatchedFor);
		
		else if (f.getParent() instanceof UnmatchedForStatement)
			controlStructuresTypes.add(ConditionCodeGenerator.UnmatchedFor);
		
		controlStructuresExprJumpFixupAddresses.add(new ArrayList<Integer> ());
		
		breakStatementsAddresses.add(new ArrayList<Integer> ());
		
	}
	
	/** First statement in for loop, done before body of loop;
	 * <br> first address for this statement in controlStructuresExprJumpFixupAddresses - (0) Condition
	 */
	public void visit(FirstDesignatorStatement fds) {
		
		controlStructuresExprJumpFixupAddresses.get(ifAndForCounter - 1).add(Code.pc);
		
	}
	
	/** First statement in for loop omitted;
	 * <br> first address for this statement in controlStructuresExprJumpFixupAddresses - (0) Condition
	 */
	public void visit(NoFirstDesignatorStatement nfds) {
		
		if (controlStructuresExprJumpFixupAddresses.isEmpty()
				|| controlStructuresExprJumpFixupAddresses.get(ifAndForCounter - 1) == null)
			controlStructuresExprJumpFixupAddresses.add(new ArrayList<Integer> ());
		
		controlStructuresExprJumpFixupAddresses.get(ifAndForCounter - 1).add(Code.pc);
		
	}
	
	/** Condition of for loop, visited and generated in ForCondition code generator; controlStructuresExprJumpFixupAddresses - (0) Condition, (1..n) n factors in last terms for jumps out of for loop
	 * <br> put in jump to statement body; add address to fix - controlStructuresExprJumpFixupAddresses - (0) Condition, (1..n) n jumps out of for loop, (n + 1) jump to for loop body
	 * <br> memorize address as start of last designator statement - controlStructuresExprJumpFixupAddresses - (0) Condition, (1..n) n jumps out of for loop, (n + 1) jump to for loop body, (n + 2) LastDesignatorStatement
	 */
	public void visit(ForCondition fc) {
		
		// jump to statement body
		Code.putJump(0);
		
		controlStructuresExprJumpFixupAddresses.get(ifAndForCounter - 1).add(Code.pc - 2);
		
		// address of last designator statement
		controlStructuresExprJumpFixupAddresses.get(ifAndForCounter - 1).add(Code.pc);
		
	}
	
	/** Condition of for loop omitted;
	 * generate code as if true was loaded; then do as in ForCondition
	 */
	public void visit(NoForCondition nfc) {
		
		// generate condition and jump out of loop
		Code.loadConst(1);
		Code.loadConst(1);
		Code.putFalseJump(Code.eq, 0);
		
		controlStructuresExprJumpFixupAddresses.get(ifAndForCounter - 1).add(Code.pc - 2);
		
		// jump to statement body
		Code.putJump(0);
		
		controlStructuresExprJumpFixupAddresses.get(ifAndForCounter - 1).add(Code.pc - 2);
		
		// address of last designator statement
		controlStructuresExprJumpFixupAddresses.get(ifAndForCounter - 1).add(Code.pc);
		
	}
	
	/** Last statement of for loop; put jump back to condition; then fix jump to statement body, as next statement is first statement of for loop body and remove that addr
	 * <br> controlStructuresExprJumpFixupAddresses - (0) Condition, (1..n) n jumps out of for loop, (n + 1) jump to for loop body, (n + 2) LastDesignatorStatement
	 * <br> after: controlStructuresExprJumpFixupAddresses - (0) Condition, (1..n) n jumps out of for loop, (n + 1) LastDesignatorStatement
	 */
	public void visit(SecondDesignatorStatement sds) {
		
		// jump to condition
		Code.putJump(controlStructuresExprJumpFixupAddresses.get(ifAndForCounter - 1).get(0));
		
		// address of statement body (fixup jump)
		Code.fixup (
				controlStructuresExprJumpFixupAddresses.get(ifAndForCounter - 1).remove(
						controlStructuresExprJumpFixupAddresses.get(ifAndForCounter - 1).size() - 2));
		
	}
	
	/** Last statement of for omitted; do same as when non omitted
	 */
	public void visit(NoSecondDesignatorStatement nsds) {
		
		// jump to condition
		Code.putJump(controlStructuresExprJumpFixupAddresses.get(ifAndForCounter - 1).get(0));
		
		// address of statement body (fixup jump)
		Code.fixup (
				controlStructuresExprJumpFixupAddresses.get(ifAndForCounter - 1).remove(
						controlStructuresExprJumpFixupAddresses.get(ifAndForCounter - 1).size() - 2));
		
	}
	
	/**Foreach structure starter;
	 * <br> increment ifAndForCounter
	 */
	public void visit(Foreach f) {
		
		ifAndForCounter++;
		
		if (f.getParent() instanceof MatchedForeachStatement)
			controlStructuresTypes.add(ConditionCodeGenerator.MatchedForeach);
		
		else if (f.getParent() instanceof UnmatchedForeachStatement)
			controlStructuresTypes.add(ConditionCodeGenerator.UnmatchedForeach);
		
		breakStatementsAddresses.add(new ArrayList<Integer> ());
		
		controlStructuresExprJumpFixupAddresses.add(new ArrayList<Integer> ());
		
	}
	
	/**Foreach array; used in foreach loop;
	 * <br> designator of array is loaded here; 
	 * <br> load const -1; store pc, and iteration code starts: expr stack: adr, -1
	 * <br> load const 1; add; expr stack: adr, 0
	 * <br> dup_x1 -> pop -> dup2 -> arraylen: expr stack: 0, adr, 0, len(adr) 
	 * <br> jmp out of loop if greater or equals; expr stack: 0, adr
	 * <br> dup_x1 -> pop -> dup2 -> load element; expr stack: adr, 0, adr[0]
	 * <br> store value in foreachIdent; expr stack: adr, 0
	 * <br> array address and current index are laoded, so repeat these until jump condition is met
	 */
	public void visit(ForeachArray fa) {
		
		DestinationCodeGenerator foreachArrayCodeGenerator = new DestinationCodeGenerator();
		
		fa.traverseBottomUp(foreachArrayCodeGenerator);
		
		Code.loadConst(-1);
		
		controlStructuresExprJumpFixupAddresses.get(ifAndForCounter - 1).add(Code.pc);
		
		Code.loadConst(1);
		Code.put(Code.add);
		
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
		Code.put(Code.dup2);
		Code.put(Code.arraylength);
		
		Code.putFalseJump(Code.lt, 0);
		controlStructuresExprJumpFixupAddresses.get(ifAndForCounter - 1).add(Code.pc - 2);
		
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
		Code.put(Code.dup2);
		
		if (foreachIdent.get(foreachIdent.size() - 1).getType() == MyTabImpl.charType)
			Code.put(Code.baload);
		else
			Code.put(Code.aload);
		
		Code.store(foreachIdent.get(foreachIdent.size() - 1));
		
	}
	
	/**Matched if statement;
	 * <br>decrement ifAndForCounter
	 * <br> fixup all jump addresses to current pc
	 */
	public void visit(MatchedIfStatement mis) {
		
		ifAndForCounter--;
		
		controlStructuresTypes.remove(ifAndForCounter);
		
		//address fixup
		
		for (Integer address : controlStructuresExprJumpFixupAddresses.get(ifAndForCounter))
			Code.fixup(address);
		
		controlStructuresExprJumpFixupAddresses.remove(ifAndForCounter);
		
	}
	
	/**Unmatched if statement;
	 * <br>decrement ifAndForCounter
	 * <br> fixup all jump addresses to current pc
	 */
	public void visit(UnmatchedIfStatement uis) {
		
		ifAndForCounter--;
		
		controlStructuresTypes.remove(ifAndForCounter);
		
		//address fixup
		
		for (Integer address : controlStructuresExprJumpFixupAddresses.get(ifAndForCounter))
			Code.fixup(address);
		
		controlStructuresExprJumpFixupAddresses.remove(ifAndForCounter);
		
	}
	
	/**Unmatched else statement;
	 * <br>decrement ifAndForCounter
	 * <br> fixup all jump addresses to current pc
	 */
	public void visit(UnmatchedElseStatement ues) {
		
		ifAndForCounter--;
		
		controlStructuresTypes.remove(ifAndForCounter);
		
		//address fixup
		
		for (Integer address : controlStructuresExprJumpFixupAddresses.get(ifAndForCounter))
			Code.fixup(address);
		
		controlStructuresExprJumpFixupAddresses.remove(ifAndForCounter);
		
	}
	
	/**Matched for statement;
	 * <br>decrement ifAndForCounter
	 * <br> put jump to last statement
	 * <br>remove first and last element from list; do fixups
	 */
	public void visit(MatchedForStatement mfs) {
		
		ifAndForCounter--;
		
		controlStructuresTypes.remove(ifAndForCounter);
		
		Code.putJump(
				controlStructuresExprJumpFixupAddresses.get(ifAndForCounter).remove(
						controlStructuresExprJumpFixupAddresses.get(ifAndForCounter).size() - 1));
		
		controlStructuresExprJumpFixupAddresses.get(ifAndForCounter).remove(0);
		
		// address fixup
		for (Integer address : controlStructuresExprJumpFixupAddresses.get(ifAndForCounter))
			Code.fixup(address);
		
		controlStructuresExprJumpFixupAddresses.remove(ifAndForCounter);
		
		// break fixup
		
		for (Integer address : breakStatementsAddresses.get(breakStatementsAddresses.size() - 1))
			Code.fixup(address);
		
		breakStatementsAddresses.remove(breakStatementsAddresses.size() - 1);
		
	}
	
	/**Unmatched for statement;
	 * <br>decrement ifAndForCounter
	 * <br> put jump to last statement
	 * <br>remove first and last element from list; do fixups
	 */
	public void visit(UnmatchedForStatement ufs) {
		
		ifAndForCounter--;
		
		controlStructuresTypes.remove(ifAndForCounter);
		
		Code.putJump(
				controlStructuresExprJumpFixupAddresses.get(ifAndForCounter).remove(
						controlStructuresExprJumpFixupAddresses.get(ifAndForCounter).size() - 1));
		
		controlStructuresExprJumpFixupAddresses.get(ifAndForCounter).remove(0);
		
		// address fixup
		for (Integer address : controlStructuresExprJumpFixupAddresses.get(ifAndForCounter))
			Code.fixup(address);
		
		controlStructuresExprJumpFixupAddresses.remove(ifAndForCounter);
		
		// break fixup
		
		for (Integer address : breakStatementsAddresses.get(breakStatementsAddresses.size() - 1))
			Code.fixup(address);
		
		breakStatementsAddresses.remove(breakStatementsAddresses.size() - 1);
		
	}
	
	/**Matched foreach statement;
	 * <br>decrement ifAndForCounter
	 * <br> put uncoditional jump back to condition of loop
	 * <br> fixup foreach condition and break jumps
	 * <br> remove adr and ind from expr stack
	 */
	public void visit(MatchedForeachStatement mfs) {
		
		ifAndForCounter--;
		
		controlStructuresTypes.remove(ifAndForCounter);
		
		Code.putJump(controlStructuresExprJumpFixupAddresses.get(ifAndForCounter).get(0));
		
		//fixup
		Code.fixup(controlStructuresExprJumpFixupAddresses.get(ifAndForCounter).get(1));
		
		controlStructuresExprJumpFixupAddresses.remove(ifAndForCounter);
		
		// break fixup
		
		for (Integer address : breakStatementsAddresses.get(breakStatementsAddresses.size() - 1))
			Code.fixup(address);
		
		breakStatementsAddresses.remove(breakStatementsAddresses.size() - 1);
		
		Code.put(Code.pop); Code.put(Code.pop);
		
		if (mfs.getForeachArray().myobjimpl.getKind() == Obj.Fld) Code.put(Code.pop);
		
		foreachIdent.remove(foreachIdent.size() - 1);
		
	}
	
	/**Unmatched foreach statement;
	 * <br>decrement ifAndForCounter
	 * <br> put uncoditional jump back to condition of loop
	 * <br> fixup foreach condition and break jumps
	 * <br> remove adr and ind from expr stack
	 */
	public void visit(UnmatchedForeachStatement ufs) {
		
		ifAndForCounter--;
		
		controlStructuresTypes.remove(ifAndForCounter);
		
		Code.putJump(controlStructuresExprJumpFixupAddresses.get(ifAndForCounter).get(0));
		
		//fixup
		Code.fixup(controlStructuresExprJumpFixupAddresses.get(ifAndForCounter).get(1));
		
		controlStructuresExprJumpFixupAddresses.remove(ifAndForCounter);
		
		// break fixup
		
		for (Integer address : breakStatementsAddresses.get(breakStatementsAddresses.size() - 1))
			Code.fixup(address);
		
		breakStatementsAddresses.remove(breakStatementsAddresses.size() - 1);
		
		Code.put(Code.pop); Code.put(Code.pop);
		
		if (ufs.getForeachArray().myobjimpl.getKind() == Obj.Fld) Code.put(Code.pop);
		foreachIdent.remove(foreachIdent.size() - 1);
		
	}
	
	/** Break Statement; breaks nearest for/foreach loop;
	 * <br> put false jump and fix address to list
	 */
	public void visit(BreakStatement bs) {
		
		Code.putJump(0);
		
		breakStatementsAddresses.get(breakStatementsAddresses.size() - 1).add(Code.pc - 2);
		
	}
	
	/** Continue statement; jumps to last designator statement in for loop or to next iteration of foreach loop
	 * <br> find closest for/foreach loop;
	 * <br> if for closest, put jump to last address in controlStructuresExprJumpFixupAddresses
	 * <br> if foreach closest, TODO
	 */
	public void visit(ContinueStatement cs) {
		
		
		for (int i = controlStructuresTypes.size() - 1 ; i >= 0; i-- ) {
			
			if (controlStructuresTypes.get(i) == ConditionCodeGenerator.MatchedFor
					|| controlStructuresTypes.get(i) == ConditionCodeGenerator.UnmatchedFor) {
				
				Code.putJump(controlStructuresExprJumpFixupAddresses.get(i).
								get (controlStructuresExprJumpFixupAddresses.get(i).size() - 1));
				
			}
			
			else if (controlStructuresTypes.get(i) == ConditionCodeGenerator.MatchedForeach
					|| controlStructuresTypes.get(i) == ConditionCodeGenerator.UnmatchedForeach) {
				
			}
			
		}
		
	}

}
