package bibletext;

import java.io.*;
import java.util.*;
import creek.*;

public class StrongsBSB extends AbstractStrongs {

	public Strongs load ( String path ) throws Exception {
		for (File f : FileActions.recurse(path)) {
			Table table = new CSVFile( f ).table();
			for (List<String> row : table.data()) {
			
				String language = row.get(3).trim();
				String prefix = "";
				if (language.equals("Hebrew")) prefix = "H";
				else if (language.equals("Aramaic")) prefix = "H";
				else if (language.equals("Greek")) prefix = "G";
				else continue;
				
				String strongs = prefix+row.get(10).trim();
				String original = row.get(5).trim();
				String translit = row.get(7).trim();
				String bsb = row.get(14).trim();

				link( strongs, original );
				//replacement( strongs, translit );
			}
		}
		return this;
	}
	
	public static void main ( String[] args ) throws Exception {
		Strongs s = new StrongsBSB().load( args[0] );
		//System.out.println( s.data().get("basic").serialize() );
		Stats.displayMemory();
		System.out.println( s.filtered() );
	}
	
}
