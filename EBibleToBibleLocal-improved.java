package bibletext;

import java.util.*;
import java.time.*;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.io.*;
import paddle.*;
import creek.*;

public class EBibleToBibleLocal {

	public static String sanitize ( String raw ) {
		return raw.replaceAll("[^abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ]", "_" );
	}
	
	public static String safeLangName ( String rawLangName, String description ) throws Exception {
		List<String> words = Regex.groups( rawLangName, "(\\w+)" );
		
		// try to build hyphenated name from sanitized words
		StringBuilder safeLangName = new StringBuilder();
		String delim = "";
		for (String word : words) {
			safeLangName.append( delim ).append( sanitize( word ) );
			delim = "-";
		}
		String safeLangString = safeLangName.toString();
		
		// check if name starts with underscore and default to first word in description
		if (safeLangString.length()>0 && safeLangString.substring(0,1).equals("_")) safeLangString = sanitize( Regex.first( description, "(\\w+)" ) );
		
		// check if somehow we still have an empty name
		if (safeLangString==null || safeLangString.length()<4) safeLangString = "UNKNOWN";
		
		System.out.println( "Converted name '"+rawLangName+"' to '"+safeLangString+"'" );
		return safeLangString;
	}
	
	public static byte[] eBibleItem ( String path, boolean dataOnly ) throws Exception {
		Stats stats = new Stats();
		OutboundHTTP http = new OutboundHTTP ( "ebible.org", path, 100*1024*1024 ); // max 100 MiB
		while( !http.response().complete() ) Thread.sleep(1);
		byte[] item = null;
		if (dataOnly) item = http.response().data();
		else item = http.inboundMemory();
		double kiBps = ((double)item.length/stats.delta())*10e9/1024;
		System.out.println( "Rate: "+Math.round(kiBps)+" kiB/s" );
		return item;
	}
	
	public boolean upToDate ( String date, File current ) {
		if (!current.exists()) return false;
		FileTime fileTime = Files.getLastModifiedTime( current.toPath() );
		LocalDate sourceDate = LocalDate.parse( date );
		LocalDate convertedFileTime = LocalDate.ofInstant( fileTime.toInstant(), ZoneId.systemDefault() );
		if (sourceDate.compareTo( convertedFileTime ) < 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void itemToDirectory ( String eBiblePath, FilesystemTree ft, String language, String type, String code, String date ) throws Exception {
		System.out.println( "Downloading "+eBiblePath+" to "+language+"/"+type+"/"+code );
		FilesystemTree file = ((FilesystemTree)ft.auto( language ).auto( type ).auto( code ));
		
		if (!type.equals("epub")) {
			if (upToDate( date, new File( file.file(), "signature.txt.asc" ) )) return;
			file.toDirectory();
			ZipActions.toFiles(
				eBibleItem( eBiblePath, true ),
				file.file()
			);
		} else {
			if (upToDate( date, new File( file.file(), "signature.txt.asc" ) )) return;
			FileActions.write(
				file.file(),
				eBibleItem( eBiblePath, true )
			);
		}
	}

	public static void main ( String[] args ) throws Exception {
		
		CSV eBibleCSV = new CSV( new String( eBibleItem( "/Scriptures/translations.csv", true ) ) );
		//System.out.println( eBibleCSV.data().get(0) ); // print first line
		
		LookupTable eBibleLookup = new LookupTable( eBibleCSV );
		Set<String> langCodes = eBibleLookup.colLookup(1).keySet();
		langCodes.remove( "translationId" );
		
		FilesystemTree bibleLocalEBible = new FilesystemTree( args[0] );
		
		for (String code : langCodes) {
		
			// basic info
			String date = eBibleLookup.lookup( 1, code, 0, "sourceDate" );
			String title = eBibleLookup.lookup( 1, code, 0, "title" );
			String name = eBibleLookup.lookup( 1, code, 0, "languageNameInEnglish" );
			String description = eBibleLookup.lookup( 1, code, 0, "description" );
			
			// safe directory name
			String language = safeLangName( name, description );
			
			// download into directory
			itemToDirectory( "/Scriptures/"+code+"_readaloud.zip", bibleLocalEBible, language, "text", code, date );
			itemToDirectory( "/Scriptures/"+code+"_html.zip", bibleLocalEBible, language, "html", code, date );
			itemToDirectory( "/epub/"+code+".epub", bibleLocalEBible, language, "epub", code, date );
		}
	}

}
