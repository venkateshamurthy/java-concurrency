/**
 * 
 */
package annotation.experiments;
import lombok.extern.log4j.Log4j2;

import org.junit.Test;

import annotation.experiments.ImmutablePerson;
/**
 * @author vmurthy
 *
 */
//Log4j Handle creator (from lombok)
@Log4j2
public class TestImmutablePerson {
	@Test
	public void test1() {
		ImmutablePerson ip=ImmutablePerson.of("Iam Immutable");
		//Un comment it to show that its erroring ip.setName("Can u mute");
		ip.getName();
	}
	
}
