Error Listener Plugin for JMeter,v1.0
by Mindtree Ltd.
6th Dec 2012
Contributors : TestLabs(testlabs@mindtree.com)

License: Apache License, Version 2.0 and Mindtree Copyright 

Contents:

I.   Description of Purpose
II.  Minimum Requirements
III. How to deploy the plugin
IV.  Usage


I.   Description of Purpose

The purpose of developing Error listener plugin is to capture error information and the instantaneous state of JMeter during performance testing for an in depth analysis. Error Listener provides additional features, flexibility and control over JMeter state.
It addresses the following concerns:

- When the error occurs, the existing (Save Responses to File) listener simply logs the response to file from the perspective  of errors. The number of files corresponds to number of errors even if the response is same. Hence, too many files to deal   with
- Also, the state of JMeter (variables-properties) is not logged or stored to a specific file during the time of error.   Hence, as a performance tester to analyze the JMeter state together with the error response files becomes difficult     considering the time available
- In order to gather JMeter state (properties/variables), they may be added explicitly as a part of the file name prefix.   This provides the state values on the error response file name. But, as the number of properties to monitor increases, it     becomes difficult for the testers to use and consolidate the properties based on just the filename

II.  Minimum Requirements

You need to run JMeter 2.4 or above with a JRE 1.6 or above to use the plugin
 
III. How to deploy the plugin

Copy the ErrorListener.jar file inside JMETER_INSTALL_DIR/lib/ and JMETER_INSTALL_DIR/lib/ext directories.
Now you can restart JMeter and view Error Listener in Listenerís group.

IV. Usage

Refer the doc ~/doc/InstallationAndUsage.doc or ErrorListener-Usage.jpg


Copyright 2012 Mindtree Ltd.













