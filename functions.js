// building-block functions

function getId ( id ) {
	return document.getElementById( id );
}

function getVal ( id ) {
	return getId( id ).value;
}

function setVal ( id, val ) {
	console.log( 'setting '+id+' to '+val );
	var prev = getId( id ).value;
	getId( id ).value = val;
	return prev;
}
	
function basicChar ( c ) {
	// check for substitute greek char
	if ('άᾳᾴᾶᾷὰάἀἁἂἃἄἅἆᾄᾅ'.indexOf( c ) > -1) return 'α';
	if ('ἈἉἋἌἍἎ'.indexOf( c ) > -1) return 'Α';
	if ('έἐἑἓἔἕ'.indexOf( c ) > -1) return 'ε';
	if ('ἘἙἛἜἝ'.indexOf( c ) > -1) return 'Ε';
	if ('ἨἩἪἫἬἭἮ'.indexOf( c ) > -1) return 'Η';
	if ('ϊΐὶίίῒΐῖἰἱἳἴἵἶἷ'.indexOf( c ) > -1) return 'ι';
	if ('ἸἹἼἽ'.indexOf( c ) > -1) return 'Ι';
	if ('όὸόὀὁὂὃὄὅ'.indexOf( c ) > -1) return 'ο';
	if ('ὈὉὋὌὍ'.indexOf( c ) > -1) return 'Ο';
	if ('ΰῦῢὐὑὒὓὔὕὖὗ'.indexOf( c ) > -1) return 'υ';
	if ('ὙὝὟ'.indexOf( c ) > -1) return 'Υ';
	if ('ώῳῴῶῷᾠᾧὠὡὢὤὥὦὧὼώ'.indexOf( c ) > -1) return 'ω';
	if ('ὨὩὪὬὭὮὯᾯ'.indexOf( c ) > -1) return 'Ω';
	if ('ἐἑἓἔἕὲέέ'.indexOf( c ) > -1) return 'ε';
	if ('ύϋὺύ'.indexOf( c ) > -1) return 'ν';
	if ('ὴήῃῄῆῇᾐᾑᾔᾖᾗήἠἡἢἣἤἥἦἧ'.indexOf( c ) > -1) return 'η';
	if ('ῥ'.indexOf( c ) > -1) return 'ρ';
	if ('Ῥ'.indexOf( c ) > -1) return 'Ρ';
	
	// check for basic greek char
	if ('-᾽’ΑαΒβΓγΔδΕεΖζΗηΘθΙιΚκΛλΜμΝνΞξΟοΠπΡρΣσςΤτΥυΦφΧχΨψΩω'.indexOf( c ) > -1) return c;
	
	// check for basic hebrew char
	if ('אבגדהוזחטיךכלםמןנסעףפץצקרשתװױײ׳״־׀׃׆'.indexOf( c ) > -1) return c;
	
	// otherwise return nothing
	//console.log( "non-printed char: '"+c+"'" );
	return '';
}

function basicWord ( word ) {
	var basic = '';
	for (let i = 0; i < word.length; i++) {
		basic += basicChar( word[i] );
	}
	return basic;
}

function substringSet ( word ) {
	var wordLength = word.length;
	var set = new Set();
	
	for (let size=wordLength-1; size>=1; size--) {
		let maxPos = wordLength-size; // max start-index of substring
		for (let pos=0; pos<=maxPos; pos++) {
			let subWord = word.substring(pos, pos+size);
			set.add( subWord );
		}
	}
	return set;
}

function wordHash ( key ) {
	let len = key.length;
	if (len <= 2) return key;
	return key.substring( 0, 1 )+key.substring( len-1, len ); // first+last
}

function last ( array ) {
	return array[array.length-1];
}

function something ( anything ) {
	if (anything!==undefined && anything!=null && anything!=='') return true;
	else return false;
}

function objectPath ( obj, pathArray ) {
	console.log( pathArray );
	if (!something(obj)) return null;
	for (const key of Object.values(pathArray)) {
		if (!something(obj[key])) {
			//console.log(obj);
			//console.log( "couldn't find key: "+key );
			return null;
		}
		obj = obj[key];
	}
	return obj;
}

