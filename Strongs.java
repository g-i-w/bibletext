package bibletext;

import java.util.*;
import creek.*;

public interface Strongs {

	public Strongs load ( String path ) throws Exception;
	
	public Strongs data ( Strongs strongs );
	

	public void link ( String strongs, String full_word );

	public void replacement ( String strongs, String replacement );
	
		
	public Tree data ();
	/* {
		"strongs":{ strongs: { replacement }},
		"basic":{ 
			basic_word: { strongs: data().get("strongs").get(strongs) }
		},
	} */
	
	
	public Tree search ( String basic_or_full_word );
	
	public String filtered (); // list of characters filtered out
	
}
