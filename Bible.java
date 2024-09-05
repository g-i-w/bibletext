package bibletext;

import java.util.*;
import creek.*;

public interface Bible {

	public Bible load ( String path ) throws Exception;

	public Set<String> books ();
	public Map<String,String> aliases ();
	public String alias ( String raw );

	public String verse ( String refStr ) throws Exception;
	public String verse ( String book, String chap, String verse ) throws Exception;

	public Tree text ();
	// { book:{ chap:{ verse:text }}}

	public Tree lookup ();
	// { basic_word:{ book:{ chap:{ verse:{verse_obj} }}}}
	
	public Tree words ();
	/* {
		basic_word:{
			"id":    id,
			//"chars": [ AAAABBBB, CCCCDDDD ... ],
			"full":  { full_word:qty_str }
		}
	} */
	
	public Tree compressed ();
	/* {
		"text":     { book: { chap:{ verse:[ id_0, id_1 ... ] }}             },
		"basic":    { id: basic_word                                         },
		"lookup":   { basic_word: { book:{ chap:[ verse_0, verse_1 ... ] }}  }
	} */
	
	public Tree search ();
	/* {
		basic_word:{
			"super": { super_basic_word: lookup().get(super_basic_word) },
			"sub":   { sub_basic_word:   lookup().get(sub_basic_word)   },
			"this":  { basic_word:       lookup().get(basic_word)       }
		}
	} */
	
	public Tree search ( String full_or_basic_word ); // check as is (basic), then convert to basic, then begin to reduce length and check for basic.
	
	public String basic ( String full_word ); // convert full_word to basic_word
	
}
