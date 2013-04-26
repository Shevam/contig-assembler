package cs182.scaffold;

public class Sequence {
	
	String _seq;
	
	public Sequence(String seq) {
		_seq = seq.trim().toUpperCase();
	}
	
	public Sequence getReverseComplement() {
		StringBuilder newSeq = new StringBuilder(_seq);
		newSeq.reverse();
		for (int i = 0; i < newSeq.length(); i++)
			newSeq.setCharAt(i, getComplement(newSeq.charAt(i)));
		
		return new Sequence(newSeq.toString());
	}
	
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
