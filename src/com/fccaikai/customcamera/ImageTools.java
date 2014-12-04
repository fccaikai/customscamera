package com.fccaikai.customcamera;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

public final class ImageTools {

	/**
	 * 
	 * */
	public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels){
		int initialSize = computeInitialSampleSize(options, minSideLength,maxNumOfPixels);
		int roundedSize;
		if (initialSize <= 8 ){
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		}else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}
		return roundedSize;
	}
	
	private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels){
		double w = options.outWidth;
		double h = options.outHeight;
		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 150 : (int) Math.min(Math.floor(w / minSideLength),Math.floor(h / minSideLength));
		if (upperBound < lowerBound){
			return lowerBound;
		}
		if((maxNumOfPixels == -1) && (minSideLength == -1)){
			return 1;
		}else if (minSideLength == -1){
			return lowerBound;
		}else {
			return upperBound;
		}
	}
	
	/**
	 * ���URI��ȡͼƬ����·��
	 * 
	 * */
	public static String getAbsoluteImagePath(Uri uri, Activity activity){
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = activity.managedQuery(uri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
	
	
}
