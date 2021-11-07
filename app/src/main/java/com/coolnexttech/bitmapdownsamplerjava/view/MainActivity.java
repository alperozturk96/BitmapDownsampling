package com.coolnexttech.bitmapdownsamplerjava.view;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.BitmapCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.coolnexttech.bitmapdownsamplerjava.databinding.ActivityMainBinding;
import com.coolnexttech.bitmapdownsamplerjava.utils.AppConst;
import com.coolnexttech.bitmapdownsamplerjava.utils.BitmapManager;
import com.coolnexttech.bitmapdownsamplerjava.utils.PermissionManager;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ActivityResultLauncher<Intent> activityResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        requestPermission();
        addActivityResults();
        binding.btnPickImage.setOnClickListener(view -> pickImage());
    }

    private void requestPermission()
    {
        PermissionManager manager = new PermissionManager();
        manager.checkAndRequestPermissions(this);
    }

    private void addActivityResults(){
        activityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null)
                {
                    int rawSize = getRawBitmapSizeFromUri(data.getData());
                    int downsampledSize = getDownsampledBitmapSize(data);
                    updateUI(rawSize,downsampledSize);
                }
            }
        });
    }

    private void loadDownsampledBitmapToPreview(Bitmap downsampleBitmap){
        binding.imgVDownsampledImage.setImageBitmap(downsampleBitmap);
    }

    private int getDownsampledBitmapSize(Intent data)
    {
        BitmapManager bitmapManager = new BitmapManager();
        Bitmap bitmap = bitmapManager.getBitmapFromGallery(data,this);
        loadDownsampledBitmapToPreview(bitmap);
        int downsampledSize = BitmapCompat.getAllocationByteCount(bitmap);
        return downsampledSize;
    }

    private int getRawBitmapSizeFromUri(Uri data)
    {
        try
        {
            Bitmap rawBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data);
            int rawSize = BitmapCompat.getAllocationByteCount(rawBitmap);
            return rawSize;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateUI(int rawSize, int downsampledSize)
    {
        String rawSizeInKB = (rawSize / 1024) + "kb";
        String downsampledSizeInKB = (downsampledSize / 1024) + "kb";

        binding.tvRawImageSize.setText("RawSize: " + rawSizeInKB);
        binding.tvDownsampledImageSize.setText("DownsampledSize: " + downsampledSizeInKB);
    }

    private void changeDesiredWidth(){
        String valueOfDesiredSize = binding.edtChangeDesiredSize.getText().toString().trim();
        if (valueOfDesiredSize.length() > 1)
        {
            AppConst.desiredWidth = Integer.parseInt(valueOfDesiredSize);
        }
    }

    private void pickImage() {
        changeDesiredWidth();
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        activityResult.launch(intent);
    }
}