package cs182.scaffold;

import java.util.LinkedList;

public class MatePair {
	
	Sequence _seq1;
	Sequence _seq2;
	int _insertLength;
	LinkedList<Alignment> _alignments;
	
	public MatePair(String s1, String s2, int l) {
		_seq1 = new Sequence(s1);
		_seq2 = new Sequence(s2);
		_insertLength = l;
		_alignments = new LinkedList<Alignment>();
	}
	
	public MatePair(Sequence s1, Sequence s2, int l) {
		_seq1 = s1;
		_seq2 = s2;
		_insertLength = l;
	}
	
	public MatePair getReverseComplement() {
		return new MatePair(_seq2.getReverseComplement(), _seq1.getReverseComplement(), _insertLength);
	}
	
	@Override
	public boolean equals(Object o) {
		boolean retval = false;
		if (o instanceof MatePair) {
			MatePair otherPair = (MatePair) o;
			retval = _seq1.equals(otherPair._seq1) && _seq2.equals(otherPair._seq2);
		}
		return retval;
	}
	
	@Override
	public int hashCode() {
		return _seq1.hashCode() ^ _seq2.hashCode();
	}
}
