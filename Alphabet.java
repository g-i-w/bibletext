package bibletext;

import java.util.*;
import creek.*;

public class Alphabet {

	private Map<Character,Character> convertChars = new HashMap<>();
	private Set<Character> basicChars = new TreeSet<>();
	private Set<Character> filteredChars = new TreeSet<>();
	
	public static Tree CONVERSION;
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
			if (convertChars.containsKey(c)) c = convertChars.get(c);
			// verify it's part of the basicChars set
			if (basicChars.contains(c)) build.append( c );
			else filteredChars.add(c);
		}
		return build.toString();
	}
	
	public Set<Character> basic () {
		return basicChars;
	}
	
	public Set<Character> filtered () {
		return filteredChars;
	}
	
	public Map<Character,Character> conversion () {
		return convertChars;
	}
	
	// use a table column for a character set
	public static void main ( String[] args ) throws Exception {
		//Table table = new CSVFile( args[0] ).table();
		//Alphabet alpha = new Alphabet( table, Integer.parseInt(args[1]) );
		Alphabet alpha = new Alphabet();
		alpha.filter( Alphabet.EXT_GREEK );
		System.out.println( alpha.conversion() );
		System.out.println( alpha.basic() );
		System.out.println( alpha.filtered() );
	}
}
