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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ContextService;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.SynchronizationType;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

@WebServlet("/ConcurrencySample")
@PersistenceContext(name = "persistence/ee7-concurrency-sample", synchronization = SynchronizationType.SYNCHRONIZED)
public class ConcurrencySampleServlet extends HttpServlet {
    private static final long serialVersionUID = 8503407862186240723L;

    @Resource
    private ContextService contextService;

    @Resource
    private ManagedScheduledExecutorService executor;

    @Resource
    private UserTransaction tran;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Servlet invoked: " + request.getServletPath() + '?' + request.getQueryString());
        String action = request.getParameter("action");
        String[] items = request.getParameterValues("item");

        // Asynchronously load information about all items or selected/recommended items from the database
        Future<List<Item>> allItemsFuture = null;
        List<Future<Item>> selectedFutures = new ArrayList<Future<Item>>();
        List<Future<Item>> recommendedFutures = new ArrayList<Future<Item>>();
        if (items == null || "Check out".equals(action))
            allItemsFuture = executor.submit(new LoadItemsTask());
        else {
            Arrays.sort(items);
            for (String item : items) {
                selectedFutures.add(executor.submit(new LoadItemTask(item)));
                if ("Add to Cart".equals(action)) {
                    SoldTogether recommended = SoldTogetherQueryTask.map.get(item);
                    if (recommended != null)
                        for (String r : recommended.getProductIds().split(","))
                            if (Arrays.binarySearch(items, r) < 0)
                                recommendedFutures.add(executor.submit(new LoadItemTask(r)));
                }
            }
        }

        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset=\"utf-8\"></meta>");
        out.println("<title>Concurrency Utilities for Java EE - Sample Application</title>");
        out.println("<style>");
        out.println(".frm1{padding: 15px;background-color: #9666af; margin-bottom: 10px;}");
        out.println(".frm2{padding-left: 25px; font-family: Verdana; color: #440055;}");
        out.println(".frm3 {padding-left: 25px;	font-family: Verdana; font-size: 12px; 	color: #443355; white-space: nowrap;}");
        out.println(".big{font-size: 26px; color: white;}");
        out.println(".small{font-size: 12px;}");
        out.println("button, select{padding: 5px; padding-left: 20px; padding-right: 20px; margin:10px; width: 270px}");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class=\"frm1\">");
        out.println("<div class=\"big\">WAS Java EE 7 Sample - Concurrency Utilities for Java EE 1.0</div>");
        out.println("</div>");
        out.println("<div class=\"frm2\">");
        out.println("<div class=\"small\">This application has been tested with Firefox and Chrome </div>");
        out.println("<div class=\"small\"> The source code for this application can be found on: <a href=\"https://github.com/WASdev/\">https://github.com/WASdev/</a> </div>");
        out.println("<div class=\"frm2\"> </div>");
        out.println("</div>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class=\"frm3\">");
        if ("Check out".equals(action)) {
            out.println("<p>Thank you for your purchase.</p>");
            if (items.length > 1)
                executor.submit(new SoldTogetherUpdateTask(items));
        }
        out.println(" <form action=" + request.getRequestURL() + " method=\"GET\">");
        out.println("  <table>");
        String newAction;
        try {
            if (!selectedFutures.isEmpty() && "Add to Cart".equals(action)) {
                out.println("    <tr><th colspan=2 align=left>Your Shopping Cart Contains</th></tr>");
                for (Future<Item> future : selectedFutures)
                    print(out, future.get(), false);
                if (!recommendedFutures.isEmpty()) {
                    out.println("    <tr><th colspan=2 align=left>Recommendations</th></tr>");
                    for (Future<Item> future : recommendedFutures)
                        print(out, future.get(), true);
                }
                newAction = "Check out";
            } else {
                out.println("   <tr><th colspan=2 align=left>Select items to add to shopping cart</th></tr>");
                for (Item item : allItemsFuture.get())
                    print(out, item, true);
                newAction = "Add to Cart";
            }
        } catch (ExecutionException | InterruptedException x) {
            x.printStackTrace();
            throw new ServletException(x);
        }
        out.println("   <tr><td colspan=2><input type=\"submit\" name=\"action\" value=\"" + newAction + "\"/></td></tr>");
        out.println("  </table>");
        out.println(" </form>");
        out.println(" <p>");
        out.println(" <a href=\"index.jsp\">Return to the main page</a>");
        out.println(" </p>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }

    /**
     * Initialize the sample data and schedule the periodic task that runs in the background
     * to refresh cached information about items purchased together.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        Consumer<String> insertToDB = new Consumer<String>() {
            @Override
            public void accept(String line) {
                Item item = new Item();
                String[] parts = line.split(",");
                item.setProductId(parts[0]);
                item.setPrice(Float.valueOf(parts[1]));
                item.setName(parts[2]);
                try {
                    tran.begin();
                    try {
                        EntityManager em = InitialContext.doLookup("java:comp/env/persistence/ee7-concurrency-sample");
                        em.persist(item);
                        System.out.println("Inserted " + line);
                    } finally {
                        tran.commit();
                    }
                } catch (Exception x) {
                    x.printStackTrace();
                    throw new RuntimeException(x);
                }
            }
        };

        // Contextualize the callback operation so that it can run on unmanaged threads
        insertToDB = contextService.createContextualProxy(insertToDB, Consumer.class);

        try {
            String root = servletConfig.getServletContext().getResource("/").getPath();
            int index = root.indexOf('!');
            Path path;
            if (index < 0) {
                if (root.indexOf(':') > 0 && root.charAt(0) == '/')
                    root = root.substring(1, root.length());
                path = FileSystems.getDefault().getPath(root, "/WEB-INF/data.txt");
            } else {
                URI uri = new URI("jar:" + root.substring(0, index));
                path = FileSystems.newFileSystem(uri, Collections.<String, Object> emptyMap()).getPath("/WEB-INF/data.txt");
            }
            try (Stream<String> stream = Files.lines(path).parallel()) {
                stream.forEach(insertToDB);
            } finally {
                if (index > 0)
                    path.getFileSystem().close();
            }
        } catch (IOException | URISyntaxException x) {
            x.printStackTrace();
            throw new ServletException(x);
        }

        // Schedule the background task
        executor.schedule(new SoldTogetherQueryTask(), new SoldTogetherTrigger());
    }

    /**
     * Utility method to print a table entry with information about an item
     */
    private void print(PrintWriter out, Item item, boolean checkbox) {
        String type = checkbox ? "checkbox" : "hidden";
        out.println("    <tr><td><input type=\"" + type + "\" name=\"item\" value=\"" +
                    item.getProductId() + "\">" +
                    item.getName() + "</input></td><td>" +
                    item.getPrice() + "</td></tr>");
    }
}
