package bibletext;

import java.io.*;
import java.util.*;
import creek.*;

public class STEPBibleData extends AbstractStrongs {

	Map<String,String> unparsables = new LinkedHashMap<>();

	public Strongs load ( String path ) throws Exception {
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
						List<String> groups = Regex.groups( row.get(11), "\\{([HA]\\d+).*?=([^= ,\\[\\]\\+\\.\\/:@_\\}»]+)=([\\w\\s]*)" );
						//if (groups.size()!=3) groups = Regex.groups( row.get(11), "\\{([HA]\\d+).*?=([^=]+)=([\\w\\s]*)" );
						if (groups.size()>=3) {
							//System.out.println( groups );
							String code = groups.get(0).trim();
							String original = groups.get(1).trim();
							String replacement = groups.get(2).trim();

							if (original.length()>0) {
								link( code, original );
								lookup( code, original, book, chap, verse );
							}
							if (replacement.length()>0) replacement( code, "english", replacement );
						} else {
							groups = Regex.groups( row.get(11), "\\{([HA]\\d+).*?=([^=]+)=([\\w\\s]*)" );
							if (groups.size()>=3) unparsables.put( groups.get(1).trim(), groups.get(2) );
							else unparsables.put( row.get(11), null );
							//System.out.println( "Couldn't parse: "+row.get(11) );
							//throw new RuntimeException( "Stopped at line "+rowNum+" in "+file+" -> "+groups );
						}
					} else {
						String code = Regex.first( row.get(3).trim(), "(G\\d+)" );
						String original = Regex.first( row.get(1).trim(), "([^\\s,.¶ͅ;·\\[]+)" );
						String replacement = Regex.first( row.get(4).trim(), "=([\\/\\w\\s]+)" );
						//System.out.println( row );
						//String filtered = alpha.filter(original);
						//System.out.println( code+", "+original+"/"+filtered+", "+replacement );

						if (original!=null) {
							if (original.length()>0) {
								link( code, original );
								lookup( code, original, book, chap, verse );
							}
							if (replacement.length()>0) replacement( code, "english", replacement );
						}
					}
				}
			}
		}
		System.err.println( "Couldn't parse:\n"+unparsables );
		return this;
	}
	
	public static void main ( String[] args ) throws Exception {
		Strongs s = new STEPBibleData().load( args[0] );
		//System.out.println( s.data().get("strongs").serialize() );
		System.out.println( s.filtered() );
	}
	
}
