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
package com.ibm.ws.samples.concurrency;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.enterprise.concurrent.LastExecution;
import javax.enterprise.concurrent.Trigger;

/**
 * Computes the next time to execute a task based on the effectiveness of the previous execution.
 */
public class SoldTogetherTrigger implements Trigger {
    @Override
    public Date getNextRunTime(LastExecution lastExecution, Date taskScheduledTime) {
        // on the first time, run 30 seconds after originally scheduled
        if (lastExecution == null)
            return new Date(taskScheduledTime.getTime() + TimeUnit.SECONDS.toMillis(30));

        // after the first time, schedule from when the task most recently completed
        long next = lastExecution.getRunEnd().getTime();

        // choose a shorter delay if the most recent execution made 3 or more updates, otherwise longer
        int prevResult = (Integer) lastExecution.getResult();
        next += TimeUnit.SECONDS.toMillis(prevResult >= 3 ? 20 : 40);

        return new Date(next);
    }

    @Override
    public boolean skipRun(LastExecution lastExecution, Date scheduledRunTime) {
        return false;
    }
}