package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.*;

public class SemanticAnalyzer extends VisitorAdaptor {
	
	Logger log = Logger.getLogger(SemanticAnalyzer.class);
	
	private Boolean semanticErrorFound = false;
	
	private Boolean mainFound = false;
	
	private ReportHelper report = new ReportHelper();
	
	// members used for class declaration and processing
	private MyStructImpl currentClass;
	
	// members used for method declaration and processing
	private MyObjImpl currentMethod;
	
	private Boolean returnFound = null;
	
	private int currentForLoops = 0;
	
	public Boolean semanticErrorFound () {
		return semanticErrorFound;
	}
	
	public void setSemanticErrorFound () {
		semanticErrorFound = true;
	}
	
	public Boolean mainFound () {
		
		return mainFound;
		
	}
	
	/*
	 * report methods
	 */
	
	public void reportSemanticDeclaration (SemanticElement message, SyntaxNode syntaxNode, MyObjImpl obj) {
		
		StringBuilder output = new StringBuilder ();
		
		output.append("Declared ")
				.append(message.getMessage())
				.append(": ")
				.append(obj.getName())
				.append(" on line: ")
				.append(syntaxNode.getLine());
		
		log.info(output.toString());
		
		if (obj != null) {
			
			obj.accept(report);
			
			log.info("Symbol Table output: " + report.getOutput());
			
			report.resetOutput();
		
		}
		
	}
	
	public void reportSemanticDeclarationFinish (SemanticElement message, SyntaxNode syntaxNode, MyObjImpl obj) {
		
		StringBuilder output = new StringBuilder ();
		
		output.append("Finished declaring ")
				.append(message.getMessage())
				.append(": ")
				.append(obj.getName())
				.append(" on line: ")
				.append(syntaxNode.getLine());
		
		log.info(output.toString());
		
		obj.accept(report);
		
		log.info("Symbol Table output: " + report.getOutput());
		
		report.resetOutput();
		
	}
	
	public void reportSemanticDetection (SemanticElement message, SyntaxNode syntaxNode, MyObjImpl obj) {
		
		StringBuilder output = new StringBuilder ();
		
		output.append("Detected usage of ")
				.append(message.getMessage())
				.append(": ")
				.append(obj.getName())
				.append(" on line: ")
				.append(syntaxNode.getLine());
		
		log.info(output.toString());
		
		obj.accept(report);
		
		log.info("Symbol Table output: " + report.getOutput());
		
		report.resetOutput();
		
	}
	
	private void reportSemanticError (String message, SyntaxNode syntaxNode) {
		
		StringBuilder output = new StringBuilder ();
		
		output.append("Semantic error");
		
		if (syntaxNode != null 
				&& syntaxNode.getLine() != 0) 
			output.append(" on line ").append(syntaxNode.getLine());
		
		output.append(": ").append(message);
		
		log.error(output.toString());
		
		semanticErrorFound = true;
		
	}
	
	/*
	 * Analysis start/end
	 */
	
	/** ProgramName processing;
	 * 	<br> context check: ProgramName = IDENT
	 *  <br>Prog obj is inserted, and program scope is opened;
	 */
	public void visit(ProgramName programName) { 
		
		programName.myobjimpl = MyTabImpl.insert(Obj.Prog, programName.getProgramName(), MyTabImpl.noType);
    	MyTabImpl.openScope();
    	
	}
	
	/** Program finish processing;
	 * 	<br> Program = PROGRAM ProgramName GlobalDeclarationsList LEFT_BRACE GlobalMethodDeclarationsList RIGHT_BRACE 
	 *  <br>Prog obj is fetched from ProgramName neterminal, local symbols are chained and program scope is closed
	 */
	public void visit(Program program) { 
		
		MyTabImpl.chainLocalSymbols(program.getProgramName().myobjimpl);
		MyTabImpl.closeScope();
		
	}
	
	/*
	 * symbolic constants
	 */
	
	/** number processing;
	 *  <br>create Obj.Con of type Int and adr = NumberConst, pass it in myobjimpl field
	 */
	public void visit(NumberConst nc) {
		
		nc.myobjimpl = new MyObjImpl(Obj.Con, nc.getNumberConst().toString(), 
										MyTabImpl.intType, nc.getNumberConst(), MyObjImpl.NO_VALUE);
		reportSemanticDetection(SemanticElement.SYMB_CONST, nc, nc.myobjimpl);
		
	}
	
	/** printable character processing;
	 *  <br>create Obj.Con of type Char and adr = Integer.valueOf(CharConst), pass it in myobjimpl field
	 */
	public void visit(CharConst cc) {
		
		cc.myobjimpl = new MyObjImpl(Obj.Con, "'" + cc.getCharConst() + "'", 
										MyTabImpl.charType, Integer.valueOf(cc.getCharConst()), MyObjImpl.NO_VALUE);
		reportSemanticDetection(SemanticElement.SYMB_CONST, cc, cc.myobjimpl);
		
	}
	
	/** bool constant processing;
	 *  <br>create Obj.Con of type Bool and adr = 1 if true 0 if false, pass it in myobjimpl field
	 */
	public void visit(BoolConst bc) {
		
		bc.myobjimpl = new MyObjImpl(Obj.Con, bc.getBoolConst() ? "true" : "false", 
										MyTabImpl.boolType, bc.getBoolConst() ? 1 : 0, MyObjImpl.NO_VALUE);
		reportSemanticDetection(SemanticElement.SYMB_CONST, bc, bc.myobjimpl);
		
	}
	
	/*
	 * type
	 */
	
	/** Type processing;
	 * 	<br> context check: Type = IDENT
	 * 	<br> &nbsp;&nbsp;&nbsp;&nbsp; IDENT must be obj.Type
	 * 	<br>must check if IDENT is declared; pass result in mystructimpl field
	 */
	public void visit(Type type) {
		
		MyObjImpl objFound = MyTabImpl.find(type.getTypeName());
		
		if (objFound != null) {
			
			if (objFound.getKind() != Obj.Type)
				reportSemanticError(type.getTypeName() + " must be declared as type!", type);
			
			else
				type.mystructimpl = (MyStructImpl) objFound.getType();
			
		}
		
		else
			reportSemanticError(type.getTypeName() + " is not declared!", type);
		
	}
	
	/*
	 * global constants
	 */
	
	/**Global constant declaration processing;
	 * <br> context check: ConstDecl = CONST Type IDENT EQUALS ConstValue
	 * <br> &nbsp;&nbsp;&nbsp;&nbsp; Type and ConstValue must be equal types
	 * <br> check if IDENT is not declared; fetch parent instanceof ConstDecl to check if Type != null; if OK, insert new symbol
	 */
	public void visit(SingleConstDecl scd) {
		
		MyObjImpl objFound = MyTabImpl.find(scd.getConstName());
		
		if (objFound == null) {
			
			SyntaxNode typeNode = scd.getParent();
			
			while (typeNode.getClass() != ConstDecl.class) typeNode = typeNode.getParent();
			
			MyStructImpl constType = ((ConstDecl) typeNode).getType().mystructimpl;
			
			MyObjImpl constValue = scd.getConstValue().myobjimpl;
			
			if (constType != null) {
				
				if (!constType.equals( (MyStructImpl) constValue.getType()))
					reportSemanticError("type of constant value must be equal to declared constant type", scd.getConstValue());
				
				else {
					
					MyObjImpl newObj = MyTabImpl.insert(Obj.Con, scd.getConstName(), constType);
					newObj.setAdr(scd.getConstValue().myobjimpl.getAdr());
					
					reportSemanticDeclaration(SemanticElement.GLOB_CONST, scd, newObj);
					
				}
				
			}
			
		}
		
		else
			reportSemanticError(scd.getConstName() + " already declared!", scd);
		
	}
	
	/*
	 * global variables
	 */
	
