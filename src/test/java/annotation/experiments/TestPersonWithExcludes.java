/**
 * 
 */
package annotation.experiments;
import lombok.extern.log4j.Log4j2;

import org.junit.Before;
import org.junit.Test;
//Using lombok annotation for log4j handle
//Use log4j and hence basic configurator

import annotation.experiments.PersonWithExcludes;

/**
 * @author vmurthy
 *
 */
//Log4j Handle creator (from lombok)
@Log4j2
public class TestPersonWithExcludes {
	PersonWithExcludes p3, p4;

	@Before
	public void before() {
		p3 = new PersonWithExcludes();
		p4 = new PersonWithExcludes();
	}
	
	@Test
	public void testToString() {
		log.info(p3);
	}

	@Test
	public void testEqualsAndHash() {
		if(p3.equals(p4) && p3.equals(p4))
		log.error(
				"Both p3 & p4 should have been equal");
		if(p3.hashCode() == p4.hashCode())
		log.error(
				"Expecting hashes to be equal to " + p3.hashCode());
	}

}
