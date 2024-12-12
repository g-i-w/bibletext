package bibletext;

import java.util.*;
import java.io.*;
import creek.*;

public class InterlinearData {
	public static void main ( String[] args ) throws Exception {

		// main directory
		String rootPath = args[0];


		// main Tree
		Tree jsonTree = new JSON( JSON.RETAIN_ORDER );
		
		
		// Original
		System.err.println( "*** Original ***" );
		
		Bible original = new EBibleOrgText();

		System.err.println( "Loading Hebrew..." );
		original.load( rootPath+"/biblesd/bibles/ebible.org/Hebrew/text/hbo" );
		Stats.displayMemory();

		System.err.println( "Loading Greek..." );
		original.load( rootPath+"/biblesd/bibles/ebible.org/Greek-Ancient/text/grctr" );
		Stats.displayMemory();
		
		jsonTree.add( "text", original.text() );
		jsonTree.add( "data", original.lookupHashed() );
		
		Stats.displayMemory();

		System.err.println( "Reducing memory..." );
		original = null;
		System.gc();
		Thread.sleep(1000);
		Stats.displayMemory();
		
		
		// Translations
		System.err.println( "*** Translations ***" );
		Tree translationsTree = new JSON( JSON.RETAIN_ORDER );

		System.err.println( "Loading English..." );
		Bible english = new EBibleOrgText().load( rootPath+"/biblesd/bibles/ebible.org/English/text/eng-web/" );
		jsonTree.auto( "aliases" ).add( english.aliases() );
		jsonTree.auto( "translations" ).add( "eng-web", english.text() );
		Stats.displayMemory();

		System.err.println( "Loading Chinese..." );
		Bible chinese = new EBibleOrgText().load( rootPath+"/biblesd/bibles/ebible.org/Chinese/text/cmn-cu89s/" );
		jsonTree.auto( "aliases" ).add( chinese.aliases() );
		jsonTree.auto( "translations" ).add( "cmn-cu89s", chinese.text() );
		Stats.displayMemory();

		System.err.println( "Reducing memory..." );
		english = null;
		chinese = null;
		System.gc();
		Thread.sleep(1000);
		Stats.displayMemory();
		
		
		// Strongs
		System.err.println( "*** Strongs ***" );
		Tree strongsTree = new JSON( JSON.RETAIN_ORDER );

		System.err.println( "Loading Strongs..." );
		//Strongs msb = new StrongsMsbNt().load( "biblelookup/majoritybible.com/msb_nt_tables.JSON" );
		Strongs bsb = new StrongsBSB().load( rootPath+"/biblelookup/openbible.org/csv/" );
		Strongs strongs = new StrongsSwordProject().data( bsb ).load( rootPath+"/biblelookup/SwordProject/" );
		strongsTree.map( strongs.dataHashed().map() );
		Stats.displayMemory();
		
		
		// Write output JSON
		FileActions.write( rootPath+"/interlinear.js", "var interlinear = "+jsonTree.serialize()+";\n" );

	}
}
