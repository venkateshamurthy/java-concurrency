/**
 * 
 */
package generics.examples;
import java.util.ArrayList;

import java.util.List;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
//Using lombok annotation for log4j handle
//Use log4j and hence basic configurator
/**
 * @author vmurthy
 *
 */
//Log4j Handle creator (from lombok)
@Log4j2
@NoArgsConstructor
public class ArrayGenericsCovariance {
	
	public static void main(String[] args) {
		new ArrayGenericsCovariance().verifyArrayCoVariance();
		new ArrayGenericsCovariance().verifyCollectionCoVariance();
	}
	public void verifyArrayCoVariance() {
		Integer[] i=new Integer[10];
		Number[] n=new Number[10];
		for(int j=0;j<i.length;j++)
			i[j]=j;
		n=i;
		//n[3]=3.2f;
		for(int k=0;k<i.length;k++)
			log.info(String.format("i[%d]=%d, n[%d]=%d",k,i[k],k,n[k]));
	}
	
	public void verifyCollectionCoVariance() {
		List<Integer> i=new ArrayList<>(10);
		List<Number> n=new ArrayList<>(10);
		for(int j=0;j<10;j++)
			i.add(j);
		for(int k:i)
			log.info(k);
		//Un-comment this to show compiler error 
		//n=i;
		n.addAll(i);//this is allowed however u cant assign n=i
		n.add(3.2f); //this is also allowed to collect float numbers along with ints
		for(Object k:n)
			log.info(k);
	}
}
