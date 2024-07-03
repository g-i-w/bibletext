package bibletext;

import java.io.*;
import java.util.*;
import creek.*;

public class STEPBibleData {

	public static void links ( Strongs strongs, String path ) throws Exception {
		Alphabet alpha = new Alphabet();
		for (File file : FileActions.recurse(path)) {
			Table table = new CSV( FileActions.read(file), "\t" );
			for (int rowNum=0; rowNum<table.rowCount(); rowNum++) {
				List<String> row = table.data().get(rowNum);
				List<String> reference = Regex.groups( row.get(0), "^(\\w+)\\.(\\d+)\\.(\\d+)" );
				if (reference.size()==3) {
					String book = reference.get(0);
					String chap = reference.get(1);
					String verse = reference.get(2);
				
					//System.out.println( row.get(11) );
					if (Regex.exists( row.get(11), "\\{[HA]\\d+" )) {
						List<String> groups = Regex.groups( row.get(11), "\\{([HA]\\d+).*?=([^=]+)=([\\w\\s]*)" );
						if (groups.size()>=3) {
							//System.out.println( groups );
							String code = groups.get(0);
							String original = groups.get(1);
							String replacement = groups.get(2);

							if (original.length()>0) strongs.link( code, original, book, chap, verse, replacement );
							//if (replacement.length()>0) strongs.replacement( code, replacement );
						} else {
							System.out.println( "Couldn't parse: "+row.get(11) );
							throw new RuntimeException( "Stopped at line "+rowNum+" in "+file+" -> "+groups );
						}
					} else {
						String code = Regex.first( row.get(3), "(G\\d+)" );
						String original = Regex.first( row.get(1), "(\\S+)" );
						String replacement = Regex.first( row.get(4), "=([\\/\\w\\s]+)" );
						//System.out.println( row );
						//String filtered = alpha.filter(original);
						//System.out.println( code+", "+original+"/"+filtered+", "+replacement );

						if (original!=null) {
							if (original.length()>0) strongs.link( code, original, book, chap, verse, replacement );
							//if (replacement.length()>0) strongs.replacement( code, replacement );
						}
					}
				}
			}
		}
	}
	
	public static void main ( String[] args ) throws Exception {
		Strongs s = new Strongs();
		STEPBibleData.links( s, args[0] );
		System.out.println( s.data().serialize() );
	}
	
}
