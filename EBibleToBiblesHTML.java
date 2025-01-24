package bibletext;

import java.util.*;
import creek.*;

public class EBibleToBiblesHTML {

	public static void main ( String[] args ) throws Exception {
		StringBiulder html = new StringBuilder();
	
		Tree langGroups = new JSON();
		langGroups.deserialize( FileActions.read( args[0] ) );
		
		for (Map.Entry<String,Tree> region : langGroups.map().entries()) {
			html.append( "<div class="content"><h3>"+region.getKey()+"</h3><div class=\"scroll-box\"><table class=\"compact-table\">\n" );
			for (Map.Entry<String,Tree> country : region.getValue().map().entries()) {
				// flag
				String flag = country.getValue().get("flag").value();
				int qty = country.getValue().get("translations").size();
				String flagCell = "<td rowspan="+qty+"><img src='bibles/flags/"+flag+"'><br>"+country.getKey()+"</td>";
				// translations
				for (Map.Entry<String,Tree> translation : country.getValue().get("translations").map().entries()) {
					String code = translation.getValue().get("code").value();
					String dir = translation.getValue().get("dir").value();
					html.append( "<tr>"+flagCell+"<td><a href='bibles/ebible.org/"+dir+"/html/"+code+"/index.htm'>"+translation.getKey()+"</a></td><td><a href='bibles/ebible.org/"+dir+"/epub/"+code+".epub'>eBook</a></td></tr>\n" );
					flagCell = "";
				}
			}
			html.append( "</table></div></div>\n" );
		}
		
		FileActions.write( args[1], html.toString() );
		
	}

}
