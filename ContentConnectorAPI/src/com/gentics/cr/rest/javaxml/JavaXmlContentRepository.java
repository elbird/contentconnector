package com.gentics.cr.rest.javaxml;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.gentics.cr.CRError;
import com.gentics.cr.CRException;
import com.gentics.cr.CRResolvableBean;
import com.gentics.cr.rest.ContentRepository;

/**
 * @author Christopher
 *
 * Implementaion of XML representation for a REST contentrepositroy.
 *  
 */
public class JavaXmlContentRepository extends ContentRepository {

	private static final long serialVersionUID = 003433324L;


	public JavaXmlContentRepository(String[] attr) {
		
		super(attr);

		this.setResponseEncoding("UTF-8");
		
	}
	public JavaXmlContentRepository(String[] attr, String encoding) {
		
		super(attr);

		this.setResponseEncoding(encoding);
		
	}
	
	public JavaXmlContentRepository(String[] attr, String encoding, String[] options) {
		
		super(attr,encoding,options);

		//this.setResponseEncoding(encoding);
		
	}
	

	public String getContentType() {
		return "text/xml";
	}
	
	
	public void respondWithError(OutputStream stream,CRException ex, boolean isDebug){

		CRError e = new CRError(ex);
		if(!isDebug)
		{
			e.setStringStackTrace(null);
		}
	
		XMLEncoder enc = new XMLEncoder(new BufferedOutputStream(stream));
		
		enc.writeObject(e);
		
		enc.close();
		
	}
	
	private void preprocessingNoByteArray(Collection<CRResolvableBean> coll)
	{
		Iterator<CRResolvableBean> it = coll.iterator();
		while(it.hasNext())
		{
			CRResolvableBean bean = it.next();
			HashMap<String,Object> attributes = (HashMap<String,Object>)bean.getAttrMap();
			if(attributes.containsKey("binarycontent"))
			{
				String ccr_bin_url="ccr_bin?contentid="+bean.getContentid();
				attributes.remove("binarycontent");
				attributes.put("binarycontenturl",ccr_bin_url);
				bean.setAttrMap(attributes);
			}
			if(!bean.getChildRepository().isEmpty())
			{
				preprocessingNoByteArray(bean.getChildRepository());
			}
		}
	}

	public void toStream(OutputStream stream) throws CRException {
		
		if(this.resolvableColl.isEmpty())
		{
			//No Data Found
			throw new CRException("NoDataFound","Data could not be found.",CRException.ERRORTYPE.NO_DATA_FOUND);
		}
		else
		{
			//Elements found/status ok
			XMLEncoder e = new XMLEncoder(new BufferedOutputStream(stream));
			String[] options = this.getOptionsArray();
			if(options!=null)
			{
				ArrayList<String> optArr = new ArrayList<String>(Arrays.asList(options));
				
				if(optArr.contains("nobytearray"))
				{
					this.preprocessingNoByteArray(this.resolvableColl);
				}
			}
			
			e.writeObject(this.resolvableColl);
			e.close();
		}
		
		
	}

}