/**
 * 
 */
package annotation.experiments;

import lombok.extern.log4j.Log4j2;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import annotation.experiments.Person;

/**
 * @author vmurthy
 * 
 */
// Log4j Handle creator (from lombok)
@Log4j2
public class TestPerson {
	@BeforeClass
	public static void beforeClass() {
		log.info("Wow! I am ready even without declaring!!. Thanks to Lombok");
	}

	Person p, p1, p2;

	@Before
	public void before() {
		p = new Person();
		p1 = new Person();
		p2 = new Person();
	}

	@Test
	public void testToString() {
		log.info(p);
	}

	@Test
	public void testEqualsAndHash() {
		if(p1.equals(p2) && p2.equals(p1))
		log.error(
				"Both p1 and p2 should have been equal");
		if(p.hashCode() == p2.hashCode())
		log.error(
				"Expecting hashes to be equal to " + p1.hashCode());
	}
}
