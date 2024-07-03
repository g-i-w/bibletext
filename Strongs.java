package bibletext;

import java.util.*;
import creek.*;

public class Strongs {

	private Tree data = new JSON( JSON.AUTO_ORDER );
	private Alphabet alphabet = new Alphabet();
	
	private void increment ( Tree leaf ) {
		if (leaf==null) return;
		String valStr = leaf.value();
		int val = ( valStr!=null ? Integer.parseInt( valStr ) : 0 );
		leaf.value( String.valueOf( ++val ) );
	}
	
	public void link ( String strongs, String original ) {
		link( strongs, original, null, null, null, null );
	}

	public void link ( String strongs, String original, String book, String chap, String verse, String replacement ) {
		// Strongs
		Tree strongsObj = data.auto( strongs );
		
		// Strongs -> original
		Tree originalObj = strongsObj.auto( "original" ).auto( original );
		if (book!=null && chap!=null && verse!=null ) originalObj.auto( book ).auto( chap ).add( verse );
		
		// Strongs -> basic
		String basic = alphabet.filter( original );
		if (basic!=null && basic.length()>0) increment( strongsObj.auto( "basic" ).auto( basic ) );
		if (replacement!=null && replacement.length()>0) increment( strongsObj.auto( "english" ).auto( replacement ) );

		// basic -> Strongs
		data.auto( basic ).add( strongs, strongsObj );
	}
	
	public void definition ( String strongs, String definition ) {
		data.auto( strongs ).add( "definition", definition );
	}
	
	public void replacement ( String strongs, String replacement ) {
		data.auto( strongs ).auto( "replacement" ).auto( replacement );
	}
	
	public Tree data () {
		return data;
	}
	
	public Tree data ( String word ) {
		return data.get( word );
	}
	
}
