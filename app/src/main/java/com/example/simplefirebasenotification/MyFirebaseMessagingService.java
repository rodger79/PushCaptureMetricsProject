package com.example.simplefirebasenotification;

import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseServices";

    //CPU vairiables
    TextView textViewCPU ;
    ProcessBuilder processBuilder;
    String Holder = "";
    String[] DATA = {"/system/bin/cat", "/proc/cpuinfo"};
    InputStream inputStream;
    Process process ;
    byte[] byteArry ;

    private  void writeToDatabase(String msgRef){
        Log.d(TAG,"Attempting to write to database" + msgRef);

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mChildRef = mDatabase.child(msgRef);
        mChildRef.setValue("test2");
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
        DatabaseReference mCPU = mChildRef.child("CPU");
        mCPU.setValue(Holder);
    /*
        //Charging
        // Charging State
        Context context = this;
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        // Are we charging / charged?
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        DatabaseReference mIsCharging = mChildRef.child("isCharging");
        mIsCharging.setValue(isCharging);

        // How are we charging?
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        DatabaseReference mchargePlug = mChildRef.child("usbCharge");
        mchargePlug.setValue(usbCharge);

        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        DatabaseReference macCharge = mChildRef.child("acCharge");
        macCharge.setValue(acCharge);
        //Battery Cycles
        int chargeCount = BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER;
        DatabaseReference mchargeCount = mChildRef.child("chargeCount");
        mchargeCount.setValue(chargeCount);


        //Storage
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        //long bytesAvailable = (long)stat.getBlockSize() *(long)stat.getBlockCount();
        long bytesAvailable = stat.getFreeBytes();
        long MegAvailable = bytesAvailable / 1048576; //meg Avail Storage Float.toString(val)
        DatabaseReference mMegAvailable = mChildRef.child("MegAvailable");
        mchargeCount.setValue(Float.toString(MegAvailable));

        long bytesTotal = stat.getTotalBytes();
        long MegTotal = bytesTotal / 1048576; //megTotal Storage
        DatabaseReference mMegTotal = mChildRef.child("MegTotal");
        mchargeCount.setValue(Float.toString(MegTotal));

        //RAM
        ActivityManager actManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        long totalMemory = memInfo.totalMem / 1048576;
        DatabaseReference mtotalMemory = mChildRef.child("totalMemory");
        mchargeCount.setValue(Float.toString(totalMemory));
        long availRAM = memInfo.availMem / 1048576; //Float.toSTring
        DatabaseReference mavailRAM = mChildRef.child("availRAM");
        mchargeCount.setValue(Float.toString(availRAM));
        boolean lowMemory = memInfo.lowMemory;
        DatabaseReference mlowMemory = mChildRef.child("lowMemory");
        mchargeCount.setValue(mlowMemory);

        //network
        TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String carrierName = manager.getNetworkOperatorName();
        DatabaseReference mcarrierName = mChildRef.child("carrierName");
        mchargeCount.setValue(carrierName);

        //time
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentDateTime = dateFormat.format(new Date());
        DatabaseReference mcurrentDateTime = mChildRef.child("TimeStanp");
        mchargeCount.setValue(currentDateTime);

     */
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
        String message = String.valueOf(remoteMessage.getData());
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
        writeToDatabase("testingDataBase");

    }

    private void createAndSendNotificationB(RemoteMessage remoteMessage){
        //Code here
    }

    private void createAndSendNotificationC(String messageBody){
        //Code here
    }

}
