package bibletext;

import java.util.*;
import java.time.*;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.io.*;
import java.security.MessageDigest;
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
		if (safeLangString==null || safeLangString.length()<2) safeLangString = "UNKNOWN";
		
		//System.out.println( "Converted name '"+rawLangName+"' to '"+safeLangString+"'" );
		return safeLangString;
	}
	
	public static byte[] eBibleItem ( String path, boolean dataOnly ) throws Exception {
		Stats stats = new Stats();
		System.out.println( "Downloading "+path+"..." );
		OutboundHTTP http = new OutboundHTTP ( "ebible.org", path, 8*1024*1024, 100*1024*1024 ); // 8MiB, 100MiB
		while( !http.response().complete() ) Thread.sleep(1);
		byte[] item = null;
		if (dataOnly) item = http.response().data();
		else item = http.inboundMemory();
		double MiBps = ((double)item.length/stats.delta())*10e9/(1024*1024);
		System.out.println( "Rate: "+String.format("%.2f", MiBps)+" MiB/s" );
		return item;
	}
	
	public static boolean upToDate ( FileTree test, String date ) throws Exception {
		System.out.print( "Source: "+date );
		if (test==null) {
			System.out.println( " -> NEW" );
			return false;
		}
		LocalDate testDate = test.localDate();
		System.out.print( " -> Existing: "+testDate );
		LocalDate downloadDate = LocalDate.parse( date );
		if (testDate.compareTo( downloadDate ) >= 0) {
			System.out.println( " -> OK" );
			return true; // existing is newer or equal to download
		} else {
			System.out.println( " -> UPDATE" );
			return false;
		}
	}
	
	public static void zipToDirectory ( String eBiblePath, FileTree ft, String language, String type, String code, String date ) throws Exception {
		System.out.println( "ZIP: "+eBiblePath+" -> "+language+"/"+type+"/"+code );
		
		FileTree zipDir = (FileTree) ft.auto( language ).auto( type ).auto( code );
		
		if (! (upToDate( (FileTree) zipDir.get("signature.txt.asc"), date ) || upToDate( (FileTree) zipDir.get("keys.asc"), date )) ) {
			System.out.println( "Downloading and upacking ZIP..." );
			ZipActions.toFiles(
				zipDir.file(),
				eBibleItem( eBiblePath, true ),
				false, // forceOverwrite
				false // verbose
			);
		}
		verifySHA256( zipDir.file() );
	}

	public static void epubToDirectory ( String eBiblePath, FileTree ft, String language, String type, String code, String date ) throws Exception {
		System.out.println( "EPUB: "+eBiblePath+" -> "+language+"/"+type+"/"+code );
		
		FileTree epubDir = (FileTree) ft.auto( language ).auto( type ).auto( code );
		String epubName = code+".epub";
		
		if (!upToDate( (FileTree) epubDir.get(epubName), date )) {
			System.out.println( "Downloading EPUB..." );
			epubDir.write(
				epubName,
				eBibleItem( eBiblePath, true )
			);
		}
	}
	
	public static boolean verifySHA256 ( File dir ) throws Exception {
		System.out.print( "Checking SHA256 sums" );
		File signature = new File( dir, "signature.txt.asc" );
		if (!signature.exists()) {
			System.out.println( " -> MISSING signature.txt.asc" );
			return false;
		}
		List<String> sumsFiles = Regex.groups( FileActions.read( signature ), "([a-zA-Z0-9]{64})\\s+(\\S+\\.[a-z]{3})" );
		if (sumsFiles.size()==0) {
			System.out.println( " -> No SHA256 sums found in signature.txt.asc" );
			return false;
		}
		for (int i=0; i<sumsFiles.size(); i+=2) {
			String recordedSHA256 = sumsFiles.get(i).toLowerCase();
			String fileName = sumsFiles.get(i+1);
			File file = new File( dir, fileName );
			if (!file.exists()) {
				System.out.print( " -> NOT FOUND: "+fileName );
				continue;
			}
			byte[] fileData = FileActions.readBytes( file );
			byte[] computedSHA256 = MessageDigest.getInstance( "SHA-256" ).digest( fileData );
			String computedSHA256str = new Bytes( computedSHA256 ).toString().toLowerCase();
			if (!recordedSHA256.equals( computedSHA256str )) {
				System.out.println( fileName+": recorded "+recordedSHA256+" != computed "+computedSHA256str+" -> FAILED" );
			}
		}
		System.out.println( " -> GOOD" );
		return true;
	}

	public static void main ( String[] args ) throws Exception {
		
		CSV eBibleCSV = new CSV( new String( eBibleItem( "/Scriptures/translations.csv", true ) ) );
		//System.out.println( eBibleCSV.data().get(0) ); // print first line
		
		LookupTable eBibleLookup = new LookupTable( eBibleCSV );
		Set<String> langCodes = eBibleLookup.colLookup(1).keySet();
		langCodes.remove( "translationId" );
		
		FileTree bibleLocalEBible = new FilesystemTree( args[0] );
		
		Set<String> skipSet = new HashSet<>();
		
		for (int i=1; i<args.length; i++) {
			skipSet.add( args[i] );
		}
		
		for (String code : langCodes) {
		
			if (skipSet.contains(code)) continue;
		
			// basic info
			String date = eBibleLookup.lookup( 1, code, 0, "sourceDate" );
			String title = eBibleLookup.lookup( 1, code, 0, "title" );
			String name = eBibleLookup.lookup( 1, code, 0, "languageNameInEnglish" );
			String description = eBibleLookup.lookup( 1, code, 0, "description" );
			
			// safe directory name
			String language = safeLangName( name, description );
			System.out.println( "\n*** Checking "+code+": ("+language+") ***" );
			
			// download into directory
			zipToDirectory( "/Scriptures/"+code+"_html.zip", bibleLocalEBible, language, "html", code, date );
			zipToDirectory( "/Scriptures/"+code+"_readaloud.zip", bibleLocalEBible, language, "text", code, date );
			epubToDirectory( "/epub/"+code+".epub", bibleLocalEBible, language, "epub", code, date );
		}
	}

}
