package cs182.scaffold;

public class Sequence {
	
	String _seq;
	String _complement;
	
	public Sequence(String seq) {
		_seq = seq.trim().toUpperCase();
		_complement = buildComplement(_seq);
	}
	
	/**
	 * This method switches the orientation of the sequence in place to its reverse complement
	 */
	public void switchOrientation() {
		_seq = reverse(_complement);
		_complement = buildComplement(_seq);
	}
	
	/**
	 * @return A new sequence representing the reverse complement of the this sequence
	 */
	public Sequence getReverseComplement() {
		return new Sequence(reverse(_complement));
	}
	
	
	/**
	 * @param seq A string
	 * @return the input string, reversed
	 */
	private static String reverse(String seq) {
		StringBuilder sb = new StringBuilder(seq);
		return sb.reverse().toString();
	}
	
	/**
	 * @return the string representing the complementary strand of the primary sequence
	 */
	private static String buildComplement(String seq) {
		StringBuilder newSeq = new StringBuilder(seq);
		for (int i = 0; i < newSeq.length(); i++)
			newSeq.setCharAt(i, getComplement(newSeq.charAt(i)));
		
		return newSeq.toString();
	}
	
	/**
	 * @param c character representing a nucleotide
	 * @return character representing the complementary nucleotide
	 */
	private static char getComplement(char c) {
		switch (c) {
		case 'A' : return 'T';
		case 'T' : return 'A';
		case 'C' : return 'G';
		case 'G' : return 'C';
		default : return ' ';
		}
	}
}
