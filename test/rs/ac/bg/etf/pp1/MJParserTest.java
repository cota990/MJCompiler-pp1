package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java_cup.runtime.Symbol;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;
import rs.etf.pp1.symboltable.visitors.DumpSymbolTableVisitor;
import rs.etf.pp1.symboltable.visitors.SymbolTableVisitor;

public class MJParserTest {

	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}
	
	public static void main(String[] args) throws Exception {
		
		Logger log = Logger.getLogger(MJParserTest.class);
		
		Reader br = null;
		try {
			File sourceCode = new File("test/program.mj");
			log.info("Compiling source file: " + sourceCode.getAbsolutePath());
			
			br = new BufferedReader(new FileReader(sourceCode));
			Yylex lexer = new Yylex(br);
			
			MJParser p = new MJParser(lexer);
	        Symbol s = p.parse();  //pocetak parsiranja
	        
	        Program prog = (Program)(s.value); 
			// ispis sintaksnog stabla
			log.info("\n" + prog.toString("   "));
			log.info("===================================");
			if (p.syntaxErrorFound) {
				
				log.info("Syntax errors found; parsing interrupted");				
				
			}
			else {
				
				log.info("No Syntax errors found; proceed to semantic analysis");				
				
			}
			
						
			Tab.init();
			Tab.currentScope.addToLocals(new Obj (Obj.Type, "bool", new Struct (Struct.Bool), -1, -1));
			//Tab.currentScope.addToLocals(new Obj (Obj.Type, "enum", new Struct(Struct.Enum), -1, -1));
			//Tab.currentScope.addToLocals(new Obj (Obj.Type, "interface", new Struct(Struct.Interface), -1, -1));
			
			SemanticAnalyzer v = new SemanticAnalyzer();
			prog.traverseBottomUp(v); 
			
			MyDumpSymbolTableVisitor stv = new MyDumpSymbolTableVisitor ();
			Tab.dump(stv); 
			
			if (v.semanticErrorFound())
				log.info("Semantic errors found; code will not be generated");				
			else
				log.info("No semantic errors found; proceed to code generation");				

			// ispis prepoznatih programskih konstrukcija
			//SemanticAnalyzer v = new SemanticAnalyzer();
			//prog.traverseBottomUp(v); 
			
			//prog.traverseTopDown(v);
			
		} 
		finally {
			if (br != null) try { br.close(); } catch (IOException e1) { log.error(e1.getMessage(), e1); }
		}

	}
	
	
}
