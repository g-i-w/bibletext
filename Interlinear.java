package bibletext;

import java.util.*;
import creek.*;

public class Interlinear {

	private Bible bible;
	private Strongs strongs;
	
	public Interlinear ( String hebrewPath, String greekPath, String strongsPath ) throws Exception {
		bible = new EBibleOrgText();
		strongs = new STEPBibleData();

		System.err.print( "Loading Hebrew..." );
		bible.load( hebrewPath );
		System.err.println( "done." );
		Stats.displayMemory();

		System.err.print( "Loading Greek..." );
		bible.load( greekPath );
		System.err.println( "done." );
		Stats.displayMemory();

		System.err.print( "Loading Strongs..." );
		strongs.load( strongsPath );
		System.err.println( "done." );
		Stats.displayMemory();
	}
	
	public Table verse ( String book, String chap, String verse ) throws Exception {
		List<String> idList = bible.compressed().get("text").get(book).get(chap).get(verse).values();
		Table table = new SimpleTable();
		for (String id : idList) {
			String basicWord = bible.compressed().get("words").get(id).value();
			Tree strongsObj = strongs.data().get("basic").get(basicWord);
			if (strongsObj != null) {
				List<List<String>> paths = strongsObj.paths();
				for (List<String> path : paths) {
					path.add( 0, basicWord );
					path.add( 0, id );
					table.append( path );
				}
			} else {
				table.append(
					Arrays.asList(
						new String[]{ id, basicWord, "-", "-", "-" }
					)
				);
			}
		}
		return table;
	}
	
	public Bible bible () {
		return bible;
	}
	
	public Strongs strongs () {
		return strongs;
	}
	
	public static void main ( String[] args ) throws Exception {
		Interlinear ie = new Interlinear( args[0], args[1], args[2] );
		System.out.println( ie.bible().books() );
		System.out.println( ie.verse( args[3], args[4], args[5] ) );
	}
	
}
