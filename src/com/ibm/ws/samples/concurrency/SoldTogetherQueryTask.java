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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

/**
 * Periodically scan the database and refresh our locally cached data on items that are purchased together.
 */
public class SoldTogetherQueryTask implements Callable<Integer> {
    /**
     * Map of product id to the item in combination with it is most commonly sold.
     */
    static final ConcurrentHashMap<String, SoldTogether> map = new ConcurrentHashMap<String, SoldTogether>();

    @Override
    public Integer call() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, NamingException, NotSupportedException, RollbackException, SecurityException, SystemException {
        UserTransaction tran = InitialContext.doLookup("java:comp/UserTransaction");
        EntityManager em = InitialContext.doLookup("java:comp/env/persistence/ee7-concurrency-sample");
        List<SoldTogether> results;
        tran.begin();
        try {
            TypedQuery<SoldTogether> query = em.createQuery("SELECT s FROM SoldTogether s", SoldTogether.class);
            results = query.getResultList();
        } finally {
            tran.commit();
        }

        int numUpdates = 0;
        for (SoldTogether s : results)
            for (String id : s.getProductIds().split(",")) {
                SoldTogether prev = map.get(id);
                if (prev == null || s.getCount() > prev.getCount()) {
                    map.put(id, s);
                    numUpdates++;
                }
            }
        System.out.println("Made " + numUpdates + " updates to locally cached 'SoldTogether' data");
        return numUpdates;
    }
}