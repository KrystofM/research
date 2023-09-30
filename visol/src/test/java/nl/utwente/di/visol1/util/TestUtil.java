package nl.utwente.di.visol1.util;

import java.util.ArrayList;
import java.util.List;

public class TestUtil {
	public static <T> boolean listEqualsIgnoreOrder(List<T> first, List<T> second) {
		// Why would you do it like this? Is simply checking if both lists contain all the others' elements not enough?
		// No, it is not. Consider lists [a, b, c] and [c, b, a, a]. They are not equal, yet both contain all elements of the other.
		// Okay, then check if the size is equal. That should suffice, nay?
		// Nay, consider [a, b, c, b] and [c, b, a, a]. Both contain all elements of the other, and are of equal size, yet are still not equal.

		if (first == null || second == null) return first == second;
		if (first.size() != second.size()) return false;

		// Copy the lists to avoid modifying them using the pass-by-reference
		List<T> firstCopy = new ArrayList<>(first);
		List<T> secondCopy = new ArrayList<>(second);

		// Sort them so all elements will be in the same order
		firstCopy.sort(null);
		secondCopy.sort(null);

		// Now, we can finally just compare them
		return firstCopy.equals(secondCopy);
	}
}
