package com.gentics.cr.template;

import com.gentics.cr.exceptions.CRException;

/**
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public interface ITemplate {
	/**
	 * gets the key of the template. usually a md5 hash
	 * @return key
	 */
	public String getKey();
	public String getKey(boolean useFileResourceLoader);

	/**
	 * gets the source of the template
	 * @return source
	 * @throws CRException when the source could not be read
	 */
	public String getSource() throws CRException;
	
	/**
	 * Check if the template uses the Velocity File ResourceLoader
	 */
	public boolean isFileResourceLoaderUsable();
}
