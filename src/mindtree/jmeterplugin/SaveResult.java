/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package mindtree.jmeterplugin;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Save the SampleErrorResponse and Jmeter State in files 
 * 
 */
public class SaveResult {

	/**Holds the property name and value */
	static StringBuilder propertyHeader = new StringBuilder(1000);
	static StringBuilder propertyValue = new StringBuilder(1000);

	/**Holds the error and property filename*/
	private String propFileName=null;
	private String errorFileName=null;

	/**Holds the string to be logged into property file*/
	private String JPropertiesString=null;

	/**Holds the hash value of error response*/
	private static String errorResponseHashValue=null;

	/**Holds the variable name which contains the Error file name*/
	String variable =null;

	/**
	 * @param s SampleErrorResult to save
	 * @param num number to append to variable (if >0)
	 */
	public void SaveSampleResult(SampleResult s, int num)
	{
		// Should we save the sample?
		if (s.isSuccessful()){
			if (ErrorListener.ERRORS_ONLY){
				return;
			}
		} else {
			if (ErrorListener.SUCCESS_ONLY){
				return;
			}
		}

		/**Log Error Response*/	
		if(ErrorListener.ERRORSANDPROPS)
		{
			LogErrorResponse(s,num);
		}

		/**Log Properties to csv file*/	

		// Format the string to log into the property log file in csv format
		JPropertiesString=FormatString(s);

		// Get the Property Log file name    
		propFileName=ErrorListener.PROPFILE;
		//propFileName=GetPropertyFileName(s);

		// Write the Jmeter Properties data to file
		ErrorListenerLogger logtocsv=new ErrorListenerLogger(propFileName,true);
		try {
			logtocsv.logtocsv(JPropertiesString);
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	/**
	 * @return fileName composed of fixed prefix, thread number,timestamp in human readable format
	 * and milliseconds.A suffix derived from the contentType 
	 * e.g. Content-Type:text/html;charset=ISO-8859-1
	 */
	private String makeErrorFileName(String contentType,SampleResult s)
	{
		StringBuilder sb = new StringBuilder(ErrorListener.BASEPATH);
		sb.append(ErrorListener.ERRORFILE+"_"+s.getThreadName()+"_"+"TimeStamp-"+ConvertTimeStampToHMS(s.getTimeStamp())+"_"+s.getTimeStamp());

		if(null!=ErrorListener.FILEEXT && !ErrorListener.FILEEXT.isEmpty())
		{
			sb.append('.');
			sb.append(ErrorListener.FILEEXT);
		}
		else
		{
			if (!ErrorListener.SKIP_SUFFIX){
				sb.append('.');
				if (contentType != null) {
					int i = contentType.indexOf("/"); // $NON-NLS-1$
					if (i != -1) {
						int j = contentType.indexOf(";"); // $NON-NLS-1$
						if (j != -1) {
							sb.append(contentType.substring(i + 1, j));
						} else {
							sb.append(contentType.substring(i + 1));
						}
					} else {
						sb.append("unknown");
						//sb.append(ErrorListener.FILEEXT);
					}
				} else {
					sb.append("unknown");
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Get the current timestamp in huma n readable format
	 * 
	 */
	public static String ConvertTimeStampToHMS(long currenttimestamp)
	{
		long epoch = currenttimestamp/1000;
		String date = new java.text.SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new java.util.Date (epoch*1000));
		return date; 
	}

	/**
	 * Format the string to be logged in the property csv file
	 * 
	 */ 
	public String FormatString(SampleResult s)
	{	
		propertyHeader =new StringBuilder();
		propertyValue=new StringBuilder();

		for(int loop=0; loop<ErrorListener.PropertyCount; loop++)
		{
			if(null!= ErrorListenerGui.propertyHeaderArray[loop] && !ErrorListenerGui.propertyHeaderArray[loop].isEmpty() )
			{
				String propValue=JMeterContextService.getContext().getVariables().get(ErrorListenerGui.propertyHeaderArray[loop]);
				if(null==propValue ||propValue.isEmpty())
				{
					propValue="";
				}
				propertyHeader.append(","+"\""+ErrorListenerGui.propertyHeaderArray[loop].trim()+"\"");
				propertyValue.append(","+"\""+propValue.trim()+"\"");
			}
			else
			{
				continue;
			}
		}

		// for intermediate use
		StringBuilder sb = new StringBuilder(100); // output line buffer

		// Construct the headers
		if(ErrorListener.headersCreated==false)
		{
			if(ErrorListener.ERRORSANDPROPS==true)
			{

				sb.append("\"TimeStamp\""+","+"\"TimeStamp\""+","+"\"ThreadName\""+","+"\"nthErroneousRequest\""+","+"\"Response Code\""+
						","+"\"RequestLabel\""+","+"\"Url\""+","+"\"AgentMachineName\""+","+"\"AgentMachineIp\""+propertyHeader+","+"\"ErrorFileName\"");
				sb.append("\n");
			}
			else
			{
				sb.append("\"TimeStamp\""+","+"\"TimeStamp\""+","+"\"ThreadName\""+","+"\"nthErroneousRequest\""+","+"\"Response Code\""+
						","+"\"RequestLabel\""+","+"\"Url\""+","+"\"AgentMachineName\""+","+"\"AgentMachineIp\""+propertyHeader);
				sb.append("\n");
			}
			ErrorListener.headersCreated=true;
		}
		// Construct the values
		if(ErrorListener.ERRORSANDPROPS==true)
		{
			sb.append("\""+ConvertTimeStampToHMS(s.getTimeStamp())+"\""+","+"\""+s.getTimeStamp()+"\""+","+"\""+s.getThreadName()+"\""+","+"\""+JMeterContextService.getContext().getVariables().getIteration()+"\""+","+
					"\""+s.getResponseCode()+"\""+","+"\""+s.getSampleLabel()+"\""+","+"\""+s.getUrlAsString()+"\""+","+"\""+JMeterUtils.getLocalHostName()+"\""+","+"\""+JMeterUtils.getLocalHostIP()+"\""+propertyValue+","+"\""+s.getResultFileName()+"\"");
			sb.append("\n");
		}
		else
		{
			sb.append("\""+ConvertTimeStampToHMS(s.getTimeStamp())+"\""+","+"\""+s.getTimeStamp()+"\""+","+"\""+s.getThreadName()+"\""+","+"\""+JMeterContextService.getContext().getVariables().getIteration()+"\""+","+
					"\""+s.getResponseCode()+"\""+","+"\""+s.getSampleLabel()+"\""+","+"\""+s.getUrlAsString()+"\""+","+"\""+JMeterUtils.getLocalHostName()+"\""+","+"\""+JMeterUtils.getLocalHostIP()+"\""+propertyValue);
			sb.append("\n");
		}
		return sb.toString();
	}

	/** Log error response to the file*/
	private void LogErrorResponse(SampleResult s,int num)
	{
		/**Create hash value of the response*/
		try {
			errorResponseHashValue = MD5Hash(s.getResponseData());
		}
		catch (NoSuchAlgorithmException e) {	
			System.out.println("Error: " + e.getMessage());
		}

		/**Make error file name*/     
		if(!ErrorListener.ErrorFileSampleMap.containsKey(errorResponseHashValue))
		{
			errorFileName = makeErrorFileName(s.getContentType(),s);
			s.setResultFileName(errorFileName);// Associate sample with file name
			ErrorListener.ErrorFileSampleMap.put(errorResponseHashValue,s.getResultFileName());
			variable = ErrorListener.VARIABLE_NAME;
			if (variable.length()>0){
				if (num > 0) {
					StringBuilder sb = new StringBuilder(variable);
					sb.append(num);
					variable=sb.toString();
				}
				JMeterContextService.getContext().getVariables().put(variable, errorFileName);
			}      

			/**Write the error data to file*/     
			ErrorListenerLogger logger =new ErrorListenerLogger(errorFileName);
			try {
				logger.logBytes(s.getResponseData(),s);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			errorFileName=ErrorListener.ErrorFileSampleMap.get(errorResponseHashValue);
			JMeterContextService.getContext().getVariables().remove(ErrorListener.VARIABLE_NAME);
			variable = ErrorListener.VARIABLE_NAME;
			JMeterContextService.getContext().getVariables().put(variable, errorFileName);
			s.setResultFileName(errorFileName);
		}
	}
	
	/**
	 * MD5 implementation as Hash value 
	 * 
	 * @param a_sDataBytes - a original data as byte[] from String
	 * @return String as Hex value 
	 * @throws NoSuchAlgorithmException 
	 */

	public static String MD5Hash(byte[] dataBytes) throws NoSuchAlgorithmException {
		if( dataBytes == null) return "";

		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(dataBytes);
		byte[] digest = md.digest();

		// convert it to the hexadecimal 
		BigInteger bi = new BigInteger(digest);
		String hash = bi.toString(16);
		if( hash.length() %2 != 0)
		{
			hash = "0"+hash;
		}
		return hash;
	}
}	
