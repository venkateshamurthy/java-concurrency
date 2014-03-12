/**
 * 
 */
package concurrent.util.collection;

import java.util.Map;


//Using lombok annotation for log4j handle
/**
 * @author vmurthy
 *
 */
//Log4j Handle creator (from lombok)
public interface TestInterface {

	/**
	 * @param s
	 * @param i
	 * @return
	 */
	Integer putIfAbsent(String s, Integer i);

	/**
	 * @return
	 */
	long testAdd();

	/**
	 * @param noThreads
	 * @param mapTest
	 * @return
	 */
	long doTest(int noThreads, TestInterface mapTest);

	/**
	 * @return
	 */
	Map<String, Integer> getM();

}
