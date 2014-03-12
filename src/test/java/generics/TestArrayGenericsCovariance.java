/**
 * 
 */
package generics;
import static org.junit.Assert.fail;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.junit.Test;
/**
 * @author vmurthy
 *
 */
@Log4j2
@NoArgsConstructor
public class TestArrayGenericsCovariance {
	@Test(expected=ArrayStoreException.class)
	public void verifyArrayCoVariance() {
		Integer[] i=new Integer[10];
		Number[] n=new Number[10];
		for(int j=0;j<i.length;j++)
			i[j]=j;
		n=i;
		n[3]=3.2f;//Hey this must throw exception
		
		for(int k=0;k<i.length;k++)
			log.info(String.format("i[%d]=%d, n[%d]=%f",k,i[k],k,n[k]));
		fail("It cannot proceed here as we expect ArrayStoreException");
	}
	
}
