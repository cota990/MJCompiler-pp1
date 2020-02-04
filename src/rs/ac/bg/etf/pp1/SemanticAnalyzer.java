package rs.ac.bg.etf.pp1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class SemanticAnalyzer extends VisitorAdaptor {
	
	Logger log = Logger.getLogger(SemanticAnalyzer.class);
	
	private Boolean semanticErrorFound = false;
	
	private MyDumpSymbolTableVisitor stv = new MyDumpSymbolTableVisitor ();
	
	public Boolean semanticErrorFound () {
		return semanticErrorFound;
	}
	
	/* Main program obj node processing */
	
	public void visit(ProgramName ProgramName) { 
		
		ProgramName.obj = Tab.insert(Obj.Prog, ProgramName.getProgramName(), Tab.noType);
    	Tab.openScope();
    	
	}
	
	public void visit(Program Program) { 
		
		Tab.chainLocalSymbols(Program.getProgramName().obj);
		Tab.closeScope();
		Program.obj = Program.getProgramName().obj;
		
	}
	
	/* Type processing */
	public void visit(Type Type) { 
		
		Obj type = Tab.find(Type.getTypeName());
		
		if (type == Tab.noObj) {
			
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
			log.info("Usage of type: " + Type.getTypeName() + " on line: " + Type.getLine() + ". SymbolTable output: " + stv.getOutput());
			
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
				
				if (constType.getKind() == Struct.Bool) {
					
					if (cnst.getBoolValue()) constValue = 1;
					else constValue = 0;
				
				}
				
				else {
					
					log.error("Semantic error on line " + SingleConstDeclSuccess.getConstValue().getLine() + ": constant " + SingleConstDeclSuccess.getConstName() + " must be declared as BOOL type!");
					semanticErrorFound = true;
				
				}
			
			}	
			
			if (constValue != null) {
				
				Obj constFound = Tab.find(SingleConstDeclSuccess.getConstName());
	    		
	    		
	    		if (constFound.equals(Tab.noObj)) {
	    			
	    			constFound = Tab.insert(Obj.Con, SingleConstDeclSuccess.getConstName(), constType);
	    			constFound.setAdr(constValue);
	    			constFound.accept(stv);
	    			log.info("Declared global constant: " + SingleConstDeclSuccess.getConstName() + " on line " + SingleConstDeclSuccess.getLine() + "; SymbolTable output: " + stv.getOutput() );
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
    		
    		Obj varFound = Tab.find(SingleVarDeclSuccess.getVarName());
    		
    		if (varFound.equals(Tab.noObj)) {
    			
    			if (SingleVarDeclSuccess.getArrayOption() instanceof ArrayVariable) {
    				
    				varFound = Tab.insert(Obj.Var, SingleVarDeclSuccess.getVarName(), new Struct (Struct.Array, varType));
    				varFound.accept(stv);
        			log.info("Declared global array variable: " + SingleVarDeclSuccess.getVarName() + " on line " + SingleVarDeclSuccess.getLine() + "; SymbolTable output: " + stv.getOutput());
        			stv = new MyDumpSymbolTableVisitor();
    			
    			}
    			
    			else if (SingleVarDeclSuccess.getArrayOption() instanceof NoArrayVariable) {
    				
    				varFound = Tab.insert(Obj.Var, SingleVarDeclSuccess.getVarName(), varType);
    				varFound.accept(stv);
    				log.info("Declared global variable: " + SingleVarDeclSuccess.getVarName() + " on line " + SingleVarDeclSuccess.getLine() + "; SymbolTable output: " + stv.getOutput());
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
    	
    	Obj classFound = Tab.find(AbstractClassName.getAbstractClassName());
    	
    	if (classFound == Tab.noObj) {
			
    		MyStructImpl newClassStruct = new MyStructImpl(new Struct (Struct.Class));
    		newClassStruct.setAbstract(true);
    		classFound = Tab.insert(Obj.Type, AbstractClassName.getAbstractClassName(), newClassStruct);
    		log.info("Declared abstract class: " + AbstractClassName.getAbstractClassName() + " on line " + AbstractClassName.getLine() );
    		AbstractClassName.obj = classFound;
    		Tab.openScope();
    		
		}
		else {
			
			log.error ("Semantic error on line " + AbstractClassName.getLine() + ": " + AbstractClassName.getAbstractClassName() + " already declared");
			semanticErrorFound = true;
			AbstractClassName.obj = null;
			
		}
    	
    }
    
    public void visit(ClassName ClassName) { 
    	
    	Obj classFound = Tab.find(ClassName.getClassName());
    	
    	if (classFound == Tab.noObj) {
			
    		MyStructImpl newClassStruct = new MyStructImpl(new Struct (Struct.Class));
    		newClassStruct.setAbstract(false);
    		classFound = Tab.insert(Obj.Type, ClassName.getClassName(), newClassStruct);
    		ClassName.obj = classFound;
    		Tab.openScope();
			log.info("Declared class: " + ClassName.getClassName() + " on line " + ClassName.getLine() );
    		
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
    	    	    				
    	    		    				if (Tab.currentScope.findSymbol(parentClassObject.getName()) == null)
    	    		    					Tab.insert(parentClassObject.getKind(), parentClassObject.getName(), parentClassObject.getType());
    	    		    				
    	    		    			}
    	    		    			
    	    		    			else if (parentClassObject.getKind() == Obj.Meth) {
    	    		    				
    	    		    				// TODO PROVERA METODA; ako je parent metoda obicna, sve ok; ako je apstraktna mora da implementira
    	    		    				
    	    		    				Obj methodFound = Tab.currentScope.findSymbol(parentClassObject.getName());
    	    		    				
    	    		    				if (methodFound == null) {
    	    		    					 
    	    		    					// not found; check if abstract; if abstract - error; if not abstract - inherit
    	    		    					
    	    		    					if (parentClassObject instanceof MyObjImpl) {
        	    		    					
        	    		    					if (((MyObjImpl)parentClassObject).isAbstract()) {
        	    		    						
        	    		    						log.error ("Semantic error on line " + ClassDecl.getLine() + ": " + ClassDecl.getClassName().getClassName() + " must implement inherited abstract method: " + parentClassObject.getName());
        	    		    						semanticErrorFound = true;
        	    		    					}
        	    		    					
        	    		    					else {
        	    		    						
        	    		    						Tab.currentScope.addToLocals(parentClassObject);    
        	    		    						
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
    	    		    								
    	    		    								log.info(methodFound.getName() + " " + methodFound.getLevel());
    	    		    								log.info(parentClassObject.getName() + " " + parentClassObject.getLevel());
    	    		    								
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
	    	    		    										
	    	    		    										if (obj.getKind() == Obj.Var && obj.getFpPos() == i) {
	    	    		    											
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
    	    		    										
    	    		    										if (abstractFormPar == null || formPar == null || !abstractFormPar.equals(formPar) ) {
    	    		    											
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
        	    	    				
    	    		    				if (Tab.currentScope.findSymbol(parentClassObject.getName()) == null)
    	    		    					Tab.insert(parentClassObject.getKind(), parentClassObject.getName(), parentClassObject.getType());
    		    					}
    		    					
    		    					else if (parentClassObject.getKind() == Obj.Meth) {
    		    						
    		    						if (Tab.currentScope.findSymbol(parentClassObject.getName()) == null)
    		    							Tab.currentScope.addToLocals(parentClassObject);    
    		    						
    		    					}
		    		    		
		    		    		}
		    		    		
    		    			}
    		    		
    		    	}
    		    	
    			}
    			
    		}
    		    		
    		Tab.chainLocalSymbols(ClassDecl.getClassName().obj.getType());
    		Tab.closeScope();
    		
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
    	    	    				
	    		    				if (Tab.currentScope.findSymbol(parentClassObject.getName()) == null)
	    		    					Tab.insert(parentClassObject.getKind(), parentClassObject.getName(), parentClassObject.getType());
		    					}
		    					
		    					else if (parentClassObject.getKind() == Obj.Meth) {
		    						
		    						if (Tab.currentScope.findSymbol(parentClassObject.getName()) == null)
		    							Tab.currentScope.addToLocals(parentClassObject);    
		    						
		    					}
	    		    		
	    		    		}
    		    		
    		    			/*if (((MyStructImpl)parentClass).isAbstract()) {
    		    				
    		    				// ovde ide provera da li implementira sve apstraktne metode; ili ih gazi
    		    				
    		    				for (Obj parentClassObject : parentClass.getMembers()) {    	    		    			
    	    		    			
    	    		    			if (parentClassObject.getKind() == Obj.Fld) {
    	    	    				
    	    		    				if (Tab.currentScope.findSymbol(parentClassObject.getName()) == null)
    	    		    					Tab.insert(parentClassObject.getKind(), parentClassObject.getName(), parentClassObject.getType());
    	    		    				
    	    		    			}
    	    		    			
    	    		    			else if (parentClassObject.getKind() == Obj.Meth) {
    	    		    				
    	    		    				// TODO PROVERA METODA; pitanje: da li se apstraktne metode redefinisu? Ako da, onda radi sve normalno, sta fali dodaje
    	    		    			}
    	    		    		
    	    		    		}
    		    			
    		    			}
    		    			else {
    		    				
    		    				for (Obj parentClassObject : parentClass.getMembers()) {
		    		    			
		    		    			log.info(parentClassObject.getName());
		    	    				
		    	    				if (Tab.currentScope.findSymbol(parentClassObject.getName()) == null)
		    	    					Tab.insert(parentClassObject.getKind(), parentClassObject.getName(), parentClassObject.getType());
		    		    		
		    		    		}
		    		    		
    		    			}    	*/	    	
    		    	
    		    	}
    		    	
    			}
    		
    		}
    		
    		Tab.chainLocalSymbols(AbstractClassDecl.getAbstractClassName().obj.getType());
    		Tab.closeScope();
    		
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
    	}
    	
    }
    
    /* class variables */
    
    public void visit (SingleClassVarDeclSuccess SingleClassVarDeclSuccess) {
    	
    	SyntaxNode type = SingleClassVarDeclSuccess.getParent();
		
		while (type.getClass() != ClassVarDeclSuccess.class) type = type.getParent();
		
		Struct classVarType = ((ClassVarDeclSuccess) type).getType().struct;
		
		if (classVarType != null) {
    		
    		Obj varFound = Tab.find(SingleClassVarDeclSuccess.getFieldName());
    		
    		if (varFound.equals(Tab.noObj)) {
    			
    			if (SingleClassVarDeclSuccess.getArrayOption() instanceof ArrayVariable) {
    				
    				varFound = Tab.insert(Obj.Fld, SingleClassVarDeclSuccess.getFieldName(), new Struct (Struct.Array, classVarType));
    				varFound.accept(stv);
        			log.info("Declared array field: " + SingleClassVarDeclSuccess.getFieldName() + " on line " + SingleClassVarDeclSuccess.getLine() + "; SymbolTable output: " + stv.getOutput());
        			stv = new MyDumpSymbolTableVisitor();
    			
    			}
    			
    			else if (SingleClassVarDeclSuccess.getArrayOption() instanceof NoArrayVariable) {
    				
    				varFound = Tab.insert(Obj.Fld, SingleClassVarDeclSuccess.getFieldName(), classVarType);
    				varFound.accept(stv);
    				log.info("Declared field: " + SingleClassVarDeclSuccess.getFieldName() + " on line " + SingleClassVarDeclSuccess.getLine() + "; SymbolTable output: " + stv.getOutput());
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
    	
    	MethodVoidReturn.struct = Tab.noType;
    	
    }
    
    public void visit(MethodTypeReturn MethodTypeReturn) {
    	
    	MethodTypeReturn.struct = MethodTypeReturn.getType().struct;
    	
    }
    
    public void visit(ClassMethodName ClassMethodName) {
    	
    	Obj methodFound = Tab.currentScope().findSymbol(ClassMethodName.getClassMethodName());
    	
    	Struct returnType = ClassMethodName.getReturnType().struct;
    	
    	log.info(returnType == null);
    	
    	if (methodFound == null && returnType != null) {
    		
    		MyObjImpl newMethodObj = new MyObjImpl(Obj.Meth, ClassMethodName.getClassMethodName(), returnType);
    		newMethodObj.setLevel(0);
    		newMethodObj.setAbstract(false);
    		Tab.currentScope().addToLocals(newMethodObj);
    		ClassMethodName.obj = newMethodObj;
    		Tab.openScope();
    		log.info("Declared class method: " + ClassMethodName.getClassMethodName() + " on line: " + ClassMethodName.getLine() );
    		
    	}
    	else if (methodFound != null) {
    		
    		log.error ("Semantic error on line " + ClassMethodName.getLine() + ": " + ClassMethodName.getClassMethodName() + " already declared in this scope");
			semanticErrorFound = true;
			ClassMethodName.obj = null;
    	}
    	
    }
    
    public void visit(AbstractMethod AbstractMethod) {   	
    	
    	Obj methodFound = Tab.currentScope().findSymbol(AbstractMethod.getAbstractMethodName());
    	
    	Struct returnType = AbstractMethod.getReturnType().struct;
    	
    	log.info(returnType == null);
    	
    	if (methodFound == null && returnType != null) {
    		
    		MyObjImpl newMethodObj = new MyObjImpl(Obj.Meth, AbstractMethod.getAbstractMethodName(), returnType);
    		newMethodObj.setLevel(0);
    		newMethodObj.setAbstract(true);
    		Tab.currentScope().addToLocals(newMethodObj);
    		AbstractMethod.obj = newMethodObj;
    		Tab.openScope();
    		log.info("Declared abstract method: " + AbstractMethod.getAbstractMethodName() + " on line: " + AbstractMethod.getLine() );
    		
    	}
    	else if (methodFound != null) {
    		
    		log.error ("Semantic error on line " + AbstractMethod.getLine() + ": " + AbstractMethod.getAbstractMethodName() + " already declared in this scope");
			semanticErrorFound = true;
			AbstractMethod.obj = null;
    	}
    	
    }
    
    public void visit(MethodName MethodName) {
    	
    	Obj methodFound = Tab.currentScope().findSymbol(MethodName.getMethodName());
    	
    	Struct returnType = MethodName.getReturnType().struct;
    	
    	log.info(returnType == null);
    	
    	if (methodFound == null && returnType != null) {
    		
    		MyObjImpl newMethodObj = new MyObjImpl(Obj.Meth, MethodName.getMethodName(), returnType);
    		newMethodObj.setLevel(0);
    		newMethodObj.setAbstract(false);
    		Tab.currentScope().addToLocals(newMethodObj);
    		MethodName.obj = newMethodObj;
    		Tab.openScope();
    		log.info("Declared method: " + MethodName.getMethodName() + " on line: " + MethodName.getLine() );
    		
    	}
    	else if (methodFound != null) {
    		
    		log.error ("Semantic error on line " + MethodName.getLine() + ": " + MethodName.getMethodName() + " already declared in this scope");
			semanticErrorFound = true;
			MethodName.obj = null;
    	}
    	
    }
    
    public void visit (ClassMethodDeclSuccess ClassMethodDeclSuccess) {
    	
    	if (ClassMethodDeclSuccess.getClassMethodName().obj != null) {
    		
    		Tab.chainLocalSymbols(ClassMethodDeclSuccess.getClassMethodName().obj);
    		Tab.closeScope();
    		
    	}
    }
    
    public void visit (AbstractMethodDeclSuccess AbstractMethodDeclSuccess) {
    	
    	if (AbstractMethodDeclSuccess.getAbstractMethod().obj != null) {
    		
    		Tab.chainLocalSymbols(AbstractMethodDeclSuccess.getAbstractMethod().obj);
    		Tab.closeScope();
    		
    	}
    	
    }
    
    public void visit (MethodDeclSuccess MethodDeclSuccess) {
    	
    	if (MethodDeclSuccess.getMethodName().obj != null) {
    		
    		Tab.chainLocalSymbols(MethodDeclSuccess.getMethodName().obj);
    		Tab.closeScope();
    		
    	}
    	
    }
    
    /* formal parameters and local variables*/
    
    public void visit (SingleFormParSuccess SingleFormParSuccess) {
    	
    	SyntaxNode method = SingleFormParSuccess.getParent();
		
		while (method.getClass() != MethodDeclSuccess.class && method.getClass() != AbstractMethodDeclSuccess.class && method.getClass() != ClassMethodDeclSuccess.class) 
			method = method.getParent();

		if (method instanceof MethodDeclSuccess) {
			
			MethodDeclSuccess meth = (MethodDeclSuccess) method;
			
			if (meth.getMethodName().obj != null && SingleFormParSuccess.getType().struct != null) {
				
				Obj formParFound = Tab.currentScope().findSymbol(SingleFormParSuccess.getFormParName());
				
				if (formParFound == null) {
					
					Obj formPar ;
					if (SingleFormParSuccess.getArrayOption() instanceof NoArrayVariable) 
						formPar = Tab.insert(Obj.Var, SingleFormParSuccess.getFormParName(), SingleFormParSuccess.getType().struct);
					
					else 						
						formPar = Tab.insert(Obj.Var, SingleFormParSuccess.getFormParName(), new Struct (Struct.Array, SingleFormParSuccess.getType().struct));
					
					log.info("Declared formal parameter: " + SingleFormParSuccess.getFormParName() + " on line: " + SingleFormParSuccess.getLine() );
					formPar.setFpPos(meth.getMethodName().obj.getLevel());
					meth.getMethodName().obj.setLevel(meth.getMethodName().obj.getLevel() + 1);
					
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
				
				Obj formParFound = Tab.currentScope().findSymbol(SingleFormParSuccess.getFormParName());
				
				if (formParFound == null) {
					
					Obj formPar ;
					if (SingleFormParSuccess.getArrayOption() instanceof NoArrayVariable) 
						formPar = Tab.insert(Obj.Var, SingleFormParSuccess.getFormParName(), SingleFormParSuccess.getType().struct);
					
					else 						
						formPar = Tab.insert(Obj.Var, SingleFormParSuccess.getFormParName(), new Struct (Struct.Array, SingleFormParSuccess.getType().struct));
					
					log.info("Declared formal parameter: " + SingleFormParSuccess.getFormParName() + " on line: " + SingleFormParSuccess.getLine() );
					
					log.info(meth.getAbstractMethod().obj.getLevel());
					formPar.setFpPos(meth.getAbstractMethod().obj.getLevel());
					meth.getAbstractMethod().obj.setLevel(meth.getAbstractMethod().obj.getLevel() + 1);
					log.info(meth.getAbstractMethod().obj.getLevel());
					
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
				
				Obj formParFound = Tab.currentScope().findSymbol(SingleFormParSuccess.getFormParName());
				
				if (formParFound == null) {
					
					Obj formPar ;
					if (SingleFormParSuccess.getArrayOption() instanceof NoArrayVariable) 
						formPar = Tab.insert(Obj.Var, SingleFormParSuccess.getFormParName(), SingleFormParSuccess.getType().struct);
					
					else 						
						formPar = Tab.insert(Obj.Var, SingleFormParSuccess.getFormParName(), new Struct (Struct.Array, SingleFormParSuccess.getType().struct));
					
					log.info("Declared formal parameter: " + SingleFormParSuccess.getFormParName() + " on line: " + SingleFormParSuccess.getLine() );
					formPar.setFpPos(meth.getClassMethodName().obj.getLevel());
					meth.getClassMethodName().obj.setLevel(meth.getClassMethodName().obj.getLevel() + 1);
					
				}
				else {
					
					log.error ("Semantic error on line " + SingleFormParSuccess.getLine() + ": Formal parameter " + SingleFormParSuccess.getFormParName() + " already declared in this scope");
					semanticErrorFound = true;
					
				}
				
			}
			
		}
    	
    }

}
