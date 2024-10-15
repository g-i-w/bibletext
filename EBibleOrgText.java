package bibletext;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import creek.*;
import paddle.*;

public class EBibleOrgText extends AbstractBible {

	public Bible load ( String path ) throws Exception {
		for (File file : FileActions.recurse(path)) {
			List<String> ref = Regex.groups( file.getName(), "(\\w{3})_(\\d{2})_read.txt$" );
			if (ref.size()==2) {
				String book = ref.get(0);
				String chap = Regex.first( ref.get(1), "([^0]\\d*)" ); // trim leading zeros
				List<String> lines = FileActions.readLines( file, FileActions.UTF8 );
				String fullBookName = lines.get(0).trim();
				//System.out.println( book+","+fullBookName+","+(new Bytes(fullBookName.getBytes( FileActions.UTF16BE )).toString()) );
				// no longer needed because of basic( ): //fullBookName = fullBookName.replace("\uFEFF", ""); // see https://stackoverflow.com/questions/54247407/why-utf-8-bom-bytes-efbbbf-can-be-replaced-by-ufeff, previously I found EF BB BF at the beginning, which is UTF8-speak for FE FF (UTF-16)
				aliases().put( fullBookName, book );
				lines.remove(0);
				lines.remove(0); // remove first two lines
				importChapter( book, chap, lines, "(\\S+)" );
			}
		}
		return this;
	}
	
	public static void main ( String[] args ) throws Exception {
		Bible eBible = new EBibleOrgText().load( args[0] );
		
		System.out.println( eBible.words().serialize() );
		System.out.println( eBible.aliases() );
		
		if (args.length>1) System.out.println( "\n"+args[1]+":\n"+eBible.verse( args[1] ) );              // verse( )
		if (args.length>2) System.out.println( "\n"+args[2]+":\n"+eBible.search( args[2] ).serialize() ); // search( )
		
	}

}

