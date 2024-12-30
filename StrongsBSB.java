package bibletext;

import java.io.*;
import java.util.*;
import creek.*;

public class StrongsBSB extends AbstractStrongs {

	public Strongs load ( String path ) throws Exception {
		Tree names = new JSON( "{\"1 Chronicles\": \"1CH\",\"1 Corinthians\": \"1CO\",\"1 John\": \"1JN\",\"1 Kings\": \"1KI\",\"1 Peter\": \"1PE\",\"1 Samuel\": \"1SA\",\"1 Thessalonians\": \"1TH\",\"1 Timothy\": \"1TI\",\"2 Chronicles\": \"2CH\",\"2 Corinthians\": \"2CO\",\"2 John\": \"2JN\",\"2 Kings\": \"2KI\",\"2 Peter\": \"2PE\",\"2 Samuel\": \"2SA\",\"2 Thessalonians\": \"2TH\",\"2 Timothy\": \"2TI\",\"3 John\": \"3JN\",\"Acts\": \"ACT\",\"Amos\": \"AMO\",\"Colossians\": \"COL\",\"Daniel\": \"DAN\",\"Deuteronomy\": \"DEU\",\"Ecclesiastes\": \"ECC\",\"Ephesians\": \"EPH\",\"Esther\": \"EST\",\"Exodus\": \"EXO\",\"Ezekiel\": \"EZK\",\"Ezra\": \"EZR\",\"Galatians\": \"GAL\",\"Genesis\": \"GEN\",\"Habakkuk\": \"HAB\",\"Haggai\": \"HAG\",\"Hebrews\": \"HEB\",\"Hosea\": \"HOS\",\"Isaiah\": \"ISA\",\"James\": \"JAS\",\"Judges\": \"JDG\",\"Jeremiah\": \"JER\",\"John\": \"JHN\",\"Job\": \"JOB\",\"Joel\": \"JOL\",\"Jonah\": \"JON\",\"Joshua\": \"JOS\",\"Jude\": \"JUD\",\"Lamentations\": \"LAM\",\"Leviticus\": \"LEV\",\"Luke\": \"LUK\",\"Malachi\": \"MAL\",\"Matthew\": \"MAT\",\"Micah\": \"MIC\",\"Mark\": \"MRK\",\"Nahum\": \"NAM\",\"Nehemiah\": \"NEH\",\"Numbers\": \"NUM\",\"Obadiah\": \"OBA\",\"Philemon\": \"PHM\",\"Philippians\": \"PHP\",\"Proverbs\": \"PRO\",\"Psalm\": \"PSA\",\"Revelation\": \"REV\",\"Romans\": \"ROM\",\"Ruth\": \"RUT\",\"Song of Solomon\": \"SNG\",\"Titus\": \"TIT\",\"Zechariah\": \"ZEC\",\"Zephaniah\": \"ZEP\"}" );

		for (File f : FileActions.recurse(path)) {
			Table table = new CSVFile( f ).table();
			
			List<String> reference = new ArrayList<>();
			for (List<String> row : table.data()) {
			
				String language = row.get(3).trim();
				String prefix = "";
				if (language.equals("Hebrew")) prefix = "H";
				else if (language.equals("Aramaic")) prefix = "H";
				else if (language.equals("Greek")) prefix = "G";
				else continue;
				
				if (!row.get(11).equals("")) reference = Regex.groups( row.get(11).trim(), "(.+?)\\s+(\\d+):(\\d+)" );
				String book = null;
				String chap = null;
				String verse = null;
				if (reference.size()==3) {
					Tree bookName = names.get( reference.get(0) );
					if (bookName==null) System.out.println( "can't find name: "+reference.get(0) );
					book = bookName.value(); // allow null pointer exceptions to happen
					chap = reference.get(1);
					verse = reference.get(2);
					//System.out.print( book+chap+":"+verse+"," );
				} else {
					System.out.println( "error parsing "+row.get(11) );
				}
				
				String strongs = prefix+row.get(10).trim();
				String original = row.get(5).trim();
				String translit = row.get(7).trim();
				String bsb = row.get(14).trim();

				link( strongs, original, book, chap, verse );
				//replacement( strongs, translit );
			}
		}
		return this;
	}
	
	public static void main ( String[] args ) throws Exception {
		//Strongs s = new StrongsBSB().load( args[0] );
		Strongs bsb = new StrongsBSB().load( args[0]+"/biblelookup/openbible.org/csv/" );
		Strongs strongs = new StrongsSwordProject().data( bsb ).load( args[0]+"/biblelookup/SwordProject/" );
		//System.out.println( s.data().get("basic").serialize() );
		Stats.displayMemory();
		//System.out.println( strongs.filtered() );
		System.out.println( strongs.data().get("lookup").serialize() );
	}
	
}
