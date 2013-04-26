package cs182.assembler;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.PriorityQueue;

import javax.imageio.ImageIO;


public class DeBruijnGraph {
	
	private final static boolean DEBUG = false;
	
	private int _k;
	private int _coverage;
	private HashMap<String,Kmer> _kmerMap;
	private HashMap<Integer,Read> _readMap;
	private HashMap<Kmer,Block> _blockMap;
	private double _estGenomeLength;
	private ArrayList<Path> _contigs;
	private ArrayList<String> _contigStrings;
	private HashSet<Kmer> _recyclingBin;
	
	
	public DeBruijnGraph(LinkedList<String> reads, int coverage, int k, boolean revcomp) {
		_k = k;
		_coverage = coverage;
		_kmerMap = new HashMap<String,Kmer>();
		_readMap = new HashMap<Integer,Read>();
		_blockMap = new HashMap<Kmer,Block>();
		_contigs = new ArrayList<Path>();
		_recyclingBin = new HashSet<Kmer>();
		_contigStrings = new ArrayList<String>();
		
		double totalLength = 0;
		for (int i = 0; i < reads.size(); i++) {
			_readMap.put(i,new Read(i,reads.get(i).toUpperCase()));
			totalLength += reads.get(i).length();
		}
		
		_estGenomeLength = totalLength/coverage;
		if (revcomp)
			_estGenomeLength = _estGenomeLength/2;
		
		
		for (Read r : _readMap.values())
			r.makeBlocks();
		
		System.out.println("condenseGraph 1");
		condenseGraph();
		
		System.out.println("removeTips 1");
		removeTips();
		
		System.out.println("makeContigs 1");
		makeContigs();
		
//		System.out.println("removeTips 2");
//		removeTips();
//		
//		System.out.println("condenseGraph 2");
//		condenseGraph();
//		
//		System.out.println("makeContigs 2");
//		makeContigs();
		
		ArrayList<String> revContigs = new ArrayList<String>();
		for (int i = 0; i < _contigs.size(); i++) {
			String contigSeq = _contigs.get(i).toString();
			if (revcomp) {
				if (revContigs.contains(contigSeq)) {
					_contigs.remove(i);
					i--;
					continue;
				} else {
					revContigs.add(getReverseComplement(contigSeq));
				}
			}
			_contigStrings.add(contigSeq);
		}
		
		
	}
	
	private void condenseGraph() {
		for (Kmer kmer : _kmerMap.values()) {
			if (_blockMap.containsKey(kmer)) {
				Block nextBlock = _blockMap.get(kmer).canCondense();
				while (nextBlock != null) {
					nextBlock.condenseBlocks(_blockMap.get(kmer), nextBlock);
					nextBlock = _blockMap.get(kmer).canCondense();
				}
			}
		}
	}
	
	private void removeTips() {
		
		for (Block b : getStartBlocks()) {
			LinkedList<Block> chain = new LinkedList<Block>();
			Block curBlock = b;
			HashMap<Kmer,Integer> arcs = curBlock.getForwardConnections();
			HashMap<Kmer,Integer> backArcs = curBlock.getReverseConnections();
			while (arcs.size() == 1 && backArcs.size() <= 1) {
				chain.add(curBlock);
				curBlock = _blockMap.get(arcs.keySet().iterator().next());
				arcs = curBlock.getForwardConnections();
				backArcs = curBlock.getReverseConnections();
			}
			
			if (chain.isEmpty()) {
				continue;
			}
			
			int length = 0;
			for (Block a : chain)
				length += a.size();
			
			if (length < 4*_k && arcs.size() == 0 && backArcs.size() == 1) {
				removeBlocks(chain);
				continue;
			}
			if (length < 2*_k) {
				int countIn = chain.getLast().getForwardConnections().values().iterator().next();
				if (backArcs.size() > 1) {
					for (int countOut : backArcs.values()) {
						if (countIn < countOut) {
							removeBlocks(chain);
							chain = null;
							break;
						}
					}
				}
				if (chain != null && arcs.size() > 1) {
					for (int countOut : arcs.values()) {
						if (countIn < countOut) {
							removeBlocks(chain);
							chain = null;
							break;
						}
					}
				}
			}
		}
		
		HashMap<Kmer,Block> revmap = getReverseBlockMap();
		for (Block b : getEndBlocks()) {
			LinkedList<Block> chain = new LinkedList<Block>();
			Block curBlock = b;
			HashMap<Kmer,Integer> arcs = curBlock.getReverseConnections();
			HashMap<Kmer,Integer> backArcs = curBlock.getForwardConnections();
			while (arcs.size() == 1 && backArcs.size() <= 1) {
				chain.add(curBlock);
				curBlock = revmap.get(arcs.keySet().iterator().next());
				arcs = curBlock.getReverseConnections();
				backArcs = curBlock.getForwardConnections();
			}
			
			if (chain.isEmpty()) {
				continue;
			}
			
			int length = 0;
			for (Block a : chain)
				length += a.size();
			
			if (length < 4*_k && arcs.size() == 0 && backArcs.size() == 1) {
				removeBlocks(chain);
				continue;
			}
			
			if (length < 2*_k) {
				
				int countIn = chain.getLast().getReverseConnections().values().iterator().next();
				if (backArcs.size() > 1) {
					for (int countOut : backArcs.values()) {
						if (countIn < countOut) {
							removeBlocks(chain);
							chain = null;
							break;
						}
					}
				}
				if (chain != null && arcs.size() > 1) {
					for (int countOut : arcs.values()) {
						if (countIn < countOut) {
							removeBlocks(chain);
							chain = null;
							break;
						}
					}
				}
			}
			
		}
		
	}
	
