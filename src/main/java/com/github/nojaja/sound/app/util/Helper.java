package com.github.nojaja.sound.app.util;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;


public class Helper {

	public static URL getURLFromFileName(String filename) {
		try {
			char sep = File.separator.charAt(0);
			String file = filename.replace(sep, '/');
			if ((file.charAt(0) != '/')&&(file.charAt(1) != ':')) {
				String dir = System.getProperty("user.dir");
				dir = dir.replace(sep, '/') + '/';
				if (dir.charAt(0) != '/') {
					dir = "/" + dir;
				}
				file = dir + file;
			}
			return (new URL("file", "", file));
		} catch (MalformedURLException e) {
			throw (new InternalError("can't convert from filename"));
		}
	}

}
