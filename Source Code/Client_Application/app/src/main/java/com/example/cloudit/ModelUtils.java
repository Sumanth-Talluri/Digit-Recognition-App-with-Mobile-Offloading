package com.example.cloudit;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Base64;
import android.view.View;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class ModelUtils {
    public static Mat scaleImage(Mat imageMatrix){
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

    private static MappedByteBuffer loadModelFile(Context context, String modelFilePath) throws IOException {
        AssetFileDescriptor fileDescriptor= context.getAssets().openFd(modelFilePath);
        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startOffset=fileDescriptor.getStartOffset();
        long declareLength=fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declareLength);
    }

    public static int argmax(float[] array) {
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

    public static float predictNumber(Mat inputMatrix, int partNumber, Context context) {
        Interpreter tflite = null;
        try {
            tflite = new Interpreter(loadModelFile(context, "final_model_"+partNumber+".tflite"));
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

        int predictedNumber = argmax(output[0]);
        return predictedNumber;
    }

    public static Mat BitMapToArr(Bitmap bmp){
        Mat mat = new Mat();
        Bitmap bmp32 = bmp.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);
        Mat resizeimage = new Mat();
        Size sz = new Size(14,14);
        Imgproc.resize( mat, resizeimage, sz );
        return resizeimage;
    }

}