	/**Global variable declaration processing;
	 * <br> context check: VarDecl = Type IDENT ArrayOption
	 * <br> check if IDENT is not declared; fetch parent instanceof VarDecl to check if Type != null; if OK, insert new symbol
	 * with type Type if ArrayOption instanceof NoArrayVariable or type Struct(Array, Type) if ArrayOption instanceof ArrayVariable
	 */
	public void visit(SingleVarDecl svd) {
		
		MyObjImpl objFound = MyTabImpl.find(svd.getVarName());
		
		if (objFound == null) {
			
			SyntaxNode typeNode = svd.getParent();
			
			while (typeNode.getClass() != VarDecl.class) typeNode = typeNode.getParent();
			
			MyStructImpl varType = ((VarDecl) typeNode).getType().mystructimpl;
			
			if (varType != null) {
				
				if (svd.getArrayOption() instanceof ArrayVariable)
					varType = new MyStructImpl( 
								new Struct(Struct.Array, varType)
							  );
				
				MyObjImpl newObj = MyTabImpl.insert(Obj.Var, svd.getVarName(), varType);
				
				reportSemanticDeclaration(SemanticElement.GLOB_VAR, svd, newObj);
				
			}
			
		}
		
		else
			reportSemanticError(svd.getVarName() + " already declared!", svd);
		
	}
	
	/*
	 * Abstract/non abstract class declaration
	 */
	
	/** Class declaration;
	 * <br> context check: ClassName = IDENT
	 * <br> check if IDENT is not declared; fetch parent and set isAbstract = true if parent instanceof AbstractClassDeclSuccess or
	 * set isAbstract = false if parent instanceof ClassDeclSuccess; if OK, insert new Obj.Type symbol and open new scope
	 */
	public void visit(ClassName cn) {
		
		MyObjImpl objFound = MyTabImpl.find(cn.getClassName());
		
		if (objFound == null) {
			
			SyntaxNode classDeclaration = cn.getParent();
			
			while (classDeclaration.getClass() != AbstractClassDeclSuccess.class
					&& classDeclaration.getClass() != ClassDeclSuccess.class) classDeclaration = classDeclaration.getParent();
			
			Boolean isAbstract = null;
			
			if (classDeclaration instanceof AbstractClassDeclSuccess)
				isAbstract = true;
			
			else if (classDeclaration instanceof ClassDeclSuccess)
				isAbstract = false;
			
			if (isAbstract != null) {
				
				currentClass = new MyStructImpl(
									new Struct (Struct.Class)
							   );
				
				currentClass.setAbstract(isAbstract);
				
				cn.myobjimpl = MyTabImpl.insert(Obj.Type, cn.getClassName(), currentClass);
				MyTabImpl.openScope();
				
				reportSemanticDeclaration(isAbstract ? SemanticElement.ABS_CLASS : SemanticElement.CLASS, cn, cn.myobjimpl);
				
			}
			
		}
		
		else
			reportSemanticError(cn.getClassName() + " already declared!", cn);
		
	}
	
	/** Abstract class declaration finish;
	 *  <br> all inherited non-overriden fields and methods from parent are added to scope;
	 *  <br> currentClass symbols are chained and scope is closed;
	 */
	public void visit(AbstractClassDecl acd) {
		
		if (currentClass != null) {
			
			MyStructImpl parentClass = currentClass.getElemType() == null
										? null
										: (MyStructImpl) currentClass.getElemType();
			
			if (parentClass != null) {
				
				// go through accessible parentClass locals, for each check if overridden; if not add to locals with isInherited = true
				
				for (Obj obj : parentClass.getMembers()) {
					
					// check if accessible
					
					MyObjImpl parentFieldOrMethod = (MyObjImpl) obj;
					
					if (parentFieldOrMethod.getAccessModifier() != MyObjImpl.Private) {
						
						// check in current scope for parentFieldOrMethod.getName()
						
						MyObjImpl foundInLocals = MyTabImpl.findInCurrent(obj.getName());
						
						// if not found, add to locals
						
						if (foundInLocals == null)
							MyTabImpl.currentScope().addToLocals(parentFieldOrMethod);
						
					}
					
				}
				
			}
			
			
			MyTabImpl.chainLocalSymbols(currentClass);
			MyTabImpl.closeScope();
			
			MyObjImpl classNode = (
									(AbstractClassDeclSuccess) acd.getAbstractClassDeclSyntaxCheck()
								  )
									.getClassName().myobjimpl;
			
			reportSemanticDeclarationFinish(SemanticElement.ABS_CLASS, acd, classNode);
			
			currentClass = null;
			
		}
		
	}
	
	/** Class declaration finish;
	 * 	<br> check for non implemented inherited abstract classes;
	 * 	<br> all inherited non-overridden fields and methods from parent are added to scope;
	 *  <br> currentClass symbols are chained and scope is closed;
	 */
	public void visit(ClassDecl cd) {
		
		if (currentClass != null) {
			
			MyStructImpl parentClass = currentClass.getElemType() == null
					? null
					: (MyStructImpl) currentClass.getElemType();

			if (parentClass != null) {
				
				// go through accessible parentClass locals, for each check if overridden; if not check if abstract method; if not add to locals
				
				for (Obj obj : parentClass.getMembers()) {
					
					// check if accessible
					
					MyObjImpl parentFieldOrMethod = (MyObjImpl) obj;
					
					if (parentFieldOrMethod.getAccessModifier() != MyObjImpl.Private) {
						
						// check in current scope for parentFieldOrMethod.getName()
						
						MyObjImpl foundInLocals = MyTabImpl.findInCurrent(obj.getName());
						
						// if not found, check if abstract method; if true throw error; if not add to locals
						
						if (foundInLocals == null) {
							
							if (parentFieldOrMethod.getKind() == Obj.Meth
									&& parentFieldOrMethod.isAbstract()) 
								reportSemanticError("class does not implement abstract method " + parentFieldOrMethod.getName(), null);
							
							else
								MyTabImpl.currentScope().addToLocals(parentFieldOrMethod);
							
						}	
						
					}
					
				}
				
			}
			
			MyTabImpl.chainLocalSymbols(currentClass);
			MyTabImpl.closeScope();
			
			MyObjImpl classNode = (
					(ClassDeclSuccess) cd.getClassDeclSyntaxCheck()
				  )
					.getClassName().myobjimpl;

			reportSemanticDeclarationFinish(SemanticElement.CLASS, cd, classNode);
			
			currentClass = null;
			
		}
		
	}
	
	/*
	 * class inheritance
	 */
	
	/**Class inheritance processing;
	 * <br> context check: Class = EXTENDS Type
	 * <br> &nbsp;&nbsp;&nbsp;&nbsp; Type must be Struct.Class
	 * <br> Type already processed, check if Type != null; if OK, set elemType of currentClass to Type
	 */
	public void visit(ClassInheritanceSuccess cis) {
		
		if (cis.getType().mystructimpl != null) {
			
			if (cis.getType().mystructimpl.getKind() != Struct.Class)
				reportSemanticError("class can only extend other classes", cis);
			
			else
				currentClass.setElementType(cis.getType().mystructimpl);
			
		}
		
	}
	
	/*
	 * class fields
	 */
	
