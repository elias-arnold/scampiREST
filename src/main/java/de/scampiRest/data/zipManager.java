package de.scampiRest.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class zipManager {

	List<String> fileList;

	public static void unZip(String zipFile, String outputFolder) {

		try {
			ZipFile zFile = new ZipFile(new File(zipFile));

			Enumeration<? extends ZipEntry> entries = zFile.entries();

			while (entries.hasMoreElements()) {
				ZipEntry zipEntry = entries.nextElement();

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Unzip it
	 * @param zipFile input zip file
	 * @param output zip file output folder
	 */
	public static void unZipIt(String zipFile, String outputFolder){


		if (!outputFolder.endsWith("/")){
			outputFolder = outputFolder + "/";
		}
		byte[] buffer = new byte[1024];

		try{

			//create output directory is not exists
			File folder = new File(outputFolder);
			if(!folder.exists()){
				folder.mkdir();
			}

			//get the zip file content
			//ZipInputStream zis = 
			// 	new ZipInputStream(new FileInputStream(zipFile));


			ZipFile zFile = new ZipFile(new File(zipFile));

			Enumeration<? extends ZipEntry> entries = zFile.entries();

			while (entries.hasMoreElements()) {
				ZipEntry ze = entries.nextElement();



				//get the zipped file list entry

				//ZipEntry ze = zis.getNextEntry();
				if (ze.isDirectory()){
					File newEntryFolder = new File(outputFolder + ze.getName());
					if (!newEntryFolder.exists()){
						newEntryFolder.mkdirs();
					}
				} else {

					String fileName = ze.getName();
					File newFile = new File(outputFolder + File.separator + fileName);

					System.out.println("file unzip : "+ newFile.getAbsoluteFile());

					//create all non exists folders
					//else you will hit FileNotFoundException for compressed folder
					new File(newFile.getParent()).mkdirs();

					FileOutputStream fos = new FileOutputStream(newFile);             
					
					InputStream zis = zFile.getInputStream(ze);
					
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}

					fos.close();   
				}
			}
			// zis.closeEntry();
			// zis.close();
		// }	
		System.out.println("Done");

	}catch(

			IOException ex)

	{
		ex.printStackTrace();
	}
}}
