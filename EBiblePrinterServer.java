package bibletext;

import java.util.*;
import creek.*;
import paddle.*;

public class EBiblePrinterServer extends ServerState {
	
	String rootPath;
	TemplateFile printerTemplate;
	TemplateFile titleCopyrightFragment;
	TemplateFile pagedjsTemplate;

	public EBiblePrinterServer ( String rootPath, int port ) {
		this.rootPath = rootPath;
		printerTemplate = new TemplateFile( rootPath+"/printer.html", "////" );
		titleCopyrightFragment = new TemplateFile( rootPath+"/title-copyright-fragment.html", "////" );
		pagedjsTemplate = new TemplateFile( rootPath+"/pagedjs-template.html", "////" );
		new ServerHTTP (
			this,
			port,
			"EBiblePrinterServer",
			1024, // inbound memory size
			20000  // timeout [ms]
		);
	}
	
	private void sendFile ( InboundHTTP session, String mimeType ) {
		String path = session.request().path();
		if (path.indexOf("..") == -1) { // block dir traversal
			try {
				session.response(
					new ResponseHTTP(
						new String[]{ "Content-Type", mimeType },
						FileActions.readBytes( rootPath+path )
					)
				);
			} catch (Exception e) {
				session.response(
					new ResponseHTTP( "404", "Not Found", null, null )
				);
			}
		} else {
			session.response(
				new ResponseHTTP( "403", "Forbidden", null, null )
			);
		}
	}
	
	@Override
	public void received ( Connection c ) {
		InboundHTTP session = http( c );
		
		Map<String,String> query = httpQuery( session );

		String textPath = rootPath+"/biblesd/bibles/ebible.org/"+query.get("lang")+"/text/"+query.get("ver");
		String htmlPath = rootPath+"/biblesd/bibles/ebible.org/"+query.get("lang")+"/html/"+query.get("ver");
		
		String hiddenInputs = 
			"<input type='hidden' name='lang' value='"+query.get("lang")+"'>"+
			"<input type='hidden' name='ver' value='"+query.get("ver")+"'>";
			
		String textLink = "/?type=text&lang="+query.get("lang")+"&ver="+query.get("ver");
		String coverLink = "/?type=cover&lang="+query.get("lang")+"&ver="+query.get("ver");
		
		if (httpQuery( session, "type", "text" )) {
			String title = query.get("ver");
			String text = "";
			try {
				title 		= EBiblePrinter.title( htmlPath );
				text 		= EBiblePrinter.textHTML( textPath );
			} catch (Exception e) {
				e.printStackTrace();
				text = "<h4>Encountered an error:\n"+query+"\n"+e+"</h4>";
			}
			session.response(
				new ResponseHTTP(
					new String[]{ "Content-Type", "text/html" },
					printerTemplate
						.replace( "title", title )
						.replace( "toggle-cover", "Cover Page" )
						.replace( "toggle-cover-link", coverLink )
						.replace( "html", text )
						.replace( "hidden-inputs", hiddenInputs )
						.toString()
				)
			);
			
		} else if (httpQuery( session, "type", "cover" )) {
			String copyright = "";
			String title = query.get("ver");
			try {
				copyright = EBiblePrinter.copyrightHTML( htmlPath );
				title = EBiblePrinter.title( htmlPath );
			} catch (Exception e) {
				e.printStackTrace();
				copyright = "<h4>copyright: "+query+"</h4>";
			}
			session.response(
				new ResponseHTTP(
					new String[]{ "Content-Type", "text/html" },
					printerTemplate
						.replace( "title", title )
						.replace(
							"html",
							titleCopyrightFragment
							.replace( "title", title )
							.replace( "copyright", copyright )
							.toString()
						)
						.replace( "toggle-cover", "Bible Text" )
						.replace( "toggle-cover-link", textLink )
						.replace( "hidden-inputs", hiddenInputs )
						.toString()
				)
			);
			
		} else if (httpQuery( session, "type", "pagedjs" )) {
			String title = query.get("ver");
			String copyright = "";
			String text = "";
			String html = "";
			// get data from files
			try {
				title 		= EBiblePrinter.title( htmlPath );
				copyright 	= EBiblePrinter.copyrightHTML( htmlPath );
				text 		= EBiblePrinter.textHTML( textPath );
			} catch (Exception e) {
				e.printStackTrace();
				text = "<h4>Encountered an error:\n"+query+"\n"+e+"</h4>";
			}
			// add title and copyright pages
			if (httpQuery ( session, "cover" )) html += titleCopyrightFragment
				.replace( "title", title )
				.replace( "copyright", copyright )
				.toString();
			// add Bible text
			if (httpQuery ( session, "text" )) html += text;
			// send response
			System.out.println( query );
			session.response(
				new ResponseHTTP(
					new String[]{ "Content-Type", "text/html" },
					pagedjsTemplate
						.replace( query )
						.replace( "hidden-inputs", hiddenInputs )
						.replace( "title", title )
						.replace( "html", html )
						.toString()
				)
			);
			
		} else if ( session.request().path().substring( 0, 6 ).equals( "/flags" ) ) {
			sendFile( session, "image/png" );
		
		} else if ( session.request().path().substring( 0, 5 ).equals( "/pics" ) ) {
			sendFile( session, "image/jpg" );
		
		} else if ( session.request().path().equals( "/paged.polyfill.js" ) ) {
			sendFile( session, "application/javascript" );
		
		} else {
			session.response(
				new ResponseHTTP( "404", "Not Found", null, null )
			);
		}
	}
	
	public static void main ( String[] args ) throws Exception {
		// args: <template_path> <EBiblePrinter_path> <port>
		new EBiblePrinterServer(
			args[0],
			Integer.valueOf( args[1] )
		);
	}

}
