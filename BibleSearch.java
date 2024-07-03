package bibletext;

import java.util.*;
import java.io.*;
import creek.*;

public class BibleSearch {

	private Bible bible;
	private int minWordSize;
	private Alphabet alphabet;
	
	private Tree wordCompounds;

	public BibleSearch ( Bible bible, int minWordSize ) {
		alphabet = new Alphabet();
	
		if (minWordSize<1) minWordSize = 1;
	
		this.bible = bible;
		this.minWordSize = minWordSize;

		wordCompounds = new JSON( JSON.AUTO_ORDER );
		Set<String> wordSet = bible.wordLookup().keys();
		for (String word : wordSet) {
			int wordLength = word.length();
			Tree fullWord = bible.fullWord( word );
			Tree lookupTree = bible.wordLookup( word );
			Tree basicWord = wordCompounds.auto( word ).auto( "this" ).add( word, lookupTree );
			for (String full : fullWord.keys()) {
				wordCompounds.auto( full ).auto( "this" ).add( word, basicWord );
			}
			//System.out.println( word+" "+wordLength );
			for (int size=minWordSize; size<wordLength; size++) {
				int maxPos = wordLength-size;
				for (int pos=0; pos<=maxPos; pos++) {
					String subWord = word.substring(pos, pos+size);
					//System.out.println( subWord );
					if (wordSet.contains( subWord )) {
						//System.out.println( word+" "+subWord );
						wordCompounds.auto( word ).auto( "sub" ).add( subWord, bible.wordLookup().get(subWord) );
						wordCompounds.auto( subWord ).auto( "super" ).add( word, bible.wordLookup().get(word) );
					}
				}
			}
		}
		
		
	}
	
	public Tree compounds () {
		return wordCompounds;
	}
	
	public Tree search ( String word ) {
		word = alphabet.filter(word);
		if (wordCompounds.keys().contains(word)) {
			return wordCompounds.get(word);
		} else {
			return null;
		}
	}
	

}
