package rs.ac.bg.etf.pp1;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
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
	
	/*
	 * (non-Javadoc)
	 * @see rs.etf.pp1.symboltable.concepts.Struct#assignableTo(rs.etf.pp1.symboltable.concepts.Struct)
	 * 
	 * dodela:
	 * ako su ekvivalentni
	 * ako je dst referenca, a src null
	 * ako je dst osnovna, a src izvedena klasa
	 */
	public boolean assignableTo(MyStructImpl dest) {
		
		if (this.equals(dest)) return true;
		
		else if (this == MyTabImpl.noType && dest.isRefType()) return true;
		
		else if (this.getKind() == Array && dest.getKind() == Array && dest.getElemType() == MyTabImpl.noType) return true;
		
		else if (this.getKind() == Class) {
			
			MyStructImpl parent = (MyStructImpl) this;
			
			while (parent != null) {
				
				if (parent.equals(dest)) return true;
				
				else parent = parent.getElemType() == null ? null : (MyStructImpl) parent.getElemType();
				
			}
		}
		
		return false;
		
	}
	
	public boolean compatibleWith(MyStructImpl other) {
		return this.equals(other) || this == MyTabImpl.nullType && other.isRefType()
				|| other == MyTabImpl.nullType && this.isRefType();
	}
	
	public boolean equals(MyStructImpl other) {
		
		if (this.getKind() == Array) return other.getKind() == Array
				&& this.getElemType().equals(other.getElemType());

		if (this.getKind() == Class) return other.getKind() == Class && this.getNumberOfFields() == other.getNumberOfFields()
				&& Obj.equalsCompleteHash(this.getMembersTable(), other.getMembersTable());

		// mora biti isti Struct cvor
		return this.getKind() == other.getKind();
		
	}

}
