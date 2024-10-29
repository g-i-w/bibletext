package bibletext;

import java.util.*;
import java.io.*;
import creek.*;

public class InterlinearDirectory {
	public static void main ( String[] args ) throws Exception {

		// main directory
		String rootDir = args[0];
		File interlinearDir = new File( rootDir );
		
		
		// aliases tree
		Tree aliasesTree = new FilesystemTree( new File( interlinearDir, "aliases" ) );


		// Original
		System.err.println( "*** Original ***" );
		
		Bible original = new EBibleOrgText();
		Tree originalTree = new DataFileTree( new File( interlinearDir, "original" ), 1 );

		System.err.println( "Loading Hebrew..." );
		original.load( rootDir+"/biblesd/bibles/ebible.org/Hebrew/text/hbo" );
		Stats.displayMemory();

		System.err.println( "Loading Greek..." );
		original.load( rootDir+"/biblesd/bibles/ebible.org/Greek-Ancient/text/grctr" );
		Stats.displayMemory();
		
		originalTree.add( "text", original.text() );
		originalTree.add( "data", original.lookupHashed() );
		
		Stats.displayMemory();

		System.err.println( "Reducing memory..." );
		original = null;
		System.gc();
		Thread.sleep(1000);
		Stats.displayMemory();
		
		
		// Translations
		System.err.println( "*** Translations ***" );
		Tree translationsTree = new DataFileTree( new File( interlinearDir, "translations" ), 3 );

		System.err.println( "Loading English..." );
		Bible english = new EBibleOrgText().load( rootDir+"/biblesd/bibles/ebible.org/English/text/eng-web/" );
		aliasesTree.add( english.aliases() );
		translationsTree.add( "eng-web", english.text() );
		Stats.displayMemory();

		System.err.println( "Loading Chinese..." );
		Bible chinese = new EBibleOrgText().load( rootDir+"/biblesd/bibles/ebible.org/Chinese/text/cmn-cu89s/" );
		aliasesTree.add( chinese.aliases() );
		translationsTree.add( "cmn-cu89s", chinese.text() );
		Stats.displayMemory();

		System.err.println( "Reducing memory..." );
		english = null;
		chinese = null;
		System.gc();
		Thread.sleep(1000);
		Stats.displayMemory();
		
		
		// Strongs
		System.err.println( "*** Strongs ***" );
		Tree strongsTree = new DataFileTree( new File( interlinearDir, "strongs" ), 2 );

		System.err.println( "Loading Strongs..." );
		//Strongs msb = new StrongsMsbNt().load( "biblelookup/majoritybible.com/msb_nt_tables.csv" );
		Strongs bsb = new StrongsBSB().load( rootDir+"/biblelookup/openbible.org/csv/" );
		Strongs strongs = new StrongsSwordProject().data( bsb ).load( rootDir+"/biblelookup/SwordProject/" );
		strongsTree.map( strongs.dataHashed().map() );
		Stats.displayMemory();
		

	}
}
