/**

 * 
 */
package util;
import java.io.IOException;
import java.io.OutputStream;

import lombok.extern.log4j.Log4j2;
//Using lombok annotation for log4j handle
//Use log4j and hence basic configurator
/**
 * @author vmurthy
 *
 */
//Log4j Handle creator (from lombok)
@Log4j2
public class NullOutputStream extends OutputStream {

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {
		// TODO Auto-generated method stub

	}
}
