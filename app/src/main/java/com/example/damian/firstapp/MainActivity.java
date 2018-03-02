package com.example.damian.firstapp;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.StrictMode;
import android.renderscript.Sampler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity  {

    TextView textView2;
    DatagramSocket dsocket;
    String device_name = null;
    ListView oidListView;
    TextView editText;
    TextView editText2;
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
        editText = findViewById(R.id.editText);
        editText2 = findViewById(R.id.editText2);

        ArrayAdapter<CharSequence> adp3 = ArrayAdapter.createFromResource(this,
                R.array.spinner, android.R.layout.simple_list_item_1);

        adp3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        String[] oids={"sysDescr","sysObjectID","sysUpTime","sysName","sysServices","icmpInMsgs","icmpInDestUnreachs"};
        ListAdapter listadapter =new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,oids);
        final ListView oidListView=(ListView)findViewById(R.id.oidListView);
        oidListView.setAdapter(listadapter);

        oidListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                if(!"".equals(editText.getText().toString()) && !"".equals(editText2.getText().toString())) {
                    conntactWithServer(oidListView.getItemAtPosition(position).toString());
                }
            }
        });

    }

    public String getLocalIpAddress(){
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void conntactWithServer(final String text) {
        try {

           DatagramSocket clientSocket = new DatagramSocket();
           InetAddress IPAddress = InetAddress.getByName(editText.getText().toString());

            byte[] sendData = new byte[1024];
            String sentence = null;
            String ip_Address = getLocalIpAddress();
            device_name = editText2.getText().toString();
            SnmpMessage newMessage = new SnmpMessage(device_name,text,ip_Address);
            sentence = ObjectToJson(newMessage);
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
                String[] tmp = receivedSentence.split("\"");
                String message = JsonToObject(tmp);
                final String finalModifiedSentence = message;
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

    public String ObjectToJson(SnmpMessage newSnmpMessage)
    {
        ObjectMapper mapper = new ObjectMapper();
        SnmpMessage snmpmessage = newSnmpMessage;

        try {
            //Convert object to JSON string
            String jsonInString = mapper.writeValueAsString(snmpmessage);
            return jsonInString;

        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    };

    public SnmpTypeObject JsonToObject(tmp)
    {

        //String temporary = tmp[1] + " :" + tmp[3] + "\n" + tmp[5] + " :" + tmp[7] + "\n" + tmp[9] + " :" + tmp[11];
        ObjectMapper mapper = new ObjectMapper();
         //   SnmpTypeObject snmpmessage = new SnmpTypeObject(tmp[3], tmp[7], tmp[9]);

        // Convert JSON string to Object
        String jsonInString = tmp;
        SnmpTypeObject snmpmessageobj = null;
        try {
            snmpmessageobj = mapper.readValue(jsonInString, SnmpTypeObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
            return snmpmessageobj;

    };

    public class SnmpTypeObject {

        public String Oid;
        public String Type;
        public String Value;



        public void setOid(String Oid)
        {
            this.Oid= Oid;
        }

        public void SetType(String Type)
        {
            this.Type = Type;
        }

        public void SetValue(String Value)
        {
            this.Value = Value;
        }

        public SnmpTypeObject()
        {

        }

        public SnmpTypeObject(@JsonProperty("Oid") String Oid, @JsonProperty("Type") String Type, @JsonProperty("Value") String Value)
        {
            this.Oid = Oid;
            this.Type = Type;
            this.Value = Value;
        }
    }

    public class SnmpMessage {

        public String DeviceName;
        public String ElementName;
        public String Ip;

        public SnmpMessage(String DeviceName, String ElementName, String Ip)
        {
            this.DeviceName = DeviceName;
            this.ElementName = ElementName;
            this.Ip = Ip;
        }
    }
}