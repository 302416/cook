package com.util;

import java.io.BufferedReader;
// import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
// import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
// import java.io.OutputStreamWriter;

public class FileOps {
	public static FileOps instance = new FileOps();

	private FileOps() {
	}

	public String inputFile (String inputFile) throws IOException {
		try {
			InputStream input = new FileInputStream(inputFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\r\n");
			}
			input.close();
			br.close();
			return sb.toString();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			throw e;
		}
	}
	
	public FileOps outputFile (String outputFile, String strContent) throws IOException {
    	File file = new File(outputFile);
    	
    	if(file.exists()){
    		try {
    			file.delete();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}    		
    	}
    	
    	try {
    		file.createNewFile();    		
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	
		try {
			OutputStream os = new FileOutputStream(outputFile);
			os.write(strContent.getBytes());
			os.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			throw e;
		}
		
		return instance;
	}
	
	public FileOps appendFile (String outputFile, String strContent) throws IOException {
    	File file = new File(outputFile);
    	
    	if(!file.exists()){
    		System.out.println("File does not exist! " + outputFile);   		
    	}
    	
//    	BufferedWriter out = null;
    	
		try {
			OutputStream os = new FileOutputStream(outputFile, true);
			os.write(strContent.getBytes());
			os.close();
//			out = new BufferedWriter(new OutputStreamWriter(
//					new FileOutputStream(file, true)));
//					out.write(strContent);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			throw e;
		}
		
		return instance;
	}
		
	public static void main(String[] args) {

	}
	
	
	
}

