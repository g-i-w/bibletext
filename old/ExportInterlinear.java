package bibletext;

import java.util.*;
import creek.*;

public class ExportInterlinear {

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
	
	public ExportInterlinear ( String exportPath ) throws Exception {
	
		interlinear = new JSON( JSON.RETAIN_ORDER );

		System.err.println( "*** Original ***" );
		
		Bible original = new EBibleOrgText();

		System.err.println( "Loading Hebrew..." );
		original.load( "biblesd/bibles/ebible.org/Hebrew/text/hbo" );
		Stats.displayMemory();

		System.err.println( "Loading Greek..." );
		original.load( "biblesd/bibles/ebible.org/Greek-Ancient/text/grctr" );
		Stats.displayMemory();
		
		System.err.println( "Adding to interlinear object..." );
		interlinear.add( "text", original.text() );
		interlinear.add( "data", original.compressed() );
		
		Stats.displayMemory();

		System.err.println( "Reducing memory..." );
		original = null;
		System.gc();
		Thread.sleep(1000);
		Stats.displayMemory();

		System.err.println( "*** Translations ***" );

		System.err.println( "Adding English..." );
		Bible english = new EBibleOrgText().load( "biblesd/bibles/ebible.org/English/text/eng-web/" );
		interlinear.auto( "aliases" ).add( english.aliases() );
		interlinear.auto( "translations" ).add( english.text() );
		Stats.displayMemory();

		System.err.println( "Adding Chinese..." );
		Bible chinese = new EBibleOrgText().load( "biblesd/bibles/ebible.org/Chinese/text/cmn-cu89s/" );
		interlinear.auto( "aliases" ).add( chinese.aliases() );
		interlinear.auto( "translations" ).add( chinese.text() );
		Stats.displayMemory();

		System.err.println( "Reducing memory..." );
		english = null;
		chinese = null;
		System.gc();
		Thread.sleep(1000);
		Stats.displayMemory();

		System.err.println( "*** Strongs ***" );

		System.err.println( "Loading Strongs..." );
		//Strongs msb = new StrongsMsbNt().load( "biblelookup/majoritybible.com/msb_nt_tables.csv" );
		Strongs bsb = new StrongsBSB().load( "biblelookup/openbible.org/csv/" );
		Strongs strongs = new StrongsSwordProject().data( bsb ).load( "biblelookup/SwordProject/" );
		Stats.displayMemory();
		
		System.err.println( "Adding Strongs..." );
		Set<String> notFound = new LinkedHashSet<>();
		Set<String> noTranslation = new LinkedHashSet<>();
		Set<String> strongsUsed = new TreeSet<>();
		for (String id : interlinear.get( "data" ).get( "words" ).keys()) {
			String basicWord = interlinear.get( "data" ).get( "words" ).get( id ).value();
			Tree strongsCodes = strongs.data().get( "basic" ).get( basicWord );
			if (strongsCodes==null) {
				notFound.add( basicWord );
				continue;
			}
			for (String strongsCode : strongsCodes.keys()) {
				interlinear.auto( "data" ).auto( "strongs" ).auto( id ).add( strongsCode );
				strongsUsed.add( strongsCode );
			}
		}
		for (String strongsCode : strongsUsed) {
			Tree replacements = strongs.data().get("strongs").get( strongsCode );
			if (replacements==null) {
				noTranslation.add( strongsCode );
				continue;
			}
			for (String replacement : replacements.keys()) {
				interlinear.auto( "data" ).auto( "translations" ).auto( strongsCode ).add( replacement );
			}
		}
		Stats.displayMemory();
		
		System.out.println( "basic words not found in Strongs: "+notFound.size() );
		Thread.sleep(1000);
		System.out.println( "strongs codes with no translation: "+noTranslation.size() );
		Thread.sleep(1000);

		System.err.println( "*** Writing... ***" );

		FileActions.write( exportPath, interlinear.serialize(), "UTF-8" );
		
	}
		
	public static void main ( String[] args ) throws Exception {
		ExportInterlinear export = new ExportInterlinear( args[0] );
	}
	
}