	private void removeBlocks(List<Block> blocks) {
//		System.out.println("removing " + blocks.size() + " blocks");
		for (Block a : blocks) {
//			Kmer forwardKmer = a.getForwardKmer();
//			Kmer reverseKmer = a.getReverseKmer();
//			for (Kmer k : _blockMap.keySet()) {
//				Block b = _blockMap.get(k);
//				b.removeForwardConnection(forwardKmer);
//				b.removeReverseConnection(reverseKmer);
//				_blockMap.put(k, b);
//			}
			if (!a.isInContig())
				_blockMap.remove(a.getForwardKmer());
		}
	}
	
	private HashMap<Kmer,Block> getReverseBlockMap() {
		HashMap<Kmer,Block> r = new HashMap<Kmer,Block>();
		for (Block b : _blockMap.values())
			r.put(b.getReverseKmer(),b);
		return r;
	}
	
	private LinkedList<Block> getStartBlocks() {
		LinkedList<Block> blocks = new LinkedList<Block>();
		for (Block b : _blockMap.values())
			if (b.isStart())
				blocks.add(b);
		
		System.out.println(blocks.size() + " start blocks retrieved");
		return blocks;
	}
	
	private LinkedList<Block> getEndBlocks() {
		LinkedList<Block> blocks = new LinkedList<Block>();
		for (Block b : _blockMap.values())
			if (b.isEnd())
				blocks.add(b);
		return blocks;
	}
	
	public void outputContigs(String filename) throws IOException {
		PrintWriter contigWriter = new PrintWriter(new BufferedWriter(new FileWriter(filename)), true);
		for (String contig : _contigStrings)
			contigWriter.println(contig);
		contigWriter.close();
	}
	
	public void debugBlocks() throws IOException {
		String indexName = "/home/sdemane/course/cs182/assembler/full_genome_noerror/block_index.txt";
		String connectionsName = "/home/sdemane/course/cs182/assembler/full_genome_noerror/block_connections.txt";
		
		PrintWriter index = new PrintWriter(new BufferedWriter(new FileWriter(indexName)), true);
		PrintWriter connections = new PrintWriter(new BufferedWriter(new FileWriter(connectionsName)), true);
		
		ArrayList<Kmer> blockKeys = new ArrayList<Kmer>();
		blockKeys.addAll(_blockMap.keySet());
		
		HashMap<Kmer,Integer> reverseIndex = new HashMap<Kmer,Integer>();
		for (int i = 0; i < blockKeys.size(); i++) {
			index.println(i + "\t" + _blockMap.get(blockKeys.get(i)).toString());
			reverseIndex.put(blockKeys.get(i), i);
		}
		index.close();
		
		HashMap<Kmer,Integer> arcs;
		for (int i = 0; i < blockKeys.size(); i++) {
			connections.println(i + "\t->");
			arcs = _blockMap.get(blockKeys.get(i)).getForwardConnections();
			for (Kmer k : arcs.keySet()) {
				connections.println("\t" + reverseIndex.get(k) + "\t(" + arcs.get(k) + ")");
			}
			connections.println();
		}
		connections.close();
	}
	