	/**Class field declaration;
	 * <br> context check: FieldDecl = AccessModifier Type IDENT ArrayOption
	 * <br> check if IDENT is not declared in current scope; fetch parent instanceof ClassField to check if Type != null; if OK, insert new symbol
	 * with type Type if ArrayOption instanceof NoArrayVariable or type Struct(Array, Type) if ArrayOption instanceof ArrayVariable;
	 * set accessModifier from ClassField
	 */
	public void visit (SingleClassFieldDecl scfd) {
		
		MyObjImpl objFound = MyTabImpl.findInCurrent(scfd.getFieldName());
		
		if (objFound == null) {
			
			SyntaxNode typeNode = scfd.getParent();
			
			while (typeNode.getClass() != ClassField.class) typeNode = typeNode.getParent();
			
			MyStructImpl fieldType = ((ClassField) typeNode).getType().mystructimpl;
			
			if (fieldType != null) {
				
				if (scfd.getArrayOption() instanceof ArrayVariable)
					fieldType = new MyStructImpl( 
									new Struct(Struct.Array, fieldType)
							    );
				
				MyObjImpl newObj = MyTabImpl.insert(Obj.Fld, scfd.getFieldName(), fieldType);
				
				AccessModifier access = ((ClassField) typeNode).getAccessModifier();
				newObj.setAccessModifier(access instanceof PrivateAccess
											? MyObjImpl.Private
											: access instanceof ProtectedAccess
												? MyObjImpl.Protected
												: access instanceof PublicAccess
													? MyObjImpl.Public
													: MyObjImpl.NO_VALUE);
				
				reportSemanticDeclaration(SemanticElement.CLASS_FIELD, scfd, newObj);
				
			}
			
		}
		
		else
			reportSemanticError(scfd.getFieldName() + " already declared!", scfd);
		
	}
	
	/*
	 * abstract/class/global methods
	 */
	
	/** Non-void return type;
	 * <br> context check: ReturnType = Type
	 * <br> Type is already checked, pass that result to mystructimpl field
	 */
	public void visit(NoVoidReturn nvr) {
		
		nvr.mystructimpl = nvr.getType().mystructimpl;
		
	}
	
	/** Void return type;
	 * <br> context check: ReturnType = VOID
	 * <br> set my mystructimpl field to Tab.noType
	 */
	public void visit(VoidReturn vr) {
		
		vr.mystructimpl = MyTabImpl.noType;
		
	}
	
	/** Abstract/class/global method declaration
	 * <br> context check: MethodName = IDENT
	 * <br> check if IDENT is not declared in current scope; 
	 * <br> fetch parent and set isAbstract = true, isGlobal = false if parent instanceof 
	 * AbstractMethodDeclSuccess or isAbstractt = false, isGlobal = false if parent instanceof ClassMethodDecl or
	 * isAbstract = false, isGlobal = true if parent instanceof MethodDecl; 
	 * <br> is abstract or class method, check currentClass != null;
	 * <br>if isAbstract = true and MyObjImpl.Private report error;
	 * <br> from same parent collect type and accessModifier, check if type != null,  if OK, insert new Obj.Meth symbol and open new scope;
	 * if method is class method, insert this as implicit parameter
	 */
	public void visit(MethodName mn) {
		
		MyObjImpl objFound = MyTabImpl.findInCurrent(mn.getMethodName());
		
		if (objFound == null) {
			
			SyntaxNode methodDeclaration = mn.getParent();
			
			while (methodDeclaration.getClass() != AbstractMethodDeclSuccess.class
					&& methodDeclaration.getClass() != ClassMethodDecl.class
					&& methodDeclaration.getClass() != MethodDecl.class) methodDeclaration = methodDeclaration.getParent();
			
			Boolean isAbstract = null, isGlobal = null;
			MyStructImpl returnType = null;
			AccessModifier access = null;
			
			if (methodDeclaration instanceof AbstractMethodDeclSuccess) {
				
				if (currentClass != null) {
					
					isAbstract = true;
					isGlobal = false;
					returnType = (
									(AbstractMethodDeclSuccess) methodDeclaration
								 )
									.getReturnType().mystructimpl;
					
					access = (
								(AbstractMethodDeclSuccess) methodDeclaration
							 )
								.getAccessModifier();
					
					if (access instanceof PrivateAccess) {
						
						returnType = null;
						reportSemanticError("abstract method can't have private access", mn);
						
					}
				
				}
				
			}
			
			else if (methodDeclaration instanceof ClassMethodDecl) {
				
				if (currentClass != null) {
				
					isAbstract = false;
					isGlobal = false;
					returnType = (
									(ClassMethodDecl) methodDeclaration
								 )
									.getReturnType().mystructimpl;
					
					access = (
								(ClassMethodDecl) methodDeclaration
							 )
								.getAccessModifier();
				
				}
				
			}
			
			else if (methodDeclaration instanceof MethodDecl) {
				
				isAbstract = false;
				isGlobal = true;
				returnType = (
								(MethodDecl) methodDeclaration
							 )
								.getReturnType().mystructimpl;
				
			}
			
			if (returnType != null) {
				
				currentMethod = MyTabImpl.insert(Obj.Meth, mn.getMethodName(), returnType);
				currentMethod.setAbstract(isAbstract);
				currentMethod.setGlobal(isGlobal);
				currentMethod.setLevel(0);
				
				if (access != null)
					currentMethod.setAccessModifier(access instanceof PrivateAccess
													? MyObjImpl.Private
													: access instanceof ProtectedAccess
														? MyObjImpl.Protected
														: access instanceof PublicAccess
															? MyObjImpl.Public
															: MyObjImpl.NO_VALUE);
				
				mn.myobjimpl = currentMethod;
				
				MyTabImpl.openScope();
				
				reportSemanticDeclaration(isAbstract ? SemanticElement.ABS_METH
													 : isGlobal ? SemanticElement.GLOB_METH
															    : SemanticElement.CLASS_METH, mn, currentMethod);
				
				if (!isAbstract)
					returnFound = false;
				
				if (!isAbstract && !isGlobal) {
					
					MyObjImpl thisPointer = MyTabImpl.insert(Obj.Var, "this", currentClass);
					thisPointer.setFpPos(-1);
					
				}
				
			}
			
		}
		
		else
			reportSemanticError(mn.getMethodName() + " already declared!", mn);
		
	}

	/**Abstract method declaration finish;
	 * <br> currentMethod symbols are chained and scope is closed;
	 */
	public void visit(AbstractMethodDeclSuccess amdc) {
		
		if (currentMethod != null) {
			
			MyTabImpl.chainLocalSymbols(currentMethod);
			MyTabImpl.closeScope();

			reportSemanticDeclarationFinish(SemanticElement.ABS_METH, amdc, currentMethod);
			
			currentMethod = null;
			
		}
		
	}
	
	/**Class method declaration finish;
	 * <br> non void methods are checked for return statement
	 * <br> currentMethod symbols are chained and scope is closed; 
	 */
	public void visit(ClassMethodDecl cmd) {
		
		if (currentMethod != null) {
			
			if (currentMethod.getType() != MyTabImpl.noType 
					&& !returnFound)
				reportSemanticError("no return statement found in non void method " + currentMethod.getName(), null);
			
			MyTabImpl.chainLocalSymbols(currentMethod);
			MyTabImpl.closeScope();

			reportSemanticDeclarationFinish(SemanticElement.CLASS_METH, cmd, currentMethod);
			
			currentMethod = null;
			returnFound = null;
			
		}
		
	}
	
	/**Global method declaration finish;
	 * <br> check for return statement in non void method
	 * <br> check if methodName == "main", then must be void without parameters
	 * <br> currentMethod symbols are chained and scope is closed;
	 */
	public void visit(MethodDecl md) {
		
		if (currentMethod != null) {
			
			if (currentMethod.getType() != MyTabImpl.noType 
					&& !returnFound)
				reportSemanticError("no return statement found in non void method " + currentMethod.getName(), null);
			
			MyTabImpl.chainLocalSymbols(currentMethod);
			MyTabImpl.closeScope();

			reportSemanticDeclarationFinish(SemanticElement.GLOB_METH, md, currentMethod);
			
			// main check
			
			if (currentMethod.getName().equals("main")) {
				
				if (currentMethod.getLevel() > 0)
					reportSemanticError("main method must be declared as method with 0 formal parameters", md);
				
				if ((
						(MyStructImpl) currentMethod.getType()
					)
						!= MyTabImpl.noType)
					reportSemanticError("main method must be declared as void", md);
				
				mainFound = true;
			
			}
			
			currentMethod = null;
			returnFound = null;
			
		}
		
	}
	
