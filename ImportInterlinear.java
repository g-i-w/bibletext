package bibletext;

import java.util.*;
import creek.*;

public class ImportInterlinear {

	private Tree interlinear;
	
	public ImportInterlinear ( String path ) throws Exception {
		System.err.println( "Loading Interlinear..." );
		interlinear = new JSON( JSON.RETAIN_ORDER );
		interlinear.deserialize( FileActions.read( path ) );
		Stats.displayMemory();
	}
	
	public Table parallel ( String book, String chap, String verse ) throws Exception {
		return interlinear.get("original").get(book).get(chap).get(verse);
	}

	public Table strongs ( String book, String chap, String verse ) throws Exception {
		List<String> idList = interlinear.get("data").get("text").get(book).get(chap).get(verse).values();
		Table table = new SimpleTable();
		for (String basicId : idList) {
			String basicWord = interlinear.get("data").get("basic").get(basicId).value();
			for (String strongsCode : interlinear.get("strongs").get("basic").get(basicId).values()) {
				for (String replacement : interlinear.get("strongs").get("translate").get(strongsCode).values()) {
					table.append( String[]{ basicId, basicWord, strongsCode, replacement } );
				}
			}
		}
		return table;
	}
	
	public Tree text () {
		return interlinear.get("text");
	}
	
	public Tree compressed () {
		return interlinear.get("compressed");
	}
	
	public Tree strongs () {
		return interlinear.get("strongs");
	}
	
	public static void main ( String[] args ) throws Exception {
		ImportInterlinear i = new ImportInterlinear( args[0] );
		System.out.println( i.text().keys() );
		System.out.println( i.verse( args[1], args[2], args[3] ) );
	}
	
}
