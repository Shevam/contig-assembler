package cs182.scaffold;

public class Contig extends Sequence implements Comparable<Contig> {
	
	int _relativeLocation = 0;
	
	public Contig(String seq) {
		super(seq);
	}
	
	public void alignMatePair(MatePair mp) {
		
	}
	
	@Override
	public int compareTo(Contig arg0) {
		return this._relativeLocation - arg0._relativeLocation;
	}

}
