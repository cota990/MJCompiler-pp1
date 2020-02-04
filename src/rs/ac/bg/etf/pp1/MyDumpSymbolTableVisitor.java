package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Scope;
import rs.etf.pp1.symboltable.concepts.Struct;
import rs.etf.pp1.symboltable.visitors.SymbolTableVisitor;

public class MyDumpSymbolTableVisitor extends SymbolTableVisitor {
	
	Logger log = Logger.getLogger(MyDumpSymbolTableVisitor.class);
	
	protected StringBuilder output = new StringBuilder();
	protected final String indent = "   ";
	protected StringBuilder currentIndent = new StringBuilder();
	
	protected void nextIndentationLevel() {
		
		currentIndent.append(indent);
		
	}
	
	protected void previousIndentationLevel() {
		
		if (currentIndent.length() > 0)
			currentIndent.setLength(currentIndent.length()-indent.length());
		
	}

	@Override
	public void visitObjNode(Obj objToVisit) {
		
		//output.append("[");
		switch (objToVisit.getKind()) {
		case Obj.Con:  output.append("Con "); break;
		case Obj.Var:  output.append("Var "); break;
		case Obj.Type: output.append("Type "); break;
		case Obj.Meth: {
			if (objToVisit instanceof MyObjImpl) 
				if (((MyObjImpl) objToVisit).isAbstract()) output.append("Abstract ");
			output.append("Meth "); break;
		}
		case Obj.Fld:  output.append("Fld "); break;
		case Obj.Prog: output.append("Prog "); break;
		}
		
		output.append(objToVisit.getName());
		output.append(": ");
		
		if ((Obj.Var == objToVisit.getKind()) && "this".equalsIgnoreCase(objToVisit.getName()))
			output.append("");
		else if ((Obj.Var == objToVisit.getKind()) && objToVisit.getType().getKind() == Struct.Class) {
			
			log.info("TRAZI TIP (IME) KLASE");
			
			log.info(Tab.currentScope().getLocals().symbols().size());
			
			for (Obj prog : Tab.currentScope().getLocals().symbols()) {
				
				boolean found = false;
							
				if (prog.getKind() == Obj.Prog) {
					
					for (Obj type : prog.getLocalSymbols() ) {
						
						if (type.getType().equals(objToVisit.getType())) {
							
							output.append(type.getName());
							found = true;
							break;
						}
							
					}					
					
				}
				
				if (found) break;
			}
		}
		else
			objToVisit.getType().accept(this);
		
		output.append(", ");
		output.append(objToVisit.getAdr());
		output.append(", ");
		output.append(objToVisit.getLevel() + " ");
				
		if (objToVisit.getKind() == Obj.Prog || objToVisit.getKind() == Obj.Meth) {
			output.append("\n");
			nextIndentationLevel();
		}
		

		for (Obj o : objToVisit.getLocalSymbols()) {
			output.append(currentIndent.toString());
			o.accept(this);
			output.append("\n");
		}
		
		if (objToVisit.getKind() == Obj.Prog || objToVisit.getKind() == Obj.Meth) 
			previousIndentationLevel();

		//output.append("]");
		
	}

	@Override
	public void visitScopeNode(Scope scope) {
		
		for (Obj o : scope.values()) {
			o.accept(this);
			output.append("\n");
		}
		
	}

	@Override
	public void visitStructNode(Struct structToVisit) {
		
		switch (structToVisit.getKind()) {
		case Struct.None:
			output.append("notype");
			break;
		case Struct.Int:
			output.append("int");
			break;
		case Struct.Char:
			output.append("char");
			break;
		case Struct.Bool:
			output.append("bool");
			break;
		case Struct.Array:
			output.append("Arr of ");
			
			switch (structToVisit.getElemType().getKind()) {
			case Struct.None:
				output.append("notype");
				break;
			case Struct.Int:
				output.append("int");
				break;
			case Struct.Char:
				output.append("char");
				break;
			case Struct.Bool:
				output.append("bool");
				break;
			case Struct.Class:
				output.append("Class");
				break;
			}
			break;
		case Struct.Class:
			if (structToVisit instanceof MyStructImpl) 
				if (((MyStructImpl) structToVisit).isAbstract()) output.append("Abstract ");
			
			output.append("Class [");
			for (Obj obj : structToVisit.getMembers()) {
				obj.accept(this);
			}
			output.append("]");
			break;
		}

	}

	@Override
	public String getOutput() {
		
		return output.toString();
		
	}

}
