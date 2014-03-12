package util.collection;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.Synchronized;

import org.springframework.util.StringUtils;

/**
 * Lists is a utility Enum to aid in creation with a static typing
 * 
 * @author vmurthy
 * 
 */
public enum Lists {

	safe {
		@Override
		public <T> List<T> create() {
			return new CopyOnWriteArrayList<>();
		}

		@Override
		public <T> List<T> create(int size) {
			throw new UnsupportedOperationException("Not supported with size as parameter ");
		}

		@Override
		public <T> List<T> create(T... tArray) {
			return new CopyOnWriteArrayList<>(tArray);
		}
	},

	array {
		@Override
		public <T> List<T> create() {
			return new ArrayList<>();
		}

		@Override
		public <T> List<T> create(int size) {
			return new ArrayList<>(size);
		}

		@Override
		public <T> List<T> create(T... tArray) {
			return new ArrayList<>(Arrays.asList(tArray));
		}
	},
	linked {
		@Override
		public <T> List<T> create() {
			return new LinkedList<>();
		}

		@Override
		public <T> List<T> create(int size) {
			return create();
		}

		@Override
		public <T> List<T> create(T... tArray) {
			// TODO Auto-generated method stub
			return new LinkedList<>(Arrays.asList(tArray));
		}
	};
	/**
	 * Dont use this base type
	 * 
	 * @return
	 */
	public abstract <T> List<T> create();

	/**
	 * new Collection based on size
	 * 
	 * @param size
	 * @return
	 */
	public abstract <T> List<T> create(int size);
	/**
	 * new Collection based on array
	 * @param tArray
	 * @return
	 */
	public abstract <T> List<T> create(T... tArray);

	/**
	 * Return an array
	 * 
	 * @param size
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T>[] createArray(int size) {
		return (List<T>[]) Array.newInstance(List.class, size);
	}

	/**
	 * An empty List
	 * 
	 * @return
	 */
	public static <T> List<T> empty() {
		return Collections.<T> emptyList();
	}

	/**
	 * Creates list of longs from a csvString
	 * 
	 * @param csvString
	 * @return
	 */
	public static List<Long> createLongListFromCSV(String csvString) {
		List<Long> list = Lists.array.create();
		Set<String> s = StringUtils.commaDelimitedListToSet(csvString);
		for (String a : s)
			list.add(Long.parseLong(a));
		return list;
	}

	/**
	 * Creates list of ints from a csvString
	 * 
	 * @param csvString
	 * @return
	 */
	public static List<Integer> createIntegerListFromCSV(String csvString) {
		List<Integer> list = Lists.array.create();
		Set<String> s = StringUtils.commaDelimitedListToSet(csvString);
		for (String a : s)
			list.add(Integer.parseInt(a));
		return list;
	}

	/**
	 * Creates list of doubles from a csvString
	 * 
	 * @param csvString
	 * @return
	 */
	public static List<Double> createDoubleListFromCSV(String csvString) {
		List<Double> list = Lists.array.create();
		Set<String> s = StringUtils.commaDelimitedListToSet(csvString);
		for (String a : s)
			list.add(Double.parseDouble(a));
		return list;
	}
	@Synchronized()
	public  <T> List<T> create(List<T> originalList){
		List<T> copyList=create();
		copyList.addAll(originalList);
		return copyList;
	}
}