function inArray ( arrObj, arrVal ) {
	for (let val of Object.values(arrObj)) {
		if (arrVal==val) return true;
	}
	return false;
}



// structural functions

function verseText ( bibleObj, book, chap, verse ) {
	//console.log( 'verseText: '+book+','+chap+','+verse );
	if (something( bibleObj ) && something( bibleObj[book] ) && something( bibleObj[book][chap] )) {
		return bibleObj[book][chap][verse];
	} else {
		//return `[NOT FOUND: ${book} ${chap}:${verse}]`;
		return '';
	}
}

function getBooks ( bibleObj ) {
	if (something( bibleObj )) return Object.keys( bibleObj );
	else return [];
}

function getChaps ( bibleObj, book ) {
	if (something( bibleObj ) && something( bibleObj[book] )) return Object.keys( bibleObj[book] );
	else return [];
}

function getVerses ( bibleObj, book, chap ) {
	if (something( bibleObj ) && something( bibleObj[book] ) && something( bibleObj[book][chap] )) return Object.keys( bibleObj[book][chap] );
	else return [];
}

function verseData ( bibleObj, book, chap, verse ) {
	var prevBook = book;
	var nextBook = book;
	var booksArr = getBooks(bibleObj);
	for (let i=0; i<booksArr.length; i++) {
		if (book==booksArr[i]) {
			if (i>0) prevBook=booksArr[i-1];
			if (i<booksArr.length-1) nextBook=booksArr[i+1];
			break;
		}
	}
	//console.log( 'prevBook: '+prevBook+', book: '+book+', nextBook: '+nextBook );
	var chapNum = Number(chap);
	var lastChapNum = Number(last(getChaps(bibleObj,book)));
	var verseNum = Number(verse);
	var lastVerseNum = Number(last(getVerses(bibleObj,book,chap)));
	//console.log( 'chapNum: '+chapNum+', lastChapNum: '+lastChapNum+', verseNum: '+verseNum+', lastVerseNum: '+lastVerseNum );
	
	var data = {
		prevVerse: { book:null, chap:null, verse:null },
		nextVerse: { book:null, chap:null, verse:null }
	};
	
	// next verse
	if (verseNum+1 > lastVerseNum) {
		if (chapNum+1 > lastChapNum) {
			if (nextBook==book) {
				// do nothing
				data.nextVerse.book = '';
				data.nextVerse.chap = '';
				data.nextVerse.verse = '';
				console.log( "NEXT: do nothing: "+data.nextVerse.book+','+data.nextVerse.chap+','+data.nextVerse.verse );
			} else {
				// otherwise next book
				data.nextVerse.book = nextBook;
				data.nextVerse.chap = '1';
				data.nextVerse.verse = '1';
				console.log( "NEXT: next book: "+data.nextVerse.book+','+data.nextVerse.chap+','+data.nextVerse.verse );
			}
		} else {
			// otherwise next chapter
			data.nextVerse.book = book;
			data.nextVerse.chap = (chapNum+1).toString();
			data.nextVerse.verse = '1';
			//console.log( "NEXT: next chap: "+data.nextVerse.book+','+data.nextVerse.chap+','+data.nextVerse.verse );
		}
	} else {
		// otherwise next verse
		data.nextVerse.book = book;
		data.nextVerse.chap = chap;
		data.nextVerse.verse = (verseNum+1).toString();
		//console.log( "NEXT: next verse: "+data.nextVerse.book+','+data.nextVerse.chap+','+data.nextVerse.verse );
	}
	
	// prev verse
	if (verseNum-1 < 1) {
		if (chapNum-1 < 1) {
			if (prevBook==book) {
				// do nothing
				data.prevVerse.book = '';
				data.prevVerse.chap = '';
				data.prevVerse.verse = '';
				//console.log( "PREV: do nothing: "+data.prevVerse.book+','+data.prevVerse.chap+','+data.prevVerse.verse );
			} else {
				// otherwise prev book
				let prevChap = last(getChaps(bibleObj,prevBook));
				let prevVerse = last(getVerses(bibleObj,prevBook,prevChap));
				data.prevVerse.book = prevBook;
				data.prevVerse.chap = prevChap;
				data.prevVerse.verse = prevVerse;
				//console.log( "PREV: prev book: "+data.prevVerse.book+','+data.prevVerse.chap+','+data.prevVerse.verse );
			}
		} else {
			// otherwise prev chapter
			let prevChap = (chapNum-1).toString();
			let prevVerse = last(getVerses(bibleObj,book,prevChap));
			data.prevVerse.book = book;
			data.prevVerse.chap = prevChap;
			data.prevVerse.verse = prevVerse;
			//console.log( "PREV: prev chap: "+data.prevVerse.book+','+data.prevVerse.chap+','+data.prevVerse.verse );
		}
	} else {
		// otherwise prev verse
		let prevVerse = (verseNum-1).toString();
		data.prevVerse.book = book;
		data.prevVerse.chap = chap;
		data.prevVerse.verse = prevVerse;
		//console.log( "PREV: prev chap: "+data.prevVerse.book+','+data.prevVerse.chap+','+data.prevVerse.verse );
	}
	
	return data;
}

