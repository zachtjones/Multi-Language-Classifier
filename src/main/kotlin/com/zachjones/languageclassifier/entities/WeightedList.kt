package com.zachjones.languageclassifier.entities

import java.io.Serializable
import java.util.function.Predicate
import java.util.stream.Stream

class WeightedList<T> : Serializable {
    private val values: ArrayList<Pair<Double, T>>

    constructor() {
        // empty weights list
        this.values = ArrayList()
    }

    /** Creates an evenly weighted group of size count.  */
    constructor(values: List<T>) {
        this.values = ArrayList()
        for (value in values) {
            this.values.add(Pair(1.0 / values.size, value))
        }
    }

    /** Normalizes this range, setting the sum of the weights to 1.0.
     * Note this returns a new weights list and doesn't modify the old one.  */
    fun normalize(): WeightedList<T> {
        // divide each by the sum
        val sum = values.stream().mapToDouble { i: Pair<Double?, T> -> i.first!! }.sum()
        val returned = WeightedList<T>()
        for (i in this.values) {
            returned.values.add(Pair(i.first/ sum, i.second))
        }
        return returned
    }

    /** returns the weight for the element at index.  */
    fun getWeight(index: Int): Double {
        return values[index].first
    }

    /** Sets the weight at index. Note this requires normalization afterwards.  */
    fun setWeight(index: Int, value: Double) {
        values[index] = Pair<Double, T>(value, values[index].second)
    }

    /** Adds a weight to this list.  */
    fun addWeight(weight: Double, value: T) {
        values.add(Pair(weight, value))
    }

    /** Returns the number of elements in this weighted list.  */
    fun size(): Int {
        return values.size
    }

    /** Creates a stream of these elements.  */
    fun stream(): Stream<Pair<Double, T>> {
        return values.stream()
    }

    /** Creates and returns a weighted list of the elements where the condition passes.  */
    fun valuesWith(condition: Predicate<T>): WeightedList<T> {
        val values = WeightedList<T>()
        for (i in this.values) {
            if (condition.test(i.second)) {
                values.addWeight(i.first, i.second)
            }
        }
        return values
    }

    /** Returns the total weight of this list.  */
    fun totalWeight(): Double {
        return values.stream().mapToDouble { i: Pair<Double, T> -> i.first }.sum()
    }

    /** Returns the pair at the index.  */
    operator fun get(index: Int): Pair<Double, T> {
        return values[index]
    }

    /** Returns the item at the index.  */
    fun getItem(index: Int): T {
        return values[index].second
    }
}