	/*
	 * formal parameters
	 */
	
	/** Formal parameter declaration; check if currentMethod != null
	 * <br> context check: FormPar = Type IDENT ArrayOption
	 * <br> check if IDENT is not declared in current scope;check if Type != null; if OK, insert new symbol
	 * with type Type if ArrayOption instanceof NoArrayVariable or type Struct(Array, Type) if ArrayOption instanceof ArrayVariable
	 */
	public void visit(SingleFormParSuccess sfps) {
		
		if (currentMethod != null) {
		
			MyObjImpl objFound = MyTabImpl.findInCurrent(sfps.getFormParName());
			
			if (objFound == null) {
				
				MyStructImpl formParType = sfps.getType().mystructimpl;
				
				if (formParType != null) {
					
					if (sfps.getArrayOption() instanceof ArrayVariable)
						formParType = new MyStructImpl( 
										new Struct(Struct.Array, formParType)
								  	  );
					
					MyObjImpl newObj = MyTabImpl.insert(Obj.Var, sfps.getFormParName(), formParType);
					newObj.setFpPos(currentMethod.getLevel());
					currentMethod.setLevel(currentMethod.getLevel() + 1);
					
					reportSemanticDeclaration(SemanticElement.FORM_PAR, sfps, newObj);
					
				}
				
			}
			
			else
				reportSemanticError(sfps.getFormParName() + " already declared!", sfps);
		
		}
		
	}
	
	/*
	 * local variables
	 */
	
	/**Local variable declaration; check if currentMethod != null
	 * <br> context check: LocalVarDecl = Type IDENT ArrayOption
	 * <br> check if IDENT is not declared in current scope; fetch parent instanceof LocalVarDeclSuccess to check if Type != null; 
	 * if OK, insert new symbol with type Type if ArrayOption instanceof NoArrayVariable or type Struct(Array, Type) 
	 * if ArrayOption instanceof ArrayVariable
	 */
	public void visit(SingleLocalVarDeclSuccess slvds) {
		
		MyObjImpl objFound = MyTabImpl.findInCurrent(slvds.getLocalVarName());
		
		if (objFound == null) {
			
			SyntaxNode typeNode = slvds.getParent();
			
			while (typeNode.getClass() != LocalVarDeclSuccess.class) typeNode = typeNode.getParent();
			
			MyStructImpl localVarType = ((LocalVarDeclSuccess) typeNode).getType().mystructimpl;
			
			if (localVarType != null) {
				
				if (slvds.getArrayOption() instanceof ArrayVariable)
					localVarType = new MyStructImpl( 
								new Struct(Struct.Array, localVarType)
							  );
				
				MyObjImpl newObj = MyTabImpl.insert(Obj.Var, slvds.getLocalVarName(), localVarType);
				newObj.setFpPos(-1);
				
				reportSemanticDeclaration(SemanticElement.LOCAL_VAR, slvds, newObj);
				
			}
			
		}
		
		else
			reportSemanticError(slvds.getLocalVarName() + " already declared!", slvds);
		
	}
	
	/*
	 * statements
	 */
	
	/**AssignStatement;
	 * <br> context check: DesignatorStatement = Destination Assignop Source
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Destination must be Obj.Var, Obj.Fld or Obj.Elem
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Source must be assignable to Destination
	 * <br> Destination and Source already processed, check if != null
	 */
	public void visit(AssignStatement as) {
		
		if (as.getDestination().myobjimpl != null
				&& as.getSource().mystructimpl != null) {
			
			if (as.getDestination().myobjimpl.getKind() != Obj.Var
					&& as.getDestination().myobjimpl.getKind() != Obj.Fld
						&& as.getDestination().myobjimpl.getKind() != Obj.Elem)
				reportSemanticError(as.getDestination().myobjimpl.getName() + " must be variable, class field or array element", as.getDestination());
			
			else if (!as.getSource().mystructimpl
						.assignableTo(
								(MyStructImpl) as.getDestination().myobjimpl.getType()
						)) 
				reportSemanticError("expression after assign must be assignable to destination", as.getSource());
		
		}
		
	}
	
	/**MethodCallStatement;
	 * <br> context check: DesignatorStatement = Destination LEFT_PARENTHESIS ActParsOption RIGHT_PARENTHESIS
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Destination must be Obj.Meth
	 * <br> Destination already processed, check if != null, collect it, and do ActPars check
	 */
	public void visit(MethodCallStatement mcs) {
		
		if (mcs.getDestination().myobjimpl != null) {
			
			if (mcs.getDestination().myobjimpl.getKind() != Obj.Meth)
				reportSemanticError(mcs.getDestination().myobjimpl.getName() + " must be method", mcs.getDestination());
			
			else {
				
				//TODO Actual Parameters analysis
				
			}
			
		}
		
	}
	
	/**IncrementStatement;
	 * <br> context check: DesignatorStatement = Destination INC
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Destination must be Obj.Var, Obj.Fld or Obj.Elem
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Destination must be Struct.Int
	 * <br> Destination already processed, check if != null
	 */
	public void visit(IncrementStatement is) {
		
		if (is.getDestination().myobjimpl != null) {
			
			if (is.getDestination().myobjimpl.getKind() != Obj.Var
					&& is.getDestination().myobjimpl.getKind() != Obj.Fld
						&& is.getDestination().myobjimpl.getKind() != Obj.Elem)
				reportSemanticError(is.getDestination().myobjimpl.getName() + " must be variable, class field or array element", is.getDestination());
			
			if (is.getDestination().myobjimpl.getType() != MyTabImpl.intType) 
				reportSemanticError("designator in increment statement must be int", is.getDestination());
		}
		
	}
	
	/**DecrementStatement;
	 * <br> context check: DesignatorStatement = Destination DEC
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Destination must be Obj.Var, Obj.Fld or Obj.Elem
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Destination must be Struct.Int
	 * <br> Destination already processed, check if != null
	 */
	public void visit(DecrementStatement ds) {
		
		if (ds.getDestination().myobjimpl != null) {
			
			if (ds.getDestination().myobjimpl.getKind() != Obj.Var
					&& ds.getDestination().myobjimpl.getKind() != Obj.Fld
						&& ds.getDestination().myobjimpl.getKind() != Obj.Elem)
				reportSemanticError(ds.getDestination().myobjimpl.getName() + " must be variable, class field or array element", ds.getDestination());
			
			if (ds.getDestination().myobjimpl.getType() != MyTabImpl.intType) 
				reportSemanticError("designator in decrement statement must be int", ds.getDestination());
		}
		
	}
	
	/** For statement starter;
	 * <br> increase number of active for loops, for break and continue statements check
	 */
	public void visit(For f) {
		
		currentForLoops++;
		
	}
	
	/**Matched for statement finish;
	 * <br> decrease number of active for loops, for break and continue statements check
	 */
	public void visit(MatchedForStatement mfs) {
		
		currentForLoops--;
		
	}
	
	/**Unmatched for statement finish;
	 * <br> decrease number of active for loops, for break and continue statements check
	 */
	public void visit(UnmatchedForStatement ufs) {
		
		currentForLoops--;
		
	}
	
	/**Foreach statement starter;
	 * <br> increase number of active for loops, for break and continue statements check
	 */
	public void visit(Foreach f) {
		
		currentForLoops++;
	}
	
