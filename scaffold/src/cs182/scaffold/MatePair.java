package cs182.scaffold;

public class MatePair {
	
	Sequence _seq1;
	Sequence _seq2;
	int _insertLength;
	
	public MatePair(String s1, String s2, int l) {
		_seq1 = new Sequence(s1);
		_seq2 = new Sequence(s2);
		_insertLength = l;
	}
	
	public MatePair(Sequence s1, Sequence s2, int l) {
		_seq1 = s1;
		_seq2 = s2;
		_insertLength = l;
	}
	
	public MatePair getReverseComplement() {
		return new MatePair(_seq2.getReverseComplement(), _seq1.getReverseComplement(), _insertLength);
	}
}
