package com.coolnexttech.bitmapdownsamplerjava.utils;

import android.content.Intent;
import android.graphics.BitmapFactory;


import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;

import android.util.Log;
import android.util.Size;
import androidx.annotation.RequiresApi;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class BitmapManager {


    @RequiresApi(api = Build.VERSION_CODES.P)
    private Bitmap downscaleBitmapViaImageDecoder(Uri source, Context context) {
        try
        {
            ImageDecoder.OnHeaderDecodedListener onHeaderDecodedListener = (imageDecoder, imageInfo, source1) -> {
                Size size = imageInfo.getSize();
                int sampleSize = calculateInSampleSize(size.getWidth(), Math.min(AppConst.desiredWidth, size.getWidth()));
                int newHeight = size.getHeight() / sampleSize;
                int newWidth = size.getWidth() / sampleSize;
                Size newSize = new Size(newWidth, newHeight);
                imageDecoder.setTargetSampleSize(sampleSize);
                imageDecoder.setCrop(new Rect(0, 0, newSize.getWidth(), newSize.getHeight()));
            };
            ImageDecoder.Source decoderSource = ImageDecoder.createSource(context.getContentResolver(), source);
            return ImageDecoder.decodeBitmap(decoderSource, onHeaderDecodedListener);
        }
        catch (IOException e) {
            Log.d("","error caught at downscaleBitmapViaImageDecoder: "+e);
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap downscaledBitmapViaBitmapFactory(Uri source, Context context, boolean isMediaReadingFromFile, String mediaPath) {
        final BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

        try {
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(source), null, bitmapOptions);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int imageWidth = bitmapOptions.outWidth;

        bitmapOptions.inDensity = calculateInSampleSize(imageWidth, AppConst.desiredWidth);
        bitmapOptions.inTargetDensity = 1;
        InputStream in = null;
        try {
            in = context.getContentResolver().openInputStream(source);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap scaledBitmap = null;
        if (isMediaReadingFromFile)
        {
            scaledBitmap = BitmapFactory.decodeFile(mediaPath, bitmapOptions);
        }
        else
        {
            scaledBitmap = BitmapFactory.decodeStream(in, null, bitmapOptions);
        }

        scaledBitmap.setDensity(Bitmap.DENSITY_NONE);

        return getResizedBitmap(scaledBitmap, AppConst.desiredWidth);
    }

    private int calculateInSampleSize(int currentWidth, int requiredWidth) {
        int inSampleSize = 1;

        if (currentWidth > requiredWidth) {
            int halfWidth = currentWidth / 2;

            while (halfWidth / inSampleSize >= requiredWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public Bitmap getBitmapFromGallery(Intent data, Context context){
        Uri imagePickedFromGallery = data.getData();
        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            {
                //Modern way and for new devices
                return downscaleBitmapViaImageDecoder(imagePickedFromGallery,context);
            }
            else {
                return downscaledBitmapViaBitmapFactory(imagePickedFromGallery,context,false,null);
            }
        }
        catch (Exception e)
        {
            Log.d("Exception", "error caught at bitmap decode: " + e);
            return null;
        }
    }
}
