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
		if (basic==null || basic.length()==0) {
			System.out.println( "Couldn't create basic version of:\n***\n"+original+"\n***" );
			return "";
		}
		basicCache.put( original, basic );
		return basic;
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
		
		String basic = basic(original);
		//System.out.println( "full:"+original+", basic:"+basic );
		
		//data().auto( "strongs" ).auto( strongs  ).auto( "full"    ).auto( original ).increment();
		//data().auto( "strongs" ).auto( strongs  ).auto( "basic"   ).auto( basic    ).increment();
		data().auto( "strongs" ).auto( strongs  ).auto( basic    ).increment();

		//data().auto( "basic"   ).auto( basic    ).auto( "full"    ).auto( original ).increment();
		//data().auto( "basic"   ).auto( basic    ).auto( "strongs" ).auto( strongs  ).increment();
		data().auto( "basic"   ).auto( basic    ).add( strongs, data().get("strongs").get(strongs) );
	}

	/*public void lookup ( String strongs, String original, String book, String chap, String verse ) {
		if (strongs==null || original==null || book==null || chap==null || verse==null) return;
		
		String basic = basic(original);
		
		lookup().auto( "strongs" ).auto( strongs  ).auto( original ).auto( book ).auto( chap ).add( verse );
		lookup().auto( "basic"   ).auto( basic    ).auto( original ).auto( book ).auto( chap ).add( verse );
	}*/
	
	/*public void definition ( String strongs, String language, String definition ) {
		if (strongs==null || language==null || definition==null) return;
	
		data().auto( "strongs" ).auto( strongs ).auto( "definition" ).auto( language ).auto( definition );
	}*/
	
	public void replacement ( String strongs, String language, String replacement ) {
		if (strongs==null || language==null || replacement==null) return;
	
		data().auto( "strongs" ).auto( strongs ).auto( replacement ).increment();
	}
	
	public Tree data () {
		if (data==null) data = new JSON( JSON.AUTO_ORDER );
		return data;
	}
	
	/*public Tree lookup () {
		if (lookup==null) lookup = new JSON( JSON.RETAIN_ORDER );
		return lookup;
	}*/
	
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
