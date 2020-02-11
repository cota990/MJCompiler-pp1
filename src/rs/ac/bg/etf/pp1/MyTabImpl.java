package rs.ac.bg.etf.pp1;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Scope;
import rs.etf.pp1.symboltable.concepts.Struct;
import rs.etf.pp1.symboltable.visitors.DumpSymbolTableVisitor;
import rs.etf.pp1.symboltable.visitors.SymbolTableVisitor;

public class MyTabImpl extends Tab {
	
	public static final Struct boolType = new Struct (Struct.Bool);
	
	private static int currentLevel;
	
	public static MyObjImpl chrObj, ordObj, lenObj;
	
	public static void init() {
		Scope universe = currentScope = new Scope(null);
		
		universe.addToLocals(new Obj(Obj.Type, "int", intType));
		universe.addToLocals(new Obj(Obj.Type, "char", charType));
		universe.addToLocals(new Obj(Obj.Type, "bool", boolType));
		universe.addToLocals(new Obj(Obj.Con, "eol", charType, 10, 0));
		universe.addToLocals(new Obj(Obj.Con, "null", nullType, 0, 0));
		
		chrObj = new MyObjImpl(Obj.Meth, "chr", charType, 0, 1);
		chrObj.setAbstract(false);
		chrObj.setGlobal(true);
		chrObj.setActParamsProcessed(0);
		
		ordObj = new MyObjImpl(Obj.Meth, "ord", intType, 0, 1);
		ordObj.setAbstract(false);
		ordObj.setGlobal(true);
		ordObj.setActParamsProcessed(0);
		
		lenObj = new MyObjImpl(Obj.Meth, "len", intType, 0, 1);
		lenObj.setAbstract(false);
		lenObj.setGlobal(true);
		lenObj.setActParamsProcessed(0);
		
		universe.addToLocals(chrObj);
		{
			openScope();
			currentScope.addToLocals(new Obj(Obj.Var, "i", intType, 0, 1));
			chrObj.setLocals(currentScope.getLocals());
			closeScope();
		}
		
		universe.addToLocals(ordObj);
		{
			openScope();
			currentScope.addToLocals(new Obj(Obj.Var, "ch", charType, 0, 1));
			ordObj.setLocals(currentScope.getLocals());
			closeScope();
		} 
		
		
		universe.addToLocals(lenObj);
		{
			openScope();
			currentScope.addToLocals(new Obj(Obj.Var, "arr", new Struct(Struct.Array, noType), 0, 1));
			lenObj.setLocals(currentScope.getLocals());
			closeScope();
		}
		
		currentLevel = -1;
	}
	
	public static void chainLocalSymbols(Obj outerScopeObj) {
		outerScopeObj.setLocals(currentScope.getLocals());
	}

	public static void chainLocalSymbols(Struct innerClass) {
		innerClass.setMembers(currentScope.getLocals());
	}
	
	/**
	 * Otvaranje novog opsega
	 */
	public static void openScope() {
		currentScope = new Scope(currentScope);
		currentLevel++;
	}

	/**
	 * Zatvaranje opsega
	 */
	public static void closeScope() {
		currentScope = currentScope.getOuter();
		currentLevel--;
	}
	
	public static void dump(SymbolTableVisitor stv) {
		System.out.println("=====================SYMBOL TABLE DUMP=========================");
		if (stv == null)
			stv = new DumpSymbolTableVisitor();
		for (Scope s = currentScope; s != null; s = s.getOuter()) {
			s.accept(stv);
		}
		System.out.println(stv.getOutput());
	}
	
	/** Stampa sadrzaj tabele simbola. */
	public static void dump() {
		dump(null);
	}

}
