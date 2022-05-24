/*  Student information for assignment:
 *
 *  On MY honor Yugam Goyal, this programming assignment is MY own work
 *  and I have not provided this code to any other student.
 *
 *  Number of slip days used: 0 
 *
 *  Student 1: Yugam Goyal 
 *  UTEID: yg8338
 *  email address: yug.goyal46@gmail.com
 *  Grader name: David K 
 *
 */

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/*
 * This class creates the HuffManTree which is used for the HuffMan Compression Algo
 * 
 * Author: Yugam Goyal
 */
public class HuffManTree {

	private static final String NON_LEAF = "0";
	private static final String LEAF = "1";
	private static final int IS_LEAF = 1;
	private static final int IS_NOT_LEAF = 0;
	private static final int LEFT_VAL = 0;
	private TreeNode huffMan;
	private Map<Integer, String> values;
	private int numOfLeaf;
	private int numOfNode;

	/*
	 * Pre: data != null Post: Makes the HuffManTree object
	 */
	public HuffManTree(int[] data) {

		if (data == null) {
			throw new IllegalArgumentException("data is null");
		}

		// Gpes through the frequency and adds everything to the queue
		PrQ<TreeNode> dataStored = new PrQ<>();
		for (int x = 0; x < data.length; x++) {
			if (data[x] != 0) {
				dataStored.enqueue(new TreeNode(x, data[x]));
			}
		}
		
		// System.out.println("size of queue" + dataStored.size());

		// System.out.println("First" + dataStored.toString()); 
		
//		while(!dataStored.isEmpty()) {
//			System.out.println(dataStored.dequeue());
//		}
		
		numOfLeaf = dataStored.size();
		numOfNode =  dataStored.size();
		
		getTree(dataStored);
		
		huffMan = dataStored.returnData();
		
		
	// 	System.out.println("num of leaves: " + numOfLeaf + "numOfNode: " + numOfNode);
		
		// huffMan.printTree(huffMan);
		

	}

	/*
	 * Pre: None Post: Makes a huffman
	 */
	public HuffManTree(TreeNode node) {
		huffMan = node;
		numOfLeaf = getNumOfLeafs(node);
		numOfNode = getNumOfNodes(node);
		
	// 	System.out.println("num of leaves: " + numOfLeaf + "numOfNode: " + numOfNode);
	}

	/*
	 * Pre: None Post: returns the number of nodes you have
	 */
	private int getNumOfNodes(TreeNode node) {
		if (node.isLeaf()) {
			return 1;
		}
		return 1 + getNumOfLeafs(node.getRight())
				+ getNumOfLeafs(node.getLeft());
	}

	/*
	 * Pre: None Post: returns the number of leaf nodes you have
	 */
	private int getNumOfLeafs(TreeNode node) {
		
		if (node.isLeaf()) {
			return 1;
		}
		return getNumOfLeafs(node.getRight()) + getNumOfLeafs(node.getLeft());
	}

	/*
	 * Pre: input != null Post: Makes the HuffManTree object
	 */
	public HuffManTree(BitInputStream input) throws IOException {

		if (input == null) {
			throw new IOException("input == null");
		}

		// Gpes through the input and adds everything to the queue
		PrQ<TreeNode> dataStored = new PrQ<>();
		for (int x = 0; x < IHuffConstants.ALPH_SIZE; x++) {
			int value = input.readBits(IHuffConstants.BITS_PER_INT);

			if (value > 0) {
				dataStored.enqueue(new TreeNode(x, value));
			}
		}

		dataStored.enqueue(new TreeNode(IHuffConstants.PSEUDO_EOF, 1));

		getTree(dataStored);

		huffMan = dataStored.returnData();
		

		numOfLeaf = getNumOfLeafs(huffMan);
		numOfNode = getNumOfNodes(huffMan);
		
		// System.out.println("num of leaves: " + numOfLeaf + "numOfNode: " + numOfNode);
		

	}

