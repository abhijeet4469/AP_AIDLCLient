package com.acs.ap_aidlclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.TextView;
import android.widget.Toast;

import com.acs.ap_aidl.IOrientationData;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    IOrientationData iOrientationData;
    int timeInterval = 8;
    TextView txtRotatVector, txtRotatVectorRoll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtRotatVector = findViewById(R.id.txtRotatVector);
        txtRotatVectorRoll = findViewById(R.id.txtRotatVectorRoll);

        Intent intentService = new Intent("com.acs.ap_aidl_Service");
        bindService(inplicitToExplicit(intentService, this), serviceConnection, Context.BIND_AUTO_CREATE);

    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            iOrientationData = IOrientationData.Stub.asInterface(service);
            Toast.makeText(MainActivity.this, "Service Connected", Toast.LENGTH_SHORT).show();
            getSensorData();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(MainActivity.this, "Service Disconnected", Toast.LENGTH_SHORT).show();
        }
    };

    public Intent inplicitToExplicit(Intent implicit, Context context){
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> resolveInfos = packageManager.queryIntentServices(implicit, 0);
        if (resolveInfos == null || resolveInfos.size() !=1){
            return null;
        }
        ResolveInfo serviceInfo = resolveInfos.get(0);
        ComponentName componentName = new ComponentName(serviceInfo.serviceInfo.packageName, serviceInfo.serviceInfo.name);

        Intent explicitIntent = new Intent(implicit);
        explicitIntent.setComponent(componentName);
        return explicitIntent;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.acs.ap_aidl.BROAD_CAST");
        this.registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        if (receiver != null) {
            this.unregisterReceiver(receiver);
            receiver = null;
        }
        super.onPause();
    }


    // get data by broadcast receiver
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == "com.acs.ap_aidl.BROAD_CAST"){
                float[] values = intent.getFloatArrayExtra("SENSOR_DATA");
                calculateVector(values);
            }
        }
    };

    // get data by interface
    private void getSensorData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /*try {
                    if (iOrientationData != null){
                        calculateVector(iOrientationData.orientationDataListener());
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }*/
            }
        }, timeInterval);
    }

    private void calculateVector(float[] val){
        if (val!=null){
            float[] rotationMatrix = new float[9];
            float[] adjustedRotationMatrix = new float[9];
            float[] orientation = new float[3];

            SensorManager.getRotationMatrixFromVector(rotationMatrix, val);
            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, adjustedRotationMatrix);
            SensorManager.getOrientation(adjustedRotationMatrix, orientation);
            float roll = orientation[2] * -57;

            txtRotatVector.setText(val[0]+"  "+val[1]+"  "+val[2]+"  "+val[3]);
            txtRotatVectorRoll.setText("Roll :"+roll);
            //System.out.println("Client ==========="+val[0]+"  "+val[1]+"  "+val[2]+"  "+val[3]+"  "+roll);
        }
        getSensorData();
    }
}