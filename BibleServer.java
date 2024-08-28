package bibletext;

import java.util.*;
import creek.*;
import paddle.*;

public class BibleServer extends ServerState {

	Interlinear interlinear;
	List<Bible> parallel;
	String htmlStart =
		"<!DOCTYPE html><html><head>"+
		"<title>Bibles</title>"+
		"<meta charset='utf-8' />"+
		"<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />"+
		"</head><body>";
	String htmlEnd = "</body></html>";


	public BibleServer ( int port, Interlinear interlinear, List<Bible> parallel ) {
		this.interlinear = interlinear;
		this.parallel = parallel;
		new ServerHTTP (
			this,
			port,
			"BibleServer",
			1024, // memory size
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
		String html;
		
		if (httpQuery( session, "type", "interlinear" )) {
			try {
				html = "<table><tr>";
				for (Bible bible : parallel) {
					html += "<td>"+bible.verse( book, chap, verse )+"</td>";
				}
				html += "</tr></table><br>";
				html += Tables.html(
					interlinear.verse( book, chap, verse )
				);
			} catch (Exception e) {
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
				String basicWord = interlinear.bible().compressed().get("words").get( word ).value();
				html = Tables.html(
					new SimpleTable().data(
						interlinear.bible().lookup().get( basicWord ).paths()
					)
				);
			} catch (Exception e) {
				html = "<h3>where-used: "+query+"</h3>";
			}
			session.response(
				new ResponseHTTP(
					new String[]{ "Content-Type", "text/html" },
					htmlStart+html+htmlEnd
				)
			);
		}
	}
	
	public static void main ( String[] args ) throws Exception {
		List<Bible> bibles = new ArrayList<>();
		for (int arg=3; arg<args.length; arg++) {
			bibles.add( new EBibleOrgText().load( args[arg] ) );
			Stats.displayMemory();
		}
		new BibleServer(
			7077,
			new Interlinear(
				args[0], // Hebrew path
				args[1], // Greek path
				args[2]  // Strongs path (bridge to other languages, like English)
			),
			bibles
		);
	}

}
