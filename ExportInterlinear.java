package bibletext;

import java.util.*;
import creek.*;

public class ExportInterlinear {

	private Bible bible;
	private Strongs strongs;
	private Tree interlinear;
	
	public ExportInterlinear ( String exportPath, String[] parallelBiblePaths ) throws Exception {
		bible = new EBibleOrgText();
		//strongs = new STEPBibleData();

		System.err.println( "*** ExportInterlinear ***" );
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
		
		// interlinear structure:
		/* {
			"aliases": {
				alias: book
			}
			"original": {
				book:{ chap:{ verse:text }}
			},
			"translations": [
				{ book:{ chap:{ verse:text }} }, ...
			],
			"data": {
				"text":     { book: { chap:{ verse:[ basicIds... ] }} },
				"basic":    { basicId: basicWord },
				"lookup":   { basicId: { book:{ chap:[ verses... ] }} }
			},
			"strongs": {
				"basic":     { basicId: [ strongsCodes... ] },
				"translate": { strongsCode: [ replacements... ] }
			}
		} */

		System.err.println( "Adding Bible data..." );
		interlinear = new JSON( JSON.RETAIN_ORDER )
			.add( "original", bible.text() )
			.add( "data", bible.compressed() )
		;
		Stats.displayMemory();
		
		System.err.println( "Adding Strongs data..." );
		for (String basicId : bible.compressed().get("basic").keys()) {
			String basicWord = bible.compressed().get("basic").get(basicId).value();
			for (String strongsCode : strongs.data().get("basic").get(basicWord).keys()) {
				interlinear.auto( "strongs" ).auto( "basic" ).auto( basicId ).add( strongsCode );
				for (String replacement : strongs.data().get("strongs").get( strongsCode ).keys()) {
					interlinear.auto( "strongs" ).auto( "translate" ).auto( strongsCode ).add( replacement );
				}
			}
		}
		Stats.displayMemory();		
		
		FileActions.write( exportPath, interlinear.serialize(), "UTF-8" );
		
	}
		
	public static void main ( String[] args ) throws Exception {
		ExportInterlinear export = new ExportInterlinear( args[0] );
	}
	
}
