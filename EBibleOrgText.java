package bibletext;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.*;
import creek.*;
import paddle.*;

public class EBibleOrgText {

	public static void text ( Bible bible, String path ) throws Exception {
		for (File file : FileActions.recurse(path)) {
			List<String> ref = Regex.groups( file.getName(), "(\\w{3})_(\\d{2})_read.txt$" );
			if (ref.size()==2) {
				String book = ref.get(0);
				String chap = Regex.first( ref.get(1), "([^0]\\d*)" ); // trim leading zeros
				List<String> lines = FileActions.readLines( file );
				String fullBookName = Regex.first( lines.get(0), "(\\S+).$" );
				fullBookName = fullBookName.replace("\uFEFF", ""); // see https://stackoverflow.com/questions/54247407/why-utf-8-bom-bytes-efbbbf-can-be-replaced-by-ufeff, previously I found EF BB BF at the beginning, which is UTF8-speak for FE FF (UTF-16)
				bible.alias( fullBookName, book );
				lines.remove(0);
				lines.remove(0); // remove first two lines
				bible.importChapter( book, chap, lines );
			}
		}
	}
	
	public static void main ( String[] args ) throws Exception {
		Bible bible = new Bible();
		EBibleOrgText.text( bible, args[0] );
		
		BibleSearch search = new BibleSearch( bible, 2 );
		
		System.out.println( bible.wordData().serialize() );
		System.out.println( bible.aliases() );
		
		for (int i=1; i<args.length; i++) {
			System.out.println( "\n"+args[i]+":\n"+search.search( args[i] ).serialize() ); // BibleSearch.search
			//System.out.println( "\n"+args[i]+":\n"+bible.verse( args[i] ) ); // Bible.verse
		}
	}

}

