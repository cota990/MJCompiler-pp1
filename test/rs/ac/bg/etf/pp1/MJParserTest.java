package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java_cup.runtime.Symbol;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;

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
			
						
			MyTabImpl.init();
			//Tab.currentScope.addToLocals(new Obj (Obj.Type, "bool", new Struct (Struct.Bool), -1, -1));
			
			SemanticAnalyzer v = new SemanticAnalyzer();
			prog.traverseBottomUp(v); 
			
			MyDumpSymbolTableVisitor stv = new MyDumpSymbolTableVisitor ();
			if (!v.mainFound()) {
				
				log.error("Semantic error: No main method declared");
				v.setSemanticErrorFound();
				
			}
			
			MyTabImpl.dump(stv); 
			
			if (v.semanticErrorFound())
				log.info("Semantic errors found; code will not be generated");				
			else
				log.info("No semantic errors found; proceed to code generation");	
			
			if (!p.syntaxErrorFound && !v.semanticErrorFound()) {
				
				log.info("CODE GENERATION");
				
				File objectFile = new File ("test/program.obj");
				if (objectFile.exists()) objectFile.delete();
				
				CodeGenerator cg = new CodeGenerator();
				
				prog.traverseBottomUp(cg);
				
				Code.dataSize = cg.getnVars();
				//Code.dataSize = 5;
				Code.mainPc = cg.getMainPc();
				Code.write(new FileOutputStream(objectFile));
				
				log.info("PARSING COMPLETED");
				
			}
		} 
		finally {
			if (br != null) try { br.close(); } catch (IOException e1) { log.error(e1.getMessage(), e1); }
		}

	}
	
	
}
