package util;


public class Util {

	/**
	 * cast method is a small nice-to-have work around to remove dreaded yellow
	 * line in eclipse indicating that  must be used to
	 * indicate the unchecked cast
	 * 
	 * @param x
	 * @param defaultValue
	 * @return casted object or null or a default value
	 */
	@SuppressWarnings("unchecked")
	public static <T> T cast(Object x, T defaultValue) {
		T t;
		try {
			t= (x != null) ? (T) x : (T)null;
		} catch (Throwable e) {
			e.printStackTrace();
			//using class cast exception as an easier way 
			//to deal than finding out generic class type 
			t= defaultValue;
		}
		return t;
	}

	/**
	 * 
	 * @param x
	 * @return casted object or null
	 */
	public static <T> T cast(Object x) {
		return cast(x, (T)null);
	}
	
}
