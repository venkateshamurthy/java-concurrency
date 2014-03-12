/**
 * 
 */
package annotation.experiments;


import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

//Use log4j and hence basic configurator

/**
 * This class demonstrates Automatic ToString, Log4j, Equals and Hashcode
 * @author vmurthy
 * 
 */
@Log4j2
@ToString(/* callSuper = true, */includeFieldNames = true)
@EqualsAndHashCode
public
class Person {
	static  {
		// Add the Basic Configurator for log4j
		log.info("Person.log initialized");
	}
	private String firstName = "Gregory";
	private String lastName = "Peck";
}




