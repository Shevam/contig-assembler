package cs182.assembler;

import java.util.LinkedList;
import java.util.Random;

public class Simulator {
	
	private String _sequence;
	private int _coverage;
	private int _readLength;
	private int _stdDev = 3;
	private int _sequenceLength;
	private int _numReads;
	private double _error;
	private LinkedList<String> _reads;
	private Random _rand;
	
	public Simulator(String sequence, int coverage, int readLength, double err) {
		_rand = new Random();
		_sequence = sequence;
		_coverage = coverage;
		_readLength = readLength;
		_error = err;
		_sequenceLength = _sequence.length();
		_numReads = _coverage * _sequenceLength / _readLength;
		generateReads();
	}
	
	public Simulator(String sequence, int coverage, int readLength, double err, long seed) {
		_rand = new Random(seed);
		_sequence = sequence;
		_coverage = coverage;
		_readLength = readLength;
		_error = err;
		_sequenceLength = _sequence.length();
		_numReads = _coverage * _sequenceLength / _readLength;
		generateReads();
	}
	
	public LinkedList<String> getReads() {
		return _reads;
	}
	
	public void generateReads() {
		_reads = new LinkedList<String>();
		for (int i = 0; i < _numReads; i++)
			_reads.add(addErrors(sampleRead()));
	}
	
	private String sampleRead() {
		int start;
		int len;
	
		do {
			start = generateReadStart();
			len = generateReadLength();
		} while (start + len > _sequenceLength);
		return _sequence.substring(start, start+len);
	}
	
	private int generateReadLength() {
		return (int) Math.round((_rand.nextGaussian() * _stdDev) + _readLength);
	}
	
	private int generateReadStart() {
		return _rand.nextInt(_sequenceLength);
	}
	
	private String addErrors(String s) {
		if (_error == 0)
			return s;
		
		char[] bases = s.toCharArray();
		for (int i = 0; i < bases.length; i++) {
			if (_rand.nextDouble() < _error) {
				bases[i] = substituteBase(bases[i]);
			}
		}
		return new String(bases);
	}
	
	private char substituteBase(char c) {
		String bases = "ACGT";
		char newBase = c;
		
		while (newBase == c) {
			newBase = bases.charAt(_rand.nextInt(4));
		}
		return newBase;
	}
	
	
	
	

}
