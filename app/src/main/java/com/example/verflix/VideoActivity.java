package com.example.verflix;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class VideoActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    private ImageView profileImageView;
    private TextView userNameTextView;
    private TextView userAgeTextView;
    private VideoView videoView;
    private Button btnPlay, btnPause, btnForward, btnRewind;

    private String userName;
    private int userAge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);


        profileImageView = findViewById(R.id.image_profile);
        userNameTextView = findViewById(R.id.text_user_name);
        userAgeTextView = findViewById(R.id.text_user_age);
        videoView = findViewById(R.id.video_view);

        btnPlay = findViewById(R.id.btn_play);
        btnPause = findViewById(R.id.btn_pause);
        btnForward = findViewById(R.id.btn_forward);
        btnRewind = findViewById(R.id.btn_rewind);


        loadUserData();

        Bitmap profileImage = loadImageFromInternalStorage(userName);
        if (profileImage != null) {
            profileImageView.setImageBitmap(profileImage);
        } else {
            profileImageView.setImageResource(R.drawable.perfil);
        }

        userNameTextView.setText(userName);
        userAgeTextView.setText(String.valueOf(userAge));


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }


        Intent intent = getIntent();
        String category = intent.getStringExtra("category");


        Uri videoUri = null;
        if (category != null) {
            switch (category) {
                case "Caricaturas":
                    videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.caricatura1);
                    break;
                case "Acción":
                    videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.accion1);
                    break;
                case "Terror":
                    videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.terror1);
                    break;
                default:
                    Toast.makeText(this, R.string.error_categoria_no_reconocida, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
            }

            if (videoUri != null) {
                videoView.setVideoURI(videoUri);
            }
        } else {
            Toast.makeText(this, R.string.error_categoria_no_reconocida, Toast.LENGTH_SHORT).show();
            finish();
        }


        btnPlay.setOnClickListener(v -> showPhotoDialog());
        btnPause.setOnClickListener(v -> {
            if (videoView.isPlaying()) {
                videoView.pause();
            }
        });
        btnForward.setOnClickListener(v -> {
            int currentPosition = videoView.getCurrentPosition();
            int newPosition = currentPosition + 10000;
            videoView.seekTo(newPosition);
        });
        btnRewind.setOnClickListener(v -> {
            int currentPosition = videoView.getCurrentPosition();
            int newPosition = currentPosition - 10000;
            videoView.seekTo(newPosition);
        });
    }

    private void showPhotoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.debes_tomarte_foto)
                .setPositiveButton(R.string.aceptar, (dialog, id) -> dispatchTakePictureIntent())
                .setNegativeButton(R.string.cancelar, (dialog, id) -> {

                    Intent mainIntent = new Intent(VideoActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            profileImageView.setImageBitmap(imageBitmap);
            saveImageToInternalStorage(imageBitmap, userName);
            videoView.start();
        }
    }

    private void saveImageToInternalStorage(Bitmap bitmap, String userName) {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(userName + "_profile_image.png", MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Bitmap loadImageFromInternalStorage(String userName) {
        try {
            FileInputStream fis = openFileInput(userName + "_profile_image.png");
            return BitmapFactory.decodeStream(fis);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    private void loadUserData() {
        FileInputStream fis = null;
        try {
            fis = openFileInput("current_user.txt");
            byte[] data = new byte[fis.available()];
            fis.read(data);
            String userData = new String(data);
            String[] userInfo = userData.split(",");
            userName = userInfo[0];
            userAge = Integer.parseInt(userInfo[1]);
        } catch (Exception e) {
            e.printStackTrace();
            userName = getString(R.string.usuario_default);
            userAge = 0;
        } finally {
            try {
                if (fis != null) fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {

                Toast.makeText(this, R.string.permiso_camara_denegado, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
