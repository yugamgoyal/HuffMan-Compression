import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PrQ<E  extends Comparable<E>> {

	public Map<Integer, AbstractData<E>> storedData;
	public Object[] con;
	private int size;

	public PrQ() {
		storedData = new HashMap<>();
		con = (new Object[10]);
	}

	public boolean enqueue(E data) {
		int vall = data.hashCode(); 
		AbstractData<E> val = storedData.get(vall);

		if (size >= con.length - 1) {
			enlargeArray(con.length * 2 + 1);
		}

		size++;
		int indexToPlace = size;
		AbstractData<E> tempVal = (AbstractData<E>) (con[indexToPlace / 2]);
		while ((indexToPlace > 1) && (data.compareTo(tempVal.getData()) < 0)) {
			con[indexToPlace] = con[indexToPlace / 2]; // swap
			indexToPlace /= 2; // change indexToPlace to parent
			tempVal = (AbstractData<E>) (con[indexToPlace / 2]);
		}

		if (val == null) {
			val = new AbstractData<E>(data);
			storedData.put(data.hashCode(), val);
		} else {
			val.add(data);
		}

		con[indexToPlace] = val;

		return true;
	}

	private void enlargeArray(int newSize) {
		Object[] temp = (new Object[size * 2 + 10]);
		System.arraycopy(con, 1, temp, 1, size);
		con = temp;
	}

	public String toString() {

		StringBuilder temp = new StringBuilder();
		temp.append("[");
		for (int x = 1; x < size; x++) {
			temp.append(con[x].toString() + ", ");
		}
		temp.append(con[size].toString() + "]");
		return temp.toString();
	}

	public int size() {
		return size;
	}

	public E dequeue() {

		AbstractData<E> top = (AbstractData<E>) con[1];
		// System.out.println("List Value for "  + top.getList().toString());
		AbstractData<E> said = storedData.get(top.getData().hashCode());
		int hole = 1;
		boolean done = false;
		while (hole * 2 < size && !done) {
			int child = hole * 2;
			// see which child is smaller
			AbstractData<E> childVal = (AbstractData<E>) con[child];
			AbstractData<E> child2 = (AbstractData<E>) con[child + 1];
			if (childVal.getData().compareTo(child2.getData()) > 0)
				child++; // child now points to smaller
			// is replacement value bigger than child?
			childVal = (AbstractData<E>) con[child];
			child2 = (AbstractData<E>) con[child + 1];
			AbstractData<E> child3 = (AbstractData<E>) con[size];
			if (child3.getData().compareTo(childVal.getData()) > 0) {
				con[hole] = con[child];
				hole = child;
			} else {
				done = true;
			}
		}
		con[hole] = con[size];
		size--;
		E data = said.value.remove();
		if (storedData.get(data.hashCode()).value.size() == 0) {
			storedData.remove(data.hashCode());
		}
		return data;

	}

	public E returnData() {

		if (this.isEmpty()) {
			throw new IllegalArgumentException("size == 0");
		}

		AbstractData<E> childVal = (AbstractData<E>) con[1];
		return childVal.getData();
	}

	public boolean isEmpty() {

		return size == 0;
	}

	public boolean isMoreThanOne() {
		return size > 1;
	}
}
