package rs.ac.bg.etf.pp1;

import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class MyObjImpl extends Obj {
	
	// meth: if abstract method then true
	private boolean isAbstract;
		
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
}
