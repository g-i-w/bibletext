package bibletext;

import java.util.*;
import creek.*;

public interface Strongs {

	public Strongs load ( String path ) throws Exception;
	
	public Strongs data ( Strongs strongs );
	

	public void link ( String strongs, String full_word );

	public void lookup ( String strongs, String full_word, String book, String chap, String verse );
	
	public void definition ( String strongs, String language, String definition );
	
	public void replacement ( String strongs, String language, String replacement );
	
	
	public Tree data ();
	/* {
		"strongs":{ strongs: {
			"basic":  { basic_word: qty },
			"definition":    { language:   { definition:null }},
			"replacement":    { language:   { replacement:null }}
		}},
		"basic":{ 
			basic_word: { strongs: data().get("strongs").get(strongs) }
		},
	} */
	
	public Tree lookup ();
	/* {
		"strongs":{ strongs: {
			full_word:{ book: { chap:[ verse ] }},
		}},
		"basic":{ basic_word: {
			full_word:{ book: { chap:[ verse ] }}
		}},
	} */
	
	public Tree search ( String basic_or_full_word );
	
	public String filtered (); // list of characters filtered out
	
}
