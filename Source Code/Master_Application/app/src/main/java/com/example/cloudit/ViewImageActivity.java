package com.example.cloudit;
import static org.opencv.core.CvType.CV_8U;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;
import org.tensorflow.lite.Interpreter;


public class ViewImageActivity extends Activity {
    //Spinner dropDown;
    Bitmap selectedImage;
    int statusCode;

    String quadrantURL1 = "";
    String quadrantURL2 = "";
    String quadrantURL3 = "";
    String quadrantURL4 = "";
    Bitmap[]  parts;
    List<String> responses = new ArrayList<String>();
    private static int REQUEST_CODE = 100;
    OutputStream outputStream;

    public Mat scaleImage(Mat imageMatrix){
        byte[] byteArr = new byte[(int)(imageMatrix.total()*imageMatrix.channels())];
        imageMatrix.get(0,0,byteArr);
        int index = 0,whiteCount=0,blackCount=0;
        for(int i=0;i<14;i++){
            for(int j=0;j<14;j++){
                byte b = byteArr[index];
                int value = b&0xff;
                if(value>128)
                    whiteCount++;
                else
                    blackCount++;
                index++;
            }
        }
        index=0;
        if(blackCount<whiteCount){
            for(int i=0;i<14;i++){
                for(int j=0;j<14;j++){
                    byte b = byteArr[index];
                    int value = b&0xff;
                    if(value>=128)
                        value=0;
                    else
                        value=255;
                    byteArr[index]=(byte)value;
                    index++;
                }
            }
        }//0-> black 255->white
        else{
            for(int i=0;i<14;i++){
                for(int j=0;j<14;j++){
                    byte b = byteArr[index];
                    int value = b&0xff;
                    if(value>=128)
                        value=255;
                    else
                        value=0;
                    byteArr[index]=(byte)value;
                    index++;
                }
            }
        }
        imageMatrix.put(0,0,byteArr);
        return imageMatrix;
    }

