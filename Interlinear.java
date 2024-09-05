package bibletext;

import java.util.*;
import creek.*;

public class Interlinear {

	private Bible bible;
	private Strongs strongs;
	
	public Interlinear () throws Exception {
		bible = new EBibleOrgText();
		//strongs = new STEPBibleData();

		Stats.displayMemory();

		System.err.println( "Loading Hebrew..." );
		bible.load( "biblesd/bibles/ebible.org/Hebrew/text/hbo" );
		System.err.println( "done." );
		Stats.displayMemory();

		System.err.println( "Loading Greek..." );
		bible.load( "biblesd/bibles/ebible.org/Greek-Ancient/text/grctr" );
		System.err.println( "done." );
		Stats.displayMemory();

		System.err.println( "Loading Strongs..." );
		//Strongs msb = new StrongsMsbNt().load( "biblelookup/majoritybible.com/msb_nt_tables.csv" );
		Strongs bsb = new StrongsBSB().load( "biblelookup/openbible.org/csv/" );
		strongs = new StrongsSwordProject().data( bsb ).load( "biblelookup/SwordProject/" );
		System.err.println( "done." );
		Stats.displayMemory();
	}
	
	public Table verse ( String book, String chap, String verse ) throws Exception {
		List<String> idList = bible.compressed().get("text").get(book).get(chap).get(verse).values();
		Table table = new SimpleTable();
		for (String id : idList) {
			String basicWord = bible.compressed().get("basic").get(id).value();
			
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
		Interlinear i = new Interlinear();
		//System.out.println( i.strongs().data().get("basic").serialize() );
		System.out.println( i.bible().books() );
		System.out.println( i.verse( args[0], args[1], args[2] ) );
	}
	
}
