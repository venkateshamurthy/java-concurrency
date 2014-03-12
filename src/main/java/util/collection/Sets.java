package util.collection;


import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
/**
 * Sets is a utility Enum to aid in creation with a static typing
 * @author vmurthy
 *
 */
public enum Sets {
	concurrentskip{

		@Override
		public <T> Set<T> create() {
			// TODO Auto-generated method stub
			return new ConcurrentSkipListSet<>();
		}

		@Override
		public <T> Set<T> create(int size) {
			// TODO Auto-generated method stub
			 return new ConcurrentSkipListSet<>();
		}
		
	},
	hash{
		@Override
		public <T> Set<T> create(){
			return new HashSet<>();
		}
		@Override
		public <T> Set<T> create(int size){
			return new HashSet<>(size);
		}
	},
	linked{
		@Override
		public <T> Set<T> create(){
			return new LinkedHashSet<>();
		}
		@Override
		public <T> Set<T> create(int size){
			return new LinkedHashSet<>(size);
		}
	};
	/**
	 * Dont use this base type
	 * @return
	 */
	public  abstract <T>  Set<T> create();
	/**
	 * new Collection based on size
	 * @param size
	 * @return
	 */
	public  abstract <T>  Set<T> create(int size);
	/**
	 * Return an array
	 * @param size
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T>  Set<T>[] createArray(int size){
		return (Set<T>[]) Array.newInstance(Set.class,size);
	}
	/**
	 * An empty Set
	 * @return
	 */
	public static <T> Set<T> empty(){
		return Collections.<T> emptySet();
	}

}
