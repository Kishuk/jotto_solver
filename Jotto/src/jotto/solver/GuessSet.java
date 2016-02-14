package jotto.solver;

import java.io.Serializable;
import java.util.ArrayList;

public class GuessSet implements Serializable {
	private static final long serialVersionUID = -1262749080150328413L;
	private ArrayList<String> guesses;
	private ArrayList<Integer> matchCounts;
	private int dictSize;
	private int wordLength;
	// Identifier for age in cache
	public int cacheNum;
	
	public GuessSet(int wordLength, int dictSize) {
		this.guesses = new ArrayList<String>();
		this.matchCounts = new ArrayList<Integer>();
		this.wordLength = wordLength;
		this.dictSize = dictSize;
	}
	
	public GuessSet addGuessResult(String guess, int matches, int newDictSize) {
		GuessSet newSet = new GuessSet(wordLength, newDictSize);
		newSet.guesses.addAll(guesses);
		newSet.guesses.add(guess);
		newSet.matchCounts.addAll(matchCounts);
		newSet.matchCounts.add(matches);
		return newSet;
	}
	
	public int possibleWordCt() {
		return dictSize;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof GuessSet)) {
			return false;
		}
		GuessSet gs = (GuessSet)o;
		if(guesses.size() != gs.guesses.size()) {
			return false;
		}
		if(wordLength != gs.wordLength) {
			return false;
		}
		for(int i=0; i<guesses.size(); i++) {
			if(!guesses.get(i).equals(gs.guesses.get(i)) ||
			   !matchCounts.get(i).equals(gs.matchCounts.get(i))) {
				return false;
			}
		}
		return true;
	}
	@Override
	public int hashCode() {
		int result = 0;
		for(int i=0; i<guesses.size(); i++) {
			result += guesses.get(i).hashCode();
			result += matchCounts.get(i).hashCode();
		}
		result += 100000*((Integer)wordLength).hashCode();
		return result;
	}
}
