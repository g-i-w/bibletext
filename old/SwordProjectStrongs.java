package bibletext;

import java.io.*;
import java.util.*;
import creek.*;

public class SwordProjectStrongs {

	public static void definitions ( Strongs strongs, String path, String prefix ) throws Exception {
		int counter = 0;
		String number = "";
		StringBuilder definition = null;
		String all = FileActions.read( path );
		List<String> definitions = Regex.groups( all, "\\$T000(\\d+)[\\s\\d\\\\]+([^\\$]+)" );
		int size = definitions.size();
		for (int i=0; i<size; i+=2) {
			strongs.definition( prefix+definitions.get(i), definitions.get(i+1) );
		}
	}
	
	public static void main ( String[] args ) throws Exception {
		Strongs s = new Strongs();
		SwordProjectStrongs.definitions( s, args[0], args[1] );
		System.out.println( s.data().serialize() );
	}

}
