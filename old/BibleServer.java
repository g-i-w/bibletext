package bibletext;

import java.util.*;
import creek.*;
import paddle.*;

public class BibleServer extends ServerState {

	ImportInterlinear interlinear;
	
	String htmlStart =
		"<!DOCTYPE html><html><head>"+
		"<title>Bibles</title>"+
		"<meta charset='utf-8' />"+
		"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />"+
		"</head><body>";
		
	String htmlEnd = "</body></html>";


	public BibleServer ( int port, ImportInterlinear interlinear ) {
		this.interlinear = interlinear;
		new ServerHTTP (
			this,
			port,
			"BibleServer",
			1024, // inbound memory size
			4000  // timeout [ms]
		);
	}
	
	@Override
	public void received ( Connection c ) {
		InboundHTTP session = http( c );
		
		Map<String,String> query = httpQueryFields( session, new String[]{ "book", "chap", "verse", "word" } );
		String book = query.get("book");
		String chap = query.get("chap");
		String verse = query.get("verse");
		String word = query.get("word");
		String html = "";
		
		if (httpQuery( session, "type", "interlinear" )) {
			try {
				html += Tables.html( interlinear.translations( book, chap, verse ) );
				html += "<br>";
				html += Tables.html( interlinear.data( book, chap, verse ) );
			} catch (Exception e) {
				e.printStackTrace();
				html = "<h3>interlinear: "+query+"</h3>";
			}
			session.response(
				new ResponseHTTP(
					new String[]{ "Content-Type", "text/html" },
					htmlStart+html+htmlEnd
				)
			);
			
		} else if (httpQuery( session, "type", "where-used" )) {
			try {
				html = Tables.html( interlinear.lookup( word ) );
			} catch (Exception e) {
				e.printStackTrace();
				html = "<h3>where-used: "+query+"</h3>";
			}
			session.response(
				new ResponseHTTP(
					new String[]{ "Content-Type", "text/html" },
					htmlStart + html + htmlEnd
				)
			);
		}
	}
	
	public static void main ( String[] args ) throws Exception {
		new BibleServer(
			7077,
			new ImportInterlinear( args[0] )
		);
	}

}
