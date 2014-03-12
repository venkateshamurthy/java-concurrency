/**
 * 
 */
package annotation.experiments;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
//Using lombok annotation for log4j handle
//Use log4j and hence basic configurator

@Log4j2
@Accessors(fluent=true)
@Data
@AllArgsConstructor(staticName="create")
@NoArgsConstructor
public
class PersonWithAllAccessors{
	static  {
		// Add the Basic Configurator for log4j
		log.info("Wow! I am ready even without declaring!!. Thanks to Lombok");
	}
	
	private String firstName = "Gregory";
	private String lastName = "Peck";
}