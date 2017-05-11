package com.gprinter;

import java.util.Vector;
import java.util.UUID;
import java.io.OutputStream;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ArrayUtils;

import com.facebook.react.bridge.*;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.gprinter.command.EscCommand;
import com.gprinter.command.EscCommand.*;
import com.gprinter.command.GpCom;
import com.gprinter.command.GpCom.*;
import com.gprinter.command.TscCommand;
import com.gprinter.command.TscCommand.*;
import com.gprinter.io.GpDevice;
import com.gprinter.service.GpPrintService;
import com.gprinter.aidl.GpService;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

public class GPrinterModule extends ReactContextBaseJavaModule {

    private static final String DEBUG_TAG = "GPrinterModule";
    private static final String TAG = "GPrinter";
    public static final String CONNECT_STATUS = "connect.status";
    private GpService mGpService;

    private  int mPrinterIndex = 0;

    // 获取到蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter;
    // UUID，蓝牙建立链接需要的
    private final UUID SERVICE_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    // 选中发送数据的蓝牙设备，全局变量，否则连接在方法执行完就结束了
    private BluetoothDevice selectDevice;
    // 获取到选中设备的客户端串口，全局变量，否则连接在方法执行完就结束了
    private BluetoothSocket clientSocket;
    // 获取到向设备写的输出流，全局变量，否则连接在方法执行完就结束了
    private OutputStream os;

    private ServiceConnection connService =  new ServiceConnection() {

    		@Override
    		public void onServiceDisconnected(ComponentName name) {
                Log.i(TAG,"Service disconnected: " +name);
    			mGpService = null;
    		}

    		@Override
    		public void onServiceConnected(ComponentName name, IBinder service) {
    			Log.i(TAG,"Service connected: " +name);
    			mGpService = GpService.Stub.asInterface(service);
    		}
    	};

    public GPrinterModule(ReactApplicationContext reactContext){
        super(reactContext);
        // 获取到蓝牙默认的适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Intent intent = new Intent();
        intent.setPackage("com.gprinter.aidl");
        intent.setAction("com.gprinter.aidl.GpService");
        reactContext.startService(intent);
        reactContext.bindService(intent, connService, Context.BIND_AUTO_CREATE);
    }

    @Override
    public String getName() {
        return TAG;
    }

    @ReactMethod
    public void printReceipt(final ReadableMap options, Callback callBack){
     if(!options.hasKey("address") || options.getString("address") == ""){
                    throw new IllegalArgumentException("Invalid params: address is required.");
                }
                String address = options.getString("address");//蓝牙地址
                EscCommand esc = new EscCommand();
                esc.addPrintAndFeedLines((byte)3);
                esc.addSelectJustification(JUSTIFICATION.CENTER);//设置打印居中
                esc.addSelectPrintModes(FONT.FONTA, ENABLE.OFF,ENABLE.ON, ENABLE.ON, ENABLE.OFF);//设置为倍高倍宽
                esc.addText("Sample\n");   //  打印文字
                esc.addPrintAndLineFeed();

                /*打印文字*/
                esc.addSelectPrintModes(FONT.FONTA, ENABLE.OFF,ENABLE.OFF, ENABLE.OFF, ENABLE.OFF);//取消倍高倍宽
                esc.addSelectJustification(JUSTIFICATION.LEFT);//设置打印左对齐
                esc.addText("Print text\n");   //  打印文字
                esc.addText("Welcome to use Gprinter!\n");   //  打印文字
                esc.addPrintAndLineFeed();
                /*打印图片*/
                esc.addText("Print bitmap!\n");   //  打印文字
    //            		Bitmap b = BitmapFactory.decodeResource(getResources(),
    //            				R.drawable.gprinter);
    //            		esc.addRastBitImage(b,b.getWidth(),0);   //打印图片

                /*打印一维条码*/
                esc.addText("Print code128\n");   //  打印文字
                esc.addSelectPrintingPositionForHRICharacters(HRI_POSITION.BELOW);//设置条码可识别字符位置在条码下方
                esc.addSetBarcodeHeight((byte)60); //设置条码高度为60点
                esc.addCODE128("Gprinter");  //打印Code128码
                esc.addPrintAndLineFeed();

                /*QRCode命令打印
                    此命令只在支持QRCode命令打印的机型才能使用。
                    在不支持二维码指令打印的机型上，则需要发送二维条码图片
                */
                esc.addText("Print QRcode\n");   //  打印文字
                esc.addSelectErrorCorrectionLevelForQRCode((byte)0x31); //设置纠错等级
                esc.addSelectSizeOfModuleForQRCode((byte)3);//设置qrcode模块大小
                esc.addStoreQRCodeData("www.gprinter.com.cn");//设置qrcode内容
                esc.addPrintQRCode();//打印QRCode
                esc.addPrintAndLineFeed();

                /*打印文字*/
                esc.addSelectJustification(JUSTIFICATION.CENTER);//设置打印左对齐
                esc.addText("Completed!\r\n");   //  打印结束
                esc.addPrintAndFeedLines((byte)8);

                Vector<Byte> datas = esc.getCommand(); //发送数据
                Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
                byte[] bytes = ArrayUtils.toPrimitive(Bytes);
                byte[] sendData = Base64.decode(Base64.encodeToString(bytes, Base64.DEFAULT), 0);
                Vector vector = new Vector();
                for (byte b : sendData) {
                     vector.add(Byte.valueOf(b));
                }
                try {
                   ERROR_CODE backCode = writeDataImmediately(address, vector);
                   callBack.invoke("send Data: "+backCode.toString());
                } catch (Exception e) {
                   // TODO Auto-generated catch block
                   e.printStackTrace();
                }
            }

