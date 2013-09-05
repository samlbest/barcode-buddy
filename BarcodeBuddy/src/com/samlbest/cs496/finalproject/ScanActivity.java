package com.samlbest.cs496.finalproject;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.mirasense.scanditsdk.ScanditSDKAutoAdjustingBarcodePicker;
import com.mirasense.scanditsdk.interfaces.ScanditSDK;
import com.mirasense.scanditsdk.interfaces.ScanditSDKListener;

public class ScanActivity extends Activity implements ScanditSDKListener {
    public static final String sScanditSdkAppKey = "Jqc6hv+NEeKflt+9p838Fcv44M5tVH8w+lPAxyKAI5A";
    private ScanditSDK mPicker;
    public JSONObject product;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	            WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
	    startBarcodeScanner();
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
	
	//Start barcode scanner
	public void startBarcodeScanner() {
		ScanditSDKAutoAdjustingBarcodePicker picker = new 
		ScanditSDKAutoAdjustingBarcodePicker(this, sScanditSdkAppKey,
				ScanditSDKAutoAdjustingBarcodePicker.CAMERA_FACING_BACK);
				
		//Set view to scanner
		setContentView(picker);
		mPicker = picker;
		
        mPicker.getOverlayView().addListener(this);
        mPicker.getOverlayView().showSearchBar(true);
	}
	
	@Override
	public void didScanBarcode(String barcode, String symbology) {
		//new RetrieveProductData().execute(barcode);
	   	Intent i = new Intent(getApplicationContext(), AddProduct.class);
		 
	   	//Pass product data through extras to main activity
	   	//i.putExtra("productInfo", result.toString()); 
	   	 i.putExtra("barcode", barcode);
	   	//Start main activity
	   	startActivity(i);
	   	 
	   //End scanner activity
	   	finish();
	}

	@Override
	public void didManualSearch(String entry) {
		//new RetrieveProductData().execute(barcode);
	   	Intent i = new Intent(getApplicationContext(), AddProduct.class);
		 
	   	//Pass product data through extras to main activity
	   	//i.putExtra("productInfo", result.toString()); 
	   	 i.putExtra("barcode", entry);
	   	//Start main activity
	   	startActivity(i);
	   	 
	   //End scanner activity
	   	finish();
	}
	
	@Override
	protected void onResume() {
		if (mPicker != null) {
			mPicker.startScanning();
		}
	    super.onResume();
	}

	@Override
	protected void onPause() {
		if (mPicker != null) {
			mPicker.stopScanning();
		}
	    super.onPause();
	}

	@Override
	public void didCancel() {
        mPicker.stopScanning();
        finish();
	}
    
    @Override
    public void onBackPressed() {
        mPicker.stopScanning();
        finish();
    }
}
