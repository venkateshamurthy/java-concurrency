/**
 * 
 */
package designpatterns;

import java.util.Collections;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.Builder;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;//Using lombok annotation for log4j handle
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.Range;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.StringFormatterMessageFactory;
import org.springframework.util.Assert;

/**
 * @author vmurthy
 * 
 */
// Log4j Handle creator (from lombok)
@Log4j2
@Data
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BuilderExample {
	static final Logger log = LogManager
			.getLogger(StringFormatterMessageFactory.INSTANCE);

	public static void main(String[] args) {
		PreparedStatement psmnt = PreparedStatement.Builder.builder().name("name")
				.sql("")
				.resultSetConcurrency(java.sql.ResultSet.TYPE_FORWARD_ONLY)
				.paramMap(Collections.singletonMap("K", (Object)"V"))
				.build();
		log.info(psmnt.toString());
	}

}

@Data(staticConstructor = "of")
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Builder
class PreparedStatement {
	@NotEmpty
	String name;
	@NotEmpty
	String sql;
	@Range(max = java.sql.ResultSet.TYPE_SCROLL_SENSITIVE, min = java.sql.ResultSet.TYPE_FORWARD_ONLY)
	Integer resultSetType;
	
	Integer resultSetConcurrency;

	Integer resultSetHoldability;
	@NotEmpty
	Map<String, Object> paramMap;
	
	/**
	 * This Builder is extending from Lombok created builder just to add build time invariant tests
	 * @author vmurthy
	 *
	 */
	@Data(staticConstructor = "builder")
	public static class Builder extends PreparedStatementBuilder {
		@Override
		public PreparedStatement build() {
			Assert.notEmpty(super.paramMap);
			Assert.hasText(super.name);
			Assert.hasText(super.sql);
			return  super.build();
		}

	}

}