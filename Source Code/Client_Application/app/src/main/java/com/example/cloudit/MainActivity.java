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
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText quadrantET;
    EditText portET;
    Integer quadrant;
    Integer port;
    TextView ipAddressOfThisDevice;
    String hostname;
    Button startOrStop;

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':')<0;
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hostname=getIPAddress(true);
        ipAddressOfThisDevice = findViewById(R.id.ipAddressOfDevice);
        System.out.println(hostname);
        if (!OpenCVLoader.initDebug())
            System.out.println("Unable to load OpenCV!");
        else
            System.out.println("OpenCV loaded Successfully!");

        quadrantET = (EditText) findViewById(R.id.quadrant);
        portET = (EditText) findViewById(R.id.port);
        startOrStop = (Button)findViewById(R.id.startServer);
        ipAddressOfThisDevice.setText(hostname);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //stop webserver on destroy of service or process
        TinyWebServer.stopServer();
    }

    public void startServer(View view){


        try
        {
            quadrant = Integer.parseInt(quadrantET.getText().toString());
            port = Integer.parseInt(portET.getText().toString());
        }
        catch (NumberFormatException e)
        {
            // handle the exception
        }
        TinyWebServer.startServer(hostname, port, "/web/server", getApplicationContext(), MainActivity.this);
        portET.setEnabled(false);
        quadrantET.setEnabled(false);
        startOrStop.setText("Stop Server");
        Toast.makeText(MainActivity.this, "port : " + port + " quadrant : " + quadrant, Toast.LENGTH_LONG).show();

    }

}