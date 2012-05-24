package com.example.temperaturesensor;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.TextView;

//for Android 2.3.4+ devices:
/*
import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;
*/

//for Android 3.1+ devices
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;


public class BaseActivity extends Activity implements Runnable
{
    private static final String TAG = "AOA,BaseActivity";
    private static final String ACTION_USB_PERMISSION =
            "com.example.aoaTempSensor.action.USB_PERMISSION";
    private static final int MESSAGE_TEMPERATURE = 2;
    private static final DecimalFormat TEMP_FORMATTER =
            new DecimalFormat("### " + (char) 0x00B0 + "C");

    private UsbManager mUsbManager;
    private PendingIntent mPermissionIntent;
    private boolean mPermissionRequestPending;
    private UsbAccessory mAccessory;
    private ParcelFileDescriptor mFileDescriptor;
    private FileInputStream mInputStream;
    private TextView temperatureValue;
    
    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if (msg.what == MESSAGE_TEMPERATURE)
            {
                handleTemperatureMessage((Integer) msg.obj);
            }
        }
    };
    
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action))
            {
                synchronized (this)
                {
                	// 2.3.4+ devices:
                    //UsbAccessory accessory = UsbManager.getAccessory(intent);
                    
                    // 3.1+ devies:
                    UsbAccessory accessory =
                            (UsbAccessory) intent
                                    .getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    
                    boolean hasPermission =
                            intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false); 
                    if (hasPermission)
                    {
                        openAccessory(accessory);
                    }
                    else
                    {
                        Log.d(TAG,
                                "permission denied for accessory " + accessory);
                    }
                    mPermissionRequestPending = false;
                }
            }
            else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action))
            {
            	// 2.3.4+ devices:
                //UsbAccessory accessory = UsbManager.getAccessory(intent);

            	// 3.1+ devices
                UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                
                if (accessory != null && accessory.equals(mAccessory))
                {
                    closeAccessory();
                }
            }
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aoa);
        
        temperatureValue = (TextView) findViewById(R.id.temperatureValue);

        // 2.3.4+ devices:
        //mUsbManager = UsbManager.getInstance(this);
        
        // 3.1+ devices:
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        
        mPermissionIntent =
                PendingIntent.getBroadcast(this,
                        0,
                        new Intent(ACTION_USB_PERMISSION),
                        0);
        
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(mUsbReceiver, filter);

        if (getLastNonConfigurationInstance() != null)
        {
            mAccessory = (UsbAccessory) getLastNonConfigurationInstance();
            openAccessory(mAccessory);
        }

        if (mAccessory != null)
        {
            showTemp();
        }
        else
        {
            hideTemp();
        }
    }
    
    @Override
    public void onResume()
    {
        super.onResume();

        UsbAccessory[] accessories = mUsbManager.getAccessoryList();
        UsbAccessory accessory = (accessories == null ? null : accessories[0]);
        if (accessory != null)
        {
            if (mUsbManager.hasPermission(accessory))
            {
                openAccessory(accessory);
            }
            else
            {
                synchronized (mUsbReceiver)
                {
                    if (!mPermissionRequestPending)
                    {
                        mUsbManager.requestPermission(accessory,
                                mPermissionIntent);
                        
                        mPermissionRequestPending = true;
                    }
                }
            }
        }
        else
        {
            Log.d(TAG, "mAccessory is null");
        }
    }
    
    @Override
    public void onPause()
    {
        super.onPause();
        closeAccessory();
    }
    
    @Override
    public void onDestroy()
    {
        unregisterReceiver(mUsbReceiver);
        super.onDestroy();
    }
    
    @Override
    public Object onRetainNonConfigurationInstance()
    {
        if (mAccessory != null)
        {
            return mAccessory;
        }
        else
        {
            return super.onRetainNonConfigurationInstance();
        }
    }
    
    private void handleTemperatureMessage(Integer temperature)
    {
        if (temperature != null)
        {
        	// The calibration factors below (4.9, 400, 19.5) come from the temperature sensor's datasheet 
            double voltagemv = temperature * 4.9;
            double kVoltageAtZeroCmv = 400;
            double kTemperatureCoefficientmvperC = 19.5;
            double temperatureC = ((double) voltagemv - kVoltageAtZeroCmv)
                    / kTemperatureCoefficientmvperC;
            
            temperatureValue.setText(TEMP_FORMATTER.format(temperatureC));
        }
    }
    
    private Integer composeInt(byte hi, byte lo)
    {
        int val = (int) hi & 0xff;
        val *= 256;
        val += (int) lo & 0xff;
        return val;
    }
    
    public void run()
    {
        int ret = 0;
        // As explained on http://developer.android.com/guide/topics/usb/accessory.html, "The Android accessory protocol supports packet buffers up to 16384 bytes, so you can choose to always declare your buffer to be of this size for simplicity."
        byte[] buffer = new byte[16384];
        int i;

        while (ret >= 0)
        {
            try
            {
                ret = mInputStream.read(buffer);
            }
            catch (IOException e)
            {
                break;
            }

            i = 0;
            while (i < ret)
            {
                int len = ret - i;

                switch (buffer[i])
                {
                    case 0x0:
                        if (len >= 3)
                        {
                            Message m = Message.obtain(mHandler,
                                    MESSAGE_TEMPERATURE);
                            m.obj = composeInt(buffer[i + 1], buffer[i + 2]);
                            mHandler.sendMessage(m);
                        }
                        i += 3;
                        break;

                    default:
                        Log.d(TAG, "unknown msg: " + buffer[i]);
                        i = len;
                        break;
                }
            }
        }
    }
    
    private void openAccessory(UsbAccessory accessory)
    {
        mFileDescriptor = mUsbManager.openAccessory(accessory);
        if (mFileDescriptor != null)
        {
            mAccessory = accessory;
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            new Thread(null, this, "AOATempSensor").start();
            Log.d(TAG, "accessory opened");
            showTemp();
        }
        else
        {
            Log.d(TAG, "accessory open fail");
        }
    }
    
    private void closeAccessory()
    {
        hideTemp();

        try
        {
            if (mFileDescriptor != null)
            {
                mFileDescriptor.close();
            }
        }
        catch (IOException e)
        {
            Log.e(TAG, "Error closing file", e);
        }
        finally
        {
            mFileDescriptor = null;
            mAccessory = null;
        }
    }
    
    private void showTemp()
    {
        temperatureValue.setText("");
    }
    
    private void hideTemp()
    {
        temperatureValue.setText("Please connect the accessory.");
    }
}
