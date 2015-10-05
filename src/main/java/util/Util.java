package util;

import com.google.common.base.Optional;

import concurrent.util.contextual.TaskContext;


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
	
	public static TaskContext<String> StringCtx = new TaskContext<String>(){
		@Override
		public String getContext() {
			return "StringContext";
		}
	};
	public static TaskContext<Integer> intCtx = new TaskContext<Integer>(){
		@Override
		public Integer getContext() {
			return 10;
		}
	}; 
	public static <T> TaskContext<T> makeCtx(Object o){
		try{
			return (TaskContext<T>)o;
		
		}catch(Throwable t){
			t.printStackTrace();
			return TaskContext.NONE;
		}
	}
	public static void main(String[] args){
		TaskContext<String> t=makeCtx(StringCtx);
		System.out.println(t.getContext());
		t=makeCtx(intCtx);
		System.out.println(t.getContext());
	}
}