	/**Matched foreach statement finish;
	 * <br> context check: Statement = FOREACH LEFT_PARENTHESIS IteratorName COLON ForeachArray RIGHT_PARENTHESIS MatchedStatement
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;IteratorName must be same type as ForeachArray
	 * <br> IteratorName and ForeachArray already done, check if != null
	 * <br> decrease number of active for loops, for break and continue statements check
	 */
	public void visit(MatchedForeachStatement mfs) {
		
		currentForLoops--;
		
		if (mfs.getIteratorName().myobjimpl != null
				&& mfs.getForeachArray().myobjimpl != null) {
			
			if (!(
					(MyStructImpl) mfs.getIteratorName().myobjimpl.getType()
				)
					.equals(
								(MyStructImpl) mfs.getForeachArray().myobjimpl.getType()
							))
				reportSemanticError("iterator and array in foreach statement must be same type", mfs);
			
		}
		
	}
	
	/**Unmatched foreach statement finish;
	 * <br> context check: Statement = FOREACH LEFT_PARENTHESIS IteratorName COLON ForeachArray RIGHT_PARENTHESIS UnmatchedStatement
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;IteratorName must be same type as ForeachArray
	 * <br> IteratorName and ForeachArray already done, check if != null
	 * <br> decrease number of active for loops, for break and continue statements check
	 */
	public void visit(UnmatchedForeachStatement ufs) {
		
		currentForLoops--;
		
		if (ufs.getIteratorName().myobjimpl != null
				&& ufs.getForeachArray().myobjimpl != null) {
			
			if (!(
					(MyStructImpl) ufs.getIteratorName().myobjimpl.getType()
				)
					.equals(
								(MyStructImpl) ufs.getForeachArray().myobjimpl.getType()
							))
				reportSemanticError("iterator and array in foreach statement must be same type", ufs);
			
		}
		
	}
	
	/**Break statement;
	 * <br> context check: Statement = BREAK SEMICOLON
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;must be inside for/foreach loop
	 */
	public void visit(BreakStatement bs) {
		
		if (currentForLoops == 0)
			reportSemanticError("break statement must be inside for/foreach loop", bs);
		
	}
	
	/**Continue statement;
	 * <br> context check: Statement = CONTINUE SEMICOLON
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;must be inside for/foreach loop
	 */
	public void visit(ContinueStatement cs) {
		
		if (currentForLoops == 0)
			reportSemanticError("continue statement must be inside for/foreach loop", cs);
		
	}
	
	/** Return statement;
	 * <br> context check: Statement = RETURN [Expr] SEMICOLON
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Expr must be equal to currentMethod type
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;if Expr is omitted, currentMethod must be void
	 */
	public void visit(ReturnStatement rs) {
		
		if (currentMethod != null) {
			
			returnFound = true;
			
			if (rs.getReturnExprOption() instanceof ReturnExpr
					&& rs.getReturnExprOption().mystructimpl != null) {
				
				if (!rs.getReturnExprOption().mystructimpl
						.equals(
								(MyStructImpl) currentMethod.getType()
								))
					reportSemanticError("expression in return statement must be same type as method return type", rs.getReturnExprOption());
				
			}
			
			else if (rs.getReturnExprOption() instanceof NoReturnExpr) {
				
				if (currentMethod.getType() != MyTabImpl.noType)
					reportSemanticError("return without expression is only allowed for void methods", rs);
				
			}
			
		}
		
	}
	
	/**ReadStatement;
	 * <br> context check: Statement = READ LEFT_PARENTHESIS Designator RIGHT_PARENTHESIS SEMICOLON
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Designator must be Obj.Var, Obj.Fld or Obj.Elem
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Designator must be Struct.Int, Struct.Char or Struct.Bool
	 * <br>Designator already processed, check if != null
	 */
	public void visit(ReadStatement rs) {
		
		if (rs.getDesignator().myobjimpl != null) {
			
			if (rs.getDesignator().myobjimpl.getKind() != Obj.Var
					&& rs.getDesignator().myobjimpl.getKind() != Obj.Fld
						&& rs.getDesignator().myobjimpl.getKind() != Obj.Elem)
				reportSemanticError(rs.getDesignator().myobjimpl.getName() + " must be variable, class field or array element", rs.getDesignator());
			
			if (rs.getDesignator().myobjimpl.getType() != MyTabImpl.intType
					&& rs.getDesignator().myobjimpl.getType() != MyTabImpl.charType
						&& rs.getDesignator().myobjimpl.getType() != MyTabImpl.boolType)
				reportSemanticError(rs.getDesignator().myobjimpl.getName() + " must be int, char or bool type", rs.getDesignator());
			
		}
		
	}
	
	/*
	 * statement designators and expressions
	 */
	
	/** Destination;
	 * <br> context check: Destination = Designator
	 * <br> Designator already processed, collect result
	 */
	public void visit(Destination d) {
		
		d.myobjimpl = d.getDesignator().myobjimpl;
		
	}
	
	/** Source;
	 * <br> context check: Source = Expr
	 * <br> Expr already processed, collect result
	 */
	public void visit(SourceSuccess s) {
		
		s.mystructimpl = s.getExpr().mystructimpl;
		
	}
	
	/** IteratorName;
	 * <br> context check: IteratorName = IDENT
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;IDENT must be declared, and must be global or local variable
	 */
	public void visit(IteratorName in) {
		
		MyObjImpl objFound = MyTabImpl.find(in.getIteratorName());
		
		if (objFound != null) {
			
			if (objFound.getKind() != Obj.Var
					|| (objFound.getLevel() > 0 && objFound.getFpPos() > -1))
				reportSemanticError(in.getIteratorName() + " must be global or local variable", in);
			
			else
				in.myobjimpl = objFound;
			
		}
		
	}
	
	/** ForeachArray;
	 * <br> context check: ForeachArray = Designator
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Designator must be Struct.Array
	 * <br> Designator already processed, check if null
	 */
	public void visit(ForeachArray fa) {
		
		if (fa.getDesignator().myobjimpl != null) {
			
			if (fa.getDesignator().myobjimpl.getType().getKind() != Struct.Array)
				reportSemanticError(fa.getDesignator().myobjimpl.getName() + " must be array", fa.getDesignator());
			
			else
				fa.myobjimpl = fa.getDesignator().myobjimpl;
			
		}
		
	}
	
	/**ReturnExpr;
	 * <br> context check: ReturnExpr = Expr;
	 * <br> Expr already processed
	 */
	public void visit(ReturnExpr re) {
		
		re.mystructimpl = re.getExpr().mystructimpl;
		
	}
	
	/*
	 * actual parameters
	 */
	
	/*
	 * conditions
	 */
	
	/**MultipleTermCondition;
	 * <br> context check: Condition = Condition OR CondTerm
	 * <br> Condition and CondTerm already processed, check if != null
	 */
	public void visit(MultipleTermCondition mtc) {
		
		if (mtc.getCondition().mystructimpl != null
				&& mtc.getCondTerm().mystructimpl != null)
			mtc.mystructimpl = mtc.getCondition().mystructimpl;
		
	}
	
	/**SingleTermCondition;
	 * <br> context check: Condition = CondTerm
	 * <br> CondTerm already processed, collect result
	 */
	public void visit(SingleTermCondition stc) {
		
		stc.mystructimpl = stc.getCondTerm().mystructimpl;
		
	}
	
	/*
	 * condition terms
	 */
	
	/**MultipleFactTerm;
	 * <br> context check: CondTerm =  CondTerm AND CondFact
	 * <br> CondFact and CondTerm already processed, check if != null
	 */
	public void visit(MultipleFactTerm mft) {
		
		if (mft.getCondTerm().mystructimpl != null
				&& mft.getCondFact().mystructimpl != null)
			mft.mystructimpl = mft.getCondTerm().mystructimpl;
		
	}
	
	/**SingleFactTerm;
	 * <br> context check: CondTerm = CondFact
	 * <br> CondFact already processed, collect result
	 */
	public void visit(SingleFactTerm sft) {
		
		sft.mystructimpl = sft.getCondFact().mystructimpl;
		
	}
	
	/*
	 * condition factors
	 */
	
