package bibletext;

import java.io.*;
import java.util.*;
import creek.*;

public class StrongsMsbNt extends AbstractStrongs {

	public Strongs load ( String path ) throws Exception {
		Table table = new CSVFile( path ).table();
		for (List<String> row : table.data()) {
			String strongs = "G"+row.get(33).trim();
			String greekTR = row.get(31).trim();
			String greekMT = row.get(24).trim();
			String englishGloss = row.get(36).trim();
			String englishMSB = row.get(16).trim();
			link( strongs, greekTR );
			//link( strongs, greekMT );
			//replacement( strongs, englishGloss );
		}
		return this;
	}
	
	public static void main ( String[] args ) throws Exception {
		Strongs s = new StrongsMsbNt().load( args[0] );
		//System.out.println( s.data().get("strongs").serialize() );
		System.out.println( s.filtered() );
	}
	
}
