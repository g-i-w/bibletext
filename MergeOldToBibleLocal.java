package bibletext;

import java.util.*;
import java.io.*;
import java.nio.file.Files;
import paddle.*;
import creek.*;

public class MergeOldToBibleLocal extends EBibleToBibleLocal {

	private FileTree oldTree;

	public MergeOldToBibleLocal ( String oldDir, String newDir, boolean ignoreErrors ) throws Exception {
		super( newDir, ignoreErrors );
		oldTree = new FilesystemTree( oldDir );
	}
	
	public void merge () throws Exception {
		for (Tree lang : oldTree.branches()) {
			FileTree epub = (FileTree) lang.get("epub");
			if (epub!=null) {
				for (String name : epub.keys()) {
					String code = name.substring( 0, name.length()-5 );
					Files.move( new File( epub.file(), name ).toPath(), new File( targetDir( "epub", code ).file(), name ).toPath() );
				}
			}
			FileTree html = (FileTree) lang.get("html");
			if (html!=null) {
				for (String code : html.keys()) {
					Files.move( new File( html.file(), code ).toPath(), targetDir( "html", code ).file().toPath() );
				}
			}
			FileTree text = (FileTree) lang.get("text");
			if (text!=null) {
				for (String code : text.keys()) {
					Files.move( new File( text.file(), code ).toPath(), targetDir( "text", code ).file().toPath() );
				}
			}
		}
	}
	
	public static void main ( String[] args ) throws Exception {
		MergeOldToBibleLocal m = new MergeOldToBibleLocal( args[0], args[1], false );
		m.merge();
	}

}
