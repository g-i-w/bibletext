package bibletext;

import java.util.*;
import creek.*;

public class VerseTable {

	public static void main ( String[] args ) throws Exception {
	
		Tree data = new JSON( FileActions.read(args[0]) );
		System.out.println( data.serialize() );
		
		Table table = new CSV();
		
		Map<String,Bible> bibles = new LinkedHashMap<>();
		
		String rootPath = data.get("rootPath").value();
		String htmlPath = data.get("htmlPath").value();
		
		for (Map.Entry<String,Tree> entry : data.get("bibles").map().entrySet()) {
			String lang = entry.getValue().value();
			String code = entry.getKey();
			String biblePath = lang+"/text/"+code+"/";
			Bible bible = new EBibleOrgText().load( rootPath+biblePath );
			System.out.println( bible.books() );
			bibles.put( biblePath, bible );
		}
		System.out.println( bibles );
		
		for (String refStr : data.get("verses").values()) {
			List<String> ref = Regex.groups( refStr, "([^\\d\\s]+)\\s*(\\d+):(\\d+)" );
			String book = ref.get(0);
			String chap = ref.get(1);
			String verse = ref.get(2);
			List<String> row = new ArrayList<>();
			row.add( verse );
			for (String biblePath : bibles.keySet()) {
				Bible bible = bibles.get( biblePath );
				System.out.println( "key: "+biblePath+", "+bible+", "+book+", "+chap+", "+verse );
				String verseText = bible.verse( book, chap, verse );
				System.out.println( verse+" '"+verseText+"'" );
				String verseLinked = "<a href=\""+htmlPath+biblePath.replaceAll("text","html")+book+( chap.length()==1 ? "0"+chap : chap )+".htm#V"+verse+"\">"+verseText+"</a>";
				row.add( verseLinked );
			}
			table.append( row );
		}
	
		System.out.println( table );
		FileActions.write( args[1], Tables.html( table ) );
	
	}
	
}

