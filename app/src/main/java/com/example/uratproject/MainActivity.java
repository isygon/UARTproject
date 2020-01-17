package com.example.uratproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public EditText edtAddr, edtMsg, edtIn, edtOut;
    public ToggleButton _tbtOn, _tbtOf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btnMessage = (Button) findViewById(R.id.btnCreate);
        final Button btnConnetc = findViewById(R.id.btnConnect);

        btnMessage.setOnClickListener(this);
        btnConnetc.setOnClickListener(this);

    }

    private void createMsg(EditText edtMsg, EditText edtAddr, ToggleButton _tbtOn, ToggleButton _tbtOf, EditText edtOUT) {
        edtMsg.setText("01110000,10100101");
        edtAddr.setText("00001111");
        String _addr = edtAddr.getText().toString();
        if (_tbtOn.isChecked()) {
            _addr = "0" + _addr.substring(1);
        } else if (_tbtOf.isChecked()) _addr = "1" + _addr.substring(1);

        edtAddr.setText(_addr);

        byte[] _msg = formatMessage(edtMsg.getText(), edtAddr.getText().toString());
        String _msgText = "";

        for (int i = 0; i < 4; i++) {
            int a = (_msg[i] > 0) ? (byte) _msg[i] : 256 + _msg[i];
            _msgText = _msgText + String.format("%8s", Integer.toBinaryString(a)).replace(' ', '0') + " ";
        }
        edtOUT.setText(_msgText);
    }

    private void connectUART() {
        String YOUR_DEVICE_NAME;
        byte[] DATA;
        int TIMEOUT;

        UsbManager manager = (UsbManager) getApplicationContext().getSystemService(Context.USB_SERVICE);

        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        TextView txtConnectInfo = (TextView) findViewById(R.id.txtConnect);
        CharSequence _devices = "";
        if (deviceList.size() > 0) {
            for (String _device : deviceList.keySet()) {
                _devices = _devices + " " + _device;
                txtConnectInfo.setText(_devices);
                /*while (deviceIterator.hasNext()) {
                    nodevices = false;
                    device = deviceIterator.next();
                    clog("getDeviceName: " + device.getDeviceName());
                    clog("toString: " + device.toString());
                }*/
                UsbDevice device;
                String s = "";
                int i = 0;
                boolean nodevices = true;
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                while (deviceIterator.hasNext()) {
                    nodevices = false;
                    device = deviceIterator.next();
                    s = s + i + "." + device.getDeviceName() + " " + device.toString();
                    txtConnectInfo.setText(s);
                    dataChange(txtConnectInfo);
                }
                if (nodevices) {
                    txtConnectInfo.setText("No usb");
                }


            }
        } else txtConnectInfo.setText("No devices");



    }


    void dataChange(TextView txtConnectInfo) {
        TextView txtConnectInfo1 = (TextView) findViewById(R.id.txtConnect);
        txtConnectInfo1.setText("start");
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);

        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        UsbSerialDriver driver = availableDrivers.get(0);

        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());

        UsbSerialPort port = driver.getPorts().get(0);


        try {

            port.open(connection);
            txtConnectInfo.setText("Hello World");
            port.setParameters(9600, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

            int usbResultOut, usbResultIn1, usbResultIn2, usbResultIn3;
            String tOut = "S/r", s = "";//This is the data i am sending to serial device.
            byte[] bytesOut = tOut.getBytes(); //convert String to byte[]
            byte[] bytesIn1 = new byte[25];
            byte[] bytesIn2 = new byte[25];
            byte[] bytesIn3 = new byte[25];

            usbResultOut = port.write(bytesOut, 1000); //write the data to serial device.
       //     usbResultIn1 = port.read(bytesIn1, 1000);  //read the data but in my case 0 bytes received.
      //   usbResultIn2 = port.read(bytesIn2, 1000);  //read the data but in my case 0 bytes received.
     // usbResultIn3 = port.read(bytesIn3, 1000);  //read the data, this time the data is received.

            //s = s + "output" + Integer.toString(usbResultIn1);

            EditText edtIn = findViewById(R.id.edtIn);
            edtIn.setText(tOut);//s);
        }
        catch (Exception e) {
            edtIn.setText("ups");
            e.printStackTrace();

        }
    }


    byte[] formatMessage(CharSequence _dateVal, String _addr) {

        String _strDateVal = _dateVal.toString();
        int _data0 = Integer.parseInt(_addr);//TODO: normal string array parse without _addr
        int _data1 = Integer.parseInt(_strDateVal.substring(0, _strDateVal.indexOf(',')), 2);
        int _data2 = Integer.parseInt(_strDateVal.substring(_strDateVal.indexOf(',') + 1, _strDateVal.length()), 2);
        byte[] _OutMsg = new byte[]{(byte) _data0, (byte) _data1, (byte) _data2, (byte) 00000000};
        _OutMsg[3] = CRC(_OutMsg);
        return _OutMsg;
    }


    byte CRC(byte[] _data) {
        int intCRC = 0;

        for (byte i = 0; i < 3; i++) {

            intCRC ^= _data[i];
        }
        byte _CRC = (byte) (intCRC & 0xFF);
        return _CRC;

    }


    @Override
    public void onClick(View v) {
        final ToggleButton _tbtOn = (ToggleButton) findViewById(R.id.tgbOn);
        final ToggleButton _tbtOf = (ToggleButton) findViewById(R.id.tgbOff);

        EditText edtAddr = (EditText) findViewById(R.id.edtAddr);
        EditText edtMsg = (EditText) findViewById(R.id.edtMsg);
        EditText edtIn = (EditText) findViewById(R.id.edtIn);
        EditText edtOut = (EditText) findViewById(R.id.edtOut);
        switch (v.getId()) {

            case R.id.btnCreate:
                createMsg(edtMsg, edtAddr, _tbtOn, _tbtOf, edtOut);
            case R.id.btnConnect:
                connectUART();
        }
    }
}



