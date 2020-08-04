package com.example.simplefirebasenotification;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseServices";

    //CPU vairiables

    ProcessBuilder processBuilder;
    String Holder = "";
    String[] DATA = {"/system/bin/cat", "/proc/cpuinfo"};
    InputStream inputStream;
    Process process ;
    byte[] byteArry ;

    private  void writeToDatabase(String msgRef){
        Log.d(TAG,"Attempting to write to: " + msgRef);

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mChildRef = mDatabase.child(msgRef);

        Map<String, String> captureData = new HashMap<String, String>();

        //mChildRef.setValue("test2");
        //CPU
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

        captureData.put("CPU",Holder);


        //Charging
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

        //Is phone charging
        if (isCharging) {
            captureData.put("isCharging","True");
        }else{
            captureData.put("isCharging","False");
        }
        //Is device plugged in
        if (usbCharge) {
            captureData.put("usbCharge","True");
        }else{
            captureData.put("usbCharge","False");
        }
        //is device Wireless charging
        if (acCharge) {
            captureData.put("acCharge","True");
        }else{
            captureData.put("acCharge","False");
        }

  /*  */
        //Storage
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        //long bytesAvailable = (long)stat.getBlockSize() *(long)stat.getBlockCount();
        long bytesAvailable = stat.getFreeBytes();
        long storageMBAvailable = bytesAvailable / 1048576; //meg Avail Storage Float.toString(val)
        captureData.put("storageMBAvailable",Float.toString(storageMBAvailable));

        long bytesTotal = stat.getTotalBytes();
        long storageMBTotal = bytesTotal / 1048576; //megTotal Storage
        captureData.put("storageMBTotal",Float.toString(storageMBTotal));

        //RAM
        ActivityManager actManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        long totalRAM = memInfo.totalMem / 1048576;
        captureData.put("totalRAM",Float.toString(totalRAM));
        long availRAM = memInfo.availMem / 1048576; //Float.toSTring
        captureData.put("availRAM",Float.toString(availRAM));
        boolean lowMemory = memInfo.lowMemory;
        if (lowMemory) {
            captureData.put("lowMemory","True");
        }else{
            captureData.put("lowMemory","False");
        }
        //network
        TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String carrierName = manager.getNetworkOperatorName();
        captureData.put("carrierName",carrierName);

        //time
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateTime = dateFormat.format(new Date());
        captureData.put("currentDateTime",currentDateTime);

        //store in DB
        DatabaseReference mDevices = mChildRef.child("DeviceProfiles");
        //mCPU.setValue(Holder);
        mDevices.push().setValue(captureData);
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(token);
    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Date init = new Date();
        //String messageBody = remoteMessage.getNotification().getBody();
        //Log.d(TAG,messageBody);
                // Check if message contains a data payload (beauty messages).
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            createAndSendNotificationB(remoteMessage);

        }

        // Check if message contains a notification payload (from console).
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            createAndSendNotificationC(remoteMessage.getNotification().getBody());
        }

        //Database testing
        writeToDatabase("Campaign_1" );
        Date finalTime = new Date();
        long diffInMillies = init.getTime() - finalTime.getTime();
        Log.d(TAG,"Database write time: " + diffInMillies);
    }

    private void createAndSendNotificationB(RemoteMessage remoteMessage){
        //Code here
    }

    private void createAndSendNotificationC(String messageBody){
        //Code here
    }

}
