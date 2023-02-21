
package com.example.cloudit.appapis.queryfiles;

import static android.app.PendingIntent.getActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.Toast;

import com.example.cloudit.MainActivity;
import com.example.cloudit.ModelUtils;
import com.example.cloudit.TinyWebServer;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import xdroid.toaster.Toaster;

public class AppApis {
    // public Context mainContext;
    public AppApis(){
    }
    /*
    public void setContext(Context context){
        mainContext = context;
    }

     */

    public String predictImage(HashMap queryParams, Context context) throws JSONException, UnsupportedEncodingException {
        JSONObject response = new JSONObject();
        float result=0.0f;

        if(queryParams!=null){
            String encodedImageString=queryParams.get("imageString")+"";
            String partNumber = queryParams.get("partNumber")+"";
            System.out.println("Parts :"+partNumber);
            System.out.println("Encoded -"+encodedImageString);
            encodedImageString = URLDecoder.decode(encodedImageString,"UTF-8");
            System.out.println("Enocded regex - "+encodedImageString);
            byte[] decodedbytesString = Base64.decode(encodedImageString,Base64.DEFAULT);
            Bitmap inputImageBitMap = BitmapFactory.decodeByteArray(decodedbytesString, 0, decodedbytesString.length);
            System.out.println(inputImageBitMap);

            Mat imageMatrix = ModelUtils.BitMapToArr(inputImageBitMap);
            Imgproc.cvtColor(imageMatrix, imageMatrix, Imgproc.COLOR_RGB2GRAY,4);
            System.out.println(imageMatrix.dump());
            imageMatrix=ModelUtils.scaleImage(imageMatrix);
            System.out.println(imageMatrix.dump());
            result = ModelUtils.predictNumber( imageMatrix,Integer.valueOf(partNumber), context);
            System.out.println("Result = "+result);
        }
        //response.put("predictedNumber", (int)result);
        int resultInt = (int) result;
        // MainActivity mainActivity = new MainActivity();
        int finalResult = resultInt;
        Toaster.toast("Predicted Number - " + finalResult);

        return String.valueOf(resultInt);
    }

    public String helloworld(HashMap qparms){
        //demo of simple html webpage from controller method 
        TinyWebServer.CONTENT_TYPE="text/html";
        return "<html><head><title>Simple HTML and Javascript Demo</title>\n" +
                "  <script>\n" +
                "  \n" +
                "</script>\n" +
                "  \n" +
                "  </head><body style=\"text-align:center;margin-top: 5%;\" cz-shortcut-listen=\"true\" class=\"\">\n" +
                "    <h3>Say Hello !</h3>\n" +
                "<div style=\"text-align:center;margin-left: 29%;\">\n" +
                "<div id=\"c1\" style=\"width: 100px;height: 100px;color: gray;background: gray;border-radius: 50%;float: left;\"></div>\n" +
                "<div id=\"c2\" style=\"width: 100px;height: 100px;color: gray;background: yellow;border-radius: 50%;float: left;\"></div>\n" +
                "<div id=\"c3\" style=\"width: 100px;height: 100px;color: gray;background: skyblue;border-radius: 50%;float: left;\"></div>\n" +
                "<div id=\"c4\" style=\"width: 100px;height: 100px;color: gray;background: yellowgreen;border-radius: 50%;float: left;\"></div>\n" +
                "<div id=\"c5\" style=\"width: 100px;height: 100px;color: gray;background: red;border-radius: 50%;position: ;position: ;float: left;\" class=\"\"></div></div>\n" +
                "  </body></html>";
    }

    public String simplejson(HashMap qparms){
        //simple json output demo from controller method
        String json = "{\"name\":\"sonu\",\"age\":29}";
        return json.toString();
    }

    public String simplegetparm(HashMap qparms){
        /*
        qparms is hashmap of get and post parameter
        
        simply use qparms.get(key) to get parameter value
        user _POST as key for post data
        e.g to get post data use qparms.get("_POST"), return will be post method 
        data
        */

        System.out.println("output in simplehelloworld "+qparms);
        String p="";
        if(qparms!=null){
            p=qparms.get("age")+"";
        }
        String json = "{\"name\":\"sonu\",\"age\":"+p+",\"isp\":yes}";
        return json.toString();
    }


    //implement web callback here and access them using method name
}