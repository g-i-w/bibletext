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

	private boolean continueOnError;

	private CSV eBibleCSV;
	
	private String rootPath;
	private FileTree rootTree;
	
	private Set<String> langCodes;
	
	private LookupTable translations;
	private Tree translationsData;
	

	// static methods

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
			delim = "_";
		}
		String safeLangString = safeLangName.toString();
		
		// check for something sane and default to first word of description otherwise
		if (safeLangString==null || safeLangString.length()<2 || safeLangString.substring(0,1).equals("_")) {
			safeLangString = sanitize( Regex.first( description, "(\\w+)" ) );
		}

		// final check for something sane
		if (safeLangString==null || safeLangString.length()<2) {
			throw new Exception( "Unable to create a filesystem-safe name from '"+rawLangName+"' or from '"+description );
			//safeLangString = "UNKNOWN";
		}
		
		//System.out.println( "Converted name '"+rawLangName+"' to '"+safeLangString+"'" );
		return safeLangString;
	}
	
	public static byte[] download ( String path, boolean dataOnly ) throws Exception {
		Stats stats = new Stats();
		System.out.println( "Downloading "+path+"..." );
		OutboundHTTP http = new OutboundHTTP ( "ebible.org", path, 8*1024*1024, 100*1024*1024 ); // 8MiB, 100MiB
		while( !http.response().complete() ) Thread.sleep(1);
		byte[] item = null;
		if (dataOnly) item = http.response().data();
		else item = http.inboundMemory();
		//double MiBps = ((double)item.length/stats.delta())*10e9/(1024*1024);
		//System.out.println( "Rate: "+String.format("%.2f", MiBps)+" MiB/s" );
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
	
	public static boolean verifyEBibleSHA256 ( File dir ) throws Exception {
		System.out.print( "Checking SHA256 signatures for "+dir );
		File signature = new File( dir, "signature.txt.asc" );
		if (!signature.exists()) {
			System.out.println( " -> ERROR: MISSING signature.txt.asc -> FAIL" );
			return false;
		}
		List<String> sumsFiles = Regex.groups( FileActions.read( signature ), "([a-zA-Z0-9]{64})\\s+(\\S+\\.[a-z]{3})" );
		if (sumsFiles.size()==0) {
			System.out.println( " -> ERROR: No SHA256 sums found in signature.txt.asc -> FAIL" );
			return false;
		}
		boolean ret = true;
		for (int i=0; i<sumsFiles.size(); i+=2) {
			String recordedSHA256 = sumsFiles.get(i).toLowerCase();
			String fileName = sumsFiles.get(i+1);
			
			if (fileName.substring( fileName.length()-3 ).equals( "wof" )) continue; // ignore many missing ".wof" font files
			
			File file = new File( dir, fileName );
			if (!file.exists()) {
				System.out.print( " -> NOT FOUND: "+fileName );
				ret = false;
				continue;
			}
			byte[] fileData = FileActions.readBytes( file );
			byte[] computedSHA256 = MessageDigest.getInstance( "SHA-256" ).digest( fileData );
			String computedSHA256str = new Bytes( computedSHA256 ).toString().toLowerCase();
			if (!recordedSHA256.equals( computedSHA256str )) {
				System.out.println( " -> ERROR "+fileName+" recorded:"+recordedSHA256+" != computed:"+computedSHA256str );
				ret = false;
			}
		}
		if (ret) System.out.println( " -> PASS" );
		else System.out.println( " -> FAIL" );
		return ret;
	}
	

	// constructor

	public EBibleToBibleLocal () throws Exception {
		this( "ebible.org" ); // default directory name
	}

	public EBibleToBibleLocal ( String rootPath ) throws Exception {
		this( rootPath, true ); // default to ignoring errors
	}

	public EBibleToBibleLocal ( String rootPath, boolean continueOnError ) throws Exception {
	
		this.continueOnError = continueOnError;

		// root tree
		this.rootPath = rootPath;
		rootTree = new FilesystemTree( rootPath );
			
		// translations.csv
		eBibleCSV = new CSV( new String( download( "/Scriptures/translations.csv", true ) ) );
		translations = new LookupTable( eBibleCSV );		
		
		// language code set
		langCodes = translations.colLookup(1).keySet();
		langCodes.remove( "translationId" );
		
		// translation data tree
		translationsData = new JSON( JSON.AUTO_ORDER );
		for (String code : langCodes) {
			if (translations.lookup( 1, code, 0, "downloadable" ).equals("False")) continue;
			// name & description
			String languageNameInEnglish = translations.lookup( 1, code, 0, "languageNameInEnglish" );
			String description = translations.lookup( 1, code, 0, "description" );
			String safeLangName = safeLangName( languageNameInEnglish, description );
			// safeLangName FWD
			translationsData.auto( "code" ).auto( code ).add( "safeLangName" , safeLangName );
			translationsData.auto( "safeLangName" ).auto( safeLangName ).add( code );
			// other data
			translationsData.auto( "code" ).auto( code ).add( "safeTitle" , safeLangName( translations.lookup( 1, code, 0, "shortTitle" ), description ) );
			translationsData.auto( "code" ).auto( code ).add( "languageNameInEnglish" , languageNameInEnglish );
			translationsData.auto( "code" ).auto( code ).add( "description" , description );
			translationsData.auto( "code" ).auto( code ).add( "sourceDate" , translations.lookup( 1, code, 0, "sourceDate" ) );
		}
		
		//System.out.println( translationsData.serialize() );

	}
	
	
	// instance methods
	
	public Tree translationsData () {
		return translationsData;
	}
	
	public void downloadUpdates () throws Exception {
		for (String code : langCodes) {
			// status
			System.out.println( "\n*** Checking "+code+": ("+translationsData.get( "code" ).get( code ).get( "languageNameInEnglish" ).value()+") ***" );	
			// date
			String date = translationsData.get( "code" ).get( code ).get( "sourceDate" ).value();
			// target
			zipToDirectory( "/Scriptures/"+code+"_html.zip", "html", code, date );
			zipToDirectory( "/Scriptures/"+code+"_readaloud.zip", "text", code, date );
			epubToDirectory( "/epub/"+code+".epub", "epub", code, date );
		}
	}
	
	public void zipToDirectory ( String path, String type, String code, String date ) throws Exception {
		FileTree zipDir = targetDir( type, code );
	
		if (!upToDate( (FileTree) zipDir.get("signature.txt.asc"), date )) {
			System.out.println( "Downloading and upacking ZIP..." );
			ZipActions.toFiles(
				zipDir.file(),
				download( path, true ),
				false, // forceOverwrite
				false // verbose
			);
		}
		if (!verifyEBibleSHA256( zipDir.file() ) && !continueOnError) throw new Exception( "FAILED SHA256 CHECK!" );
	}

	public void epubToDirectory ( String path, String type, String code, String date ) throws Exception {
		FileTree epubDir = targetDir( type, code );
		
		String epubName = code+".epub";
	
		if (!upToDate( (FileTree) epubDir.get( epubName ), date )) {
			System.out.println( "Downloading EPUB..." );
			epubDir.write(
				epubName,
				download( path, true )
			);
		}
	}
	
	public FileTree targetDir ( String type, String code ) {
		String safeLangName = null;
		String shortTitle = null;
		
		Tree codeBranch = translationsData.get( "code" ).get( code );
		if (codeBranch!=null) {
			safeLangName = codeBranch.get( "safeLangName" ).value();
			shortTitle = codeBranch.get( "safeTitle" ).value();
		} else {
			safeLangName = "MISSING_LANGUAGE";
			shortTitle = "MISSING_TITLE";
		}
		
		return (FileTree) rootTree.auto( safeLangName ).auto( shortTitle ).auto( type );
	}
	
	public String exportHTML ( String countriesCSV, String regionsCSV, String rootPath, String flagsPath ) throws Exception {
		
		// build links tree
		
		LookupTable countriesLookup = new LookupTable( new CSV( FileActions.read( countriesCSV ) ) );
		LookupTable regionsLookup = new LookupTable( new CSV( FileActions.read( regionsCSV ) ) );
		
		Tree linksTree = new JSON( JSON.AUTO_ORDER );
		
		for (Map.Entry<String,Tree> safeFileNameEntry : translationsData.get( "safeLangName" ).map().entrySet()) {
			for (String code : safeFileNameEntry.getValue().values()) {
			
				String safeFileName = safeFileNameEntry.getKey();
				String language = translations.lookup( 1, code, 0, "languageName" );
				String title = translations.lookup( 1, code, 0, "shortTitle" );
				String country = countriesLookup.colLookup( 1, 0, language );
				
				System.err.println( "code:"+code+" -> language:"+language+" -> country:"+country );
				
				String region = regionsLookup.colLookup( 1, 2, country );
				String flag = regionsLookup.colLookup( 1, 0, country );
				
				linksTree.auto( region ).auto( country ).auto( "translations" ).add( title, code );
				linksTree.auto( region ).auto( country ).add( "flag", flag );
				
			}
		}
		
		System.err.println( linksTree.serialize() );
		
		// build HTML
		
		StringBuilder html = new StringBuilder();
		
		for (Map.Entry<String,Tree> region : linksTree.map().entrySet()) {
			html.append( "<div class=\"content\">\n<h3>"+region.getKey()+"</h3>\n\t<div class=\"scroll-box\">\n\t\t<table class=\"compact-table\">\n" );
			for (Map.Entry<String,Tree> country : region.getValue().map().entrySet()) {
				// flag
				String flag = country.getValue().get("flag").value();
				int qty = country.getValue().get("translations").size();
				File flagFile = new File( new File( flagsPath ), flag );
				flagFile.getParentFile().mkdir();
				if (!flagFile.exists()) {
					System.err.println( "Downloading /flags/"+flag );
					FileActions.write( flagFile, download( "/flags/"+flag, true ) );
				}
				String flagCell = "<td rowspan="+qty+"><img src='"+flagFile.getPath()+"'><br>"+country.getKey()+"</td>";
				// translations
				for (Map.Entry<String,Tree> translationEntry : country.getValue().get("translations").map().entrySet()) {
					String title = translationEntry.getKey();
					String code = translationEntry.getValue().value();
					String htmlPath = targetDir( "html", code ).file().getPath()+"/index.htm";
					String textPath = targetDir( "text", code ).file().getPath()+"/";
					String epubPath = targetDir( "epub", code ).file().getPath()+"/"+code+".epub";
					html.append( "\t\t\t<tr>"+flagCell+"<td><a href='"+htmlPath+"'>"+title+"</a></td><td>&nbsp;<a href='"+epubPath+"'  title='eBook (ePub)'>📖</a>&nbsp;</td><td>&nbsp;<a href='"+textPath+"' title='Text'>📄</a>&nbsp;</td></tr>\n" );
					flagCell = "";
				}
			}
			html.append( "\t\t</table>\n\t</div>\n</div>\n" );
		}
		
		return html.toString();
	}

	// main
	public static void main ( String[] args ) throws Exception {
		EBibleToBibleLocal updateProcess = new EBibleToBibleLocal( args[0], ( args.length>1 ? Boolean.parseBoolean( args[1] ) : true ) );
		updateProcess.downloadUpdates();
	}

}


class EBibleToBiblesHTML {

	public static void main ( String[] args ) throws Exception {
		EBibleToBibleLocal dataProcess = new EBibleToBibleLocal( args[2] );
		System.out.println( dataProcess.exportHTML( args[0], args[1], args[2], args[3] ) );
	}

}