function exactWord ( text, basic ) {
	//console.log( text );
	var html = '';
	if (something( text )) {
		for (let word of text.split(/\s+/)) {
			if (basicWord( word )==basic) html += " <a href=\"#wordView\" onclick=\"changeWord('"+word+"')\">"+word+"</a>";
		}
	}
	return html;
}

function strongsInfo ( word ) {
	var basicToCode = interlinear.strongs.basicToCode;
	var codeToReplacement = interlinear.strongs.codeToReplacement;
	var basic = basicWord( word );
	var hash = wordHash( basic );
	var html = '<table>';
	if (something( basicToCode[hash] ) && something( basicToCode[hash][basic] )) {
		for (const [code, codeObj] of Object.entries(basicToCode[hash][basic])) {
			let codeHash = code.substring(0,3);
			if (something( codeToReplacement[codeHash] ) && something( codeToReplacement[codeHash][code] )) {
				for (const [rep, repObj] of Object.entries(codeToReplacement[codeHash][code])) {
					html += "<tr><td style=\"font-size:0.7em;color:gray;\">"+code+"</td><td style=\"font-size:0.8em;\">"+rep+"</td></tr>";
				}
			}
		}
	}
	html += '</table>';
	return html;
}

function strongsList ( word ) {
	var basicToCode = interlinear.strongs.basicToCode;
	var codeToReplacement = interlinear.strongs.codeToReplacement;
	var basic = basicWord( word );
	var hash = wordHash( basic );
	var list = [];
	if (something( basicToCode[hash] ) && something( basicToCode[hash][basic] )) {
		for (const [code, codeObj] of Object.entries(basicToCode[hash][basic])) {
			let codeHash = code.substring(0,3);
			if (something( codeToReplacement[codeHash] ) && something( codeToReplacement[codeHash][code] )) {
				for (const [rep, repObj] of Object.entries(codeToReplacement[codeHash][code])) {
					list.push.apply( list, alphaText(rep) );
				}
			}
		}
	}
	return list;
}

function strongsInfoVerse ( word, book, chap, verse ) {
	var basicToCode = interlinear.strongs.basicToCode;
	var codeToReplacement = interlinear.strongs.codeToReplacement;
	var basic = basicWord( word );
	var hash = wordHash( basic );
	var html = '<table>';
	if (something( basicToCode[hash] ) && something( basicToCode[hash][basic] )) {
		for (const [code, codeObj] of Object.entries(basicToCode[hash][basic])) {
			let codeHash = code.substring(0,3);
			if (something( codeToReplacement[codeHash] ) && something( codeToReplacement[codeHash][code] )) {
				for (const [rep, repObj] of Object.entries(codeToReplacement[codeHash][code])) {
					//console.log( "testing strongs path: "+word );
					var obj = objectPath( interlinear.data, [ hash, basic, code, book, chap ] );
					if (something(obj)) {
						//console.log(obj);
						//console.log( verse );
						if (inArray(obj, verse))
							html += "<tr><td style=\"font-size:0.7em;color:gray;\">"+code+"</td><td style=\"font-size:0.8em;\">"+rep+"</td></tr>";
					}
				}
			}
		}
	}
	html += '</table>';
	return html;
}

