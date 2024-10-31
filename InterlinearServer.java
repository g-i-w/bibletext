package bibletext;

import java.util.*;
import java.io.*;
import creek.*;
import paddle.*;

public class InterlinearServer extends ServerState {

	Tree interlinearTree;
	Alphabet alphabet;
	
	String htmlStart =
		"<!DOCTYPE html><html><head>"+
		"<title>Bibles</title>"+
		"<meta charset='utf-8' />"+
		"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />"+
		"</head><body>";
		
	String htmlEnd = "</body></html>";


	public InterlinearServer ( String path, int port ) throws Exception {
		this( path, port, false );
	}

	public InterlinearServer ( String path, int port, boolean debug ) throws Exception {
		interlinearTree = new DataFileTree( new File(path), debug );
		alphabet = new Alphabet();
		new ServerHTTP (
			this,
			port,
			"InterlinearServer",
			1024, // inbound memory size
			4000  // timeout [ms]
		);
	}
	
	
	private String mergeKeys ( Set<String> keys ) {
		StringBuilder output = new StringBuilder();
		String delim = "";
		for (String key : keys) {
			output.append( delim ).append( key );
			delim = ",";
		}
		return output.toString();
	}
	
	private Tree verseDiagram ( String book, String chap, String verse ) throws Exception {
		String verseText = interlinearTree.get( "original" ).get( "text" ).get( book ).get( chap ).get( verse ).value();
		Tree strongs = interlinearTree.get( "strongs" );
		Tree output = new JSON( JSON.RETAIN_ORDER );
		List<String> wordsRaw = Regex.groups( verseText, "(\\S+)" );
		for (String raw : wordsRaw) {
			String basic = alphabet.filter( raw );
			String wordHash = alphabet.wordHash( basic );
			try {
				Tree codes = strongs.get( "basicToCode" ).get( wordHash ).get( basic );
				for (String code : codes.keys()) {
					String codeHash = alphabet.strongsHash( code );
					String replacements = mergeKeys( strongs.get( "codeToReplacement" ).get( codeHash ).get( code ).keys() );
					output.auto( raw ).auto( code ).value( replacements );
				}
			} catch (Exception e) {
				output.auto( raw ).auto( "" ).value( "" );
			}
		}
		return output;
	}
	
	private Tree translations ( String book, String chap, String verse ) throws Exception {
		Tree input = interlinearTree.get( "translations" );
		Tree output = new JSON( JSON.RETAIN_ORDER );
		for (String key : input.keys()) {
			output.auto( book ).auto( chap ).auto( verse ).add( key,
				input.get( key ).get( book ).get( chap ).get( verse )
			);
		}
		return output;
	}
	
	private Tree whereUsed ( String raw ) throws Exception {
		Tree input = interlinearTree.get( "original" ).get( "data" );
		Tree output = new JSON( JSON.RETAIN_ORDER );
		String basic = alphabet.filter( raw );
		String wordHash = alphabet.wordHash( basic );
		output.map( input.get( wordHash ).get( basic ).map() );
		return output;
	}
	
	String htmlTree ( Tree tree ) {
		return 
		htmlStart+
		Tables.html(
			new SimpleTable().data(
				tree.paths()
			)
		)+
		htmlEnd;
	}
	
	@Override
	public void received ( Connection c ) {
		InboundHTTP session = http( c );
		
		Map<String,String> query = httpQueryFields( session, new String[]{ "book", "chap", "verse", "word" } );
		
		System.out.println( session.request().path() );
		System.out.println( query );
		
		String html = null;
		try {
		
			if (httpPathBegins( session, "/query" )) {
				if (httpQuery( session, "type", "diagram" )) {
					httpRespondHTML( session,
						htmlTree( verseDiagram( query.get("book"), query.get("chap"), query.get("verse") ) )
					);
					
				} else if (httpQuery( session, "type", "translations" )) {
					httpRespondHTML( session,
						htmlTree( translations( query.get("book"), query.get("chap"), query.get("verse") ) )
					);

				} else if (httpQuery( session, "type", "where-used" )) {
					httpRespondHTML( session,
						htmlTree( whereUsed( query.get("word") ) )
					);
					
				} else {
					httpRespondBadRequest( session );
				}
			} else {
				httpRespondHTML( session, interlinearTree.get( "index" ).value() );
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			httpRespondNotFound( session );
		}
	}
	
	public static void main ( String[] args ) throws Exception {
		new InterlinearServer(
			args[0],
			7077,
			( args.length > 1 ? Boolean.valueOf( args[1] ) : false )
		);
	}

}
