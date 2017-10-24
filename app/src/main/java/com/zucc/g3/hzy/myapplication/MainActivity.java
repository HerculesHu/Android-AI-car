package com.zucc.g3.hzy.myapplication;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.moquette.server.Server;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity  implements Button.OnClickListener{

    private String host ="127.0.0.1:1883";
    private  String username="";
    private String password="";
    private  String pubTopic="666";
    private  String subTopic="OTA";

    private final static int CONNECTED=1;
    private final static int LOST=2;
    private final static int FAIL=3;
    private final static int RECEIVE=4;



    String   PubMsg="";
    int PWM=0;
    int dir=0;
    int L_PWM=0;
    int R_PWM=0;
    String car_speed;
    VerticalSeekBar speed;
    TextView View1=null;
    TextView View2=null;
    Switch gravity;
    boolean eight_or_four = false;
    boolean gravity_on_off = false;
    double alpha_of_yxl ;
    private RockerView rocker_view_left;
    SensorManager smanger;
    Sensor sensor_gravity;
    String[] text={"","","","","","","","","","","",""};
    String text2,text3,text4,text5;
    Server server =  new Server();



    String subMsg_text;
    boolean launched=false;
    private MqttAsyncClient mqttClient;
    boolean connected=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent=getIntent();
        host=intent.getStringExtra("broker");
        pubTopic=intent.getStringExtra("pubtopic");
        subTopic=intent.getStringExtra("subtopic");
        try{

            server.startServer();
        }catch (Exception e){
            e.printStackTrace();
        }
        ExitApplication.getInstance().addActivity(this);

        View1 = (TextView) findViewById(R.id.chs_text);
        View2 = (TextView) findViewById(R.id.textView);
        speed=(VerticalSeekBar)findViewById(R.id.seekBar);
        rocker_view_left = (RockerView) findViewById(R.id.rockerView_left);
        gravity = (Switch) findViewById(R.id.complicated_gravity);

         speed.setOnSeekBarChangeListener(new VerticalSeekBar.OnSeekBarChangeListener() {
             @Override
             public void onProgressChanged(VerticalSeekBar seekBar, int progress, boolean fromUser) {
                 String temp=speed.getText().toString();
                 if(!(temp.equals(car_speed))){
                     car_speed=temp;

                 if(car_speed.equals("停止")){
                     PWM=4;
                 }else if(car_speed=="停止"){
                     PWM=4;
                 }
                 else if(car_speed.equals("龟速")){
                     PWM=280;
                 }
                 else if(car_speed.equals("低速")){
                     PWM=500;
                 }
                 else if(car_speed.equals("中速")){
                     PWM=750;
                 }
                 else if(car_speed.equals("高速")){
                     PWM=1024;
                 }
                  Control();
                 }
             }

             @Override
             public void onStartTrackingTouch(VerticalSeekBar seekBar) {

             }

             @Override
             public void onStopTrackingTouch(VerticalSeekBar seekBar) {

             }

             @Override
             public void onrequestDisallowInterceptTouchEvent(boolean enable) {

             }
         });




        connectBroker();
        rocker_view_left.setRockerChangeListener(new RockerView.RockerChangeListener() {
            @Override
            public void report(float x, float y) {

                alpha_of_yxl = Math.toDegrees(Math.atan(y / x));
                boolean left_or_right_L;
                boolean up_or_down_L;
                if (eight_or_four) {
                    left_or_right_L = (-45 <= alpha_of_yxl && alpha_of_yxl <= 45);
                    up_or_down_L = (-45 > alpha_of_yxl || alpha_of_yxl > 45);
                } else {
                    left_or_right_L = (-67.5 < alpha_of_yxl && alpha_of_yxl < 67.5);
                    up_or_down_L = (-22.5 > alpha_of_yxl || alpha_of_yxl > 22.5);
                }
                if (left_or_right_L) {
                    if (x < 0) {
                        text[0] = "D";
                        text[1] = "";
                    }
                    if (x > 0) {
                        text[0] = "A";
                        text[1] = "";
                    }
                } else {
                    text[0] = "";
                    text[1] = "";
                }
                if (up_or_down_L) {
                    if (y < 0) {
                        text[2] = "W";
                        text[3] = "";
                    }
                    if (y > 0) {
                        text[3] = "S";
                        text[2] = "";
                    }
                } else {
                    text[2] = "";
                    text[3] = "";
                }
                text3 = "摇杆:";
                for (int i = 0; i <= 7; i++) {
                    text3 = text3 + text[i];

                }
                if (text2.equals(text3)) {
                    launched = true;
                } else {
                    text2 = text3;
                    launched = false;
                }
                if (!gravity_on_off) {
                    View1.setText("");
                    if (text2.equals("摇杆:A") && !launched) {
                        dir = 4;
                        Control();
                    } else if (text2.equals("摇杆:D") && !launched) {
                        dir = 3;
                        Control();
                    } else if (text2.equals("摇杆:W") && !launched) {
                        dir = 1;
                        Control();
                    } else if (text2.equals("摇杆:S") && !launched) {
                        dir = 2;
                        Control();
                    } else if (text2.equals("摇杆:AW") && !launched) {
                        dir = 5;
                        L_PWM = PWM;
                        R_PWM = 0;
                        Control();
                    } else if (text2.equals("摇杆:DW") && !launched) {
                        dir = 5;
                        L_PWM = 0;
                        R_PWM = PWM;
                        Control();
                    } else if (text2.equals("摇杆:AS") && !launched) {
                        L_PWM = 0;
                        R_PWM = 0-PWM;
                        dir = 5;
                        Control();
                    } else if (text2.equals("摇杆:DS") && !launched) {
                        L_PWM = 0-PWM;
                        R_PWM = 0;
                        dir = 5;
                        Control();
                    } else if (text2.equals("摇杆:") && !launched) {
                        dir = 0;
                        Control();
                    }

                }
            }
        });



        final SensorEventListener myListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float X = sensorEvent.values[0];
                float Y = sensorEvent.values[1];
                float Z = sensorEvent.values[2];
                double sqr = Math.sqrt(X*X+Y*Y+Z*Z);
                double alpha = Math.toDegrees(Math.atan(Math.sqrt(Math.pow(X,2) + Math.pow(Z,2))/Y ));//左右旋转角
                double beta = Math.toDegrees(Math.atan(Math.sqrt(Math.pow(Y,2) + Math.pow(Z,2))/X ));//上下旋转角
                if(sqr<11 && gravity_on_off){
                    if( (beta<88&&beta>-88) || (alpha<88&&alpha>-88)){
                        //留出一个平放模式
                        if(alpha>-85 && alpha<0){
                            text[8] = "D";
                        } else{
                            text[8] = "";
                        }
                        if(alpha<85 && alpha>0){
                            text[9] = "A";
                        } else{
                            text[9] = "";
                        }
                        if(beta>50||beta<-50){
                            text[10] = "W";

                        } else{
                            text[10] = "";
                        }
                        if(beta>-40&&beta<40){
                            text[11] = "S";
                        } else{
                            text[11] = "";
                        }
                    }
                }
                text2="摇杆:";
                for (int i=0;i<=7;i++){
                    text2=text2+text[i];
                }
                text4="";
                for (int i=8;i<=11;i++){
                    text4=text4+text[i];
                }
                if(gravity_on_off) {
                    View2.setText("");
                    if (text5!=text4) {
                        launched = false;
                        text5 = text4;
                    }
                    if (text5.equals("A") && !launched) {
                        dir = 4;
                        Control();
                    } else if (text5.equals("D") && !launched) {
                        dir = 3;
                        Control();
                    } else if (text5.equals("W") && !launched) {
                        dir = 1;
                        Control();
                    } else if (text5.equals("S") && !launched) {
                        dir = 2;
                        Control();
                    } else if (text5.equals("AW") && !launched) {
                        dir = 5;
                        L_PWM = PWM;
                        R_PWM = 0;
                        Control();
                    } else if (text5.equals("DW") && !launched) {
                        dir = 5;
                        L_PWM = 0;
                        R_PWM = PWM;
                        Control();
                    } else if (text5.equals("AS") && !launched) {
                        L_PWM = 0;
                        R_PWM = 0-PWM;
                        dir = 5;
                        Control();
                    } else if (text5.equals("DS") && !launched) {
                        L_PWM = 0-PWM;
                        R_PWM = 0;
                        dir = 5;
                        Control();
                    } else if (text5.equals("") && !launched) {
                        dir = 0;
                        Control();
                    }


                }



            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        smanger = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor_gravity = smanger.getDefaultSensor(Sensor.TYPE_ACCELEROMETER );
        smanger.registerListener(myListener,sensor_gravity,100000);
        gravity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    gravity.setText("重力感应:开");
                    Toast.makeText(getApplicationContext(),"请倾斜手机在45度左右",Toast.LENGTH_SHORT).show();
                    gravity_on_off = true;
                    dir=0;
                    Control();
                }else {
                    gravity.setText("重力感应:关");
                    gravity_on_off =false;
                    dir=0;
                    Control();
                    for(int i=8;i<=11;i++){
                        text[i] = "";
                    }
                }
            }
        });
    }

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what==CONNECTED){
                Toast.makeText(MainActivity.this,"连接成功",Toast.LENGTH_SHORT).show();
                connected=true;
                try {
                    mqttClient.subscribe(subTopic.toString(), 2);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }else if(msg.what==LOST){
                Toast.makeText(MainActivity.this,"连接丢失，进行重连",Toast.LENGTH_SHORT).show();
                connectBroker();
                connected=false;
            }else if(msg.what==FAIL){
                connected=true;
                Toast.makeText(MainActivity.this,"请打开WLAN热点连接小车",Toast.LENGTH_SHORT).show();

            }else if(msg.what==RECEIVE){
                subMsg_text=(String)msg.obj;
               // subMsg.append((String)msg.obj);
            }
            super.handleMessage(msg);
        }
    };

    private IMqttActionListener mqttActionListener=new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            //连接成功处理
            Message msg=new Message();
            msg.what=CONNECTED;
            handler.sendMessage(msg);
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            exception.printStackTrace();
            //连接失败处理
            Message msg=new Message();
            msg.what=FAIL;
            handler.sendMessage(msg);
        }
    };

    private MqttCallback callback=new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            //连接断开
            Message msg=new Message();
            msg.what=LOST;
            handler.sendMessage(msg);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            //消息到达
