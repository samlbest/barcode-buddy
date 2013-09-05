/* Sources: 
 * Custom listview: http://saigeethamn.blogspot.com/2010/04/custom-listview-android-developer.html */

package com.samlbest.cs496.finalproject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.google.cloud.backend.android.CloudBackendActivity;
import com.google.cloud.backend.android.CloudCallbackHandler;
import com.google.cloud.backend.android.CloudEntity;
import com.google.cloud.backend.android.CloudQuery;
import com.google.cloud.backend.android.CloudQuery.Scope;
import com.google.cloud.backend.android.F;

public class ViewProducts extends CloudBackendActivity {
	List<CloudEntity> products = new LinkedList<CloudEntity>();
	
	
    ArrayList<HashMap<String,String>> productList = new ArrayList<HashMap<String,String>>();
	private static final SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy ", Locale.US);

    // List view
    private ListView lv;
    EditText inputSearch;
    SimpleAdapter adapter;
     
    // Search EditText
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_products);

        //Initialize UI
        lv = (ListView) findViewById(R.id.list_view);
        inputSearch = (EditText) findViewById(R.id.inputSearch);
         
        // Adding items to listview
        //listAllPosts();
        adapter = new SimpleAdapter(
        		this,
        		productList,
        		R.layout.list_product,
        		new String[] {"product", "brand", "price", "upc"},
        		new int[] {R.id.product, R.id.brand, R.id.price}
        );
        
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new OnItemClickListener() {
            @SuppressWarnings("unchecked")
			public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) { 
            	
            	Intent i = new Intent(getApplicationContext(), ProductDetails.class);
            	HashMap<String, String> item = (HashMap<String,String>) lv.getItemAtPosition(position);
            	
            	String upc = item.get("upc");
            	String productName = item.get("product");
            	String brand = item.get("brand");
            	String price = item.get("price");
            	
        	   	i.putExtra("upc", upc);
        	   	i.putExtra("productName", productName);
        	   	i.putExtra("brand", brand);
        	   	i.putExtra("lastPrice", price);
        	   	startActivity(i);
            }
        });

        /**
         * Enabling Search Filter
         * */
            
        inputSearch.addTextChangedListener(new TextWatcher() {
             
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                ViewProducts.this.adapter.getFilter().filter(cs);   
            }
             
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                    int arg3) {
                // TODO Auto-generated method stub
                 
            }
             
            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub                          
            }
        });
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
    
    @Override
    protected void onPostCreate() {
    	super.onPostCreate();
    	listAllPosts();
    }

    private void listAllPosts() {
      // create a response handler that will receive the query result or an error
      CloudCallbackHandler<List<CloudEntity>> handler = new CloudCallbackHandler<List<CloudEntity>>() {
	        @Override
	        public void onComplete(List<CloudEntity> results) {
	        	products = results;
	        	updateProductList();
	        }
	
	        @Override
	        public void onError(IOException exception) {
	        	handleEndpointException(exception);
	        }
	  };

      // execute the query with the handler
	  CloudQuery query = new CloudQuery("Product");
	    query.setLimit(50);
	    if (getAccountName() != null) {
	    	query.setFilter(F.eq(CloudEntity.PROP_CREATED_BY, getAccountName()));
	    }
	    query.setScope(Scope.FUTURE_AND_PAST);
	    getCloudBackend().list(query, handler);
    }
    
    // convert posts into string and update UI
    private void updateProductList() {
    	productList.clear();
    	for (CloudEntity product : products) {
    		//Toast.makeText(ViewProducts.this, getCreatorName(product), Toast.LENGTH_LONG).show();
    		HashMap<String,String> temp = new HashMap<String,String>();
    		temp.put("upc", product.get("upc").toString());
    		temp.put("product", product.get("name").toString());
    		temp.put("brand", product.get("brand").toString());
    		temp.put("price", "$" + String.format("%.2f", product.get("price")) +
    						  " (" + sdf.format(product.getCreatedAt()) + ")");
    		productList.add(temp);
    	}
    	adapter.notifyDataSetChanged();
    }
       
    private void handleEndpointException(IOException e) {
        Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
    }
}
