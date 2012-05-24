/*
 * Copyright 2011 Greg Milette and Adam Stroud
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package root.gast.playground.sensor.aoa;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import root.gast.R;
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

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

/**
 * TODO
 * 
 * @author David Hutchison &#60;<a href="mailto:david.n.hutch@gmail.com">david.n.hutch@gmail.com</a>&#62;
 * @author Adam Stroud &#60;<a href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public class BaseActivity extends Activity implements Runnable
{
    private static final String TAG = "AOA,BaseActivity";
    private static final String ACTION_USB_PERMISSION =
            "com.example.aoaTempSensor.action.USB_PERMISSION";

    private UsbManager mUsbManager;
    private PendingIntent mPermissionIntent;
    private boolean mPermissionRequestPending;
    private AccessoryController mAccController;

    UsbAccessory mAccessory;
    ParcelFileDescriptor mFileDescriptor;
    FileInputStream mInputStream;
    FileOutputStream mOutputStream;

    private static final int MESSAGE_TEMPERATURE = 2;

    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mUsbManager = UsbManager.getInstance(this);
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

        setContentView(R.layout.aoa_base);

        accessoryAttached(false);

        if (mAccessory != null)
        {
            showTemp();
        }
        else
        {
            hideTemp();
        }
    }

    /**
     * @see android.app.Activity#onResume()
     */
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

    /**
     * @see android.app.Activity#onPause()
     */
    @Override
    public void onPause()
    {
        super.onPause();
        closeAccessory();
    }

    /**
     * @see android.app.Activity#onDestroy()
     */
    @Override
    public void onDestroy()
    {
        unregisterReceiver(mUsbReceiver);
        super.onDestroy();
    }
    
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
                    UsbAccessory accessory = UsbManager.getAccessory(intent);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
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
                UsbAccessory accessory = UsbManager.getAccessory(intent);
                
                if (accessory != null && accessory.equals(mAccessory))
                {
                    closeAccessory();
                }
            }
        }
    };

    /**
     * @see android.app.Activity#onRetainNonConfigurationInstance()
     */
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

    /**
     * TODO
     * 
     * @author David Hutchison &#60;<a href="mailto:david.n.hutch@gmail.com">david.n.hutch@gmail.com</a>&#62;
     * @author Adam Stroud &#60;<a href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
     */
    protected class TemperatureMsg
    {
        private int temperature;

        /**
         * TODO
         * 
         * @param temperature
         */
        public TemperatureMsg(int temperature)
        {
            this.temperature = temperature;
        }

        /**
         * TODO
         * 
         * @return
         */
        public int getTemperature()
        {
            return temperature;
        }
    }

    /**
     * TODO
     * 
     * @param t
     */
    protected void handleTemperatureMessage(TemperatureMsg t)
    {
        if (mAccController != null)
        {
            mAccController.setTemperature(t.getTemperature());
        }
    }

    /**
     * TODO
     * 
     * @param hi
     * @param lo
     * @return
     */
    private int composeInt(byte hi, byte lo)
    {
        int val = (int) hi & 0xff;
        val *= 256;
        val += (int) lo & 0xff;
        return val;
    }

    /**
     * TODO
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run()
    {
        int ret = 0;
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

                    case 0x4:
                        if (len >= 3)
                        {
                            Message m = Message.obtain(mHandler,
                                    MESSAGE_TEMPERATURE);
                            m.obj = new TemperatureMsg(composeInt(
                                    buffer[i + 1], buffer[i + 2]));
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

    Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {

                case MESSAGE_TEMPERATURE:
                    TemperatureMsg t = (TemperatureMsg) msg.obj;
                    handleTemperatureMessage(t);
                    break;

            }
        }
    };

    /**
     * TODO
     * 
     * @param accessory
     */
    private void openAccessory(UsbAccessory accessory)
    {
        mFileDescriptor = mUsbManager.openAccessory(accessory);
        if (mFileDescriptor != null)
        {
            mAccessory = accessory;
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);
            Thread thread = new Thread(null, this, "AOATempSensor");
            thread.start();
            Log.d(TAG, "accessory opened");
            accessoryAttached(true);
        } else
        {
            Log.d(TAG, "accessory open fail");
        }
    }

    /**
     * TODO
     */
    private void closeAccessory()
    {
        accessoryAttached(false);

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

    /**
     * TODO
     * 
     * @param enable
     */
    protected void accessoryAttached(boolean enable)
    {
        if (enable)
        {
            showTemp();
        } else
        {
            hideTemp();
        }
    }

    /**
     * TODO
     */
    protected void showTemp()
    {
        setContentView(R.layout.main);
        mAccController = new AccessoryController(this);
        mAccController.onAccessoryAttached();
    }

    /**
     * TODO
     */
    protected void hideTemp()
    {
        setContentView(R.layout.no_device);
        mAccController = null;
    }
}
