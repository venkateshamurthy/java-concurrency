/**
 * 
 */
package annotation.experiments;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * @author vmurthy
 *
 */
//Log4j Handle creator (from lombok)
@Log4j2
@Getter
@AllArgsConstructor(staticName="of")
public class ImmutablePerson {
	
	private String name;
}

@net.jcip.annotations.Immutable
@AllArgsConstructor
class ImmutablePerson2 {
	
	private String name;
}