package org.hibernate.tool.hbm2x;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import javax.xml.parsers.SAXParserFactory;

import org.hibernate.tool.NonReflectiveTestCase;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

public class DocExporterTest extends NonReflectiveTestCase {

	private boolean ignoreDot;

	public DocExporterTest(String name) {
		super( name, "docoutput" );
	}


	protected String[] getMappings() {
		return new String[] { 
				"Customer.hbm.xml",
				"Order.hbm.xml",
				"LineItem.hbm.xml",
				"Product.hbm.xml",
				"HelloWorld.hbm.xml",
				"UnionSubclass.hbm.xml",
				"DependentValue.hbm.xml"
		};
	}
	
	protected String getBaseForMappings() {
		return "org/hibernate/tool/hbm2x/";
	}
	protected void setUp() throws Exception {
		super.setUp();
		DocExporter exporter = new DocExporter(getCfg(), getOutputDir() );
		Properties properties = new Properties();
		properties.put( "jdk5", "true"); // test generics
		if(File.pathSeparator.equals(";")) { // to work around windows/jvm not seeming to respect executing just "dot"
			properties.put("dot.executable", System.getProperties().getProperty("dot.executable","dot.exe"));
		} else {
			properties.put("dot.executable", System.getProperties().getProperty("dot.executable","dot"));
		}
		
		// Set to ignore dot error if dot exec not specfically set.
		// done to avoid test failure when no dot available.
		boolean dotSpecified = System.getProperties().containsKey("dot.executable");
		ignoreDot =  !dotSpecified;
		
		properties.setProperty("dot.ignoreerror", Boolean.toString(ignoreDot));
		
		exporter.setProperties( properties );
		exporter.start();
	}
	
	
    public void testExporter() {
    	
    	assertFileAndExists(new File(getOutputDir(), "index.html") );
	 
    	assertFileAndExists(new File(getOutputDir(), "assets/doc-style.css") );
    	assertFileAndExists(new File(getOutputDir(), "assets/hibernate_logo.gif") );
    	
    	assertFileAndExists(new File(getOutputDir(), "tables/PUBLIC.PUBLIC/summary.html") );
    	assertFileAndExists(new File(getOutputDir(), "tables/PUBLIC.PUBLIC/Customer.html") );
    	assertFalse(new File(getOutputDir(), "tables/PUBLIC.PUBLIC/UPerson.html").exists() );
    	assertFileAndExists(new File(getOutputDir(), "tables/PUBLIC.CROWN/CROWN_USERS.html") );
    	
    	assertFileAndExists(new File(getOutputDir(), "entities/org/hibernate/tool/hbm2x/Customer.html") );
    	assertTrue(new File(getOutputDir(), "entities/org/hibernate/tool/hbm2x/UPerson.html").exists() );
    	assertFileAndExists(new File(getOutputDir(), "entities/org/hibernate/tool/hbm2x/UUser.html") );
    	
		if (!ignoreDot) {
			assertFileAndExists(new File(getOutputDir(), "entities/entitygraph.dot"));
			assertFileAndExists(new File(getOutputDir(), "entities/entitygraph.png"));
			assertFileAndExists(new File(getOutputDir(), "tables/tablegraph.dot"));
	    	assertFileAndExists(new File(getOutputDir(), "tables/tablegraph.png"));
	    	
		}
		
    	    	new FileVisitor() {
    			protected void process(File dir) {
    				if(dir.isFile() && dir.getName().endsWith( ".html" )) {
    					testHtml(dir);
    				}
    				
    			}
    	}.visit( getOutputDir() );
    	
    	
	}
    
    public void testCommentIncluded() {
    	//A unique customer comment!
    	File tableFile = new File(getOutputDir(), "tables/PUBLIC.PUBLIC/Customer.html");
		assertFileAndExists(tableFile );
		
		assertNotNull(findFirstString("A unique customer comment!", tableFile));
    }
    
    public void testGenericsRenderedCorrectly() {
//    	A unique customer comment!
    	File tableFile = new File(getOutputDir(), "entities/org/hibernate/tool/hbm2x/Customer.html");
		assertFileAndExists(tableFile );
		
		assertEquals("Generics syntax should not occur verbatim in html",null,findFirstString("List<", tableFile));
		assertNotNull("Generics syntax occur verbatim in html",findFirstString("List&lt;", tableFile));
    }
    
	public void testInheritedProperties()
	{
		File entityFile = new File(getOutputDir(), "entities/org/hibernate/tool/hbm2x/UUser.html");
		assertFileAndExists(entityFile);

		assertNotNull("Missing inherited property", findFirstString("firstName", entityFile));
	}

	private void testHtml(File file) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			XMLReader parser = factory.newSAXParser().getXMLReader();
			TestHandler handler = new TestHandler();
			parser.setErrorHandler(handler);
			parser.setEntityResolver(new TestResolver());
			parser.parse(new InputSource(new FileInputStream(file)));
			assertEquals(file + "has errors ", 0, handler.errors);
			assertEquals(file + "has warnings ", 0, handler.warnings);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	private class TestResolver implements EntityResolver {
		@Override
		public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
			return new InputSource(new StringReader(""));
		}		
	}
	
	private class TestHandler implements ErrorHandler {
		int warnings = 0;
		int errors = 0;
		@Override
		public void warning(SAXParseException exception) throws SAXException {
			warnings++;
		}
		@Override
		public void error(SAXParseException exception) throws SAXException {
			errors++;
		}
		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			errors++;
		}		
	}
	
}
