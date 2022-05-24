import java.util.LinkedList;

public class AbstractData<E> {

	public E data;
	public LinkedList<E> value;

	public AbstractData(E data) {
		this.data = data;
		value = new LinkedList<E>();
		value.add(data);
	}

	public E getData() {
		return data;
	}

	public LinkedList<E> getList() {
		return value;
	}

	public boolean add(E dataVal) {
		return value.add(dataVal);
	}
	
	public String toString() {
		return data.toString(); 
	}
}
