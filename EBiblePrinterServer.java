package bibletext;

import java.util.*;
import creek.*;
import paddle.*;

public class EBiblePrinterServer extends ServerState {
	
	EBiblePrinter printer;
	String rootPath;
	String eBiblePath;

	public EBiblePrinterServer ( String rootPath, String eBiblePath, int port ) {
		this.rootPath = rootPath;
		this.eBiblePath = eBiblePath;
		printer = new EBiblePrinter( rootPath+"/printer.html" );
		new ServerHTTP (
			this,
			port,
			"EBiblePrinterServer",
			1024, // inbound memory size
			4000  // timeout [ms]
		);
	}
	
	@Override
	public void received ( Connection c ) {
		InboundHTTP session = http( c );
		
		Map<String,String> query = httpQueryFields( session, new String[]{ "lang", "ver", "type" } );
		String lang = query.get("lang");
		String ver = query.get("ver");
		
		String path = session.request().path();
		
		String html = "";
		
		if (httpQuery( session, "type", "text" )) {
			try {
				html = printer.textToHTML( eBiblePath+"/"+lang+"/text/"+ver );
			} catch (Exception e) {
				e.printStackTrace();
				html = "<h3>text: "+query+"</h3>";
			}
			session.response(
				new ResponseHTTP(
					new String[]{ "Content-Type", "text/html" },
					html
				)
			);
			
		} else if (httpQuery( session, "type", "cover" )) {
			try {
				html = printer.coverHTML( eBiblePath+"/"+lang+"/html/"+ver );
			} catch (Exception e) {
				e.printStackTrace();
				html = "<h3>cover: "+query+"</h3>";
			}
			session.response(
				new ResponseHTTP(
					new String[]{ "Content-Type", "text/html" },
					html
				)
			);
			
		} else if ( path.substring( 0, 6 ).equals( "/flags" ) ) {
			if (path.indexOf("..") == -1) { // block dir traversal
				try {
					session.response(
						new ResponseHTTP(
							new String[]{ "Content-Type", "image/png" },
							FileActions.readBytes( rootPath+path )
						)
					);
				} catch (Exception e) {
					session.response(
						new ResponseHTTP( "403", "Forbidden", null, null )
					);
				}
			}
		
		} else {
			session.response(
				new ResponseHTTP( "404", "Not Found", null, null )
			);
		}
	}
	
	public static void main ( String[] args ) throws Exception {
		// args: <template_path> <eBible_path> <port>
		new EBiblePrinterServer(
			args[0],
			args[1],
			Integer.valueOf( args[2] )
		);
	}

}
