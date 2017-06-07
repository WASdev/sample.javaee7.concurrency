<!-- 
 /*
 * COPYRIGHT LICENSE: This information contains sample code provided in source
 * code form. You may copy, modify, and distribute these sample programs in any 
 * form without payment to IBM for the purposes of developing, using, marketing 
 * or distributing application programs conforming to the application programming 
 * interface for the operating platform for which the sample code is written. 
 * 
 * Notwithstanding anything to the contrary, IBM PROVIDES THE SAMPLE SOURCE CODE 
 * ON AN "AS IS" BASIS AND IBM DISCLAIMS ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING, 
 * BUT NOT LIMITED TO, ANY IMPLIED WARRANTIES OR CONDITIONS OF MERCHANTABILITY, 
 * SATISFACTORY QUALITY, FITNESS FOR A PARTICULAR PURPOSE, TITLE, AND ANY WARRANTY OR 
 * CONDITION OF NON-INFRINGEMENT. IBM SHALL NOT BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OR
 * OPERATION OF THE SAMPLE SOURCE CODE. IBM HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS OR MODIFICATIONS TO THE SAMPLE
 * SOURCE CODE.
 * 
 * (C) Copyright IBM Corp. 2016.
 * 
 * All Rights Reserved. Licensed Materials - Property of IBM.  
 */
 
-->


<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta charset="utf-8"></meta>
<title>Concurrency Utilities for Java EE WAS tests</title>
<style>
.frm1{padding: 15px;background-color: #9666af; margin-bottom: 10px;}
.frm2{padding-left: 25px; font-family: Verdana; color: #440055;}
.frm3{padding-left: 25px; font-family: Verdana; font-size: 12px; color: #443355;}
.big{font-size: 26px; color: white;}
.small{font-size: 12px;}
button, select{padding: 5px; padding-left: 20px; padding-right: 20px; margin:10px; width: 270px}
</style>
</head>
<body>
<div class="frm1">
<div class="big"> WAS Java EE 7 Sample - Concurrency Utilities for Java EE 1.0</div>
</div>
<div class="frm2"> 
<div class="small">This application has been tested with Firefox and Chrome </div>
<div class="small"> The source code for this application can be found on: <a href="https://github.com/WASdev/">https://github.com/WASdev/</a> </div>
<div class="frm2"> </div>
</div>
</head>
<body>

<div class="frm3">
		
  <p>This sample demonstrates the use of <i>Concurrency Utilities for Java EE 1.0</i> to run tasks and callbacks asynchronously
  as other processing continues. The sample simulates an online store that keeps track of which products are purchased together
  and uses this information makes recommendations before checkout. This information is recorded on background threads so that the
  order may proceed more quickly. A repeating task runs in the background to analyze the information every 20 to 40 seconds.</p>
  <ul>
    <li>In the Servlet.init method, a ContextService is used to contextualize the callback that is invoked in the
        processing of a stream of lines from a data file which is used to pre-populate the database used by the sample.</li>
    <li>In the Servlet.init method, a ManagedScheduledExecutorService and a Trigger are used to schedule a
        repeating background task that updates a local cache of associations between products that are purchased
        together. The Trigger includes custom business logic that adjusts the delay between executions based
        on the previous execution.</li>
    <li>In the Servlet.doGet method, a ManagedExecutorService is used to submit and obtain Futures for various tasks
        that run in parallel and query the database for details about selected products, recommended products, or all products.
        Later in the method, Future.get is used to obtain the results of the parallel operations.</li>
    <li>In the Servlet.doGet method, a ManagedExecutorService is used to submit a background task that writes to a database
        information about items that are purchased together. It does not wait for a result before returning,
        allowing the customer to see a response sooner.</li>
  </ul>
  <p>
    <a href="ConcurrencySample">Run the sample application</a>
  </p>
  
</div>
  
</body>
</html>