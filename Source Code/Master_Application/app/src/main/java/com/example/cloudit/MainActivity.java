package com.example.cloudit;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.contract.ActivityResultContracts;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    String quadrantURL1 = "";
    String quadrantURL2 = "";
    String quadrantURL3 = "";
    String quadrantURL4 = "";
    EditText q1;
    EditText q2;
    EditText q3;
    EditText q4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
         q1 = (EditText) findViewById(R.id.serverPort1);
         q2 = (EditText) findViewById(R.id.serverPort2);
         q3 = (EditText) findViewById(R.id.serverPort3);
         q4 = (EditText) findViewById(R.id.serverPort4);

        /*Toast.makeText(MainActivity.this, "URL1 : " + quadrantURL1, Toast.LENGTH_LONG).show();
        Toast.makeText(MainActivity.this, "URL2 : " + quadrantURL2, Toast.LENGTH_LONG).show();
        Toast.makeText(MainActivity.this, "URL3 : " + quadrantURL3, Toast.LENGTH_LONG).show();
        Toast.makeText(MainActivity.this, "URL4 : " + quadrantURL4, Toast.LENGTH_LONG).show();*/
    }

    public void captureImage(View view) {
        quadrantURL1 = "http://" + q1.getText().toString()+"/predictImage" ;
        quadrantURL2 = "http://" + q2.getText().toString()+"/predictImage" ;
        quadrantURL3 = "http://" + q3.getText().toString()+"/predictImage" ;
        quadrantURL4 = "http://" + q4.getText().toString()+"/predictImage" ;
        System.out.println(quadrantURL1);
        System.out.println(quadrantURL2);
        System.out.println(quadrantURL3);
        System.out.println(quadrantURL4);
        Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        captureImageActivityResult.launch(intent);
    }

    ActivityResultLauncher<Intent> captureImageActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Bitmap capturedImage = (Bitmap) result.getData().getExtras().get("data");
                Intent intent =  new Intent(MainActivity.this, ViewImageActivity.class);
                intent.putExtra("capturedImage",capturedImage);
                intent.putExtra("quadrantURL1", quadrantURL1);
                intent.putExtra("quadrantURL2", quadrantURL2);
                intent.putExtra("quadrantURL3", quadrantURL3);
                intent.putExtra("quadrantURL4", quadrantURL4);
                startActivity(intent);
            }
        }
    });

}