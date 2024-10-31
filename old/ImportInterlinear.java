package bibletext;

import java.util.*;
import creek.*;

public class ImportInterlinear {

	// interlinear structure:
	/* {
		"aliases": {
			alias: book
		}
		"translations": [
			{ book:{ chap:{ verse:text }} }, ...
		],
		"text": {
			book: { chap:{ verse:text }}
		},
		"data": {
			"text":         { book: { chap:{ verse:[ basicIds... ] }} },
			"words":        { basicId: basicWord },
			"lookup":       { basicId: { book:{ chap:[ verses... ] }} }
			"strongs":      { basicId: [ strongsCodes... ] },
			"translations": { strongsCode: [ replacements... ] }
		}
	} */

	private Tree interlinear;
	
	private String book ( String alias ) {
		if (interlinear.auto("aliases").keys().contains( alias )) return interlinear.auto("aliases").get(alias).value();
		return alias;
	}
	
	private List<String> verseIds ( String book, String chap, String verse ) {
		return interlinear.get("data").get("text").get( book(book) ).get(chap).get(verse).values();
	}
	
	private String word ( String basicId ) {
		Tree word = interlinear.get("data").get("words").get(basicId);
		if (word!=null) return word.value();
		else return "";
	}
	
	private List<String> strongs ( String basicId ) {
		Tree strongs = interlinear.get("data").get("strongs").get(basicId);
		if (strongs!=null) return strongs.values();
		else return new ArrayList<>();
	}
	
	private List<String> translations ( String strongsCode ) {
		Tree translations = interlinear.get("data").get("translations").get(strongsCode);
		if (translations!=null) return translations.values();
		else return new ArrayList<>();
	}
	
	

	public ImportInterlinear ( String path ) throws Exception {
		System.err.println( "Loading Interlinear..." );
		interlinear = new JSON( JSON.RETAIN_ORDER );
		interlinear.deserialize( FileActions.read( path ) );
		Stats.displayMemory();
	}
	
	public Table translations ( String alias, String chap, String verse ) throws Exception {
		String book = book(alias);
		Table table = new CSV();
		List<String> row = new ArrayList<String>();
		row.add( alias+" "+chap+":"+verse );
		row.add( interlinear.get("text").get(book).get(chap).get(verse).value() );
		for (Tree translation : interlinear.get("translations").branches()) {
			row.add( translation.get(book).get(chap).get(verse).value() );
		}
		return table.append( row );
	}

	public Table data ( String alias, String chap, String verse ) throws Exception {
		String book = book(alias);
		List<String> idList = verseIds( book, chap, verse );
		Table table = new CSV();
		for (String basicId : idList) {
			String basicWord = word( basicId );
			for (String strongsCode : strongs( basicId )) {
				for (String replacement : translations( strongsCode )) {
					table.append( new String[]{ basicId, basicWord, strongsCode, replacement } );
				}
			}
		}
		return table;
	}
	
	public Table lookup ( String basicId ) throws Exception {
		return new SimpleTable().data(
			interlinear.get("data").get("lookup").get( basicId ).paths()
		);
	}
	
	public static void main ( String[] args ) throws Exception {
		ImportInterlinear i = new ImportInterlinear( args[0] );
		System.out.println( i.translations( args[1], args[2], args[3] ) );
		System.out.println( i.data( args[1], args[2], args[3] ) );
	}
	
}