	/**MultipleExprFact;
	 * <br> context check: CondFact = Expr Relop Expr
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Expr types must be compatible
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;if Expr types Struct.Class or Struct.Array, Relop must be Equals or NotEquals
	 * <br> Expr already processed, check if != null
	 */
	public void visit(MultipleExprFact mef) {
		
		if (mef.getFirstExpr().mystructimpl != null
				&& mef.getSecondExpr().mystructimpl != null) {
			
			if (!mef.getFirstExpr().mystructimpl
					.compatibleWith(mef.getSecondExpr().mystructimpl))
				reportSemanticError("relop operands are not compatible", mef.getRelop());
			
			else {
				
				if (mef.getFirstExpr().mystructimpl.getKind() == Struct.Class
						|| mef.getFirstExpr().mystructimpl.getKind() == Struct.Array
							|| mef.getSecondExpr().mystructimpl.getKind() == Struct.Class
								|| mef.getSecondExpr().mystructimpl.getKind() == Struct.Array) {
					
					if (mef.getRelop() instanceof Equals
							|| mef.getRelop() instanceof NotEquals)
						mef.mystructimpl = MyTabImpl.boolType;
					
					else
						reportSemanticError("classes and arrays can only be compared using == and != operators", mef.getRelop());
					
				}
				
				else
					mef.mystructimpl = MyTabImpl.boolType;
				
			}
			
		}
		
	}
	
	/**SingleExprFact;
	 * <br> context check: CondFact = Expr
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Expr must be Struct.Bool
	 * <br> Expr already processed, check if != null
	 */
	public void visit(SingleExprFact sef) {
		
		if (sef.getExpr().mystructimpl != null) {
			
			if (sef.getExpr().mystructimpl != MyTabImpl.boolType)
				reportSemanticError("conditional factor must be bool", sef);
			
			else
				sef.mystructimpl = sef.getExpr().mystructimpl;
			
		}
		
	}
	
	/**First expression of relation operator;
	 * <br> context check: FirstExpr = Expr
	 * <br> Expr already processed, collect it
	 */
	public void visit (FirstExpr fe) {
		
		fe.mystructimpl = fe.getExpr().mystructimpl;
		
	}
	
	/**Second expression of relation operator;
	 * <br> context check: SecondExpr = Expr
	 * <br> Expr already processed, collect it
	 */
	public void visit (SecondExpr se) {
		
		se.mystructimpl = se.getExpr().mystructimpl;
		
	}
	
	/*
	 * expressions
	 */
	
	/**ExprWithAssign;
	 * <br> context check: Expr = Destination Assignop Expr
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Destination must be Obj.Var, Obj.Elem or Obj.Fld
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp; if Assignop not instanceof Assign Destination and Expr must be Struct.Int
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp; if Assignop instanceof Assign Expr must be assignable to Destination
	 * <br> Destination and Expr already processed, check if != null
	 */
	public void visit(ExprWithAssign ewa) {
		
		if (ewa.getDestination().myobjimpl != null
				&& ewa.getExpr().mystructimpl != null) {
			
			if (ewa.getDestination().myobjimpl.getKind() != Obj.Var
					&& ewa.getDestination().myobjimpl.getKind() != Obj.Elem
						&& ewa.getDestination().myobjimpl.getKind() != Obj.Fld)
				reportSemanticError(ewa.getDestination().myobjimpl.getName() + " must be variable, class field or array element", ewa.getDestination());
			
			else if (ewa.getAssignop() instanceof Assign) {
				
				if (!ewa.getExpr().mystructimpl
						.assignableTo(
								(MyStructImpl) ewa.getDestination().myobjimpl.getType()
								))
					reportSemanticError("expression after assign must be assignable to destination", ewa.getExpr());
				
				else
					ewa.mystructimpl = (MyStructImpl) ewa.getDestination().myobjimpl.getType();
				
			}
			
			else {
				
				if (ewa.getDestination().myobjimpl.getType() != MyTabImpl.intType
						|| ewa.getExpr().mystructimpl != MyTabImpl.intType) {
					
					if (ewa.getDestination().myobjimpl.getType() != MyTabImpl.intType)
						reportSemanticError("designator before combined operator must be int", ewa.getDestination());
					
					if (ewa.getExpr().mystructimpl != MyTabImpl.intType)
						reportSemanticError("expression after combined operator must be int", ewa.getExpr());
					
				}
				
				else
					ewa.mystructimpl = (MyStructImpl) ewa.getDestination().myobjimpl.getType();
				
			}
			
		}
		
	}
	
	/**ExprWithoutAssign;
	 * <br> context check: Expr = NoAssignExpr
	 * <br> NoAssignExpr already processed, collect it
	 */
	public void visit(ExprWithoutAssign ewa) {
		
		ewa.mystructimpl = ewa.getNoAssignExpr().mystructimpl;
		
	}
	
	/**MultipleTermExpr;
	 * <br> context check: Expr = Expr AddopLeft Term
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Expr and Term must be Struct.Int
	 * <br> Factor and Term already processed, check if != null
	 */
	public void visit(MultipleTermExpr mte) {
		
		if (mte.getNoAssignExpr().mystructimpl != null
				&& mte.getTerm().mystructimpl != null) {
			
			if (mte.getNoAssignExpr().mystructimpl != MyTabImpl.intType
					&& mte.getTerm().mystructimpl != MyTabImpl.intType) {
				
				if (mte.getNoAssignExpr().mystructimpl != MyTabImpl.intType)
					reportSemanticError("expression before addop operator must be int", mte.getNoAssignExpr());
				
				if (mte.getTerm().mystructimpl != MyTabImpl.intType)
					reportSemanticError("term after addop operator must be int", mte.getTerm());
				
			}
			
			else
				mte.mystructimpl = mte.getNoAssignExpr().mystructimpl;
		}
		
	}
	
	/**SingleTermExpr;
	 * <br> context check: Expr = Term
	 * <br> Term already processed, collect it
	 */
	public void visit(SingleTermExpr ste) {
		
		ste.mystructimpl = ste.getTerm().mystructimpl;
		
	}
	
	/**MinusTermExpr;
	 * <br> context check: Expr = MINUS Term
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Term must be Struct.Int
	 * <br> Term already processed, check if != null
	 */
	public void visit(MinusTermExpr mte) {
		
		if (mte.getTerm().mystructimpl != null) {
			
			if (mte.getTerm().mystructimpl != MyTabImpl.intType)
				reportSemanticError("term after negation operator must be int", mte.getTerm());
			
			else
				mte.mystructimpl = mte.getTerm().mystructimpl;
			
		}
		
	}
	
	/*
	 * terms
	 */
	
	/**MultipleFactorTerm;
	 * <br> context check: Term = Term MulopLeft Factor
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Term and Factor must be Struct.Int
	 * <br> Factor and Term already processed, check if != null
	 */
	public void visit(MultipleFactorTerm mft) {
		
		if (mft.getTerm().mystructimpl != null
				&& mft.getFactor().mystructimpl != null) {
			
			if (mft.getTerm().mystructimpl != MyTabImpl.intType
					|| mft.getFactor().mystructimpl != MyTabImpl.intType) {
				
				if (mft.getTerm().mystructimpl != MyTabImpl.intType)
					reportSemanticError("term before mulop operator must be int", mft.getTerm());
				
				if (mft.getFactor().mystructimpl != MyTabImpl.intType)
					reportSemanticError("factor after mulop operator must be int", mft.getFactor());
				
			}
			
			else
				mft.mystructimpl = mft.getTerm().mystructimpl;
			
		}
		
	}
	
	/**SingleFactorTerm;
	 * <br> context check: Term = Factor
	 * <br> Factor already processed, collect it
	 */
	public void visit(SingleFactorTerm sft) {
		
		sft.mystructimpl = sft.getFactor().mystructimpl;
		
	}
	
	/*
	 * factors
	 */
	
