package cs182.scaffold;

public class LocalAligner {

	AlignLocation[][] dpArray;
	char[] v;
	char[] w;
	int match;
	int mismatch;
	int gap;
	int gapExtend;
	int n;
	int m;
	String[] alignment = new String[2];
	boolean switched = false;
	
	int maxScore = 0;
	int[] endCoords;
	int startIndex; // this assumes that seq1 is longer than seq2
	
	int matchCount = 0;
	int colCount = 0;

	public LocalAligner(String seq1, String seq2) {
		if (seq1.length() >= seq2.length()) {
			this.v = seq1.toCharArray();
			this.w = seq2.toCharArray();
		} else {
			this.v = seq2.toCharArray();
			this.w = seq1.toCharArray();
			switched = true;
		}
		this.match = 1;
		this.mismatch = 0;
		this.gap = Integer.MIN_VALUE;
		this.n = v.length;
		this.m = w.length;
		dpArray = new AlignLocation[n+1][m+1];
		fillDPArray();
		endCoords = getEndCoords();
		getTraceback();
	}

	public void fillDPArray() {
		//iterating through dpArray
		for (int j = 0; j <= m; j++) {
			for (int i = 0; i <= n; i++) {

				AlignLocation wgap = null;
				AlignLocation vgap = null;
				AlignLocation nogap = null;
				int wgapScore;                                                                      
				int vgapScore;                                                                      
				int nogapScore;                                                                     

				//determining adjacent alignment locations to initialize new AlignLocation          
				if (i > 0) {                                                                        
					wgap = dpArray[i-1][j];                                                     
					wgapScore = wgap.score + gap;                                               
					if (wgapScore < 0) {                                                        
						wgap = null;                                                        
						wgapScore = 0;                                                      
					}                                                                           
				} else {                                                                            
					wgapScore = 0;                                                              
				}                                                                                   

				if (j > 0) {                                                                        
					vgap = dpArray[i][j-1];                                                     
					vgapScore = vgap.score + gap;                                               
					if (vgapScore < 0) {                                                        
						vgap = null;                                                        
						vgapScore = 0;                                                      
					}                                                                           
				} else {
					vgapScore = 0;
				}

				if (i > 0 && j > 0) {
					nogap = dpArray[i-1][j-1];
					if (v[i-1] == w[j-1]) {
						nogapScore = nogap.score + match;
					} else {
						nogapScore = nogap.score + mismatch;
					}
					if (nogapScore < 0) {
						nogap = null;
						nogapScore = 0;
					}
				} else {
					nogapScore = 0;
				}

				int max1 = Math.max(wgapScore,vgapScore);
				int max2 = Math.max(max1,nogapScore);


				if (max2 == nogapScore) {
					dpArray[i][j] = new AlignLocation(nogapScore,nogap,i,j);
				} else if (max2 == wgapScore) {
					dpArray[i][j] = new AlignLocation(wgapScore,wgap,i,j);
				} else if (max2 == vgapScore) {
					dpArray[i][j] = new AlignLocation(vgapScore,vgap,i,j);
				}

			}
		}

	}

	public int[] getEndCoords() {

		int maxI = 0;
		int maxJ = 0;
		for (int i = 0; i <= n; i++) {
			for (int j = 0; j <= m; j++) {
				if (dpArray[i][j].score > maxScore) {
					maxScore = dpArray[i][j].score;
					maxI = i;
					maxJ = j;
				}
			}
		}
		int[] coords = {maxI,maxJ};
		startIndex = maxI - maxJ;
		return coords;
	}

	public void getTraceback() {

		AlignLocation a = dpArray[endCoords[0]][endCoords[1]];
		AlignLocation b = a.traceback;
		StringBuffer vBuf = new StringBuffer();
		StringBuffer wBuf = new StringBuffer();

		while (b != null) {
			if (a.i != b.i) {
				vBuf.append(v[b.i]);
			} else {
				vBuf.append("-");
			}
			if (a.j != b.j) {
				wBuf.append(w[b.j]);
			} else {
				wBuf.append("-");
			}
			if (a.i != b.i && a.j != b.j && v[b.i] == w[b.j]) {
				matchCount++;
			}
			colCount++;
			a = b;
			b = a.traceback;
		}

		if (switched) {
			alignment[0] = wBuf.reverse().toString();
			alignment[1] = vBuf.reverse().toString();
		} else {
			alignment[0] = vBuf.reverse().toString();
			alignment[1] = wBuf.reverse().toString();
		}
	}


	public class AlignLocation {

		int score;
		AlignLocation traceback;
		int i;
		int j;

		public AlignLocation(int score, AlignLocation traceback, int i, int j) {
			this.score = score;
			this.traceback = traceback;
			this.i = i;
			this.j = j;
		}

	}
}
