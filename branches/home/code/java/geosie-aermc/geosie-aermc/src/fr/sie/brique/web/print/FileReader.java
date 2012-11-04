package fr.sie.brique.web.print;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Reads File.
 * 
 * @author Francois Tertre (BRGM - STI/AD)
 * @version $Id: FileReader.java,v 1.1 2010/10/28 11:05:22 quique Exp $
 */
public class FileReader {

	/**
	 * Reads File content and puts it into a String.
	 * 
	 * @param fileName
	 *            Name of file.
	 * @return Content of File into a String.
	 * @throws java.io.IOException
	 */
	public static String read(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileName), "UTF8"));
		String result = "";
		String line;
		while ((line = br.readLine()) != null) {
			result += line + System.getProperty("line.separator");
		}
		return result;
	}

	/**
	 * Reads InputStream content and puts it into a String.
	 * 
	 * @param is
	 *            InputStream to read.
	 * @return Content of InputStream into a String.
	 * @throws java.io.IOException
	 */
	public static String read(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(
				new InputStreamReader(is, "UTF8"));
		String result = "";
		String line;
		while ((line = br.readLine()) != null) {
			result += line + System.getProperty("line.separator");
		}
		return result;
	}

}
