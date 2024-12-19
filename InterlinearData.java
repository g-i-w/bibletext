package bibletext;

import java.util.*;
import java.io.*;
import creek.*;

public class InterlinearData {

	public static void loadLanguage ( Tree jsonTree, String rootPath, String langName, String langCode, String langTitle ) throws Exception {
		System.err.println( "Loading "+langName+"..." );
		Bible lang = new EBibleOrgText().load( rootPath+"/biblesd/bibles/ebible.org/"+langName+"/text/"+langCode+"/" );
		jsonTree.auto( "aliases" ).auto( langTitle ).add( lang.aliases() );
		jsonTree.auto( "translations" ).add( langTitle, lang.text() );
		System.err.println( "Reducing memory..." );
		lang = null;
		Stats.displayMemory();
		System.gc();
	}


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

		/*
		System.err.println( "Loading English..." );
		Bible english = new EBibleOrgText().load( rootPath+"/biblesd/bibles/ebible.org/English/text/engwebp/" );
		jsonTree.auto( "aliases" ).auto( "English" ).add( english.aliases() );
		jsonTree.auto( "translations" ).add( "English", english.text() );
		Stats.displayMemory();

		System.err.println( "Loading Chinese..." );
		Bible chinese = new EBibleOrgText().load( rootPath+"/biblesd/bibles/ebible.org/Chinese/text/cmn-cu89s/" );
		jsonTree.auto( "aliases" ).auto( "简体中文" ).add( chinese.aliases() );
		jsonTree.auto( "translations" ).add( "简体中文", chinese.text() );
		Stats.displayMemory();
		*/
		
		loadLanguage( jsonTree, rootPath, "English", "engwebp", "English" );
		loadLanguage( jsonTree, rootPath, "Chinese", "cmn-cu89s", "新标点和合本 (Simplified Chinese)" );
		loadLanguage( jsonTree, rootPath, "Hindi", "hincv", "सरल हिन्दी बाइबल (Hindi)" );
		loadLanguage( jsonTree, rootPath, "Spanish", "spablm", "Santa Biblia (Spanish)" );
		loadLanguage( jsonTree, rootPath, "French", "frasbl", "Sainte Bible (French)" );
		loadLanguage( jsonTree, rootPath, "Arabic", "arbnav", "كتاب الحياة (Arabic)" );
		loadLanguage( jsonTree, rootPath, "Bengali", "benirv", "ইন্ডিয়ান রিভাইজড ভার্সন (Bengali)" );
		loadLanguage( jsonTree, rootPath, "Russian", "russyn", "Синодальный перевод (Russian)" );
		
		
		// Strongs
		System.err.println( "*** Strongs ***" );

		System.err.println( "Loading Strongs..." );
		//Strongs msb = new StrongsMsbNt().load( "biblelookup/majoritybible.com/msb_nt_tables.JSON" );
		Strongs bsb = new StrongsBSB().load( rootPath+"/biblelookup/openbible.org/csv/" );
		Strongs strongs = new StrongsSwordProject().data( bsb ).load( rootPath+"/biblelookup/SwordProject/" );
		jsonTree.auto( "strongs" ).map( strongs.dataHashed().map() );
		Stats.displayMemory();
		
		
		// Write output JSON
		FileActions.write( rootPath+"/interlinear.js", "var interlinear = "+jsonTree.serialize()+";\n" );

	}
}