        @ReactMethod
        public void printLabel(final ReadableMap options,final ReadableMap cssOptions, Callback callBack){
            Log.d(DEBUG_TAG, "Print start ");
            if(!options.hasKey("address") || options.getString("address") == ""){
                throw new IllegalArgumentException("Invalid params: address is required.");
            }
            String address = options.getString("address");//蓝牙地址
            String[] text = options.getString("text").split(",");

            String qrCode = options.getString("qrCode");
            String barCode = options.getString("barCode");

            int labelHeight = cssOptions.getInt("labelHeight");
            int labelWidth = cssOptions.getInt("labelWidth");
            int labelGap = cssOptions.getInt("labelGap");
            int textGap = cssOptions.getInt("textGap");
            int textPositionX = cssOptions.getInt("textPositionX");
            int textPositionY = cssOptions.getInt("textPositionY");

            boolean sound = false;
            if(options.hasKey("sound") && options.getBoolean("sound")){
                sound = true;
            }
            TscCommand tsc = new TscCommand();
            tsc.addSize(labelHeight, labelWidth); //设置标签尺寸，按照实际尺寸设置
            tsc.addGap(labelGap);           //设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0
            tsc.addDirection(DIRECTION.FORWARD,MIRROR.NORMAL);//设置打印方向
            tsc.addReference(0, 0);//设置原点坐标
            tsc.addTear(ENABLE.ON); //撕纸模式开启
            tsc.addCls();// 清除打印缓冲区
            //绘制简体中文
            for(int i=0;i<text.length;i++){
                try{
                    byte[] temp = text[i].getBytes("UTF-8");
                    String temStr = new String(temp,"UTF-8");
                    text[i] = new String(temStr.getBytes("GB2312"),"GB2312");//打印的文字
                }catch(Exception e){
                     Toast.makeText(getReactApplicationContext(), "Message: "+e.getMessage(),
                                      Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                tsc.addText(textPositionX,textPositionY,FONTTYPE.SIMPLIFIED_CHINESE/*字体类型*/,
                                ROTATION.ROTATION_0/*旋转角度*/,FONTMUL.MUL_1/*横向放大*/,FONTMUL.MUL_1/*纵向放大*/,text[i]+"\n");
                textPositionY+=textGap;
            }

            //绘制图片
//          Bitmap b = BitmapFactory.decodeResource(getResources(),
//            				R.drawable.gprinter);
//          tsc.addBitmap(20,50, BITMAP_MODE.OVERWRITE, b.getWidth(),b);

            if(StringUtils.isNotBlank(qrCode)){
                tsc.addQRCode(cssOptions.getInt("qrCodePositionX"), cssOptions.getInt("qrCodePositionY"), EEC.LEVEL_L,cssOptions.getInt("qrCodeWidth"),ROTATION.ROTATION_0, qrCode);
            }
            if(StringUtils.isNotBlank(barCode)){
                //绘制一维条码
                tsc.add1DBarcode(cssOptions.getInt("barCodePositionX"), cssOptions.getInt("barCodePositionY"), BARCODETYPE.CODE128, 40, READABEL.EANBEL, ROTATION.ROTATION_0, barCode);
            }

            tsc.addPrint(1,1); // 打印标签
            if(sound){
                tsc.addSound(2, 100); //打印标签后 蜂鸣器响
            }
            Vector<Byte> datas = tsc.getCommand(); //发送数据
            Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
            byte[] bytes = ArrayUtils.toPrimitive(Bytes);
            byte[] sendData = Base64.decode(Base64.encodeToString(bytes, Base64.DEFAULT), 0);
            Vector vector = new Vector();
            for (byte b : sendData) {
                vector.add(Byte.valueOf(b));
            }
            try {
                ERROR_CODE backCode = writeDataImmediately(address, vector);
                callBack.invoke("send Data: "+backCode.toString());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @ReactMethod
        public void calibration(final String address){
            TscCommand tsc = new TscCommand();
            tsc.addHome();
            tsc.addSound(2, 100); // 蜂鸣器
            Vector<Byte> Command = new Vector<Byte>(4096, 1024);
            Command = tsc.getCommand();// 获取上面编辑的打印命令
            writeDataImmediately(address,Command);
        }

       @ReactMethod
       public void setCutOn(final String address){
            cutSetting(address, ENABLE.ON);
       }

       @ReactMethod
       public void setCutOff(final String address){
            cutSetting(address, ENABLE.OFF);
       }

      private void cutSetting(String address, ENABLE able){
          TscCommand tsc = new TscCommand();
          tsc.addCutter(able);//设置切刀是否有效
          tsc.addSound(2, 100); // 蜂鸣器
          Vector<Byte> Command = new Vector<Byte>(4096, 1024);
          Command = tsc.getCommand();
          writeDataImmediately(address,Command); // 发送命令
      }

        private ERROR_CODE writeDataImmediately(final String address, Vector<Byte> data){
                Log.d(DEBUG_TAG, "write void in. "+address);
                ERROR_CODE retval = ERROR_CODE.SUCCESS;
                //通过地址获取到该设备
                try{
                    selectDevice = mBluetoothAdapter.getRemoteDevice(address);
                }catch(Exception e){
                    Log.d(DEBUG_TAG, "get remote device failed"+e.getMessage());
                }
                // 这里需要try catch一下，以防异常抛出
                try {
                    // 获取到客户端接口
                    clientSocket = selectDevice
                            .createRfcommSocketToServiceRecord(SERVICE_UUID);
                    // 判断客户端接口是否连接
                    Log.d(DEBUG_TAG, "get client socket.");
                    try{
                         // 向服务端发送连接
                        clientSocket.connect();
                        // 获取到输出流，向外写数据
                        os = clientSocket.getOutputStream();
                    }catch(Exception e){
                        Log.d(DEBUG_TAG, "Try connect again: "+e.getMessage());
                        try{
                            clientSocket = (BluetoothSocket) selectDevice.getClass().getMethod("createRfcommSocket",
                                                                            new Class[]{int.class}).invoke(selectDevice,1);
                            clientSocket.connect();
                        }catch(Exception e2){
                             Log.e("", "Couldn't establish Bluetooth connection!");
                        }
                    }

                    // 判断是否拿到输出流
                    if (os != null) {
                        Log.d(DEBUG_TAG, "os not null,start to write.");
                        // 需要发送的信息
                        if ((data != null) && (data.size() > 0)) {
                        Log.d(DEBUG_TAG, "print message"+data.size());
                              byte[] sendData = new byte[data.size()];

                              if (data.size() > 0) {
                              Log.d(DEBUG_TAG, "arrange data.");
                                for (int i = 0; i < data.size(); i++) {
                                  sendData[i] = ((Byte)data.get(i)).byteValue();
                                }

                                try
                                {
                                Log.d(DEBUG_TAG, "start to write");
                                  os.write(sendData);
                                  Log.d(DEBUG_TAG, "write end");
                                  os.flush();
                                } catch (Exception e) {
                                  Log.d(DEBUG_TAG, "Error to write data: " + e.getMessage());
                                  Toast.makeText(getReactApplicationContext(), "获取OutputStream失败",
                                                                            Toast.LENGTH_LONG).show();
                                  retval = ERROR_CODE.FAILED;
                                }
                              }
                            }
                    }
                } catch (Exception e) {
                    retval = ERROR_CODE.FAILED;
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Toast.makeText(getReactApplicationContext(), "Send failed",
                                          Toast.LENGTH_LONG).show();
                }
                return retval;
              }


}
