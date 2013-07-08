package com.gentics.cr.template;

import java.util.HashMap;

import com.gentics.cr.exceptions.CRException;

/**
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public interface ITemplateManager {

	/**
	 * Deploy and Object into the Render Context.
	 * @param key
	 * @param value
	 */
	public void put(String key, Object value);
	
	/**
	 * Render the given template into a String.
	 * @param templatename
	 * @param templatesource
	 * @return rendered template as string.
	 * @throws CRException
	 */
	@Deprecated
	public String render(String templatename, String templatesource) throws CRException;
	
	/**
	 * Render the given template into a String
	 * 
	 * @param template the CR Template
	 * @return the rendered template as string
	 */
	public String render(ITemplate template) throws CRException;
	
	
	/**
	 * Render the given template into a String using 
	 * 
	 * @param crTemplate the CR Template
	 * @param contextObjects context obje 
	 * @return the rendered template as string
	 */
	public String render(ITemplate crTemplate, HashMap<String, Object> contextObjects) throws CRException;

}
