package jotto.solver;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class JottoCache implements Serializable {
	private static final long serialVersionUID = 8152857895174325353L;
	// We won't cache if the set of guesses has narrowed
	// the possible dictionary to a size smaller than this (in words)
	private static final int MAX_DICT_SIZE = 1000;
	
	// Max number of items in the cache - oldest ones beyond this will be deleted after a call to cleanExpired
	private static final int CACHE_LIMIT = 100;
	
	private int ageIdNext = 0;
	
	private HashMap<GuessSet, CacheObj> cache = new HashMap<GuessSet, CacheObj>();
	
	public String get(GuessSet gs) {
		CacheObj co = cache.get(gs);
		if(co != null) {
			// Update age of this cache item
			co.ageId = ageIdNext++;
		}
		return co == null ? null : co.guess;
	}
	
	public String put(GuessSet gs, String ans) {
		if(gs.possibleWordCt() >= MAX_DICT_SIZE) {
			CacheObj old = cache.put(gs, new CacheObj(ans, ageIdNext++));
			return old == null ? null : old.guess;
		}
		return null;
	}
	
	public void cleanExpired() {
		int numToDelete = cache.size() - CACHE_LIMIT;
		if(numToDelete > 0) {
			CacheObjAgeComparator coac = new CacheObjAgeComparator(cache);
			TreeMap<GuessSet, CacheObj> sorted_map = new TreeMap<GuessSet, CacheObj>(coac);
			sorted_map.putAll(cache);
			
			// Will re-assign ageIds to numbers [0 .. (CACHE_LIMIT-1)]
			ageIdNext = 0;
			
			for(GuessSet g : sorted_map.keySet()) {
				CacheObj co = cache.get(g);
				if(numToDelete > 0) {
					// Purge from cache
					cache.remove(g);
					numToDelete--;
				}
				else {
					// Reassign ageId
					co.ageId = ageIdNext++;
				}
			}
		}
	}
	
	private class CacheObj implements Serializable {
		private static final long serialVersionUID = 1771676120133822121L;
		public String guess;
		public int ageId;
		public CacheObj(String guess, int ageId) {
			this.guess = guess;
			this.ageId = ageId;
		}		
	}
	
	// http://stackoverflow.com/questions/109383/how-to-sort-a-mapkey-value-on-the-values-in-java
	private class CacheObjAgeComparator implements Comparator<GuessSet> {
		Map<GuessSet, CacheObj> base;
		public CacheObjAgeComparator(Map<GuessSet, CacheObj> base) {
			this.base = base;
		}
		
		@Override
		public int compare(GuessSet a, GuessSet b) {
			// Order oldest items first
			return base.get(a).ageId - base.get(b).ageId;
		}
		
	}
}
