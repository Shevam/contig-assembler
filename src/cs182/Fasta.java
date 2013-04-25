package cs182;

import java.io.*;
import java.util.ArrayList;

public class Fasta {
	
	public static String[] readHeaders(String filename) throws FileNotFoundException, IOException {
		ArrayList<String> headers = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
		String line = br.readLine();
		while (line != null) {
			if (line.startsWith(">"))
				headers.add(line);
			line = br.readLine();
		}
		return (String[]) headers.toArray();
	}
	
	public static String[] readSequences(String filename) throws FileNotFoundException, IOException {
		ArrayList<String> sequences = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
		StringBuilder sb = null;
		String line = br.readLine();
		while (line != null) {
			if (line.startsWith(">")) {
				if (sb != null)
					sequences.add(sb.toString());
				sb = new StringBuilder();
			} else {
				if (sb == null)
					throw new IOException("Invalid fasta format");
				sb.append(line);
			}
			line = br.readLine();
		}
		sequences.add(sb.toString());
		return sequences.toArray(new String[sequences.size()]);
	}

}