//            subMsg.append(new String(message.getPayload())+"\n"); //不能直接修改,需要在UI线程中操作
            Message msg=new Message();
            msg.what=RECEIVE;
            msg.obj=new String(message.getPayload())+"\n";
            handler.sendMessage(msg);
        }
        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            //消息发送完成
        }
    };

    private void connectBroker(){
        try {
            mqttClient=new MqttAsyncClient("tcp://"+host,"ClientID"+Math.random(),new MemoryPersistence());
            mqttClient.connect(getOptions(),null,mqttActionListener);
            mqttClient.setCallback(callback);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private MqttConnectOptions getOptions(){

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);//重连不保持状态
        if(username!=null&&username.length()>0&&password!=null&&password.length()>0){
            options.setUserName(username);//设置服务器账号密码
            options.setPassword(password.toCharArray());
        }
        options.setConnectionTimeout(10);//设置连接超时时间
        options.setKeepAliveInterval(30);//设置保持活动时间，超过时间没有消息收发将会触发ping消息确认
        return options;
    }

    private void Control(){

        if(dir<5){
            try {
                String  pubMsg="{\"D\":"+dir+",\"S\":"+PWM+"}";
                if(!pubMsg.equals(PubMsg)){
                    mqttClient.publish(pubTopic, pubMsg.getBytes(), 1, false);
                    PubMsg=pubMsg;
                }
                launched=true;
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        if(dir==5){
               try {
                   String  pubMsg="{\"D\":5,\"L\":"+L_PWM+",\"R\":"+R_PWM+"}";
                   if(!pubMsg.equals(PubMsg)){
                       mqttClient.publish(pubTopic, pubMsg.getBytes(), 1, false);
                       PubMsg=pubMsg;
                   }
                   launched = true;
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

    }
    @Override
    public void onClick(View v) {

    }

    //如果返回键被下按则退出程序
    @Override
    public void onBackPressed() {
      server.stopServer();
        this.finish();
        ExitApplication.getInstance().exit();
    }
}

