/**
 * 
 */
package annotation.experiments;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import lombok.Cleanup;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * @author vmurthy
 * 
 */
// Log4j Handle creator (from lombok)
@Log4j2
@Data
@Accessors(fluent = true)
@NoArgsConstructor(staticName = "of")
public class CloseableResource {
	Closeable c;

	InputStream in2;
	OutputStream out2;
	final String input = "c:/hosts";
	final String output = "c:/vmurthy/b.txt";

	public void workWithDB() throws SQLException {
		@Cleanup
		Connection c = DriverManager.getConnection("");
	}
	public void lombokCleanUpBasedFiles() throws IOException {
		
		@Cleanup
		InputStream in = new FileInputStream(input) {
			public void close() throws IOException {
				super.close();
				if(!getFD().valid())
				log.error(input
						+ " InputStream FD shouldnt be valid!!"
						+ getFD().valid());
				log.info("Closing.."+input);
			}
		};
		in2(in);

		@Cleanup
		OutputStream out = new FileOutputStream(output) {
			public void close() throws IOException {
				super.close();
				if(!getFD().valid())
				log.error( output
						+ " OutputStream FD shouldnt be valid!!"
						+ getFD().valid());
				log.info("Closing.."+output);
			}
		};
		out2(out);
		byte[] b = new byte[10000];
		while (true) {
			int r = in.read(b);
			if (r == -1)
				break;
			out.write(b, 0, r);
		}
		log.info("Read and writes done to:" + output);
	}

	public void java7CleanUpBasedFiles() throws IOException {
		try (InputStream in = new FileInputStream("c:/hosts") {
			public void close() throws IOException {
				super.close();
				if(!getFD().valid())
				log.error("Input FD shouldnt be valid!!"
						+ getFD().valid());
				log.info("Closing.."+input);
			}
		}) {
			in2(in);
			try (OutputStream out = new FileOutputStream("c:/vmurthy/b.txt") {
				public void close() throws IOException {
					super.close();
					if(!getFD().valid())
					log.error( "Output FD shouldnt be valid!!"
							+ getFD().valid());
					log.info("Closing.."+output);
				}
			}) {
				byte[] b = new byte[10000];
				while (true) {
					int r = in.read(b);
					if (r == -1)
						break;
					out.write(b, 0, r);
				}
				out2(out);
				log.info("Read and writes done to:" + output);
			}
		}
	}

}
