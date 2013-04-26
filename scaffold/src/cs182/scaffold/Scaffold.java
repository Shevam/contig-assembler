package cs182.scaffold;

import java.util.LinkedList;

public class Scaffold {
	
	LinkedList<Sequence> _contigs;
	LinkedList<MatePair> _matePairs;
	
	public Scaffold() {
		_contigs = new LinkedList<Sequence>();
		_matePairs = new LinkedList<MatePair>();
	}
	
	
	
	
	
	
	
	
	
	
	public void addContig(String s) {
		Sequence seq = new Sequence(s);
		_contigs.add(seq);
		_contigs.add(seq.getReverseComplement());
	}
	
	public void addMatePair(String line) {
		String[] split = line.split("\\t");
		
		if (split.length != 3) {
			System.err.println("Invalid mate pair input. Skipping.");
			return;
		}
		
		try {
			MatePair mp = new MatePair(split[0], split[2], Integer.parseInt(split[1]));
			_matePairs.add(mp);
			_matePairs.add(mp.getReverseComplement());
		} catch (NumberFormatException e) {
			System.err.println("Invalid mate pair input. Skipping.");
			return;
		}
	}
	
}