function isVerse ( book, chap, verse ) {
	//return ( langToCode(getVal("book"))==langToCode(book) && getVal("chap")==chap && getVal("verse")==verse );
	return ( state.book==book && state.chap==chap && state.verse==verse );
}

function isWord ( word ) {
	return ( state.word == basicWord(word) );
}

function nbNumber ( book ) {
	if (book.indexOf(' ')==1) return book.substring(0,1)+'&nbsp;'+book.substring(2); // if second char is space, replace with '&nbsp;'
	else return book;
}

function verseLink ( book, chap, verse, link ) {
	if (!something( link )) link = nbNumber(codeToLang(book))+'&nbsp;'+chap+':'+verse;
	if (isVerse( book, chap, verse ))
		return `<a href="#verseView" onclick="state.view='verseView';changeVerse('${book}','${chap}','${verse}');" class="highlighted">${link}</a>`;
	else
		return `<a href="#verseView" onclick="state.view='verseView';changeVerse('${book}','${chap}','${verse}');">${link}</a>`;
}

function wordLink ( word, link ) {
	if (!something( link )) link = word;
	if (isWord( word ))
		return `<a href="#wordView" onclick="state.view='wordView';changeWord('${word}');" class="highlighted">${link}</a>`;
	else
		return `<a href="#wordView" onclick="state.view='wordView';changeWord('${word}');">${link}</a>`;
}

function grayedOut ( icon ) {
	return '<span style="color:lightgray;">'+icon+'</span>';
}

function verseFwdBack () {
	var html = '';
	var history = Object.values(verseHistory);
	for (let i=0; i<history.length; i++) {
		if( isVerse( history[i].book, history[i].chap, history[i].verse ) ) {
			// back link
			if (i > 0) html += verseLink( history[i-1].book, history[i-1].chap, history[i-1].verse, leftArrow );
			else html += grayedOut(leftArrow);
			html += '&nbsp;';
			// fwd link
			if (i < history.length-1) html += verseLink( history[i+1].book, history[i+1].chap, history[i+1].verse, rightArrow );
			else html += grayedOut(rightArrow);
		}
	}
	return html;
}
	
function wordFwdBack () {
	var html = '';
	var history = Object.keys(wordHistory);
	for (let i=0; i<history.length; i++) {
		if( isWord( history[i] ) ) {
			// back link
			if (i > 0) html += wordLink( history[i-1], leftArrow );
			else html += grayedOut(leftArrow);
			html += '&nbsp;';
			// fwd link
			if (i < history.length-1) html += wordLink( history[i+1], rightArrow );
			else html += grayedOut(rightArrow);
		}
	}
	return html;
}

function verseAnalysis ( verseArray, book, chap, verse ) {
	var html = '<table>';
	for (let word of verseArray) {
		if (word.indexOf('־')>-1) {
			let delim = 'border-right:solid 1px lightgray;';
			for (let subWord of Object.values(word.split(/־/))) {
				//console.log( subWord );
				html += '<tr><td style="'+delim+'">'+wordLink( subWord )+'</td><td>'+overlap( verseText( interlinear.translations[languageStrongs], book, chap, verse ), strongsList( subWord ) )+'</td><td>'+gematria( subWord )[0]+'</td></tr>';
				delim = 'border-top:0;border-right:solid 1px lightgray;';
			}
		} else {
			html += '<tr><td>'+wordLink( word )+'</td><td>'+overlap( verseText( interlinear.translations[languageStrongs], book, chap, verse ), strongsList( word ) )+'</td><td>'+gematria( word )[0]+'</td></tr>';
		}
	}
	html += '</table>';
	return html;
}
	
