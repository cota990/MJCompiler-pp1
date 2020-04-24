package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.report.MyDumpSymbolTableVisitor;
import rs.ac.bg.etf.pp1.semantic.GlobalDeclarationsSemanticVisitor;
import rs.ac.bg.etf.pp1.semantic.GlobalMethodsSemanticVisitor;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class SemanticAnalyzer extends VisitorAdaptor {
	
	Logger log = Logger.getLogger(SemanticAnalyzer.class);
	
	
	private Boolean semanticErrorFound = false;
	
	private Boolean mainFound = false;
	
	private MyDumpSymbolTableVisitor stv = new MyDumpSymbolTableVisitor ();
	
	public Boolean semanticErrorFound () {
		return semanticErrorFound;
	}
	
	public void setSemanticErrorFound () {
		semanticErrorFound = true;
	}
	
	public Boolean mainFound () {
		
		return mainFound;
		
	}
	
	/**
	 * 
	 * Main program obj node processing 
	 * Neterminal ProgramName is used as obj which is inserted in symbol table;
	 * that obj is later used to chainLocalSymbols and close scope
	 * 
	 */
	
	public void visit(ProgramName programName) { 
		
		programName.myobjimpl = MyTabImpl.insert(Obj.Prog, programName.getProgramName(), MyTabImpl.noType);
    	MyTabImpl.openScope();
    	
    	GlobalDeclarationsSemanticVisitor globalDeclarationsSemanticVisitor = new GlobalDeclarationsSemanticVisitor ();
		((Program) programName.getParent()).getGlobalDeclarationsList().traverseBottomUp(globalDeclarationsSemanticVisitor);
		
		GlobalMethodsSemanticVisitor globalMethodsSemanticVisitor = new GlobalMethodsSemanticVisitor();
		((Program) programName.getParent()).getGlobalMethodDeclarationsList().traverseBottomUp(globalMethodsSemanticVisitor);
		
		mainFound = globalMethodsSemanticVisitor.isMainFound();
		
		if (globalDeclarationsSemanticVisitor.getSemanticErrorFound())
			semanticErrorFound = true;
    	
	}
	
	public void visit(Program program) { 
		
		MyTabImpl.chainLocalSymbols(program.getProgramName().myobjimpl);
		MyTabImpl.closeScope();
		
	}
		
    /*
     * 
     * statements
     * 
     */
    
    /*
     * 
     * condition terms
     * 
     */
    
    /*
     * 
     * SingleFactTerm;
     * context check: CondTerm = CondFact
     * 		CondFact must be bool (already checked)
     * 
     */
    public void visit (SingleFactTerm SingleFactTerm) {
    	
    	SingleFactTerm.mystructimpl = SingleFactTerm.getCondFact().mystructimpl;
    	
    }
    
    /*
     * 
     * MultipleFactTerm; must check if both CondTerm and CondFact are not null
     * 
     */
    public void visit (MultipleFactTerm MultipleFactTerm) {
    	
    	if (MultipleFactTerm.getCondTerm().mystructimpl != null
    			&& MultipleFactTerm.getCondFact().mystructimpl != null)
    		MultipleFactTerm.mystructimpl = MultipleFactTerm.getCondTerm().mystructimpl;
    	
    	else
    		MultipleFactTerm.mystructimpl = null;
    	
    }
    
    /*
     * 
     * condition factors
     * 
     */
    
    /*
     * 
     * SingleExprFact;
     * context check: CondFact = Expr
     * 		Expr must be bool
     * 
     */
    public void visit (SingleExprFact SingleExprFact) {
    	
    	if (SingleExprFact.getExpr().mystructimpl != null) {
    		
    		if (SingleExprFact.getExpr().mystructimpl.getKind() != Struct.Bool) {
    			
    			log.error("Semantic error on line " + SingleExprFact.getExpr().getLine() + ": single expression in condition factor must be bool");
				semanticErrorFound = true;
				SingleExprFact.mystructimpl = null;
				
    		}
    		
    		else
    			SingleExprFact.mystructimpl = SingleExprFact.getExpr().mystructimpl;
    		
    	}
    	
    	else
    		SingleExprFact.mystructimpl = null;
    	
    }
    
    /*
     * 
     * MultipleExprFact;
     * context check: CondFact = Expr Relop Expr
     * 		both Expr must be compatible
     * 		if Expr type is Class or Array, then Relop must be == or !=
     * 
     */
    public void visit (MultipleExprFact MultipleExprFact) {
    	
    	if (MultipleExprFact.getFirstExpr().mystructimpl != null
    			&& MultipleExprFact.getSecondExpr().mystructimpl != null) {
    		
    		if (!MultipleExprFact.getFirstExpr().mystructimpl
    				.compatibleWith(MultipleExprFact.getSecondExpr().mystructimpl)
    				||
    				(
						((MultipleExprFact.getFirstExpr().mystructimpl.getKind() == Struct.Class
							|| MultipleExprFact.getFirstExpr().mystructimpl.getKind() == Struct.Array)
						||
						(MultipleExprFact.getSecondExpr().mystructimpl.getKind() == Struct.Class
							|| MultipleExprFact.getSecondExpr().mystructimpl.getKind() == Struct.Array))
						&& !(MultipleExprFact.getRelop() instanceof Equals
								|| MultipleExprFact.getRelop() instanceof NotEquals))) {
    			
    			if (!MultipleExprFact.getFirstExpr().mystructimpl
        				.compatibleWith(MultipleExprFact.getSecondExpr().mystructimpl)) {
        			
        			log.error("Semantic error on line " + MultipleExprFact.getFirstExpr().getLine() + ": types of expressions for relational operator are not compatible");
    				semanticErrorFound = true;
        			
        		}
    			
    			if (
					((MultipleExprFact.getFirstExpr().mystructimpl.getKind() == Struct.Class
						|| MultipleExprFact.getFirstExpr().mystructimpl.getKind() == Struct.Array)
					||
					(MultipleExprFact.getSecondExpr().mystructimpl.getKind() == Struct.Class
						|| MultipleExprFact.getSecondExpr().mystructimpl.getKind() == Struct.Array))
					&& !(MultipleExprFact.getRelop() instanceof Equals
							|| MultipleExprFact.getRelop() instanceof NotEquals)) {
    				
    				log.error("Semantic error on line " + MultipleExprFact.getFirstExpr().getLine() + ": classes and arays can only be compared with != or == operators");
    				semanticErrorFound = true;
    				    				
    			}
    			
    			MultipleExprFact.mystructimpl = null;
    			
    		}
    		
    		else 
    			MultipleExprFact.mystructimpl = MyTabImpl.boolType;
    		
    	}
    	
    	else
    		MultipleExprFact.mystructimpl = null;
    	
    }
    
    /*
     * 
     * FirstExpr
     * 
     */
    public void visit (FirstExpr FirstExpr) {
    	
    	FirstExpr.mystructimpl = FirstExpr.getExpr().mystructimpl;
    	
    }
    
    /*
     * 
     * SecondExpr
     * 
     */
    public void visit (SecondExpr SecondExpr) {
    	
    	SecondExpr.mystructimpl = SecondExpr.getExpr().mystructimpl;
    	
    }
    
    /*
     * 
     * expressions
     * 
     */
    
    
    /*
     * 
     * ExprWithAssign;
     * context check: Destination AddopRight|MulopRight Expr
     * 		Destination must be Var, Elem or Fld
     * 		Destination and Expr must be int type
     */
    public void visit (ExprWithAssign ExprWithAssign) {
    	
    	if (ExprWithAssign.getDestination().myobjimpl != null
    			&& ExprWithAssign.getExpr().mystructimpl != null) {
    		
    		if ((ExprWithAssign.getDestination().myobjimpl.getKind() != Obj.Var
    				&& ExprWithAssign.getDestination().myobjimpl.getKind() != Obj.Fld
    				&& ExprWithAssign.getDestination().myobjimpl.getKind() != Obj.Elem)
    				|| (ExprWithAssign.getDestination().myobjimpl.getType().getKind() != Struct.Int
    						|| ExprWithAssign.getExpr().mystructimpl.getKind() != Struct.Int)) {
    			
    			if (ExprWithAssign.getDestination().myobjimpl.getKind() != Obj.Var
        				&& ExprWithAssign.getDestination().myobjimpl.getKind() != Obj.Fld
        				&& ExprWithAssign.getDestination().myobjimpl.getKind() != Obj.Elem) {
    				
    				log.error("Semantic error on line " + ExprWithAssign.getDestination().getLine() + ": designator before combined assign operator must be variable, array element or class field");
    				semanticErrorFound = true;
    				
    			}
    			
    			if (ExprWithAssign.getDestination().myobjimpl.getType().getKind() != Struct.Int
						|| ExprWithAssign.getExpr().mystructimpl.getKind() != Struct.Int) {
    				
    				if (ExprWithAssign.getDestination().myobjimpl.getType().getKind() != Struct.Int) {
    					
    					log.error("Semantic error on line " + ExprWithAssign.getDestination().getLine() + ": designator before combined assign operator must be int");
        				semanticErrorFound = true;
        				
    				}
    				
    				if (ExprWithAssign.getExpr().mystructimpl.getKind() != Struct.Int) {
    					
    					log.error("Semantic error on line " + ExprWithAssign.getExpr().getLine() + ": expression after combined assign operator must be int");
        				semanticErrorFound = true;
        				
    				}
    				
    			}
    			
    			ExprWithAssign.mystructimpl = null;
    			
    		}
    		
    		else
    			ExprWithAssign.mystructimpl = ExprWithAssign.getExpr().mystructimpl;
    		
    	}
    	
    	else
    		ExprWithAssign.mystructimpl = null;
    	
    }
    
    
    /*
     * 
     * ExprWithoutAssign
     * 
     */
    public void visit (ExprWithoutAssign ExprWithoutAssign) {
    	
    	ExprWithoutAssign.mystructimpl = ExprWithoutAssign.getNoAssignExpr().mystructimpl;
    	
    }
    
    
    /*
     * 
     * SingleTermExpr; no context checks
     * 
     */
    public void visit (SingleTermExpr SingleTermExpr) {
    	
    	SingleTermExpr.mystructimpl = SingleTermExpr.getTerm().mystructimpl;
    	
    }
    
    
    /*
     * 
     * MinusTermExpr;
     * context check: MINUS Term
     * 		Term must be int 
     * 
     */    
    public void visit (MinusTermExpr MinusTermExpr) {
    	
    	if (MinusTermExpr.getTerm().mystructimpl != null) {
    		
    		if (MinusTermExpr.getTerm().mystructimpl.getKind() != Struct.Int) {
    			
    			log.error("Semantic error on line " + MinusTermExpr.getTerm().getLine() + ": term after negation operator must be int");
				semanticErrorFound = true;
				MinusTermExpr.mystructimpl = null;
				
    		}
    		
    		else
    			MinusTermExpr.mystructimpl = MinusTermExpr.getTerm().mystructimpl;
    		
    	}
    	
    	else
    		MinusTermExpr.mystructimpl = null;
    	
    }
    
    
    /*
     * 
     * MultipleTermExpr;
     * context check: Expr AddopLeft Term
     * 		Expr and Term must be int
     * 
     */
    public void visit (MultipleTermExpr MultipleTermExpr) {
    	
    	if (MultipleTermExpr.getNoAssignExpr().mystructimpl != null
    			&& MultipleTermExpr.getTerm().mystructimpl != null) {
    		
    		if (MultipleTermExpr.getNoAssignExpr().mystructimpl.getKind() != Struct.Int
    				|| MultipleTermExpr.getTerm().mystructimpl.getKind() != Struct.Int) {
    			
    			if (MultipleTermExpr.getTerm().mystructimpl.getKind() != Struct.Int) {
    				
    				log.error("Semantic error on line " + MultipleTermExpr.getTerm().getLine() + ": term after addition operator must be int");
    				semanticErrorFound = true;
    				    				
    			}
    			
    			if (MultipleTermExpr.getNoAssignExpr().mystructimpl.getKind() != Struct.Int) {
    				
    				log.error("Semantic error on line " + MultipleTermExpr.getNoAssignExpr().getLine() + ": expr before addition operator must be int");
    				semanticErrorFound = true;
    				    				
    			}
    			
    			MultipleTermExpr.mystructimpl = null;
    			
    		}
    		
    	}
    	
    	else
    		MultipleTermExpr.mystructimpl = null;
    	
    }

   
//    
//    public void visit (ActPar ActPar) {
//    	
//    	if (!leftAssociation.isEmpty()) leftAssociation.remove(leftAssociation.size() - 1);
//    	leftAssociation.add(false);
//    	
//    	SyntaxNode parent = ActPar.getParent();
//    	MyObjImpl meth = null;
//    	
//    	while (parent != null) {
//    		
//
//    		//log.info(parent.getClass());
//    		parent = parent.getParent();
//    		//log.info(parent.getClass());
//    		    		
//    		if (parent instanceof MethodDesignator) {
//    			 
//    			parent = parent.getParent();
//    			//log.info(parent.getClass());
//    			
//    			if (parent instanceof SingleFactorTerm) {
//    				
//    				SingleFactorTerm sft = (SingleFactorTerm) parent;
//    				
//    				if (sft.getFactor() instanceof MethodDesignator) {
//	    				
//    					MethodDesignator md = (MethodDesignator) sft.getFactor();
//	    				if (md.getDesignator().obj != null) 
//	    					
//	    					if (md.getDesignator().obj instanceof MyObjImpl) 
//	    						
//	    						meth = (MyObjImpl) md.getDesignator().obj;
//    				
//    				}
//    			
//    			}
//    				
//    			else if (parent instanceof  MultipleFactorTerm) {
//    				
//    				MultipleFactorTerm mft = (MultipleFactorTerm) parent;
//    				
//    				if (mft.getFactor() instanceof MethodDesignator) {
//	    				
//    					MethodDesignator md = (MethodDesignator) mft.getFactor();
//	    				if (md.getDesignator().obj != null) 
//	    					
//	    					if (md.getDesignator().obj instanceof MyObjImpl) 
//	    						
//	    						meth = (MyObjImpl) md.getDesignator().obj;
//    				
//    				}
//    				
//    			}
//    			 
//    			break;
//    		
//    		}
//    		
//    		else if (parent instanceof MethodCallStatement) {
//    			
//    			MethodCallStatement mcs = (MethodCallStatement) parent;
//    			
//    			if (mcs.getDestination().obj != null) 
//					
//					if (mcs.getDestination().obj instanceof MyObjImpl) 
//						
//						meth = (MyObjImpl) mcs.getDestination().obj;
//    			
//    			break;
//    			
//    		}
//    		
//    	}
//    	
//    	//log.info(meth == null);
//    	//log.info(meth.getActParamsProcessed());
//    	//log.info(meth.getLevel());
//    	if (meth.getActParamsProcessed() >= meth.getLevel()) {
//    		
//    		log.error("Semantic error on line " + ActPar.getLine() + ": number of actual and formal parameters does not match (more actual than formal parameters)");
//    		semanticErrorFound = true;
//    		
//    	}
//    	
//    	else if (ActPar.getExpr().obj != null) {
//    		
//    		for (Obj obj : meth.getLocalSymbols()) {
//    			
//    			if (obj.getFpPos() == meth.getActParamsProcessed() && !obj.getName().equals("this")) {
//    				
//    				boolean assignable = false;
//    				
//    				if (!ActPar.getExpr().obj.getType().assignableTo(obj.getType())) {
//    	    			
//    	    			if (ActPar.getExpr().obj.getType().getKind() == Struct.Class && ActPar.getExpr().obj.getType().getElemType() != null) {
//    	    				
//    	    				Struct parentClass = ActPar.getExpr().obj.getType().getElemType();
//    	    				    				
//    	    				while (parentClass != null) {
//    	    					
//    	    					if (parentClass.assignableTo(obj.getType())) {
//    	    						
//    	    						assignable = true;
//    	    						break;
//    	    						
//    	    					}
//    	    					
//    	    					else
//    	    						parentClass = parentClass.getElemType();
//    	    						
//    	    				}
//    	    			}
//    	    			
//    	    			if (!assignable) {
//    		    			
//    	    				log.error("Semantic error on line " + ActPar.getLine() + ": actual parameter " + (meth.getActParamsProcessed() + 1) + " is not compatible with formal parameter");
//    		        		semanticErrorFound = true;
//    	        		
//    	    			}
//    	        		
//    	    		}
//    				break;
//    				
//    			}
//    		}
//    		
//    	}
//    	
//    	meth.setActParamsProcessed(meth.getActParamsProcessed() + 1);
//    	
//    	
//    	
//    }
//    
//    /* statements */
//    
//    public void visit (Destination Destination) {
//    	
//    	Destination.obj = Destination.getDesignator().obj;
//    	
//    	leftAssociation.add(false);
//    	
//    }
//    
//    public void visit (Source Source) {
//    	
//    	if (Source.getExpr().obj != null)
//    		Source.struct = Source.getExpr().obj.getType();
//    	
//    	else Source.struct = null;
//    	
//    	if (!leftAssociation.isEmpty()) leftAssociation.remove(leftAssociation.size() - 1);
//    	
//    }
//    
//    public void visit (AssignStatementSuccess AssignStatementSuccess) {
//    	
//    	if (AssignStatementSuccess.getDestination().obj != null && AssignStatementSuccess.getSource().struct != null) {
//    		
//    		boolean assignable = false;
//    		
//    		if (AssignStatementSuccess.getDestination().obj.getKind() != Obj.Var &&
//    				AssignStatementSuccess.getDestination().obj.getKind() != Obj.Elem &&
//    				AssignStatementSuccess.getDestination().obj.getKind() != Obj.Fld) {
//    			
//    			log.error("Semantic error on line " + AssignStatementSuccess.getLine() + ": assign destination must be variable, array element or class field");
//        		semanticErrorFound = true;
//        		
//    		}
//    		
//    		//log.info(AssignStatementSuccess.getSource().struct.getKind());
//    		//log.info(AssignStatementSuccess.getDestination().obj.getType().getKind());
//    		//log.info(AssignStatementSuccess.getSource().struct.equals(AssignStatementSuccess.getDestination().obj.getType()));
//    		//log.info(AssignStatementSuccess.getSource().struct.getClass());
//    		//log.info(AssignStatementSuccess.getDestination().obj.getType().getClass());
//    		
//    		if (!AssignStatementSuccess.getSource().struct.assignableTo(AssignStatementSuccess.getDestination().obj.getType())) {
//    			
//    			if (AssignStatementSuccess.getSource().struct.getKind() == Struct.Class && AssignStatementSuccess.getSource().struct.getElemType() != null) {
//    				
//    				Struct parentClass = AssignStatementSuccess.getSource().struct.getElemType();
//    				    				
//    				while (parentClass != null) {
//    					
//    					if (parentClass.assignableTo(AssignStatementSuccess.getDestination().obj.getType())) {
//    						
//    						assignable = true;
//    						break;
//    						
//    					}
//    					
//    					else
//    						parentClass = parentClass.getElemType();
//    						
//    				}
//    			}
//    			
//    			if (!assignable) {
//	    			
//    				log.error("Semantic error on line " + AssignStatementSuccess.getLine() + ": source type is not assignable to destination type");
//	        		semanticErrorFound = true;
//        		
//    			}
//        		
//    		}
//    	}
//    }
//    
//    public void visit (IncrementStatement IncrementStatement) {
//    	
//    	SyntaxNode parent = IncrementStatement.getParent();
//    	while (parent != null) {
//    		
//    		//log.info(parent.getClass());
//    		parent = parent.getParent();
//    		
//    	}
//    	
//    	if (IncrementStatement.getDestination().obj != null) {
//    		
//    		if (IncrementStatement.getDestination().obj.getKind() != Obj.Var &&
//    				IncrementStatement.getDestination().obj.getKind() != Obj.Elem &&
//    						IncrementStatement.getDestination().obj.getKind() != Obj.Fld) {
//    			
//    			log.error("Semantic error on line " + IncrementStatement.getLine() + ": increment statement destination must be variable, array element or class field");
//        		semanticErrorFound = true;
//        		
//    		}
//    		
//    		if (IncrementStatement.getDestination().obj.getType() != MyTabImpl.intType) {
//    			
//    			log.error("Semantic error on line " + IncrementStatement.getLine() + ": increment statement destination must be int type");
//        		semanticErrorFound = true;
//        		
//    		}
//    		
//    	}
//    	
//    }
//    
//    public void visit (DecrementStatement DecrementStatement) {
//    	
//    	SyntaxNode parent = DecrementStatement.getParent();
//    	while (parent != null) {
//    		
//    		//log.info(parent.getClass());
//    		parent = parent.getParent();
//    		
//    	}
//    	
//    	if (DecrementStatement.getDestination().obj != null) {
//    		
//    		if (DecrementStatement.getDestination().obj.getKind() != Obj.Var &&
//    				DecrementStatement.getDestination().obj.getKind() != Obj.Elem &&
//    						DecrementStatement.getDestination().obj.getKind() != Obj.Fld) {
//    			
//    			log.error("Semantic error on line " + DecrementStatement.getLine() + ": decrement statement destination must be variable, array element or class field");
//        		semanticErrorFound = true;
//        		
//    		}
//    		
//    		if (DecrementStatement.getDestination().obj.getType() != MyTabImpl.intType) {
//    			
//    			log.error("Semantic error on line " + DecrementStatement.getLine() + ": decrement statement destination must be int type");
//        		semanticErrorFound = true;
//        		
//    		}
//    	}
//
//    }
//    
//    public void visit (MethodCallStatement MethodCallStatement) {
//    	
//    	if (MethodCallStatement.getDestination().obj != null) {
//    		
//    		if (MethodCallStatement.getDestination().obj.getKind() != Obj.Meth) {
//    			
//    			log.error("Semantic error on line " + MethodCallStatement.getLine() + ": designator must be global or class method");
//        		semanticErrorFound = true;
//        		
//    		}
//    		
//    		else {
//    			
//    			if (MethodCallStatement.getDestination().obj instanceof MyObjImpl) {
//    				
//    				if (((MyObjImpl)MethodCallStatement.getDestination().obj).getActParamsProcessed() < MethodCallStatement.getDestination().obj.getLevel()) {
//    					
//    					log.error("Semantic error on line " + MethodCallStatement.getDestination().getLine() + ": number of actual and formal parameters does not match (less actual than formal parameters) ");
//    					semanticErrorFound = true;
//    					
//    				}
//    				
//    				((MyObjImpl)MethodCallStatement.getDestination().obj).setActParamsProcessed(0);
//    			}
//    		}
//    		
//    	}
//    	
//    }
//    
//    public void visit (BreakStatement BreakStatement) {
//    	
//    	SyntaxNode parent = BreakStatement.getParent();
//    	boolean forFound = false;
//    	while (parent != null && !forFound) {
//    		
//    		if (!(parent instanceof MatchedForStatement || parent instanceof UnmatchedForStatement 
//    				|| parent instanceof MatchedForEachStatement || parent instanceof UnmatchedForEachStatement))
//    			parent = parent.getParent();
//    		
//    		else     			
//    			forFound = true;
//    		
//    	}
//    	
//    	if (!forFound) {
//    		
//    		log.error("Semantic error on line " + BreakStatement.getParent().getLine() + ": break statement must be inside for loop");
//    		semanticErrorFound = true;
//    		
//    	}
//    	
//    }
//    
//    public void visit (ContinueStatement ContinueStatement) {
//    	
//    	SyntaxNode parent = ContinueStatement.getParent();
//    	boolean forFound = false;
//    	while (parent != null && !forFound) {
//    		
//    		if (!(parent instanceof MatchedForStatement || parent instanceof UnmatchedForStatement 
//    				|| parent instanceof MatchedForEachStatement || parent instanceof UnmatchedForEachStatement))
//    			parent = parent.getParent();
//    		
//    		else     			
//    			forFound = true;
//    		
//    	}
//    	
//    	if (!forFound) {
//    		
//    		log.error("Semantic error on line " + ContinueStatement.getParent().getLine() + ": continue statement must be inside for loop");
//    		semanticErrorFound = true;
//    		
//    	}
//    	
//    }
//    
//    public void visit (ReadStatement ReadStatement) {
//    	
//    	if (ReadStatement.getDesignator().obj != null) {
//    		
//    		if (ReadStatement.getDesignator().obj.getKind() != Obj.Var &&
//    				ReadStatement.getDesignator().obj.getKind() != Obj.Elem &&
//    						ReadStatement.getDesignator().obj.getKind() != Obj.Fld) {
//    			
//    			log.error("Semantic error on line " + ReadStatement.getLine() + ": designator in read statement must be variable, array element or class field");
//        		semanticErrorFound = true;
//        		
//    		}
//    		
//    		if (ReadStatement.getDesignator().obj.getType() != MyTabImpl.intType &&
//    				ReadStatement.getDesignator().obj.getType() != MyTabImpl.charType &&
//    				ReadStatement.getDesignator().obj.getType() != MyTabImpl.boolType) {
//    			
//    			log.error("Semantic error on line " + ReadStatement.getLine() + ": designator in read statement must be int, char or bool type");
//        		semanticErrorFound = true;
//        		
//    		}
//    	}
//    }
//    
//    public void visit (PrintStatement PrintStatement) {
//    	
//    	if (PrintStatement.getExpr().obj != null) {
//    		
//    		if (PrintStatement.getExpr().obj.getType() != MyTabImpl.intType &&
//    				PrintStatement.getExpr().obj.getType() != MyTabImpl.charType &&
//    						PrintStatement.getExpr().obj.getType() != MyTabImpl.boolType) {
//    			
//    			log.error("Semantic error on line " + PrintStatement.getLine() + ": expression in print statement must be int, char or bool type");
//        		semanticErrorFound = true;
//        		
//    		}
//    		
//    	}
//    	
//    	if (!leftAssociation.isEmpty()) leftAssociation.remove(leftAssociation.size() - 1);
//    
//    }
//    
//    public void visit (ReturnStatement ReturnStatement) {
//    	
//    	SyntaxNode parent = ReturnStatement.getParent();
//    	
//    	if (!leftAssociation.isEmpty()) leftAssociation.remove(leftAssociation.size() - 1);
//    	
//    	while (parent != null) {
//    		
//    		//log.info(parent.getClass());
//    		parent = parent.getParent();
//    		
//    		if (parent instanceof MethodDeclSuccess) {
//    			
//    			MethodDeclSuccess mds = (MethodDeclSuccess) parent;
//    			
//    			//log.info("obradjuje return statement za " + mds.getMethodName().obj.getName());
//    			
//    			if (mds.getMethodName().obj.getType() == Tab.noType && ReturnStatement.getReturnExprOption() instanceof ReturnExpr) {
//    				
//    				log.error("Semantic error on line " + ReturnStatement.getLine() + ": return statement can't have expression for void methods");
//    				semanticErrorFound = true;
//    				
//    			}
//    			
//    			else if (mds.getMethodName().obj.getType() != Tab.noType && ReturnStatement.getReturnExprOption() instanceof NoReturnExpr) {
//    				
//    				log.error("Semantic error on line " + ReturnStatement.getLine() + ": return statement must have return expression for non-void methods");
//    				semanticErrorFound = true;
//    				
//    			}
//    			
//    			else if (mds.getMethodName().obj.getType() != Tab.noType && ReturnStatement.getReturnExprOption() instanceof ReturnExpr) {
//    				
//    				ReturnExpr returnExpr = (ReturnExpr) ReturnStatement.getReturnExprOption();
//    				if (returnExpr.getExpr().obj != null && 
//    						!mds.getMethodName().obj.getType().equals(returnExpr.getExpr().obj.getType())) {
//    					
//    					log.error("Semantic error on line " + ReturnStatement.getLine() + ": return expression must be equal as delared return type of method");
//        				semanticErrorFound = true;
//        				
//    				}
//    				
//    				else {
//        				
//        				if (mds.getMethodName().obj instanceof MyObjImpl) {
//        					
//        					//log.info("Pronasao return za " + (mds.getMethodName().obj.getName()));
//        					((MyObjImpl) mds.getMethodName().obj).setReturnFound(true);
//        					
//        				}
//        				
//        			}
//    				
//    			}
//    			
//    		}
//    		
//    	}
//    }
//    
//    public void visit (IteratorName IteratorName) {
//    	
//    	Obj ident = MyTabImpl.find(IteratorName.getIteratorName());
//    	
//    	if (ident == Tab.noObj) {
//    		
//    		log.error("Semantic error on line " + IteratorName.getLine() + ": " + IteratorName.getIteratorName() + " is not declared");
//    		semanticErrorFound = true;
//    		
//    		IteratorName.obj = null;
//    		
//    	}
//    	
//    	else
//    		IteratorName.obj = ident;
//    }
//    
//    public void visit (MatchedForEachStatement MatchedForEachStatement) {
//    	
//    	if (MatchedForEachStatement.getForeachArray().getDesignator().obj != null && MatchedForEachStatement.getIteratorName().obj != null) {
//			
//			if (MatchedForEachStatement.getForeachArray().getDesignator().obj.getType().getKind() != Struct.Array
//					|| MatchedForEachStatement.getIteratorName().obj.getKind() != Obj.Var) {
//				
//				if (MatchedForEachStatement.getForeachArray().getDesignator().obj.getType().getKind() != Struct.Array) {
//					
//					log.error("Semantic error on line " + MatchedForEachStatement.getLine() + ": designator in foreach loop must be an array");
//		    		semanticErrorFound = true;
//		    		
//				}
//				
//				if (MatchedForEachStatement.getIteratorName().obj.getKind() != Obj.Var) {
//					
//					log.error("Semantic error on line " + MatchedForEachStatement.getLine() + ": identifier in foreach loop must be global or local variable");
//		    		semanticErrorFound = true;
//		    		
//				}
//				
//			}
//			
//			else {
//				
//				if (!MatchedForEachStatement.getForeachArray().getDesignator().obj.getType().getElemType().compatibleWith(MatchedForEachStatement.getIteratorName().obj.getType())) {
//					
//					log.error("Semantic error on line " + MatchedForEachStatement.getLine() + ": identifier in foreach loop must be same type as elements of designator array");
//		    		semanticErrorFound = true;
//		    		
//				}
//			}
//		}
//	}
//    
//    
//    /* conditions, condition terms and condition factors */
//    
//    public void visit (SingleExprFact SingleExprFact) {
//    	
//    	if (SingleExprFact.getExpr().obj != null) {
//    		
//    		if (SingleExprFact.getExpr().obj.getType() != MyTabImpl.boolType) {
//    			
//    			log.error("Semantic error on line " + SingleExprFact.getLine() + ": expression must be bool type");
//        		semanticErrorFound = true;
//        		SingleExprFact.struct = null;
//        		
//    		}
//    		
//    		else
//    			SingleExprFact.struct = SingleExprFact.getExpr().obj.getType();
//    		
//    	}
//    	
//    	else
//    		SingleExprFact.struct = null;
//    	
//    	if (!leftAssociation.isEmpty()) leftAssociation.remove(leftAssociation.size() - 1);
//    	
//    }
//    
//    public void visit (MultipleExprFact MultipleExprFact) {
//    	
//    	if (MultipleExprFact.getFirstExpr().struct != null && MultipleExprFact.getSecondExpr().struct != null) {
//    		
//    		if (!MultipleExprFact.getFirstExpr().struct.compatibleWith(MultipleExprFact.getSecondExpr().struct)) {
//    			
//    			log.error("Semantic error on line " + MultipleExprFact.getLine() + ": expressions are not compatible");
//        		semanticErrorFound = true;
//        		MultipleExprFact.struct = null;
//        		
//    		}
//    		
//    		else if (MultipleExprFact.getFirstExpr().struct.getKind() == Struct.Array ||
//    				MultipleExprFact.getFirstExpr().struct.getKind() == Struct.Class) {
//    			
//    			if (! (MultipleExprFact.getRelop() instanceof Equals || MultipleExprFact.getRelop() instanceof NotEquals)) {
//    				
//    				log.error("Semantic error on line " + MultipleExprFact.getLine() + ": only != and == are allowed for class or array expressions");
//            		semanticErrorFound = true;
//            		MultipleExprFact.struct = null;
//            		
//    			}
//    			
//    			else
//    				MultipleExprFact.struct = MyTabImpl.boolType;
//    			
//    		}
//    		
//    		else 
//    			MultipleExprFact.struct = MyTabImpl.boolType;
//    		
//    	}
//    	
//    	else
//    		MultipleExprFact.struct = null;
//    	
//    }
//    
//    public void visit (SingleFactTerm SingleFactTerm) {
//    	
//    	SingleFactTerm.struct = SingleFactTerm.getCondFact().struct;
//    	
//    }
//    
//    public void visit (MultipleFactTerm MultipleFactTerm) {
//    	
//    	if (MultipleFactTerm.getCondTerm().struct != null && MultipleFactTerm.getCondFact().struct != null)
//    		MultipleFactTerm.struct = MultipleFactTerm.getCondTerm().struct;
//    	
//    	else
//    		MultipleFactTerm.struct = null;
//    	
//    }
//    
//    public void visit (SingleTermCondition SingleTermCondition) {
//    	
//    	SingleTermCondition.struct = SingleTermCondition.getCondTerm().struct;
//    	
//    }
//    
//    public void visit (MultipleTermCondition MultipleTermCondition) {
//    	
//    	if (MultipleTermCondition.getCondition().struct != null && MultipleTermCondition.getCondTerm().struct != null)
//    		MultipleTermCondition.struct = MultipleTermCondition.getCondition().struct;
//    	
//    	else
//    		MultipleTermCondition.struct = null;
//    	
//    }
//
}
