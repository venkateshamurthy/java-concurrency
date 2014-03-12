package annotation.experiments;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

/**
 * This class demonstrates Automatic ToString, Log4j, Equals and Hashcode
 * @author vmurthy
 *
 */
@Log4j2
@ToString(/* callSuper = true, */includeFieldNames = true,exclude= {"myName"})
@EqualsAndHashCode(exclude="myName")
public
class PersonWithExcludes{
	static  {
		// Add the Basic Configurator for log4j
		log.info("Wow! I am ready even without declaring!!. Thanks to Lombok");
	}
	private String myName = "George";
	private String familyName = "Kennedy";

}
