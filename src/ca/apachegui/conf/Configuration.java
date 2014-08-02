package ca.apachegui.conf;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import apache.conf.global.Utils;
import apache.conf.parser.Parser;
import apache.conf.parser.File;
import ca.apachegui.db.Settings;
import ca.apachegui.global.Constants;
import ca.apachegui.global.Utilities;
import ca.apachegui.modules.SharedModuleHandler;
import ca.apachegui.modules.StaticModuleHandler;

public class Configuration {
	private static Logger log = Logger.getLogger(Configuration.class);
	
	/**
	 * Calls apache configuration test command and returns the output
	 * 
	 * @return a String with the apache output
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static String testServerConfiguration() throws IOException, InterruptedException
	{
		log.trace("Configuration.testServerConfiguration called");
		String output="";
		
		if(Utils.isWindows()) {
			String command[] = { "cmd", "/c", Settings.getSetting(Constants.binFile), "-t" };
			output=Utils.RunProcessWithOutput(command);
		} else {
			String command[]={Settings.getSetting(Constants.binFile),"-t"};
			output=Utils.RunProcessWithOutput(command);
		}
		output=output.replaceAll(Constants.newLine, "<br/>");
			
		log.trace("returning " + output);
		return output;
	}

	/**
	 * Searches every file in the configuration directory for the input filter.
	 * 
	 * @param filter - the filter to search for. The filter is a regex.
	 * @param activeFilesFilter - a boolean indicating whether we should only return results in active files.
	 * @param includeCommentsFilter - a boolean indicating whether comments should be included in the results
	 * @return an array of JSON objects with the details of the search result
	 * The results are limited to the value defined by Constants.maximumConfigurationSearchResults.
	 * @throws Exception
	 */
	public static JSONArray searchConfiguration(String filter, boolean activeFilesFilter, boolean includeCommentsFilter) throws Exception
	{
		String confFiles[];
		  
		if(activeFilesFilter) {			
	        String includedFiles[] = new Parser(Settings.getSetting(Constants.confFile),Settings.getSetting(Constants.serverRoot), StaticModuleHandler.getStaticModules(), SharedModuleHandler.getSharedModules()).getActiveConfFileList();
			confFiles = includedFiles;
	        
		} else {
			confFiles = Utils.getFileList(new File(Settings.getSetting(Constants.confDirectory)));
		}
		
		if(!Utils.isWindows()) {
			confFiles = ConfFiles.sanitizeConfFiles(confFiles);
		}
		
		Pattern pattern=Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
		java.util.regex.Matcher patternMatcher = null; 
		
		JSONArray results = new JSONArray();
		FileInputStream fstream;
		DataInputStream in;
		BufferedReader br;
		String strLine;
		JSONObject object;
		int line=1;
		for(String confFile : confFiles)
		{
			if((new File(confFile)).exists())
			{	
				line=1;
				fstream = new FileInputStream(confFile); 
				in = new DataInputStream(fstream);
				br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
				while ((strLine = br.readLine()) != null)   
				{
					patternMatcher = pattern.matcher(strLine); 
    				// Print the content on the console
    				if(patternMatcher.find())
    				{	
    					// Always include the line if comments are allowed,
    					// or if comments arent allowed and the line isnt commented
    					if(includeCommentsFilter || (!includeCommentsFilter && !Parser.isCommentMatch(strLine))) {
    						object = new JSONObject();
    						object.put("path", confFile);
    						object.put("line", line);
    						object.put("content", Utilities.forHTML(strLine));
    						
    						if(results.length()<Constants.maximumConfigurationSearchResults) {
    							results.put(object);
    						} else {
    							break;
    						}        					
    					}
    				}
    				line ++;
				}
				in.close();
			}
		}
		
		return results;
		
	}
}