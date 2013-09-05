package com.samlbest.cs496.finalproject;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;

@SuppressWarnings("serial")
public class UpdateAggregates  extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		//Create set of UPCs (no duplicates)
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query("Product");
		query.addProjection(new PropertyProjection("upc", String.class));
		List<Entity> results = ds.prepare(query).asList(FetchOptions.Builder.withDefaults());
		Set<String> uniqueUpcs = new HashSet<String>();
		
		//Create hashset from list of all products
		for (Entity entity : results) {
			uniqueUpcs.add(entity.getProperty("upc").toString());
		}
		
		//Update average for each
		for (String upc : uniqueUpcs) {
			updateAverage(upc);
		}
	}
		
	private void updateAverage(String upc) {
		Query query = new Query("Product");
		query.setFilter(new Query.FilterPredicate("upc", Query.FilterOperator.EQUAL, upc));
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		List<Entity> results = ds.prepare(query).asList(FetchOptions.Builder.withDefaults());
		int count = results.size();
		Float sum = 0.0f;
		 
		//Calculate sum of all prices
        for (Entity entity : results) {
        	sum += Float.parseFloat(entity.getProperty("price").toString());
        }
        
        //Create new entity (need all these to be compatible with CloudEntity)
        Entity avg = new Entity("Aggregate", upc.trim());
        avg.setProperty("_createdAt", new Date());
        avg.setProperty("_createdBy", "cron");
        avg.setProperty("_kindName", "Aggregate");
        avg.setProperty("_owner", "cron");
        avg.setProperty("_updatedAt", new Date());
        avg.setProperty("_updatedBy", "cron");
        avg.setProperty("averagePrice", sum/count);
        avg.setProperty("count", count);
        avg.setProperty("upc", upc.trim());
        
        //Add to DS
        ds.put(avg);
	}
}