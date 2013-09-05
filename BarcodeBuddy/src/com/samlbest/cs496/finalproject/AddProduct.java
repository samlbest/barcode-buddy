package com.samlbest.cs496.finalproject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.cloud.backend.android.CloudBackendActivity;
import com.google.cloud.backend.android.CloudCallbackHandler;
import com.google.cloud.backend.android.CloudEntity;
import com.google.cloud.backend.android.CloudQuery;
import com.google.cloud.backend.android.F;
import com.google.cloud.backend.android.CloudQuery.Scope;

public class AddProduct extends CloudBackendActivity {
	//Constants
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	
	//Product info if it's looked up by scandit sdk
	private JSONObject product;
	private CloudEntity cloudProduct;
	private Uri fileUri;
	private static Bitmap userPhoto;
	
	//UI Elements
	private Button addProduct;
	private ImageButton addImage;
	
	private EditText productBrand;
	private EditText productName;
	private EditText productPrice;
	private EditText upc;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.add_product);
	    
	    //Set button
	    addProduct = (Button) findViewById(R.id.add_product_button);
	    addImage = (ImageButton) findViewById(R.id.add_image_button);

	    //Set UI element globals
	    productName = (EditText) findViewById(R.id.product_data);
	    productBrand = (EditText) findViewById(R.id.category_data);
	    productPrice = (EditText) findViewById(R.id.price_data);
	    upc = (EditText) findViewById(R.id.upc_data);
	    
	    //Set listeners for button  
	    addImage.setOnClickListener(new View.OnClickListener() {
	    	@Override
	    	public void onClick(View v) {	    		
	    		//Source: http://stackoverflow.com/questions/10825810/image-from-camera-intent-issue-in-android
	            String _path = Environment.getExternalStorageDirectory()
	                    + File.separator + "scannercapturedphoto.jpg";
	                    File file = new File(_path);
	                    fileUri = Uri.fromFile(file);
	                    Intent intent = new Intent(
	                    android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
	                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
	                    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	    	}
	    });
	    
	    addProduct.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {       	
	        	if (!isEmpty(productName) && !isEmpty(productBrand)
	        			&& !isEmpty(productPrice) && !isEmpty(upc)) {
	        		addToCloud();
	        		//addPhotoToCloud();
	        	}
	        		        	
	        	else {
	        		Toast.makeText(AddProduct.this, "All fields are required.", Toast.LENGTH_LONG).show();
	        	}           	
	        }
	    });
	    
	    Bundle extras = getIntent().getExtras();
	    if (extras != null) { 
	    	upc.setText(extras.getString("barcode"), TextView.BufferType.EDITABLE);
	    	
	    	//Check cloud for data:
	    	getProductFromCloud(extras.getString("barcode"));
	    }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.action_bar, menu);
	    return true;
	}
	
	//Source: http://www.vogella.com/articles/AndroidActionBar/article.html
	//Sets actionbar buttons
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
	
	void getProductFromCloud(final String barcode) {
		 CloudCallbackHandler<List<CloudEntity>> handler = new CloudCallbackHandler<List<CloudEntity>>() {
		        @Override
		        public void onComplete(List<CloudEntity> results) {		  	
		  		  	if (!results.isEmpty()) {
		  		  		cloudProduct = results.iterator().next();
			  		  	productName.setText(cloudProduct.get("name").toString(), TextView.BufferType.EDITABLE);
			  		  	productBrand.setText(cloudProduct.get("brand").toString(), TextView.BufferType.EDITABLE);
			  		  	productPrice.setText(String.format("%.2f", cloudProduct.get("price")), TextView.BufferType.EDITABLE);
		  		  	}
		  		  	
		  		  	else {
		  		  		new RetrieveProductData().execute(barcode);
		  		  	}
		        }
		
		        @Override
		        public void onError(IOException exception) {
		        	handleEndpointException(exception);
		        }
		  };

		  CloudQuery query = new CloudQuery("Product");
		    query.setLimit(1);
		    query.setFilter(F.eq("upc", barcode));
		    query.setScope(Scope.PAST);
		    getCloudBackend().list(query, handler);
	}
	
	private void handleEndpointException(IOException e) {
		Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
		addProduct.setEnabled(true);
	}
	
	private void addToCloud() {
	    // create a CloudEntity with the new post
	    CloudEntity newProduct = new CloudEntity("Product");
	    newProduct.setCreatedBy(getAccountName());
	    newProduct.put("brand", productBrand.getText().toString());
	    newProduct.put("name", productName.getText().toString());
	    newProduct.put("price", Float.valueOf(productPrice.getText().toString()));
	    newProduct.put("upc", upc.getText().toString());
	    // create a response handler that will receive the result or an error
	    CloudCallbackHandler<CloudEntity> handler = new CloudCallbackHandler<CloudEntity>() {
	      @Override
	      public void onComplete(final CloudEntity result) {
	    	  productName.setText("", TextView.BufferType.EDITABLE);
	  		  productBrand.setText("", TextView.BufferType.EDITABLE);
	  		  productPrice.setText("", TextView.BufferType.EDITABLE);
	  		  upc.setText("", TextView.BufferType.EDITABLE);
	    	  Toast.makeText(AddProduct.this, "Product added.", Toast.LENGTH_SHORT).show();
	    	  addProduct.setEnabled(true);
	      }

	      @Override
	      public void onError(final IOException exception) {
	        handleEndpointException(exception);
	      }
	    };

	    // execute the insertion with the handler
	    getCloudBackend().insert(newProduct, handler);
	    addProduct.setEnabled(false);
	}
	
	private void addPhotoToCloud() {
	    // create a CloudEntity with the new post
	    CloudEntity photo = new CloudEntity("Photo");
	    photo.setCreatedBy(getAccountName());
	    photo.put("upc", upc.getText().toString());

	    if (userPhoto != null) {
        	Toast.makeText(this, "Uploading image.", Toast.LENGTH_SHORT).show();

        	ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        	userPhoto.compress(Bitmap.CompressFormat.JPEG, 15, ostream);
        	byte[] byteArray = ostream.toByteArray();
        	photo.put("photo", byteArray);
	    }
	    // create a response handler that will receive the result or an error
	    CloudCallbackHandler<CloudEntity> handler = new CloudCallbackHandler<CloudEntity>() {
	      @Override
	      public void onComplete(final CloudEntity result) {
	    	  addProduct.setEnabled(true);
	    	  if (userPhoto != null) {
		    	  userPhoto.recycle();
		    	  userPhoto = null;
	    	  }
	      }

	      @Override
	      public void onError(final IOException exception) {
	        handleEndpointException(exception);
	        addProduct.setEnabled(true);
	      }
	    };

	    // execute the insertion with the handler
	    getCloudBackend().insert(photo, handler);
	    addProduct.setEnabled(false);
	}

	//Source: http://stackoverflow.com/questions/6290531/check-if-edittext-is-empty
	private boolean isEmpty(EditText text) {
		return text.getText().toString().trim().length() == 0;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            String _path = Environment.getExternalStorageDirectory()
            + File.separator + "scannercapturedphoto.jpg";
          	BitmapFactory.Options options = new BitmapFactory.Options();
        	options.inSampleSize = 8;
            userPhoto = BitmapFactory.decodeFile(_path, options);
            if (userPhoto == null) {
            	Toast.makeText(this, "Image capture error.", Toast.LENGTH_SHORT).show();
            }
            
            else {
            	Toast.makeText(this, "Image captured.", Toast.LENGTH_SHORT).show();
            	addPhotoToCloud();
            }
        }
	}
		
    //AsyncTask class retrieves JSONObject from scandit api
	private class RetrieveProductData extends AsyncTask<String, Void, JSONObject> {
		@Override
		protected JSONObject doInBackground(String... upc_in) {
		    /*HttpURLConnection connection = null;
			try {
			   	URL url;
				url = new URL(
						 "http://eandata.com/feed/?v=3&keycode=BF782110521E775B&mode=json&find=" + upc_in[0]);
				
				connection = (HttpURLConnection) url.openConnection();
				connection = url.openConnection();
				connection.setInstanceFollowRedirects(false); //prevent redirection away from api to mobile site

			   	String line;
		    	StringBuilder builder = new StringBuilder();
		    	BufferedReader reader;
				reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		    	while((line = reader.readLine()) != null) {
		    		builder.append(line);
		       	}
		    	
		    	if (builder != null) {
		    		product = new JSONObject(builder.toString());
		    		return product;
		    	}
			} catch (IOException e2) {
				e2.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}*/
			
			/* Source: http://stackoverflow.com/questions/5577857/retrieving-json-from-url-on-android
			(My original code was only working on emulator) */
			try {
				String requestUrl = "http://eandata.com/feed/?v=3&keycode=BF782110521E775B&mode=json&find=" + upc_in[0];
				
				HttpClient client = new DefaultHttpClient();
		        client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "android");
		        HttpGet request = new HttpGet();
		        request.setHeader("Content-Type", "text/plain; charset=utf-8");
		        request.setURI(new URI(requestUrl));
		        HttpResponse response = client.execute(request);
		        BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	
		        StringBuffer sb = new StringBuffer("");
		        String line = "";
	
		        String NL = System.getProperty("line.separator");
		        while ((line = in.readLine()) != null) {
		            sb.append(line + NL);
		        }
		        in.close();
		        String page = sb.toString();
		        
		        product = new JSONObject(page);
		        return product;
				} catch (IOException e2) {
					e2.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			return null;
		}

		@Override
	     protected void onPostExecute(JSONObject result) {
	    	 //Intent i = new Intent(getApplicationContext(), AddProduct.class);
	    	 
	    	 if (result != null) {
	    	 //Pass product data through extras to main activity
		        try {
					JSONObject productResult = result.getJSONObject("product").getJSONObject("attributes");
					JSONObject company = result.getJSONObject("company");
					
					//Update text fields
					productName.setText(productResult.getString("product"), TextView.BufferType.EDITABLE);
					productBrand.setText(company.getString("name"), TextView.BufferType.EDITABLE);
				} catch (JSONException e) {
					e.printStackTrace();
				}
	    	 }
	     }
	}
}
