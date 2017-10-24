package com.zucc.g3.hzy.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.eclipse.moquette.server.Server;


public class SetActivity extends AppCompatActivity implements Button.OnClickListener{
    private EditText Broker,Pub,Sub;
    private Button Start;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);

        Pub=(EditText)findViewById(R.id.pubtopic);
        Sub=(EditText)findViewById(R.id.subtopic);
        Broker=(EditText)findViewById(R.id.broker);
        Start=(Button)findViewById(R.id.button);
        Start.setOnClickListener(this);
        ExitApplication.getInstance().addActivity(this);

    }

    //如果返回键被下按则退出程序
    @Override
    public void onBackPressed() {
        ExitApplication.getInstance().exit();
    }

    public void onClick(View v) {
        if(v==Start){
            String bro=Broker.getText().toString();
            String pubtopic=Pub.getText().toString();
            String subtopic=Sub.getText().toString();
            Intent intent =new Intent(SetActivity.this,MainActivity.class);
            intent.putExtra("broker",bro);
            intent.putExtra("pubtopic",pubtopic);
            intent.putExtra("subtopic",subtopic);
            startActivity(intent);
            this.finish();

        }

    }
}
