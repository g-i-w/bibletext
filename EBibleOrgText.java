package bibletext;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import creek.*;
import paddle.*;

public class EBibleOrgText extends AbstractBible {

	Tree bookNames;

	public Bible load ( String path ) throws Exception {
		bookNames = new JSON( "{\"GEN\":\"null\",\"EXO\":\"null\",\"LEV\":\"null\",\"NUM\":\"null\",\"DEU\":\"null\",\"JOS\":\"null\",\"JDG\":\"null\",\"RUT\":\"null\",\"1SA\":\"null\",\"2SA\":\"null\",\"1KI\":\"null\",\"2KI\":\"null\",\"1CH\":\"null\",\"2CH\":\"null\",\"EZR\":\"null\",\"NEH\":\"null\",\"EST\":\"null\",\"JOB\":\"null\",\"PSA\":\"null\",\"PRO\":\"null\",\"ECC\":\"null\",\"SNG\":\"null\",\"ISA\":\"null\",\"JER\":\"null\",\"LAM\":\"null\",\"EZK\":\"null\",\"DAN\":\"null\",\"HOS\":\"null\",\"JOL\":\"null\",\"AMO\":\"null\",\"OBA\":\"null\",\"JON\":\"null\",\"MIC\":\"null\",\"NAM\":\"null\",\"HAB\":\"null\",\"ZEP\":\"null\",\"HAG\":\"null\",\"ZEC\":\"null\",\"MAL\":\"null\",\"MAT\":\"null\",\"MRK\":\"null\",\"LUK\":\"null\",\"JHN\":\"null\",\"ACT\":\"null\",\"ROM\":\"null\",\"1CO\":\"null\",\"2CO\":\"null\",\"GAL\":\"null\",\"EPH\":\"null\",\"PHP\":\"null\",\"COL\":\"null\",\"1TH\":\"null\",\"2TH\":\"null\",\"1TI\":\"null\",\"2TI\":\"null\",\"TIT\":\"null\",\"PHM\":\"null\",\"HEB\":\"null\",\"JAS\":\"null\",\"1PE\":\"null\",\"2PE\":\"null\",\"1JN\":\"null\",\"2JN\":\"null\",\"3JN\":\"null\",\"JUD\":\"null\",\"REV\":\"null\"}" );
		
		for (File file : FileActions.recurse(path)) {
			List<String> ref = Regex.groups( file.getName(), "(\\w{3})_(\\d{2,3})_read" );
			if (ref.size()==2 && ref.get(1).equals("000")) continue;
			if (ref.size()==2) {
				String book = ref.get(0);
				if (!bookNames.keys().contains(book)) continue; // skip any books not already in bookNames
				String chap = Regex.first( ref.get(1), "([^0]\\d*)" ); // trim leading zeros
				List<String> lines = FileActions.readLines( file, FileActions.UTF8 );
				String fullBookName = lines.get(0).trim();
				fullBookName = fullBookName.replace("\uFEFF", ""); // see https://stackoverflow.com/questions/54247407/why-utf-8-bom-bytes-efbbbf-can-be-replaced-by-ufeff, previously I found EF BB BF at the beginning, which is UTF8-speak for FE FF (UTF-16)
				//System.out.println( fullBookName );
				//List<String> nameList = Regex.groups( fullBookName, "([^ \\.]+)" );
				//String bookName = nameList.get( nameList.size()-1 );
				//System.out.println( bookName );
				//System.out.println( book+","+fullBookName+","+(new Bytes(fullBookName.getBytes( FileActions.UTF16BE )).toString()) );
				bookNames.add( book, fullBookName );
				//aliases().put( bookName, book );
				lines.remove(0);
				lines.remove(0); // remove first two lines
				importChapter( book, chap, lines, "(\\S+)" );
			}
		}
		for (String book : bookNames.keys()) {
			String bookName = bookNames.get(book).value();
			if (!aliases().containsKey(bookName)) aliases().put( bookName, book );
			else aliases().put( bookName+"-"+book, book );
		}
		return this;
	}
	
	public Tree names () {
		return bookNames;
	}
	
	public static void main ( String[] args ) throws Exception {
		Bible eBible = new EBibleOrgText().load( args[0] );
		
		System.out.println( eBible.words().serialize() );
		System.out.println( eBible.aliases() );
		
		if (args.length>1) System.out.println( "\n"+args[1]+":\n"+eBible.verse( args[1] ) );              // verse( )
		if (args.length>2) System.out.println( "\n"+args[2]+":\n"+eBible.search( args[2] ).serialize() ); // search( )
		
	}

}