	/*
	 * Pre: None Post: Creates the tree based on the priority queue
	 */
	private void getTree(PrQ<TreeNode> dataStored) {
		while (dataStored.isMoreThanOne()) {
			TreeNode firstD = dataStored.dequeue();
			TreeNode secondD = dataStored.dequeue();
			numOfNode += 1;
			TreeNode addVal = new TreeNode(firstD,
					firstD.getFrequency() + secondD.getFrequency(), secondD);
			dataStored.enqueue(addVal);
		}
		
		 // System.out.println("SUP" + dataStored.toString()); 
		//  dataStored.returnData().printTree(dataStored.returnData());
	}

	/*
	 * Pre: None Post: Returns a map of indexs and codes
	 */
	public Map<Integer, String> getEncodedCodes() {

		values = new TreeMap<>();
		getValues(huffMan, "", values);
		return values;
	}

	/*
	 * Pre: None Post: Generates all the codes for each leaf value and puts them
	 * a map
	 */
	private void getValues(TreeNode data, String temp,
			Map<Integer, String> values) {

		if (data != null) {

			getValues(data.getLeft(), temp + NON_LEAF, values);
			// If leaf then add to map
			if (data.getRight() == null && data.getLeft() == null) {
				values.put(data.getValue(), temp);
			}
			getValues(data.getRight(), temp + LEAF, values);
		}

	}

	/*
	 * Pre: None Post: Returns the size of the tree
	 */
	public int getTreeSize() {
		return numOfLeaf * (IHuffConstants.BITS_PER_WORD + 1) + numOfNode;
	}

	/*
	 * Pre: None Post: the number of bits written
	 */
	public int getStoreTreeData(BitOutputStream out) {
		return getStoreTreeData(huffMan, out);
	}

	/*
	 * Pre: None Post: Writes the compressed data and returns the number of bits
	 * written
	 */
	private int getStoreTreeData(TreeNode node, BitOutputStream out) {

		if (node != null) {
			int totalBBits = 1;
			if (node.isLeaf()) {
				out.writeBits(1, IS_LEAF);
				out.writeBits(IHuffConstants.BITS_PER_WORD + 1,
						node.getValue());
				totalBBits += IHuffConstants.BITS_PER_WORD + 1;
			} else {
				out.writeBits(1, IS_NOT_LEAF);
				totalBBits += getStoreTreeData(node.getLeft(), out);
				totalBBits += getStoreTreeData(node.getRight(), out);
			}
			return totalBBits;
		}

		return 0;

	}

	/*
	 * Pre: None Post: Return the number of bits read and write the compressed
	 * file and uncompressed
	 */
	public int writeData(BitInputStream input, BitOutputStream output) throws IOException {
		boolean isPENOF = false;
		TreeNode temp = huffMan;
		int totalBits = 0;
		while (!isPENOF) {
			int bit = input.readBits(1);
			if (bit == -1) {
				output.close();
				input.close();
				throw new IOException("Error reading compressed file. "
						+ "\n unexpected end of input. No PSEUDO_EOF value.");
			} else {

				// 0 indicated left and 1 indicated right
				if (bit == LEFT_VAL) {
					temp = temp.getLeft();
				} else {
					temp = temp.getRight();
				}
				
				// TODO: do I need this?
				if(temp == null) {
					throw new IOException("Error reading compressed file. "
							+ "\n unexpected end of input. No PSEUDO_EOF value.");
				}
				
				// If we are at a leaf then we need to copy the data and if its
				// PEOF then we stop
				if (temp.isLeaf()) {
					if (temp.getValue() == IHuffConstants.PSEUDO_EOF) {
						isPENOF = true;
					} else {
						// Write the bits if its not PEOF
						output.writeBits(IHuffConstants.BITS_PER_WORD, temp.getValue());
						totalBits += IHuffConstants.BITS_PER_WORD;
						temp = huffMan;
					}
				}
			}
		}

		return totalBits;
	}

}
