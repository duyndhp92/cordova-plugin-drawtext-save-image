package com.sgoldm.plugin.joinImages;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Base64;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.ByteArrayOutputStream;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import java.io.IOException;

/**
 * Class that joins two images together using an Android Canvas and Bitmap.
 *
 * @author ocarty
 */
public class JoinImages extends CordovaPlugin {
    
    public static final String ACTION_JOIN_IMAGES_FROM_DATA = "joinImagesFromData";
    public static final String ACTION_RESIZE_IMAGE_FROM_DATA = "resizeImageFromData";
    public static final int MEGABYTES_MULTIPLIER = 1048576;
    public static final double SCALE_FACTOR = 0.8;
    public static final double DEFAULT_MB_LIMIT = 5.0;
    public static final int QUALITY = 90;
    public static final Bitmap.CompressFormat COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
    
    /**
     * Method that connects the javascript code to the native android code.
     *
     * @param action          The action to execute.
     * @param args            The exec() arguments.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return true if action can be successfully completed, false otherwise.
     * @throws JSONException if JSON args cannot be read.
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        
        if (action.equals(ACTION_JOIN_IMAGES_FROM_DATA)) {
            String encodedImage = joinImagesFromData(args);			
            callbackContext.success(encodedImage);
            return true;
        }
        else if (action.equals(ACTION_RESIZE_IMAGE_FROM_DATA)) {
            String encodedImage = resizeImageFromData(args);
            callbackContext.success(encodedImage);
            return true;
            
        }
        else {
            callbackContext.error("Invalid action");
            return false;
        }
    }
    
    private String joinImagesFromData(JSONArray args) {
        String firstImageDataString = args.optString(0, "");	
		String ffolder = args.optString(2, "");
		String ffilename = args.optString(3, "");		
		String contentText = args.optString(4, "");		
        //double sizeLimitInBytes = args.optDouble(1, DEFAULT_MB_LIMIT) * MEGABYTES_MULTIPLIER;

        // get the images as bitmaps
        Bitmap bitmap = getBitmapFromEncodedString(firstImageDataString);        		        		
		Bitmap bmOverlay = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		String root = Environment.getExternalStorageDirectory().toString();
		File myDir = new File(root + "/" + ffolder);
		myDir.mkdirs();
		File imageFile = new File(myDir, ffilename);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(imageFile);
			Canvas canvas = new Canvas(bmOverlay);
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setColor(Color.WHITE); // Text Color
			paint.setStrokeWidth(100); // Text Size
			paint.setTextSize(200);
			paint.setStyle(Style.FILL);
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text Overlapping Pattern
			canvas.drawBitmap(bitmap, 0, 0, paint);
			canvas.drawText(contentText, 30, 40, paint);
			bmOverlay.compress(Bitmap.CompressFormat.JPEG, 100, out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

        // scale it down and encode it, return that string
        //return scaleAndEncodeBitmap(newPhoto, sizeLimitInBytes);
		return "Capture success";
    }
    
    /**
     * Takes one image, resizes it with params and returns it as a base 64 string.
     *
     * @param args one image as a string and a size limit
     * @return the encoded string as a Base64
     */
    private String resizeImageFromData(JSONArray args) {	 
        String imageDataString = args.optString(0, "");
        double sizeLimitInBytes = args.optDouble(1, DEFAULT_MB_LIMIT) * MEGABYTES_MULTIPLIER;

        // get the image as bitmap
        Bitmap bitmapOfImage = getBitmapFromEncodedString(imageDataString);

        // scale it down and encode it, return that string
        return scaleAndEncodeBitmap(bitmapOfImage, sizeLimitInBytes);
    }

    /**
     * Method that scales down a bitmap and returns it as a base64 encoded string
     *
     * @param bitmap the bitmap passed in
     * @param sizeLimitInBytes the size limit in bytes
     * @return the scaled down bitmap as an encoded string
     */
    private String scaleAndEncodeBitmap(Bitmap bitmap, double sizeLimitInBytes) {
        // get the current size in bytes
        int sizeOfBitmapInBytes = bitmap.getRowBytes() * bitmap.getHeight();
        // create output stream
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // while its outside of the size limit
        while(sizeOfBitmapInBytes > sizeLimitInBytes) {
            // scale it down and check the size again
            bitmap = getResizedBitmap(bitmap, (int) (bitmap.getHeight() * SCALE_FACTOR), (int) (bitmap.getWidth() * SCALE_FACTOR));
            sizeOfBitmapInBytes = bitmap.getRowBytes() * bitmap.getHeight();
        }
        // compress it and get byte array
        bitmap.compress(COMPRESS_FORMAT, QUALITY, stream);
        byte[] byteArray = stream.toByteArray();

        // encode it to base64 string, recycle the bitmap and return the string
        String encodedImageAsAString = Base64.encodeToString(byteArray, Base64.DEFAULT);
        bitmap.recycle();

        return encodedImageAsAString;

    }

    /**
     * Method that returns a resized bitmap based on the height and width passed in.
     *
     * @param bitmap the bitmap passed in
     * @param newHeight the new height that the bitmap should be scaled to
     * @param newWidth the new width that the bitmap should be scaled to
     * @return the scaled bitmap
     */
    private Bitmap getResizedBitmap(Bitmap bitmap, int newHeight, int newWidth) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    /**
     * Method that returns a bitmap from a base64 string
     *
     * @param encodedString the encoded data passed in
     * @return bitmap Of image
     */
    private Bitmap getBitmapFromEncodedString(String encodedString) {
        byte[] decodedByteArray = Base64.decode(encodedString, Base64.DEFAULT);
        Bitmap bitmapOfImage = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
        return bitmapOfImage;
    }
}
