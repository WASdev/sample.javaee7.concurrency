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

import java.util.Arrays;
import java.util.concurrent.Callable;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

/**
 * Increment (or create) entries in the database that track items sold together.
 */
public class SoldTogetherUpdateTask implements Callable<Void> {
    private final String[] productIds;

    public SoldTogetherUpdateTask(String... productIds) {
        this.productIds = productIds;
    }

    @Override
    public Void call() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, NamingException, NotSupportedException, RollbackException, SecurityException, SystemException {
        UserTransaction tran = InitialContext.doLookup("java:comp/UserTransaction");
        EntityManager em = InitialContext.doLookup("java:comp/env/persistence/ee7-concurrency-sample");

        for (int i = 0; i < productIds.length - 1; i++)
            for (int j = i + 1; j < productIds.length; j++) {
                SoldTogether s = new SoldTogether();
                s.setProductIds(productIds[i], productIds[j]);

                boolean updated;
                tran.begin();
                try {
                    Query query = em.createQuery("UPDATE SoldTogether s SET s.count=s.count+1 WHERE s.productIds=:p");
                    query.setParameter("p", s.getProductIds());
                    updated = query.executeUpdate() >= 1;
                } finally {
                    tran.commit();
                }

                if (!updated) {
                    boolean created = true;
                    tran.begin();
                    try {
                        s.setCount(1);
                        em.persist(s);
                        em.flush();
                    } catch (PersistenceException x) {
                        created = false;
                    } finally {
                        if (created)
                            tran.commit();
                        else {
                            tran.rollback();
                            tran.begin();
                            try {
                                Query query = em.createQuery("UPDATE SoldTogether s SET s.count=s.count+1 WHERE s.productIds=:p");
                                query.setParameter("p", s.getProductIds());
                                query.executeUpdate();
                            } finally {
                                tran.commit();
                            }
                        }
                    }
                }
            }
        System.out.println("Items sold together: " + Arrays.asList(productIds));
        return null;
    }
}