function wordInfo ( word ) {
	//console.log( 'wordInfo: '+word );
	
	var basic = basicWord( word );
	var hash = wordHash( basic );
	
	// history
	var html = '<table><tr><td class="top-td">'+wordFwdBack()+'</td><td class="top-td">';
	var delim = '';
	for (const [key, nullObj] of Object.entries(wordHistory)) {
		html += delim+wordLink( key );
		delim = '&nbsp;<span style="color:lightgray;">|</span> ';
	}
	html += '</td></tr></table>';

	// word info
	html += '<table><tr><td class="highlighted"><h1>'+basic+'</h1>'+gematria( word )[1]+'<br><br>'+gematria( word )[0]+'</td><td class="highlighted">'+strongsInfo( word )+'</td></tr>';
	html += '</table>';
	
	// word locations
	html += '<table>';
	if (something( interlinear.data[hash] ) && something( interlinear.data[hash][basic] )) {
		for (var [code, codeObj] of Object.entries(interlinear.data[hash][basic])) {
			if (code=='') code = missingPlaceholder;
			html += '<tr><td style="font-size:0.7em;color:gray;vertical-align:top;border-right:solid 1px lightgray;" rowspan='+Object.entries(codeObj).length+'>'+code+'</td>';
			for (const [book, bookObj] of Object.entries(codeObj)) {
				html += '<td>'+nbNumber(codeToLang(book))+'</td><td>';
				let delim = '';
				for (const [chap, chapObj] of Object.entries(bookObj)) {
					for (const verse of Object.values(chapObj)) {
						let text = verseText( interlinear.text, book, chap, verse );
						html += delim+verseLink( book, chap, verse, chap+':'+verse );
						delim = '&nbsp; ';
					}
				}
				html += '</td></tr>';
			}
		}
	}
	html += '</table>';
	html += '<table>';
	// sub-word info
	for (let sub of substringSet( basic )) {
		let subHash = wordHash( sub );
		if (something( interlinear.data[subHash] ) && something( interlinear.data[subHash][sub] )) {
			html += "<tr><td>"+wordLink( sub )+'<br>'+gematria( sub )[0]+"</td><td>"+strongsInfo( sub )+"</td></tr>";
		}
	}
	html += '</table>';
	
	return html;
}

function verseInfo ( book, chap, verse ) {
	//console.log( 'verseInfo: '+book+','+chap+','+verse );

	// verse history
	var html = '<table>';
	html += '<tr><td class="top-td">'+verseFwdBack()+'</td><td class="top-td">';
	var delim = '';
	for (const [key, verseObj] of Object.entries(verseHistory)) {
		html += delim+verseLink( verseObj.book, verseObj.chap, verseObj.verse );
		delim = '&nbsp;<span style="color:lightgray;">|</span> ';
	}
	html += '</td></tr>';
	html += '</table>';
	
	// translations￼
	html += '<table>';
	var lang = interlinear.translations[language];
	let data = verseData( lang, book, chap, verse );
	if (something(data.prevVerse.book)) html += '<tr><td class="highlighted"><center>'+verseLink(data.prevVerse.book, data.prevVerse.chap, data.prevVerse.verse, upArrow)+'</center></td><td class="highlighted" style="color:gray;">'+data.prevVerse.chap+':'+data.prevVerse.verse+'</td><td class="highlighted" style="color:gray;">'+verseText( lang, data.prevVerse.book, data.prevVerse.chap, data.prevVerse.verse )+'</td></tr>';
	html += '<tr><td class="highlighted">'+nbNumber(codeToLang(book))+'</td><td class="highlighted">'+chap+':'+verse+'</td><td class="highlighted" style="">'+verseText( lang, book, chap, verse )+'</td></tr>';
	if (something(data.nextVerse.book)) html += '<tr><td class="highlighted"><center>'+verseLink(data.nextVerse.book, data.nextVerse.chap, data.nextVerse.verse, downArrow)+'</center></td><td class="highlighted" style="color:gray;">'+data.nextVerse.chap+':'+data.nextVerse.verse+'</td><td class="highlighted" style="color:gray;">'+verseText( lang, data.nextVerse.book, data.nextVerse.chap, data.nextVerse.verse )+'</td></tr>';
	html += '</table>';
	
	// original text
	var text = verseText( interlinear.text, book, chap, verse );
	if (something(text)) { // Zech 1:18-21 were at beginning of chap 2 in 'hbo'.  This ensures it's clear if something is missing!
		html += '<table>';
		html += '<tr><td style="text-align:center;padding-top:24px;padding-bottom:24px;">';
		var verseArray = text.split(/\s+/);
		for (let word of verseArray) {
			html += ' '+wordLink( word );
		}
		html += '</td></tr>';
		html += '</table>';
		
		// each word with Strongs info
		html += verseAnalysis( verseArray, book, chap, verse );
	} else {
		html += missingPlaceholder;
	}
	return html;
}



