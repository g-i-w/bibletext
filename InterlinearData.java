package bibletext;

import java.util.*;
import java.io.*;
import creek.*;

public class InterlinearData {

	public static void loadLanguage ( Tree jsonTree, Tree aliasesTree, String rootPath, String langName, String langCode, String langTitle ) throws Exception {
		System.err.println( "Loading "+langName+"..." );
		EBibleOrgText lang = new EBibleOrgText();
		lang.load( rootPath+"/biblesd/bibles/ebible.org/"+langName+"/text/"+langCode+"/" );
		if (aliasesTree!=null) {
			aliasesTree.auto( "langToCode" ).auto( langTitle ).add( lang.aliases() );
			aliasesTree.auto( "codeToLang" ).auto( langTitle ).map( lang.names().map() );
		}
		jsonTree.auto( "translations" ).add( langTitle, lang.text() );
		System.err.println( "Reducing memory..." );
		lang = null;
		Stats.displayMemory();
		System.gc();
	}
	
	public static void writeLanguage ( String rootPath, String langName, String langCode, String langTitle ) throws Exception {
		Tree langTree = new JSON( JSON.RETAIN_ORDER );
		loadLanguage( langTree, null, rootPath, langName, langCode, langTitle );
		FileActions.write( rootPath+"/"+langCode+".js", "interlinear.translations['"+langTitle+"'] = "+langTree.get("translations").get(langTitle).serialize()+";\n" );
	}


	public static void main ( String[] args ) throws Exception {

		// main directory
		String rootPath = args[0];


		// Tree structures
		Tree jsonTree = new JSON( JSON.RETAIN_ORDER );
		Tree aliasesTree = new JSON( JSON.RETAIN_ORDER );
		
		
		// Strongs
		System.err.println( "*** Strongs ***" );

		System.err.println( "Loading Strongs..." );
		//Strongs msb = new StrongsMsbNt().load( "biblelookup/majoritybible.com/msb_nt_tables.JSON" );
		Strongs bsb = new StrongsBSB().load( rootPath+"/biblelookup/openbible.org/csv/" );
		Strongs strongs = new StrongsSwordProject().data( bsb ).load( rootPath+"/biblelookup/SwordProject/" );
		jsonTree.auto( "strongs" ).map( strongs.dataHashed().map() );
		Stats.displayMemory();
		

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
		//jsonTree.add( "data", original.lookupHashed() );
		for (String hash : original.lookupHashed().keys()) {
			for (String basic : original.lookupHashed().get(hash).keys()) {
				for (String book : original.lookupHashed().get(hash).get(basic).keys()) {
					for (String chap : original.lookupHashed().get(hash).get(basic).get(book).keys()) {
						for (String verse : original.lookupHashed().get(hash).get(basic).get(book).get(chap).keys()) {
							String code = "";
							List<String> path = Arrays.asList( new String[]{ basic, book, chap, verse } );
							Tree codeNode = strongs.data().get("lookup").get( path );
							if (codeNode!=null && !codeNode.value().equals("")) code = codeNode.value();
							jsonTree.auto( "data" ).auto( hash ).auto( basic ).auto( code ).auto( book ).auto( chap ).add( verse );
						}
					}
				}
			}
		}
		
		Stats.displayMemory();

		System.err.println( "Reducing memory..." );
		original = null;
		System.gc();
		Thread.sleep(1000);
		Stats.displayMemory();
		
		
		// Translations
		System.err.println( "*** Translations ***" );
		
		
		// add all languages too the interlinear.js file
		
		/*
		loadLanguage( jsonTree, aliasesTree, rootPath, "English", "eng-kjv2006", "English KJV" );
		loadLanguage( jsonTree, aliasesTree, rootPath, "English", "engwebp", "English WEB" );
		loadLanguage( jsonTree, aliasesTree, rootPath, "Chinese", "cmn-cu89s", "新标点和合本" );
		loadLanguage( jsonTree, aliasesTree, rootPath, "Hindi", "hincv", "सरल हिन्दी बाइबल" );
		loadLanguage( jsonTree, aliasesTree, rootPath, "Spanish", "spablm", "Santa Biblia libre para el mundo" );
		loadLanguage( jsonTree, aliasesTree, rootPath, "French", "frasbl", "Sainte Bible libre pour le monde" );
		loadLanguage( jsonTree, aliasesTree, rootPath, "Arabic-Standard", "arbnav", "كتاب الحياة" );
		loadLanguage( jsonTree, aliasesTree, rootPath, "Bengali", "benirv", "ইন্ডিয়ান রিভাইজড ভার্সন" );
		loadLanguage( jsonTree, aliasesTree, rootPath, "Russian", "russyn", "Синодальный перевод" );
		
		FileActions.write( rootPath+"/interlinear.js", "var interlinear = "+jsonTree.serialize(  )+";\n" );
		FileActions.write( rootPath+"/aliases-auto.js", "var aliases = "+aliasesTree.serialize()+";\n" );
		*/
		
		
		// write main text & data
		
		FileActions.write( rootPath+"/text.js", "interlinear.text = "+jsonTree.get("text").serialize(  )+";\n" ); // contains var declaration
		FileActions.write( rootPath+"/strongs.js", "interlinear.strongs = "+jsonTree.get("strongs").serialize(  )+";\n" );
		FileActions.write( rootPath+"/data.js", "interlinear.data = "+jsonTree.get("data").serialize(  )+";\n" );


		// write each language to a seperate file
		
		writeLanguage( rootPath, "English", "eng-kjv2006", "English KJV" );
		writeLanguage( rootPath, "English", "engwebp", "English WEB" );
		writeLanguage( rootPath, "Chinese", "cmn-cu89s", "新标点和合本" );
		writeLanguage( rootPath, "Hindi", "hincv", "सरल हिन्दी बाइबल" );
		writeLanguage( rootPath, "Spanish", "spablm", "Santa Biblia libre para el mundo" );
		writeLanguage( rootPath, "French", "frasbl", "Sainte Bible libre pour le monde" );
		writeLanguage( rootPath, "Arabic-Standard", "arbnav", "كتاب الحياة" );
		writeLanguage( rootPath, "Bengali", "benirv", "ইন্ডিয়ান রিভাইজড ভার্সন" );
		writeLanguage( rootPath, "Russian", "russyn", "Синодальный перевод" );

	}
}
