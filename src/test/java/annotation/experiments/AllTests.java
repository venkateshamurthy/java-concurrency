/**
 * 
 */
package annotation.experiments;

import lombok.extern.log4j.Log4j2;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author vmurthy
 * 
 */
// Log4j Handle creator (from lombok)
@Log4j2
@RunWith(Suite.class)
@SuiteClasses({ TestPerson.class, TestPersonWithExcludes.class,
		TestPersonWithAllAccessors.class, TestClosableResource.class,
		TestImmutablePerson.class, TestPersonWithChecks.class })
public class AllTests {
	@BeforeClass
	public static void beforeClass() {
		// Add the Basic Configurator for log4j
		log.info("AllTests..");

	}
}
