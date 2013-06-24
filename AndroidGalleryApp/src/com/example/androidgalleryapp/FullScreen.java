package com.example.androidgalleryapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.CompressFormat;

public class FullScreen extends Activity{

	private String filename;
	   Bitmap bm; 
		   
	   ImageView iv;
	   Button btnDelete;
	   Button btnShare;
	   
	   @Override
	   public void onCreate(Bundle savedInstanceState){
		   
		   super.onCreate(savedInstanceState);
		   setContentView(R.layout.photoshare_fullscreen);
		   
		   iv = (ImageView)findViewById(R.id.fullimageView);
		   btnDelete = (Button) findViewById(R.id.delete);
	 	   btnShare = (Button) findViewById(R.id.share);
	       
	       Intent i = getIntent();
	       Bundle extras = i.getExtras();
	       BitmapFactory.Options bfo = new BitmapFactory.Options();
	       bfo.inSampleSize = 2;
	       filename = extras.getString("filename");
	        
	       bm = BitmapFactory.decodeFile(filename, bfo);
	       iv.setImageBitmap(bm);
	 
	       btnDelete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			
				File file = new File(filename);
				boolean deleted = file.delete();
				Log.d("Delete", filename.toString() + " " + deleted);
				Intent i = new Intent(FullScreen.this, ImageThumbnailsActivity.class);
				//i.putExtra("deleted pic", value);
				startActivity(i);
				finish();				
		
			}
		});
	
	       btnShare.setOnClickListener(new OnClickListener() {
	    	   
				@Override
				public void onClick(View v) {
					// create a file name to store the watermarked image - these images will be stored directly in the sdcard
					StringBuffer fileName = new StringBuffer("");
					OutputStream os = null;
					fileName.delete(0, fileName.length());
					fileName.append("tanger" + new java.util.Date().getTime());
					
					try {
						os = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/"+fileName.toString()+".jpg");
						Log.i("Filepath", "File PATH = file://" + Environment.getExternalStorageDirectory()
								.getPath() + "/" + fileName.toString() + ".jpg");
							
						bm.compress(CompressFormat.JPEG, 100, os);
						os.flush();
						os.close();
						
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					Intent share = new Intent(Intent.ACTION_SEND);
					share.setType("image/png");
					share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + Environment.getExternalStorageDirectory().getPath() + "/" + fileName.toString() + ".jpg"));
					startActivity(Intent.createChooser(share, "Share Image"));
					
						
				}
			});
	         
	   }//end of oncreate()
	
}
   
  