package de.scampiRest.data;

import java.util.Collection;
import java.util.TreeMap;

import fi.tkk.netlab.dtn.scampi.applib.SCAMPIMessage;
import fi.tkk.netlab.dtn.scampi.applib.impl.message.BufferContentType;
import fi.tkk.netlab.dtn.scampi.applib.impl.message.ContentType;
import fi.tkk.netlab.dtn.scampi.applib.impl.message.FloatContentType;
import fi.tkk.netlab.dtn.scampi.applib.impl.message.IntegerContentType;
import fi.tkk.netlab.dtn.scampi.applib.impl.message.UTF8ContentType;

public class RestScampiMessage {

	String appTag = "";
	
	private TreeMap<String, String> stringMap = new TreeMap<String, String>();
	private TreeMap<String, Long> integerMap = new TreeMap<String, Long>();
	private TreeMap<String, Double> floatMap = new TreeMap<String, Double>();
	private TreeMap<String, String> binaryMap = new TreeMap<String, String>();
	
	public RestScampiMessage(){
	}
	
	public RestScampiMessage( SCAMPIMessage message, String service) {
		Collection<ContentType> cont = message.getContent();
		this.appTag = message.getAppTag();
		
		for (ContentType contentType : cont) {
			
			if (contentType instanceof UTF8ContentType){
				// String
				stringMap.put(contentType.name, ((UTF8ContentType) contentType).string);
			} else if (contentType instanceof BufferContentType){
				// BIN
				binaryMap.put(contentType.name, "" + message.getAppTag() + "/" + contentType.name);
			} else if (contentType instanceof FloatContentType){
				// Double
				floatMap.put(contentType.name, ((FloatContentType) contentType).value);
			} else if (contentType instanceof IntegerContentType){
				// Long
				integerMap.put(contentType.name, ((IntegerContentType) contentType).value);
			}
		} // FOR
	}

	public String getAppTag() {
		return appTag;
	}

	public void setAppTag(String appTag) {
		this.appTag = appTag;
	}

	public TreeMap<String, String> getStringMap() {
		return stringMap;
	}

	public void setStringMap(TreeMap<String, String> stringMap) {
		this.stringMap = stringMap;
	}

	public TreeMap<String, Long> getIntegerMap() {
		return integerMap;
	}

	public void setIntegerMap(TreeMap<String, Long> integerMap) {
		this.integerMap = integerMap;
	}

	public TreeMap<String, Double> getFloatMap() {
		return floatMap;
	}

	public void setFloatMap(TreeMap<String, Double> floatMap) {
		this.floatMap = floatMap;
	}

	public TreeMap<String, String> getBinaryMap() {
		return binaryMap;
	}

	public void setBinaryMap(TreeMap<String, String> binaryMap) {
		this.binaryMap = binaryMap;
	}

	
}