	/** FactorDesignator;
	 * <br> context check: Factor = Designator
	 * <br> Designator already processed, collect result
	 */
	public void visit(FactorDesignator fd) {
		
		fd.mystructimpl = fd.getDesignator().myobjimpl == null
							? null
							: (MyStructImpl) fd.getDesignator().myobjimpl.getType();
		
	}
	
	/**MethodDesignator;
	 * <br> context check: Factor = Designator LEFT_PARENTHESIS ActParsOption RIGHT_PARENTHESIS
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Designator must be Obj.Meth
	 * <br> Designator already processed, check if != null, collect it, and do ActPars check
	 */
	public void visit(MethodDesignator md) {
		
		if (md.getDesignator().myobjimpl != null) {
			
			if (md.getDesignator().myobjimpl.getKind() != Obj.Meth)
				reportSemanticError(md.getDesignator().myobjimpl.getName() + " must be method", md.getDesignator());
			
			else {
				
				//TODO Actual Parameters analysis
				md.mystructimpl = (MyStructImpl) md.getDesignator().myobjimpl.getType();
				
			}
			
		}
		
	}
	
	/**ConstFactor;
	 * <br> context check: Factor = ConstValue
	 * <br> ConstValue already processed, collect result
	 */
	public void visit(ConstFactor cf) {
		
		cf.mystructimpl = cf.getConstValue().myobjimpl == null
							? null
							: (MyStructImpl) cf.getConstValue().myobjimpl.getType();
		
	}
	
	/**NewFactor;
	 * <br> context check: Factor = NEW Type
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Type must be Struct.Class
	 * <br> Type already processed, check if != null, collect it
	 */
	public void visit(NewFactor nf) {
		
		if (nf.getType().mystructimpl != null) {
			
			if (nf.getType().mystructimpl.getKind() != Struct.Class)
				reportSemanticError("can only instatiate classes", nf.getType());
			
			else if ((
						(MyStructImpl) nf.getType().mystructimpl
					 )
						.isAbstract())
				reportSemanticError("can't instatiate objects of abstract classes", nf.getType());
			
			else
				nf.mystructimpl = nf.getType().mystructimpl;
			
		}
		
	}
	
	/**NewArrayFactor;
	 * <br> context check: Factor = NEW Type LEFT_BRACKET Expr RIGHT_BRACKET
	 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Expr must be Struct.Int
	 * <br> Type and Expr already processed, check if != null, and return new Struct (Struct.Array, Type)
	 */
	public void visit(NewArrayFactor naf) {
		
		if (naf.getType().mystructimpl != null
				&& naf.getExpr().mystructimpl != null) {
			
			if (naf.getExpr().mystructimpl != MyTabImpl.intType)
				reportSemanticError("expression inside [] must be int", naf.getExpr());
			
			else
				naf.mystructimpl = new MyStructImpl( 
										new Struct(Struct.Array, naf.getType().mystructimpl)
										);
			
		}
		
	}
	
	/**CompositeFactor;
	 * <br> context check: Factor = LEFT_PARENTHESIS Expr RIGHT_PARENTHESIS
	 * <br> Expr already processed, collect it
	 */
	public void visit(CompositeFactor cf) {
		
		cf.mystructimpl = cf.getExpr().mystructimpl == null
							? null
							: cf.getExpr().mystructimpl ;
		
	}
	
	/*
	 * designators
	 */
	
	/**Simple Designator; check if currentMethod != null
	 * <br> context check: Designator = IDENT
	 * <br> if currentMethod is global then MyTabImpl.find(IDENT)
	 * <br> if currentMethod is class then look in look in currentScope (method scope), outerScope (class scope), parentClass members, global scope
	 */
	public void visit(SimpleDesignator sd) {
		
		if (currentMethod != null) {
			
			if (currentMethod.isGlobal()) {
				
				MyObjImpl designFound = MyTabImpl.find(sd.getDesignatorName());
				
				if (designFound == null)
					reportSemanticError(sd.getDesignatorName() + " is not declared!", sd);
				
				else {
					
					sd.myobjimpl = designFound;
					
					// reporting
					if (designFound.getKind() == Obj.Con)
						reportSemanticDetection(SemanticElement.GLOB_CONST, sd, designFound);
					
					else if (designFound.getKind() == Obj.Var) {
						
						if (designFound.getLevel() == 0)
							reportSemanticDetection(SemanticElement.GLOB_VAR, sd, designFound);
					
						else if (designFound.getFpPos() > -1)
							reportSemanticDetection(SemanticElement.FORM_PAR, sd, designFound);
					
						else
							reportSemanticDetection(SemanticElement.LOCAL_VAR, sd, designFound);
						
					}
					
					else if (designFound.getKind() == Obj.Meth)
						reportSemanticDetection(SemanticElement.GLOB_METH, sd, designFound);
					
				}	
				
			}
			
			else {
				
				MyObjImpl designFound = MyTabImpl.findInCurrent(sd.getDesignatorName());
				
				if (designFound != null) {
					
					sd.myobjimpl = designFound;
					
					if (designFound.getName().equals("this")
							|| designFound.getFpPos() > -1)
						reportSemanticDetection(SemanticElement.FORM_PAR, sd, designFound);
					
					else
						reportSemanticDetection(SemanticElement.LOCAL_VAR, sd, designFound);
					
				}
				
				else {
					
					designFound = MyTabImpl.currentScope().getOuter().findSymbol(sd.getDesignatorName()) == null
									? null
									: (MyObjImpl) MyTabImpl.currentScope().getOuter().findSymbol(sd.getDesignatorName());
					
					if (designFound != null) {
						
						sd.myobjimpl = designFound;
						
						if (designFound.getKind() == Obj.Fld)
							reportSemanticDetection(SemanticElement.CLASS_FIELD, sd, designFound);
						
						else if (designFound.getKind() == Obj.Meth
									&& !designFound.isAbstract())
							reportSemanticDetection(SemanticElement.CLASS_METH, sd, designFound);
						
					}
					
					else {
						
						MyStructImpl parentClass = currentClass.getElemType() == null
													? null
													: (MyStructImpl) currentClass.getElemType();
						
						if (parentClass != null) {
							
							designFound = parentClass.getMembersTable().searchKey(sd.getDesignatorName()) == null
											? null
											: (MyObjImpl) parentClass.getMembersTable().searchKey(sd.getDesignatorName());
							
							if (designFound != null
									&& designFound.getAccessModifier() != MyObjImpl.Private) {
								
								sd.myobjimpl = designFound;
								
								if (designFound.getKind() == Obj.Fld)
									reportSemanticDetection(SemanticElement.CLASS_FIELD, sd, designFound);
								
								else if (designFound.getKind() == Obj.Meth
											&& !designFound.isAbstract())
									reportSemanticDetection(SemanticElement.CLASS_METH, sd, designFound);
									
								}
								
							}
							
							else
								designFound = null;
							
						}
						
					if (designFound == null) {
						
						designFound = MyTabImpl.find(sd.getDesignatorName());
						
						if (designFound == null)
							reportSemanticError(sd.getDesignatorName() + " is not declared!", sd);
						
						else {
							
							sd.myobjimpl = designFound;
							
							// reporting
							
							if (designFound.getKind() == Obj.Con)
								reportSemanticDetection(SemanticElement.GLOB_CONST, sd, designFound);
							
							else if (designFound.getKind() == Obj.Var) 
									reportSemanticDetection(SemanticElement.GLOB_VAR, sd, designFound);
							
							else if (designFound.getKind() == Obj.Meth)
								reportSemanticDetection(SemanticElement.GLOB_METH, sd, designFound);
						
						}
					
					}
					
				}
					
			}
				
		}
		
	}
	
