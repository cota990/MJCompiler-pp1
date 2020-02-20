package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.Tab;
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
	
	/* Main program obj node processing */
	
	public void visit(ProgramName ProgramName) { 
		
		ProgramName.obj = MyTabImpl.insert(Obj.Prog, ProgramName.getProgramName(), MyTabImpl.noType);
    	MyTabImpl.openScope();
    	//MyTabImpl.insert(Obj.Var, "foreach", MyTabImpl.intType);
    	
	}
	
	public void visit(Program Program) { 
		
		//MyTabImpl.insert(Obj.Var, "foreach", MyTabImpl.intType);
		MyTabImpl.chainLocalSymbols(Program.getProgramName().obj);
		MyTabImpl.closeScope();
		Program.obj = Program.getProgramName().obj;
		
	}
	
	/* Type processing */
	public void visit(Type Type) { 
		
		Obj type = MyTabImpl.find(Type.getTypeName());
		
		if (type == MyTabImpl.noObj) {
			
			log.error ("Semantic error on line " + Type.getLine() + ": " + Type.getTypeName() + " is not declared!");
    		semanticErrorFound = true;
    		Type.struct = null;
    		
		}
		
		else if (type.getKind() != Obj.Type) {
			
			log.error ("Semantic error on line " + Type.getLine() + ": " + Type.getTypeName() + " must be declared as type!");
    		semanticErrorFound = true;
    		Type.struct = null;
    		
		}
		else {
			
			Type.struct = type.getType();
			type.accept(stv);
			log.info("Usage of type: " + Type.getTypeName() + " on line: " + Type.getLine() + "\nSymbolTable output: " + stv.getOutput());
			
			stv = new MyDumpSymbolTableVisitor();
			
		}
	}
	
	/* global const declarations */
	public void visit(SingleConstDeclSuccess SingleConstDeclSuccess) { 
		
		SyntaxNode type = SingleConstDeclSuccess.getParent();
		
		while (type.getClass() != ConstDeclSuccess.class) type = type.getParent();
		
		Integer constValue = null;
		Struct constType = ((ConstDeclSuccess) type).getType().struct;
		
		if (constType != null) {
			
			if (SingleConstDeclSuccess.getConstValue() instanceof NumConst) {
				
				NumConst cnst = (NumConst) SingleConstDeclSuccess.getConstValue();
				
				if (constType.getKind() == Struct.Int)
					constValue = cnst.getNumberValue();
				
				else {
					
					log.error("Semantic error on line " + SingleConstDeclSuccess.getConstValue().getLine() + ": constant " + SingleConstDeclSuccess.getConstName() + " must be declared as INT type!");
					semanticErrorFound = true;
				
				}
			
			}
		
			else if (SingleConstDeclSuccess.getConstValue() instanceof CharConst) {
				
				CharConst cnst = (CharConst) SingleConstDeclSuccess.getConstValue();
				
				if (constType.getKind() == Struct.Char)
					constValue = Integer.valueOf(cnst.getCharValue());
				
				else {
					
					log.error("Semantic error on line " + SingleConstDeclSuccess.getConstValue().getLine() + ": constant " + SingleConstDeclSuccess.getConstName() + " must be declared as CHAR type!");
					semanticErrorFound = true;
				
				}
			
			}
		
			else if (SingleConstDeclSuccess.getConstValue() instanceof BoolConst) {
			
				BoolConst cnst = (BoolConst) SingleConstDeclSuccess.getConstValue();
				
				if (constType == MyTabImpl.boolType) {
					
					if (cnst.getBoolValue()) constValue = 1;
					else constValue = 0;
				
				}
				
				else {
					
					log.error("Semantic error on line " + SingleConstDeclSuccess.getConstValue().getLine() + ": constant " + SingleConstDeclSuccess.getConstName() + " must be declared as BOOL type!");
					semanticErrorFound = true;
				
				}
			
			}	
			
			if (constValue != null) {
				
				Obj constFound = MyTabImpl.find(SingleConstDeclSuccess.getConstName());
	    		
	    		
	    		if (constFound.equals(MyTabImpl.noObj)) {
	    			
	    			constFound = MyTabImpl.insert(Obj.Con, SingleConstDeclSuccess.getConstName(), constType);
	    			constFound.setAdr(constValue);
	    			constFound.accept(stv);
	    			log.info("Declared global constant: " + SingleConstDeclSuccess.getConstName() + " on line " + SingleConstDeclSuccess.getLine() + "\nSymbolTable output: " + stv.getOutput() );
	    			stv = new MyDumpSymbolTableVisitor();
	    		
	    		}
	    		
	    		else {
	    			
	    			log.error ("Semantic error on line " + SingleConstDeclSuccess.getLine() + ": " + SingleConstDeclSuccess.getConstName() + " already declared");
	    			semanticErrorFound = true;
	    			
	    		}
	    		
			}
			
		}
		
	}

    
    /* global variables declarations */
    public void visit(SingleVarDeclSuccess SingleVarDeclSuccess) { 
    	
    	SyntaxNode type = SingleVarDeclSuccess.getParent();
		
		while (type.getClass() != VarDeclSuccess.class) type = type.getParent();
		
		Struct varType = ((VarDeclSuccess) type).getType().struct;
    	
    	if (varType != null) {
    		
    		Obj varFound = MyTabImpl.find(SingleVarDeclSuccess.getVarName());
    		
    		if (varFound.equals(MyTabImpl.noObj)) {
    			
    			if (SingleVarDeclSuccess.getArrayOption() instanceof ArrayVariable) {
    				
    				varFound = MyTabImpl.insert(Obj.Var, SingleVarDeclSuccess.getVarName(), new Struct (Struct.Array, varType));
    				varFound.accept(stv);
        			log.info("Declared global array variable: " + SingleVarDeclSuccess.getVarName() + " on line " + SingleVarDeclSuccess.getLine() + "\nSymbolTable output: " + stv.getOutput());
        			stv = new MyDumpSymbolTableVisitor();
    			
    			}
    			
    			else if (SingleVarDeclSuccess.getArrayOption() instanceof NoArrayVariable) {
    				
    				varFound = MyTabImpl.insert(Obj.Var, SingleVarDeclSuccess.getVarName(), varType);
    				varFound.accept(stv);
    				log.info("Declared global variable: " + SingleVarDeclSuccess.getVarName() + " on line " + SingleVarDeclSuccess.getLine() + "\nSymbolTable output: " + stv.getOutput());
    				stv = new MyDumpSymbolTableVisitor();
    				
    			} 
    			    		
    		}
    		
    		else {
    			
    			log.error ("Semantic error on line " + SingleVarDeclSuccess.getLine() + ": " + SingleVarDeclSuccess.getVarName() + " already declared");
    			semanticErrorFound = true;
    			
    		}
    		
    	}
    	
    } 
    
    /* abstract class/regular class declarations */
    
    public void visit(AbstractClassName AbstractClassName) { 
    	
    	Obj classFound = MyTabImpl.find(AbstractClassName.getAbstractClassName());
    	
    	if (classFound == MyTabImpl.noObj) {
			
    		MyStructImpl newClassStruct = new MyStructImpl(new Struct (Struct.Class));
    		newClassStruct.setAbstract(true);
    		classFound = MyTabImpl.insert(Obj.Type, AbstractClassName.getAbstractClassName(), newClassStruct);
    		AbstractClassName.obj = classFound;
    		MyTabImpl.openScope();
    		
		}
		else {
			
			log.error ("Semantic error on line " + AbstractClassName.getLine() + ": " + AbstractClassName.getAbstractClassName() + " already declared");
			semanticErrorFound = true;
			AbstractClassName.obj = null;
			
		}
    	
    }
    
    public void visit(ClassName ClassName) { 
    	
    	Obj classFound = MyTabImpl.find(ClassName.getClassName());
    	
    	if (classFound == MyTabImpl.noObj) {
			
    		MyStructImpl newClassStruct = new MyStructImpl(new Struct (Struct.Class));
    		newClassStruct.setAbstract(false);
    		classFound = MyTabImpl.insert(Obj.Type, ClassName.getClassName(), newClassStruct);
    		ClassName.obj = classFound;
    		MyTabImpl.openScope();
    		
		}
		else {
			
			log.error ("Semantic error on line " + ClassName.getLine() + ": " + ClassName.getClassName() + " already declared");
			semanticErrorFound = true;
			ClassName.obj = null;
			
		}
    }
    
    public void visit(ClassDecl ClassDecl) { 
    	
    	if (ClassDecl.getClassName().obj != null) {    		
    		
    		// inheritance check; add to local symbols everything that's not overriden
    		if (ClassDecl.getExtendsOption() instanceof ClassInheritance) {
    			
    			if (((ClassInheritance)ClassDecl.getExtendsOption()).getExtendsSyntaxCheck() instanceof ClassInheritanceSuccess) {
    				
    				Struct parentClass = ((ClassInheritanceSuccess)((ClassInheritance)ClassDecl.getExtendsOption()).getExtendsSyntaxCheck()).getType().struct;
    		    	
    		    	if (parentClass != null) {
    		    		
    		    		ClassDecl.getClassName().obj.getType().setElementType(parentClass);
    		    		

    		    		if (parentClass instanceof MyStructImpl)
    		    			if (((MyStructImpl)parentClass).isAbstract()) {
    		    				
    		    				// ovde ide provera da li implementira sve apstraktne metode
    		    				
    		    				for (Obj parentClassObject : parentClass.getMembers()) {
    	    		    			
    	    		    			if (parentClassObject.getKind() == Obj.Fld) {
    	    	    				
    	    		    				if (MyTabImpl.currentScope.findSymbol(parentClassObject.getName()) == null)
    	    		    					MyTabImpl.insert(parentClassObject.getKind(), parentClassObject.getName(), parentClassObject.getType());
    	    		    				
    	    		    			}
    	    		    			
    	    		    			else if (parentClassObject.getKind() == Obj.Meth) {
    	    		    				
    	    		    				// TODO PROVERA METODA; ako je parent metoda obicna, sve ok; ako je apstraktna mora da implementira
    	    		    				
    	    		    				Obj methodFound = MyTabImpl.currentScope.findSymbol(parentClassObject.getName());
    	    		    				
    	    		    				if (methodFound == null) {
    	    		    					 
    	    		    					// not found; check if abstract; if abstract - error; if not abstract - inherit
    	    		    					
    	    		    					if (parentClassObject instanceof MyObjImpl) {
        	    		    					
        	    		    					if (((MyObjImpl)parentClassObject).isAbstract()) {
        	    		    						
        	    		    						log.error ("Semantic error on line " + ClassDecl.getLine() + ": " + ClassDecl.getClassName().getClassName() + " must implement inherited abstract method: " + parentClassObject.getName());
        	    		    						semanticErrorFound = true;
        	    		    					}
        	    		    					
        	    		    					else {
        	    		    						
        	    		    						MyTabImpl.currentScope.addToLocals(parentClassObject);    
        	    		    						
        	    		    					}
        	    		    					
        	    		    				}
    	    		    				}
    	    		    				
    	    		    				else {
    	    		    					
    	    		    					// method found; if abstract - check for signature; if not abstract - do nothing
    	    		    					
    	    		    					if (parentClassObject instanceof MyObjImpl) {
    	    		    						
    	    		    						if (((MyObjImpl)parentClassObject).isAbstract()) {
    	    		    							
    	    		    							if (methodFound.getKind() != Obj.Meth) {
    	    		    								
    	    		    								log.error ("Semantic error on line " + ClassDecl.getLine() + ": " + ClassDecl.getClassName().getClassName() + " must implement inherited abstract method: " + parentClassObject.getName() + " ; instead it has field with that name");
            	    		    						semanticErrorFound = true;
    	    		    							}
    	    		    							
    	    		    							else {
    	    		    								
    	    		    								//log.info(methodFound.getName() + " " + methodFound.getLevel());
    	    		    								//log.info(parentClassObject.getName() + " " + parentClassObject.getLevel());
    	    		    								
    	    		    								if (!methodFound.getType().equals(parentClassObject.getType()) 
    	    		    										|| methodFound.getLevel() != parentClassObject.getLevel() ) {
    	    		    								
	    	    		    								if (!methodFound.getType().equals(parentClassObject.getType())) {
	    	    		    									
	    	    		    									log.error ("Semantic error on line " + ClassDecl.getLine() + ": Implemnted method: " + parentClassObject.getName() + " does not have same return type as declared in abstract class");
	                	    		    						semanticErrorFound = true;
	                	    		    						
	    	    		    								}
	    	    		    								
	    	    		    								if (methodFound.getLevel() != parentClassObject.getLevel()) {
	    	    		    									
	    	    		    									log.error ("Semantic error on line " + ClassDecl.getLine() + ": Implemnted method: " + parentClassObject.getName() + " does not have same number of formal parameters as declared in abstract class");
	                	    		    						semanticErrorFound = true;
	                	    		    						
	    	    		    								}
	    	    		    								
    	    		    								}
    	    		    								
    	    		    								else {
    	    		    									
    	    		    									for (int i = 0; i < methodFound.getLevel(); i++) {
	    	    		    									
    	    		    										Obj abstractFormPar = null;
	    	    		    									Obj formPar = null;
	    	    		    									
	    	    		    									for (Obj obj : methodFound.getLocalSymbols()) {
	    	    		    										
	    	    		    										if (!obj.getName().equals("this") && obj.getKind() == Obj.Var && obj.getFpPos() == i) {
	    	    		    											
	    	    		    											formPar = obj;
	    	    		    											break;
	    	    		    											
	    	    		    										}
	    	    		    									}
	    	    		    									
    	    		    										for (Obj obj : parentClassObject.getLocalSymbols()) {
	    	    		    										
	    	    		    										if (obj.getKind() == Obj.Var && obj.getFpPos() == i) {
	    	    		    											
	    	    		    											abstractFormPar = obj;
	    	    		    											break;
	    	    		    											
	    	    		    										}
	    	    		    									}
    	    		    										
    	    		    										//log.info(abstractFormPar == null ? "null" : abstractFormPar.getName());
    	    		    										//log.info(formPar == null ? "null" : formPar.getName());
    	    		    										
    	    		    										if (abstractFormPar == null || formPar == null || 
    	    		    												abstractFormPar.getKind() != formPar.getKind() || !abstractFormPar.getName().equals(formPar.getName()) ||
    	    		    												!abstractFormPar.getType().equals(formPar.getType())) {
    	    		    											
    	    		    											log.error ("Semantic error on line " + ClassDecl.getLine() + ": Formal Parameter number: " + i + " of implemnted method: " + parentClassObject.getName() + " is not compatible with corresponding formal parameter declared in abstract class");
    	                	    		    						semanticErrorFound = true;
    	                	    		    						
    	    		    										}
	    	    		    									
    	    		    									}
    	    		    								}
    	    		    							}
    	    		    							
    	    		    						}
    	    		    						
    	    		    					}
    	    		    					
    	    		    				}
    	    		    			}
    	    		    		
    	    		    		}
    		    			
    		    			}
    		    			else {
    		    				
    		    				for (Obj parentClassObject : parentClass.getMembers()) {
		    		    			
    		    					if (parentClassObject.getKind() == Obj.Fld) {
        	    	    				
    	    		    				if (MyTabImpl.currentScope.findSymbol(parentClassObject.getName()) == null)
    	    		    					MyTabImpl.insert(parentClassObject.getKind(), parentClassObject.getName(), parentClassObject.getType());
    		    					}
    		    					
    		    					else if (parentClassObject.getKind() == Obj.Meth) {
    		    						
    		    						if (MyTabImpl.currentScope.findSymbol(parentClassObject.getName()) == null)
    		    							MyTabImpl.currentScope.addToLocals(parentClassObject);    
    		    						
    		    					}
		    		    		
		    		    		}
		    		    		
    		    			}
    		    		
    		    	}
    		    	
    			}
    			
    		}
    		    		
    		MyTabImpl.chainLocalSymbols(ClassDecl.getClassName().obj.getType());
    		MyTabImpl.closeScope();
    		
    		ClassDecl.getClassName().obj.accept(stv);
    		log.info("Declared class: " + ClassDecl.getClassName().getClassName() + " on line " + ClassDecl.getClassName().getLine() + "\nSymbolTable output: " + stv.getOutput());
    		stv = new MyDumpSymbolTableVisitor();
    		
    	}
    }
    
    public void visit(AbstractClassDecl AbstractClassDecl) { 
    	
    	if (AbstractClassDecl.getAbstractClassName().obj != null) {
    		
    		// inheritance check; add to local symbols everything that's not overriden    		
    		if (AbstractClassDecl.getExtendsOption() instanceof ClassInheritance) {
    			
    			if (((ClassInheritance)AbstractClassDecl.getExtendsOption()).getExtendsSyntaxCheck() instanceof ClassInheritanceSuccess) {
    				
    				Struct parentClass = ((ClassInheritanceSuccess)((ClassInheritance)AbstractClassDecl.getExtendsOption()).getExtendsSyntaxCheck()).getType().struct;
    		    	
    		    	if (parentClass != null) {
    		    		
    		    		AbstractClassDecl.getAbstractClassName().obj.getType().setElementType(parentClass);
    		    		
    		    		if (parentClass instanceof MyStructImpl) 
    		    			// TODO if abstract inherits regular class - no need for checking, inherits everything missing
    		    			// if abstract inherits abstract class - can redefine abstract method; inherits everything missing
    		    			
    		    			for (Obj parentClassObject : parentClass.getMembers()) {
	    		    			
		    					if (parentClassObject.getKind() == Obj.Fld) {
    	    	    				
	    		    				if (MyTabImpl.currentScope.findSymbol(parentClassObject.getName()) == null)
	    		    					MyTabImpl.insert(parentClassObject.getKind(), parentClassObject.getName(), parentClassObject.getType());
		    					}
		    					
		    					else if (parentClassObject.getKind() == Obj.Meth) {
		    						
		    						if (MyTabImpl.currentScope.findSymbol(parentClassObject.getName()) == null)
		    							MyTabImpl.currentScope.addToLocals(parentClassObject);    
		    						
		    					}
	    		    		
	    		    		}
    		    		
    		    			/*if (((MyStructImpl)parentClass).isAbstract()) {
    		    				
    		    				// ovde ide provera da li implementira sve apstraktne metode; ili ih gazi
    		    				
    		    				for (Obj parentClassObject : parentClass.getMembers()) {    	    		    			
    	    		    			
    	    		    			if (parentClassObject.getKind() == Obj.Fld) {
    	    	    				
    	    		    				if (MyTabImpl.currentScope.findSymbol(parentClassObject.getName()) == null)
    	    		    					MyTabImpl.insert(parentClassObject.getKind(), parentClassObject.getName(), parentClassObject.getType());
    	    		    				
    	    		    			}
    	    		    			
    	    		    			else if (parentClassObject.getKind() == Obj.Meth) {
    	    		    				
    	    		    				// TODO PROVERA METODA; pitanje: da li se apstraktne metode redefinisu? Ako da, onda radi sve normalno, sta fali dodaje
    	    		    			}
    	    		    		
    	    		    		}
    		    			
    		    			}
    		    			else {
    		    				
    		    				for (Obj parentClassObject : parentClass.getMembers()) {
		    		    			
		    		    			log.info(parentClassObject.getName());
		    	    				
		    	    				if (MyTabImpl.currentScope.findSymbol(parentClassObject.getName()) == null)
		    	    					MyTabImpl.insert(parentClassObject.getKind(), parentClassObject.getName(), parentClassObject.getType());
		    		    		
		    		    		}
		    		    		
    		    			}    	*/	    	
    		    	
    		    	}
    		    	
    			}
    		
    		}
    		
    		MyTabImpl.chainLocalSymbols(AbstractClassDecl.getAbstractClassName().obj.getType());
    		MyTabImpl.closeScope();

    		AbstractClassDecl.getAbstractClassName().obj.accept(stv);
    		log.info("Declared abstract class: " + AbstractClassDecl.getAbstractClassName().getAbstractClassName() + " on line " + AbstractClassDecl.getAbstractClassName().getLine() + "\nSymbolTable output: " + stv.getOutput());
    		stv = new MyDumpSymbolTableVisitor();
    		
    	}
    	
    }
        
    public void visit(ClassInheritanceSuccess ClassInheritanceSuccess) {
    	
    	// check if class is extending another class
    	
    	Struct parentClass = ClassInheritanceSuccess.getType().struct;
    	    	
    	if (parentClass != null) {
    		
    		if (parentClass.getKind() != Struct.Class ) {
    			
    			log.error("Semantic error on line " + ClassInheritanceSuccess.getLine() + ": " + ClassInheritanceSuccess.getType().getTypeName() + " must be class!");
    			semanticErrorFound = true;
    			ClassInheritanceSuccess.getType().struct = null;
    			
    		}
    		
    		else {
    			
    			SyntaxNode parent = ClassInheritanceSuccess.getParent();
    	    	
    	    	while (parent != null) {
    	    		
    	    		//log.info(parent.getClass());
    	    		parent = parent.getParent();
    	    		
    	    		if (parent instanceof ClassDecl) {
    	    			
    	    			ClassDecl cd = (ClassDecl) parent;
    	    			
    	    			cd.getClassName().obj.getType().setElementType(parentClass);
    	    			
    	    			break;
    	    		}
    	    		
    	    	}
    			
    		}
    	}
    	
    }
    
    /* class variables */
    
    public void visit (SingleClassVarDeclSuccess SingleClassVarDeclSuccess) {
    	
    	SyntaxNode type = SingleClassVarDeclSuccess.getParent();
		
		while (type.getClass() != ClassVarDeclSuccess.class) type = type.getParent();
		
		Struct classVarType = ((ClassVarDeclSuccess) type).getType().struct;
		
		if (classVarType != null) {
    		
    		Obj varFound = MyTabImpl.find(SingleClassVarDeclSuccess.getFieldName());
    		
    		if (varFound.equals(MyTabImpl.noObj)) {
    			
    			if (SingleClassVarDeclSuccess.getArrayOption() instanceof ArrayVariable) {
    				
    				varFound = MyTabImpl.insert(Obj.Fld, SingleClassVarDeclSuccess.getFieldName(), new Struct (Struct.Array, classVarType));
    				varFound.accept(stv);
        			log.info("Declared array field: " + SingleClassVarDeclSuccess.getFieldName() + " on line " + SingleClassVarDeclSuccess.getLine() + "\nSymbolTable output: " + stv.getOutput());
        			stv = new MyDumpSymbolTableVisitor();
    			
    			}
    			
    			else if (SingleClassVarDeclSuccess.getArrayOption() instanceof NoArrayVariable) {
    				
    				varFound = MyTabImpl.insert(Obj.Fld, SingleClassVarDeclSuccess.getFieldName(), classVarType);
    				varFound.accept(stv);
    				log.info("Declared field: " + SingleClassVarDeclSuccess.getFieldName() + " on line " + SingleClassVarDeclSuccess.getLine() + "\nSymbolTable output: " + stv.getOutput());
    				stv = new MyDumpSymbolTableVisitor();
    				
    			} 
    			    		
    		}
    		
    		else {
    			
    			log.error ("Semantic error on line " + SingleClassVarDeclSuccess.getLine() + ": " + SingleClassVarDeclSuccess.getFieldName() + " already declared");
    			semanticErrorFound = true;
    			
    		}
    		
    	}
    	
    }
    
    /* class/global methods */
    
    public void visit(MethodVoidReturn MethodVoidReturn) {
    	
    	MethodVoidReturn.struct = MyTabImpl.noType;
    	
    }
    
    public void visit(MethodTypeReturn MethodTypeReturn) {
    	
    	MethodTypeReturn.struct = MethodTypeReturn.getType().struct;
    	
    }
    
    public void visit(ClassMethodName ClassMethodName) {
    	
    	Obj methodFound = MyTabImpl.currentScope().findSymbol(ClassMethodName.getClassMethodName());
    	
    	Struct returnType = ClassMethodName.getReturnType().struct;
    	
    	SyntaxNode parent = ClassMethodName.getParent();
    	
    	while (parent != null && !(parent instanceof ClassDecl || parent instanceof AbstractClassDecl)) {
    		
    		//log.info(parent.getClass());
    		parent = parent.getParent();
    		
    	}
    	
    	if (methodFound == null && returnType != null) {
    		
    		MyObjImpl newMethodObj = new MyObjImpl(Obj.Meth, ClassMethodName.getClassMethodName(), returnType);
    		newMethodObj.setLevel(0);
    		newMethodObj.setAbstract(false);
    		newMethodObj.setGlobal(false);
    		newMethodObj.setActParamsProcessed(0);
    		newMethodObj.setReturnFound(false);
    		MyTabImpl.currentScope().addToLocals(newMethodObj);
    		ClassMethodName.obj = newMethodObj;
    		MyTabImpl.openScope();
    		
    		Struct thisType;
    		if (parent instanceof ClassDecl)
    			thisType = ((ClassDecl) parent).getClassName().obj.getType();
    		
    		else if (parent instanceof AbstractClassDecl)
    			thisType = ((AbstractClassDecl) parent).getAbstractClassName().obj.getType();
    		
    		else
    			thisType = null;
    		
    		if (thisType != null) 
    			MyTabImpl.insert(Obj.Var, "this", thisType);
    		
    	}
    	else if (methodFound != null) {
    		
    		log.error ("Semantic error on line " + ClassMethodName.getLine() + ": " + ClassMethodName.getClassMethodName() + " already declared in this scope");
			semanticErrorFound = true;
			ClassMethodName.obj = null;
    	}
    	
    }
    
    public void visit(AbstractMethod AbstractMethod) {   	
    	
    	Obj methodFound = MyTabImpl.currentScope().findSymbol(AbstractMethod.getAbstractMethodName());
    	
    	Struct returnType = AbstractMethod.getReturnType().struct;
    	
    	//log.info(returnType == null);
    	
    	if (methodFound == null && returnType != null) {
    		
    		MyObjImpl newMethodObj = new MyObjImpl(Obj.Meth, AbstractMethod.getAbstractMethodName(), returnType);
    		newMethodObj.setLevel(0);
    		newMethodObj.setAbstract(true);
    		newMethodObj.setGlobal(false);
    		newMethodObj.setActParamsProcessed(0);
    		MyTabImpl.currentScope().addToLocals(newMethodObj);
    		AbstractMethod.obj = newMethodObj;
    		MyTabImpl.openScope();
    		
    	}
    	else if (methodFound != null) {
    		
    		log.error ("Semantic error on line " + AbstractMethod.getLine() + ": " + AbstractMethod.getAbstractMethodName() + " already declared in this scope");
			semanticErrorFound = true;
			AbstractMethod.obj = null;
    	}
    	
    }
    
    public void visit(MethodName MethodName) {
    	
    	Obj methodFound = MyTabImpl.currentScope().findSymbol(MethodName.getMethodName());
    	
    	Struct returnType = MethodName.getReturnType().struct;
    	
    	//log.info(returnType == null);
    	
    	if (methodFound == null && returnType != null) {
    		
    		MyObjImpl newMethodObj = new MyObjImpl(Obj.Meth, MethodName.getMethodName(), returnType);
    		newMethodObj.setLevel(0);
    		newMethodObj.setAbstract(false);
    		newMethodObj.setGlobal(true);
    		newMethodObj.setActParamsProcessed(0);
    		newMethodObj.setReturnFound(false);
    		MyTabImpl.currentScope().addToLocals(newMethodObj);
    		MethodName.obj = newMethodObj;
    		MyTabImpl.openScope();
    		    		
    	}
    	else if (methodFound != null) {
    		
    		log.error ("Semantic error on line " + MethodName.getLine() + ": " + MethodName.getMethodName() + " already declared in this scope");
			semanticErrorFound = true;
			MethodName.obj = null;
    	}
    	
    }
    
    public void visit (ClassMethodDeclSuccess ClassMethodDeclSuccess) {
    	
    	if (ClassMethodDeclSuccess.getClassMethodName().obj != null) {
    		
    		MyTabImpl.chainLocalSymbols(ClassMethodDeclSuccess.getClassMethodName().obj);
    		MyTabImpl.closeScope();
    		
    		if (ClassMethodDeclSuccess.getClassMethodName().obj instanceof MyObjImpl) {
    			
    			MyObjImpl meth = (MyObjImpl) ClassMethodDeclSuccess.getClassMethodName().obj;
    			
    			if (meth.getType() != Tab.noType && !meth.isReturnFound ()) {
    				
    				log.error("Semantic error on line " + ClassMethodDeclSuccess.getLine() + ": no return statement found in non-void method");
    				semanticErrorFound = true;
    				
    			}
    			
    		}
    		
    		ClassMethodDeclSuccess.getClassMethodName().obj.accept(stv);
    		log.info("Declared class method: " + ClassMethodDeclSuccess.getClassMethodName().getClassMethodName() + " on line: " + ClassMethodDeclSuccess.getClassMethodName().getLine() + "\nSymbolTable output: " + stv.getOutput());
    		stv = new MyDumpSymbolTableVisitor();
    		
    	}
    }
    
    public void visit (AbstractMethodDeclSuccess AbstractMethodDeclSuccess) {
    	
    	if (AbstractMethodDeclSuccess.getAbstractMethod().obj != null) {
    		
    		MyTabImpl.chainLocalSymbols(AbstractMethodDeclSuccess.getAbstractMethod().obj);
    		MyTabImpl.closeScope();
    		
    		AbstractMethodDeclSuccess.getAbstractMethod().obj.accept(stv);
    		log.info("Declared abstract method: " + AbstractMethodDeclSuccess.getAbstractMethod().getAbstractMethodName() + " on line: " + AbstractMethodDeclSuccess.getAbstractMethod().getLine() + "\nSymbolTable output: " + stv.getOutput());
    		stv = new MyDumpSymbolTableVisitor();
    		
    	}
    	
    }
    
    public void visit (MethodDeclSuccess MethodDeclSuccess) {
    	
    	if (MethodDeclSuccess.getMethodName().obj != null) {
    		
    		MyTabImpl.chainLocalSymbols(MethodDeclSuccess.getMethodName().obj);
    		MyTabImpl.closeScope();
    		
    		MethodDeclSuccess.getMethodName().obj.accept(stv);
    		log.info("Declared method: " + MethodDeclSuccess.getMethodName().getMethodName() + " on line: " + MethodDeclSuccess.getMethodName().getLine() + "\nSymbolTable output: " + stv.getOutput());
    		stv = new MyDumpSymbolTableVisitor();
    		
    		if (MethodDeclSuccess.getMethodName().obj.getName().equals("main")) {
    			
    			if (MethodDeclSuccess.getMethodName().obj.getLevel() != 0) {
    				
    				log.error("Semantic error on line " + MethodDeclSuccess.getMethodName().getLine() + ": main method cannnot have arguments");
    				semanticErrorFound = true;
    				
    			}
    			
    			if (MethodDeclSuccess.getMethodName().obj.getType() != MyTabImpl.noType) {
    				
    				log.error("Semantic error on line " + MethodDeclSuccess.getMethodName().getLine() + ": main method must be declared as void");
    				semanticErrorFound = true;
    				
    			}
    			
    			mainFound = true;
    			
    		}
    		
    		if (MethodDeclSuccess.getMethodName().obj instanceof MyObjImpl) {
    			
    			MyObjImpl meth = (MyObjImpl) MethodDeclSuccess.getMethodName().obj;
    			
    			if (meth.getType() != Tab.noType && !meth.isReturnFound ()) {
    				
    				log.error("Semantic error on line " + MethodDeclSuccess.getLine() + ": no return statement found in non-void method");
    				semanticErrorFound = true;
    				
    			}
    			
    		}
    		
    	}
    	
    }
    
    /* local variables, formal and actual parameters*/
    
    public void visit(SingleMethodVarDeclSuccess SingleMethodVarDeclSuccess) { 
    	
    	SyntaxNode type = SingleMethodVarDeclSuccess.getParent();
		
		while (type.getClass() != MethodVarDeclSuccess.class) type = type.getParent();
		
		Struct varType = ((MethodVarDeclSuccess) type).getType().struct;
    	
    	if (varType != null) {
    		
    		Obj varFound = MyTabImpl.currentScope().findSymbol(SingleMethodVarDeclSuccess.getMethodVarName());
    		
    		if (varFound == null) {
    			
    			if (SingleMethodVarDeclSuccess.getArrayOption() instanceof ArrayVariable) {
    				
    				varFound = MyTabImpl.insert(Obj.Var, SingleMethodVarDeclSuccess.getMethodVarName(), new Struct (Struct.Array, varType));
    				varFound.setLevel(1);
    				varFound.accept(stv);
        			log.info("Declared local array variable: " + SingleMethodVarDeclSuccess.getMethodVarName() + " on line " + SingleMethodVarDeclSuccess.getLine() + "\nSymbolTable output: " + stv.getOutput());
        			stv = new MyDumpSymbolTableVisitor();
    			
    			}
    			
    			else if (SingleMethodVarDeclSuccess.getArrayOption() instanceof NoArrayVariable) {
    				
    				varFound = MyTabImpl.insert(Obj.Var, SingleMethodVarDeclSuccess.getMethodVarName(), varType);
    				varFound.accept(stv);
    				varFound.setLevel(1);
    				log.info("Declared local variable: " + SingleMethodVarDeclSuccess.getMethodVarName() + " on line " + SingleMethodVarDeclSuccess.getLine() + "\nSymbolTable output: " + stv.getOutput());
    				stv = new MyDumpSymbolTableVisitor();
    				
    			} 
    			    		
    		}
    		
    		else {
    			
    			log.error ("Semantic error on line " + SingleMethodVarDeclSuccess.getLine() + ": Local variable " + SingleMethodVarDeclSuccess.getMethodVarName() + " already declared in this scope");
				semanticErrorFound = true;
    			
    		}
    		
    	}
    	
    } 
    
    public void visit (SingleFormParSuccess SingleFormParSuccess) {
    	
    	SyntaxNode method = SingleFormParSuccess.getParent();
		
		while (method.getClass() != MethodDeclSuccess.class && method.getClass() != AbstractMethodDeclSuccess.class && method.getClass() != ClassMethodDeclSuccess.class) 
			method = method.getParent();

		if (method instanceof MethodDeclSuccess) {
			
			MethodDeclSuccess meth = (MethodDeclSuccess) method;
			
			if (meth.getMethodName().obj != null && SingleFormParSuccess.getType().struct != null) {
				
				Obj formParFound = MyTabImpl.currentScope().findSymbol(SingleFormParSuccess.getFormParName());
				
				if (formParFound == null) {
					
					Obj formPar ;
					if (SingleFormParSuccess.getArrayOption() instanceof NoArrayVariable) 
						formPar = MyTabImpl.insert(Obj.Var, SingleFormParSuccess.getFormParName(), SingleFormParSuccess.getType().struct);
					
					else 						
						formPar = MyTabImpl.insert(Obj.Var, SingleFormParSuccess.getFormParName(), new Struct (Struct.Array, SingleFormParSuccess.getType().struct));
					
					formPar.setFpPos(meth.getMethodName().obj.getLevel());
					formPar.setLevel(1);
					meth.getMethodName().obj.setLevel(meth.getMethodName().obj.getLevel() + 1);
					
					formPar.accept(stv);
					log.info("Declared formal parameter: " + SingleFormParSuccess.getFormParName() + " on line: " + SingleFormParSuccess.getLine() + "\nSymbolTable output: " + stv.getOutput() );
					stv = new MyDumpSymbolTableVisitor();
					
				}
				else {
					
					log.error ("Semantic error on line " + SingleFormParSuccess.getLine() + ": Formal parameter " + SingleFormParSuccess.getFormParName() + " already declared in this scope");
					semanticErrorFound = true;
					
				}
			}
			
		}
		
		else if (method instanceof AbstractMethodDeclSuccess) {
			
			AbstractMethodDeclSuccess meth = (AbstractMethodDeclSuccess) method;
			
			if (meth.getAbstractMethod().obj != null && SingleFormParSuccess.getType().struct != null) {
				
				Obj formParFound = MyTabImpl.currentScope().findSymbol(SingleFormParSuccess.getFormParName());
				
				if (formParFound == null) {
					
					Obj formPar ;
					if (SingleFormParSuccess.getArrayOption() instanceof NoArrayVariable) 
						formPar = MyTabImpl.insert(Obj.Var, SingleFormParSuccess.getFormParName(), SingleFormParSuccess.getType().struct);
					
					else 						
						formPar = MyTabImpl.insert(Obj.Var, SingleFormParSuccess.getFormParName(), new Struct (Struct.Array, SingleFormParSuccess.getType().struct));
					
					formPar.setFpPos(meth.getAbstractMethod().obj.getLevel());
					meth.getAbstractMethod().obj.setLevel(meth.getAbstractMethod().obj.getLevel() + 1);
					
					formPar.accept(stv);
					log.info("Declared formal parameter: " + SingleFormParSuccess.getFormParName() + " on line: " + SingleFormParSuccess.getLine() + "\nSymbolTable output: " + stv.getOutput() );
					stv = new MyDumpSymbolTableVisitor();
					
				}
				else {
					
					log.error ("Semantic error on line " + SingleFormParSuccess.getLine() + ": Formal parameter " + SingleFormParSuccess.getFormParName() + " already declared in this scope");
					semanticErrorFound = true;
					
				}
				
			}
		}
		
		else if (method instanceof ClassMethodDeclSuccess) {
			
			ClassMethodDeclSuccess meth = (ClassMethodDeclSuccess) method;
			
			if (meth.getClassMethodName().obj != null && SingleFormParSuccess.getType().struct != null) {
				
				Obj formParFound = MyTabImpl.currentScope().findSymbol(SingleFormParSuccess.getFormParName());
				
				if (formParFound == null) {
					
					Obj formPar ;
					if (SingleFormParSuccess.getArrayOption() instanceof NoArrayVariable) 
						formPar = MyTabImpl.insert(Obj.Var, SingleFormParSuccess.getFormParName(), SingleFormParSuccess.getType().struct);
					
					else 						
						formPar = MyTabImpl.insert(Obj.Var, SingleFormParSuccess.getFormParName(), new Struct (Struct.Array, SingleFormParSuccess.getType().struct));
					
					formPar.setFpPos(meth.getClassMethodName().obj.getLevel());
					meth.getClassMethodName().obj.setLevel(meth.getClassMethodName().obj.getLevel() + 1);
					
					formPar.accept(stv);
					log.info("Declared formal parameter: " + SingleFormParSuccess.getFormParName() + " on line: " + SingleFormParSuccess.getLine() + "\nSymbolTable output: " + stv.getOutput() );
					stv = new MyDumpSymbolTableVisitor();
					
				}
				else {
					
					log.error ("Semantic error on line " + SingleFormParSuccess.getLine() + ": Formal parameter " + SingleFormParSuccess.getFormParName() + " already declared in this scope");
					semanticErrorFound = true;
					
				}
				
			}
			
		}
    	
    }
    
    public void visit (ActPar ActPar) {
    	
    	SyntaxNode parent = ActPar.getParent();
    	MyObjImpl meth = null;
    	
    	while (parent != null) {
    		

    		//log.info(parent.getClass());
    		parent = parent.getParent();
    		//log.info(parent.getClass());
    		    		
    		if (parent instanceof MethodDesignator) {
    			 
    			parent = parent.getParent();
    			//log.info(parent.getClass());
    			
    			if (parent instanceof SingleFactorTerm) {
    				
    				SingleFactorTerm sft = (SingleFactorTerm) parent;
    				
    				if (sft.getFactor() instanceof MethodDesignator) {
	    				
    					MethodDesignator md = (MethodDesignator) sft.getFactor();
	    				if (md.getDesignator().obj != null) 
	    					
	    					if (md.getDesignator().obj instanceof MyObjImpl) 
	    						
	    						meth = (MyObjImpl) md.getDesignator().obj;
    				
    				}
    			
    			}
    				
    			else if (parent instanceof  MultipleFactorTerm) {
    				
    				MultipleFactorTerm mft = (MultipleFactorTerm) parent;
    				
    				if (mft.getFactor() instanceof MethodDesignator) {
	    				
    					MethodDesignator md = (MethodDesignator) mft.getFactor();
	    				if (md.getDesignator().obj != null) 
	    					
	    					if (md.getDesignator().obj instanceof MyObjImpl) 
	    						
	    						meth = (MyObjImpl) md.getDesignator().obj;
    				
    				}
    				
    			}
    			 
    			break;
    		
    		}
    		
    		else if (parent instanceof MethodCallStatement) {
    			
    			MethodCallStatement mcs = (MethodCallStatement) parent;
    			
    			if (mcs.getDestination().obj != null) 
					
					if (mcs.getDestination().obj instanceof MyObjImpl) 
						
						meth = (MyObjImpl) mcs.getDestination().obj;
    			
    			break;
    			
    		}
    		
    	}
    	
    	//log.info(meth == null);
    	//log.info(meth.getActParamsProcessed());
    	//log.info(meth.getLevel());
    	if (meth.getActParamsProcessed() >= meth.getLevel()) {
    		
    		log.error("Semantic error on line " + ActPar.getLine() + ": number of actual and formal parameters does not match (more actual than formal parameters)");
    		semanticErrorFound = true;
    		
    	}
    	
    	else if (ActPar.getExpr().obj != null) {
    		
    		for (Obj obj : meth.getLocalSymbols()) {
    			
    			if (obj.getFpPos() == meth.getActParamsProcessed() && !obj.getName().equals("this")) {
    				
    				boolean assignable = false;
    				
    				if (!ActPar.getExpr().obj.getType().assignableTo(obj.getType())) {
    	    			
    	    			if (ActPar.getExpr().obj.getType().getKind() == Struct.Class && ActPar.getExpr().obj.getType().getElemType() != null) {
    	    				
    	    				Struct parentClass = ActPar.getExpr().obj.getType().getElemType();
    	    				    				
    	    				while (parentClass != null) {
    	    					
    	    					if (parentClass.assignableTo(obj.getType())) {
    	    						
    	    						assignable = true;
    	    						break;
    	    						
    	    					}
    	    					
    	    					else
    	    						parentClass = parentClass.getElemType();
    	    						
    	    				}
    	    			}
    	    			
    	    			if (!assignable) {
    		    			
    	    				log.error("Semantic error on line " + ActPar.getLine() + ": actual parameter " + (meth.getActParamsProcessed() + 1) + " is not compatible with formal parameter");
    		        		semanticErrorFound = true;
    	        		
    	    			}
    	        		
    	    		}
    				break;
    				
    			}
    		}
    		
    	}
    	
    	meth.setActParamsProcessed(meth.getActParamsProcessed() + 1);
    	
    	
    	
    }
    
    /* statements */
    
    public void visit (Destination Destination) {
    	
    	Destination.obj = Destination.getDesignator().obj;
    	
    }
    
    public void visit (Source Source) {
    	
    	if (Source.getExpr().obj != null)
    		Source.struct = Source.getExpr().obj.getType();
    	
    	else Source.struct = null;
    }
    
    public void visit (AssignStatementSuccess AssignStatementSuccess) {
    	
    	if (AssignStatementSuccess.getDestination().obj != null && AssignStatementSuccess.getSource().struct != null) {
    		
    		boolean assignable = false;
    		
    		if (AssignStatementSuccess.getDestination().obj.getKind() != Obj.Var &&
    				AssignStatementSuccess.getDestination().obj.getKind() != Obj.Elem &&
    				AssignStatementSuccess.getDestination().obj.getKind() != Obj.Fld) {
    			
    			log.error("Semantic error on line " + AssignStatementSuccess.getLine() + ": assign destination must be variable, array element or class field");
        		semanticErrorFound = true;
        		
    		}
    		
    		//log.info(AssignStatementSuccess.getSource().struct.getKind());
    		//log.info(AssignStatementSuccess.getDestination().obj.getType().getKind());
    		//log.info(AssignStatementSuccess.getSource().struct.equals(AssignStatementSuccess.getDestination().obj.getType()));
    		//log.info(AssignStatementSuccess.getSource().struct.getClass());
    		//log.info(AssignStatementSuccess.getDestination().obj.getType().getClass());
    		
    		if (!AssignStatementSuccess.getSource().struct.assignableTo(AssignStatementSuccess.getDestination().obj.getType())) {
    			
    			if (AssignStatementSuccess.getSource().struct.getKind() == Struct.Class && AssignStatementSuccess.getSource().struct.getElemType() != null) {
    				
    				Struct parentClass = AssignStatementSuccess.getSource().struct.getElemType();
    				    				
    				while (parentClass != null) {
    					
    					if (parentClass.assignableTo(AssignStatementSuccess.getDestination().obj.getType())) {
    						
    						assignable = true;
    						break;
    						
    					}
    					
    					else
    						parentClass = parentClass.getElemType();
    						
    				}
    			}
    			
    			if (!assignable) {
	    			
    				log.error("Semantic error on line " + AssignStatementSuccess.getLine() + ": source type is not assignable to destination type");
	        		semanticErrorFound = true;
        		
    			}
        		
    		}
    	}
    }
    
    public void visit (IncrementStatement IncrementStatement) {
    	
    	SyntaxNode parent = IncrementStatement.getParent();
    	while (parent != null) {
    		
    		//log.info(parent.getClass());
    		parent = parent.getParent();
    		
    	}
    	
    	if (IncrementStatement.getDestination().obj != null) {
    		
    		if (IncrementStatement.getDestination().obj.getKind() != Obj.Var &&
    				IncrementStatement.getDestination().obj.getKind() != Obj.Elem &&
    						IncrementStatement.getDestination().obj.getKind() != Obj.Fld) {
    			
    			log.error("Semantic error on line " + IncrementStatement.getLine() + ": increment statement destination must be variable, array element or class field");
        		semanticErrorFound = true;
        		
    		}
    		
    		if (IncrementStatement.getDestination().obj.getType() != MyTabImpl.intType) {
    			
    			log.error("Semantic error on line " + IncrementStatement.getLine() + ": increment statement destination must be int type");
        		semanticErrorFound = true;
        		
    		}
    		
    	}
    	
    }
    
    public void visit (DecrementStatement DecrementStatement) {
    	
    	SyntaxNode parent = DecrementStatement.getParent();
    	while (parent != null) {
    		
    		//log.info(parent.getClass());
    		parent = parent.getParent();
    		
    	}
    	
    	if (DecrementStatement.getDestination().obj != null) {
    		
    		if (DecrementStatement.getDestination().obj.getKind() != Obj.Var &&
    				DecrementStatement.getDestination().obj.getKind() != Obj.Elem &&
    						DecrementStatement.getDestination().obj.getKind() != Obj.Fld) {
    			
    			log.error("Semantic error on line " + DecrementStatement.getLine() + ": decrement statement destination must be variable, array element or class field");
        		semanticErrorFound = true;
        		
    		}
    		
    		if (DecrementStatement.getDestination().obj.getType() != MyTabImpl.intType) {
    			
    			log.error("Semantic error on line " + DecrementStatement.getLine() + ": decrement statement destination must be int type");
        		semanticErrorFound = true;
        		
    		}
    	}

    }
    
    public void visit (MethodCallStatement MethodCallStatement) {
    	
    	if (MethodCallStatement.getDestination().obj != null) {
    		
    		if (MethodCallStatement.getDestination().obj.getKind() != Obj.Meth) {
    			
    			log.error("Semantic error on line " + MethodCallStatement.getLine() + ": designator must be global or class method");
        		semanticErrorFound = true;
        		
    		}
    		
    		else {
    			
    			if (MethodCallStatement.getDestination().obj instanceof MyObjImpl) {
    				
    				if (((MyObjImpl)MethodCallStatement.getDestination().obj).getActParamsProcessed() < MethodCallStatement.getDestination().obj.getLevel()) {
    					
    					log.error("Semantic error on line " + MethodCallStatement.getDestination().getLine() + ": number of actual and formal parameters does not match (less actual than formal parameters) ");
    					semanticErrorFound = true;
    					
    				}
    				
    				((MyObjImpl)MethodCallStatement.getDestination().obj).setActParamsProcessed(0);
    			}
    		}
    		
    	}
    	
    }
    
    public void visit (BreakStatement BreakStatement) {
    	
    	SyntaxNode parent = BreakStatement.getParent();
    	boolean forFound = false;
    	while (parent != null && !forFound) {
    		
    		if (!(parent instanceof MatchedForStatement || parent instanceof UnmatchedForStatement 
    				|| parent instanceof MatchedForEachStatement || parent instanceof UnmatchedForEachStatement))
    			parent = parent.getParent();
    		
    		else     			
    			forFound = true;
    		
    	}
    	
    	if (!forFound) {
    		
    		log.error("Semantic error on line " + BreakStatement.getParent().getLine() + ": break statement must be inside for loop");
    		semanticErrorFound = true;
    		
    	}
    	
    }
    
    public void visit (ContinueStatement ContinueStatement) {
    	
    	SyntaxNode parent = ContinueStatement.getParent();
    	boolean forFound = false;
    	while (parent != null && !forFound) {
    		
    		if (!(parent instanceof MatchedForStatement || parent instanceof UnmatchedForStatement 
    				|| parent instanceof MatchedForEachStatement || parent instanceof UnmatchedForEachStatement))
    			parent = parent.getParent();
    		
    		else     			
    			forFound = true;
    		
    	}
    	
    	if (!forFound) {
    		
    		log.error("Semantic error on line " + ContinueStatement.getParent().getLine() + ": continue statement must be inside for loop");
    		semanticErrorFound = true;
    		
    	}
    	
    }
    
    public void visit (ReadStatement ReadStatement) {
    	
    	if (ReadStatement.getDesignator().obj != null) {
    		
    		if (ReadStatement.getDesignator().obj.getKind() != Obj.Var &&
    				ReadStatement.getDesignator().obj.getKind() != Obj.Elem &&
    						ReadStatement.getDesignator().obj.getKind() != Obj.Fld) {
    			
    			log.error("Semantic error on line " + ReadStatement.getLine() + ": designator in read statement must be variable, array element or class field");
        		semanticErrorFound = true;
        		
    		}
    		
    		if (ReadStatement.getDesignator().obj.getType() != MyTabImpl.intType &&
    				ReadStatement.getDesignator().obj.getType() != MyTabImpl.charType &&
    				ReadStatement.getDesignator().obj.getType() != MyTabImpl.boolType) {
    			
    			log.error("Semantic error on line " + ReadStatement.getLine() + ": designator in read statement must be int, char or bool type");
        		semanticErrorFound = true;
        		
    		}
    	}
    }
    
    public void visit (PrintStatement PrintStatement) {
    	
    	if (PrintStatement.getExpr().obj != null) {
    		
    		if (PrintStatement.getExpr().obj.getType() != MyTabImpl.intType &&
    				PrintStatement.getExpr().obj.getType() != MyTabImpl.charType &&
    						PrintStatement.getExpr().obj.getType() != MyTabImpl.boolType) {
    			
    			log.error("Semantic error on line " + PrintStatement.getLine() + ": expression in print statement must be int, char or bool type");
        		semanticErrorFound = true;
        		
    		}
    		
    	}
    
    }
    
    public void visit (ReturnStatement ReturnStatement) {
    	
    	SyntaxNode parent = ReturnStatement.getParent();
    	
    	while (parent != null) {
    		
    		//log.info(parent.getClass());
    		parent = parent.getParent();
    		
    		if (parent instanceof MethodDeclSuccess) {
    			
    			MethodDeclSuccess mds = (MethodDeclSuccess) parent;
    			
    			//log.info("obradjuje return statement za " + mds.getMethodName().obj.getName());
    			
    			if (mds.getMethodName().obj.getType() == Tab.noType && ReturnStatement.getReturnExprOption() instanceof ReturnExpr) {
    				
    				log.error("Semantic error on line " + ReturnStatement.getLine() + ": return statement can't have expression for void methods");
    				semanticErrorFound = true;
    				
    			}
    			
    			else if (mds.getMethodName().obj.getType() != Tab.noType && ReturnStatement.getReturnExprOption() instanceof NoReturnExpr) {
    				
    				log.error("Semantic error on line " + ReturnStatement.getLine() + ": return statement must have return expression for non-void methods");
    				semanticErrorFound = true;
    				
    			}
    			
    			else if (mds.getMethodName().obj.getType() != Tab.noType && ReturnStatement.getReturnExprOption() instanceof ReturnExpr) {
    				
    				ReturnExpr returnExpr = (ReturnExpr) ReturnStatement.getReturnExprOption();
    				if (returnExpr.getExpr().obj != null && 
    						!mds.getMethodName().obj.getType().equals(returnExpr.getExpr().obj.getType())) {
    					
    					log.error("Semantic error on line " + ReturnStatement.getLine() + ": return expression must be equal as delared return type of method");
        				semanticErrorFound = true;
        				
    				}
    				
    				else {
        				
        				if (mds.getMethodName().obj instanceof MyObjImpl) {
        					
        					//log.info("Pronasao return za " + (mds.getMethodName().obj.getName()));
        					((MyObjImpl) mds.getMethodName().obj).setReturnFound(true);
        					
        				}
        				
        			}
    				
    			}
    			
    		}
    		
    	}
    }
    
    public void visit (IteratorName IteratorName) {
    	
    	Obj ident = MyTabImpl.find(IteratorName.getIteratorName());
    	
    	if (ident == Tab.noObj) {
    		
    		log.error("Semantic error on line " + IteratorName.getLine() + ": " + IteratorName.getIteratorName() + " is not declared");
    		semanticErrorFound = true;
    		
    		IteratorName.obj = null;
    		
    	}
    	
    	else
    		IteratorName.obj = ident;
    }
    
    public void visit (MatchedForEachStatement MatchedForEachStatement) {
    	
    	if (MatchedForEachStatement.getForeachArray().getDesignator().obj != null && MatchedForEachStatement.getIteratorName().obj != null) {
			
			if (MatchedForEachStatement.getForeachArray().getDesignator().obj.getType().getKind() != Struct.Array
					|| MatchedForEachStatement.getIteratorName().obj.getKind() != Obj.Var) {
				
				if (MatchedForEachStatement.getForeachArray().getDesignator().obj.getType().getKind() != Struct.Array) {
					
					log.error("Semantic error on line " + MatchedForEachStatement.getLine() + ": designator in foreach loop must be an array");
		    		semanticErrorFound = true;
		    		
				}
				
				if (MatchedForEachStatement.getIteratorName().obj.getKind() != Obj.Var) {
					
					log.error("Semantic error on line " + MatchedForEachStatement.getLine() + ": identifier in foreach loop must be global or local variable");
		    		semanticErrorFound = true;
		    		
				}
				
			}
			
			else {
				
				if (!MatchedForEachStatement.getForeachArray().getDesignator().obj.getType().getElemType().compatibleWith(MatchedForEachStatement.getIteratorName().obj.getType())) {
					
					log.error("Semantic error on line " + MatchedForEachStatement.getLine() + ": identifier in foreach loop must be same type as elements of designator array");
		    		semanticErrorFound = true;
		    		
				}
			}
		}
	}
    
    
    /* conditions, condition terms and condition factors */
    
    public void visit (SingleExprFact SingleExprFact) {
    	
    	if (SingleExprFact.getExpr().obj != null) {
    		
    		if (SingleExprFact.getExpr().obj.getType() != MyTabImpl.boolType) {
    			
    			log.error("Semantic error on line " + SingleExprFact.getLine() + ": expression must be bool type");
        		semanticErrorFound = true;
        		SingleExprFact.struct = null;
        		
    		}
    		
    		else
    			SingleExprFact.struct = SingleExprFact.getExpr().obj.getType();
    		
    	}
    	
    	else
    		SingleExprFact.struct = null;
    	
    }
    
    public void visit (MultipleExprFact MultipleExprFact) {
    	
    	if (MultipleExprFact.getFirstExpr().struct != null && MultipleExprFact.getSecondExpr().struct != null) {
    		
    		if (!MultipleExprFact.getFirstExpr().struct.compatibleWith(MultipleExprFact.getSecondExpr().struct)) {
    			
    			log.error("Semantic error on line " + MultipleExprFact.getLine() + ": expressions are not compatible");
        		semanticErrorFound = true;
        		MultipleExprFact.struct = null;
        		
    		}
    		
    		else if (MultipleExprFact.getFirstExpr().struct.getKind() == Struct.Array ||
    				MultipleExprFact.getFirstExpr().struct.getKind() == Struct.Class) {
    			
    			if (! (MultipleExprFact.getRelop() instanceof Equals || MultipleExprFact.getRelop() instanceof NotEquals)) {
    				
    				log.error("Semantic error on line " + MultipleExprFact.getLine() + ": only != and == are allowed for class or array expressions");
            		semanticErrorFound = true;
            		MultipleExprFact.struct = null;
            		
    			}
    			
    			else
    				MultipleExprFact.struct = MyTabImpl.boolType;
    			
    		}
    		
    		else 
    			MultipleExprFact.struct = MyTabImpl.boolType;
    		
    	}
    	
    	else
    		MultipleExprFact.struct = null;
    	
    }
    
    public void visit (SingleFactTerm SingleFactTerm) {
    	
    	SingleFactTerm.struct = SingleFactTerm.getCondFact().struct;
    	
    }
    
    public void visit (MultipleFactTerm MultipleFactTerm) {
    	
    	if (MultipleFactTerm.getCondTerm().struct != null && MultipleFactTerm.getCondFact().struct != null)
    		MultipleFactTerm.struct = MultipleFactTerm.getCondTerm().struct;
    	
    	else
    		MultipleFactTerm.struct = null;
    	
    }
    
    public void visit (SingleTermCondition SingleTermCondition) {
    	
    	SingleTermCondition.struct = SingleTermCondition.getCondTerm().struct;
    	
    }
    
    public void visit (MultipleTermCondition MultipleTermCondition) {
    	
    	if (MultipleTermCondition.getCondition().struct != null && MultipleTermCondition.getCondTerm().struct != null)
    		MultipleTermCondition.struct = MultipleTermCondition.getCondition().struct;
    	
    	else
    		MultipleTermCondition.struct = null;
    	
    }
    
    /* designators, factors, terms and expressions */
    
    public void visit (SimpleDesignator SimpleDesignator) {
    	
    	Obj design = MyTabImpl.find(SimpleDesignator.getDesignName());
    	
    	if (design.equals(MyTabImpl.noObj)) {
    		
    		SyntaxNode parent = SimpleDesignator.getParent();
    		
    		while (parent != null) {
    			
    			//log.info(parent.getClass());
    			parent = parent.getParent();
    			
    			if (parent instanceof ClassDecl) {
    				
    				ClassDecl cd = (ClassDecl) parent;
    				
    				if (cd.getClassName().obj.getType().getKind() == Struct.Class && cd.getClassName().obj.getType().getElemType() != null) {
    					
    					design = cd.getClassName().obj.getType().getElemType().getMembersTable().searchKey(SimpleDesignator.getDesignName());
    				}
    			}
    			
    		}
    		
    		if (design == null || design.equals(MyTabImpl.noObj)) {
	    		
    			log.error("Semantic error on line " + SimpleDesignator.getLine() + ": " + SimpleDesignator.getDesignName() + " is not declared");
	    		semanticErrorFound = true;
	    		SimpleDesignator.obj = null;
    		
    		}
    		else {
    			
    			SimpleDesignator.obj = design;
        		
        		design.accept(stv);
    			log.info("Usage of symbol: " + design.getName() + " on line: " + SimpleDesignator.getLine() + "\nSymbolTable output: " + stv.getOutput() );
    			stv = new MyDumpSymbolTableVisitor();
    			
    		}
    	
    	}
    	
    	else {
    		SimpleDesignator.obj = design;
    		
    		design.accept(stv);
			log.info("Usage of symbol: " + design.getName() + " on line: " + SimpleDesignator.getLine() + "\nSymbolTable output: " + stv.getOutput() );
			stv = new MyDumpSymbolTableVisitor();
			
    	}
    	
    }
    
    public void visit (ClassDesignator ClassDesignator) {
    	
    	if (ClassDesignator.getDesignator().obj != null) {
    	
	    	if (ClassDesignator.getDesignator().obj.getType().getKind() != Struct.Class) {
	    		
	    		log.error("Semantic error on line " + ClassDesignator.getLine() + ": designator before . sign must be class");
	    		semanticErrorFound = true;
	    		ClassDesignator.obj = null;
	    		
	    	}
	    	
	    	else {
	    		
	    		Obj design = ClassDesignator.getDesignator().obj.getType().getMembersTable().searchKey(ClassDesignator.getFieldName());
	    		
	    		if (design == null && ClassDesignator.getDesignator().obj.getName().equals("this")) {
	    			
	    			//log.info("nije ga nasao, ali sad trazi this");
	    			
	    			design = MyTabImpl.currentScope().getOuter().findSymbol((ClassDesignator.getFieldName()));
	    			
	    			//log.info(design == null);
	    			
	    			if (design == null) {
	    				
	    				//log.info("nema u outer scope, proverava nasledjivanje"); 
	    				
	    				SyntaxNode parent = ClassDesignator.getParent();
	    				
	    				while (parent != null) {
	    					
	    					//log.info(parent.getClass());
	    					parent = parent.getParent();
	    					
	    					if (parent instanceof ClassDecl) {
	    						
	    						ClassDecl cd = (ClassDecl) parent;
	    						//log.info(cd.getExtendsOption().getClass());
	    						
	    					}
	    				}
	    				
	    				Struct parentClass = ClassDesignator.getDesignator().obj.getType().getElemType();
	    				
	    				if (parentClass != null) {
	    					
	    					//log.info("pretrazuje roditelja");
	    					design = parentClass.getMembersTable().searchKey(ClassDesignator.getFieldName());
	    				}
	    				//else
	    				//log.info("nema roditelja");
	    				
	    				//log.info(design == null);
	    				
	    			}
	    			
	    				    			
	    		}
	    		
	    		
	    		if (design == null) {
	    			
	    			log.error("Semantic error on line " + ClassDesignator.getLine() + ": " + ClassDesignator.getFieldName() + " is not declared as field or method of class");
	        		semanticErrorFound = true;
	        		ClassDesignator.obj = null;
	        		
	    		}
	    		
	    		else {
	    			
	    			ClassDesignator.obj = design;
	    			
	    			design.accept(stv);
	    			log.info("Usage of symbol: " + design.getName() + " on line: " + ClassDesignator.getLine() + "\nSymbolTable output: " + stv.getOutput() );
	    			stv = new MyDumpSymbolTableVisitor();
	    			
	    		}
	    	}
    	
    	}
    	
    }    
    
    public void visit (ArrayDesignator ArrayDesignator) {
    	
    	if (ArrayDesignator.getDesignator().obj != null && ArrayDesignator.getExpr().obj != null) {
    		
    		if (ArrayDesignator.getDesignator().obj.getType().getKind() != Struct.Array || ArrayDesignator.getExpr().obj.getType().getKind () != Struct.Int) {
    			
    			if (ArrayDesignator.getDesignator().obj.getType().getKind() != Struct.Array) {
    			
    				log.error("Semantic error on line " + ArrayDesignator.getLine() + ": designator before [ sign must be array");
    				semanticErrorFound = true;
    				ArrayDesignator.obj = null;
	    		
    			}
    			
    			if ( ArrayDesignator.getExpr().obj.getType().getKind () != Struct.Int) {
        			
    				log.error("Semantic error on line " + ArrayDesignator.getLine() + ": expression inside [] must be int");
    				semanticErrorFound = true;
    				ArrayDesignator.obj = null;
	    		
    			}
    			
    		}
    		
    		else 
    			ArrayDesignator.obj = new Obj (Obj.Elem, "", ArrayDesignator.getDesignator().obj.getType().getElemType()); 
    	}
    }
    
    public void visit (DeclDesignator DeclDesignator) {
    	
    	log.info("DeclDesignator");
    	
    	SyntaxNode parent = DeclDesignator.getParent();
    	
    	while (parent != null) {
    		
    		log.info(parent.getClass());
    		parent = parent.getParent();
    		
    		if (parent instanceof MultipleFactorTermAssign
    				|| parent instanceof MultipleTermExprAssign) {
    			
    			
    		}
    		
    	}
    	
    	if (DeclDesignator.getDesignator().obj != null)
    		DeclDesignator.obj = DeclDesignator.getDesignator().obj;
    	
    	else 
    		DeclDesignator.obj = null;
    }
    
    public void visit (MethodDesignator MethodDesignator) {
    	
    	if (MethodDesignator.getDesignator().obj != null) {
	    	
    		if (MethodDesignator.getDesignator().obj.getKind() != Obj.Meth) {
	    		
	    		log.error("Semantic error on line " + MethodDesignator.getLine() + ": designator " + MethodDesignator.getDesignator().obj.getName() + " must be global or class method");
				semanticErrorFound = true;
				MethodDesignator.obj = null;
				
	    	}
    		
    		else {
    			
    			MethodDesignator.obj = MethodDesignator.getDesignator().obj;
    			if (MethodDesignator.getDesignator().obj instanceof MyObjImpl) {
    				
    				if (((MyObjImpl)MethodDesignator.getDesignator().obj).getActParamsProcessed() < MethodDesignator.getDesignator().obj.getLevel()) {
    					
    					log.error("Semantic error on line " + MethodDesignator.getLine() + ": number of actual and formal parameters does not match (less actual than formal parameters) ");
    					semanticErrorFound = true;
    					
    				}
    				((MyObjImpl)MethodDesignator.getDesignator().obj).setActParamsProcessed(0);
    			}
    		}
    	
    	}
    	else
    		MethodDesignator.obj = null;
    	
    }
    
    public void visit (ConFactor ConFactor) {
    	
    	if (ConFactor.getConstFactor() instanceof NumFactor)
    		ConFactor.obj = new Obj (Obj.Con, "",  MyTabImpl.intType);
    	
    	else if (ConFactor.getConstFactor() instanceof CharFactor)
    		ConFactor.obj = new Obj (Obj.Con, "",  MyTabImpl.charType);
    		
		else if (ConFactor.getConstFactor() instanceof BoolFactor)
			ConFactor.obj = new Obj (Obj.Con, "",  MyTabImpl.boolType);
    	
		else
			ConFactor.obj = null;
    	
    }
    
    public void visit (NewFactor NewFactor) {
    	
    	if (NewFactor.getType().struct != null) {
    		
    		if (NewFactor.getType().struct.getKind() != Struct.Class || 
    				(NewFactor.getType().struct.getKind() == Struct.Class && ((MyStructImpl)NewFactor.getType().struct).isAbstract())) {
    		
    			if (NewFactor.getType().struct.getKind() != Struct.Class) {
	    		
    				log.error("Semantic error on line " + NewFactor.getLine() + ": can only instantiate user defined classes with operator new");
    				semanticErrorFound = true;
    				    				
    			}
    			
    			if (NewFactor.getType().struct.getKind() == Struct.Class && ((MyStructImpl)NewFactor.getType().struct).isAbstract()) {
    				
    				log.error("Semantic error on line " + NewFactor.getLine() + ": abstract classes cannot be instantiated");
    				semanticErrorFound = true;
    				
    			}
    			
    			NewFactor.obj = null;
				
	    	}
    		
    		else 
    			NewFactor.obj = new Obj (Obj.Var, "",  NewFactor.getType().struct);
    		
    	}
    	
    	else
    		NewFactor.obj = null;
    }
    
    public void visit (NewArrayFactor NewArrayFactor) {
    	
    	if (NewArrayFactor.getType().struct != null && NewArrayFactor.getExpr().obj != null) {
	    	
    		if (NewArrayFactor.getExpr().obj.getType().getKind () != Struct.Int) {
	    		
	    		log.error("Semantic error on line " + NewArrayFactor.getLine() + ": expression inside [] after new operator must be int");
				semanticErrorFound = true;
				NewArrayFactor.obj = null;
				
	    	}
    		
    		else
    			NewArrayFactor.obj = new Obj (Obj.Var, "", new Struct(Struct.Array, NewArrayFactor.getType().struct )) ;
	    	
    	}
    	
    	else
    		NewArrayFactor.obj = null;
    
    }
    
    public void visit (CompositeFactor CompositeFactor) {
    	
    	CompositeFactor.obj = CompositeFactor.getExpr().obj;
    	
    }
    
    public void visit (SingleFactorTerm SingleFactorTerm) {
    	
    	SingleFactorTerm.obj = SingleFactorTerm.getFactor().obj;
    	
    }
    
    public void visit (MultipleFactorTerm MultipleFactorTerm) {
    	
    	log.info("MultipleFactorTerm");
    	
    	if (MultipleFactorTerm.getFactor().obj != null && MultipleFactorTerm.getTerm().obj != null) {
    		
    		if (MultipleFactorTerm.getFactor().obj.getType() != MyTabImpl.intType 
    				|| MultipleFactorTerm.getTerm().obj.getType() != MyTabImpl.intType) {
    			
    			if (MultipleFactorTerm.getFactor().obj.getType() != MyTabImpl.intType) {
    				
    				log.error("Semantic error on line " + MultipleFactorTerm.getLine() + ": factor after multiplication operator must be int");
    				semanticErrorFound = true;
    				MultipleFactorTerm.obj = null;
    				
    			}
    			
    			if (MultipleFactorTerm.getTerm().obj.getType() != MyTabImpl.intType) {
    				
    				log.error("Semantic error on line " + MultipleFactorTerm.getLine() + ": term before multiplication operator must be int");
    				semanticErrorFound = true;
    				MultipleFactorTerm.obj = null;
    				
    			}
    		}
    		
    		else
    			MultipleFactorTerm.obj = MultipleFactorTerm.getTerm().obj;
    	}
    	
    	else
    		MultipleFactorTerm.obj = null;
    
    }
    
    public void visit (MultipleFactorTermAssign MultipleFactorTermAssign) {
    	
    	log.info("MultipleFactorTermAssign");
    	
    	if (MultipleFactorTermAssign.getFactor().obj != null && MultipleFactorTermAssign.getTerm().obj != null) {
    		
    		if (MultipleFactorTermAssign.getFactor().obj.getType() != MyTabImpl.intType 
    				|| MultipleFactorTermAssign.getTerm().obj.getType() != MyTabImpl.intType
    				|| (MultipleFactorTermAssign.getTerm().obj.getKind() != Obj.Elem
    						&& MultipleFactorTermAssign.getTerm().obj.getKind() != Obj.Fld
    						&& MultipleFactorTermAssign.getTerm().obj.getKind() != Obj.Var)) {
    			
    			if (MultipleFactorTermAssign.getFactor().obj.getType() != MyTabImpl.intType) {
    				
    				log.error("Semantic error on line " + MultipleFactorTermAssign.getLine() + ": factor after combined multiplication operator must be int");
    				semanticErrorFound = true;
    				MultipleFactorTermAssign.obj = null;
    				
    			}
    			
    			if (MultipleFactorTermAssign.getTerm().obj.getType() != MyTabImpl.intType) {
    				
    				log.error("Semantic error on line " + MultipleFactorTermAssign.getLine() + ": term before combined multiplication operator must be int");
    				semanticErrorFound = true;
    				MultipleFactorTermAssign.obj = null;
    				
    			}
    			
    			if (MultipleFactorTermAssign.getTerm().obj.getKind() != Obj.Elem
    						&& MultipleFactorTermAssign.getTerm().obj.getKind() != Obj.Fld
    						&& MultipleFactorTermAssign.getTerm().obj.getKind() != Obj.Var) {
    				
    				log.error("Semantic error on line " + MultipleFactorTermAssign.getLine() + ": term before combined multiplication operator must be variable, array element or class field");
    				semanticErrorFound = true;
    				MultipleFactorTermAssign.obj = null;
    				
    			}
    		}
    		
    		else
    			MultipleFactorTermAssign.obj = MultipleFactorTermAssign.getTerm().obj;
    	}
    	
    	else
    		MultipleFactorTermAssign.obj = null;
    
    }
    
    public void visit (SingleTermExpr SingleTermExpr) {
    	
    	SingleTermExpr.obj = SingleTermExpr.getTerm().obj;
    	
    }
    
    public void visit (MinusSingleTermExpr MinusSingleTermExpr) {
    	
    	if (MinusSingleTermExpr.getTerm().obj != null) {
    		
    		if (MinusSingleTermExpr.getTerm().obj.getType() != MyTabImpl.intType) {
    			
    			log.error("Semantic error on line " + MinusSingleTermExpr.getLine() + ": term after minus sign must be int");
				semanticErrorFound = true;
				MinusSingleTermExpr.obj = null;
				
    		}
    		
    		else
    			MinusSingleTermExpr.obj = MinusSingleTermExpr.getTerm().obj;
    		
    	}
    	
    	else
    		MinusSingleTermExpr.obj = null;
    	
    }
    
    public void visit (MultipleTermExpr MultipleTermExpr) {
    	
    	if (MultipleTermExpr.getTerm().obj != null && MultipleTermExpr.getExpr().obj != null) {
    		
    		if (MultipleTermExpr.getTerm().obj.getType() != MyTabImpl.intType || MultipleTermExpr.getExpr().obj.getType() != MyTabImpl.intType) {
    			
    			if (MultipleTermExpr.getTerm().obj.getType() != MyTabImpl.intType) {
    				
    				log.error("Semantic error on line " + MultipleTermExpr.getLine() + ": term after addition/substraction operator must be int");
    				semanticErrorFound = true;
    				MultipleTermExpr.obj = null;
    				
    			}
    			
    			if (MultipleTermExpr.getExpr().obj.getType() != MyTabImpl.intType) {
    				
    				log.error("Semantic error on line " + MultipleTermExpr.getLine() + ": expression before addition/substraction operator must be int");
    				semanticErrorFound = true;
    				MultipleTermExpr.obj = null;
    				
    			}
    		}
    		
    		else
    			MultipleTermExpr.obj = MultipleTermExpr.getTerm().obj;
    	}
    	
    	else
    		MultipleTermExpr.obj = null;
    	
    }
    
    public void visit (MultipleTermExprAssign MultipleTermExprAssign) {
    	
    	log.info("MultipleFactorTermAssign");
    	
    	if (MultipleTermExprAssign.getTerm().obj != null && MultipleTermExprAssign.getExpr().obj != null) {
    		
    		if (MultipleTermExprAssign.getTerm().obj.getType() != MyTabImpl.intType 
    				|| MultipleTermExprAssign.getExpr().obj.getType() != MyTabImpl.intType
    				|| (MultipleTermExprAssign.getExpr().obj.getKind() != Obj.Elem
    						&& MultipleTermExprAssign.getExpr().obj.getKind() != Obj.Fld
    						&& MultipleTermExprAssign.getExpr().obj.getKind() != Obj.Var)) {
    			
    			if (MultipleTermExprAssign.getTerm().obj.getType() != MyTabImpl.intType) {
    				
    				log.error("Semantic error on line " + MultipleTermExprAssign.getLine() + ": factor after combined addition operator must be int");
    				semanticErrorFound = true;
    				MultipleTermExprAssign.obj = null;
    				
    			}
    			
    			if (MultipleTermExprAssign.getExpr().obj.getType() != MyTabImpl.intType) {
    				
    				log.error("Semantic error on line " + MultipleTermExprAssign.getLine() + ": expr before combined addition operator must be int");
    				semanticErrorFound = true;
    				MultipleTermExprAssign.obj = null;
    				
    			}
    			
    			if (MultipleTermExprAssign.getExpr().obj.getKind() != Obj.Elem
    						&& MultipleTermExprAssign.getExpr().obj.getKind() != Obj.Fld
    						&& MultipleTermExprAssign.getExpr().obj.getKind() != Obj.Var) {
    				
    				log.error("Semantic error on line " + MultipleTermExprAssign.getLine() + ": expr before combined addition operator must be variable, array element or class field");
    				semanticErrorFound = true;
    				MultipleTermExprAssign.obj = null;
    				
    			}
    		}
    		
    		else
    			MultipleTermExprAssign.obj = MultipleTermExprAssign.getExpr().obj;
    	}
    	
    	else
    		MultipleTermExprAssign.obj = null;
    
    }

}
