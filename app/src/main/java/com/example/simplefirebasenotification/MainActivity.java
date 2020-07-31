package com.example.simplefirebasenotification;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

//public class MainActivity extends AppCompatActivity implements ValueEventListener {
public class MainActivity extends AppCompatActivity  {

    private static final String TAG = "MyFirebaseServices";
    private static final String SN = "DEVICE_STATE";

    //CPU vairiables
    TextView textViewCPU ;
    ProcessBuilder processBuilder;
    String Holder = "";
    String[] DATA = {"/system/bin/cat", "/proc/cpuinfo"};
    InputStream inputStream;
    Process process ;
    byte[] byteArry ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_layout);
        setTitle("Notification Metrics");

        //get views by id
        final TextView ChargeCycles = (TextView) findViewById(R.id.chargeCycles);
        final TextView BatteryIsCharging = (TextView) findViewById(R.id.batteryCharging);
        final TextView pluggedIn = (TextView) findViewById(R.id.pluggedIn);
        final TextView wirelessCharging = (TextView) findViewById(R.id.wirelessCharging);



        //Get Device State
        // Charging State
        Context context = this;
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        // Are we charging / charged?
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        // How are we charging?
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        //Battery Cycles
        int chargeCount = BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER;

        //Charge Cycles
        ChargeCycles.setText(Integer.toString(chargeCount));

        //Is phone charging
        if (isCharging) {
            BatteryIsCharging.setText("True");
        }else{
            BatteryIsCharging.setText("False");
        }
        //Is device plugged in
        if (usbCharge) {
            pluggedIn.setText("True");
        }else{
            pluggedIn.setText("False");
        }
        //is device Wireless charging
        if (acCharge) {
            wirelessCharging.setText("True");
        }else{
            wirelessCharging.setText("False");
        }

        //CPU

        textViewCPU = (TextView)findViewById(R.id.textViewCPU);

        byteArry = new byte[1024];
        try{
            processBuilder = new ProcessBuilder(DATA);

            process = processBuilder.start();

            inputStream = process.getInputStream();

            while(inputStream.read(byteArry) != -1){

                Holder = Holder + new String(byteArry);
            }
            inputStream.close();

        } catch(IOException ex){

            ex.printStackTrace();
        }
        textViewCPU.setText(Holder);
        Log.d("SN",Holder);

        //Storage
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        //long bytesAvailable = (long)stat.getBlockSize() *(long)stat.getBlockCount();
        long bytesAvailable = stat.getFreeBytes();
        long megAvailable = bytesAvailable / 1048576; //meg Avail Storage Float.toString(val)
        long bytesTotal = stat.getTotalBytes();
        long megTotal = bytesTotal / 1048576; //megTotal Storage
        final TextView textViewStorage = (TextView)findViewById(R.id.storage);
        textViewStorage.setText(Float.toString(megTotal) + "/" + Float.toString(megAvailable) + " MB");


        //RAM
        ActivityManager actManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        long totalMemory = memInfo.totalMem / 1048576;
        long availRAM = memInfo.availMem / 1048576; //Float.toSTring
        boolean lowMemory = memInfo.lowMemory;
        final TextView textViewRAM = (TextView)findViewById(R.id.ram);
        final TextView textViewLowRAM = (TextView)findViewById(R.id.lowMemory);
        textViewRAM.setText(Float.toString(totalMemory) +"/"+ Float.toString(availRAM)+ " MB");
        if (lowMemory){
            textViewLowRAM.setText("TRUE");
        } else{
            textViewLowRAM.setText("FALSE");
        }

        //network
        //ConnectivityManager cm =
         //       (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String carrierName = manager.getNetworkOperatorName();
        Log.d(SN, carrierName);

        //time
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateTime = dateFormat.format(new Date());
        Log.d(SN,currentDateTime);



        //FirebaseDatabase database = FirebaseDatabase.getInstance();
        //DatabaseReference myRef = database.getReference("message");
        //myRef.setValue(Holder);



        //push test
       // final FirebaseDatabase database = FirebaseDatabase.getInstance();
        //DatabaseReference ref = database.getReference();
       // DatabaseReference postsRef = ref.child("posts");

       // DatabaseReference newPostRef = postsRef.push();
     //   newPostRef.setValue("gracehop", "Announcing COBOL, a New Programming Language");



        //token capturing
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                       // String msg = getString(R.string.msg_token_fmt, token);
                        Log.d(TAG, token);
                      //  Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();


                    }
                });
    }



}
