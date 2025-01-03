package bibletext;

import java.util.*;
import java.io.*;
import creek.*;
import paddle.*;

public class EBiblePrinter {

	public static String title ( String eBibleHTMLPath ) throws Exception {
		// title from index.htm
		String titleHTML = FileActions.read( eBibleHTMLPath+"/index.htm" );
		String title = Regex.first( titleHTML, "<title>(.*?)</title>" ).trim();
		return title;
	}

	public static String copyrightHTML ( String eBibleHTMLPath ) throws Exception {
		// copyright info from copyright.htm
		String copyrightHTML = FileActions.read( eBibleHTMLPath+"/copyright.htm" );
		System.out.println( copyrightHTML );
		String copyright = Regex.first( copyrightHTML, "</h1>([\\s\\S]*?)<div" );
		return copyright;
	}

	public static String textHTML ( String eBibleTextPath ) throws Exception {
		StringBuilder html = new StringBuilder();
		String currentBook = "";
		
		for (File file : FileActions.recurse( eBibleTextPath )) {
			List<String> ref = Regex.groups( file.getName(), "_([A-Z]\\w+)_(\\d+)_read.txt$" );
			if (ref.size()==2) {
				
				// book (from file name)
				String book = ref.get(0);
				
				// chap (from file name)
				String chap = Regex.first( ref.get(1), "([^0]\\d*)" ); // trim leading zeros
				
				// all lines
				List<String> lines = FileActions.readLines( file, FileActions.UTF8 );
				
				// check for book change
				if (!book.equals(currentBook)) {
					// book name (from first line)
					String fullBookName = lines.get(0).trim();
					//System.out.println( book+","+fullBookName+","+(new Bytes(fullBookName.getBytes( FileActions.UTF16BE )).toString()) );
					fullBookName = fullBookName.replace("\uFEFF", ""); // see https://stackoverflow.com/questions/54247407/why-utf-8-bom-bytes-efbbbf-can-be-replaced-by-ufeff, previously I found EF BB BF at the beginning, which is UTF8-speak for FE FF (UTF-16)			
					System.out.println( fullBookName );
					// complete the previous book
					if (!currentBook.equals("")) html.append( "</tbody>\n</table>" );
					// start new book
					html.append( "<table class='book-table'>\n<thead>\n<tr><th class='book-th'>" ).append( fullBookName ).append( "</th></tr>\n</thead>\n<tbody>\n" );
					currentBook = book;
				}
				
				// remove first two lines (containing book name)
				lines.remove(0);
				lines.remove(0);
				
				// verse lines
				StringBuilder chapterText = new StringBuilder();
				for (int i=0; i<lines.size(); i++) {
					chapterText.append( "<b>" ).append( i+1 ).append( "</b>" ).append( " " ).append( lines.get(i) ).append( " " );
				}
				html.append( "<tr><th>" ).append( chap ).append( "</th></tr><tr><td>" ).append( chapterText ).append( "</td></tr>\n" );
			}
		}
		
		if (!currentBook.equals("")) html.append( "</tbody>\n</table>\n" );
		return html.toString();
	}
	
	public static void main ( String[] args ) throws Exception {
		// args: eBible_path/ template.html output.html
		FileActions.write( args[1],
			EBiblePrinter.textHTML( args[0] )
		);
	}
	
}
