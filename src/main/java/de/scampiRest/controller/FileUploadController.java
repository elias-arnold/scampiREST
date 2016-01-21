package de.scampiRest.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import de.scampiRest.applib.ScampiCommunicator;
import de.scampiRest.data.RestScampiMessage;
import de.scampiRest.data.RestScampiMessageRepository;
import de.scampiRest.exception.KeyNotValid;
import de.scampiRest.exception.NoMessageIdFound;
import de.scampiRest.exception.UploadError;

@Controller
public class FileUploadController {
	
	@Value("${multipart.location}")
	private String filePath;
	
	@Autowired private ScampiCommunicator scampiCommunicator;
	@Autowired private RestScampiMessageRepository restScampiMessageRepository;
	
    @RequestMapping(value="/upload", method=RequestMethod.GET)
    public @ResponseBody String provideUploadInfo() {
        return "You can upload a file by posting to this same URL.";
    }

	@RequestMapping(value="/upload", method=RequestMethod.POST)
    public @ResponseBody TreeMap<String, String> handleFileUpload(@RequestParam("id") String id,
    		@RequestParam("key") String key,
            @RequestParam("file") MultipartFile file) throws Exception{
    	
    	if (key.contains(".")){
    		throw new KeyNotValid("Key must not contains dots!");
    	}
    	
        if (!file.isEmpty()) {
            try {
            	String name = file.getOriginalFilename();
                byte[] bytes = file.getBytes();
                File temporaryFile = new File(filePath + "/" + name);
                BufferedOutputStream stream =
                        new BufferedOutputStream(new FileOutputStream(temporaryFile));
                stream.write(bytes);
                stream.close();
                
                RestScampiMessage restScampiMessage = restScampiMessageRepository.findOne(id);
                if (restScampiMessage != null){
                	if (key == null){
                		restScampiMessage.addNewBinary(temporaryFile, name, "");
                	} else {
                		restScampiMessage.addNewBinary(temporaryFile, name, key);
                	}
                	
                	restScampiMessageRepository.save(restScampiMessage);
                } else {
                	throw new NoMessageIdFound("The id of the scampi message was not found in the mongo database."); // The id was not found in the database
                }
                TreeMap<String, String> tm = new TreeMap<String, String>();
    			tm.put("id", restScampiMessage.getAppTag());
                return tm;
            } catch (Exception e) {
                throw e;
            }
        } else {
            throw new UploadError("File was empty");
        }
    }

}
