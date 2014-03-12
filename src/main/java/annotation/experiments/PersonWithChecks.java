/**
 * 
 */
package annotation.experiments;

import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import net.sf.oval.ConstraintViolation;
import net.sf.oval.Validator;
import net.sf.oval.constraint.AssertFieldConstraints;
import net.sf.oval.constraint.DateRange;
import net.sf.oval.constraint.Length;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.exception.ConstraintsViolatedException;
import net.sf.oval.guard.Guarded;
import net.sf.oval.localization.message.ResourceBundleMessageResolver;
//Using lombok annotation for log4j handle
//Use log4j and hence basic configurator

/**
 * @author vmurthy
 * 
 */
// Log4j Handle creator (from lombok)
@Log4j2
@Accessors(fluent = true)
@Data
@AllArgsConstructor
@Guarded(applyFieldConstraintsToSetters = true, applyFieldConstraintsToConstructors = true)
public class PersonWithChecks {
	/**
	 * 
	 */
	protected static final String DATE_RANGE_VIOLATED = "net.sf.oval.constraint.DateRange.violated";
	static Validator validator = new Validator();
	static final String DATE_FMT = "yyyy-MM-dd";

	static {
		// Add the Basic Configurator for log4j
		ResourceBundleMessageResolver.INSTANCE.addMessageBundle(ResourceBundle
				.getBundle("myMessages"));
	}

	@NonNull	@NotBlank	@Length(min = 5, max = 10)
	private String firstName;

	public PersonWithChecks firstName(@AssertFieldConstraints String firstName) {
		this.firstName = firstName;
		return this;
	}

	@NonNull	@NotBlank	@Length(min = 5, max = 10)
	private String lastName;

	@NonNull	@NotBlank	@Length(min = 5, max = 45)
	private String address;

	@DateRange(format = DATE_FMT, errorCode = DATE_RANGE_VIOLATED)
	private String dateOfBirth;

	@DateRange(format = DATE_FMT, errorCode = DATE_RANGE_VIOLATED)
	private String dateOfJoining;

	@PostConstruct
	public void validate() throws ConstraintsViolatedException {
		List<ConstraintViolation> listErrors = validator.validate(this);
		if (listErrors.isEmpty())
			log.info(this);
		else
			throw new ConstraintsViolatedException(listErrors);
	}
}


