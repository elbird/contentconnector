package com.gentics.cr.template;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apache.velocity.runtime.resource.util.StringResourceRepositoryImpl;

import com.gentics.cr.exceptions.CRException;

/**
 * 
 * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
 * @version $Revision: 545 $
 * @author $Author: supnig@constantinopel.at $
 *
 */
public class VelocityTemplateManager implements ITemplateManager {

	/*
	 * Private simple ITemplate implementation, needed as long
	 * as deprecated method render(String, String) is still used
	 */
	private class BasicTemplate implements ITemplate {

		private String key;
		private String source;
		
		public BasicTemplate(String key, String source) {
			this.key = key;
			this.source = source;
		}
		@Override
		public String getKey() {
			return key;
		}
		public final String getKey(boolean useFileResourceLoader) {
			return this.getKey();
		}

		@Override
		public String getSource() {
			return source;
		}

		@Override
		public boolean isFileResourceLoaderUsable() {
			return false;
		}
		
		
	}
	/**
	 * Log4j Logger.
	 */
	private static Logger log = Logger.getLogger(VelocityTemplateManager.class);
	private String encoding;
	private HashMap<String, Object> objectstoput;

	/**
	 * Templatecache.
	 */
	private HashMap<String, Template> templates;

	/**
	 * Create Instance.
	 * @param encoding
	 */
	public VelocityTemplateManager(final String encoding) {
		this.encoding = encoding;
		this.objectstoput = new HashMap<String, Object>();
		this.templates = new HashMap<String, Template>();
	}

	/**
	 * implements {@link com.gentics.cr.template.ITemplateManager#put(String, Object)}.
	 */
	public void put(final String key, final Object value) {
		if (value != null) {
			this.objectstoput.put(key, value);
		}
	}
	
	/**
	 * implements {@link com.gentics.cr.template.ITemplateManager#render(String, String)}.
	 */
	@Deprecated
	public String render(String templateName, String templateSource) throws CRException {
		return render(new BasicTemplate(templateName, templateSource));
	}

	/**
	 * implements {@link com.gentics.cr.template.ITemplateManager#render(ITemplate)}
	 */
	public String render(ITemplate crTemplate) throws CRException {
		return this.render(crTemplate, this.objectstoput);
	}
	/**
	 * implements {@link com.gentics.cr.template.ITemplateManager#render(ITemplate, HashMap<String, Object>)}
	 */
	public String render(ITemplate crTemplate, HashMap<String, Object> contextObjects) throws CRException {
		String renderedTemplate = null;
		StringResourceRepository rep = null;
		long s1 = System.currentTimeMillis();
		if(!crTemplate.isFileResourceLoaderUsable()) {
			rep = StringResourceLoader.getRepository();
			if (rep == null) {
				rep = new StringResourceRepositoryImpl();
				StringResourceLoader.setRepository(StringResourceLoader.REPOSITORY_NAME_DEFAULT, rep);
			}
			rep.setEncoding(this.encoding);
		}
		try {
			Template template = this.templates.get(crTemplate.getKey(rep == null ? true : false));
			if (template == null) {
				if(rep != null) {				
					rep.putStringResource(crTemplate.getKey(), crTemplate.getSource());
					template = Velocity.getTemplate(crTemplate.getKey());
					rep.removeStringResource(crTemplate.getKey());
				} else {
					template = Velocity.getTemplate(crTemplate.getKey(true));
				}
				this.templates.put(crTemplate.getKey(rep == null ? true : false), template);
			} 
			
			VelocityContext context = new VelocityContext();
			Iterator<String> it = contextObjects.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				context.put(key, contextObjects.get(key));
			}
			StringWriter ret = new StringWriter();
			template.merge(context, ret);
			renderedTemplate = ret.toString();
		} catch (ResourceNotFoundException e) {
			throw new CRException(e);
		} catch (ParseErrorException e) {
			throw new CRException(e);
		} catch (Exception e) {
			throw new CRException(e);
		} finally {
			this.objectstoput = new HashMap<String, Object>();
		}
		if (log.isDebugEnabled()) {
			log.debug("Velocity has been rendered in " + (System.currentTimeMillis() - s1) + "ms");
		}
		return renderedTemplate;
	}

}
