package bibletext;

import java.io.*;
import java.util.*;
import creek.*;

public class StrongsSwordProject extends AbstractStrongs {
	
	Alphabet alpha = new Alphabet( Alphabet.WESTERN+" ()," );
	
	public Strongs load ( String path ) throws Exception {
		for (File f : FileActions.recurse(path)) {
			System.out.println( "Loading "+f.getName()+"..." );
			String prefix = "H";
			if (Regex.exists(f.getName(),"greek")) prefix = "G";
			List<String> definitions = Regex.groups(
				FileActions.read( f ),
				"\\$T000(\\d+)[\\s\\d\\\\]+([^\\$]+)"
			);
			int size = definitions.size();
			for (int i=0; i<size; i+=2) {
				String strongs = prefix+definitions.get(i);
				String definition = definitions.get(i+1);
				String replacement = Regex.first( definition, "--([^\\.]+)\\." );
				if (replacement!=null) replacement = alpha.filter( replacement.trim() ).replaceAll( "X ", "" );
				else replacement = definition;
				//replacement( strongs, definition );
				replacement( strongs, replacement );
			}
		}
		return this;
	}
	
	public static void main ( String[] args ) throws Exception {
		Strongs s = new StrongsSwordProject().load( args[0] );
		System.out.println( s.data().get("strongs").serialize() );
		Stats.displayMemory();
	}
	

}