    private MappedByteBuffer loadModelFile(String modelFilePath) throws IOException {
        AssetFileDescriptor fileDescriptor=this.getAssets().openFd(modelFilePath);
        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startOffset=fileDescriptor.getStartOffset();
        long declareLength=fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declareLength);
    }

    public int argmax(float[] array) {
        float max = array[0];
        int re = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
                re = i;
            }
        }
        return re;
    }

    public float predictNumber(Mat inputMatrix,int partNumber) {
        Interpreter tflite = null;
        try {
            tflite = new Interpreter(loadModelFile("final_model_"+partNumber+".tflite"));
        }catch (Exception ex){
            ex.printStackTrace();
        }

        byte[] byteArr = new byte[(int)(inputMatrix.total()*inputMatrix.channels())];
        float [][][][]scaledMatrix = new float[1][14][14][1];
        int index=0;
        inputMatrix.get(0,0,byteArr);
        for(int i=0;i<14;i++){
            for(int j=0;j<14;j++){
                byte b = byteArr[index];
                int value = b&0xff;
                float fvalue = value/255.0f;
                scaledMatrix[0][i][j][0]=fvalue;
                index++;
            }
        }

        float [][]output=new float[1][10];

        tflite.run(scaledMatrix,output);

        /*for(int i=0;i<14;i++){
            for(int j=0;j<14;j++){
                System.out.print(scaledMatrix[0][i][j][0]+" ");
            }
            System.out.println();
        }

        System.out.println("Model Output : "+output);
        for(int i=0;i<10;i++)
            System.out.print(output[0][i]+" ");
        System.out.println();*/

        int predictedNumber = argmax(output[0]);
        return predictedNumber;
    }

    private void createDirectoryAndSaveFile(Bitmap imageToSave, String fileName) {

        File direct = new File(Environment.getExternalStorageDirectory() + "/DirName");

        if (!direct.exists()) {
            File wallpaperDirectory = new File("/sdcard/DirName/");
            wallpaperDirectory.mkdirs();
        }

        File file = new File("/DirName/", fileName);
        System.out.println(file.getAbsolutePath());
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Mat BitMapToArr(Bitmap bmp){
        Mat mat = new Mat();
        Bitmap bmp32 = bmp.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);
        Mat resizeimage = new Mat();
        Size sz = new Size(14,14);
        Imgproc.resize( mat, resizeimage, sz );
        return resizeimage;
    }

    public void saveImage(Mat imageMatrix,String filePath){
        Imgcodecs imageCodecs = new Imgcodecs();
        imageCodecs.imwrite(filePath, imageMatrix);
        System.out.println("Image Saved ............");
    }

    public Bitmap[] splitBitmap(Bitmap src) {
        Bitmap[] divided = new Bitmap[4];
        divided[0] = Bitmap.createBitmap(
                src,
                0, 0,
                src.getWidth() / 2, src.getHeight() / 2
        );
        divided[1] = Bitmap.createBitmap(
                src,
                src.getWidth() / 2, 0,
                src.getWidth() / 2, src.getHeight() / 2
        );
        divided[2] = Bitmap.createBitmap(
                src,
                0, src.getHeight() / 2,
                src.getWidth() / 2, src.getHeight() / 2
        );
        divided[3] = Bitmap.createBitmap(
                src,
                src.getWidth() / 2, src.getHeight() / 2,
                src.getWidth() / 2, src.getHeight() / 2
        );
        return divided;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_image_page);

        Intent intent = getIntent();
        quadrantURL1 = intent.getStringExtra("quadrantURL1");
        quadrantURL2 = intent.getStringExtra("quadrantURL2");
        quadrantURL3 = intent.getStringExtra("quadrantURL3");
        quadrantURL4 = intent.getStringExtra("quadrantURL4");
        System.out.println(quadrantURL1);
        System.out.println(quadrantURL2);
        System.out.println(quadrantURL3);
        System.out.println(quadrantURL4);

        selectedImage = (Bitmap) intent.getParcelableExtra("capturedImage");
        ImageView imageView = findViewById(R.id.capturedImageView);
        parts = splitBitmap(selectedImage);
        imageView.setImageBitmap(selectedImage);

        //selectedImage = parts[1];
        /*
        Mat imageMatrix = BitMapToArr(selectedImage);
        Imgproc.cvtColor(imageMatrix, imageMatrix, Imgproc.COLOR_RGB2GRAY,4);
        System.out.println(imageMatrix.dump());
        imageMatrix=scaleImage(imageMatrix);
        System.out.println(imageMatrix.dump());
        float result = predictNumber(imageMatrix,1);
        System.out.println(result);

        Bitmap resultBitmap = Bitmap.createBitmap(imageMatrix.cols(),  imageMatrix.rows(),Bitmap.Config.ARGB_8888);;
        Utils.matToBitmap(imageMatrix, resultBitmap);
        String filePath = "savedImage.jpg";
        //createDirectoryAndSaveFile(resultBitmap,filePath);
        //saveImage(imageMatrix,filePath);
        //Imgcodecs imgcodecs = new Imgcodecs();
        //imgcodecs.imwrite(filePath, imageMatrix);
        ImageView imageView = findViewById(R.id.capturedImageView);
        imageView.setImageBitmap(resultBitmap);
        //System.out.println(imageMatrix.rows());
        //System.out.println(imageMatrix.cols());
        /*dropDown = findViewById(R.id.spinner);

        String[] dropDownItems = new String[]{"Mobile", "iPod", "Laptop", "TV", "HeadPhones"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dropDownItems);
        dropDown.setAdapter(adapter);
        */

    }



    private void askPermission() {

        ActivityCompat.requestPermissions(ViewImageActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE);

    }
    public void saveImageNew(){
        if (ContextCompat.checkSelfPermission(ViewImageActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            saveImage("empty");
        }else {
            askPermission();
        }
        // saveToInternalStorage(selectedImage);
        /*

        Uri images;
        ContentResolver contentResolver = getContentResolver();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            images = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        }
        else{
            images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        System.out.println(images);

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME,System.currentTimeMillis()+".jpg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE,"images/*");
        Uri uri = contentResolver.insert(images,contentValues);
        System.out.println(uri);
        try{
            /*
            OutputStream outputStream = contentResolver.openOutputStream(Objects.requireNonNull(uri));
            selectedImage.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
            Objects.requireNonNull(outputStream);

             //*/
        /*
            FileOutputStream outStream = null;
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/MC/1");
            dir.mkdirs();
            String fileName = String.format("%d.jpg", System.currentTimeMillis());
            File outFile = new File(dir, fileName);
            outStream = new FileOutputStream(outFile);

            selectedImage.compress(Bitmap.CompressFormat.JPEG,100,outStream);
            outStream.flush();
            outStream.close();


            //refreshGallery(outFile);
            Toast.makeText(ViewImageActivity.this, "Image Saved Successfully!!", Toast.LENGTH_LONG).show();
        }
        catch (Exception e){
            Toast.makeText(ViewImageActivity.this, "Image not saved!!" , Toast.LENGTH_LONG).show();
        }
        */
    }

    private void saveImage(String outputDir) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"SaveImage/"+outputDir);

        if (!dir.exists()){

            dir.mkdir();

        }


        File file = new File(dir,System.currentTimeMillis()+".jpg");
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        selectedImage.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
        Toast.makeText(ViewImageActivity.this,"Successfuly Saved",Toast.LENGTH_SHORT).show();

        try {
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void onSaveImageButtonClick(View view) {
        System.out.println("Size" + responses.size());
        String output = "";
        if(responses.size()>0){
            Map<String, Integer> count = new HashMap<>();
            int maxFrequency=0;
            String digit = "";
            for(String response:responses){
                if(count.containsKey(response)){
                    //count.get(response)+1
                    count.put(response,count.get(response)+1);
                }
                else{
                    count.put(response,1);
                }

                if(maxFrequency < count.get(response)){
                    digit = response;
                    maxFrequency = count.get(response);
                }
            }


            if(maxFrequency==1){
                output = responses.get(1);
            }
            else{
                output=digit;
            }
            System.out.println("The API's resulted ");
            System.out.println(output);
            Toast.makeText(ViewImageActivity.this, "Predicted Number - "+output, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ViewImageActivity.this,MainActivity.class);
            startActivity(intent);
        }
        responses = new ArrayList<String>();

        saveImage(output);
    }

    public void onAnalyzeButtonClick(View view) throws InterruptedException {
        //responses = new ArrayList<String>();


        RequestFuture<String> future1 = RequestFuture.newFuture();
        RequestFuture<String> future2 = RequestFuture.newFuture();
        RequestFuture<String> future3 = RequestFuture.newFuture();
        RequestFuture<String> future4 = RequestFuture.newFuture();

        StringRequest uploadImageRequest1 = getStringRequest(parts[0],quadrantURL1,"1", responses, future1);
        StringRequest uploadImageRequest2 = getStringRequest(parts[1],quadrantURL2,"2", responses, future2);
        System.out.println(parts[1]);

        System.out.println(uploadImageRequest2.getUrl());

        StringRequest uploadImageRequest3 = getStringRequest(parts[2],quadrantURL3,"3", responses,  future3);
        StringRequest uploadImageRequest4 = getStringRequest(parts[3],quadrantURL4,"4" , responses,  future4 );

        RequestQueue uploadImageRequestQueue = Volley.newRequestQueue(ViewImageActivity.this);
        uploadImageRequestQueue.add(uploadImageRequest1);
        //uploadImageRequestQueue.
        uploadImageRequestQueue.add(uploadImageRequest2);
        uploadImageRequestQueue.add(uploadImageRequest3);
        uploadImageRequestQueue.add(uploadImageRequest4);
        /*
        try {
             // String response1 = future1.get();
            String response2 = future2.get(30, TimeUnit.SECONDS);;
            // String response3 = future3.get();
            // String response4 = future4.get();
           // responses.add(response1);responses.add(response3);responses.add(response4);
                responses.add(response2);
            // this will block
        } catch (InterruptedException e) {
            // exception handling
        } catch (ExecutionException | TimeoutException e) {
            // exception handling
        }

         */
        Thread.sleep(5000);
        Toast.makeText(ViewImageActivity.this, "Analyze Completed", Toast.LENGTH_LONG).show();

    }

    @NonNull
    private StringRequest getStringRequest(Bitmap image, String quadrantURL,String partNumber, List<String> responses,  RequestFuture<String> future ) {


        StringRequest uploadImageRequest =  new StringRequest(Request.Method.POST,quadrantURL, response -> handleResponse(response, responses), this::handleErrorResponse ){
            protected Map<String,String> getParams() {
                Map<String,String> params = new HashMap<>();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();
                String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                params.put("imageString",imageString);
                System.out.println(imageString.length());
                //Toast.makeText(ViewImageActivity.this,  "Length - "+imageString.length(), Toast.LENGTH_LONG).show();

                params.put("partNumber",partNumber);
                System.out.println(partNumber);
                System.out.println(imageString);
                return params;
            }



            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response){
                statusCode = response.statusCode;
                return super.parseNetworkResponse(response);
            }
        };
        return uploadImageRequest;
    }

    private void handleResponse(String response, List<String> responses) {

        if(statusCode==200){
            //System.out.println(response);
            responses.add(response);
        }
        else {
            Toast.makeText(ViewImageActivity.this, "Oops!! Upload failed due to technical issues. Contact network administrator", Toast.LENGTH_LONG).show();
        }
    }

    private void handleErrorResponse(VolleyError error) {
        System.out.println(error.toString());
        Toast.makeText(ViewImageActivity.this,"Oops!! Upload failed due to technical issues",Toast.LENGTH_LONG).show();
    }

}
