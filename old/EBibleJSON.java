package bibletext;

import java.util.*;
import java.io.*;
import creek.*;

public class EBibleJSON extends JSON {

	public EBibleJSON ( File f ) throws Exception {
		for (File file : FileActions.recurse(f)) {
			List<String> ref = Regex.groups(file.getName(), "(\\w{3})_(\\d{2})_read.txt$");
			if (ref.size()==2) {
				String book = ref.get(0);
				String chap = ref.get(1);
				//System.out.println( book+","+chap );
				List<String> lines = FileActions.readLines( file );
				for (int i=2; i<lines.size(); i++) {
					String verse = String.valueOf( i-1 ); // verses start at 1 instead of 0
					auto( book ).auto( chap ).add( verse, lines.get(i).trim() );
				}
			}
		}
	}
	
	public String verse ( String refStr ) throws Exception {
		List<String> ref = Regex.groups( refStr, "(\\w{3}).*?(\\d+):(\\d+)" );
		String book = ref.get(0).toUpperCase();
		int chap = Integer.parseInt( ref.get(1) );
		int verse = Integer.parseInt( ref.get(2) );
		return verse( book, chap, verse );
	}
	
	public String verse ( String book, int chap, int verse ) {
		List<String> ref = Arrays.asList( new String[]{
			book,
			String.format("%02d", chap),
			String.valueOf( verse )
		});
		Tree verseNode = get(ref);
		return ( verseNode!=null ? verseNode.value() : null );
	}
	
	public static void main ( String[] args ) throws Exception {
		EBibleJSON ebible = new EBibleJSON( new File( args[0] ) );
		//System.out.println( ebible.serialize() );
		CSV csv = new CSV();
		for (int i=2; i<args.length; i++) {
			csv.append( new String[]{ args[i], ebible.verse( args[i] ) } );
		}
		System.out.println( csv );
		new CSVFile( args[1], false, csv );
	}

}
