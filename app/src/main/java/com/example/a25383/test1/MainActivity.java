package com.example.a25383.test1;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothLeScanner mBluetoothLeScanner;
    private Device mDevice = new Device();
    private ProgressBar progressBar;
    private BluetoothGatt mBluetoothGatt;
    private static final long SCAN_PERIOD = 10000;


    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.i("ScanCallback","onBatchScanResults");
        }

        @Override
        public void onScanResult(final int callbackType, final ScanResult result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDevice.add_device(result.getDevice());
                    Log.i("onScanResult",result.getDevice().getName()+" "+callbackType);
                }
            });
        }
    };

    private void ConnectBluetooth()
    {
        Handler mHandler = new Handler();
        OpenBluetooth();
        //Set<BluetoothDevice> pairedDevice = mBluetoothAdapter.getBondedDevices();
        //if (pairedDevice.size()>0){}
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothLeScanner.stopScan(mScanCallback);
                Log.i("stopScan","STOP the BLE Scan!");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder BlueDevice =new AlertDialog.Builder(MainActivity.this);
                        BlueDevice.setTitle("附近的蓝牙");
                        Log.i("BroadcastReceiver","开始显示2  "+mDevice.getDevice().length);
                        BlueDevice.setItems(mDevice.getDevice(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Toast.makeText(MainActivity.this,""+mDevice.getmDevice(which).getName(),Toast.LENGTH_SHORT).show();
                                mBluetoothGatt = mDevice.getmDevice(which).connectGatt(MainActivity.this,false,mBluetoothGattCallback);
                            }
                        }).create().show();
                    }
                });
            }
        }, SCAN_PERIOD);

        mDevice.clear();
        mBluetoothLeScanner.startScan(mScanCallback);
        Toast.makeText(this,"正在搜索蓝牙",Toast.LENGTH_SHORT).show();
    }

    private final BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                Log.i("onServicesDiscovered", "GATT_SUCCESS");
                WriteData(mBluetoothGatt);
            }
            else {
                Log.i("onServicesDiscovered", ""+status);
            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if(newState == BluetoothProfile.STATE_CONNECTED){
                Log.i("BluetoothGattCallback","STATE_CONNECTED");
                Log.i("BluetoothGattCallback","discover Services"+mBluetoothGatt.discoverServices());


            }
            else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                Log.i("BluetoothGattCallback","STATE_DISCONNECTED");
                close();
            }
        }

    };

    void close(){
        mBluetoothGatt.disconnect();
        mBluetoothGatt.close();
    }

    private void WriteData(BluetoothGatt bluetoothGatt){
        String UUID_CONNECT_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";
        String UUID_CONNECT_CHARACT = "0000ffe1-0000-1000-8000-00805f9b34fb";

        byte [] Array = "Hello World".getBytes();

        BluetoothGattService mbluetoothGattService = bluetoothGatt.getService(UUID.fromString(UUID_CONNECT_SERVICE));

        BluetoothGattCharacteristic mbluetoothGattCharacteristic = mbluetoothGattService.getCharacteristic(UUID.fromString(UUID_CONNECT_CHARACT));

        mbluetoothGattCharacteristic.setValue(Array);
        bluetoothGatt.writeCharacteristic(mbluetoothGattCharacteristic);
    }

    private void OpenBluetooth()
    {
        if(mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth,1);
        }else{
            if(mBluetoothAdapter!= null){
                Toast.makeText(MainActivity.this,"蓝牙已打开"+mBluetoothAdapter.getName(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);


        Button linkBlue = findViewById(R.id.open_blue);
        linkBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenBluetooth();
            }
        });

        Button openblue = findViewById(R.id.link_blue);
        openblue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectBluetooth();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "蓝牙已开启", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "蓝牙未开启", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

}

class Device{
    private String [] NDevice;
    private String [] ODevice;
    private BluetoothDevice [] mDevice;
    private BluetoothDevice [] nDevice;
    private int sizen;
    private int size;
    Device()
    {
        size=0;
        sizen=0;
        ODevice = new String[sizen];
        nDevice = new BluetoothDevice[size];
    }
    void clear()
    {
        size=0;
        sizen=0;
        ODevice = new String[sizen];
        nDevice = new BluetoothDevice[size];
    }
    void add(String name){
        Log.v("Save name",name);
        sizen++;
        NDevice = new String[sizen];
        for(int i=0;i<sizen-1;i++)
        {
            NDevice[i]=ODevice[i];
        }
        NDevice[sizen-1]=name;
        ODevice = new String[sizen];
        for(int i=0;i<sizen;i++)
        {
            ODevice[i]=NDevice[i];
        }
    }

    void add_device(BluetoothDevice device){
        Log.v("Save device",""+device.getName());
        add(""+device.getName());
        size++;
        mDevice = new BluetoothDevice[size];
        for(int i=0;i<size-1;i++)
        {
            mDevice[i]=nDevice[i];
        }
        mDevice[size-1]=device;
        nDevice = new BluetoothDevice[size];
        for(int i=0;i<size;i++)
        {
            nDevice[i]=mDevice[i];
        }
    }


    public String [] getDevice(){
        return ODevice;
    }

    public BluetoothDevice getmDevice(int which){
        return nDevice[which];
    }
}

