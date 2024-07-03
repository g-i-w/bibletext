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
	Tree wordLookup;
	Tree wordData;
	
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
		wordLookup = new JSON( JSON.RETAIN_ORDER );
		wordData = new JSON( JSON.RETAIN_ORDER );
		bookAliases = new HashMap<>();
	}
	
	
	
	public void importChapter ( String book, String chap, List<String> verses ) throws Exception {
		int size = verses.size();
		for (int i=0; i<size; i++) {
			importVerse( book, chap, String.valueOf(i+1), verses.get(i).trim() ); // verses start at 1 instead of 0
		}
	}
	
	public void importVerse ( String book, String chap, String verse, String text ) throws Exception {
		Tree chapObj = bibleText.auto( book ).auto( chap ).add( verse, text );
		for (String word : Regex.groups( text, wordRegex )) {
			String basic = alphabet.filter( word );
			if (basic.length()>0) {
				wordLookup.auto( basic ).auto( book ).auto( chap ).add( verse, chapObj.get( verse ) );
				if (!wordData.keys().contains(basic)) wordData.auto( basic ).auto( "id" ).add( String.valueOf(wordCount++) );
				//wordData.auto( basic ).add( "bytes", base16( basic ) );
				if (!wordData.auto( basic ).keys().contains("chars")) wordData.auto( basic ).auto( "chars" ).add( base16List( basic ) );
				increment( wordData.auto( basic ).auto( "full" ).auto( word ) );
			}
		}
	}
	
	public Set<String> books () {
		return bibleText.keys();
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
	}

	public String verse ( String refStr ) throws Exception {
		List<String> ref = Regex.groups( refStr, "(\\S+)\\s*(\\d+):(\\d+)" );
		String book = ref.get(0);
		String chap = ref.get(1);
		String verse = ref.get(2);
		return verse( book, chap, verse );
	}
	
	public String verse ( String book, String chap, String verse ) throws Exception {
		return bibleText.get( alias(book) ).get( chap ).get( verse ).value();
	}
	
	public Tree text () {
		return bibleText;
	}
	
	public Tree wordLookup () {
		return wordLookup;
	}
	
	public Tree wordLookup ( String word ) {
		if (wordLookup.keys().contains( word )) return wordLookup.get( word );
		else return null;
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
	
}
