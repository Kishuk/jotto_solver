package jotto.solver;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.ArrayList;

public class JottoSolver {
	private ArrayList<String> dict;
	private int wordLength;
	
	private Object calcMonitor = new Object();
	private int guessesCalculated;
	private GuessSet guesses;
	private JottoCache cache;
	private boolean stopCalc;
	
	private static String[] dictionaryFiles = {"/resource/CROSSWD.TXT", "/resource/CRSWD-D.TXT"};
	private static String cacheSaveFile = "cache.ser";
	
	public JottoSolver(int wordLength) {
		reset(wordLength);
		cache = new JottoCache();
	}
	
	public void reset(int wordLength) {
		this.wordLength = wordLength;
		guessesCalculated = 0;
		dict = new ArrayList<String>();
		refreshDictionary(dict);
		guesses = new GuessSet(wordLength, dict.size());
	}
	
	private void loadIntoDict(ArrayList<String> dict, String filePath) throws IOException {
		String word;
		InputStream is = getClass().getResourceAsStream(filePath);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		while((word = br.readLine()) != null) {
			if(wordLength == -1 || word.length() == wordLength) {
				dict.add(word.toUpperCase());
			}
		}
	}
	
	private void refreshDictionary(ArrayList<String> dict) {
		// load dictionaries
		for(String s : dictionaryFiles) {
			try {
				loadIntoDict(dict, s);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String guessWord() {
		guessesCalculated = 0;
		// Look in cache
		String cachedResult = cache.get(guesses);
		if(cachedResult != null) {
			guessesCalculated = dict.size();
			return cachedResult;
		}
		
		Word optWord = new Word("", Integer.MAX_VALUE);
		int[] minWordMatchCt = new int[wordLength+1];
		boolean keepCalculating = true;
		// The slow parts begin here
		// 
		for(int k=0; k<dict.size() && keepCalculating; k++) {
			guessesCalculated++;
			keepCalculating = !stopCalc;
			String w = dict.get(k);
			int[] wordMatchCt = new int[wordLength+1];
			HashSet<Character> uniqueLets = new HashSet<Character>();
			for(int i=0; i<wordLength; i++) {
				uniqueLets.add(w.charAt(i));
			};
			for(String x : dict) {
				int count = 0;
				for(int i=0; i<wordLength; i++) {
					if(uniqueLets.contains(x.charAt(i))) {
						count++;
					}
				}
				wordMatchCt[count]++;
			}
		    int sum = 0;
			for(int i=0; i<wordMatchCt.length; i++) {
				sum += Math.pow(wordMatchCt[i], 2);
			}
			if(sum < optWord.sum) {
				optWord.word = w;
				optWord.sum = sum;
				minWordMatchCt = wordMatchCt;
			}
		}
		if(!stopCalc) {
			for(int i=0; i<minWordMatchCt.length; i++) {
				System.out.println(" - " + i + " : " + minWordMatchCt[i]);
			}
		}
		// Update cache.
		cache.put(guesses, optWord.word);
		stopCalc = false;
		return optWord.word;
	}
	
	public void tellResults(String myGuess, int matchCount) {
		// Add to guesses.
		guesses = guesses.addGuessResult(myGuess, matchCount, dict.size());
		// Update dict.
		HashSet<Character> uniqueLets = new HashSet<Character>();
		for(int i=0; i<wordLength; i++) {
			uniqueLets.add(myGuess.charAt(i));
		}
		ArrayList<String> temp = new ArrayList<String>();
		temp.addAll(dict);
		for(String w : temp) {
			int count = 0;
			for(int i=0; i<wordLength; i++) {
				if(uniqueLets.contains(w.charAt(i))) {
					count++;
				}
			}
			if(count != matchCount) {
				dict.remove(w);
			}
		}
		Collections.sort(dict);
	}
	
	public String[] getPossibleWords() {
		return dict.toArray(new String[]{});
	}
	
	public int getProgress() {
		return guessesCalculated;
	}
	
	public int getTotalWords() {
		return dict.size();
	}
	
	public int wordLength() {
		return wordLength;
	}
	
	public void killCurrentGuessCalc() {
		synchronized(calcMonitor) {
			stopCalc = true;
		}
	}
	
	private class Word {
		public String word;
		public int sum;
		public Word(String word, int sum) {
			this.word = word; this.sum = sum;
		}
	}
	
	public boolean loadCache() {
		try {
			FileInputStream fis = new FileInputStream(cacheSaveFile);
	        ObjectInputStream ois = new ObjectInputStream(fis);
	        cache = (JottoCache) ois.readObject();
	        ois.close();
	        return true;
		}
		catch(Exception e) {
			//e.printStackTrace();
			return false;
		}
	}
	
	public boolean saveCache() {
		// Clean expired items before save
		cache.cleanExpired();
		
        try {
        	FileOutputStream fos = new FileOutputStream(cacheSaveFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(cache);
			oos.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}
