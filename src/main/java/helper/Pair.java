package helper;

import java.io.Serializable;

/** Represents a pair of items */
public class Pair<T1 extends Serializable, T2 extends Serializable> implements Serializable {
	public final T1 one;
	public final T2 two;

	public Pair(T1 one, T2 two) {
		this.one = one;
		this.two = two;
	}
}
