package rs.ac.bg.etf.pp1;

public enum SemanticElement {
	
	GLOB_CONST ("global constant"), 
	SYMB_CONST ("symbolic constant"), 
	GLOB_VAR ("global variable"), 
	CLASS ("class"), 
	ABS_CLASS ("abstract class"), 
	GLOB_METH ("global method"), 
	ABS_METH ("abstract class method"), 
	CLASS_METH ("class method"), 
	FORM_PAR ("formal parameter"), 
	LOCAL_VAR ("local variable"),
	CLASS_FIELD ("class field"),
	ELEM ("element of array");
	
	private final String message;
	
	SemanticElement (String message) {
		
		this.message = message;
	
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

}
