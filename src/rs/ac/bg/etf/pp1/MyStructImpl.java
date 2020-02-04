package rs.ac.bg.etf.pp1;

import rs.etf.pp1.symboltable.concepts.Struct;

public class MyStructImpl extends Struct {
	
	// class: if abstract class then true
	private boolean isAbstract;
	
	public MyStructImpl (Struct s) {
		
		super(s.getKind());
		super.setElementType(s.getElemType());
		super.setMembers(s.getMembersTable());
		
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
