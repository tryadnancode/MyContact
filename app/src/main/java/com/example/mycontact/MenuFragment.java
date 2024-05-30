package com.example.mycontact;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MenuFragment extends Fragment {
    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 200;
    private static final int REQUEST_PERMISSION = 300;
    private static final int REQUEST_GALLERY_PERMISSION = 400;
    private float mScaleFactor = 1.0f;
    ImageView imageView, gallery, camera, img;
    TextView imagePathTextView;
    ImageView tickMarkImageView;
    Uri selectedImageUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        imageView = view.findViewById(R.id.image);
        img = view.findViewById(R.id.gallery);
        imagePathTextView = view.findViewById(R.id.image_path);
        tickMarkImageView = view.findViewById(R.id.tick_mark);
        onClick();
        return view;
    }

    private void onClick() {
        img.setOnClickListener(v -> showCustomDialog());
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                shareImage();
                return true;
            }
        });
    }
    private void shareImage() {
        if (selectedImageUri != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, selectedImageUri);
            startActivity(Intent.createChooser(shareIntent, "Share Image"));
        } else {
            Toast.makeText(getContext(), "Please select an image first", Toast.LENGTH_SHORT).show();
        }
    }
    private void showCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        camera = dialogView.findViewById(R.id.camera);
        gallery = dialogView.findViewById(R.id.gallery);
        camera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.CAMERA}, REQUEST_PERMISSION);
            } else {
                openCamera();
            }
            dialog.dismiss();
        });
        gallery.setOnClickListener(v -> {
            openGallery();
            dialog.dismiss();
        });
        dialog.show();
    }
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA && data != null) {
                // Handle image capture from camera
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imageView.setImageBitmap(imageBitmap);
                imageView.setVisibility(View.VISIBLE);
                imagePathTextView.setText("Image captured by camera");
                imagePathTextView.setVisibility(View.VISIBLE);
                tickMarkImageView.setImageResource(R.drawable.check_circle);
                // Update selectedImageUri
                selectedImageUri = saveImageAndGetUri(imageBitmap);

            } else if (requestCode == REQUEST_GALLERY && data != null) {
                // Handle image selection from gallery
                Uri selectedImage = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), selectedImage);
                    imageView.setImageBitmap(bitmap);
                    String imagePath = getPathFromUri(selectedImage);
                    imagePathTextView.setText("Image Path: " + imagePath);
                    tickMarkImageView.setImageResource(R.drawable.check_circle);
                    imageView.setVisibility(View.VISIBLE);
                    // Update selectedImageUri
                    selectedImageUri = selectedImage;
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private Uri saveImageAndGetUri(Bitmap imageBitmap) {
        File imagesDir = new File(requireContext().getFilesDir(), "images");
        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
        }

        File imageFile = new File(imagesDir, "image_" + System.currentTimeMillis() + ".jpg");

        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return FileProvider.getUriForFile(requireContext(), "com.example.mycontact.fileprovider", imageFile);
    }

    private String getPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = requireContext().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return uri.getPath();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_GALLERY_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(getContext(), "Gallery permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}