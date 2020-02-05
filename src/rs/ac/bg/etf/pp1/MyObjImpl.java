package rs.ac.bg.etf.pp1;

import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class MyObjImpl extends Obj {
	
	// meth: if abstract method then true
	private boolean isAbstract;
	
	// meth: if global then true
	private boolean global;
	
	// meth: number of actual parameters processed
	private int actParamsProcessed;
		
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
	 * @return the actParamsProcessed
	 */
	public int getActParamsProcessed() {
		return actParamsProcessed;
	}

	/**
	 * @param actParamsProcessed the actParamsProcessed to set
	 */
	public void setActParamsProcessed(int actParamsProcessed) {
		this.actParamsProcessed = actParamsProcessed;
	}	
}
