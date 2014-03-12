/**
 * 
 */
package annotation.experiments;
import lombok.extern.log4j.Log4j2;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import annotation.experiments.PersonWithAllAccessors;

/**
 * @author vmurthy
 *
 */
//Log4j Handle creator (from lombok)
@Log4j2
public class TestPersonWithAllAccessors {
	PersonWithAllAccessors p3, p4;

	@Before
	public void before() {
		p3 = new PersonWithAllAccessors();
		p4 =  PersonWithAllAccessors.create("Steve","Souders");
	}
	
	@Test
	public void testToString() {
		log.info(p3);
		log.info(p4);
	}

	@Test
	public void testUnEqualsAndHash() {
		
		Assert.assertFalse("p3 and p4 cannnot be equal",p3.equals(p4));
		Assert.assertFalse("p3 and p4 cannot have same hashCode",p3.hashCode()==p4.hashCode());
	}
	@Test
	public void testEqualsAndHashUsingSetter() {
		log.info("Please check the setter methods without actually being declared!");
		p3.firstName("Steve").lastName("Souders");
		Assert.assertEquals("p3 and p4 should have been equal",p3,p4);
		Assert.assertEquals("p3 and p4 should have had same hashCode",p3.hashCode(),p4.hashCode());
	}
	
	
}