	public void visualizeGraph(String filename) throws IOException {
		PrintWriter graph = new PrintWriter(new BufferedWriter(new FileWriter(filename)), true);
		graph.println("digraph genome {");
		graph.println("node [shape=box]");
		graph.println("edge [fontcolor=red, headport=w, tailport=e]");
		graph.println("splines=true");
		graph.println("rankdir=LR");
		graph.println();
		
		LinkedList<Block> blocks = new LinkedList<Block>();
		
		for (Block b : getStartBlocks()) {
			graph.println(b.getForwardKmer().getSeq() + " [rank=source,label=\"(" +b.getForwardKmer().getHead()+")"+b.getForwardSequence()+"\"]");
			graph.println();
			blocks.add(b);
		}
		
		HashMap<Kmer,Integer> arcs;
		for (int i = 0; i < blocks.size(); i++) {
			Block curBlock = blocks.get(i);
			arcs = curBlock.getForwardConnections();
			
			graph.print("{rank=same;");
			for (Kmer kmer : arcs.keySet()) {
				if (!blocks.contains(_blockMap.get(kmer)))
					graph.print(" " + kmer.getSeq());
			}
			
			graph.println("}");
			String color = "black";
			
			for (Kmer kmer : arcs.keySet()) {
				Block b = _blockMap.get(kmer);
				
				graph.println(kmer.getSeq() + " [label=\"(" +b.getForwardKmer().getHead()+")"+b.getForwardSequence()+"\"]");
				
				
				for (Path p : _contigs) {
					if (p.containsConnection(curBlock.getForwardKmer(), kmer)) {
						color = "red";
						break;
					} else {
						color = "black";
					}
				}
				
				graph.println(curBlock.getForwardKmer().getSeq() + "->" + kmer.getSeq() + " [color=" + color + ",label = " + arcs.get(kmer) + "]");
				if (!blocks.contains(b))
					blocks.add(b);
			}
			if (curBlock.countSelfLoop() > 0)
				graph.println(curBlock.getForwardSequence() + "->" + curBlock.getForwardSequence() + " [label=" + curBlock.countSelfLoop() + "]");
		}
		graph.println("}");
		graph.close();
	}
	
	public void visualizeReads(String filename, String args) throws IOException {
		// the args string is pulled straight from args[] in Main, for labeling
		// and analysis purposes
		
		int i = 0;
		int numRows = _coverage*2;
		
		
		ArrayList<LinkedList<Double>> readPositions = new ArrayList<LinkedList<Double>>();
		HashSet<Read> usedReads = new HashSet<Read>();
		double startX = _k*3;
		for (Path p : _contigs) {
			String sequence = p.toString();
			int seqPosition = 0;
			for (Kmer blockStart : p.getBlockPath()) {
				Block b = _blockMap.get(blockStart);
				for (String s : b._forwardChain) {
					Kmer k = _kmerMap.get(s);
					for (int rID : k.getReadSet(true)) {
						Read r = _readMap.get(rID);
						if (!usedReads.contains(r)) //comment out this line if allowing reads used multiple times
							if (seqPosition+r.length() <= sequence.length() && editDistance(r.toString(),sequence.substring(seqPosition, seqPosition+r.length())) < 5) {
								//if e=.01 and l=50, the probability of more than 4 errors occurring is ~.001, and the probability of a false positive is miniscule
								double readStart = startX+seqPosition;
								double readEnd = startX+seqPosition+r.length();
								for (i = 0; i < numRows; i++) {
									LinkedList<Double> row;
									if (i >= readPositions.size()) {
										row = new LinkedList<Double>();
										row.add(readStart);
										row.add(readEnd);
										readPositions.add(row);
										break;
									} else {
										row = readPositions.get(i);
										if (row.getLast() < readStart - 5) {
											row.add(readStart);
											row.add(readEnd);
											break;
										}
									}
								}
								//may or may not check for reads used more than once.
								usedReads.add(r);
							}
					}
				}
				seqPosition++;
			}
			startX += (sequence.length() + _k*3);
		}
		
		int width = (int)startX;
		int height = _coverage * 12;
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D drawing = bi.createGraphics();
		drawing.setStroke(new BasicStroke(3));
		
		
		
		drawing.drawString("assembler "+args+"\t estimated genome length = "+_estGenomeLength+"; approximate assembled genome length = "+startX, 5, height-5);
		int fontHeight = drawing.getFontMetrics().getHeight();
		
		
		int baseline = height-(2*fontHeight)-5;
		drawing.draw(new Line2D.Double(0, baseline, width, baseline));
		
		double[] yValues = new double[numRows];
		i = 0;
		for (int y = baseline-5; y > 0 ; y-=5) {
			yValues[i] = y;
			i++;
		}
		
		drawing.setPaint(Color.BLUE);
		
		for (i = 0; i < readPositions.size(); i++) {
			LinkedList<Double> row = readPositions.get(i);
			while (!row.isEmpty()) {
				Point2D.Double p1 = new Point2D.Double(row.poll(), yValues[i]);
				Point2D.Double p2 = new Point2D.Double(row.poll(), yValues[i]);
				drawing.draw(new Line2D.Double(p1,p2));
			}
		}
		
		ImageIO.write(bi, "png", new File(filename));
		
	}
	
	private int editDistance(String s1, String s2) {
		if (s1.length() != s2.length()) {
			return -1;
		}
		int count = 0;
		for (int i = 0; i < s1.length(); i++) 
			if (s1.charAt(i) != s2.charAt(i)) 
				count++;
			
		return count;
	}
	
