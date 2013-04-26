package cs182.scaffold;

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
		//scaffold.addAll(_contigs);
		PriorityQueue<Contig> revScaffold = new PriorityQueue<Contig>();
//		HashSet<MatePair> conflictPairs = new HashSet<MatePair>();
		
		
		
		// start with a single contig
		
		// need to check that curContig has any alignments; eventually switch out scaffold lists with new super-contigs
		curContig = _contigs.poll();
		LinkedList<MatePair> workingPairs = new LinkedList<MatePair>();
		for (Alignment a : curContig._alignments)
			workingPairs.add(a._pair);
		curContig._support++;
		scaffold.add(curContig);
		
		
		//sorting the contigs into scaffold and revscaffold, based on whether the mate pair says it should be flipped or not. updating relative location as we go
		while (!workingPairs.isEmpty()) {
			curPair = workingPairs.poll();
			curPair.reduceAlignments();
			if (curPair._alignments.size() == 2) { // standard size
				if (scaffold.contains(curPair._startAlign._contig)) {
					curContig = curPair._endAlign._contig;
					for (Alignment a : curContig._alignments)
						workingPairs.add(a._pair);
					_contigs.remove(curContig);
					if (curPair.isStraight()) {
						curContig._relativeLocation = curPair._startAlign._contig._relativeLocation + curPair._startAlign._startIndex + curPair._insertLength - curPair._endAlign._startIndex;
						curContig._support++;
						scaffold.add(curContig);
					} else {
						curContig._relativeLocation = curPair._startAlign._contig._relativeLocation + curPair._startAlign._startIndex + curPair._insertLength - curPair._endAlign._startIndex;
						curContig._support++;
						revScaffold.add(curContig);
					}
				} else if (scaffold.contains(curPair._endAlign._contig)) {
					curContig = curPair._startAlign._contig;
					for (Alignment a : curContig._alignments)
						workingPairs.add(a._pair);
					_contigs.remove(curContig);
					if (curPair.isStraight()) {
						curContig._relativeLocation = curPair._endAlign._contig._relativeLocation + curPair._endAlign._startIndex - curPair._insertLength + curPair._startAlign._startIndex;
						curContig._support++;
						scaffold.add(curContig);
					} else {
						curContig._relativeLocation = curPair._endAlign._contig._relativeLocation + curPair._endAlign._startIndex - curPair._insertLength + curPair._startAlign._startIndex;
						curContig._support++;
						revScaffold.add(curContig);
					}
				} else if (revScaffold.contains(curPair._startAlign._contig)) {
					curContig = curPair._endAlign._contig;
					for (Alignment a : curContig._alignments)
						workingPairs.add(a._pair);
					_contigs.remove(curContig);
					if (curPair.isStraight()) {
						curContig._relativeLocation = curPair._startAlign._contig._relativeLocation + curPair._startAlign._startIndex - curPair._insertLength + curPair._endAlign._startIndex;
						curContig._support++;
						revScaffold.add(curContig);
					} else {
						curContig._relativeLocation = curPair._startAlign._contig._relativeLocation + curPair._startAlign._startIndex - curPair._insertLength + curPair._endAlign._startIndex;
						curContig._support++;
						scaffold.add(curContig);
					}
				} else if (revScaffold.contains(curPair._endAlign._contig)) {
					curContig = curPair._startAlign._contig;
					for (Alignment a : curContig._alignments)
						workingPairs.add(a._pair);
					_contigs.remove(curContig);
					if (curPair.isStraight()) {
						curContig._relativeLocation = curPair._endAlign._contig._relativeLocation + curPair._endAlign._startIndex + curPair._insertLength - curPair._startAlign._startIndex;
						curContig._support++;
						revScaffold.add(curContig);
					} else {
						curContig._relativeLocation = curPair._endAlign._contig._relativeLocation + curPair._endAlign._startIndex + curPair._insertLength - curPair._startAlign._startIndex;
						curContig._support++;
						scaffold.add(curContig);
					}
				}
			}
			
		}
		
		// TODO : debug the contig sorting. if that works I'm some sort of weird sleepdep genius
		// TODO : error handling! point subs are fine, but never takes into account insert length errors or orientation errors.
		// I was going to keep a paired read support tally and flush out the error PRs...I totally knew what I was doing. Maybe tomorrow morning.
		// Yeah, I know my revScaffold is still in original orientation. That's to preserve the equals() and hashCode() functions till I'm all ready to build.
		
		
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