	/**Array Designator; check if currentMethod != null
	 * <br> context check: Designator = Designator LEFT_BRACKET Expr RIGHT_BRACKET
	 * <br> Designator must be Struct.Array
	 * <br> Expr must be Struct.Int
	 * <br> Designator and Expr already processed, must check if != null
	 */
	public void visit(ArrayDesignator ad) {
		
		if (currentMethod != null) {
			
			if (ad.getDesignator().myobjimpl != null
					&& ad.getExpr().mystructimpl != null) {
				
				if (ad.getDesignator().myobjimpl.getType().getKind() != Struct.Array
						|| ad.getExpr().mystructimpl != MyTabImpl.intType) {
					
					if (ad.getDesignator().myobjimpl.getType().getKind() != Struct.Array)
						reportSemanticError(ad.getDesignator().myobjimpl.getName() + " must be array", ad.getDesignator());
					
					if (ad.getExpr().mystructimpl != MyTabImpl.intType)
						reportSemanticError("expression between brackets must be int", ad.getExpr());
					
				}
				
				else {
					
					ad.myobjimpl = new MyObjImpl(Obj.Elem, "Elem of " + ad.getDesignator().myobjimpl.getName(), 
							(MyStructImpl) ad.getDesignator().myobjimpl.getType().getElemType(), 
							ad.getDesignator().myobjimpl.getAdr(),
							ad.getDesignator().myobjimpl.getLevel());
					
					reportSemanticDetection(SemanticElement.ELEM, ad, ad.myobjimpl);
					
				}
				
			}
			
		}
		
	}
	
	/**Class Designator; check if currentMethod != null
	 * <br> context check: Designator = Designator.IDENT
	 * <br> Designator must be Struct.Class
	 * <br> IDENT must be Obj.Fld or obj.Meth
	 * <br> Designator already processed, must check if != null
	 * <br> if currentMethod is global, access must be public
	 * <br> if currentMethod is class, if Designator is currentClass then find in current or parent, 
	 * if Designator is parent (or any parents parent) then IDENT must not be private, if Designator is not current nor parent
	 * then IDENT must be public
	 */
	public void visit(ClassDesignator cd) {
		
		if (currentMethod != null) {
			
			if (cd.getDesignator().myobjimpl != null) {
				
				if (cd.getDesignator().myobjimpl.getType().getKind() != Struct.Class)
					reportSemanticError(cd.getDesignator().myobjimpl.getName() + " must be class instance", cd.getDesignator());
				
				else {
					
					if (currentMethod.isGlobal()) {
						
						MyObjImpl designFound = cd.getDesignator().myobjimpl.getType().getMembersTable().searchKey(cd.getFieldName()) == null
								? null
								: (MyObjImpl) cd.getDesignator().myobjimpl.getType()
										.getMembersTable().searchKey(cd.getFieldName());

						if (designFound == null)
							reportSemanticError(cd.getFieldName() + " is not field or method of " + cd.getDesignator().myobjimpl.getName(), cd);
						
						else if (designFound.getAccessModifier() != MyObjImpl.Public)
							reportSemanticError(cd.getFieldName() + " is not accessible!", cd);
						
						else {
							
							cd.myobjimpl = designFound;
							
							if (designFound.getKind() == Obj.Fld)
								reportSemanticDetection(SemanticElement.CLASS_FIELD, cd, designFound);
							
							else if (designFound.getKind() == Obj.Meth
										&& !designFound.isAbstract())
								reportSemanticDetection(SemanticElement.CLASS_METH, cd, designFound);
							
						}
						
					}
					
					else {
						
						MyStructImpl designType = (MyStructImpl) cd.getDesignator().myobjimpl.getType();
						
						if (designType.equals(currentClass)) {
							
							MyObjImpl designFound = MyTabImpl.currentScope().getOuter().findSymbol(cd.getFieldName()) == null
														? null
														: (MyObjImpl) MyTabImpl.currentScope().getOuter().findSymbol(cd.getFieldName());
							
							if (designFound == null) {
								
								MyStructImpl parentClass = designType.getElemType() == null
										? null
										: (MyStructImpl) designType.getElemType();
								
								if (parentClass != null) {
									
									designFound = parentClass.getMembersTable().searchKey(cd.getFieldName()) == null
														? null
														: (MyObjImpl) parentClass.getMembersTable().searchKey(cd.getFieldName());
									
									if (designFound == null)
										reportSemanticError(cd.getFieldName() + " is not field or method of " + cd.getDesignator().myobjimpl.getName(), cd);
									
									else if (designFound.getAccessModifier() == MyObjImpl.Private)
										reportSemanticError(cd.getFieldName() + " is not accessible", cd);
									
									else {
										
										cd.myobjimpl = designFound;
										
										if (designFound.getKind() == Obj.Fld)
											reportSemanticDetection(SemanticElement.CLASS_FIELD, cd, designFound);
										
										else if (designFound.getKind() == Obj.Meth
													&& !designFound.isAbstract())
											reportSemanticDetection(SemanticElement.CLASS_METH, cd, designFound);
										
									}
									
								}
								
								else
									reportSemanticError(cd.getFieldName() + " is not field or method of " + cd.getDesignator().myobjimpl.getName(), cd);
								
							}
							
							else {
								
								cd.myobjimpl = designFound;
								
								if (designFound.getKind() == Obj.Fld)
									reportSemanticDetection(SemanticElement.CLASS_FIELD, cd, designFound);
								
								else if (designFound.getKind() == Obj.Meth
											&& !designFound.isAbstract())
									reportSemanticDetection(SemanticElement.CLASS_METH, cd, designFound);
								
							}
							
						}
						
						else {
							
							boolean inParent = false;
							MyStructImpl parentClass = designType.getElemType() == null
															? null
															: (MyStructImpl) designType.getElemType();
							
							while (!inParent 
									&& parentClass != null) {
								
								if (designType.equals(parentClass))
									inParent = true;
								
								else
									parentClass = parentClass.getElemType() == null
													? null
													: (MyStructImpl) parentClass.getElemType();
								
							}
							
							if (inParent) {
								
								MyObjImpl designFound = parentClass.getMembersTable().searchKey(cd.getFieldName()) == null
															? null
															: (MyObjImpl) parentClass.getMembersTable().searchKey(cd.getFieldName());
					
								if (designFound == null)
									reportSemanticError(cd.getFieldName() + " is not field or method of " + cd.getDesignator().myobjimpl.getName(), cd);
								
								else if (designFound.getAccessModifier() == MyObjImpl.Private)
									reportSemanticError(cd.getFieldName() + " is not accessible", cd);
								
								else {
									
									cd.myobjimpl = designFound;
									
									if (designFound.getKind() == Obj.Fld)
										reportSemanticDetection(SemanticElement.CLASS_FIELD, cd, designFound);
									
									else if (designFound.getKind() == Obj.Meth
												&& !designFound.isAbstract())
										reportSemanticDetection(SemanticElement.CLASS_METH, cd, designFound);
									
								}
								
							}
							
							else {
								
								MyObjImpl designFound = cd.getDesignator().myobjimpl.getType().getMembersTable().searchKey(cd.getFieldName()) == null
										? null
										: (MyObjImpl) cd.getDesignator().myobjimpl.getType()
												.getMembersTable().searchKey(cd.getFieldName());
		
								if (designFound == null)
									reportSemanticError(cd.getFieldName() + " is not field or method of " + cd.getDesignator().myobjimpl.getName(), cd);
								
								else if (designFound.getAccessModifier() != MyObjImpl.Public)
									reportSemanticError(cd.getFieldName() + " is not accessible!", cd);
								
								else {
									
									cd.myobjimpl = designFound;
									
									if (designFound.getKind() == Obj.Fld)
										reportSemanticDetection(SemanticElement.CLASS_FIELD, cd, designFound);
									
									else if (designFound.getKind() == Obj.Meth
												&& !designFound.isAbstract())
										reportSemanticDetection(SemanticElement.CLASS_METH, cd, designFound);
									
								}
								
							}
							
						}
						
					}
					
				}
				
			}
			
		}
		
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
