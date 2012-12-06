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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.jmeter.samplers.SampleResult;

public class ErrorListenerLogger {

	FileWriter fstream = null;
	BufferedWriter out = null;
	FileOutputStream outbytes = null;
	File bytewriter=null;
	public ErrorListenerLogger()
	{

	}
	public ErrorListenerLogger(String filePath)
	{
		try
		{
			bytewriter = new File(filePath);
			bytewriter.createNewFile();
		}
		catch(Exception ex)
		{
			System.out.println("Error: " + ex.getMessage());
		}
	}

	public ErrorListenerLogger(String filePath,boolean status)
	{
		try
		{		 
			bytewriter = new File(filePath);
			fstream = new FileWriter(filePath,status);
			out = new BufferedWriter(fstream);
		}
		catch(Exception ex)
		{
			System.out.println("Error: " + ex.getMessage());
		}

	}
	
	/** Write error response to  file
	 * @param s SampleErrorResult to save
	 * @param bytestream, the error response data to write
	 */
	public void logBytes(byte[] bytestream,SampleResult s) throws IOException{
		try
		{
			outbytes = new FileOutputStream(bytewriter);
			outbytes.write(bytestream);
			outbytes.flush();

		}
		catch (FileNotFoundException e1) 
		{
			System.out.println("Error creating sample file for " + s.getSampleLabel()+ e1);
			ErrorListener.log.error("Error creating sample file for " + s.getSampleLabel(), e1);
		} catch (IOException e1) {
			System.out.println("Error saving sample " + s.getSampleLabel()+ e1);
			ErrorListener.log.error("Error saving sample " + s.getSampleLabel(), e1);
		} 
		catch(Exception ex)
		{
			System.out.println("Error while saving sample"+ s.getSampleLabel() + ex.getMessage());
		}
		finally {
			outbytes.close();
		}
	}
	/** Write property data  to the file
	 * @param str String,the property data to write
	 */
	public void logtocsv(String str) throws IOException{
		try
		{
			out.write(str);
			out.flush();
		}
		catch(Exception ex)
		{
			System.out.println("Error while logging sample properties" + ex.getMessage());
		}
		finally
		{
			fstream.flush();
			out.close();
			fstream.close();
		}
	}

}

