package com.example.damian.firstapp;

import android.content.Context;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button button;
    TextView textView2;
    DatagramSocket dsocket;
    String OID_name = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        textView2 = findViewById(R.id.textView2);
        button = findViewById(R.id.button);
        button.setOnClickListener(MainActivity.this);

        final Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adp3 = ArrayAdapter.createFromResource(this,
                R.array.spinner, android.R.layout.simple_list_item_1);

        adp3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adp3);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                // TODO Auto-generated method stub
                OID_name = spinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

    }

    @Override
    public void onClick(View v) {
        try {
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName("192.168.43.158");
            byte[] sendData = new byte[1024];
            String sentence = null;

            if (OID_name.equals("sysDescr")) {
                sentence = ".1.3.6.1.2.1.1.1.0";
            }
            else if (OID_name.equals("sysObjectID")){
                sentence = ".1.3.6.1.2.1.1.2.0";
            }
            else if (OID_name.equals("sysUpTime")) {
                sentence = ".1.3.6.1.2.1.1.3.0";
            }
            else if (OID_name.equals("icmpInMsgs")) {
                sentence = ".1.3.6.1.2.1.5.1.0";
            }
            else if (OID_name.equals("sysName")) {
                sentence = ".1.3.6.1.2.1.1.5.0";
            }
            else if (OID_name.equals("icmpInDestUnreachs")) {
                sentence = ".1.3.6.1.2.1.5.3.0";
            }
            else if (OID_name.equals("sysServices")){
                sentence = ".1.3.6.1.2.1.1.7.0";
            }
            String tmp=Context.WIFI_SERVICE;
            this.getContext();
            WifiManager wm = (WifiManager)this.getSystemService(tmp);
            WifiInfo wifiInfo = wm.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            String ip_Address=String.format("%d.%d.%d.%d",
                    (ipAddress & 0xff),
                    (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff),
                    (ipAddress >> 24 & 0xff));
            sentence=sentence+"#"+ip_Address;


            sendData = sentence.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 11000);
            clientSocket.setBroadcast(true);
            clientSocket.send(sendPacket);
                        int port = 11001;
                        dsocket = new DatagramSocket(port);
                        byte[] buffer = new byte[2048];
                        String receivedSentence = null;
                        while (receivedSentence==null) {
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                            dsocket.receive(packet);
                            receivedSentence = new String(packet.getData());
                            final String finalModifiedSentence = receivedSentence;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textView2.setText(finalModifiedSentence);
                                }
                            });
                        }

                    dsocket.close();

            clientSocket.close();

        }
        catch (Exception e)
            {
                e.printStackTrace();
        }
    }
}
