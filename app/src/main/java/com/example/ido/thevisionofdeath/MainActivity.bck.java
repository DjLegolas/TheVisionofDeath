/*  A Back up for what I did till UVCCamera usage


package com.example.ido.thevisionofdeath;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.lang.String;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Runnable{

    private TextView deviceName;

    private static final String TAG = "MyUsbActivity";
    private UsbManager mUsbManager;
    private UsbDevice mDevice;
    private UsbDeviceConnection mConnection;
    private UsbEndpoint mEndpointIntr;


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button nextDevice = (Button) findViewById(R.id.bNextDeviec);
        deviceName = (TextView) findViewById(R.id.txtDevice);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        /*
        mDevice = (UsbDevice) getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        if (mDevice == null) {
            HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
            // TODO: check for correct devices
            // XmlResourceParser xmlParser = getResources().getXml(R.xml.device_filter);
            // while (deviceIterator.hasNext()){
            //     int device_id = xmlParser.
            // }
            if(deviceIterator.hasNext()) {
                mDevice = deviceIterator.next();
                String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
                PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                if(mDevice.getVendorId() == 6257 && mDevice.getProductId() == 53506) {
                    if (!manager.hasPermission(mDevice)) {
                        Log.d("Permission", "No permissions for device");
                        manager.requestPermission(mDevice, mPermissionIntent);
                    }
                }
            }
        }

        // A Webcam must have at least a control interface and a video interface
        if (mDevice.getInterfaceCount() < 2) {
            //throw new UnknownDeviceException();
            Log.d("mainAct.onCreate","invalid webcam inferface");
        }

        byte[] bytes = new byte[64];
        int TIMEOUT = 0;
        boolean forceClaim = true;

        UsbInterface intf =  mDevice.getInterface(0);
        UsbEndpoint endpoint = intf.getEndpoint(0);
        UsbDeviceConnection deviceConnection = manager.openDevice(mDevice);
        deviceConnection.claimInterface(intf, true);

        int conf = mDevice.getConfigurationCount();
        int inter = mDevice.getInterfaceCount();
        for (int i = 0; i < inter; i++) {
            intf = mDevice.getInterface(i);
            if (i < conf) {
                Log.d("Settings #" + i, mDevice.getConfiguration(i).toString());
            }
            Log.d("Interface #" + i, intf.getName()+"\n"+intf.getInterfaceClass()+" "+intf.getInterfaceSubclass()+"\n"+intf.toString());
            if (intf.getEndpointCount() > 0) {
                UsbEndpoint end = intf.getEndpoint(0);
                int dir = end.getDirection();
                if (dir == UsbConstants.USB_DIR_IN) {
                    Log.d("Endpoint", "USB in");
                }
                else if (dir == UsbConstants.USB_DIR_OUT) {
                    Log.d("Endpoint", "USB out");
                }
                int type = end.getType();
                switch (type) {
                    case UsbConstants.USB_ENDPOINT_XFER_CONTROL:
                        Log.d("Endpoint", "endpoint zero");
                        break;
                    case UsbConstants.USB_ENDPOINT_XFER_ISOC:
                        Log.d("Endpoint", "isochronous endpoint");
                        break;
                    case UsbConstants.USB_ENDPOINT_XFER_BULK:
                        Log.d("Endpoint", "bulk endpoint");
                        break;
                    case UsbConstants.USB_ENDPOINT_XFER_INT:
                        Log.d("Endpoint", "interrupt endpoint");
                        break;
                }
            }
        }


        //deviceConnection.bulkTransfer(endpoint, bytes, bytes.length, TIMEOUT); //do in another thread
        //Log.d("bytes", bytes.toString());
        deviceConnection.releaseInterface(intf);
        * /
        nextDevice.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getIntent();
        Log.d(TAG, "intent: " + intent);
        String action = intent.getAction();

        UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            setDevice(device);
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            if (mDevice != null && mDevice.equals(device)) {
                setDevice(null);
            }
        }
    }

    private void setDevice(UsbDevice device) {
        Log.d(TAG, "setDevice " + device);
        if (device.getInterfaceCount() == 0) {
            Log.e(TAG, "could not find interface");
            return;
        }
        UsbInterface intf = device.getInterface(0);
        // device should have one endpoint
        if (intf.getEndpointCount() != 1) {
            Log.e(TAG, "could not find endpoint");
            return;
        }
        // endpoint should be of type interrupt
        UsbEndpoint ep = intf.getEndpoint(0);
        if (ep.getType() != UsbConstants.USB_ENDPOINT_XFER_INT) {
            Log.e(TAG, "endpoint is not interrupt type");
            return;
        }
        mDevice = device;
        mEndpointIntr = ep;
        if (device != null) {
            UsbDeviceConnection connection = mUsbManager.openDevice(device);
            if (connection != null && connection.claimInterface(intf, true)) {
                Log.d(TAG, "open SUCCESS");
                mConnection = connection;
                Thread thread = new Thread(this);
                thread.start();

            } else {
                Log.d(TAG, "open FAIL");
                mConnection = null;
            }
        }
    }

    @Override
    public void run() {
        Log.d(TAG, "start run");
        int bufferMaxLength = mEndpointIntr.getMaxPacketSize();
        ByteBuffer buffer = ByteBuffer.allocate(bufferMaxLength);
        UsbRequest request = new UsbRequest();
        request.initialize(mConnection, mEndpointIntr);
        //while (true) {
        // queue a request on the interrupt endpoint
        Log.d(TAG, "queuing");
        if (request.queue(buffer, bufferMaxLength) == true) {
            mConnection.requestWait();
            byte[] buff = new byte[buffer.remaining()];
            buffer.get(buff);
            String str = "";
            for (Byte b : buff) {
                str = str + b.toString();
            }
            Log.d(TAG, "buffer " + str);
            buffer.clear();
        }
        //}
        Log.d(TAG, "end run");
    }

    @Override
    public void onClick(View v) {
        String Model = mDevice.getDeviceName();
        int DeviceID = mDevice.getDeviceId();
        int Vendor = mDevice.getVendorId();
        String Product= mDevice.getProductName();
        int ProductID = mDevice.getProductId();
        int Class = mDevice.getDeviceClass();
        int Subclass = mDevice.getDeviceSubclass();

        //deviceName.setText("Device Name: " + Model + " id: " + DeviceID + "\nVendor: " + Vendor + "Product:" + Product);
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        deviceName.setText(Model+"\n"+DeviceID+"\n"+Vendor+"\n"+Product+"\n"+ProductID+"\n"+Class+"\n"+Subclass+"\n"+manager.hasPermission(mDevice));
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     * /
    public native String stringFromJNI();
}

*/