package helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class WeightedList<T extends Serializable> implements Serializable {

	private final ArrayList<Pair<Double, T>> values;

	public WeightedList() {
		// empty weights list
		this.values = new ArrayList<>();
	}

	/** Creates an evenly weighted group of size count. */
	public WeightedList(List<T> values) {
		this.values = new ArrayList<>();
		for (T value : values) {
			this.values.add(new Pair<>(1.0 / values.size(), value));
		}
	}

	/** Normalizes this range, setting the sum of the weights to 1.0.
	 * Note this returns a new weights list and doesn't modify the old one. */
	public WeightedList<T> normalize() {
		// divide each by the sum
		double sum = values.stream().mapToDouble(i -> i.one).sum();
		WeightedList<T> returned = new WeightedList<>();
		for (Pair<Double, T> i : this.values) {
			returned.values.add(new Pair<>(i.one / sum, i.two));
		}
		return returned;
	}

	/** returns the weight for the element at index. */
	public double getWeight(int index) {
		return values.get(index).one;
	}

	/** Sets the weight at index. Note this requires normalization afterwards. */
	public void setWeight(int index, double value) {
		values.set(index, new Pair<>(value, values.get(index).two));
	}

	/** Adds a weight to this list. */
	public void addWeight(double weight, T value) {
		values.add(new Pair<>(weight, value));
	}

	/** Returns the number of elements in this weighted list. */
	public int size() {
		return values.size();
	}

	/** Creates a stream of these elements. */
	public Stream<Pair<Double, T>> stream() {
		return values.stream();
	}

	/** Creates and returns a weighted list of the elements where the condition passes. */
	public WeightedList<T> valuesWith(Predicate<T> condition) {
		WeightedList<T> val = new WeightedList<>();
		for (Pair<Double, T> i : values) {
			if (condition.test(i.two)) {
				val.addWeight(i.one, i.two);
			}
		}
		return val;
	}

	/** Returns the total weight of this list. */
	public double totalWeight() {
		return values.stream().mapToDouble(i -> i.one).sum();
	}

	/** Returns the pair at the index. */
	public Pair<Double, T> get(int index) {
		return values.get(index);
	}

	/** Returns the item at the index. */
    public T getItem(int index) {
		return values.get(index).two;
    }
}
