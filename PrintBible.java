package bibletext;

import java.util.*;
import creek.*;

public class PrintBible {

	Bible bible;
	Map<String,String> bookNames;

	public PrintBible ( Bible bible ) {
		this.bible = bible;
		bookNames = new HashMap<>();
		for (String name : bible.aliases().keySet()) {
			String abrev = bible.aliases().get(name);
			if (!bookNames.containsKey(abrev)) bookNames.put( abrev, name ); // if abrev isn't there yet, put it there
			else if (name.length() > bookNames.get(abrev).length()) bookNames.put( abrev, name ); // else if it's the longer name, then use it instead
		}
	}
	
	public String html ( String title, String min, String holePunch ) {
		StringBuilder html = new StringBuilder();
		html.append(
			"<!DOCTYPE html>\n<html>\n<head>\n<title>"+title+"</title>\n<meta charset='utf-8' />\n"+
			"<style type=\"text/css\">\n"+
			"body{font-size:8px;font-family:sans-serif;border:0;padding:0;margin:2px;}\n"+
			"table{border-collapse:collapse;/*break-before:page;*/margin-bottom:32px;}\n"+ // now disabling break-before, since it adds roughly 50% extra pages
			"th,td{border:0;padding:0;}"+
			"th{font-size:10px;}"+
			//"thead{border-bottom:solid #002080 2px;}"+
			".book{border-bottom:solid black 1px;}"+
			"@page{margin:"+min+";}"+ // default
			"</style>\n"+
			"<style type=\"text/css\" media=\"print\">"+
			"#controls{display:none;}"+
			"</style>"+
			"</head>\n<body id=\"body\">\n"+
			"<div id=\"controls\" style=\"background-color:gray;font-size:12px;padding:4px;margin-bottom:4px;color:white;font-weight:bold;\">"+
			//"Font size: <input value='8' onchange=\"document.getElementById('body').style['font-size']=this.value+'px'\" style=\"font-size:12px;width:16px;\">px&nbsp;&nbsp;&nbsp;"+
			"Font size: <input value='8' onchange=\"setFontSize(this.value);\" style=\"font-size:12px;width:16px;\">px"+
			"&nbsp;&nbsp; <select id=\"margins-select\" onchange=\"selectMargins(this.value);\">"+
				"<option value=\"inkjet\">Basic margins</option>"+
				"<option value=\"zero\">Zero margins</option>"+
				"<option value=\"holeSingle\">Hole punch (single sided)</option>"+
				"<option value=\"holeDouble\">Hole punch (double sided)</option>"+
			"</select>\n"+
			"&nbsp;&nbsp; Margin: <input value='0.4cm' id=\"min-margin\" onchange=\"selectMargins(document.getElementById('margins-select').value);\" style=\"font-size:12px;width:32px;\">"+
			" Hole punch: <input value='1.5cm' id=\"punch-margin\" onchange=\"selectMargins(document.getElementById('margins-select').value);\" style=\"font-size:12px;width:32px;\">"+
			"&nbsp;&nbsp; <input type='button' value=\"&#x1F5B6; Print\" onclick='print()'>\n"+
			"</div>\n"+
			"<script>\n"+
			"function setFontSize(size) {"+
				"var larger = Number(size)+2;"+
				"const style=document.createElement('style');"+
				"style.innerHTML=`body{font-size:${size}px;}th{font-size:${larger}px;}`;"+
				"document.head.appendChild(style);"+
			"}\n"+
			"function selectMargins(type) {"+
				"var min = document.getElementById('min-margin').value; console.log('min: '+min);"+
				"var holePunch = document.getElementById('punch-margin').value; console.log('holePunch: '+holePunch);"+
				"const style=document.createElement('style');"+
				"if      (type==='inkjet')     {style.innerHTML='@page{margin:'+min+';}';}"+
				"else if (type==='zero')       {style.innerHTML='@page{margin:0cm;}';}"+
				"else if (type==='holeSingle') {style.innerHTML='@page{margin:'+min+' '+min+' '+min+' '+holePunch+';}';}"+
				"else if (type==='holeDouble') {style.innerHTML='@page:left{margin:'+min+' '+holePunch+' '+min+' '+min+';}@page:right{margin:'+min+' '+min+' '+min+' '+holePunch+';}';}"+
				"document.head.appendChild(style);"+
			"}\n"+
			"</script>\n"
		);
		Tree books = bible.text();
		for (String book : books.keys()) {
			html.append( "<table>\n<thead>\n<tr><th class='book'>" ).append( bookNames.get(book) ).append( "</th></tr>\n</thead>\n<tbody>\n" );
			Tree chaps = books.get(book);
			for (String chap : chaps.keys()) {
				StringBuilder chapterText = new StringBuilder();
				Tree verses = chaps.get(chap);
				for (String verse : verses.keys()) {
					String text = verses.get(verse).value();
					chapterText.append( "<b>" ).append( verse ).append( "</b>" ).append( " " ).append( text ).append( " " );
				}
				html.append( "<tr><th>" ).append( chap ).append( "</th></tr><tr><td>" ).append( chapterText ).append( "</td></tr>\n" );
			}
			html.append( "</tbody>\n</table>" );
		}
		html.append( "</body>\n</html>\n" );
		return html.toString();
	}
	
	public void printHtml ( String path ) throws Exception {
		FileActions.write( path, html( FileActions.minName( path ), "0.4cm", "1.5cm" ) );
	}
	
	public static void main ( String[] args ) throws Exception {
		new PrintBible( new EBibleOrgText().load( args[0] ) ).printHtml( args[1] );
	}
	
}
