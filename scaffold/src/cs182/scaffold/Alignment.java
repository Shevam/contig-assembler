package cs182.scaffold;

import java.util.LinkedList;

public class Alignment {
	
	int _score;
	int _startIndex;
	boolean _isFirstRead;
	boolean _isRevComp;
	Contig _contig;
	MatePair _pair;
	
	public Alignment(Contig contig, MatePair pair, int score, int startIndex, boolean isFirstRead, boolean isRevComp) {
		_score = score;
		_startIndex = startIndex;
		_isFirstRead = isFirstRead;
		_isRevComp = isRevComp;
		_contig = contig;
		_pair = pair;
	}
	
	
	
	
	public static void createAlignments(Contig contig, MatePair pair) {
		LinkedList<Alignment> a = new LinkedList<Alignment>();
		
		// aligning start and end of forward-oriented mate pair to contig
		LocalAligner forwardStart = new LocalAligner(contig._seq, pair._seq1._seq);
		LocalAligner forwardEnd = new LocalAligner(contig._seq, pair._seq2._seq);
		
		// aligning start and end of reverse-oriented mate pair to contig
		MatePair revPair = pair.getReverseComplement();
		LocalAligner reverseStart = new LocalAligner(contig._seq, revPair._seq1._seq);
		LocalAligner reverseEnd = new LocalAligner(contig._seq, revPair._seq2._seq);
		
		
		// only returning alignments with < 3 mismatches
		int cutoff = 32;
		
		if (forwardStart.maxScore > cutoff)
			a.add(new Alignment(contig, pair, forwardStart.maxScore, forwardStart.startIndex, true, true));
		if (reverseStart.maxScore > cutoff)
			a.add(new Alignment(contig, pair, reverseStart.maxScore, reverseStart.startIndex, true, false));
		
		
		if (forwardEnd.maxScore > cutoff)
			a.add(new Alignment(contig, pair, forwardEnd.maxScore, forwardEnd.startIndex, false, true));
		if (reverseEnd.maxScore > cutoff)
			a.add(new Alignment(contig, pair, reverseEnd.maxScore, reverseEnd.startIndex, false, false));
		
		
		contig._alignments.addAll(a);
		pair._alignments.addAll(a);
	}
	
	public static Alignment createAlignment(Contig contig, MatePair pair) {
		LinkedList<Alignment> a = new LinkedList<Alignment>();
		
		// aligning start and end of forward-oriented mate pair to contig
		LocalAligner forwardStart = new LocalAligner(contig._seq, pair._seq1._seq);
		LocalAligner forwardEnd = new LocalAligner(contig._seq, pair._seq2._seq);
		
		// aligning start and end of reverse-oriented mate pair to contig
		MatePair revPair = pair.getReverseComplement();
		LocalAligner reverseStart = new LocalAligner(contig._seq, revPair._seq1._seq);
		LocalAligner reverseEnd = new LocalAligner(contig._seq, revPair._seq2._seq);
		
		
		// only returning alignments with < 3 mismatches
		int cutoff = 32;
		
		if (forwardStart.maxScore > cutoff)
			a.add(new Alignment(contig, pair, forwardStart.maxScore, forwardStart.startIndex, true, true));
		if (reverseStart.maxScore > cutoff)
			a.add(new Alignment(contig, pair, reverseStart.maxScore, reverseStart.startIndex, true, false));
		
		
		if (forwardEnd.maxScore > cutoff)
			a.add(new Alignment(contig, pair, forwardEnd.maxScore, forwardEnd.startIndex, false, true));
		if (reverseEnd.maxScore > cutoff)
			a.add(new Alignment(contig, pair, reverseEnd.maxScore, reverseEnd.startIndex, false, false));
		
		while (a.size() > 1) {
			Alignment a1 = a.poll();
			Alignment a2 = a.poll();
			if (a1._score > a2._score) {
				a.push(a1);
			} else {
				a.push(a2);
			}
		}
		
		contig._alignments.addAll(a);
		pair._alignments.addAll(a);
		return a.poll();
	}
}
