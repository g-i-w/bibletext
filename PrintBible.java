package bibletext;

import java.util.*;
import creek.*;
import paddle.*;

public class PrintBible {

	Bible bible;
	Map<String,String> bookNames;
	TemplateFile htmlFile;

	public PrintBible ( Bible bible, String htmlFilePath ) {
		this.bible = bible;
		bookNames = new HashMap<>();
		for (String name : bible.aliases().keySet()) {
			String abrev = bible.aliases().get(name);
			if (!bookNames.containsKey(abrev)) bookNames.put( abrev, name ); // if abrev isn't there yet, put it there
			else if (name.length() > bookNames.get(abrev).length()) bookNames.put( abrev, name ); // else if it's the longer name, then use it instead
		}
		htmlFile = new TemplateFile( htmlFilePath, "////" );
	}
	
	public String html ( String title, String min, String holePunch ) {
		StringBuilder html = new StringBuilder();
		Tree books = bible.text();
		for (String book : books.keys()) {
			html.append( "<table>\n<thead>\n<tr><th class='book'>" ).append( bookNames.get(book) ).append( "</th></tr>\n</thead>\n<tbody>\n" );
			Tree chaps = books.get(book);
			for (String chap : chaps.keys()) {
				StringBuilder chapterText = new StringBuilder();
				Tree verses = chaps.get(chap);
				for (String verse : verses.keys()) {
					String text = verses.get(verse).value();
					chapterText.append( "<b>" ).append( verse ).append( "</b>" ).append( " " ).append( text ).append( " " );
				}
				html.append( "<tr><th>" ).append( chap ).append( "</th></tr><tr><td>" ).append( chapterText ).append( "</td></tr>\n" );
			}
			html.append( "</tbody>\n</table>" );
		}
		htmlFile
			.replace( "title", title )
			.replace( "html", html.toString() )
		;
		return htmlFile.toString();
	}
	
	public void printHtml ( String path ) throws Exception {
		FileActions.write( path, html( FileActions.minName( path ), "0.4cm", "1.5cm" ) );
	}
	
	public static void main ( String[] args ) throws Exception {
		new PrintBible( new EBibleOrgText().load( args[0] ), args[2] ).printHtml( args[1] );
	}
	
}
