package de.scampiRest.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

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
import de.scampiRest.exception.NoMessageIdFound;

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
    public @ResponseBody String handleFileUpload(@RequestParam("id") String id,
    		@RequestParam("name") String name,
            @RequestParam("file") MultipartFile file){
    	
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                File temporaryFile = new File(filePath + "/" + name);
                BufferedOutputStream stream =
                        new BufferedOutputStream(new FileOutputStream(temporaryFile));
                stream.write(bytes);
                stream.close();
                
                RestScampiMessage restScampiMessage = restScampiMessageRepository.findOne(id);
                if (restScampiMessage != null){
                	restScampiMessage.addNewBinary(temporaryFile, name);
                } else {
                	throw new NoMessageIdFound("The id of the scampi message was not found in the mongo database."); // The id was not found in the database
                }
                return "You successfully uploaded " + name + "!";
            } catch (Exception e) {
                return "You failed to upload " + name + " => " + e.getMessage();
            }
        } else {
            return "You failed to upload " + name + " because the file was empty.";
        }
    }

}
