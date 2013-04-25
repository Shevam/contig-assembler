package cs182.assembler;

import java.io.*;
import java.util.LinkedList;

import cs182.Fasta;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int a = -1;
		int l = -1;
		int k = 21;
		double err = 0;
		boolean genome = false;
		boolean reads = false;
		boolean revcomp = false;
		boolean outputContigs = false;
		boolean outputReads = false;
		boolean contigMap = false;
		boolean deBruijn = false;
		String contigFile = "";
		String readsFile = "";
		String contigMapFile = "";
		String dotFile = "";
		Long seed = null;
		LinkedList<String> fragments = new LinkedList<String>();
		
		String filename;
		
		int i;
		
		if (args.length < 1) {
			printUsage();
		}
		
		for (i = 0; i < args.length - 1; i++) {
			if (args[i].equals("--genome")) {
				genome = true;
			} else if (args[i].equals("--reads")) {
				reads = true;
			} else if (args[i].equals("--reverse-complement")) {
				revcomp = true;
			} else if (args[i].equals("-a")) {
				i++;
				try {
					a = Integer.parseInt(args[i]);
				} catch (NumberFormatException e) {
					System.err.println("ERROR: could not parse numerical argument.");
					printUsage();
				}
			} else if (args[i].equals("-l")) {
				i++;
				try {
					l = Integer.parseInt(args[i]);
				} catch (NumberFormatException e) {
					System.err.println("ERROR: could not parse numerical argument.");
					printUsage();
				}
			} else if (args[i].equals("-k")) {
				i++;
				try {
					k = Integer.parseInt(args[i]);
				} catch (NumberFormatException e) {
					System.err.println("ERROR: could not parse numerical argument.");
					printUsage();
				}
			} else if (args[i].equals("-e")) {
				i++;
				try {
					err = Double.parseDouble(args[i]);
				} catch (NumberFormatException e) {
					System.err.println("ERROR: could not parse numerical argument.");
					printUsage();
				}
			} else if (args[i].equals("--seed")) {
				i++;
				try {
					seed = Long.parseLong(args[i]);
				} catch (NumberFormatException e) {
					System.err.println("ERROR: could not parse numerical argument.");
					printUsage();
				}
			} else if (args[i].equals("--output-reads")) {
				i++;
				outputReads = true;
				readsFile = args[i];
			} else if (args[i].equals("--contig-map")) {
				i++;
				contigMap = true;
				contigMapFile = args[i];
			} else if (args[i].equals("--debruijn-graph")) {
				i++;
				deBruijn = true;
				dotFile = args[i];
			}  else if (args[i].equals("--output-contigs")) {
				i++;
				outputContigs = true;
				contigFile = args[i];
			}else {
				printUsage();
			}
		}
		i = args.length - 1;
		filename = args[i];
		
		if (genome) {
			if (reads || a == -1 || l == -1)
				printUsage();
			
			String sequence = "";
			try {
				String[] sequences = Fasta.readSequences(filename);
				// if there are multiple sequences in the fasta file, just taking the first one
				sequence = sequences[0];
			} catch (FileNotFoundException e) {
				System.err.println("ERROR: file not found");
				printUsage();
			} catch (IOException e) {
				System.err.println("ERROR: error reading fasta file");
				printUsage();
			}
			
			Simulator s;
			if (seed == null) {
				s = new Simulator(sequence, a, l, err);
			} else {
				s = new Simulator(sequence, a, l, err, seed);
			}
			fragments = s.getReads();
			if (revcomp) {
				LinkedList<String> revcompFragments = new LinkedList<String>();
				for (String r : fragments)
					revcompFragments.add(DeBruijnGraph.getReverseComplement(r));
				fragments.addAll(revcompFragments);
			}
			
			if (outputReads) {
				try {
					PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(readsFile)), true);
					for (String r : fragments)
						pw.println(r);
					
					pw.close();
				} catch (IOException e) {
					System.err.println("ERROR: error writing reads file");
					printUsage();
				}
			}
			
		} else if (reads) {
			if (genome || a == -1)
				printUsage();
			
			try {
				BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
				String line = br.readLine();
				while (line != null) {
					fragments.add(line);
					line = br.readLine();
				}
			} catch (FileNotFoundException e) {
				System.err.println("ERROR: file not found");
				printUsage();
			} catch (IOException e) {
				System.err.println("ERROR: error reading reads file");
				printUsage();
			}
			
		} else {
			printUsage();
		}
		
		DeBruijnGraph graph = new DeBruijnGraph(fragments, a, k, revcomp);
		try {
			if (outputContigs)
				graph.outputContigs(contigFile);
			
			if (deBruijn)
				graph.visualizeGraph(dotFile);
			
			if (contigMap) {
				StringBuilder arguments = new StringBuilder();
				for (String s : args)
					arguments.append(s + " ");
				graph.visualizeReads(contigMapFile, arguments.toString());
			}
			
			graph.debugBlocks();
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public static void printUsage() {
		System.err.println("Proper usage:\n" +
				"assembler [options] <filename>\n" +
				"options:\n" +
				"--genome : file provided is a genome in FASTA format (must supply either this flag or --reads)\n" +
				"--reads : file provided is a multi-set of reads to assemble (must supply either this flag or --genome)\n" +
				"-a <int> : coverage generated/expected by the assembler\n" +
				"-l <int> : mean read length (only used with --genome)\n" +
				"-k <int> : length of k-mer fragments (default value = 21)\n" +
				"-e <double> : per-base error rate (substitutions only) between 0 and 1 (default = 0)\n" +
				"--reverse-complement : input the reads and their reverse complements to the assembler (only used with --genome)\n" +
				"\n" +
				"--output-contigs <filename> : output all the assembled contigs to <filename>\n" +
				"--output-reads <filename> : output all generated reads to <filename>. should only be used with --genome flag.\n" +
				"--debruijn-graph <filename> : output file (.dot format) to output the deBruijn graph\n" +
				"--contig-map <filename> : name of .png file to which the contig map should be output\n" +
				"\n" +
				"<filename> : full path to file that contains the genome or reads");
		System.exit(-1);
	}

}
