package cs182.scaffold;

import java.util.LinkedList;

public class Alignment {
	
	int _score;
	int _startIndex;
	boolean _isFirstRead;
	boolean _isRevComp;
	
	public Alignment(int score, int startIndex, boolean isFirstRead, boolean isRevComp) {
		_score = score;
		_startIndex = startIndex;
		_isFirstRead = isFirstRead;
		_isRevComp = isRevComp;
	}
	
	
	
	
	public static LinkedList<Alignment> createAlignment(Sequence contig, MatePair pair) {
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
			a.add(new Alignment(forwardStart.maxScore, forwardStart.startIndex, true, true));
		
		if (forwardEnd.maxScore > cutoff)
			a.add(new Alignment(forwardEnd.maxScore, forwardEnd.startIndex, false, true));
		
		if (reverseStart.maxScore > cutoff)
			a.add(new Alignment(reverseStart.maxScore, reverseStart.startIndex, true, false));
		
		if (reverseEnd.maxScore > cutoff)
			a.add(new Alignment(reverseEnd.maxScore, reverseEnd.startIndex, false, false));
		
		
		
		return a;
	}
}
