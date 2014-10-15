/**
 * 
 */
package annotation.experiments;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import lombok.Cleanup;
import lombok.extern.log4j.Log4j2;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import annotation.experiments.CloseableResource;

/**
 * @author vmurthy
 * 
 */
// Log4j Handle creator (from lombok)
@Log4j2
public class TestClosableResource {
	FileInputStream fin;
	
	@Test
	public void justTestOpenClose() throws IOException {
		@Cleanup("close")
		FileInputStream in = new FileInputStream("pom.xml") ;
		fin=in;
	}
	@Test
	public void testCleanupByLombok() throws IOException {
		
		CloseableResource cr = CloseableResource.of();
		try {
			cr.lombokCleanUpBasedFiles();
			if(cr.in2().available() == 0)
			log.error( "Oh!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("Invalid handle for in2");
		}
	}
	@Test
	public void testCleanupByJava7() throws IOException {
		
		CloseableResource cr = CloseableResource.of();
		try {
			cr.java7CleanUpBasedFiles();
			if(cr.in2().available() == 0)
			log.warn( "Oh!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//log.error("Invalid handle for in2");
		}
	}
	@After
	public void after() throws IOException {
		if(fin!=null)
		Assert.assertFalse("The fin cannot be valid as its autoclosed in the scope",fin.getFD().valid());
	}
}
