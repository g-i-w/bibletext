package bibletext;

import java.util.*;
import creek.*;

public class Alphabet {

	private Map<Character,Character> convertChars = new HashMap<>();
	private Set<Character> basicChars = new TreeSet<>();
	private Set<Character> filteredChars = new TreeSet<>();
	

	public static String HEBREW = "אבגדהוזחטיךכלםמןנסעףפץצקרשתװױײ׳״־׀׃׆";
	public static String GREEK = "᾽’ΑαΒβΓγΔδΕεΖζΗηΘθΙιΚκΛλΜμΝνΞξΟοΠπΡρΣσςΤτΥυΦφΧχΨψΩω";
	public static String EXT_GREEK = "ΐΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩάέήίΰαβγδεζηθικλμνξοπρςστυφχψωϊϋόύώἀἁἂἃἄἅἆἈἉἋἌἍἎἐἑἓἔἕἘἙἛἜἝἠἡἢἣἤἥἦἧἨἩἪἫἬἭἮἰἱἳἴἵἶἷἸἹἼἽὀὁὂὃὄὅὈὉὋὌὍὐὑὒὓὔὕὖὗὙὝὟὠὡὢὤὥὦὧὨὩὪὬὭὮὯᾯὰάὲέὴήὶίὸόὺύὼώᾄᾅᾐᾑᾔᾖᾗᾠᾧᾳᾴᾶᾷ᾽ῃῄῆῇῒΐῖῢῥῦῬῳῴῶῷ’";
	public static String WESTERN = "-'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";


	// constructors used for testing
	
	public Alphabet ( Table table, int column ) {
		for (int i=0; i<table.rowCount(); i++) alphabet( table.item( i, column ) );
	}

	public Alphabet ( List<String> list ) {
		for (String word : list) alphabet( word );
	}

	public Alphabet ( String a ) {
		alphabet( a );
	}
	
	
	// constructor for normal usage
	
	public Alphabet () {
		alphabet( HEBREW+GREEK+WESTERN );
		try {
			conversion(
				new JSON(
					"{\"α\":\"άᾳᾴᾶᾷὰάἀἁἂἃἄἅἆᾄᾅ\","+
					"\"Α\":\"ἈἉἋἌἍἎ\","+
					"\"ε\":\"έἐἑἓἔἕ\","+
					"\"Ε\":\"ἘἙἛἜἝ\","+
					"\"Η\":\"ἨἩἪἫἬἭἮ\","+
					"\"ι\":\"ϊΐὶίίῒΐῖἰἱἳἴἵἶἷ\","+
					"\"Ι\":\"ἸἹἼἽ\","+
					"\"ο\":\"όὸόὀὁὂὃὄὅ\","+
					"\"Ο\":\"ὈὉὋὌὍ\","+
					"\"υ\":\"ΰῦῢὐὑὒὓὔὕὖὗ\","+
					"\"Υ\":\"ὙὝὟ\","+
					"\"ω\":\"ώῳῴῶῷᾠᾧὠὡὢὤὥὦὧὼώ\","+
					"\"Ω\":\"ὨὩὪὬὭὮὯᾯ\","+
					"\"ε\":\"ἐἑἓἔἕὲέέ\","+
					"\"ν\":\"ύϋὺύ\","+
					"\"η\":\"ὴήῃῄῆῇᾐᾑᾔᾖᾗήἠἡἢἣἤἥἦἧ\","+
					"\"ρ\":\"ῥ\","+
					"\"Ρ\":\"Ῥ\"}"
				)
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void alphabet ( String chars ) {
		if (chars==null) return;
		for (Character c : chars.toCharArray()) basicChars.add( c );
	}
	
	public void conversion ( Tree conv ) {
		if (conv==null) return;
		for (String key : conv.keys()) {
			Character keyChar = key.toCharArray()[0];
			for (Character c : conv.get(key).value().toCharArray()) convertChars.put( c, keyChar );
		}
	}

	public String filter ( String word ) {
		if (basicChars == null) return word;
		
		int length = word.length();
		StringBuilder build = new StringBuilder();
		for (Character c : word.toCharArray()) {
			// convert if necessary
			if (convertChars.containsKey(c)) {
				filteredChars.add(c);
				c = convertChars.get(c);
			}
			// verify it's part of the basicChars set
			if (basicChars.contains(c)) build.append( c );
			else filteredChars.add(c);
		}
		return build.toString().intern();
	}
	
	public Set<Character> basic () {
		return basicChars;
	}
	
	public String filtered () {
		StringBuilder sb = new StringBuilder();
		for (Character c : filteredChars) {
			sb.append( c );
		}
		return sb.toString();
	}
	
	public Map<Character,Character> conversion () {
		return convertChars;
	}
	
	public String wordHash ( String key ) {
		int len = key.length();
		if (len <= 2) return key;
		//return key.substring( 0, 3 );
		return key.substring( 0, 1 )+key.substring( len-1, len ); // first+last
	}
	
	public String strongsHash ( String key ) {
		if (key.length() <= 3) return key;
		return key.substring( 0, 3 );
	}
	
	// use a table column for a character set
	public static void main ( String[] args ) {
		Alphabet a = new Alphabet();
		System.out.println( "basic: "+a.filter( args[0] ) );
		System.out.println( "filtered: "+a.filtered() );
	}
}
