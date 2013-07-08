package com.gentics.cr.template;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.gentics.cr.exceptions.CRException;
import com.gentics.cr.util.Constants;

/**
 * loads a template from a file usint an input stream.
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class FileTemplate implements ITemplate {

	/**
	 * Template source as String.
	 */
	private String source;
	/**
	 * Key identifying this template.
	 */
	private String key;
	/**
	 * The file 
	 */
	private File file;
	
	/**
	 * Flag indicating if the {@link org.apache.velocity.runtime.resource.loader.FileResourceLoader} can be used
	 */
	private boolean isFileResourceLoaderUsable = false;
	
	/**
	 * indicates if {@link org.apache.velocity.runtime.resource.loader.FileResourceLoader} can be used
	 * @return true when the {@link org.apache.velocity.runtime.resource.loader.FileResourceLoader} can be used
	 */
	public boolean isFileResourceLoaderUsable() {
		return isFileResourceLoaderUsable;
	}

	/**
	 * gets the key of the template. usually a md5 hash
	 * or the absolute path to the template file when usesFileResourceLoader() is true
	 * @return key
	 */
	public final String getKey() {
		return key;
	}
	
	/**
	 * gets the key of the template. usually a md5 hash
	 * or the absolute path to the template file when usesFileResourceLoader() is true
	 * @return key
	 */
	public final String getKey(boolean useFileResourceLoader) {
		if (useFileResourceLoader) {
			return file.getPath();
		} 
		return this.getKey();
	}
	/**
	 * @return source of the template.
	 */
	public final String getSource() throws CRException {
		/*
		 * if source is empty and file exists: try to read source from the File
		 */
		if(source == null && file != null) {
			readSource(file);
		}
		return source;
	}

	/**
	 * Creates a new instance of FileTemplate.
	 * 
	 * @param filename the path to file witch contains the template
	 */
	public FileTemplate(final String filename) {
		this.isFileResourceLoaderUsable = true;
		this.file = new File(filename);
		this.key = file.getAbsolutePath();
	}
	/**
	 * Creates a new instance of FileTemplate.
	 * 
	 * @param file the File witch contains the template 
	 */
	public FileTemplate(final File file) {
		this.isFileResourceLoaderUsable = true;
		this.key = file.getAbsolutePath();
		this.file = file;
	}
	/**
	 * Creates a new instance of FileTemplate.
	 * @param stream - stream with the template code
	 * @throws CRException when we cannot read the stream or there was an error
	 * generating the md5sum of the stream.
	 */
	public FileTemplate(final InputStream stream) throws CRException {
		this.isFileResourceLoaderUsable = false;
		readSource(stream);
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(this.source.getBytes());
			this.key = new String(digest.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new CRException(e);
		}
	}

	/**
	 * Creates a new instance of FileTemplate. (reads the file and generates a md5 key).
	 * @param streamReader to read the template code
	 * @throws CRException when we cannot read the stream or there was an error
	 * generating the md5sum of the stream.
	 */
	public FileTemplate(final BufferedReader streamReader) throws CRException {
		this.isFileResourceLoaderUsable = false;
		readSource(streamReader);
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(this.source.getBytes());
			this.key = new String(digest.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new CRException(e);
		}
	}
	/**
	 * Reads the contents of the provided file into the template source
	 * 
	 * @param file - the file to read
	 * @throws CRException when the file could not be found or not read
	 */
	private void readSource(File file) throws CRException {
		try {
			readSource(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new CRException(e);
		}
	}

	/**
	 * Reads the given stream into the template source.
	 * @param stream - stream to read
	 * @throws CRException when the stream cannot be read
	 */
	private void readSource(final InputStream stream) throws CRException {
		try {
			this.source = slurp(stream);
		} catch (IOException e) {
			throw new CRException(e);
		}
	}

	/**
	 * Reads the content from the reader into the template source.
	 * @param reader reader to read from
	 * @throws CRException when the reader cannot access the content
	 */
	private void readSource(final BufferedReader reader) throws CRException {
		try {
			this.source = slurp(reader);
		} catch (IOException e) {
			throw new CRException(e);
		}
	}

	/**
	 * Creates a new instance of FileTemplate.
	 * @param stream - stream with the template code
	 * @param file - file used to generate the key from the filename
	 * @throws CRException when we cannot read the stream or there was an error
	 * generating the md5sum of the stream.
	 */
	public FileTemplate(final FileInputStream stream, final File file) throws CRException {
		readSource(stream);
		this.key = file.getAbsolutePath();
	}

	/**
	 * Read a String from the given InputStream.
	 * @param in - stream to read from
	 * @return String with the contents read from the stream
	 * @throws IOException when the stream cannot be read
	 */
	private static String slurp(final InputStream in) throws IOException {
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[Constants.KILOBYTE];
		int n;
		while ((n = in.read(b)) != -1) {
			out.append(new String(b, 0, n));
		}
		return out.toString();
	}

	/**
	 * Read a String from the given BufferedReader.
	 * @param reader Reader to read the content from
	 * @return String with the contents read from the reader.
	 * @throws IOException when the reader cannot read the content
	 */
	private static String slurp(final BufferedReader reader) throws IOException {
		StringBuffer out = new StringBuffer();
		String line = "";
		while ((line = reader.readLine()) != null) {
			out.append(line);
			out.append(System.getProperty("line.separator"));
		}
		return out.toString();
	}

}
