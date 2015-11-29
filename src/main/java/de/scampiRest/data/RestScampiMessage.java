package de.scampiRest.data;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.scampiRest.applib.ScampiCommunicator;
import fi.tkk.netlab.dtn.scampi.applib.ApiException;
import fi.tkk.netlab.dtn.scampi.applib.SCAMPIMessage;
import fi.tkk.netlab.dtn.scampi.applib.impl.message.BufferContentType;
import fi.tkk.netlab.dtn.scampi.applib.impl.message.ContentType;
import fi.tkk.netlab.dtn.scampi.applib.impl.message.FloatContentType;
import fi.tkk.netlab.dtn.scampi.applib.impl.message.IntegerContentType;
import fi.tkk.netlab.dtn.scampi.applib.impl.message.UTF8ContentType;

@Document
public class RestScampiMessage {

	@Transient @JsonIgnore private static final Logger logger = LoggerFactory.getLogger(RestScampiMessage.class);
	@Transient @JsonIgnore private String storagePath;
	
	@Id
	private String id;
	
	private String appTag = "";
	private String service = "";
	
	private TreeMap<String, String> stringMap = new TreeMap<String, String>();
	private TreeMap<String, Long> integerMap = new TreeMap<String, Long>();
	private TreeMap<String, Double> floatMap = new TreeMap<String, Double>();
	private TreeMap<String, String> binaryMap = new TreeMap<String, String>();
	
	private Map<String, Map<String, Object>> metaData = new TreeMap<String, Map<String, Object>>();
	
	public RestScampiMessage(){
		this.storagePath = ScampiCommunicator.getSelf().getStoragePath();
	}
	
	public RestScampiMessage( SCAMPIMessage message, String service) {
		Collection<ContentType> cont = message.getContent();
		this.appTag = message.getAppTag();
		this.service = service;
		this.storagePath = ScampiCommunicator.getSelf().getStoragePath();
		
		for (ContentType contentType : cont) {
			
			if (contentType instanceof UTF8ContentType){
				// String
				stringMap.put(contentType.name, ((UTF8ContentType) contentType).string);
			} else if (contentType instanceof BufferContentType){
				// BIN
				File fileForBinary = new File(getFullPath(""));
				try {
					fileForBinary.mkdirs();
					// fileForBinary = new File(getFullPath(contentType.name + ".zip"));
					// fileForBinary.createNewFile();
// 					message.moveBinary(contentType.name + ".zip", fileForBinary);
					binaryMap.put(contentType.name, getPath(contentType.name));
				} catch (ApiException e) {
					logger.error(getFullPath(contentType.name) + " not stored", e);
					binaryMap.put(contentType.name, "");
				}
				
			} else if (contentType instanceof FloatContentType){
				// Double
				floatMap.put(contentType.name, ((FloatContentType) contentType).value);
			} else if (contentType instanceof IntegerContentType){
				// Long
				integerMap.put(contentType.name, ((IntegerContentType) contentType).value);
			}
		} // FOR
		
		metaData = message.getMetadata();
	}
	
	public SCAMPIMessage writeSCAMPIMessage(){
		SCAMPIMessage message = ScampiCommunicator.getMessage(getAppTag());
		
		for (String name : stringMap.keySet()) {
			message.putString(name, stringMap.get(name));
		}
		for (String name : integerMap.keySet()) {
			message.putInteger(name, integerMap.get(name));
		}
		for (String name : floatMap.keySet()) {
			message.putFloat(name, floatMap.get(name));
		}
		for (String name : binaryMap.keySet()) {
			File value = new File(getFullPath(name));
			message.putBinary(name, value);
		}
		try {
			message.setMetadata(getMetaData());
		} catch (Exception e) {
			logger.debug("setMetodata not working", e);
		}
		
		return message;
	}
	
	private String getPath(String name){
		return this.getService() + "/" + this.getAppTag() + "/" + name;
	}
	
	private String getFullPath(String name){
		return this.storagePath + "/" + this.getPath(name);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public Map<String, Map<String, Object>> getMetaData() {
		return metaData;
	}

	public void setMetaData(Map<String, Map<String, Object>> metaData) {
		this.metaData = metaData;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	
}