	private int connectionCoverage(Kmer k0, Kmer k1) {
		Block b0 = _blockMap.get(k0);
		if (b0 == null) {
			return 0;
		} else {
			Integer c = b0.getForwardConnections().get(k1);
			if (c == null) {
				return 0;
			} else {
				return c;
			}
		}
	}
	
	private Path combinePaths(Path p1, Path p2) {
//		System.out.println("appending path "+p2.toString()+" with coverage "+p2.getInitialCoverage());
		p1.appendPath(p2);
		return p1;
	}
	
	private Path mergePaths(Path p1, Path p2) {
		
		int split = p1.splitLocation(p2);
		if (split == p1.blockCount() || split == p2.blockCount())
			if (split == p1.blockCount() && split == p2.blockCount()) {
				return p1;
			} else {
//				System.out.println("block count error. p1 = "+p1.blockCount()+"; p2 = "+p2.blockCount()+"; split = "+split);
				return null;
			}
		
		Path subpath1 = p1.subPath(split);
		Path subpath2 = p2.subPath(split);
		if (subpath1.alignPaths(subpath2)) {
			if (subpath1.getAvgCoverage() >= subpath2.getAvgCoverage()) {
				subpath2.trashPath();
				return p1;
			} else if (subpath1.getAvgCoverage() < subpath2.getAvgCoverage()) {
				subpath1.trashPath();
				return p2;
			} else {
//				System.out.println("winner choice error");
				return null;
			}
		}
//		System.out.println("alignPaths error");
		return null;
	}
	
	

	private void makeContigs() {
		_contigs.clear();
		System.gc();
		LinkedList<Path> finalPaths = new LinkedList<Path>();
		for (Block a : getStartBlocks()) {
			LinkedList<Path> completePaths = new LinkedList<Path>();
			PriorityQueue<Path> parallelPaths = new PriorityQueue<Path>(11,new pathSizeComparator());
			parallelPaths.add(new Path(a));
			while (!parallelPaths.isEmpty()) {
				
				//debugging output
				if (DEBUG) {
					System.out.println("parallel paths:");
					for (Path p : parallelPaths)
						System.out.println(p.toString());
					System.out.println();
					
					System.out.println("complete paths:");
					for (Path p : completePaths)
						System.out.println(p.toString());
					System.out.println();
					System.out.println("~~~");
					System.out.println();
					
					System.out.println("parallelPaths size = "+parallelPaths.size());
					System.out.println();
				}
				
				Path curPath = parallelPaths.poll();
				LinkedList<Path> alsoCurPaths = new LinkedList<Path>();
				while (!parallelPaths.isEmpty() && parallelPaths.peek().size() - curPath.size() == 0) {
					alsoCurPaths.add(parallelPaths.poll());
				}
				while (!alsoCurPaths.isEmpty()) {
					Path nextPathOver = alsoCurPaths.poll();
					Path winner = mergePaths(curPath,nextPathOver);
					
					//debugging output
					if (DEBUG) {
						System.out.println();
						System.out.println("merging paths");
						System.out.println(curPath.toString());
						System.out.println(nextPathOver.toString());
						if (winner == null) {
							System.out.println("no winner");
						} else {
							System.out.println("winner:");
							System.out.println(winner.toString());
						}
						System.out.println();
					}
					
					if (winner == null) {
						parallelPaths.add(nextPathOver);
					} else {
						System.out.println("got a winner");
						curPath = winner;
					}
				}
				
				LinkedList<Path> forks = curPath.forkPath();
				if (forks.isEmpty()) {
					completePaths.add(curPath);
				} else {
					Path p;
					while (forks.size() > 1) {
						p = forks.poll();
						Path newPath = combinePaths(new Path(curPath),p);
//						System.out.println("adding path "+newPath.toString());
						parallelPaths.add(newPath);
					}
					curPath.appendPath(forks.poll());
					parallelPaths.add(curPath);
				}
			}
			
			finalPaths.addAll(completePaths);
//			System.gc();
		}
		
		// weed out any redundant contigs
		Collections.sort(finalPaths, new pathSizeComparator());
		
		_contigs.add(finalPaths.poll());
		
		Collections.sort(finalPaths, new pathRedundancyComparator());
		while (!finalPaths.isEmpty() && finalPaths.peek().notRedundantSequence() > 2*_k) {
			_contigs.add(finalPaths.poll());
			Collections.sort(finalPaths, new pathRedundancyComparator());
		}
		
		// destroying all the blocks in _recyclingBin which aren't also contained in a contig
		LinkedList<Block> trash = new LinkedList<Block>();
		for (Kmer k : _recyclingBin) {
			trash.add(_blockMap.get(k));
		}
		_recyclingBin = new HashSet<Kmer>();
		removeBlocks(trash);
	}
	
