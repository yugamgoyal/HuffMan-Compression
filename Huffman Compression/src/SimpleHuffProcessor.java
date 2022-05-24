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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/*
 * The SimpleHuffProcessor class allows for the client to send a file to be compressed and unless 
 * forced the file in only compressed if the HuffMan algorithm saves bits. The class can both 
 * compress and uncompress a file given the correct format of the header
 * 
 * Author: Yugam Goyal 
 * 
 */

public class SimpleHuffProcessor implements IHuffProcessor {

	private static final boolean PRESENT = false;
	private static final int RANDOM_STORED_NODE_VALUE = -1;
	private static final int IS_LEAF = 1;
	
	private IHuffViewer myViewer;
	private HuffManTree huffManTree;
	private int totalBitsSaved;
	private int[] freqData;
	private int headerInfo;
	private Map<Integer, String> values;
	private boolean beenThere;

	/**
	 * Preprocess data so that compression is possible --- count
	 * characters/create tree/store state so that a subsequent call to compress
	 * will work. The InputStream is <em>not</em> a BitInputStream, so wrap it
	 * int one as needed.
	 * 
	 * @param in
	 *            is the stream which could be subsequently compressed
	 * @param headerFormat
	 *            a constant from IHuffProcessor that determines what kind of
	 *            header to use, standard count format, standard tree format, or
	 *            possibly some format added in the future.
	 * @return number of bits saved by compression or some other measure Note,
	 *         to determine the number of bits saved, the number of bits written
	 *         includes ALL bits that will be written including the magic
	 *         number, the header format number, the header to reproduce the
	 *         tree, AND the actual data.
	 * @throws IOException
	 *             if an error occurs while reading from the input file.
	 */
	public int preprocessCompress(InputStream in, int headerFormat)
			throws IOException {

		if (in == null) {
			myViewer.showError("in is null");
			return -1;
		}

		if (PRESENT) {
			myViewer.update("currently in preprocessCompress");
		}

		// makes frequency array
		freqData = extractData(in);

		if (PRESENT) {
			myViewer.update("calculated freq");
		}

		// new tree created
		huffManTree = new HuffManTree(freqData);
		
		System.out.println(huffManTree.getTreeSize());

		if (PRESENT) {
			myViewer.update("made tree");
		}

		headerInfo = headerFormat;

		// map of codes created
		values = huffManTree.getEncodedCodes();

		if (PRESENT) {
			myViewer.update("codes created");
		}

		totalBitsSaved = getTotalSavedBits();

		if (PRESENT) {
			myViewer.update("pre-comp done");
		}

		beenThere = true;
		
		System.out.println("BITS SAVED" + totalBitsSaved);
	

		return totalBitsSaved;
	}

	/*
	 * Pre: None Post: Return the number of bits saved if compression was done
	 */
	private int getTotalSavedBits() {

		int noCompressionBits = 0;
		// Counts for the magic number and header type
		int compressionBits = BITS_PER_INT * 2;

		if (headerInfo == STORE_TREE) {

			// Adds for size of free
			compressionBits += BITS_PER_INT;
			compressionBits += huffManTree.getTreeSize();

		} else if (headerInfo == STORE_COUNTS) {
			compressionBits += BITS_PER_INT * ALPH_SIZE;
		}

		// Adds the frequency times the bits it takes to store the item
		for (int x = 0; x < freqData.length; x++) {
			noCompressionBits += freqData[x] * BITS_PER_WORD;

			if (values.containsKey(x)) {
				compressionBits += freqData[x] * values.get(x).length();
			}
		}

		// the noCompressionBits does not have PEOF value
//		
		System.out.println("BITS UNCOMPRESSED: " + (noCompressionBits - BITS_PER_WORD));
		System.out.println("BITS COMPRESSED: " + (compressionBits));
		return (noCompressionBits - BITS_PER_WORD) - compressionBits;
	}

	/*
	 * Pre: None Post: return an array of frequency for each value in the file
	 */
	private int[] extractData(InputStream in) throws IOException {

		// includes index for PEOF
		int[] vals = new int[ALPH_SIZE + 1];
		BitInputStream bits = new BitInputStream(in);
		int inbits = bits.readBits(BITS_PER_WORD);
		// reads until no value is present
		while (inbits != -1) {
			vals[inbits]++;
			inbits = bits.readBits(BITS_PER_WORD);
		}
		vals[PSEUDO_EOF]++;
		bits.close();
		return vals;
	}

	/**
	 * Compresses input to output, where the same InputStream has previously
	 * been pre-processed via <code>preprocessCompress</code> storing state used
	 * by this call. <br>
	 * pre: <code>preprocessCompress</code> must be called before this method
	 * 
	 * @param in
	 *            is the stream being compressed (NOT a BitInputStream)
	 * @param out
	 *            is bound to a file/stream to which bits are written for the
	 *            compressed file (not a BitOutputStream)
	 * @param force
	 *            if this is true create the output file even if it is larger
	 *            than the input file. If this is false do not create the output
	 *            file if it is larger than the input file.
	 * @return the number of bits written.
	 * @throws IOException
	 *             if an error occurs while reading from the input file or
	 *             writing to the output file.
	 */
	public int compress(InputStream in, OutputStream out, boolean force)
			throws IOException {

		if (in == null && !beenThere) {
			myViewer.showError(
					"in is null or out is null or has not been to precompress");
			return -1;
		}
		
		beenThere = false; 

		if (!force && totalBitsSaved <= 0) {
			myViewer.update("no need to compress");
			return 0;
		}

		BitInputStream input = new BitInputStream(in);
		BitOutputStream output = new BitOutputStream(out);

		// Write the magic number and header type
		output.writeBits(BITS_PER_INT, MAGIC_NUMBER);
		output.writeBits(BITS_PER_INT, headerInfo);
		int totalBits = BITS_PER_INT * 2;

		// Writs the header and data
		totalBits += writeHeader(output);
		totalBits += writeCompressedData(input, output);

		if (PRESENT) {
			myViewer.update("writing the compressed file done");
		}

		input.close();
		output.close();

		return totalBits;
	}

