package bibletext;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.*;
import paddle.*;
import creek.*;

public abstract class AbstractBible implements Bible {

	Alphabet alphabet = new Alphabet();
	
	int wordCount = 0;
	
	Map<String,String> bookAliases;
	
	Tree bibleText = new JSON( JSON.RETAIN_ORDER );
	Tree wordLookup = new JSON( JSON.RETAIN_ORDER );
	Tree wordData = new JSON( JSON.RETAIN_ORDER );
	Tree bibleCompressed = new JSON( JSON.RETAIN_ORDER );
	
	Tree wordSearch;
	

	public void importChapter ( String book, String chap, List<String> verses, String wordRegex ) throws Exception {
		int size = verses.size();
		for (int i=0; i<size; i++) {
			importVerse( book, chap, String.valueOf(i+1), verses.get(i).trim(), wordRegex ); // verses start at 1 instead of 0
		}
	}
	
	public void importVerse ( String book, String chap, String verse, String text, String wordRegex ) throws Exception {
		// add the verse text
		Tree chapObj = bibleText.auto( book ).auto( chap ).add( verse, text );
		
		// loop through each word in the verse
		for (String word : Regex.groups( text, wordRegex )) {
			// filter down to simple characters ("basic" form)
			String basic = alphabet.filter( word );
			if (basic.length()>0) {
				// add to word lookup
				wordLookup.auto( basic ).auto( book ).auto( chap ).auto( verse ).increment();
				
				// auto-increment the word ID
				if (!wordData.keys().contains(basic)) wordData.auto( basic ).add( "id", String.valueOf(wordCount++) );
				
				// add the byte hex-string for the word
				//wordData.auto( basic ).add( "bytes", base16( basic ) );
				
				// add the byte hex-string LIST for the word
				if (!wordData.auto( basic ).keys().contains("chars")) wordData.auto( basic ).auto( "chars" ).add( base16List( basic ) );
				
				// also add the "full" word
				wordData.auto( basic ).auto( "full" ).auto( word ).auto( book ).increment();
				
				// add the verse ID to the compressed structure
				String wordId = wordData.get(basic).get("id").value();
				bibleCompressed.auto( "text" ).auto( book ).auto( chap ).auto( verse ).add( wordId );
				bibleCompressed.auto( "words" ).add( wordId, basic );
				bibleCompressed.auto( "lookup" ).auto( basic ).auto( book ).auto( chap ).add( verse );
			}
		}
	}
	
	///////////////////////// ABSTRACT /////////////////////////
	public abstract Bible load ( String path ) throws Exception;
	
	public Set<String> books () {
		return bibleText.keys();
	}
	
	public Map<String,String> aliases () {
		if (bookAliases==null) bookAliases = new HashMap<>();
		return bookAliases;
	}
	
	public String alias ( String raw ) {
		if (aliases().containsKey(raw)) return aliases().get(raw);
		else return raw;
	}
	
	public String verse ( String refStr ) throws Exception {
		List<String> ref = Regex.groups( refStr, "([^\\d\\s]+)\\s*(\\d+):(\\d+)" );
		String book = ref.get(0);
		String chap = ref.get(1);
		String verse = ref.get(2);
		return verse( book, chap, verse );
	}
	
	public String verse ( String book, String chap, String verse ) throws Exception {
		System.out.println( book+" "+chap+":"+verse );
		return bibleText.get( alias(book) ).get( chap ).get( verse ).value();
	}
	
	public Tree text () {
		return bibleText;
	}
	
	public Tree lookup () {
		return wordLookup;
	}
	
	public Tree words () {
		return wordData;
	}
	
	public Tree compressed () {
		return bibleCompressed;
	}

	public String base16 ( String word ) {
		return new Bytes( word.getBytes(StandardCharsets.UTF_16BE) ).toString();
	}
	
	public List<String> base16List ( String word ) {
		List<String> list = new ArrayList<>();
		for (int i=0; i<word.length(); i++) {
			String sub = word.substring(i,i+1);
			list.add( new Bytes( sub.getBytes(StandardCharsets.UTF_16BE) ).toString() );
		}
		return list;
	}
	
	public Tree fullWord ( String word ) {
		if (wordData.keys().contains( word )) return wordData.get(word).get("full");
		else return null;
	}

	/*public void buildSearch () {
		wordSearch = new JSON( JSON.AUTO_ORDER );
		for (String word : wordLookup.keys()) {
			int wordLength = word.length();
			//wordSearch.auto( word ).auto( "lookup" ).add( word, wordLookup.get( word ) );
			wordSearch.auto( word ).auto( "this" ).add( word, wordLookup.get( word ) );
			for (int size=2; size<wordLength; size++) {
				int maxPos = wordLength-size;
				for (int pos=0; pos<=maxPos; pos++) {
					String subWord = word.substring(pos, pos+size);
					//System.out.println( subWord );
					if (wordLookup.keys().contains( subWord )) {
						//System.out.println( word+" "+subWord );
						//wordSearch.auto( word ).auto( "data" ).auto( "sub" ).add( subWord, String.valueOf( wordLookup.get(subWord).keys().size() ) );
						//wordSearch.auto( subWord ).auto( "data" ).auto( "super" ).add( word, String.valueOf( wordLookup.get(word).keys().size() ) );
						wordSearch.auto( word ).auto( "sub" ).add( subWord, wordLookup.get(subWord) );
						wordSearch.auto( subWord ).auto( "super" ).add( word, wordLookup.get(word) );
					}
				}
			}
		}
		
	}*/
	
	public void buildSearch () {
		wordSearch = new JSON( JSON.AUTO_ORDER );
		for (String word : wordLookup.keys()) {
			wordSearch.auto( word ).auto( "this" ).add( word, wordLookup.get( word ) );
			for (String subWord : StringFunctions.substrings( word, 2 )) {
				if (wordLookup.keys().contains( subWord )) {
					wordSearch.auto( word ).auto( "sub" ).add( subWord, wordLookup.get(subWord) );
					wordSearch.auto( subWord ).auto( "super" ).add( word, wordLookup.get(word) );
				}
			}
		}
	}
	
	public Tree search () {
		if (wordSearch==null) buildSearch();
		return wordSearch;
	}
	
	public Tree search ( String word ) {
		if (wordSearch==null) buildSearch();
		
		if (wordSearch.keys().contains(word)) {
			return wordSearch.get(word);
		} else {
			String basicWord = basic(word);
			if (wordSearch.keys().contains(basicWord)) {
				return wordSearch.get(basicWord);
			} else {
				for (String subStr : StringFunctions.substrings( basicWord, 2 )) {
					if (wordSearch.keys().contains(subStr)) {
						return wordSearch.get(subStr);
					}
				}
				return null;
			}
		}
	}
	
	public String basic ( String full ) {
		return alphabet.filter(full);
	}

}