	public class Path {
		
//		private LinkedList<Kmer> _path;
		private LinkedList<Kmer> _blockPath;
		
		private Path() {
			_blockPath = new LinkedList<Kmer>();
//			_path = new LinkedList<Kmer>();
		}
		
		public Path(Block b) {
			_blockPath = new LinkedList<Kmer>();
//			_path = new LinkedList<Kmer>();
			
			if (b != null) {
				_blockPath.add(b.getForwardKmer());
//				LinkedList<String> chain = b.getForwardChain();
//				for (String s : chain)
//					_path.add(_kmerMap.get(s));
			}
		}
		
		public Path(Path p) {
			_blockPath = new LinkedList<Kmer>();
//			_path = new LinkedList<Kmer>();
			
			_blockPath.addAll(p._blockPath);
//			_path.addAll(p._path);
		}
		
		
		private int size() {
			int s = 0;
			for (Kmer k : _blockPath) {
				s += _blockMap.get(k).size();
			}
			return s;
		}
		
		private int blockCount() {
			return _blockPath.size();
		}
		
		private boolean contains(Kmer k) {
			return _blockPath.contains(k);
		}
		
//		private boolean contains(Path p) {
//			if (p == null) {
//				return false;
//			}
//			int start = _blockPath.indexOf(p.getFirstBlock());
//			if (start >= 0) {
//				for (int i = 1; i < p._blockPath.size(); i++) {
//					if (i+start >= _blockPath.size() || !p._blockPath.get(i).equals(_blockPath.get(i+start))) {
//						return false;
//					}
//				}
//				return true;
//			}
//			return false;
//		}
		
		private boolean containsConnection(Kmer k1, Kmer k2) {
			boolean firstMatch = false;
			for (Kmer k : _blockPath) {
				if (firstMatch) {
					if (k.equals(k2)) {
						return true;
					} else {
						firstMatch = false;
					}
				} else {
					firstMatch = k.equals(k1);
				}
			}
			return false;
		}
		
		private LinkedList<Kmer> getBlockPath() {
			return _blockPath;
		}
		
//		public Iterator<Kmer> iterator() {
//			return _path.iterator();
//		}
		
//		private Block getFirstBlock() {
//			return _blockMap.get(_blockPath.getFirst());
//		}
		
		private Kmer getLastBlock() {
			return _blockPath.getLast();
		}
		
//		private int getInitialCoverage() {
//			return _connectionCoverage.getFirst();
//		}
		
		private void appendBlock(Block b) {
			_blockPath.add(b.getForwardKmer());
//			LinkedList<String> chain = b.getForwardChain();
//			for (String s : chain)
//				_path.add(_kmerMap.get(s));
		}
		
		private void appendPath(Path p) {
//			System.out.println("appending path "+p.toString());
			if (p != null) {
//				_path.addAll(p._path);
				_blockPath.addAll(p._blockPath);
			}
		}
		
		private Path subPath(int startBlock) {
			// returns path starting at the given block index
			Path p = new Path();
			ListIterator<Kmer> blockIterator = _blockPath.listIterator(startBlock-1);
			while (blockIterator.hasNext()) {
				p.appendBlock(_blockMap.get(blockIterator.next()));
			}
			
			return p;
		}
		
		private boolean alignPaths(Path otherPath) {
//			System.out.println();
//			System.out.println("aligning paths:");
//			System.out.println(this.toString());
//			System.out.println(otherPath.toString());
//			System.out.println("size1 = "+size()+"; size2 = "+otherPath.size());
//			System.out.println("blocks match = "+this.getLastBlock().equals(otherPath.getLastBlock()));
			if (size() == otherPath.size() && this.getLastBlock().equals(otherPath.getLastBlock())) {
				String thisSeq = toString();
				String otherSeq = otherPath.toString();
				
//				System.out.println("edit distance = "+editDistance(thisSeq,otherSeq));
//				System.out.println("edit ratio = "+editDistance(thisSeq,otherSeq)/thisSeq.length());
//				System.out.println();
				return ((double)editDistance(thisSeq,otherSeq)/(double)thisSeq.length() < .1);  // a reasonable number, since 1/k should be several times greater than the error rate
			}
//			System.out.println();
			return false;
		}
		
		private void trashPath() {
			// this sticks all the blocks in the path into the recycling bin
			
			for (Kmer k : _blockPath) {
				_recyclingBin.add(k);
			}
			
		}
		
		public String toString() {
			if (_blockPath.isEmpty())
				return "";
			StringBuilder sb = new StringBuilder(_blockPath.getFirst().getHead());
			for (Kmer k : _blockPath) {
				sb.append(_blockMap.get(k).getForwardSequence());
			}
			return sb.toString();
		}
		
