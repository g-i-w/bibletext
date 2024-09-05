package bibletext;

import java.util.*;
import creek.*;

public abstract class AbstractStrongs implements Strongs {

	private Tree data;
	private Tree lookup;
	private Alphabet alphabet = new Alphabet();
	
	private Map<String,String> basicCache = new HashMap<>();
	
	String basic ( String original ) {
		if (basicCache.containsKey(original)) return basicCache.get(original);
		String basic = alphabet.filter( original );
		if (basic==null || basic.length()==0) return "";
		basicCache.put( original, basic );
		return basic;
	}
	
	String formatStrongs ( String raw ) {
		String prefix = raw.substring(0,1);
		String number = raw.substring(1,raw.length());
		if (!prefix.equals("H") && !prefix.equals("G")) throw new RuntimeException( "Strongs number doesn't start with H or G: "+raw );
		String padding = "0000";
		int diff = padding.length() - number.length();
		if (diff > 0) return prefix+padding.substring(0,diff)+number;
		return raw;
	}
	
	
	//////////////////// ABSTRACT ////////////////////
	public abstract Strongs load ( String path ) throws Exception;
	
	public Strongs data ( Strongs strongs ) {
		data = strongs.data();
		//lookup = strongs.lookup();
		return this;
	}
	
	public void link ( String strongs, String original ) {
		if (strongs==null || original==null) return;
		strongs = formatStrongs(strongs);
		
		String basic = basic(original);
		//System.out.println( "full:"+original+", basic:"+basic );
		
		//data().auto( "strongs" ).auto( strongs  ).auto( basic    ).increment();

		data().auto( "basic"   ).auto( basic    ).add( strongs, data().auto("strongs").auto(strongs) );
	}

	// depricated; provides compatibility
	public void replacement ( String strongs, String language, String definition ) {
		replacement( strongs, definition );
	}
	
	
	public void replacement ( String strongs, String replacement ) {
		if (strongs==null || replacement==null) return;
		strongs = formatStrongs(strongs);
	
		data().auto( "strongs" ).auto( strongs ).auto( replacement );
	}
	
	public Tree data () {
		if (data==null) data = new JSON( JSON.AUTO_ORDER );
		return data;
	}
	
	public Tree search ( String word ) {
		if (data().keys().contains(word)) return data().get(word);
		else {
			String basicWord = basic(word);
			if (data().keys().contains(basicWord)) return data().get(basicWord);
			else {
				for (String subStr : StringFunctions.substrings( basicWord, 2 )) {
					if (data().keys().contains(subStr)) return data().get(subStr);
				}
				return null;
			}
		}
	}
	
	public String filtered () {
		return alphabet.filtered();
	}
	
}
