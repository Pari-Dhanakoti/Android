package com.example.androidgalleryapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class ImageThumbnailsActivity extends Activity {

	static int id = 0;

	final int CAMERA_REQUEST = 1;
	final int PICK_FROM_GALLERY = 2;

	Button addImg;
	GridView imgGrid;
	String folder = "/GalleryApp";
	String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
    File myNewFolder = new File(extStorageDirectory + folder);
	
    File fullImagesDir = new File(myNewFolder.getPath());
    File[] imagesThumbnails; 
    
	ArrayList<ImageView> _thumbnails;
	
    static ArrayList<String>fullImageFilePaths = new ArrayList<String>();
    String[]thumbnails;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.activity_image_thumbnails);
			
		//creates a folder for Tanger in sdcard
	     if(myNewFolder.exists()==false){
	    	   myNewFolder.mkdir();
	    	  
	      }	  
	     else{
	    	 //the folder is created/exists
	    	 Log.d("ON CREATE -- folder Tanger", "created");
	     }
	   //load the grid with thumbnails
			loadGrid();
			
		final String[] option = new String[] { "Take from Camera", "Select from Gallery" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.select_dialog_item, option);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select Option");
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
				Log.e("Selected Item", String.valueOf(which));
				if (which == 0) {
					callCamera();
				}
				if (which == 1) {
					callGallery();
				}
		     }
			});
		final AlertDialog dialog = builder.create();

		addImg = (Button) findViewById(R.id.btnAdd);

		addImg.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialog.show();
			}
		});


	}

	public void loadGrid(){
		
		//parse thumbnail directory and load image to grid.
		 _thumbnails = new ArrayList<ImageView>();
	
		    // Run through the thumbnails dir :
			Log.d("LOAD GRID-Load img from: ", myNewFolder.toString());
		     imagesThumbnails = fullImagesDir.listFiles();
		    // Arrays.sort(imagesThumbnails);
		   
		   if(imagesThumbnails.length > 0) {
			   Collections.sort(new ArrayList<File>(Arrays.asList(imagesThumbnails)));
		    for(int i=0; i< imagesThumbnails.length; i++){
		    	Log.d("Thumbnail Sorted", imagesThumbnails[i].toString());
		    }
		   }
		    
		    if(imagesThumbnails.length > 0){
		    	
				    Log.d("LOAD GRID -- imagesThumbnails array length", Integer.toString(imagesThumbnails.length));
				   // Arrays.sort(imagesThumbnails);
		
				  //set the adapter
					imgGrid = (GridView) findViewById(R.id.PhoneImageGrid);
					imgGrid.setAdapter(new ImageAdapter(getApplicationContext(), imagesThumbnails));
						
				 //set item click listener for the grid item
					imgGrid.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView parent, View v, int position,
								long id) {
							// Position is counted from 0
							String clickedItemPath = imagesThumbnails[position].toString();
							Toast.makeText(getApplicationContext(), "Opening full-size image", Toast.LENGTH_LONG).show();
							
							//pass to full screen view class
							Intent intent = new Intent(getApplicationContext(), FullScreen.class);
							intent.putExtra("filename", clickedItemPath);
							startActivity(intent);
						}
					});
			}
		    else {
		    	Log.d("LOAR GRID", "grid is empty");
		    }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (resultCode != RESULT_OK)
			return;
		
		switch(requestCode){
		case CAMERA_REQUEST:
					Log.d("ID from camera_request" ,Integer.toString(id));
					Toast.makeText(getApplicationContext(), "Image Saved", Toast.LENGTH_LONG).show();
					Intent i = new Intent(ImageThumbnailsActivity.this,	ImageThumbnailsActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					loadGrid();
					startActivity(i);
					finish();
					break;
		
		case PICK_FROM_GALLERY:
					
					String imagePath = data.getData().toString();
					
					//get content://media/external/images/media path for the file
					Log.d("onActivityResult","uriImagePath Gallary :"+ imagePath); 
					
					//get the absolute path to image Eg: if chosen from zedge's wallpaper folder, this string would show its location in sdcard
					//like storage/sdcard0/zedge/wallpapers/filename.jpg
					String realPath = getRealPathFromURI(data.getData());
					Log.d("REAL PATH" , realPath); 
		      
					File source = new File(realPath);
					
					if(imagesThumbnails.length > 0)
						id = findGreatestID(imagesThumbnails)+1;
					else
						id = imagesThumbnails.length+1;
					Log.d("FILE NAME FROM CALL GALLERY", Integer.toString(id));
			
					File destination = new File(Environment.getExternalStorageDirectory()
					            + "/"+folder, id + ".jpg");
					//copy the file from sdcard's somefolder defined by realpath to sscard's tanger folder
					try {
						copyFile(source, destination);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
					//load grid again
					Intent gallIntent = new Intent(ImageThumbnailsActivity.this,	ImageThumbnailsActivity.class);
					gallIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					loadGrid();
					startActivity(gallIntent);
					finish();
					break;
		}
	
	}
	
	public void callCamera(){
		//take picture and save to newFolder
				Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				
			 	//save the image to myFolder2 
				Log.d("Image Thumbnails length from CALL CAMERA", Integer.toString(imagesThumbnails.length));
				
				//if an image is deleted, length is reduced by 1. but if an image already exists for the given id, then it will overwrite
				//to avoid that find the greatest file name. add 1 to it and set as the id for the new photo
				//id = imagesThumbnails.length+1;
				
				if(imagesThumbnails.length > 0)
					id = findGreatestID(imagesThumbnails)+1;
				else
					id = imagesThumbnails.length+1;
				Log.d("FILE NAME FROM CALL CAMERA", Integer.toString(id));
				
				 File file = new File(Environment.getExternalStorageDirectory()
				            + "/"+folder, id + ".jpg");
				 Uri imgUri = Uri.fromFile(file); //the file to which captured image will be saved. pass this to camera intent
				 Log.d("from call camera the uri", imgUri.toString());
				cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
				
				//add the absolute file name to array. this will be passed to fullImageView
				fullImageFilePaths.add(imgUri.toString());
				
				Log.d("FULLIMAGEPATH ARRAY LENGTH",Integer.toString(fullImageFilePaths.size()) );
				
				startActivityForResult(cameraIntent, CAMERA_REQUEST);

	}
	
	public void callGallery(){
		
				Intent photoPicker = new Intent(Intent.ACTION_GET_CONTENT);
				if(imagesThumbnails.length > 0)
					id = findGreatestID(imagesThumbnails)+1;
				else
					id = imagesThumbnails.length+1;
				Log.d("FILE NAME FROM CALL GALLERY", Integer.toString(id));
		
				File file = new File(Environment.getExternalStorageDirectory()
				            + "/"+folder, id + ".jpg");
				 Uri imgUri = Uri.fromFile(file); //this is the relative path shows up as content://media/external/images/media
				 Log.d("URI from callgallery", imgUri.toString());
				 photoPicker.setType("image/*");
				 photoPicker.putExtra(MediaStore.EXTRA_OUTPUT,imgUri);
				 photoPicker.putExtra("return-data", true);
		         photoPicker.putExtra("outputFormat",Bitmap.CompressFormat.JPEG.name());
		              
		         startActivityForResult(photoPicker, PICK_FROM_GALLERY);
         
	}

	
	public int findGreatestID(File[] imgThumbnails){
		ArrayList <Integer> idArray = new ArrayList<Integer>(); 
		for(int i=0; i< imgThumbnails.length; i++){
			
			String img = imgThumbnails[i].toString();
			String idName = img.substring(img.toString().lastIndexOf('/')+1, img.toString().lastIndexOf(".jpg"));
			idArray.add(Integer.parseInt(idName));
			
		}
		Collections.sort(idArray);
		int largest = idArray.get(idArray.size()-1);
		return largest;
	}

	public String getRealPathFromURI(Uri contentUri)
    {
        try
        {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = managedQuery(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        catch (Exception e)
        {
            return contentUri.getPath();
        }
    }

	
	private void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            return;
        }

        FileChannel source = null;
        FileChannel destination = null;
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            if (destination != null && source != null) {
                destination.transferFrom(source, 0, source.size());
             //   Toast.makeText(getApplicationContext(), "File copied", Toast.LENGTH_LONG).show();
                Log.d("COPY FILE", "file copied");
            }
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
         
            
		}
	
	
class ImageAdapter extends BaseAdapter{

	Context mContext;
	int numOfFiles; //has an account of number of files
	File[]paths;
	int pointer = 0;
	public ImageAdapter(Context c) {
		mContext = c;
		Log.d("IMAGE ADAPTER", "inside adapter constructor");
	}

	public ImageAdapter(Context c, File[] paths){
		mContext = c;
		this.paths = paths;
		numOfFiles = paths.length;
		Log.d("IMAGE ADAPTER: Number of files", Integer.toString(numOfFiles));
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return numOfFiles;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return paths[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d("IMAGE ADAPTER", "inside adapter getview");
		Log.d("GET VIEW FOR GRID", Integer.toString(position));
		Log.d("ID inside getView", Integer.toString(id));
		//create new image view to show the thumbnail
				FileInputStream is;
				ImageView imageView = new ImageView(mContext); //create a new imageview in the context thats passed (ie., mainView)
				//File imgFile = paths[position];
				File imgFile = paths[position];
				Log.d("GET VIEW:: Setting to imgView this image" , imgFile.toString());
				
					BitmapFactory.Options opt = new BitmapFactory.Options();
					opt.inSampleSize = 8;
					
					try {
						 is = new FileInputStream(imgFile);
						 Bitmap myBitmap = BitmapFactory.decodeStream(is, null, opt);
						 imageView.setImageBitmap(myBitmap);
						 try {
							is.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				 imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setLayoutParams(new GridView.LayoutParams(150,150));
				  
				
				return imageView;
	}
	
}

}//end of main class