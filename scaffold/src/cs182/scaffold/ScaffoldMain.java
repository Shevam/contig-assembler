package cs182.scaffold;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ScaffoldMain {

	/**
	 * @param args contig-file matepair-file
	 */
	public static void main(String[] args) {
		
		if (args.length != 2) {
			System.err.println("Wrong number of arguments.");
			printUsage();
		}
		
		Scaffold s = new Scaffold();
		
		//reading in contigs
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(args[0])));
			String line = br.readLine();
			while (line != null) {
				s.addContig(line);
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			System.err.println("ERROR reading from contig file.");
			printUsage();
			return;
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(args[1])));
			String line = br.readLine();
			while (line != null) {
				s.addMatePair(line);
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			System.err.println("ERROR reading from mate-pair file.");
			printUsage();
			return;
		}
		
		s.constructScaffold();
		
		
	}
	
	
	
	public static void printUsage() {
		System.err.println("Proper Usage:");
		System.err.println("scaffold <contig-file> <mate-pair-file>");
		System.exit(-1);
	}
}