// Greek and Hebrew gematria

function gematria ( word ) {
	basic = basicWord( word );
	var value = 0;
	var altval = 0;
	var alttitle = '';
	var pass = 0;
	var title = '';
	var list = '';
	var delim = '';
	var listdelim = '';
	
	// overall
	for (let i=0; i<basic.length; i++) {
		//console.log( 'pass: '+i+', char: '+basic[i] );
	
		// Greek
		if ('Αα'.indexOf( basic[i] ) > -1) pass += 1;
		if ('Bβ'.indexOf( basic[i] ) > -1) pass += 2;
		if ('Γγ'.indexOf( basic[i] ) > -1) pass += 3;
		if ('Δδ'.indexOf( basic[i] ) > -1) pass += 4;
		if ('Εε'.indexOf( basic[i] ) > -1) pass += 5;
		if ('Zζ'.indexOf( basic[i] ) > -1) pass += 7;
		if ('Hη'.indexOf( basic[i] ) > -1) pass += 8;
		if ('Θθ'.indexOf( basic[i] ) > -1) pass += 9;
		if ('Ιι'.indexOf( basic[i] ) > -1) pass += 10;
		if ('Kκ'.indexOf( basic[i] ) > -1) pass += 20;
		if ('Λλ'.indexOf( basic[i] ) > -1) pass += 30;
		if ('Mμ'.indexOf( basic[i] ) > -1) pass += 40;
		if ('Nν'.indexOf( basic[i] ) > -1) pass += 50;
		if ('Ξξ'.indexOf( basic[i] ) > -1) pass += 60;
		if ('Oο'.indexOf( basic[i] ) > -1) pass += 70;
		if ('Pπ'.indexOf( basic[i] ) > -1) pass += 80;
		if ('ϟϞ'.indexOf( basic[i] ) > -1) pass += 90;
		if ('Ρρ'.indexOf( basic[i] ) > -1) pass += 100;
		if ('Σσ'.indexOf( basic[i] ) > -1) pass += 200;
		if ('Tτ'.indexOf( basic[i] ) > -1) pass += 300;
		if ('Υυ'.indexOf( basic[i] ) > -1) pass += 400;
		if ('Φφ'.indexOf( basic[i] ) > -1) pass += 500;
		if ('Χχ'.indexOf( basic[i] ) > -1) pass += 600;
		if ('Ψψ'.indexOf( basic[i] ) > -1) pass += 700;
		if ('Ωω'.indexOf( basic[i] ) > -1) pass += 800;
		if ('ς' == basic[i]) pass += 200; // final sigma
		if ('Ϡ' == basic[i]) pass += 900;
		// Hebrew
		if ('א' == basic[i]) pass += 1; // or 1000 (altval);
		if ('ב' == basic[i]) pass += 2;
		if ('ג' == basic[i]) pass += 3;
		if ('ד' == basic[i]) pass += 4;
		if ('ה' == basic[i]) pass += 5;
		if ('ו' == basic[i]) pass += 6;
		if ('ז' == basic[i]) pass += 7;
		if ('ח' == basic[i]) pass += 8;
		if ('ט' == basic[i]) pass += 9;
		if ('י' == basic[i]) pass += 10;
		if ('כ' == basic[i]) pass += 20;
		if ('ך' == basic[i]) {if (i<basic.length-1) pass+=20; else pass+=500;}
		if ('ל' == basic[i]) pass += 30;
		if ('ם' == basic[i]) pass += 40;
		if ('מ' == basic[i]) {if (i<basic.length-1) pass+=40; else pass+=600;}
		if ('נ' == basic[i]) pass += 50;
		if ('ן' == basic[i]) {if (i<basic.length-1) pass+=50; else pass+=700;}
		if ('ס' == basic[i]) pass += 60;
		if ('ע' == basic[i]) pass += 70;
		if ('פ' == basic[i]) pass += 80;
		if ('ף' == basic[i]) {if (i<basic.length-1) pass+=80; else pass+=800;}
		if ('צ' == basic[i]) pass += 90;
		if ('ץ' == basic[i]) {if (i<basic.length-1) pass+=90; else pass+=900;}
		if ('ק' == basic[i]) pass += 100;
		if ('ר' == basic[i]) pass += 200;
		if ('ש' == basic[i]) pass += 300;
		if ('ת' == basic[i]) pass += 400;
		
		value += pass;
		if ('א' == basic[i]) {
			altval += 1000;
			alttitle += delim+basic[i]+':'+1000;
		} else {
			altval += pass;
			alttitle += delim+basic[i]+':'+pass;
		}
		
		title += delim+basic[i]+':'+pass;
		list += listdelim+basic[i]+':'+('א' == basic[i] ? '1||1000' : pass);

		delim = ' ';
		listdelim = '&nbsp;&nbsp; ';

		pass = 0;
	}

	return [ '<span style="font-size:0.7em;color:gray" title="'+title+'">'+value+'</span>'+(value==altval ? '' : '<span style="font-size:0.7em;color:gray" title="'+alttitle+'">,&nbsp;'+altval+'</span>'), '<span style="font-size:0.7em;color:gray">'+list+'</span>' ];
}

