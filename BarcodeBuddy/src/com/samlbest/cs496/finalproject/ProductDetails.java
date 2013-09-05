package com.samlbest.cs496.finalproject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.cloud.backend.android.CloudBackendActivity;
import com.google.cloud.backend.android.CloudCallbackHandler;
import com.google.cloud.backend.android.CloudEntity;
import com.google.cloud.backend.android.CloudQuery;
import com.google.cloud.backend.android.CloudQuery.Scope;

public class ProductDetails extends CloudBackendActivity {
	TextView productBrand;
	TextView productName;
	TextView productPrice;
	TextView averagePrice;
	ImageView photo;
	String upc;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.product_details);
	    
	    //Set UI element globals
	    productName = (TextView) findViewById(R.id.det_product_name_data);
	    productBrand = (TextView) findViewById(R.id.det_brand_data);
	    productPrice = (TextView) findViewById(R.id.det_last_price_data);
	    //upc = (TextView) findViewById(R.id.det_upc_data);
	    averagePrice = (TextView) findViewById(R.id.det_average_price_data);
	    photo = (ImageView) findViewById(R.id.photo);

	    Bundle extras = getIntent().getExtras();
	    if (extras != null) { 
	    	productName.setText(extras.getString("productName"));
	    	productBrand.setText(extras.getString("brand"));
	    	productPrice.setText(extras.getString("lastPrice"));
	    	upc = extras.getString("upc");
		    updateAverage();
		    updateImage();
	    }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.action_bar, menu);
	    return true;
	}
	
	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
		    case R.id.action_add:
                startActivity(new Intent(this, AddProduct.class));
		      	break;
		    case R.id.action_scan:
                startActivity(new Intent(this, ScanActivity.class));
		      	break;
		      
		    case R.id.action_view:
                startActivity(new Intent(this, ViewProducts.class));
			    break;
	
		    default:
		      break;
		}

	    return true;
	  } 

	void updateImage() {
		CloudCallbackHandler<List<CloudEntity>> handler = new CloudCallbackHandler<List<CloudEntity>>() {
	        @SuppressWarnings("unchecked")
			@Override
	        public void onComplete(List<CloudEntity> results) {
	        	/*if (!results.isEmpty()) {
	        		CloudEntity product = results.iterator().next();*/
	        	ArrayList<BigDecimal> photoArr = null;
	        	for (CloudEntity product : results) {
	        		if (upc.trim().equals(product.get("upc").toString())) {
			            photoArr = (ArrayList<BigDecimal>) product.get("photo");
	        			break;
	        		}
	        	}
		            
		        if (photoArr != null) {
			            //Convert arraylist to byte array
			            byte[] byteArray = new byte[photoArr.size()];
			            for (int i = 0; i < photoArr.size(); ++i) { 
			            	byteArray[i] = photoArr.get(i).byteValue();
			            }
			            
			            if (byteArray != null) {
			            	BitmapFactory.Options options = new BitmapFactory.Options();
			            	//options.inSampleSize = 8;
				        	Bitmap image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
				        	
				        	// Fill the view
				        	photo.setImageBitmap(image);
			            }
		         }
	        }
	
	        @Override
	        public void onError(IOException exception) {
	        	handleEndpointException(exception);
	        }
	  };
      // execute the query with the handler
	  CloudQuery query = new CloudQuery("Photo");
	    //query.setLimit(1);
	    //query.setFilter(F.eq("upc", upc));
	    query.setScope(Scope.PAST);
	    getCloudBackend().list(query, handler);
	}
	
	void updateAverage() {
	      CloudCallbackHandler<List<CloudEntity>> handler = new CloudCallbackHandler<List<CloudEntity>>() {
		        @Override
		        public void onComplete(List<CloudEntity> results) {
		        	/*if (!results.isEmpty()) {
		        		CloudEntity cloudProduct = results.iterator().next();
		        		averagePrice.setText("$"+String.format("%.2f", cloudProduct.get("averagePrice"))
		        				+ " (" + cloudProduct.get("count").toString() + " purchases)");		        	
		        	}
		        	
		        	else {
		        		averagePrice.setText("No average calculated.");
		        	}*/
		        	for (CloudEntity aggregate : results) {
		        		if (upc.trim().equals(aggregate.get("upc").toString())) {
		        			averagePrice.setText("$"+String.format("%.2f", aggregate.get("averagePrice"))
			        				+ " (" + aggregate.get("count").toString() + " purchases)");	
		        			break;
		        		}
		        	}
		        }
		
		        @Override
		        public void onError(IOException exception) {
		        	handleEndpointException(exception);
		        }
		  };

	      // execute the query with the handler
		  CloudQuery query = new CloudQuery("Aggregate");
		    //query.setLimit(1);
		    //query.setFilter(F.eq("upc", upc.trim()));
		    //query.setFilter(F.eq(CloudEntity.PROP_OWNER, "cron"));
		    //query.setFilter(F.eq("count", 1));
		    query.setScope(Scope.PAST);
		    getCloudBackend().list(query, handler);
	}
	
    
    private void handleEndpointException(IOException e) {
        Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
    }
}
