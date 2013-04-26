package cs182.scaffold;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class Scaffold {
	
	LinkedList<Contig> _contigs;
	LinkedList<MatePair> _matePairs;
	
	
	public Scaffold() {
		_contigs = new LinkedList<Contig>();
		_matePairs = new LinkedList<MatePair>();
	}
	
	
	public void constructScaffold() {
		
		int numContigs = _contigs.size();
		int numPairs = _matePairs.size();
		
		Contig curContig;
		MatePair curPair;
		
		// aligning mate pairs with contigs
		for (int i = 0; i < numContigs; i++) {
			curContig = _contigs.poll();
			for (int j = 0; j < numPairs; j++) {
				curPair = _matePairs.poll();
				Alignment.createAlignments(curContig, curPair);
				_matePairs.add(curPair);
			}
			_contigs.add(curContig);
		}
		
		PriorityQueue<Contig> scaffold = new PriorityQueue<Contig>();
		PriorityQueue<Contig> revScaffold = new PriorityQueue<Contig>();
		HashSet<MatePair> usedPairs = new HashSet<MatePair>();
		
		
		
		// start with a single contig
		
		// need to check that curContig has any alignments; eventually switch out scaffold lists with new super-contigs
		curContig = _contigs.poll();
		LinkedList<MatePair> workingPairs = new LinkedList<MatePair>();
		for (Alignment a : curContig._alignments)
			workingPairs.add(a._pair);
		scaffold.add(curContig);
		
		while (!workingPairs.isEmpty()) {
			curPair = workingPairs.poll();
			for (Alignment a : curPair._alignments) { // look at each alignment to this contig
				if ()
				
			}
		}
	}
	
	
	public void addContig(String s) {
		Contig seq = new Contig(s);
		_contigs.add(seq);
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
		} catch (NumberFormatException e) {
			System.err.println("Invalid mate pair input. Skipping.");
			return;
		}
	}
	
}