function alphaWord ( word ) {
	var alpha = '';
	wordLower = word.toLowerCase();
	for (let i = 0; i < word.length; i++) {
		if ("abcdefghijklmnopqrstuvwxyz".indexOf(wordLower[i]) > -1) alpha += word[i];
	}
	return alpha;
}

function alphaText ( text ) {
	var textArrayRaw = text.split(/[\s\(\)-]+/);
	var textArray = [];
	for (word of Object.values(textArrayRaw)) {
		textArray.push( alphaWord(word) );
	}
	return textArray;
}

function overlap ( verse, strongsList ) {
	//console.log( verse );
	var verseArray = alphaText(verse);
	//console.log( verseArray );
	//console.log( strongsList );
	var overlapSet = new Set();
	for (let verseWord of Object.values(verseArray)) {
		for (let strongsWord of Object.values(strongsList)) {
			//console.log( "comparing: "+strongsWord+" <-> "+verseWord );
			if (verseWord=='') continue;
			let strongsLower = strongsWord.toLowerCase();
			let verseLower = verseWord.toLowerCase();
			if ((verseLower.length>2 && strongsLower.indexOf(verseLower)==0) || (strongsLower.length>2 && verseLower.indexOf(strongsLower)==0)) {
				if (strongsLower == verseLower) overlapSet.add( verseWord );
				else overlapSet.add( strongsWord );
			} else {
				if (strongsLower == verseLower) overlapSet.add( verseWord );
			}
		}
	}
	//console.log( overlapSet );
	var overlapText = '';
	var delim = '';
	for (let word of overlapSet) {
		overlapText += delim+word;
		delim = ', ';
	}
	return overlapText;
}

// languages

function langToCode ( expression ) {
	//console.log( 'langToCode: '+expression );
	if (expression=='') return expression;
	if (something(expression) && something(aliases.langToCode[language])) {
		for (const [alias, code] of Object.entries(aliases.langToCode[language])) {
			if (alias.toLowerCase().indexOf( expression.toLowerCase() ) > -1) {
				console.log( code );
				return code;
			}
		}
	}
	return expression;
}

