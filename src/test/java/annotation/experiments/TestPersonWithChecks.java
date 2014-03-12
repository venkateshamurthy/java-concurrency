/**
 * 
 */
package annotation.experiments;
import lombok.extern.log4j.Log4j2;
import net.sf.oval.ConstraintViolation;
import net.sf.oval.exception.ConstraintsViolatedException;

import org.junit.Test;

import annotation.experiments.PersonWithChecks;
//Using lombok annotation for log4j handle
//Use log4j and hence basic configurator
/**
 * @author vmurthy
 *
 */
//Log4j Handle creator (from lombok)
@Log4j2
public class TestPersonWithChecks {

	@Test
	public void test() {
		PersonWithChecks p=new PersonWithChecks("a", "b", "", "abb", "abb");
		try{p.validate();}
		catch(ConstraintsViolatedException ce) {
			for(ConstraintViolation e:ce.getConstraintViolations())log.error(e);
		}
	}
	
	@Test
	public void test1() {
		PersonWithChecks p=new PersonWithChecks("avnvg", "byuuy", "","1970-06-24","1995-03-01");
		p.address("bchghgh");
		try{p.validate();}
		catch(ConstraintsViolatedException ce) {
			for(ConstraintViolation e:ce.getConstraintViolations())log.error(e);
		}
		
	}
}
