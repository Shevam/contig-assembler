package cs182.scaffold;

import java.util.LinkedList;

public class Contig extends Sequence implements Comparable<Contig> {
	
	int _relativeLocation = 0;
	int _support = 0;
	LinkedList<Alignment> _alignments;
	
	public Contig(String seq) {
		super(seq);
		_alignments = new LinkedList<Alignment>();
	}
	
	@Override
	public int compareTo(Contig arg0) {
		return this._relativeLocation - arg0._relativeLocation;
	}

}
