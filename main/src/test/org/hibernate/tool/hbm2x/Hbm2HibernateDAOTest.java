/*
 * Created on 2004-12-01
 *
 */
package org.hibernate.tool.hbm2x;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Persistence;

import org.apache.commons.logging.Log;
import org.hibernate.Version;
import org.hibernate.tool.NonReflectiveTestCase;
import org.hibernate.tool.test.TestHelper;

/**
 * @author max
 *
 */
public class Hbm2HibernateDAOTest extends NonReflectiveTestCase {

	public Hbm2HibernateDAOTest(String name) {
		super( name, "hbm2daooutput" );
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		
		
		POJOExporter javaExporter = new POJOExporter(getCfg(), getOutputDir() );
		POJOExporter exporter = new DAOExporter(getCfg(), getOutputDir() );
		exporter.getProperties().setProperty("ejb3", "false");
		exporter.getProperties().setProperty("jdk5", "true");
		exporter.start();
		javaExporter.start();
	}
	
	public void testFileExistence() {
		assertFileAndExists(new File(getOutputDir(), "org/hibernate/tool/hbm2x/ArticleHome.java") );
		assertFileAndExists(new File(getOutputDir(), "org/hibernate/tool/hbm2x/AuthorHome.java") );
	}
	
	public void testCompilable() throws IOException {
		
		generateComparator();
		File file = new File("compilable");
		file.mkdir();
		
		ArrayList<String> list = new ArrayList<String>();
		List<File> jars = new ArrayList<File>();
		jars.add(TestHelper.findJarFileFor(Log.class)); // for commons logging
		jars.add(TestHelper.findJarFileFor(Persistence.class)); // for jpa api
		jars.add(TestHelper.findJarFileFor(Version.class)); // for hibernate core
		TestHelper.compile(getOutputDir(), file, TestHelper.visitAllFiles(getOutputDir(), list), "1.5", TestHelper.buildClasspathFromFileList(jars) );
		
		
		TestHelper.deleteDir(file);
	}
    
	public void testNoVelocityLeftOvers() {
		
		assertEquals(null,findFirstString("$",new File(getOutputDir(), "org/hibernate/tool/hbm2x/ArticleHome.java") ) );
        assertEquals(null,findFirstString("$",new File(getOutputDir(), "org/hibernate/tool/hbm2x/AuthorHome.java") ) );
        
	}

	protected String getBaseForMappings() {
		return "org/hibernate/tool/hbm2x/";
	}
	
	protected String[] getMappings() {
		return new String[] { 
				"Article.hbm.xml",
				"Author.hbm.xml"				
		};
	}

}
