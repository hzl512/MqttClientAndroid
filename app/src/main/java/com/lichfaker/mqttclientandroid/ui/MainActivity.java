package com.lichfaker.mqttclientandroid.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.chinamobile.iot.onenet.OneNetApi;
import com.chinamobile.iot.onenet.OneNetApiCallback;
import com.google.gson.Gson;
import com.lichfaker.log.Logger;
import com.lichfaker.mqttclientandroid.R;
import com.lichfaker.mqttclientandroid.mqtt.Message;
import com.lichfaker.mqttclientandroid.mqtt.MqttBean;
import com.lichfaker.mqttclientandroid.mqtt.MqttManager;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;


public class MainActivity extends Activity {

    public static final String URL = "tcp://218.207.217.140:6002";
//    public static final String URL = "tcp://183.207.215.142:6002";
//    private String userName = "6000052";
//    private String password = "100411001";
//    private String clientId = "60007153";

    private String userName = "6000220";
    private String password = "500034408";
    private String clientId = "60200740";
    TextView textResult;

//    private String userName = "8000343";
//    private String password = "111903003";
//    private String clientId = "80038385";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        OneNetApi.init(getApplication(),true);
        textResult=(TextView) findViewById(R.id.textResult);
        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MqttManager.getInstance().setOnMqttCallback(new MqttCallback() {
                            @Override
                            public void connectionLost(Throwable cause) {

                            }

                            @Override
                            public void messageArrived(String topic, MqttMessage message) throws Exception {
                                try {
                                    final String detail = new String(message.getPayload(), "utf-8");
                                    Logger.d(detail);
                                    Handler handler=new Handler(Looper.getMainLooper());
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            textResult.setText(detail);

                                            sendData1(detail);
                                        }
                                    });
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void deliveryComplete(IMqttDeliveryToken token) {

                            }
                        });
                        final boolean b = MqttManager.getInstance().creatConnect(URL, userName, password, clientId);

                        Logger.d("isConnected: " + b);
                        if (b){
//                            boolean a= MqttManager.getInstance().subscribe("FxftCloudDevice", 1);
                            final boolean a= MqttManager.getInstance().subscribe("test", 1);
                            Logger.d("isConnected: " + b+" FxftCloudDevice :"+a);
                            Handler handler=new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    textResult.setText("isConnected: " + b+" FxftCloudDevice :"+a);
                                }
                            });
                        }
                    }
                }).start();
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MqttManager.getInstance().publish("test", 1, "hello".getBytes());
                    }
                }).start();
            }
        });

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        MqttManager.getInstance().subscribe("test", 2);
//                    }
//                }).start();
                sendData();
            }
        });

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            MqttManager.getInstance().disConnect();
                        } catch (MqttException e) {

                        }
                    }
                }).start();

            }
        });


    }

    /**
     * 订阅接收到的消息
     * 这里的Event类型可以根据需要自定义, 这里只做基础的演示
     *
     * @param message
     */
    @Subscribe
    public void onEvent(MqttMessage message) {
        try {
            String detail = new String(message.getPayload(), "utf-8");
            Logger.d(detail);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    /**
     * 订阅接收到的消息
     * 这里的Event类型可以根据需要自定义, 这里只做基础的演示
     */
    @Subscribe
    public void onEvent(String s) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean b = MqttManager.getInstance().doConnect();
                Logger.d("isConnected: " + b);
                if (b){
                    boolean a=  MqttManager.getInstance().subscribe("FxftCloudDevice", 1);
                    Logger.d("isConnected: " + b+" FxftCloudDevice :"+a);
                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
    public static final String TAG = "ceshi";


    private void sendData1(String mqttMessage) {

        try {
            MqttBean mb= new Gson().fromJson(mqttMessage,MqttBean.class);
            Message message=new Gson().fromJson(mb.getMsg(),Message.class);

            switch (message.getCmd()){
                case "001305"://立即定位 爱车位置
                    sendOneNet("dolocation",message.getCmdID());
                    break;
                case "001304"://立即拍照 远程拍照
                    sendOneNet("photo",message.getCmdID());
                    break;
                case "001302"://远程视频
                    sendOneNet("video",message.getCmdID());
                    break;
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void sendOneNet(String id,String cmdId){
        try {
            JSONObject location = new JSONObject();
            location.put("DeviceId", password);// 设备ID
            location.put("DeviceModel", "");//设备型号
            location.put("Mileage", 0);//  里程
            location.put("lon", 0);// 经度
            location.put("lat", 0);// 纬度
            location.put("OffsetLon", 0);//  经度偏移
            location.put("OffsetLat", 0);//  纬度偏移
            location.put("Direction", "");//  方向描述
            location.put("City", "福州市");//  城市
            location.put("CityCode", "0599");//  城市代码
            location.put("MediaURL", "");// 媒体文件地址
            location.put("CmdId", cmdId);// 命令ID
            JSONObject datapoint = new JSONObject();
            datapoint.putOpt("value", location);
            JSONArray datapoints = new JSONArray();
            datapoints.put(datapoint);
            JSONObject dsObject = new JSONObject();
            dsObject.putOpt("id", id);
            dsObject.putOpt("datapoints", datapoints);
            JSONArray dataStreams = new JSONArray();
            dataStreams.put(dsObject);
            JSONObject request = new JSONObject();
            request.putOpt("datastreams", dataStreams);

            OneNetApi.addDataPoints(clientId, request.toString(), new OneNetApiCallback() {
                @Override
                public void onSuccess(String response) {
                    Logger.i(TAG, " POST is onSuccess " + response.toString());
                }

                @Override
                public void onFailed(Exception e) {
                    Logger.i(TAG,"上传失败！");
                }

            });
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    /**
     * 发送位置信息到OneNET
     */
    private void sendData() {
        String deviceIdJS = clientId;
        String datastream = "dolocation";
        try {
            JSONObject location = new JSONObject();
            location.putOpt("lat", 119.2);
            location.putOpt("lon", 29.3);

            JSONObject datapoint = new JSONObject();
            datapoint.putOpt("value", location);

            JSONArray datapoints = new JSONArray();
            datapoints.put(datapoint);

            JSONObject dsObject = new JSONObject();
            dsObject.putOpt("id", datastream);
            dsObject.putOpt("datapoints", datapoints);

            JSONArray datastreams = new JSONArray();
            datastreams.put(dsObject);

            JSONObject request = new JSONObject();
            request.putOpt("datastreams", datastreams);

            OneNetApi.addDataPoints(deviceIdJS, request.toString(), new OneNetApiCallback() {
                @Override
                public void onSuccess(String response) {
                    Log.i(TAG, " POST is onSuccess " + response);
                }

                @Override
                public void onFailed(Exception e) {
                    Log.i(TAG, " POST is onFailed ");
                    e.printStackTrace();

                }

            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
