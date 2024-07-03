package bibletext;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.*;
import paddle.*;
import creek.*;

public class Bible {

	String wordRegex;
	Alphabet alphabet = new Alphabet();
	
	int wordCount = 0;
	
	Map<String,String> bookAliases;
	
	Tree bibleText;
	Tree bibleData;

	Tree wordLookup;
	Tree wordData;
	
	Tree bibleCompressed;
	
	Tree wordCompounds;
	
	private void increment ( Tree leaf ) {
		String valStr = leaf.value();
		int val = ( valStr!=null ? Integer.parseInt( valStr ) : 0 );
		leaf.value( String.valueOf( ++val ) );
	}
	
	
	
	public Bible () {
		this( "(\\S+)" );
	}
	
	public Bible ( String wordRegex ) {
		this.wordRegex = wordRegex;
		bibleText = new JSON( JSON.RETAIN_ORDER );
		bibleData = new JSON( JSON.RETAIN_ORDER );
		wordLookup = new JSON( JSON.RETAIN_ORDER );
		wordData = new JSON( JSON.RETAIN_ORDER );
		bibleCompressed = new JSON( JSON.RETAIN_ORDER );
		bookAliases = new HashMap<>();
	}
	
	
	
	public void importChapter ( String book, String chap, List<String> verses ) throws Exception {
		int size = verses.size();
		bibleData.auto( book ).auto( "chapters" ).add( chap );
		for (int i=0; i<size; i++) {
			importVerse( book, chap, String.valueOf(i+1), verses.get(i).trim() ); // verses start at 1 instead of 0
		}
	}
	
	public void importVerse ( String book, String chap, String verse, String text ) throws Exception {
		// add the verse text
		Tree chapObj = bibleText.auto( book ).auto( chap ).add( verse, text );
		
		// loop through each word in the verse
		for (String word : Regex.groups( text, wordRegex )) {
			// filter down to simple characters ("basic" form)
			String basic = alphabet.filter( word );
			if (basic.length()>0) {
				// add to word lookup
				wordLookup.auto( basic ).auto( book ).auto( chap ).add( verse, chapObj.get(verse) );
				
				// auto-increment the word ID
				if (!wordData.keys().contains(basic)) wordData.auto( basic ).add( "id", String.valueOf(wordCount++) );
				
				// add the byte hex-string for the word
				//wordData.auto( basic ).add( "bytes", base16( basic ) );
				
				// add the byte hex-string LIST for the word
				if (!wordData.auto( basic ).keys().contains("chars")) wordData.auto( basic ).auto( "chars" ).add( base16List( basic ) );
				
				// also add the "full" word
				increment( wordData.auto( basic ).auto( "full" ).auto( word ) );
				
				// add the verse ID to the compressed structure
				String wordId = wordData.get(basic).get("id").value();
				bibleCompressed.auto( "text" ).auto( book ).auto( chap ).auto( verse ).add( wordId );
				bibleCompressed.auto( "words" ).add( wordId, basic );
				bibleCompressed.auto( "lookup" ).auto( basic ).auto( book ).auto( chap ).add( verse );
			}
		}
	}
	
	public Set<String> books () {
		return bibleText.keys();
	}
	
	public Tree data () {
		return bibleData;
	}
	
	public Map<String,String> aliases () {
		return bookAliases;
	}
	
	public String alias ( String raw ) {
		if (bookAliases.containsKey(raw)) return bookAliases.get(raw);
		else return raw;
	}
	
	public void alias ( String alias, String book ) {
		bookAliases.put( alias, book );
		bibleData.auto( book ).add( "name", alias );
		bibleData.auto( book ).add( "bytes", new Bytes(alias.getBytes( StandardCharsets.UTF_16BE )).toString() );
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
	
	public Tree wordLookup () {
		return wordLookup;
	}
	
	public Tree wordLookup ( String word ) {
		return wordLookup.get( word );
	}
	
	public Tree wordData () {
		return wordData;
	}
	
	public Tree wordData ( String word ) {
		if (wordData.keys().contains( word )) return wordData.get( word );
		else return null;
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

	public void buildSearch () {
		wordCompounds = new JSON( JSON.AUTO_ORDER );
		for (String word : wordLookup().keys()) {
			int wordLength = word.length();
			wordCompounds.auto( word ).auto( "lookup" ).add( word, wordLookup.get( word ) );
			wordCompounds.auto( word ).auto( "data" ).auto( "this" ).add( word, wordData.get( word ) );
			for (int size=2; size<wordLength; size++) {
				int maxPos = wordLength-size;
				for (int pos=0; pos<=maxPos; pos++) {
					String subWord = word.substring(pos, pos+size);
					//System.out.println( subWord );
					if (wordLookup().keys().contains( subWord )) {
						//System.out.println( word+" "+subWord );
						wordCompounds.auto( word ).auto( "data" ).auto( "sub" ).add( subWord, String.valueOf( wordLookup.get(subWord).keys().size() ) );
						wordCompounds.auto( subWord ).auto( "data" ).auto( "super" ).add( word, String.valueOf( wordLookup.get(word).keys().size() ) );
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