		private LinkedList<Path> forkPath() {
			HashMap<Kmer,Integer> arcs = _blockMap.get(getLastBlock()).getForwardConnections();
			LinkedList<Path> newPaths = new LinkedList<Path>();
			for (Kmer k : arcs.keySet()) {
				if (!this.containsConnection(getLastBlock(), k)) {
					Block nextBlock = _blockMap.get(k);
					newPaths.add(new Path(nextBlock));
				}
			}
			
			return newPaths;
		}

		private double getAvgCoverage() {
			if (_blockPath.isEmpty()) {
				return 0;
			}
			
			double totalCoverage = 0;
			for (int i = 1; i < _blockPath.size(); i++)
				totalCoverage += connectionCoverage(_blockPath.get(i-1), _blockPath.get(i));
			
			return totalCoverage/(double)(_blockPath.size() - 1);
		}
		
		private int splitLocation(Path p) {
			ListIterator<Kmer> list1 = this.getBlockPath().listIterator();
			ListIterator<Kmer> list2 = p.getBlockPath().listIterator();
			while (list1.hasNext() && list2.hasNext() && list1.next().equals(list2.next()));
			return list1.nextIndex() - 1;
		}
		
		private double notRedundantSequence() {
			double l = 0;
			for (Kmer k : _blockPath) {
				boolean isRedundant = false;
				for (Path c : _contigs) {
					if (c.contains(k)) {
						isRedundant = true;
						break;
					}
				}
				if (!isRedundant) {
					l += _blockMap.get(k).size();
				}
			}
			return l;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof Path) {
				Path otherPath = (Path) o;
				ListIterator<Kmer> l1 = this.getBlockPath().listIterator();
				ListIterator<Kmer> l2 = otherPath.getBlockPath().listIterator();
				do {
					if (!l1.hasNext() && !l2.hasNext()) {
						return true;
					}
				} while (l1.hasNext() && l2.hasNext() && l1.next().equals(l2.next()));
			}
			return false;
		}
	}
	
	public class pathSizeComparator implements Comparator<Path> {
		@Override
		public int compare(Path o1, Path o2) {
			return o1.size() - o2.size();
		}
	}
	
	public class pathCoverageComparator implements Comparator<Path> {
		@Override
		public int compare(Path o1, Path o2) {
			double c = o2.getAvgCoverage() - o1.getAvgCoverage();
			if (c > 0) {
				return 1;
			} else if (c < 0) {
				return -1;
			} else {
				return 0;
			}
		}
	}
	
	public class pathRedundancyComparator implements Comparator<Path> {
		@Override
		public int compare(Path o1, Path o2) {
			double c = o2.notRedundantSequence() - o1.notRedundantSequence();
			if (c > 0) {
				return 1;
			} else if (c < 0) {
				return -1;
			} else {
				return 0;
			}
		}
	}
	
	public class pathOverlapComparator implements Comparator<Path> {
		private Path _path;
		private pathOverlapComparator(Path p) {
			_path = p;
		}
		
		@Override
		public int compare(Path o1, Path o2) {
			return o2.splitLocation(_path) - o1.splitLocation(_path);
		}
	}
	
	
	public class Kmer {
		
		private final String _seq;
		private LinkedList<Integer> _forwardInstances;
		private LinkedList<Integer> _reverseInstances;
		
		public Kmer(String seq, int readID, boolean isForward) {
			_seq = seq;
			_forwardInstances = new LinkedList<Integer>();
			_reverseInstances = new LinkedList<Integer>();
			if (isForward) {
				_forwardInstances.add(readID);
			} else {
				_reverseInstances.add(readID);
			}
		}
		
		private void addInstance(int readID, boolean isForward) {
			if (isForward) {
				_forwardInstances.add(readID);
			} else {
				_reverseInstances.add(readID);
			}
		}
		
		private String getSeq() {
			return _seq;
		}
		
		private String getHead() {
			return _seq.substring(0,_k-1);
		}
		
		private String getTail() {
			return _seq.substring(1);
		}
		
//		private char getEndChar() {
//			return _seq.charAt(_seq.length() - 1);
//		}
		
		private HashSet<Integer> getReadSet(boolean isForward) {
			HashSet<Integer> readSet = new HashSet<Integer>();
			if (isForward) {
				readSet.addAll(_forwardInstances);
			} else {
				readSet.addAll(_reverseInstances);
			}
			return readSet;
		}
		
		private boolean isConnected(Kmer nextKmer) {
			return getTail().equals(nextKmer.getHead());
		}
		
		private boolean isContinuous(Kmer nextKmer, boolean isForward) {
			return isConnected(nextKmer) && getReadSet(isForward).equals(nextKmer.getReadSet(isForward));
		}
		
		@Override
		public boolean equals(Object o) {
			boolean retval = false;
			if (o instanceof Kmer) {
				Kmer otherKmer = (Kmer) o;
				retval = this.getSeq().equals(otherKmer.getSeq());
			}
			return retval;
		}
		
		@Override
		public int hashCode() {
			return getSeq().hashCode();
		}
		
		public String toString() {
			return _seq;
		}
	}
	
	public class Read {
		
		private int _id;
		private String _seq;
		private LinkedList<String> _kmers;
//		private LinkedList<String> _revKmers;
		
		public Read(int id, String seq) {
			_id = id;
			_seq = seq;
			_kmers = makeKmers(_seq,true);
			makeKmers(getReverseComplement(_seq),false);
		}
		
		public String toString() {
			return _seq;
		}
		
		private int length() {
			return _seq.length();
		}
		
		private LinkedList<String> makeKmers(String seq, boolean isForward) {
			LinkedList<String> listKmers = new LinkedList<String>();
			for (int i = 0; i+_k < seq.length() + 1; i++) {
				String kmerSeq = seq.substring(i, i+_k);
				listKmers.add(kmerSeq);
				Kmer kmer = _kmerMap.get(kmerSeq);
				if (kmer == null) {
					kmer = new Kmer(kmerSeq, _id, isForward);
				} else {
					kmer.addInstance(_id, isForward);
				}
//				if (!isForward)
//					System.out.println("adding kmer "+kmerSeq);
				_kmerMap.put(kmerSeq, kmer);
			}
			return listKmers;
		}
		
		private void makeBlocks() {
			
			//dividing list of kmers into blocks
			int i = 0;
			LinkedList<String> chain = new LinkedList<String>();
			LinkedList<Kmer> blockStarts = new LinkedList<Kmer>();
			String curKmer = _kmers.get(i);
			String prevKmer;
			chain.add(curKmer);
			for (i = 1; i < _kmers.size(); i++) {
				prevKmer = curKmer;
				curKmer = _kmers.get(i);
				if (!_kmerMap.get(prevKmer).isContinuous(_kmerMap.get(curKmer), true)) {
					Block existingBlock = _blockMap.get(_kmerMap.get(chain.get(0)));
					
					if (existingBlock == null) {
						existingBlock = new Block(chain);
					}
					blockStarts.add(_kmerMap.get(chain.get(0)));
					_blockMap.put(_kmerMap.get(chain.get(0)), existingBlock);
					chain = new LinkedList<String>();
				}
				chain.add(curKmer);
			}
			
			// if there is only one block that was never terminated, 
			if (blockStarts.isEmpty() || !blockStarts.getLast().equals(_kmerMap.get(chain.get(0)))) {
				Block existingBlock = _blockMap.get(_kmerMap.get(chain.get(0)));
				
				if (existingBlock == null) {
					existingBlock = new Block(chain);
				}
				
				blockStarts.add(_kmerMap.get(chain.get(0)));
				_blockMap.put(_kmerMap.get(chain.get(0)), existingBlock);
			}
			
			
			//adding connections between blocks within read
			Block prevBlock,curBlock;
			curBlock = _blockMap.get(blockStarts.get(0));
			for (i = 1; i < blockStarts.size(); i++) {
				prevBlock = curBlock;
				curBlock = _blockMap.get(blockStarts.get(i));
				curBlock.addReverseConnection(prevBlock.getReverseKmer());
				prevBlock.addForwardConnection(curBlock.getForwardKmer());
				_blockMap.put(blockStarts.get(i-1),prevBlock);
			}
			_blockMap.put(blockStarts.get(i-1),curBlock);
		}
		
		@Override
		public boolean equals(Object o) {
			boolean retval = false;
			if (o instanceof Read) {
				Read otherRead = (Read) o;
				retval = this.toString().equals(otherRead.toString());
			}
			return retval;
		}
		
		@Override
		public int hashCode() {
			return toString().hashCode();
		}
	}
	
	public class Block {
		
		private LinkedList<String> _forwardChain;
		private LinkedList<String> _reverseChain;
		private int _size;
		
		private LinkedList<Kmer> _forwardConnections;
		private LinkedList<Kmer> _reverseConnections;
		
		public Block(LinkedList<String> chain) {
			_forwardChain = chain;
			_size = _forwardChain.size();
			_reverseChain = new LinkedList<String>();
			for (int i = 0; i < _size; i++) {
				String seq = _forwardChain.get(i);
				String revseq = getReverseComplement(seq);
				_reverseChain.addFirst(revseq);
			}
			_forwardConnections = new LinkedList<Kmer>();
			_reverseConnections = new LinkedList<Kmer>();
		}
		
		private int size() {
			return _size;
		}
		
		private String getForwardSequence() {
			StringBuilder f = new StringBuilder();
			for (String s : _forwardChain) {
				f.append(s.charAt(s.length()-1));
			}
			return f.toString();
		}
		
		public String toString() {
			return getForwardKmer().getHead() + getForwardSequence();
		}
		
//		private LinkedList<String> getForwardChain() {
//			return _forwardChain;
//		}
		
		private Kmer getForwardKmer() {
			return _kmerMap.get(_forwardChain.get(0));
		}
		
		private Kmer getReverseKmer() {
			//System.out.println("looking for "+_reverseChain.get(0));
			return _kmerMap.get(_reverseChain.get(0));
		}
		
		private boolean isStart() {
			return (_reverseConnections.size() == 0);
		}
		
		private boolean isEnd() {
			return (_forwardConnections.size() == 0);
		}
		
		private void addForwardConnection(Kmer kmer) {
			_forwardConnections.add(kmer);
		}
		
		private void removeForwardConnection(Kmer kmer) {
			while(_forwardConnections.remove(kmer));
		}
		
		private void addReverseConnection(Kmer kmer) {
			//System.out.println("adding reverse connection "+kmer.getSeq()+" to "+this.getForwardKmer().getSeq());
			_reverseConnections.add(kmer);
		}
		
		private void removeReverseConnection(Kmer kmer) {
			while(_reverseConnections.remove(kmer));
		}
		
		private HashMap<Kmer,Integer> getForwardConnections() {
//			if (_forwardConnections.isEmpty()) {
//				Kmer finalKmer = _kmerMap.get(_forwardChain.getLast());
//				LinkedList<Kmer> connectedKmers = new LinkedList<Kmer>();
//				
//			}
			
			
			
			HashMap<Kmer,Integer> arcs = new HashMap<Kmer,Integer>();
			HashSet<Kmer> c = new HashSet<Kmer>();
			c.addAll(_forwardConnections);
			for (Kmer kmer : c) {
				if (_blockMap.containsKey(kmer)) {
					arcs.put(kmer, Collections.frequency(_forwardConnections, kmer));
				} else {
					removeForwardConnection(kmer);
				}
			}
			arcs.remove(getForwardKmer());
			
			return arcs;
		}
		
		private HashMap<Kmer,Integer> getReverseConnections() {
			HashMap<Kmer,Integer> arcs = new HashMap<Kmer,Integer>();
			HashSet<Kmer> c = new HashSet<Kmer>();
			c.addAll(_reverseConnections);
			HashMap<Kmer,Block> revmap = getReverseBlockMap();
			for (Kmer kmer : c) {
				if (revmap.containsKey(kmer)) {
					arcs.put(kmer, Collections.frequency(_reverseConnections, kmer));
				} else {
					removeReverseConnection(kmer);
				}
			}
			arcs.remove(getReverseKmer());
			return arcs;
		}
		
		
		
		private Block canCondense() {
			if (getForwardConnections().size() == 1 && _blockMap.get(_forwardConnections.get(0)) != null && _blockMap.get(_forwardConnections.get(0)).getReverseConnections().size() == 1) {
//				System.out.println("condensing blocks "+this.getForwardKmer().getSeq()+" , "+_forwardConnections.get(0).getSeq());
				return _blockMap.get(_forwardConnections.get(0));
			} else {
//				System.out.println(getForwardConnections().size() + " -/- ");
				return null;
			}
		}
		
		private Block condenseBlocks(Block b1, Block b2) {
			LinkedList<String> newChain = new LinkedList<String>();
			newChain.addAll(b1._forwardChain);
			newChain.addAll(b2._forwardChain);
			Block b = new Block(newChain);
			for (Kmer kmer : b2._forwardConnections)
				b.addForwardConnection(kmer);
			
			for (Kmer kmer : b1._reverseConnections)
				b.addReverseConnection(kmer);
			
			_blockMap.remove(b1.getForwardKmer());
			_blockMap.remove(b2.getForwardKmer());
			_blockMap.put(b.getForwardKmer(),b);
			return b;
		}
		
		
		private int countSelfLoop() {
			return Collections.frequency(_forwardConnections, getForwardKmer());
		}
		
		public boolean isInContig() {
			for (Path c : _contigs) {
				if (c.contains(getForwardKmer()))
					return true;
			}
			return false;
		}
		
	}
	
	public static String getReverseComplement(String seq) {
		StringBuilder newSeq = new StringBuilder(seq.toUpperCase());
		newSeq.reverse();
		for (int i = 0; i < newSeq.length(); i++)
			newSeq.setCharAt(i, getComplement(newSeq.charAt(i)));
		
		return newSeq.toString();
	}
	
	private static char getComplement(char c) {
		switch (c) {
		case 'A' : return 'T';
		case 'T' : return 'A';
		case 'C' : return 'G';
		case 'G' : return 'C';
		default : return ' ';
		}
	}
}
