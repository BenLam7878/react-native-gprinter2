package com.gprinter;

import android.content.*;
import android.os.*;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.facebook.react.bridge.*;
import com.gprinter.aidl.GpService;
import com.gprinter.command.EscCommand;
import com.gprinter.command.GpCom;
import com.gprinter.command.TscCommand;
import com.gprinter.io.GpDevice;
import com.gprinter.io.PortParameters;
import com.gprinter.service.GpPrintService;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

/**
 * Created by januslo on 2017/5/27.
 */
public class GPrinterModule2 extends ReactContextBaseJavaModule {
    private static final String TAG = "GPrinter2";
    private GpService mGpService = null;
    public static final String CONNECT_STATUS = "connect.status";
    public static final String ACTION_CONNECT_STATUS = "action.connect.status";
    private PrinterServiceConnection conn = null;
    private int mPrinterIndex = 0;
    private static final long TIME_OUT = 20000;

    class OpenportBroadcastReceiver extends BroadcastReceiver {
        Handler callbackHander = null;

        public OpenportBroadcastReceiver(Handler callbackHander) {
            this.callbackHander = callbackHander;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_CONNECT_STATUS.equals(intent.getAction())) {
                int type = intent.getIntExtra(GpPrintService.CONNECT_STATUS, 0);
                int id = intent.getIntExtra(GpPrintService.PRINTER_ID, 0);
                Log.d(TAG, "connect status " + type);
                if (type == GpDevice.STATE_CONNECTING) {
                    Log.d(TAG, "connecting: " + type);
                } else if (type == GpDevice.STATE_NONE) {
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString(ACTION_CONNECT_STATUS, "Connection Lost.");
                    msg.setData(bundle);
                    Log.d(TAG, "connection lost: " + type);
                    callbackHander.sendMessage(msg);
                } else if (type == GpDevice.STATE_INVALID_PRINTER) {
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString(ACTION_CONNECT_STATUS, "Invalid Printer.");
                    msg.setData(bundle);
                    Log.d(TAG, "Invalid Printer: " + type);
                    callbackHander.sendMessage(msg);
                } else if (type == GpDevice.STATE_LISTEN) {
                    Log.d(TAG, "listening: " + type);
                } else if (type == GpDevice.STATE_VALID_PRINTER) {
                    Log.d(TAG, "Valid Printer: " + type);
                    callbackHander.sendEmptyMessage(1);
                } else if (type == GpDevice.STATE_CONNECTED) {
                    Log.d(TAG, "connected: " + type);
                    callbackHander.sendEmptyMessage(2);
                }

            }
        }
    }

    private OpenportBroadcastReceiver currentReceiver;

    class PrinterServiceConnection implements ServiceConnection {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("ServiceConnection", "onServiceDisconnected() called");
            mGpService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Toast.makeText(getReactApplicationContext(), "Service Connected",
                    Toast.LENGTH_SHORT).show();
            Log.i("ServiceConnection", "onServiceConnected() called");
            mGpService = GpService.Stub.asInterface(service);
        }
    }

    private void startService() {
        Intent i = new Intent(this.getReactApplicationContext(), GpPrintService.class);
        this.getReactApplicationContext().startService(i);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void connection() {
        conn = new PrinterServiceConnection();
        Intent intent = new Intent("com.gprinter.aidl.GpPrintService");
        intent.setPackage(this.getReactApplicationContext().getPackageName());
        this.getReactApplicationContext().bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }


    @Override
    public String getName() {
        return TAG;
    }

    public GPrinterModule2(ReactApplicationContext reactContext) {
        super(reactContext);
        startService();
        connection();
    }

    private void openPort(String address, final Handler callbackHander) throws Exception {
        int rel = mGpService.openPort(0, PortParameters.BLUETOOTH, address, 0);
        GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rel];
        if (r == GpCom.ERROR_CODE.DEVICE_ALREADY_OPEN) {
            callbackHander.sendEmptyMessage(1);
        } else {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_CONNECT_STATUS);
            currentReceiver = new OpenportBroadcastReceiver(callbackHander);
            this.getReactApplicationContext().registerReceiver(currentReceiver, filter);
            Log.i(TAG, "register");
        }
    }

    @ReactMethod
    public void printTestPage(final String address, final Promise promise) {
        final long startTime = new Date().getTime();
        try {//"DC:0D:30:04:33:69"
            this.openPort(address, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    if (msg != null && msg.getData().get(ACTION_CONNECT_STATUS) != null) {
                        Log.d(TAG, msg.getData().get(ACTION_CONNECT_STATUS).toString());
                        if (currentReceiver != null) {
                            Log.i(TAG, "unregister 0");
                            getReactApplicationContext().unregisterReceiver(currentReceiver);
                            currentReceiver = null;
                        }
                        promise.reject(new Exception(msg.getData().get(ACTION_CONNECT_STATUS).toString()));
                    } else if (msg != null && msg.what == 1) {
                        try {
                            TscCommand tsc = new TscCommand();
                            tsc.addHome();
                            Vector<Byte> datas = tsc.getCommand(); //发送数据
                            Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
                            byte[] bytes = ArrayUtils.toPrimitive(Bytes);
                            String str = Base64.encodeToString(bytes, Base64.DEFAULT);
                            int rel = mGpService.sendTscCommand(0, str);
                            Log.i(TAG, "home rel " + rel);
                            GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rel];
                            if (r != GpCom.ERROR_CODE.SUCCESS) {
                                if (new Date().getTime() - startTime > TIME_OUT) {
                                    promise.reject(new TimeoutException(TIME_OUT + " Seconds Time out."));
                                } else {
                                    printTestPage(address, promise);
                                }
                            } else {
                                rel = mGpService.printeTestPage(mPrinterIndex);
                                Log.i(TAG, "rel " + rel);
                                r = GpCom.ERROR_CODE.values()[rel];
                                if (r != GpCom.ERROR_CODE.SUCCESS) {
                                    promise.reject(new Exception(GpCom.getErrorText(r)));
                                } else {
                                    promise.resolve(null);
                                }
                            }

                        } catch (Exception e) {
                            promise.reject(e);
                        } finally {
                            if (currentReceiver != null) {
                                Log.i(TAG, "unregister");
                                getReactApplicationContext().unregisterReceiver(currentReceiver);
                                currentReceiver = null;
                            }
                        }
                    } else if (msg != null && msg.what == 2) {
                        Log.i(TAG, "Recalling  " + msg.what);
                        if (new Date().getTime() - startTime > TIME_OUT) {
                            promise.reject(new TimeoutException(TIME_OUT + " Seconds Time out."));
                        } else {
                            try {
                                Thread.sleep(1000);
                            } catch (Exception e) {
                                // ignore.
                            }
                            printTestPage(address, promise);
                        }
                        if (currentReceiver != null) {
                            Log.i(TAG, "unregister 2");
                            getReactApplicationContext().unregisterReceiver(currentReceiver);
                            currentReceiver = null;
                        }
                    }
                    return true;
                }
            }));

        } catch (Exception e1) {
            promise.reject(e1);
        }
    }


    @ReactMethod
    public void printReceipt(final ReadableMap options, final Promise promise) {
        if (!options.hasKey("address") || options.getString("address") == "") {
            throw new IllegalArgumentException("Invalid params: address is required.");
        }
        String address = options.getString("address");//蓝牙地址
        EscCommand esc = new EscCommand();
        esc.addPrintAndFeedLines((byte) 3);
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印居中
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);//设置为倍高倍宽
        esc.addText("Sample\n");   //  打印文字
        esc.addPrintAndLineFeed();

                /*打印文字*/
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);//取消倍高倍宽
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);//设置打印左对齐
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
        esc.addSelectPrintingPositionForHRICharacters(EscCommand.HRI_POSITION.BELOW);//设置条码可识别字符位置在条码下方
        esc.addSetBarcodeHeight((byte) 60); //设置条码高度为60点
        esc.addCODE128("Gprinter");  //打印Code128码
        esc.addPrintAndLineFeed();

                /*QRCode命令打印
                    此命令只在支持QRCode命令打印的机型才能使用。
                    在不支持二维码指令打印的机型上，则需要发送二维条码图片
                */
        esc.addText("Print QRcode\n");   //  打印文字
        esc.addSelectErrorCorrectionLevelForQRCode((byte) 0x31); //设置纠错等级
        esc.addSelectSizeOfModuleForQRCode((byte) 3);//设置qrcode模块大小
        esc.addStoreQRCodeData("www.gprinter.com.cn");//设置qrcode内容
        esc.addPrintQRCode();//打印QRCode
        esc.addPrintAndLineFeed();

                /*打印文字*/
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);//设置打印左对齐
        esc.addText("Completed!\r\n");   //  打印结束
        esc.addPrintAndFeedLines((byte) 8);
        sendEscCommand(esc, address, promise);
    }

    @ReactMethod
    public void printLabel(final ReadableMap options, final Promise promise) {
        if (!options.hasKey("address") || options.getString("address") == "") {
            promise.reject("INVALID_PARAMS", "Invalid params: address is required.");
            return;
        }
        String address = options.getString("address");//蓝牙地址
        int width = options.getInt("width");
        int height = options.getInt("height");
        int gap = options.hasKey("gap") ? options.getInt("gap") : 0;
        TscCommand.SPEED speed = options.hasKey("speed")?this.findSpeed(options.getInt("speed")):null;
        EscCommand.ENABLE tear = options.hasKey("tear") ?
                options.getInt("tear") == EscCommand.ENABLE.ON.getValue() ? EscCommand.ENABLE.ON : EscCommand.ENABLE.OFF
                : EscCommand.ENABLE.OFF;
        ReadableArray texts = options.getArray("text");
        ReadableArray qrCodes = options.getArray("qrcode");
        ReadableArray barCodes = options.getArray("barcode");
        ReadableArray images = options.getArray("image");
        ReadableArray reverses = options.getArray("reverse");

        TscCommand.DIRECTION direction = options.hasKey("direction") ?
                TscCommand.DIRECTION.BACKWARD.getValue() == options.getInt("direction") ? TscCommand.DIRECTION.BACKWARD : TscCommand.DIRECTION.FORWARD
                : TscCommand.DIRECTION.FORWARD;
        TscCommand.MIRROR mirror = options.hasKey("mirror") ?
                TscCommand.MIRROR.MIRROR.getValue() == options.getInt("mirror") ? TscCommand.MIRROR.MIRROR : TscCommand.MIRROR.NORMAL
                : TscCommand.MIRROR.NORMAL;
        TscCommand.DENSITY density = options.hasKey("density")?this.findDensity(options.getInt("density")):null;
        ReadableArray reference = options.getArray("reference");

        boolean sound = false;
        if (options.hasKey("sound") && options.getInt("sound") == 1) {
            sound = true;
        }
        TscCommand tsc = new TscCommand();
        if(speed != null){
            tsc.addSpeed(speed);//设置打印速度
        }
        if(density != null){
            tsc.addDensity(density);//设置打印浓度
        }
        tsc.addSize(width,height); //设置标签尺寸，按照实际尺寸设置
        tsc.addGap(gap);           //设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0
        tsc.addDirection(direction, mirror);//设置打印方向
        //设置原点坐标
        if (reference != null && reference.size() == 2) {
            tsc.addReference(reference.getInt(0), reference.getInt(1));
        } else {
            tsc.addReference(0, 0);
        }
        tsc.addTear(tear); //撕纸模式开启
        tsc.addCls();// 清除打印缓冲区
        //绘制简体中文
        for (int i = 0; i < texts.size(); i++) {
            ReadableMap text = texts.getMap(i);
            String t = text.getString("text");
            int x = text.getInt("x");
            int y = text.getInt("y");
            TscCommand.FONTTYPE fonttype = this.findFontType(text.getString("fonttype"));
            TscCommand.ROTATION rotation = this.findRotation(text.getInt("rotation"));
            TscCommand.FONTMUL xscal = this.findFontMul(text.getInt("xscal"));
            TscCommand.FONTMUL yscal = this.findFontMul(text.getInt("xscal"));
            boolean bold = text.hasKey("bold") && text.getBoolean("bold");

            try {
                byte[] temp = t.getBytes("UTF-8");
                String temStr = new String(temp, "UTF-8");
                t = new String(temStr.getBytes("GB2312"), "GB2312");//打印的文字
            } catch (Exception e) {
                promise.reject("INVALID_TEXT", e);
                return;
            }

            tsc.addText(x, y, fonttype/*字体类型*/,
                    rotation/*旋转角度*/, xscal/*横向放大*/, yscal/*纵向放大*/, t);

            if(bold){
//                tsc.addText(x-1, y, fonttype,
//                        rotation, xscal, yscal, t/*这里的t可能需要替换成同等长度的空格*/);
                tsc.addText(x+1, y, fonttype,
                        rotation, xscal, yscal, t/*这里的t可能需要替换成同等长度的空格*/);
//                tsc.addText(x, y-1, fonttype,
//                        rotation, xscal, yscal, t/*这里的t可能需要替换成同等长度的空格*/);
                tsc.addText(x, y+1, fonttype,
                        rotation, xscal, yscal, t/*这里的t可能需要替换成同等长度的空格*/);
            }
        }

        //绘制图片
//          Bitmap b = BitmapFactory.decodeResource(getResources(),
//            				R.drawable.gprinter);
//          tsc.addBitmap(20,50, BITMAP_MODE.OVERWRITE, b.getWidth(),b);

        if(images != null){
            for (int i = 0; i < images.size(); i++) {
                ReadableMap img = images.getMap(i);
                int x = img.getInt("x");
                int y = img.getInt("y");
                int imgWidth = img.getInt("width");
                TscCommand.BITMAP_MODE mode = this.findBitmapMode(img.getInt("mode"));
                String image  = img.getString("image");
                byte[] decoded = Base64.decode(image, Base64.DEFAULT);
                Bitmap b = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                tsc.addBitmap(x,y, mode, imgWidth,b);
            }
        }

        if (qrCodes != null) {
            for (int i = 0; i < qrCodes.size(); i++) {
                ReadableMap qr = qrCodes.getMap(i);
                int x = qr.getInt("x");
                int y = qr.getInt("y");
                int qrWidth = qr.getInt("width");
                TscCommand.EEC level = this.findEEC(qr.getString("level"));
                TscCommand.ROTATION rotation = this.findRotation(qr.getInt("rotation"));
                String code = qr.getString("code");
                tsc.addQRCode(x, y, level, qrWidth, rotation, code);
            }
        }
        if (barCodes != null) {
            for (int i = 0; i < barCodes.size(); i++) {
                ReadableMap bar = barCodes.getMap(i);
                int x = bar.getInt("x");
                int y = bar.getInt("y");
                int barHeight = bar.getInt("height");
                TscCommand.ROTATION rotation = this.findRotation(bar.getInt("rotation"));
                String code = bar.getString("code");
                TscCommand.BARCODETYPE type = this.findBarcodeType(bar.getString("type"));
                TscCommand.READABEL readabel = this.findReadabel(bar.getInt("readabel"));
                tsc.add1DBarcode(x, y, type, barHeight, readabel, rotation, code);
            }
        }

        if(reverses != null){
            for(int i=0; i < reverses.size(); i++){
                ReadableMap area = reverses.getMap(i);
                int ax = area.getInt("x");
                int ay = area.getInt("y");
                int aWidth = area.getInt("width");
                int aHeight = area.getInt("height");
                tsc.addReverse(ax,ay,aWidth,aHeight);
            }
        }

        tsc.addPrint(1, 1); // 打印标签
        if (sound) {
            tsc.addSound(2, 100); //打印标签后 蜂鸣器响
        }
        sendTscCommand(tsc, address, promise);
    }

    private TscCommand.BARCODETYPE findBarcodeType(String type) {
        TscCommand.BARCODETYPE barcodeType = TscCommand.BARCODETYPE.CODE128;
        for (TscCommand.BARCODETYPE t : TscCommand.BARCODETYPE.values()) {
            if (StringUtils.equals(t.getValue(), type)) {
                barcodeType = t;
                break;
            }
        }
        return barcodeType;
    }

    private TscCommand.EEC findEEC(String level) {
        TscCommand.EEC eec = TscCommand.EEC.LEVEL_L;
        for (TscCommand.EEC e : TscCommand.EEC.values()) {
            if (StringUtils.equals(e.getValue(), level)) {
                eec = e;
                break;
            }
        }
        return eec;
    }

    private TscCommand.READABEL findReadabel(int readabel) {
        TscCommand.READABEL ea = TscCommand.READABEL.EANBEL;
        if (TscCommand.READABEL.DISABLE.getValue() == readabel) {
            ea = TscCommand.READABEL.DISABLE;
        }
        return ea;
    }

    private TscCommand.FONTMUL findFontMul(int scan) {
        TscCommand.FONTMUL mul = TscCommand.FONTMUL.MUL_1;
        for (TscCommand.FONTMUL m : TscCommand.FONTMUL.values()) {
            if (m.getValue() == scan) {
                mul = m;
                break;
            }
        }
        return mul;
    }

    private TscCommand.ROTATION findRotation(int rotation) {
        TscCommand.ROTATION rt = TscCommand.ROTATION.ROTATION_0;
        for (TscCommand.ROTATION r : TscCommand.ROTATION.values()) {
            if (r.getValue() == rotation) {
                rt = r;
                break;
            }
        }
        return rt;
    }

    private TscCommand.FONTTYPE findFontType(String fonttype) {
        TscCommand.FONTTYPE ft = TscCommand.FONTTYPE.SIMPLIFIED_CHINESE;
        for (TscCommand.FONTTYPE f : TscCommand.FONTTYPE.values()) {
            if (StringUtils.equals(f.getValue(), fonttype)) {
                ft = f;
                break;
            }
        }
        return ft;
    }

    private TscCommand.BITMAP_MODE findBitmapMode(int mode){
        TscCommand.BITMAP_MODE bm = TscCommand.BITMAP_MODE.OVERWRITE;
        for (TscCommand.BITMAP_MODE m : TscCommand.BITMAP_MODE.values()) {
            if (m.getValue() == mode) {
                bm = m;
                break;
            }
        }
        return bm;
    }

    private TscCommand.SPEED findSpeed(int speed){
        TscCommand.SPEED sd = null;
        switch(speed){
            case 1:
                sd = TscCommand.SPEED.SPEED1DIV5;
                break;
            case 2:
                sd = TscCommand.SPEED.SPEED2;
                break;
            case 3:
                sd = TscCommand.SPEED.SPEED3;
                break;
            case 4:
                sd = TscCommand.SPEED.SPEED4;
                break;
        }
        return sd;
    }

    private TscCommand.DENSITY findDensity(int density){
        TscCommand.DENSITY ds = null;
        for (TscCommand.DENSITY d : TscCommand.DENSITY.values()) {
            if (d.getValue() == density) {
                ds = d;
                break;
            }
        }
        return ds;
    }

    @ReactMethod
    public void calibration(final String address, final Promise promise) {
        TscCommand tsc = new TscCommand();
        tsc.addHome();
        tsc.addSound(2, 100); // 蜂鸣器
        sendTscCommand(tsc, address, promise);
    }

    @ReactMethod
    public void setCutOn(final String address, final Promise promise) {
        cutSetting(address, EscCommand.ENABLE.ON, promise);
    }

    @ReactMethod
    public void setCutOff(final String address, final Promise promise) {
        cutSetting(address, EscCommand.ENABLE.OFF, promise);
    }

    private void cutSetting(String address, EscCommand.ENABLE able, final Promise promise) {
        TscCommand tsc = new TscCommand();
        tsc.addCutter(able);//设置切刀是否有效
        tsc.addSound(2, 100); // 蜂鸣器
        sendTscCommand(tsc, address, promise); // 发送命令
    }

    public void sendEscCommand(final EscCommand command, final String address, final Promise promise) {

        final long startTime = new Date().getTime();
        try {
            this.openPort(address, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    if (msg != null && msg.getData().get(ACTION_CONNECT_STATUS) != null) {
                        Log.d(TAG, msg.getData().get(ACTION_CONNECT_STATUS).toString());
                        if (currentReceiver != null) {
                            Log.i(TAG, "unregister 0");
                            getReactApplicationContext().unregisterReceiver(currentReceiver);
                            currentReceiver = null;
                        }
                        promise.reject(new Exception(msg.getData().get(ACTION_CONNECT_STATUS).toString()));
                    } else if (msg != null && msg.what == 1) {
                        try {
                            Vector<Byte> datas = command.getCommand(); //发送数据
                            Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
                            byte[] bytes = ArrayUtils.toPrimitive(Bytes);
                            String str = Base64.encodeToString(bytes, Base64.DEFAULT);
                            int rel = mGpService.sendEscCommand(0, str);
                            Log.i(TAG, "tcs rel " + rel);
                            GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rel];
                            if (r != GpCom.ERROR_CODE.SUCCESS) {
                                if (new Date().getTime() - startTime > TIME_OUT) {
                                    promise.reject(new TimeoutException());
                                } else {
                                    sendEscCommand(command, address, promise);
                                }
                            } else {
                                promise.resolve(null);
                            }

                        } catch (Exception e) {
                            promise.reject(e);
                        } finally {
                            if (currentReceiver != null) {
                                Log.i(TAG, "unregister");
                                getReactApplicationContext().unregisterReceiver(currentReceiver);
                                currentReceiver = null;
                            }
                        }
                    } else if (msg != null && msg.what == 2) {
                        Log.i(TAG, "Recalling  " + msg.what);
                        if (new Date().getTime() - startTime > TIME_OUT) {
                            promise.reject(new TimeoutException());
                        } else {
                            try {
                                Thread.sleep(1000);
                            } catch (Exception e) {
                                // ignore.
                            }
                            sendEscCommand(command, address, promise);
                        }
                        if (currentReceiver != null) {
                            Log.i(TAG, "unregister 2");
                            getReactApplicationContext().unregisterReceiver(currentReceiver);
                            currentReceiver = null;
                        }
                    }
                    return true;
                }
            }));
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    public void sendTscCommand(final TscCommand command, final String address, final Promise promise) {
        final long startTime = new Date().getTime();
        try {
            this.openPort(address, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    if (msg != null && msg.getData().get(ACTION_CONNECT_STATUS) != null) {
                        Log.d(TAG, msg.getData().get(ACTION_CONNECT_STATUS).toString());
                        if (currentReceiver != null) {
                            Log.i(TAG, "unregister 0");
                            getReactApplicationContext().unregisterReceiver(currentReceiver);
                            currentReceiver = null;
                        }
                        promise.reject(new Exception(msg.getData().get(ACTION_CONNECT_STATUS).toString()));
                    } else if (msg != null && msg.what == 1) {
                        try {
                            Vector<Byte> datas = command.getCommand(); //发送数据
                            Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
                            byte[] bytes = ArrayUtils.toPrimitive(Bytes);
                            String str = Base64.encodeToString(bytes, Base64.DEFAULT);
                            int rel = mGpService.sendTscCommand(0, str);
                            Log.i(TAG, "tcs rel " + rel);
                            GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rel];
                            if (r != GpCom.ERROR_CODE.SUCCESS) {
                                if (new Date().getTime() - startTime > TIME_OUT) {
                                    promise.reject(new TimeoutException());
                                } else {
                                    sendTscCommand(command, address, promise);
                                }
                            } else {
                                try {
                                    Thread.sleep(1000);
                                } catch (Exception e) {
                                    //ignore.
                                }
                                promise.resolve(null);
                            }

                        } catch (Exception e) {
                            promise.reject(e);
                        } finally {
                            if (currentReceiver != null) {
                                Log.i(TAG, "unregister");
                                getReactApplicationContext().unregisterReceiver(currentReceiver);
                                currentReceiver = null;
                            }
                        }
                    } else if (msg != null && msg.what == 2) {
                        Log.i(TAG, "Recalling  " + msg.what);
                        if (new Date().getTime() - startTime > TIME_OUT) {
                            promise.reject(new TimeoutException());
                        } else {
                            try {
                                Thread.sleep(1000);
                            } catch (Exception e) {
                                // ignore.
                            }
                            sendTscCommand(command, address, promise);
                        }
                        if (currentReceiver != null) {
                            Log.i(TAG, "unregister 2");
                            getReactApplicationContext().unregisterReceiver(currentReceiver);
                            currentReceiver = null;
                        }
                    }
                    return true;
                }
            }));
        } catch (Exception e) {
            promise.reject(e);
        }
    }
}
