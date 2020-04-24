package rs.ac.bg.etf.pp1;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Scope;
import rs.etf.pp1.symboltable.concepts.Struct;
import rs.etf.pp1.symboltable.visitors.DumpSymbolTableVisitor;
import rs.etf.pp1.symboltable.visitors.SymbolTableVisitor;

public class MyTabImpl extends Tab {
	
	public static final MyStructImpl noType = new MyStructImpl ( new Struct(Struct.None)),
			intType = new MyStructImpl (new Struct(Struct.Int)), charType = new MyStructImpl (new Struct(Struct.Char)),
			nullType = new MyStructImpl( new Struct(Struct.Class)), boolType = new MyStructImpl (new Struct (Struct.Bool));
	
	private static int currentLevel;
	
	public static MyObjImpl chrObj, ordObj, lenObj;
	
	public static final MyObjImpl noObj = new MyObjImpl(Obj.Var, "noObj", noType);
	
	public static void init() {
		Scope universe = currentScope = new Scope(null);
		
		universe.addToLocals(new MyObjImpl(Obj.Type, "int", intType));
		universe.addToLocals(new MyObjImpl(Obj.Type, "char", charType));
		universe.addToLocals(new MyObjImpl(Obj.Type, "bool", boolType));
		universe.addToLocals(new MyObjImpl(Obj.Con, "eol", charType, 10, 0));
		universe.addToLocals(new MyObjImpl(Obj.Con, "null", nullType, 0, 0));
		
		chrObj = new MyObjImpl(Obj.Meth, "chr", charType, 0, 1);
		chrObj.setAbstract(false);
		chrObj.setGlobal(true);
		
		ordObj = new MyObjImpl(Obj.Meth, "ord", intType, 0, 1);
		ordObj.setAbstract(false);
		ordObj.setGlobal(true);
		
		lenObj = new MyObjImpl(Obj.Meth, "len", intType, 0, 1);
		lenObj.setAbstract(false);
		lenObj.setGlobal(true);
		
		universe.addToLocals(chrObj);
		{
			openScope();
			currentScope.addToLocals(new MyObjImpl(Obj.Var, "i", intType, 0, 1));
			chrObj.setLocals(currentScope.getLocals());
			closeScope();
		}
		
		universe.addToLocals(ordObj);
		{
			openScope();
			currentScope.addToLocals(new MyObjImpl(Obj.Var, "ch", charType, 0, 1));
			ordObj.setLocals(currentScope.getLocals());
			closeScope();
		} 
		
		
		universe.addToLocals(lenObj);
		{
			openScope();
			currentScope.addToLocals(new MyObjImpl(Obj.Var, "arr", new Struct(Struct.Array, noType), 0, 1));
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
	
	/**
	 * Pravi se novi Obj cvor sa prosledjenim atributima kind, name i type, pa se
	 * zatim ubacuje u tabelu simbola. Povratna vrednost: - novostvoreni cvor, ako
	 * cvor sa tim imenom nije vec postojao u tabeli simbola. - postojeci cvor iz
	 * tabele simbola, ako je doslo do greske jer smo pokusali da u tabelu simbola
	 * za opseg ubacimo cvor sa imenom koje vec postoji.
	 */
	public static MyObjImpl insert(int kind, String name, MyStructImpl type) {
		// create a new Object node with kind, name, type
		MyObjImpl newObj = new MyObjImpl(kind, name, type, 0, ((currentLevel != 0)? 1 : 0)); 
		
		// append the node to the end of the symbol list
		if (!currentScope.addToLocals(newObj)) {
			MyObjImpl res = (MyObjImpl) currentScope.findSymbol(name);
			return res;
		}
		else 
			return newObj;
	}
	
	/**
	 * U hes tabeli opsega trazi Obj cvor sa imenom name, pocevsi od
	 * najugnezdenijeg opsega, pa redom kroz opsege na nizim nivoima. Povratna
	 * vrednost: - pronadjeni Obj cvor, ako je pretrazivanje bilo uspesno. -
	 * Tab.noObj objekat, ako je pretrazivanje bilo neuspesno.
	 */
	public static MyObjImpl find(String name) {
		MyObjImpl resultObj = null;
		for (Scope s = currentScope; s != null; s = s.getOuter()) {
			if (s.getLocals() != null) {
				resultObj = (MyObjImpl) s.getLocals().searchKey(name);
				if (resultObj != null) break;
			}
		}
		return resultObj;
	}
	
	public static MyObjImpl findInCurrent(String name) {
		
		Obj found = currentScope.findSymbol(name);
		
		return found == null ? null : (MyObjImpl) found;
		
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
