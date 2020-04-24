package rs.ac.bg.etf.pp1;

import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class MyObjImpl extends Obj {
	
	public static final int Private = 0, Protected = 1, Public = 2;
	
	// meth: if abstract method then true
	private boolean isAbstract;
	
	// meth: if global then true
	private boolean global;
	
	// meth: sum of number of formal arguments and number of local variables
	private int numOfParsAndVars;
	
	//meth: if any return statements found then true
	private boolean returnFound;
	
	//meth and fld: privacy level
	private int accessModifier;
	
		
	public MyObjImpl(int kind, String name, Struct type) {
		super(kind, name, type);
	}
	
	public MyObjImpl(int kind, String name, Struct type, int adr, int level) {
		super(kind, name, type, adr, level);
	}

	/**
	 * @return the isAbstract
	 */
	public boolean isAbstract() {
		return isAbstract;
	}

	/**
	 * @param isAbstract the isAbstract to set
	 */
	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	/**
	 * @return the global
	 */
	public boolean isGlobal() {
		return global;
	}

	/**
	 * @param global the global to set
	 */
	public void setGlobal(boolean global) {
		this.global = global;
	}

	/**
	 * @return the numOfParsAndVars
	 */
	public int getNumOfParsAndVars() {
		return numOfParsAndVars;
	}

	/**
	 * @param numOfParsAndVars the numOfParsAndVars to set
	 */
	public void setNumOfParsAndVars(int numOfParsAndVars) {
		this.numOfParsAndVars = numOfParsAndVars;
	}

	/**
	 * @return the returnFound
	 */
	public boolean isReturnFound() {
		return returnFound;
	}

	/**
	 * @param returnFound the returnFound to set
	 */
	public void setReturnFound(boolean returnFound) {
		this.returnFound = returnFound;
	}

	/**
	 * @return the accessModifier
	 */
	public int getAccessModifier() {
		return accessModifier;
	}

	/**
	 * @param accessModifier the accessModifier to set
	 */
	public void setAccessModifier(int accessModifier) {
		this.accessModifier = accessModifier;
	}	
}