	/*
	 * Pre: None Post: returns the total number of bits it takes to write the
	 * data and data the header in output
	 */
	private int writeCompressedData(BitInputStream in, BitOutputStream output)
			throws IOException {

		int totalBits = 0;

		int inbits = in.readBits(BITS_PER_WORD);

		// reads the original file until it has nothing else
		while (inbits != -1) {
			// gets the code associated with it
			String temp = values.get(inbits);
			// it takes the length of the code bits to write the code
			int size = values.get(inbits).length();
			totalBits += size;
			// writes the code one by one
			for (int x = 0; x < size; x++) {
				output.writeBits(1, Integer.parseInt("" + temp.charAt(x)));
			}

			inbits = in.readBits(BITS_PER_WORD);
		}

		// writes the info for PEOF
		for (int x = 0; x < values.get(PSEUDO_EOF).length(); x++) {

			output.writeBits(1,
					Integer.parseInt("" + values.get(PSEUDO_EOF).charAt(x)));
		}

		totalBits += values.get(PSEUDO_EOF).length();

		return totalBits;
	}

	/*
	 * Pre: None Post: returns the total number of bits it takes to write the
	 * header and writes the header in output
	 */
	private int writeHeader(BitOutputStream out) {

		int totalBits = 0;

		if (headerInfo == STORE_TREE) {

			// Writes the size
			out.writeBits(BITS_PER_INT, huffManTree.getTreeSize());
			totalBits += BITS_PER_INT;
			totalBits += huffManTree.getStoreTreeData(out);

		} else if (headerInfo == STORE_COUNTS) {

			for (int x = 0; x < ALPH_SIZE; x++) {
				out.writeBits(BITS_PER_INT, freqData[x]);
			}

			totalBits += BITS_PER_INT * ALPH_SIZE;
		}

		return totalBits;
	}

	/**
	 * Uncompress a previously compressed stream in, writing the uncompressed
	 * bits/data to out.
	 * 
	 * @param in
	 *            is the previously compressed data (not a BitInputStream)
	 * @param out
	 *            is the uncompressed file/stream
	 * @return the number of bits written to the uncompressed file/stream
	 * @throws IOException
	 *             if an error occurs while reading from the input file or
	 *             writing to the output file.
	 */
	public int uncompress(InputStream in, OutputStream out) throws IOException {

		if (in == null || out == null) {
			throw new IOException("is null");
		}

		BitInputStream input = new BitInputStream(in);
		BitOutputStream output = new BitOutputStream(out);

		int magic = input.readBits(BITS_PER_INT);
		if (magic != MAGIC_NUMBER) {
			myViewer.showError("Error reading compressed file. \n"
					+ "File did not start with the huff magic number.");
			input.close();
			output.close();
			return -1;
		}

		int totalBits = 0;

		// recreates the tree
		HuffManTree huffman = getTreeFromCompression(input);

		if (PRESENT) {
			showString("tree made");
		}

		// write compressed data into uncompressed
		totalBits += huffman.writeData(input, output);

		if (PRESENT) {
			showString("finished writing file, you are good to go");
		}

		input.close();
		output.close();

		return totalBits;
	}

	/*
	 * Pre: None Post: Based on the header it recreates the tree and return the
	 * root TreeNode
	 */
	private HuffManTree getTreeFromCompression(BitInputStream input)
			throws IOException {

		int headerType = input.readBits(BITS_PER_INT);
		HuffManTree data = null;

		if (headerType == STORE_TREE) {
			// gets rid of size
			input.readBits(BITS_PER_INT);
			data = new HuffManTree(decode(input));

		} else if (headerType == STORE_COUNTS) {
			data = new HuffManTree(input);
		}

		return data;
	}

	/*
	 * Pre: None Post: If the header is STORE_TREE it recreates the tree and
	 * return the root TreeNode
	 */
	private TreeNode decode(BitInputStream input) throws IOException {

		int value = input.readBits(1);
		if (value == 0) {
			TreeNode tempNode = new TreeNode(RANDOM_STORED_NODE_VALUE, RANDOM_STORED_NODE_VALUE);
			tempNode.setLeft(decode(input));
			tempNode.setRight(decode(input));
			return tempNode;
		} else if (value == IS_LEAF) {
			// if its not a 0 then it reads 9 bits to see the value
			int tempData = input.readBits(BITS_PER_WORD + 1);
			return new TreeNode(tempData, RANDOM_STORED_NODE_VALUE);
		}

		return null;
	}

	/*
	 * Given Method Pre: None Post: Sets the Viewer
	 */
	public void setViewer(IHuffViewer viewer) {
		myViewer = viewer;
	}

	/*
	 * Given Method Pre: None Post: Sets the Viewer with string
	 */
	private void showString(String s) {
		if (myViewer != null)
			myViewer.update(s);
	}
}