function codeToLang ( code ) {
	//console.log( 'codeToLang: '+code );
	if (code=='') return code;
	//console.log( aliases.codeToLang[language] );
	if (something(aliases.codeToLang[language]) && something(aliases.codeToLang[language][code])) {
		//console.log( aliases.codeToLang[language][code] );
		return aliases.codeToLang[language][code];
	}
	return code;
}



// refreshing

var interlinear = {};
interlinear.translations = {};

var verseHistory = {};	
var wordHistory = {};

var state = {
	book:'GEN',
	chap:'1',
	verse:'1',
	word:'בראשית'
};

var history;
var historyIndex;

function initHistory () {
	history = [ state ];
	historyIndex = 0;
}

function backHistory () {

}

function forwardHistory () {

}

const rightArrow = '<span style="font-size:1.5em;height:40px;">&nbsp;&#10093;&nbsp;</span>';
const leftArrow = '<span style="font-size:1.5em;height:40px;">&nbsp;&#10092;&nbsp;</span>';
const upArrow = '<span style="font-size:2em;height:40px;">&nbsp;&#x2B06;&nbsp;</span>';
const downArrow = '<span style="font-size:2em;height:40px;">&nbsp;&#x2B07;&nbsp;</span>';

const missingPlaceholder = '<span style="font-size:1.5em;color:gray;" title="Unable to find content">*</span>';

// language variables
var languageStrongs = 'English KJV';
var language;

function changeLanguages () {
	//var prevCode = langToCode( getVal( "book" ) );
	language = getVal( "lang" );
	
	changeBookSelect();
	setVal( "book", state.book );
	
	refresh();
}

function changeBookSelect () {
	var bookSelect = getId( "book" );
	bookSelect.innerHTML = "";
	//setVal( "book", codeToLang( state.book ) );
	for (let book of Object.keys(interlinear.text)) {
		const option = document.createElement('option');
		option.value = book;
		option.text = codeToLang( book );
		bookSelect.add( option );
	}
}

function getQuery () {
	var search = location.search.substring(1);
	if (something(search)) {
		console.log( "Search: "+search );
		var query = search.split( '&' );
		for (let keyval of Object.values(query)) {
			tuple = keyval.split( '=' );
			state[tuple[0]] = decodeURI(tuple[1]);
		}
		setVal( "book", state.book );
		setVal( "chap", state.chap );
		setVal( "verse", state.verse );
		setVal( "word", state.word );
	}
	
}

function loadingProgress ( prog ) {
	setVal( "loading", prog );
}

function changeBook ( book ) {
	changeVerse( book, 1, 1 );
}

function changeVerse ( book, chap, verse ) {
	//console.log( 'changeVerse: '+book+','+chap+','+verse );
	state.book = book;
	state.chap = chap;
	state.verse = verse;

	refresh();
}

function changeWord ( word ) {
	state.word = basicWord( word );
	
	refresh();
}

function refresh () {
	console.log( "state:" );
	console.log( state );

	setVal( "book", state.book );
	setVal( "chap", state.chap );
	setVal( "verse", state.verse );		
	setVal( "word", state.word );

	// verse history
	verseHistory[state.book+state.chap] = { book:state.book, chap:state.chap, verse:state.verse };
	
	// word history
	wordHistory[basicWord(state.word)] = null;
	
	// refresh divs
	getId( "verseDiv" ).innerHTML = verseInfo( state.book, state.chap, state.verse );
	getId( "wordDiv" ).innerHTML = wordInfo( state.word );
	
	// page history
	history.replaceState(
		{ book:state.book, chap:state.chap, verse:state.verse, word:state.word, view:state.view },
		state.book+' '+state.chap+':'+state.verse+' ('+state.word+')',
		'?book='+state.book+'&chap='+state.chap+'&verse='+state.verse+'&view='+state.view+'&word='+state.word
	);
	
	if (something(state.view) && something( state.view ) && something(getId( state.view ))) getId( state.view ).scrollIntoView();
}



