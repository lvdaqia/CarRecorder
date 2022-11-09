package com.example.dell.carrecorder;

import static com.azhon.jtt808.video.NV21EncoderH264.findNALU;
import static com.mapgoo.mapgooipc.MapgooIPC.MAPGOO_IPC_CONNECTED;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.AudioFormat;
import android.media.MediaCodec;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.PowerManager;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.location.AMapLocationClient;
import com.azhon.jtt808.JTT808Manager;
import com.azhon.jtt808.VideoList.AnalysisViideo;
import com.azhon.jtt808.VideoList.timeBean;
import com.azhon.jtt808.VideoList.vedioBean;
import com.azhon.jtt808.bean.JTT808Bean;
import com.azhon.jtt808.bean.JTT905Bean;
import com.azhon.jtt808.bean.TerminalParamsBean;
import com.azhon.jtt808.ftp.FTPFileUpload;
import com.azhon.jtt808.listener.OnConnectionListener;
import com.azhon.jtt808.netty.JTT808Client;
import com.azhon.jtt808.netty.JTT808ClientLocal;
import com.azhon.jtt808.netty.JTT905Client;
import com.azhon.jtt808.netty.live.LiveClient;
import com.azhon.jtt808.util.ByteUtil;
import com.azhon.jtt808.util.SharePreUtil;
import com.azhon.jtt808.video.NV21EncoderH264;
import com.example.administrator.mocam.R;
import com.example.dell.carrecorder.Location.Location808;
import com.example.dell.carrecorder.adapter.MainAdapter;
import com.example.dell.carrecorder.adapter.TitleAdapter;
import com.example.dell.carrecorder.bean.MainBean;
import com.example.dell.carrecorder.bean.SettingBean;
import com.example.dell.carrecorder.bean.TitleBean;
import com.example.dell.carrecorder.db.InstanceDBHelper;
import com.example.dell.carrecorder.util.Constants;
import com.example.dell.carrecorder.util.DateFormat;
import com.example.dell.carrecorder.util.mAudioPlayer;
import com.google.gson.Gson;
import com.mapgoo.mapgooipc.MapgooIPC;
import com.pgyersdk.crash.PgyCrashManager;
import com.pgyersdk.update.DownloadFileListener;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;
import com.pgyersdk.update.javabean.AppBean;
//import com.mediatek.carcorder.CarcorderManager;
//import com.mediatek.carcorder.IpodProxy;

/*import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;*/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import car.recorder.carrecorder;
import media.sdk.Common.AudioTrackManager.AudioTrackManager;
import media.sdk.Common.DB.CenterDB;
import media.sdk.Common.DB.RecordFileManager;
import media.sdk.Common.Playback.PlaybackFileFactory;
import media.sdk.Common.Record.RecordFileFactory;
import media.sdk.Common.UiUtil;
import media.sdk.MediaSdk;
import media.sdk.MediaSurfaceSdk.Display.ImageDrawer;


public class RecorActivity extends AppCompatActivity implements MediaSdk.LocalRecordObserver, MediaSdk.VideoEncoderObserver,
        MediaSdk.AudioEncodedObserver, MediaSdk.PreviewObserver, OnConnectionListener, MediaSdk.LocalPlaybackObserver,
        Location808.OnLocationListener, TitleAdapter.UpdateSetting, LiveClient.ReceiveLiveClientDataCallBack , View.OnTouchListener ,
        MapgooIPC.CmdDealCallBack,MapgooIPC.ConnStatusNotifCallBack,AdapterView.OnItemLongClickListener {
    private static String TAG = "RecorActivity";
    public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;
    private FrameLayout m_frameLayout = null;
    private RelativeLayout m_layoutVideo = null;
    private RelativeLayout m_layoutVideo1 = null;
    private RelativeLayout mainRelativeLayout = null;
    private ListView settingListView = null;
    private Button m_btnMenu = null;
    private boolean m_bStartLocalRecord = false;
    private Button m_btnLocalRecord = null;
    private EditText m_editModifyBitRate = null;
    private JTT808Manager manager;

    public static String ExternalPath = null;
    public static String IP;
    public static Integer PORT;

    public static String IP_LOCAL;
    public static Integer PORT_LOCAL;
    //终端手机号
    public static String PHONE;
    //制造商ID
    public static String MANUFACTURER_ID;
    //终端型号
    public static String TERMINAL_MODEL;
    //终端ID
    public static String TERMINAL_ID;
    private AMapLocationClient mlocationClient;
    private LiveClient liveClient, liveClient2, playBackClient,localLiveClient,localLiveClient2,playBackLocalClient,liveClient905, liveClient2905, playBackClient905;
    private LiveClient audioClient1,audioLocalClient1,audio905Client1;
    private boolean isStartVideoLive1 = false;
    private boolean isStartVideoLive2 = false;

    private boolean isStartLocalVideoLive1 = false;
    private boolean isStartLocalVideoLive2 = false;

    private boolean isStart905VideoLive1 = false;
    private boolean isStart905VideoLive2 = false;


    private boolean isStartVideoLiveFistTime1 = false;
    private boolean isStartVideoLiveFistTime2 = false;

    private boolean isStartLocalVideoLiveFistTime1 = false;
    private boolean isStartLocalVideoLiveFistTime2 = false;

    private boolean isStart905VideoLiveFistTime1 = false;
    private boolean isStart905VideoLiveFistTime2 = false;


    private boolean isStartVideoBackFistTime = false;
    private boolean isStartLocalVideoBackFistTime = false;

    private boolean isStartPlayBackVideoTimeFist = false;
    private boolean isStartLocalPlayBackVideoTimeFist = false;
    private boolean isStartAudioLive1 = false;
    private boolean isStartLocalAudioLive1 = false;
    private boolean isStar905AudioLive1 = false;
    private int mChannelNum;
    private MediaCodec mediaCodec;
    private boolean sing;
    private boolean ppsSenOver = false;
    private int playBackChannelNum;
    private boolean sdcard1Exist, sdcard2Exist;
    private boolean isPlayBackON;
    private MainAdapter mainAdapter;
    private TitleAdapter titleAdapter;
    private List<MainBean> mainBeans;
    private List<TitleBean> titleBeans;
    private GridView gridView;
    private boolean isStartRecord;
    private String recordFileName0;
    private String recordFileName1;
    private byte[] sps;
    private byte[] pps;
    private byte[] mdata;
    private int alarm;
    private int acc;
    private Double mlatitude=Double.MIN_VALUE,mlongitude=Double.MIN_VALUE;
    private float mSpeed=0f;
    private InstanceDBHelper mInstanceDBHelper;
    private String startRecordSysTime0;
    private String stopRecordSysTime0;
    private String startRecordSysTime1;
    private String stopRecordSysTime1;
    private long vedioDurtionTime0=0;
    private long vedioDurtionTime1=0;
    private SimpleDateFormat simpleDateFormat;
    // 音频获取源
 //   public static int audioSource = MediaRecorder.AudioSource.MIC;
    // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    public static int sampleRateInHz = 44100;
    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    public static int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    public static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区字节大小JTT808Handler
    public static int bufferSizeInBytes = 0;
    Timer m_timer = null;
    TimerTask m_task = null;
    private static final int MSG_SEND_SPS_CAN1 = 9;
    private static final int MSG_SEND_SPS_CAN2 = 10;
    private static final int MSG_SEND_PLAYBACK_SPS = 11;
    private static final int MSG_START_RECORD = 12;
    private static final int MSG_START_RECORD_START_TIME = 5000;
    private static final int MSG_STOP_RECORD = 13;
    private static final int MSG_DELAY_START_PLAYBACK = 14;
    private static final int MSG_DELAY_START_DURATION_PLAYBACK = 17;
    private static final int MSG_DELAY_UPDATE_SETTING = 15;
    private static final int MSG_INIT_CAMERA = 16;
    private static final int MSG_CAMERA_LIGHT_STATE =18;
    private static final int MSG_POWER_OFF = 19;
    private static final int MSG_START_SPEAK =20;
    private static final int MSG_MONITER=22;
    private static final int MSG_TCP_NO_ALIVE =21;
    private static final int MSG_LCD_CLOSE =23;
    private static final int IPC_START =24;
    private static final int MSG_LOCAL_TCP_NO_ALIVE =31;
    private static final int MSG_905_TCP_NO_ALIVE =32;
    private static final int MSG_SHUTDOWN =33;
    private static final int MSG_AUTO_OTA = 34;
    private boolean isStartDurationPlayBack;
    private int sendTime;
    private boolean isduration=false;
    private netWorkListing netWork;
    private mAudioPlayer audioPlayer;
    private PipedOutputStream pout;
    private int count = 0;
    private SettingBean settingBean = SettingBean.getInstance();
    byte[][] sps1, sps2;

    private String playBackFileName;
    private String playBackStartTime;
    private String playBackEndTime;
    private int playBackDurationTime;
    public static boolean isUSBCameraOpen =false;
    public static boolean isCamera0Open =true;
    public static boolean isCamera1Open =true;

  //  private CarcorderManager mCarcorderManager;
  //  private IpodProxy mIpodProxy;
    private ShutdownReceiver mShutdownReceiver;
    private PowerManager powerManager;
    private boolean IPC_CONNECTED =false;
    private int IPC_INDEX =-1;
    private int time_count=0;
    //2022-06-25 default timeout screen off start
    final static int DEFAULT_TIME_SCREEN_OFF = 30000;
    Location808 location808;
    //end
    Handler m_msgHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
//                case MSG_SEND_SPS_CAN1:
//                    Log.d("m_msgHandler", "MSG_SEND_SPS1");
//                    for (byte[] aByte : sps1) {
//                        manager.videoLive(aByte, 1, liveClient, aByte.length, 2,);
//                        Log.d("m_msgHandler", "MSG_SEND_SPS_CAN1" + aByte.length+" data:"+ByteUtil.bytesToHex(aByte));
//                    }
//                    isStartVideoLiveFistTime1 = false;
//                    break;
//                case MSG_SEND_SPS_CAN2:
//                    Log.d("m_msgHandler", "MSG_SEND_SPS2");
//                    for (byte[] aByte : sps2) {
//                        manager.videoLive(aByte, 2, liveClient2, aByte.length, 3);
//                        Log.d("m_msgHandler", "MSG_SEND_SPS_CAN2 "+aByte.length+" data:"+ByteUtil.bytesToHex(aByte));
//                    }
//                    break;
//                case MSG_SEND_PLAYBACK_SPS:
//                    sendTime++;
//                    if (sendTime < 5) {
//                        if (mdata != null) {
//                            manager.videoLive(mdata, playBackChannelNum, playBackClient, mdata.length, 4);
//                            m_msgHandler.sendEmptyMessageDelayed(MSG_SEND_PLAYBACK_SPS, 300);
//                        } else {
//                            isStartPlayBackVideoTimeFist = false;
//                        }
//                    } else {
//                        sendTime = 0;
//                        m_msgHandler.removeMessages(MSG_SEND_PLAYBACK_SPS);
//                        ppsSenOver = true;
//                    }
//
//                    break;
                case MSG_START_RECORD:
                    startRecord();
                    isStartDurationPlayBack=false;
                    break;
                case MSG_STOP_RECORD:
                    stopRecord();
                    isStartDurationPlayBack=false;
                    break;
                case MSG_DELAY_START_PLAYBACK:
                    MediaSdk.Instant().VideoPlayback_Start(0, playBackFileName);
                    isStartDurationPlayBack=false;
                    break;
                case MSG_DELAY_START_DURATION_PLAYBACK:
                    MediaSdk.Instant().VideoPlayback_Start(0, playBackFileName);
                    isStartDurationPlayBack = true;
                    break;
                case MSG_DELAY_UPDATE_SETTING:
                    Log.e(TAG,"terminal setting is changed,reconnet!!!");
                    if (titleBeans != null) titleBeans.clear();
                    initSettingData();
                    if (isPlayBackON) stopPlayBack();
                    initJTT808();
                    initLocalJTT808();
                    init905JTT808();
                    startLocation();
                    break;
                case MSG_INIT_CAMERA:
                    initCamera();
                    break;
                case MSG_POWER_OFF:
                    Log.d(TAG,"guanji"+"  "+time_count);
                    closeLCD(0);
                    stopRecord();
//                    time_count++;
//                    if(time_count==10){
//                        time_count=0;
//                        m_msgHandler.removeMessages(MSG_POWER_OFF);
//                        if(readLACC()==0){
//                            Log.d(TAG,"guanji");
//                            Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
//                            intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent);
//                        }
//                    }else {
//                        m_msgHandler.sendEmptyMessageDelayed(MSG_POWER_OFF,500);
//                    }

                    break;
                case MSG_SHUTDOWN:
                    shutdown();
                    break;
                case MSG_START_SPEAK:
                    File file = new File("/data/data/com.example.administrator.carrecorder/files/"+fileName);
                    if(file.exists()) {
                        Log.d(TAG,"audiofile  true");
                      //  AudioTrackManager.getInstance().startPlay("/data/data/com.example.administrator.carrecorder/files/" + fileName);
                    }else {
                        Log.d(TAG,"audiofile  false");
                    }
                    break;
                case MSG_LCD_CLOSE:
                    //2022-06-24 if close screen ,go to idle screen start
                    if (settingListView != null && settingListView.isShown()) {
                        mainRelativeLayout.setVisibility(View.VISIBLE);
                        settingListView.setVisibility(View.GONE);
                        gridView.setVisibility(View.VISIBLE);
                    }else{
                        mainRelativeLayout.setVisibility(View.VISIBLE);
                    }
                    /*if(!isAppForgroud(getApplicationContext())){
                        //not launcher
                        Log.d(TAG,"if now filemanager, go to home");
                        //mThread.start();
                        //new Thread(GoIdleRunnable).start();
                        *//*final Handler goHome = new Handler();
                        goHome.postDelayed(
                                GoIdleRunnable, 1); // 1 ms delay*//*
                        //goHome.sendEmptyMessageDelayed(1,20);
                    }*/
                    //end
                    closeLCD(0);
                    break;
                case MSG_MONITER:break;
                case MSG_TCP_NO_ALIVE:
                    initJTT808();
                    startLocation();
                    m_msgHandler.sendEmptyMessageDelayed(MSG_TCP_NO_ALIVE,1000*20);
                    break;
                case MSG_LOCAL_TCP_NO_ALIVE:
                    initLocalJTT808();
                    startLocation();
                    m_msgHandler.sendEmptyMessageDelayed(MSG_LOCAL_TCP_NO_ALIVE,1000*20);
                    break;
                case MSG_905_TCP_NO_ALIVE:
                    init905JTT808();
                    startLocation();
                    m_msgHandler.sendEmptyMessageDelayed(MSG_905_TCP_NO_ALIVE,1000*20);
                    break;
                case IPC_START:
                    MapgooIPC.setConnStatusNotifCallBack(RecorActivity.this);
                    MapgooIPC.setCmdDealCallBack(RecorActivity.this);
                    new Thread(new Runnable() {
                        public synchronized void run() {
                            //MapgooIPC.getDataIndexByUniqueID("com.mapgoo.mapgooipc_one");
                            MapgooIPC.sendMsg(MapgooIPC.MAPGOO_IPC_MSG_CAMERA_STATUS,1,0,0,"");
                        }

                    }).start();

                    break;
                case MSG_AUTO_OTA:
                    handleOTA(true);
                    break;

                case 1:
                    // HandleStatus();
                    break;
                case 0:
                case -2:
                case -3:
                case -4:
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void shutdown(){
        try {
            Log.d(TAG,"go to shutdown...");
            Intent intent = new Intent();
            intent.setAction("ACC_Shutdown");
            sendBroadcast(intent);
            /*Process proc = Runtime.getRuntime().exec(new String[]{"sh","-c","reboot","-p"});
            proc.waitFor();*/
            /*PowerManager pManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pManager != null) {
                Method method = pManager.getClass().getMethod("shutdown", boolean.class, String.class, boolean.class);
                method.invoke(pManager, false, null, false);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

  /*  Runnable GoIdleRunnable = new Runnable() {
        @Override
        public void run() {
            Instrumentation instrumentation = new Instrumentation();
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_HOME);
        }
    };*/

    private Handler goHome = new Handler() {
        @Override
        public void handleMessage(Message msg) {
                new Thread() {
                    public void run(){
                        Instrumentation instrumentation = new Instrumentation();
                        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_HOME);
                    }
                }.start();
            }
    };

    /*Thread mThread = new Thread(){
        @Override
        public void run() {
            Instrumentation instrumentation = new Instrumentation();
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_HOME);
        }
    };*/

    public static boolean isAppForgroud(Context context) {
        if (context != null) {
            String packName = context.getPackageName();
            ActivityManager am = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> rTasks = am.getRunningTasks(1);
            ActivityManager.RunningTaskInfo task = rTasks.get(0);
            return packName.equalsIgnoreCase(task.topActivity.getPackageName());
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        findViewID();
        initData();
        initSettingData();
        registeBro();
        initCamera();
     //   m_msgHandler.sendEmptyMessageDelayed(MSG_INIT_CAMERA,2000);
        closeLCD(1);
        Log.d(TAG, "SDSize:" + getSDAvailableSize());
        mInstanceDBHelper = InstanceDBHelper.getInstance(this);

      //  mCarcorderManager = CarcorderManager.get();
        // set ipod
     //   mIpodProxy = mCarcorderManager.getIpodProxy();
      //  mIpodProxy.setRebootControl(0);
      //  mIpodProxy.doShutdown("doshutdown", true);

        mShutdownReceiver = new ShutdownReceiver();
        IntentFilter shutdownFilter = new IntentFilter();
        shutdownFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        shutdownFilter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
        shutdownFilter.addAction("android.intent.action.MEDIA_MOUNTED");
        shutdownFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        shutdownFilter.addDataScheme("file");
        registerReceiver(mShutdownReceiver, shutdownFilter);

        MapgooIPC.setCmdDealCallBack(this);
        MapgooIPC.setConnStatusNotifCallBack(this);
        String version = MapgooIPC.getVersion();
        Log.d("MAPGOO_IPC","get ipc_client version:"+version);
        //   m_msgHandler.sendEmptyMessageDelayed(IPC_START,5000);


        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.INSTALL_NON_MARKET_APPS, 1);
       m_msgHandler.sendEmptyMessageDelayed(MSG_AUTO_OTA,1000*60*3);
       // handleOTA(true);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //Log.d(TAG, event.getAction()+"");
        if(event.getAction()==KeyEvent.ACTION_UP){
            Log.d(TAG, "----------  set screen off time -------- ");
            m_msgHandler.removeMessages(MSG_LCD_CLOSE);
            m_msgHandler.sendEmptyMessageDelayed(MSG_LCD_CLOSE, DEFAULT_TIME_SCREEN_OFF);
            if(readLCD()==0) {
                closeLCD(1);
                //m_msgHandler.removeMessages(MSG_LCD_CLOSE);
                //m_msgHandler.sendEmptyMessageDelayed(MSG_LCD_CLOSE, DEFAULT_TIME_SCREEN_OFF);
                return true;
            }/*else {
                m_msgHandler.removeMessages(MSG_LCD_CLOSE);
                m_msgHandler.sendEmptyMessageDelayed(MSG_LCD_CLOSE, DEFAULT_TIME_SCREEN_OFF);
            }*/
        }
        return false;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position){
            case 5:
                Intent i = new Intent();
                i.setComponent(new ComponentName("com.mediatek.factorymode",
                        "com.mediatek.factorymode.FactoryMode"));
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(i);
                break;
        }
        return false;
    }


    public class ShutdownReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    "android.intent.action.ACTION_SHUTDOWN")
                    || intent.getAction().equals(
                    "android.intent.action.ACTION_SHUTDOWN_IPO")) {
                Log.i(TAG, TAG + ", onReceive "+intent.getAction());
            }
            if(intent.getAction().equalsIgnoreCase(Intent.ACTION_MEDIA_MOUNTED))
            {
                Log.d("action",intent.getData().getPath());
                ExternalPath = intent.getData().getPath();
            }
            if(intent.getAction().equalsIgnoreCase(Intent.ACTION_MEDIA_EJECT))
            {
                ExternalPath = null;
                Log.d("action",intent.getData().getPath());
            }
        }
    }
    private void initCamera() {
        if(GetSharedPreferences("isUSBCameraOpen","0").equalsIgnoreCase("1")){
            isUSBCameraOpen=true;
        }else {
            isUSBCameraOpen=false;
        }
        if(GetSharedPreferences("isCamera0Open","1").equalsIgnoreCase("1")){
            isCamera0Open=true;
        }else {
            isCamera0Open=false;
        }
        if(GetSharedPreferences("isCamera1Open","1").equalsIgnoreCase("1")){
            isCamera1Open=true;
        }else {
            isCamera1Open=false;
        }
        String strText;
        strText = GetSharedPreferences("tree_uri", "");
        RecordFileFactory.m_strUri = strText;
        PlaybackFileFactory.m_strUri = strText;
        RecordFileFactory.m_strPath = strText;
        PlaybackFileFactory.m_strPath = strText;

        strText = GetSharedPreferences("audio_capture_flag", "3");
        carrecorder.m_nAudioCaptureFlag = Integer.decode(strText);
        strText = GetSharedPreferences("file_max_time", "30");
        RecordFileFactory.m_nFileTime = Integer.decode(strText);
        strText = GetSharedPreferences("file_max_size", "1024");
        RecordFileFactory.m_nFileSize = Integer.decode(strText);
        strText = GetSharedPreferences("enable_pre_record", "0");
        RecordFileFactory.m_bEnablePreRecord = Integer.decode(strText);
        strText = GetSharedPreferences("enable_post_record", "0");
        RecordFileFactory.m_bEnablePostRecord = Integer.decode(strText);
        strText = GetSharedPreferences("pre_record_time", "10");
        RecordFileFactory.m_nPreRecordTime = Integer.decode(strText);
        strText = GetSharedPreferences("post_record_time", "10");
        RecordFileFactory.m_nPostRecordTime = Integer.decode(strText);

        strText = GetSharedPreferences("location", "0");
        carrecorder.m_nLocationType = Integer.decode(strText);

        strText = GetSharedPreferences("osd_time_show", "1");
        carrecorder.m_bOsdTimeShow = Integer.decode(strText);
        strText = GetSharedPreferences("osd_location_show", "1");
        carrecorder.m_bOsdLocationShow = Integer.decode(strText);
        strText = GetSharedPreferences("osd_device_id_show", "0");
        carrecorder.m_bOsdDeviceIDShow = Integer.decode(strText);
        strText = GetSharedPreferences("osd_resolution_show", "1");
        carrecorder.m_bOsdResolutionShow = Integer.decode(strText);
        strText = GetSharedPreferences("osd_x", "1");
        carrecorder.m_nOsdXPos = Integer.decode(strText);
        strText = GetSharedPreferences("osd_y", "99");
        carrecorder.m_nOsdYPos = Integer.decode(strText);

        strText = GetSharedPreferences("crop_x_offset", "0");
        carrecorder.m_nCropXOffset = Integer.decode(strText);
        strText = GetSharedPreferences("crop_y_offset", "0");
        carrecorder.m_nCropYOffset = Integer.decode(strText);

        strText = GetSharedPreferences("frame_rate_0", "20");
        carrecorder.m_nFrameRate[0] = Integer.decode(strText);
        strText = GetSharedPreferences("frame_rate_1", "20");
        carrecorder.m_nFrameRate[1] = Integer.decode(strText);

        strText = GetSharedPreferences("bit_rate_0", "1200");
        carrecorder.m_nBitRate[0] = Integer.decode(strText);
        strText = GetSharedPreferences("bit_rate_1", "1200");
        carrecorder.m_nBitRate[1] = Integer.decode(strText);

        strText = GetSharedPreferences("width_0", Integer.toString(carrecorder.m_width));
        carrecorder.m_nWidth[0] = Integer.decode(strText);
        strText = GetSharedPreferences("height_0", Integer.toString(carrecorder.m_height));
        carrecorder.m_nHeight[0] = Integer.decode(strText);

        strText = GetSharedPreferences("width_1", Integer.toString(carrecorder.m_width));
        carrecorder.m_nWidth[1] = Integer.decode(strText);
        strText = GetSharedPreferences("height_1", Integer.toString(carrecorder.m_height));
        carrecorder.m_nHeight[1] = Integer.decode(strText);

        strText = GetSharedPreferences("video_encode_type_0", "0");
        carrecorder.m_nVideoEncodeType[0] = Integer.decode(strText);
        strText = GetSharedPreferences("video_encode_type_1", "0");
        carrecorder.m_nVideoEncodeType[1] = Integer.decode(strText);

        carrecorder.m_width = carrecorder.m_nWidth[0];
        carrecorder.m_height = carrecorder.m_nHeight[0];
        if (carrecorder.m_nWidth[1] > carrecorder.m_nWidth[0]) {
            carrecorder.m_width = carrecorder.m_nWidth[1];
            carrecorder.m_height = carrecorder.m_nHeight[1];
        }


        carrecorder.m_fft = getFromAssets("GB2312_48.ttf");
        carrecorder.CarRecordInitial();
        MediaSdk.Instant().Initial();
        CenterDB.Initial(this);
        RecordFileManager.Initial();
        RecordFileFactory.Initial(this, 8);
        PlaybackFileFactory.Initial(this, 8);
        MediaSdk.Instant().VideoRecord_Create(this, this);
        MediaSdk.Instant().VideoPlayback_Create(this, this);
        MediaSdk.Instant().VideoEncoder_Start(this);
        try {
            MediaSdk.Instant().Camera_Start(this, m_msgHandler);
        }catch (Exception e){
            Log.d(TAG, "camear error . send media error");
            e.printStackTrace();
            OnStartRecordFailed(-2);
        }
        MediaSdk.Instant().AudioInput_Start(this);

        m_layoutVideo = new RelativeLayout(this);
        m_layoutVideo1 = new RelativeLayout(this);
        m_frameLayout.addView(m_layoutVideo1);
        m_frameLayout.addView(m_layoutVideo);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) m_layoutVideo1.getLayoutParams();
        int nScreenWidth = UiUtil.getScreenHeight(this);
        int nScreenHeight = UiUtil.getScreenWidth(this);
        params.width = nScreenHeight * 2 / 5;
        params.height = nScreenWidth * 2 / 5;
        m_layoutVideo1.setLayoutParams(params);

        MediaSdk.Instant().Preview_Create(this, this);
        View imageDrawer = MediaSdk.Instant().Preview_GetView(0);
        if (imageDrawer != null) {
            m_layoutVideo.addView(imageDrawer);
        }


        imageDrawer = MediaSdk.Instant().Preview_GetView(1);
        if (imageDrawer != null) {
            m_layoutVideo1.addView(imageDrawer);
        }

        if (m_timer == null) {
            m_timer = new Timer();
            m_task = new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = 1;
                    m_msgHandler.sendMessage(message);
                }
            };
            m_timer.schedule(m_task, 1000, 1000);
        }

        MediaSdk.Instant().Preview_Start();
        try {
            mediaCodec = MediaCodec.createEncoderByType("video/avc");
        } catch (IOException e) {
            e.printStackTrace();
        }
        m_msgHandler.sendEmptyMessageDelayed(MSG_START_RECORD, MSG_START_RECORD_START_TIME);
    }

    private boolean isSurfaceCreated(int camera_id){
        ImageDrawer imageDrawer = (ImageDrawer)MediaSdk.Instant().Preview_GetView(camera_id);
        if(imageDrawer != null){
            Log.d(TAG, "camera " + camera_id + " height is " + imageDrawer.getHeight());

            if(imageDrawer.m_previewSurface == null){
                Log.d(TAG, "camera " + camera_id + " Surface doesn't Created");
                return false;
            }
        }
        return true;
    }

    private boolean isAllCameraConnected(){
        // 两个都ok
        /*if(isSurfaceCreated(0) && isSurfaceCreated(1)){
            return true;
        }
        //camera 0 ok,camera 1 fail
        if(isSurfaceCreated(0) && !isSurfaceCreated(1)){
            return false;
        }
        //camera 1 ok,camera 0 fail
        if(!isSurfaceCreated(0) && isSurfaceCreated(1)){
            return false;
        }*/

        if(Camera_0_working && Camera_1_working){
            Camera_0_working = false;
            Camera_1_working = false;
            Log.e(TAG,"two camer is open");
            return true;
        }
        //camera 0 ok,camera 1 fail
        if(Camera_0_working && !Camera_1_working){
            Camera_0_working = false;
            Camera_1_working = false;
            Log.e(TAG," camer 0 is open");
            return false;
        }
        //camera 1 ok,camera 0 fail
        if(!Camera_0_working && Camera_1_working){
            Camera_0_working = false;
            Camera_1_working = false;
            Log.e(TAG," camer 1 is open");
            return false;
        }
        if(!Camera_0_working && !Camera_1_working){
            Camera_0_working = false;
            Camera_1_working = false;
            Log.e(TAG," 2 camer 0 is fail");
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.example.administrator.carrecorder","com.example.dell.carrecorder.RecorActivity"));
            startActivity(intent);
            return true;
        }
        return true;
        /*boolean is_camera0_open = true;
        boolean is_camera1_open = true;
        if(m_layoutVideo.getVisibility() != View.VISIBLE || m_layoutVideo.getWidth() < 10 || m_layoutVideo.getHeight() < 10){
            is_camera0_open = false;
        }

        if(m_layoutVideo1.getVisibility() != View.VISIBLE || m_layoutVideo1.getWidth() < 10 || m_layoutVideo1.getHeight() < 10){
            is_camera1_open = false;
        }
        Log.e(TAG," m_layoutVideo  " + m_layoutVideo.getWidth() + " x " + m_layoutVideo.getHeight());
        Log.e(TAG," m_layoutVideo1  " + m_layoutVideo1.getWidth() + " x " + m_layoutVideo1.getHeight());
        Log.e(TAG," is_camera0_open " + is_camera0_open + " is_camera1_open " + is_camera1_open);

        isSurfaceCreated(0);
        isSurfaceCreated(1);
        if(is_camera0_open && is_camera0_open){
            return true;
        }
        if(is_camera0_open && !is_camera0_open){
            return false;
        }
        if(!is_camera0_open && is_camera0_open){
            return false;
        }
        return true;*/
    }

    private void findViewID() {
        gridView = (GridView) this.findViewById(R.id.main_gv);
        gridView.setOnItemClickListener(mainItemClickListener);
        gridView.setOnTouchListener(this);
        gridView.setOnItemLongClickListener(this);
        settingListView = ((ListView) findViewById(R.id.setting_list));
        settingListView.setOnItemClickListener(settingItemClickListener);
        Switch switch_ = new Switch(getApplicationContext());
        switch_.setChecked(SwitchState());
        switch_.setShowText(false);
        switch_.setText("    熄火关机");
        switch_.setTextSize(18);
        switch_.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e(TAG,"isChecked:" + isChecked);
                SetSharedPreferences("isNeedShutdown",isChecked?"1":"0");
                Settings.System.putInt(getApplicationContext().getContentResolver(),"isNeedShutdown",isChecked?1:0);
            }
        });
        settingListView.addFooterView(switch_);
        m_frameLayout = (FrameLayout) findViewById(R.id.layOut);
        mainRelativeLayout = (RelativeLayout) findViewById(R.id.function_list);
        mainRelativeLayout.setVisibility(View.VISIBLE);
    }

    private boolean SwitchState(){
        return GetSharedPreferences("isNeedShutdown", "1").equals("1");
    }

    private void initData() {
        if (mainBeans == null) mainBeans = new ArrayList<MainBean>();
        MainBean bean1 = new MainBean();
        bean1.setName("摄像头");
        bean1.setColor("#01B2FE");
        mainBeans.add(bean1);

        MainBean bean2 = new MainBean();
        bean2.setName("系统设置");
        bean2.setColor("#FF008E");
        mainBeans.add(bean2);


        MainBean bean3 = new MainBean();
        bean3.setName("录像文件");
        bean3.setColor("#FFBB13");
        mainBeans.add(bean3);


        MainBean bean4 = new MainBean();
        bean4.setName("车辆设置");
        bean4.setColor("#01C557");
        mainBeans.add(bean4);

        MainBean bean5 = new MainBean();
        bean5.setName("摄像头设置");
        bean5.setColor("#19C557");
        mainBeans.add(bean5);

        MainBean bean6 = new MainBean();
        bean6.setName("预留");
        bean6.setColor("#112347");
        mainBeans.add(bean6);

        MainBean bean7 = new MainBean();
        bean7.setName("软件更新\r\n" + "版本号："+getCurrentVersionCode());
        bean7.setColor("#01B2FE");
        mainBeans.add(bean7);

        MainBean bean8 = new MainBean();
        bean8.setName("...");
        bean8.setColor("#19C557");
        mainBeans.add(bean8);
        if (mainAdapter == null) mainAdapter = new MainAdapter(RecorActivity.this, mainBeans);
        gridView.setAdapter(mainAdapter);


    }
    private boolean loadSettingsFromFile()
    {
        if(!sdcard1Exist&&!sdcard2Exist)
            return false;
        String json;
        Gson gson = new Gson();
        SettingBean temp;
        BufferedReader bufferedReader;
        File file;
        try {
            if(sdcard1Exist) {
                file = new File("/storage/sdcard1/Setting.json");
                if(!file.exists())
                    return false;
                bufferedReader = new BufferedReader(new FileReader("/storage/sdcard1/Setting.json"));
            }else {
                file = new File("/storage/sdcard2/Setting.json");
                if(!file.exists())
                    return false;
                bufferedReader = new BufferedReader(new FileReader("/storage/sdcard2/Setting.json"));
            }
            try {
                json = bufferedReader.readLine();
                Log.d("load json",json);
                temp = gson.fromJson(json,SettingBean.class);
                Log.d("json:","IP:"+temp.getIP()+"\n"+
                        "PORT："+temp.getPORT()+"  platform:"+temp.getPlatform()+"  audioType:"+temp.getaudioType());
                SharePreUtil.putString(this, "PHONE", temp.getPHONE());
                SharePreUtil.putInt(this, "PORT", temp.getPORT());
                SharePreUtil.putString(this, "IP", temp.getIP());
                SharePreUtil.putInt(this, "PORT_LOCAL", temp.getPORT_LOCAL());
                SharePreUtil.putString(this, "IP_LOCAL", temp.getIP_LOCAL());
                SharePreUtil.putString(this, "TERMINAL_ID", temp.getTERMINAL_ID());
                SharePreUtil.putInt(this, "audioType", temp.getaudioType());
                settingBean.copyFrom(temp);
                bufferedReader.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
    private void saveSettingsToFile()
    {
        Log.d("save json", "...............");
//        if(sdcard1Exist||sdcard2Exist) {
//            Gson gson = new Gson();
//            String json = gson.toJson(SettingBean.getInstance(), SettingBean.class);
//            Log.d("save json", json);
//            try {
//                BufferedWriter bufferedWriter;
//                if(sdcard1Exist)
//                    bufferedWriter = new BufferedWriter(new FileWriter("/storage/sdcard1/setting.json"));
//                else
//                    bufferedWriter = new BufferedWriter(new FileWriter("/storage/sdcard2/setting.json"));
//                bufferedWriter.write(json);
//                bufferedWriter.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }
    private void initSettingData() {

        isSDCard1Exit();
        if (titleBeans == null) titleBeans = new ArrayList<TitleBean>();
        if(loadSettingsFromFile())
        {
            TitleBean IP = new TitleBean();
            IP.setName(getString(R.string.IP));
            IP.setValue(settingBean.getIP());
            titleBeans.add(IP);

            TitleBean PORT = new TitleBean();
            PORT.setName(getString(R.string.PORT));
            PORT.setValue(Integer.toString(settingBean.getPORT()));
            titleBeans.add(PORT);

            TitleBean IP_LOCAL = new TitleBean();
            IP_LOCAL.setName(getString(R.string.IP_LOCAL));
            IP_LOCAL.setValue(settingBean.getIP_LOCAL());
            titleBeans.add(IP_LOCAL);

            TitleBean PORT_LOCAL = new TitleBean();
            PORT_LOCAL.setName(getString(R.string.PORT_LOCAL));
            PORT_LOCAL.setValue(Integer.toString(settingBean.getPORT_LOCAL()));
            titleBeans.add(PORT_LOCAL);

            TitleBean TERMINAL_ID = new TitleBean();
            TERMINAL_ID.setName(getString(R.string.TERMINAL_ID));
            TERMINAL_ID.setValue(settingBean.getTERMINAL_ID());
            titleBeans.add(TERMINAL_ID);

            TitleBean PHONE = new TitleBean();
            PHONE.setName(getString(R.string.PHONE));
            PHONE.setValue(settingBean.getPHONE());
            titleBeans.add(PHONE);

            TitleBean MANUFACTURER_ID = new TitleBean();
            MANUFACTURER_ID.setName(getString(R.string.MANUFACTURER_ID));
            MANUFACTURER_ID.setValue(settingBean.getMANUFACTURER_ID());
            titleBeans.add(MANUFACTURER_ID);


            TitleBean TERMINAL_MODEL = new TitleBean();
            TERMINAL_MODEL.setName(getString(R.string.TERMINAL_MODEL));
            TERMINAL_MODEL.setValue(settingBean.getTERMINAL_MODEL());
            titleBeans.add(TERMINAL_MODEL);

            Log.e("titleBeans", titleBeans.size() + "");
            if (titleAdapter == null)
                titleAdapter = new TitleAdapter(RecorActivity.this, titleBeans, this);

            settingListView.setAdapter(titleAdapter);
        } else {
            TitleBean IP = new TitleBean();
            IP.setName(getString(R.string.IP));
            IP.setValue(SharePreUtil.getString(this, "IP", Constants.IP));
            titleBeans.add(IP);
            settingBean.setIP(SharePreUtil.getString(this, "IP", Constants.IP));

            TitleBean PORT = new TitleBean();
            PORT.setName(getString(R.string.PORT));
            PORT.setValue(SharePreUtil.getInt(this, "PORT", Constants.PORT) + "");
            titleBeans.add(PORT);
            settingBean.setPORT(SharePreUtil.getInt(this, "PORT", Constants.PORT));

            TitleBean IP_LOCAL = new TitleBean();
            IP_LOCAL.setName(getString(R.string.IP_LOCAL));
            IP_LOCAL.setValue(SharePreUtil.getString(this, "IP_LOCAL", Constants.Local_ip));
            titleBeans.add(IP_LOCAL);
            settingBean.setIP_LOCAL(SharePreUtil.getString(this, "IP_LOCAL", Constants.IP));

            TitleBean PORT_LOCAL = new TitleBean();
            PORT_LOCAL.setName(getString(R.string.PORT_LOCAL));
            PORT_LOCAL.setValue(SharePreUtil.getInt(this, "PORT_LOCAL", Constants.Local_PORT) + "");
            titleBeans.add(PORT_LOCAL);
            settingBean.setPORT_LOCAL(SharePreUtil.getInt(this, "PORT_LOCAL", Constants.Local_PORT));

            TitleBean TERMINAL_ID = new TitleBean();
            TERMINAL_ID.setName(getString(R.string.TERMINAL_ID));
            TERMINAL_ID.setValue(SharePreUtil.getString(this, "TERMINAL_ID", Constants.TERMINAL_ID));
            titleBeans.add(TERMINAL_ID);
            settingBean.setTERMINAL_ID(SharePreUtil.getString(this, "TERMINAL_ID", Constants.TERMINAL_ID));

            TitleBean PHONE = new TitleBean();
            PHONE.setName(getString(R.string.PHONE));
            PHONE.setValue(SharePreUtil.getString(this, "PHONE", Constants.PHONE));
            titleBeans.add(PHONE);
            settingBean.setPHONE(SharePreUtil.getString(this, "PHONE", Constants.PHONE));
            //settingBean.setPHONE(SharePreUtil.getString(this, "PHONE", Constants.PHONE));

            TitleBean MANUFACTURER_ID = new TitleBean();
            MANUFACTURER_ID.setName(getString(R.string.MANUFACTURER_ID));
            MANUFACTURER_ID.setValue(SharePreUtil.getString(this, "MANUFACTURER_ID", Constants.MANUFACTURER_ID));
            titleBeans.add(MANUFACTURER_ID);
            settingBean.setMANUFACTURER_ID(SharePreUtil.getString(this, "MANUFACTURER_ID", Constants.MANUFACTURER_ID));

            TitleBean TERMINAL_MODEL = new TitleBean();
            TERMINAL_MODEL.setName(getString(R.string.TERMINAL_MODEL));
            TERMINAL_MODEL.setValue(SharePreUtil.getString(this, "TERMINAL_MODEL", Constants.TERMINAL_MODEL));
            titleBeans.add(TERMINAL_MODEL);
            settingBean.setTERMINAL_MODEL(SharePreUtil.getString(this, "TERMINAL_MODEL", Constants.TERMINAL_MODEL));

            Log.e("titleBeans", titleBeans.size() + "");
            if (titleAdapter == null)
                titleAdapter = new TitleAdapter(RecorActivity.this, titleBeans, this);

            settingListView.setAdapter(titleAdapter);
            //getPhoneNumber();
            //settingBean.setPHONE(SharePreUtil.getString(this, "PHONE", Constants.PHONE));

            settingBean.setaudioType(carrecorder.AUDIOTYPE_G711);

            saveSettingsToFile();

        }

    }

    private void getPhoneNumber() {
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String tel = tm.getLine1Number();

        if (tel == null || tel.equalsIgnoreCase("")) {
            Log.d(TAG, "tel==null");
            if (titleBeans != null) {
                //titleBeans.get(3).setValue(getString(R.string.NO_SIM));
                SharePreUtil.putString(this, "PHONE", titleBeans.get(5).getValue());
                titleAdapter.notifyDataSetChanged();
            }
        } else {
            Log.d(TAG, "tel!=null");
            SharePreUtil.putString(this, "PHONE", "0" + tel);
            if (titleBeans != null) {
                titleAdapter.notifyDataSetChanged();
                titleBeans.get(5).setValue("0" + tel);
            }
        }

    }



    private void registeBro() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction("SDCARD_REMOVE");
        intentFilter.addAction("SDCARD_READY");
        intentFilter.addAction("ACC_STATUS_CHANGE");

        netWork = new netWorkListing();
        registerReceiver(netWork, intentFilter);

    }


    public int OnStartRecordSuccess(String fileName) {
        Log.d(TAG, fileName);
        if (fileName.contains("_0_")) {
            Log.d(TAG, "前摄像头:" + fileName);
            recordFileName0=fileName;
            if(simpleDateFormat==null)simpleDateFormat = new SimpleDateFormat("yyMMddHHmmss");// HH:mm:ss
            //获取当前时间
            Date date = new Date(System.currentTimeMillis());
            startRecordSysTime0 = simpleDateFormat.format(date);
            insertDB(fileName+".mp4",RecordFileFactory.strDirector+"front/"+fileName,vedioDurtionTime0);
        } else if (fileName.contains("_1_")) {
            Log.d(TAG, "后摄像头:" + fileName);
            recordFileName1=fileName;
            if(simpleDateFormat==null)simpleDateFormat = new SimpleDateFormat("yyMMddHHmmss");// HH:mm:ss
            //获取当前时间
            Date date = new Date(System.currentTimeMillis());
            startRecordSysTime1 = simpleDateFormat.format(date);
            insertDB(fileName+".mp4",RecordFileFactory.strDirector+"back/"+fileName,vedioDurtionTime0);
        }
        return 0;
    }

    public int OnStartRecordFailed(int error) {
        if (error != -1) {
            count++;
            if (isSDCard1Exit()) {
                if (count == 2) {
                    Intent it = new Intent();
                    it.setAction(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    it.putExtra("createDir", "CarCamera");
                    this.startActivityForResult(it, 44);
                    Log.d("RecordFailed", "OnStartRecordFailed");
                    count = 0;
                }
            }
        }else if(error == -2) {
            Log.d(TAG, "OnStartRecordFailed");
            //Intent intent = new Intent("com.carrecorder.media.error");
            //intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            //sendBroadcast(intent);
        }else {
            String strTip = "" + this.getText(R.string.str_create_failed);
            Toast t = Toast.makeText(this, strTip, Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
        }
        return 0;
    }

    public static boolean Camera_0_working = true;
    public static boolean Camera_1_working = true;
    public int onEncodeData(int nIndex, byte[] data, int len, long nTimeStamp) {
        //if(readLACC() == 0 && SwitchState()){
        //    return 0;
        //}
        //Log.e(TAG,"onEncodeData nIndex:" + nIndex);

        if (nIndex == 0) {
            Camera_0_working = true;
            byte[] NALUB = findNALU(0, data);
//            byte NALU = NALUB[0];
            byte offset = NALUB[1];
            if ((data[4] & 0x1F) == 7) {
                //Log.d("data2[4] & 0x1F)", "7:");
                sps1 = unPackPkg(data, offset, len);
            } else {

                if(sps1 == null || sps1.length == 0)
                    return 0;

                if (isStartVideoLive1||IPC_CONNECTED) {
                    if (isStartVideoLiveFistTime1||IPC_CONNECTED) {
//                        m_msgHandler.removeMessages(MSG_SEND_SPS_CAN1);
//                        Message message = new Message();
//                        message.what = MSG_SEND_SPS_CAN1;
//                        m_msgHandler.sendMessageDelayed(message, 1000);
                        byte NALU = NV21EncoderH264.findNALU(0, data)[0];
                        if ((NALU & 0x1F) == 5) {
                            int length = 0;
                            for (byte[] aByte : sps1) {
                                length += aByte.length;
                            }
                            length += len;
                            byte[] total_frame = new byte[length];

                            //for (byte[] aByte : sps1) {
                            System.arraycopy(sps1[0], 0, total_frame, 0, sps1[0].length);
                            System.arraycopy(sps1[1], 0, total_frame, sps1[0].length, sps1[1].length);
                            System.arraycopy(data, 0, total_frame, sps1[0].length + sps1[1].length, len);
                            if(IPC_CONNECTED){
                                Log.d("MAPGOO_IPC","PutH264Frame");
                                MapgooIPC.PutH264Frame(0,total_frame,length);
                            }
                            if(isStartVideoLive1)
                                manager.videoLive(total_frame, 1, liveClient, length, 2,nTimeStamp);
                            //Log.d("m_msgHandler", "MSG_SEND_SPS_CAN1" + aByte.length+" data:"+ByteUtil.bytesToHex(aByte));
                            //}
                            isStartVideoLiveFistTime1 = false;
                        }
                        //else
                        //{
                        //    manager.videoLive(data, 1, liveClient, len, 0);
                        //}
                    } else {
                        byte NALU = NV21EncoderH264.findNALU(0, data)[0];
                        if ((NALU & 0x1F) == 5) {
                            int length = 0;
                            for (byte[] aByte : sps1) {
                                length += aByte.length;
                            }
                            length += len;
                            byte[] total_frame = new byte[length];

                            //for (byte[] aByte : sps1) {
                            System.arraycopy(sps1[0], 0, total_frame, 0, sps1[0].length);
                            System.arraycopy(sps1[1], 0, total_frame, sps1[0].length, sps1[1].length);
                            System.arraycopy(data, 0, total_frame, sps1[0].length + sps1[1].length, len);
                            if(isStartVideoLive1)
                                manager.videoLive(total_frame, 1, liveClient, length, 2,nTimeStamp);
                            if(IPC_CONNECTED){
                                Log.d("MAPGOO_IPC","PutH264Frame");
                                MapgooIPC.PutH264Frame(0,total_frame,length);
                            }
                            //Log.d("m_msgHandler", "MSG_SEND_SPS_CAN1" + aByte.length+" data:"+ByteUtil.bytesToHex(aByte));
                            //}
                        }
                        else {
                            if(isStartVideoLive1)
                                manager.videoLive(data, 1, liveClient, len, 0, nTimeStamp);
                            if(IPC_CONNECTED){
                                Log.d("MAPGOO_IPC","PutH264Frame");
                                MapgooIPC.PutH264Frame(0,data,len);
                            }
                        }
                    }
                }

                if (isStartLocalVideoLive1) {
                    if (isStartLocalVideoLiveFistTime1) {
                        byte NALU = NV21EncoderH264.findNALU(0, data)[0];
                        if ((NALU & 0x1F) == 5) {
                            int length = 0;
                            for (byte[] aByte : sps1) {
                                length += aByte.length;
                            }
                            length += len;
                            byte[] total_frame = new byte[length];

                            //for (byte[] aByte : sps1) {
                            System.arraycopy(sps1[0], 0, total_frame, 0, sps1[0].length);
                            System.arraycopy(sps1[1], 0, total_frame, sps1[0].length, sps1[1].length);
                            System.arraycopy(data, 0, total_frame, sps1[0].length + sps1[1].length, len);

                            if(isStartLocalVideoLive1)
                                manager.videoLive(total_frame, 1, localLiveClient, length, 2,nTimeStamp);
                            //Log.d("m_msgHandler", "MSG_SEND_SPS_CAN1" + aByte.length+" data:"+ByteUtil.bytesToHex(aByte));
                            //}
                            isStartLocalVideoLiveFistTime1 = false;
                        }
                        //else
                        //{
                        //    manager.videoLive(data, 1, liveClient, len, 0);
                        //}
                    } else {
                        byte NALU = NV21EncoderH264.findNALU(0, data)[0];
                        if ((NALU & 0x1F) == 5) {
                            int length = 0;
                            for (byte[] aByte : sps1) {
                                length += aByte.length;
                            }
                            length += len;
                            byte[] total_frame = new byte[length];
                            System.arraycopy(sps1[0], 0, total_frame, 0, sps1[0].length);
                            System.arraycopy(sps1[1], 0, total_frame, sps1[0].length, sps1[1].length);
                            System.arraycopy(data, 0, total_frame, sps1[0].length + sps1[1].length, len);
                            if(isStartLocalVideoLive1)
                                manager.videoLive(total_frame, 1, localLiveClient, length, 2,nTimeStamp);

                        }
                        else {
                            if(isStartLocalVideoLive1)
                                manager.videoLive(data, 1, localLiveClient, len, 0, nTimeStamp);
                        }
                    }
                }
//----------------------------------------905 start ------------------------------------------------
                if (isStart905VideoLive1) {
                    Log.e(TAG,"jtt905 isStart905VideoLive1:" + isStart905VideoLive1 +  "  isStart905VideoLiveFistTime1:" + isStart905VideoLiveFistTime1);
                    if (isStart905VideoLiveFistTime1) {
                        byte NALU = NV21EncoderH264.findNALU(0, data)[0];
                        if ((NALU & 0x1F) == 5) {
                            int length = 0;
                            for (byte[] aByte : sps1) {
                                length += aByte.length;
                            }
                            length += len;
                            byte[] total_frame = new byte[length];

                            //for (byte[] aByte : sps1) {
                            System.arraycopy(sps1[0], 0, total_frame, 0, sps1[0].length);
                            System.arraycopy(sps1[1], 0, total_frame, sps1[0].length, sps1[1].length);
                            System.arraycopy(data, 0, total_frame, sps1[0].length + sps1[1].length, len);

                            if(isStart905VideoLive1)
                                manager.videoLive905(total_frame, 1, liveClient905, length, 2,nTimeStamp);
                            isStart905VideoLiveFistTime1 = false;
                        }
                    } else {
                        byte NALU = NV21EncoderH264.findNALU(0, data)[0];
                        if ((NALU & 0x1F) == 5) {
                            Log.e(TAG,"jtt905 I帧");
                            int length = 0;
                            for (byte[] aByte : sps1) {
                                length += aByte.length;
                            }
                            length += len;
                            byte[] total_frame = new byte[length];
                            System.arraycopy(sps1[0], 0, total_frame, 0, sps1[0].length);
                            System.arraycopy(sps1[1], 0, total_frame, sps1[0].length, sps1[1].length);
                            System.arraycopy(data, 0, total_frame, sps1[0].length + sps1[1].length, len);
                            if(isStart905VideoLive1)
                                manager.videoLive905(total_frame, 1, liveClient905, length, 2,nTimeStamp);

                        }else {
                            Log.e(TAG,"jtt905 非I帧");
                            if(isStart905VideoLive1)
                                manager.videoLive905(data, 1, liveClient905, len, 0, nTimeStamp);
                        }
                    }
                }
//----------------------------------------905 end ------------------------------------------------
            }
        }
        if (nIndex == 1) {
            Camera_1_working = true;
            byte[] NALUB = findNALU(0, data);
//            byte NALU = NALUB[0];
            byte offset = NALUB[1];
            if ((data[4] & 0x1F) == 7) {
                Log.d("data2[4] & 0x1F)", "7:");
                sps2 = unPackPkg(data, offset, len);
            } else {
                if(sps2 == null || sps2.length == 0)
                    return 0;
                if (isStartVideoLive2) {
                    if (isStartVideoLiveFistTime2) {
                        byte NALU = NV21EncoderH264.findNALU(0, data)[0];
                        if ((NALU & 0x1F) == 5) {
                            int length = 0;
                            for (byte[] aByte : sps2) {
                                length += aByte.length;
                            }
                            length += len;
                            byte[] total_frame = new byte[length];
                            System.arraycopy(sps2[0], 0, total_frame, 0, sps2[0].length);
                            System.arraycopy(sps2[1], 0, total_frame, sps2[0].length, sps2[1].length);
                            System.arraycopy(data, 0, total_frame, sps2[0].length + sps2[1].length, len);
                            manager.videoLive(total_frame, 2, liveClient2, length, 1,nTimeStamp);
                            isStartVideoLiveFistTime2 = false;
                        }
                    } else {
                        byte NALU = NV21EncoderH264.findNALU(0, data)[0];
                        if ((NALU & 0x1F) == 5) {
                            int length = 0;
                            for (byte[] aByte : sps2) {
                                length += aByte.length;
                            }
                            length += len;
                            byte[] total_frame = new byte[length];
                            System.arraycopy(sps2[0], 0, total_frame, 0, sps2[0].length);
                            System.arraycopy(sps2[1], 0, total_frame, sps2[0].length, sps2[1].length);
                            System.arraycopy(data, 0, total_frame, sps2[0].length + sps2[1].length, len);
                            manager.videoLive(total_frame, 2, liveClient2, length, 1,nTimeStamp);
                        }
                        else
                            manager.videoLive(data, 2, liveClient2, len, 1,nTimeStamp);
                    }
                }

                if (isStartLocalVideoLive2) {
                    if (isStartLocalVideoLiveFistTime2) {
//                        m_msgHandler.removeMessages(MSG_SEND_SPS_CAN2);
//                        Message message = new Message();
//                        message.what = MSG_SEND_SPS_CAN2;
//                        m_msgHandler.sendMessageDelayed(message, 1200);
                        byte NALU = NV21EncoderH264.findNALU(0, data)[0];
                        if ((NALU & 0x1F) == 5) {
                            int length = 0;
                            for (byte[] aByte : sps2) {
                                length += aByte.length;
                            }
                            length += len;
                            byte[] total_frame = new byte[length];
                            System.arraycopy(sps2[0], 0, total_frame, 0, sps2[0].length);
                            System.arraycopy(sps2[1], 0, total_frame, sps2[0].length, sps2[1].length);
                            System.arraycopy(data, 0, total_frame, sps2[0].length + sps2[1].length, len);
                            manager.videoLive(total_frame, 2, localLiveClient2, length, 1,nTimeStamp);
                            isStartLocalVideoLiveFistTime2 = false;
                        }
                    } else {
                        byte NALU = NV21EncoderH264.findNALU(0, data)[0];
                        if ((NALU & 0x1F) == 5) {
                            int length = 0;
                            for (byte[] aByte : sps2) {
                                length += aByte.length;
                            }
                            length += len;
                            byte[] total_frame = new byte[length];
                            System.arraycopy(sps2[0], 0, total_frame, 0, sps2[0].length);
                            System.arraycopy(sps2[1], 0, total_frame, sps2[0].length, sps2[1].length);
                            System.arraycopy(data, 0, total_frame, sps2[0].length + sps2[1].length, len);
                            manager.videoLive(total_frame, 2, localLiveClient2, length, 1,nTimeStamp);
                        }
                        else
                            manager.videoLive(data, 2, localLiveClient2, len, 1,nTimeStamp);
                    }
                }
//------------------------------ 905 start ---------------------------------------
                if (isStart905VideoLive2) {
                    if (isStart905VideoLiveFistTime2) {
                        byte NALU = NV21EncoderH264.findNALU(0, data)[0];
                        if ((NALU & 0x1F) == 5) {
                            int length = 0;
                            for (byte[] aByte : sps2) {
                                length += aByte.length;
                            }
                            length += len;
                            byte[] total_frame = new byte[length];
                            System.arraycopy(sps2[0], 0, total_frame, 0, sps2[0].length);
                            System.arraycopy(sps2[1], 0, total_frame, sps2[0].length, sps2[1].length);
                            System.arraycopy(data, 0, total_frame, sps2[0].length + sps2[1].length, len);
                            manager.videoLive905(total_frame, 2, liveClient2905, length, 1,nTimeStamp);
                            isStart905VideoLiveFistTime2 = false;
                        }
                    } else {
                        byte NALU = NV21EncoderH264.findNALU(0, data)[0];
                        if ((NALU & 0x1F) == 5) {
                            int length = 0;
                            for (byte[] aByte : sps2) {
                                length += aByte.length;
                            }
                            length += len;
                            byte[] total_frame = new byte[length];
                            System.arraycopy(sps2[0], 0, total_frame, 0, sps2[0].length);
                            System.arraycopy(sps2[1], 0, total_frame, sps2[0].length, sps2[1].length);
                            System.arraycopy(data, 0, total_frame, sps2[0].length + sps2[1].length, len);
                            manager.videoLive905(total_frame, 2, liveClient2905, length, 1,nTimeStamp);
                        }
                        else
                            manager.videoLive905(data, 2, liveClient2905, len, 1,nTimeStamp);
                    }
                }
//------------------------------- 905 end ----------------------------------------
            }
        }

        return 0;
    }

    public boolean OnAudioEncodedCheck(int nAudioType, int param, int nSampleRate, int nChannels) {
        if(settingBean.getPlatform()==0){
            if (nAudioType == carrecorder.AUDIOTYPE_G711) {
                return true;
            }
        }else if(settingBean.getPlatform()==1){
            if (nAudioType == carrecorder.AUDIOTYPE_AAC) {
                return true;
            }
        }
        return true;
    }

    public int OnAudioEncoded(byte[] data, int offset, int len, int nAudioType, int param, int nSampleRate, int nChannels) {
        if(settingBean.getPlatform()==0) {
            if (nAudioType == carrecorder.AUDIOTYPE_G711 && param == 0) {
                if (isStartVideoLive1) {
                    manager.audioLive(data, 1, liveClient, len, RecorActivity.this, 0,0);
                }
                if (isStartVideoLive2) {
                    manager.audioLive(data, 2, liveClient2, len, RecorActivity.this, 0,0);
                }
                if (isStartAudioLive1) {
                    manager.audioLive(data, mChannelNum, audioClient1, len, RecorActivity.this, 0,0);
                }

                if (isStartLocalVideoLive1) {
                    manager.audioLive(data, 1, localLiveClient, len, RecorActivity.this, 0,0);
                }

                if (isStartLocalVideoLive2) {
                    manager.audioLive(data, 2, localLiveClient2, len, RecorActivity.this, 0,0);
                }
                if (isStartLocalAudioLive1) {
                    manager.audioLive(data, mChannelNum, audioLocalClient1, len, RecorActivity.this, 0,0);
                }

                if (isStart905VideoLive1) {
                    manager.audioLive(data, 1, liveClient905, len, RecorActivity.this, 0,0);
                }

                if (isStart905VideoLive2) {
                    manager.audioLive(data, 2, liveClient2905, len, RecorActivity.this, 0,0);
                }
                if (isStar905AudioLive1) {
                    manager.audioLive(data, mChannelNum, audio905Client1, len, RecorActivity.this, 0,0);
                }

            }
        }else if(settingBean.getPlatform()==1){
            if (nAudioType == carrecorder.AUDIOTYPE_AAC && param == 0) {
                if (isStartVideoLive1) {
                    manager.audioLive(data, 1, liveClient, len, RecorActivity.this, 0,1);
                }
                if (isStartVideoLive2) {
                    manager.audioLive(data, 2, liveClient2, len, RecorActivity.this, 0,1);
                }
                if (isStartAudioLive1) {
                    manager.audioLive(data, mChannelNum, audioClient1, len, RecorActivity.this, 0,1);
                }
                if (isStartLocalVideoLive1) {
                    manager.audioLive(data, 1, localLiveClient, len, RecorActivity.this, 0,1);
                }
                if (isStartLocalVideoLive2) {
                    manager.audioLive(data, 2, localLiveClient2, len, RecorActivity.this, 0,1);
                }
                if (isStartLocalAudioLive1) {
                    manager.audioLive(data, mChannelNum, audioLocalClient1, len, RecorActivity.this, 0,1);
                }

                if (isStart905VideoLive1) {
                    manager.audioLive(data, 1, liveClient905, len, RecorActivity.this, 0,1);
                }
                if (isStart905VideoLive2) {
                    manager.audioLive(data, 2, liveClient2905, len, RecorActivity.this, 0,1);
                }
                if (isStar905AudioLive1) {
                    manager.audioLive(data, mChannelNum, audio905Client1, len, RecorActivity.this, 0,1);
                }
            }
        }

        if (nAudioType == carrecorder.AUDIOTYPE_PCM && (isStartAudioLive1 || isStartLocalAudioLive1 || isStar905AudioLive1)) {
            if (pout != null) {
                try {
                    pout.write(data, 0, len);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    public int OnTouch() {
        Log.d(TAG, "OnTouch");
        return 0;
    }

    private AdapterView.OnItemClickListener settingItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
            Log.d(TAG, "settingItemClick");
        }
    };


    private void startRecord() {
        if (isSDCard1Exit() && !isStartRecord) {
            if (AvailableSizeToRecord()) {
                Log.d(TAG, "startRecord~");
                isStartRecord = true;
                MediaSdk.Instant().VideoRecord_Start(0);
                MediaSdk.Instant().VideoRecord_Start(1);
                m_msgHandler.sendEmptyMessageDelayed(MSG_STOP_RECORD, 1000 * 60*5);
            }
        } else {

        }
    }

    private void stopRecord() {
        if (isStartRecord) {
            isStartRecord = false;
            MediaSdk.Instant().VideoRecord_Stop(1);
            MediaSdk.Instant().VideoRecord_Stop(0);
            /*if(SwitchState()){
                if(readLACC() == 1) {
                    m_msgHandler.sendEmptyMessageDelayed(MSG_START_RECORD, MSG_START_RECORD_START_TIME);
                }
            }else {
                m_msgHandler.sendEmptyMessageDelayed(MSG_START_RECORD, MSG_START_RECORD_START_TIME);
            }*/
            m_msgHandler.sendEmptyMessageDelayed(MSG_START_RECORD, MSG_START_RECORD_START_TIME);

            /*if(readLACC() == 1) {
                m_msgHandler.sendEmptyMessageDelayed(MSG_START_RECORD, MSG_START_RECORD_START_TIME);
            }else{
                Log.e(TAG,"停止录像");
            }*/
            if(simpleDateFormat==null)simpleDateFormat = new SimpleDateFormat("yyMMddHHmmss");// HH:mm:ss
            //获取当前时间
            Date date = new Date(System.currentTimeMillis());
            stopRecordSysTime0 = simpleDateFormat.format(date);
            vedioDurtionTime0 = getPlayBackDurationTime(startRecordSysTime0,stopRecordSysTime0);
            if (analysisViideo == null) analysisViideo = new AnalysisViideo(this);
            try {
                updateDB(recordFileName0+".mp4",RecordFileFactory.strDirector+"front/"+recordFileName0,vedioDurtionTime0,analysisViideo.getFileSize(new File(RecordFileFactory.strDirector+"front/"+recordFileName0+".mp4")));
            } catch (Exception e) {
                e.printStackTrace();
            }

            stopRecordSysTime1 = simpleDateFormat.format(date);
            vedioDurtionTime1 = getPlayBackDurationTime(startRecordSysTime1,stopRecordSysTime1);
            try {
                updateDB(recordFileName1+".mp4",RecordFileFactory.strDirector+"back/"+recordFileName1,vedioDurtionTime1,analysisViideo.getFileSize(new File(RecordFileFactory.strDirector+"back/"+recordFileName1+".mp4")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            vedioDurtionTime1=0;
            vedioDurtionTime0=0;
        }
    }

    // д SharedPreferences
    public int SetSharedPreferences(String strKey, String strValue) {
        SharedPreferences preferences = getSharedPreferences("MMCRecord", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(strKey, strValue);
        editor.commit();
        return 0;
    }

    // ��ȡ SharedPreferences
    public String GetSharedPreferences(String strKey, String strDefalutValue) {
        SharedPreferences preferences = getSharedPreferences("MMCRecord",
                MODE_PRIVATE);
        return preferences.getString(strKey, strDefalutValue);
    }

    public byte[] getFromAssets(String fileName) {
        byte[] buffer = null;
        try {
            InputStream in = getResources().getAssets().open(fileName);
            int lenght = in.available();
            buffer = new byte[lenght];
            in.read(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer;
    }
    private short[] pcmdata;
    private String fileName = "audiofile" + ".pcm";
    private  FileOutputStream outStream;
    @Override
    public void receiveData(byte[] head,byte[] vpx,byte[] packageNum,byte[] sim,byte[] channleNum,
                            byte[] dataType,byte[] time,byte[] dataLen,byte[] mdata) {
        Log.d(TAG,"\n"+"head"+
                "\n"+"head:"+ByteUtil.bytesToHex(head)+
                "\n"+"vpx:"+ByteUtil.bytesToHex(vpx)+
                "\n"+"packageNum:"+ByteUtil.bytesToHex(packageNum)+
                "\n"+"sim:"+ByteUtil.bytesToHex(sim)+
                "\n"+"channleNum:"+ByteUtil.bytesToHex(channleNum)+
                "\n"+"dataType:"+ByteUtil.bytesToHex(dataType)+
                "\n"+"time:"+ByteUtil.bytesToHex(time)+
                "\n"+"dataLen:"+ByteUtil.bytesToHex(dataLen)+
                "\n"+"mdata :"+ByteUtil.bytesToHex(mdata)+
                "\n"+"mdata len:"+mdata.length);

        try {
            outStream= RecorActivity.this.openFileOutput(fileName, Context.MODE_APPEND);
            //outStream.write(convertG711ToPcm(mdata,mdata.length,pcmdata));
            pcmdata = new short[mdata.length];
            outStream.write(AudioTrackManager.G711aDecoder(pcmdata,mdata,mdata.length));
            outStream.close();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }


    private class netWorkListing extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("mobInfo","intent:"+intent.getAction());
            if (intent.getAction().equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mobInfo = connectivityManager.getActiveNetworkInfo();
                //  Log.d(TAG, "mobInfo" + mobInfo.getTypeName());
                if(mobInfo!=null) {
                    Log.d(TAG, "mobInfo" + mobInfo.getTypeName());
                    if (mobInfo.isConnected()) {
                        Log.d(TAG, "mobInfo");
                        initJTT808();
                        initLocalJTT808();
                        init905JTT808();
                        startLocation();
                    } else {
                        Log.d(TAG, "网络断开或者没有网络");
                    }
                }
            } else if (intent.getAction().equalsIgnoreCase("SDCARD_READY")) {
                Log.d(TAG, "插入SD卡");
               // startRecord();
            } else if (intent.getAction().equalsIgnoreCase("SDCARD_REMOVE")) {
                Log.d(TAG, "拔出SD卡");
                //stopRecord();
            }else if(intent.getAction().equalsIgnoreCase("ACC_STATUS_CHANGE")){
                if(readLCD()==0) {
                    Log.d(TAG, "打开LCD~~~");
                    closeLCD(1);
                }else{
                    closeLCD(0);
                    Log.d(TAG, "关闭LCD~~~");
                }
            }
        }

    }

    @Override
    public void onStateChange(List<TitleBean> mBeans, int postion) {
        SharePreUtil.putString(this, "IP", mBeans.get(0).getValue());
        settingBean.setIP(mBeans.get(0).getValue());
        Log.d("onStateChange", mBeans.get(1).getValue());
        try {
            SharePreUtil.putInt(this, "PORT", Integer.parseInt(mBeans.get(1).getValue()));
            settingBean.setPORT(Integer.parseInt(mBeans.get(1).getValue()));
        } catch (Exception e) {

        }

        SharePreUtil.putString(this, "IP_LOCAL", mBeans.get(2).getValue());
        settingBean.setIP_LOCAL(mBeans.get(2).getValue());
        try {
            SharePreUtil.putInt(this, "PORT_LOCAL", Integer.parseInt(mBeans.get(3).getValue()));
            settingBean.setPORT_LOCAL(Integer.parseInt(mBeans.get(3).getValue()));
        } catch (Exception e) {

        }

        SharePreUtil.putString(this, "TERMINAL_ID", mBeans.get(4).getValue());
        settingBean.setTERMINAL_ID(mBeans.get(4).getValue());
        SharePreUtil.putString(this, "PHONE", mBeans.get(5).getValue());
        settingBean.setPHONE(mBeans.get(5).getValue());

        SharePreUtil.putString(this, "TERMINAL_MODEL", mBeans.get(7).getValue());
        settingBean.setTERMINAL_MODEL(mBeans.get(7).getValue());
        saveSettingsToFile();
        m_msgHandler.removeMessages(MSG_DELAY_UPDATE_SETTING);
        m_msgHandler.sendEmptyMessageDelayed(MSG_DELAY_UPDATE_SETTING, 60000);
    }

    private AdapterView.OnItemClickListener mainItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
            m_msgHandler.removeMessages(MSG_LCD_CLOSE);
            Log.d(TAG, "----------onItemClick  set screen off time -------- ");
            if(position == 0){
                m_msgHandler.sendEmptyMessageDelayed(MSG_LCD_CLOSE,DEFAULT_TIME_SCREEN_OFF*6);
            }else if(position == 1 || position == 2 || position == 5){
                m_msgHandler.sendEmptyMessageDelayed(MSG_LCD_CLOSE,DEFAULT_TIME_SCREEN_OFF*10);
            }else{
                m_msgHandler.sendEmptyMessageDelayed(MSG_LCD_CLOSE,DEFAULT_TIME_SCREEN_OFF);
            }
            Log.e(TAG,"readLCD() " + readLCD());
            if(readLCD()==0){
                Log.e(TAG,"lcd is close,don't handle click event");
                return;
            }
            switch (position) {
                case 0:
                    mainRelativeLayout.setVisibility(View.GONE);
                    break;
                case 1:
                    Intent intentS = new Intent();
                    ComponentName componentNameS = new ComponentName("com.android.settings", "com.android.settings.Settings");
                    try {
                        intentS.setComponent(componentNameS);
                        startActivity(intentS);
                    } catch (Exception e) {

                    }

                    break;
                case 2:
                    Intent intentF = new Intent();
                    ComponentName componentNameF = new ComponentName("com.mediatek.filemanager", "com.mediatek.filemanager.FileManagerOperationActivity");
                    try {
                        intentF.setComponent(componentNameF);
                        startActivity(intentF);
                    } catch (Exception e) {

                    }
                    break;
                case 3:
                    settingListView.setVisibility(View.VISIBLE);
                    gridView.setVisibility(View.GONE);
                    break;
                case 4:
                    showSecletDialog();
                    break;
                case 6: //无线升级
                    handleOTA(false);
                    break;
            }
        }
    };

    private void handleOTA(boolean silence){
        /** 新版本 **/
        new PgyUpdateManager.Builder()
                .setForced(false)                //设置是否强制提示更新,非自定义回调更新接口此方法有用
                .setUserCanRetry(false)         //失败后是否提示重新下载，非自定义下载 apk 回调此方法有用
                .setDeleteHistroyApk(true)     // 检查更新前是否删除本地历史 Apk， 默认为true
                .setUpdateManagerListener(new UpdateManagerListener() {
                    @Override
                    public void onNoUpdateAvailable() {
                        //没有更新是回调此方法
                        Log.d("pgyer", "there is no new version");
                        Toast.makeText(getApplicationContext(),"已经是最新版本!",Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onUpdateAvailable(AppBean appBean) {
                        Toast.makeText(getApplicationContext(),"发现新版本，开始升级!",Toast.LENGTH_SHORT).show();
                        //有更新回调此方法
                        Log.d("pgyer", "there is new version can update"
                                + "new versionCode is " + appBean.getVersionCode());
                        //调用以下方法，DownloadFileListener 才有效；
                        //如果完全使用自己的下载方法，不需要设置DownloadFileListener
                        PgyUpdateManager.downLoadApk(appBean.getDownloadURL());
                    }

                    @Override
                    public void checkUpdateFailed(Exception e) {
                        //更新检测失败回调
                        //更新拒绝（应用被下架，过期，不在安装有效期，下载次数用尽）以及无网络情况会调用此接口
                        Log.e("pgyer", "check update failed ", e);
                        Toast.makeText(getApplicationContext(),"更新检测失败，请稍后重试!",Toast.LENGTH_SHORT).show();
                    }
                })
                //注意 ：
                //下载方法调用 PgyUpdateManager.downLoadApk(appBean.getDownloadURL()); 此回调才有效
                //此方法是方便用户自己实现下载进度和状态的 UI 提供的回调
                //想要使用蒲公英的默认下载进度的UI则不设置此方法
                .setDownloadFileListener(new DownloadFileListener() {
                    @Override
                    public void downloadFailed() {
                        //下载失败
                        Log.e("pgyer", "download apk failed");
                    }

                    @Override
                    public void downloadSuccessful(File file) {
                        Log.e("pgyer", "download apk success path is " + file.getAbsolutePath());
                        // 使用蒲公英提供的安装方法提示用户 安装apk
                        if(!silence) {
                            PgyUpdateManager.installApk(file);
                        }else {
                            Demo.getInstance().run(getApplicationContext().getPackageManager(), file.getAbsolutePath());
                        }
                    }

                    @Override
                    public void onProgressUpdate(Integer... integers) {
                        Log.e("pgyer", "update download apk progress" + integers);
                    }})
                .register();
    }

    private void installBySystem(String absolutepath){
        Intent intent = new Intent("com.example.dell.carrecorder.insall");
        intent.putExtra("path",absolutepath);
        sendBroadcast(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG,"keyCode:"+keyCode);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        m_msgHandler.removeMessages(MSG_LCD_CLOSE);
        if(keyCode == KeyEvent.KEYCODE_CAMERA) {
            m_msgHandler.sendEmptyMessageDelayed(MSG_LCD_CLOSE, DEFAULT_TIME_SCREEN_OFF*6);
        }else{
            m_msgHandler.sendEmptyMessageDelayed(MSG_LCD_CLOSE, DEFAULT_TIME_SCREEN_OFF);
        }
        if(readLCD()==0 &&(keyCode == KeyEvent.KEYCODE_CAMERA || keyCode == KeyEvent.KEYCODE_BACK)){
            Log.e(TAG,"lcd is close, turn screen on");
            closeLCD(1);
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (settingListView != null && settingListView.isShown()) {
                mainRelativeLayout.setVisibility(View.VISIBLE);
                settingListView.setVisibility(View.GONE);
                gridView.setVisibility(View.VISIBLE);
            } else {
                mainRelativeLayout.setVisibility(View.VISIBLE);
            }
            return false;
        }else if(keyCode == KeyEvent.KEYCODE_F8){
            Log.d(TAG,"切换摄像头黑白模式");
            int state = readLightdet();
            MediaSdk.Instant().setCameraMode(state);
        }else if(keyCode == KeyEvent.KEYCODE_F9){
            Log.d(TAG,"报警 报警 报警");
            alarm=1;
            if(manager!=null)
                manager.uploadLocation(mlatitude,mlongitude,mSpeed,alarm,acc);
            alarm=0;

        }else if(keyCode == KeyEvent.KEYCODE_F10){
            Log.d(TAG,"ACC 开");
            if(manager!=null)
                manager.uploadLocation(mlatitude, mlongitude,mSpeed,alarm,acc);
            alarm=0;
            acc=1;
            closeLCD(1);
            m_msgHandler.removeMessages(MSG_POWER_OFF);
          /*  if(SwitchState()) {
                m_msgHandler.removeMessages(MSG_SHUTDOWN);
                initCamera();
            }*/
            if(isStartRecord = false){
                startRecord();
            }

        }else if(keyCode == KeyEvent.KEYCODE_F11){
            Log.d(TAG,"ACC 关");
            if(isStartRecord = true) {
                stopRecord();
            }
            if(manager!=null)
                manager.uploadLocation(mlatitude, mlongitude,mSpeed,alarm,acc);
            alarm=0;
            acc=0;
           // closeLCD(0);
            stopRecord();
            //accclose MSG_POWER_OFF
          //  m_msgHandler.sendEmptyMessageDelayed(MSG_POWER_OFF,500);
            closeLCD(0);
            m_msgHandler.removeMessages(MSG_LCD_CLOSE);
            if(SwitchState()) { //开启熄火关机以后，20分钟以后关机
                Log.d(TAG,"熄火,关机");
                m_msgHandler.sendEmptyMessageDelayed(MSG_SHUTDOWN, 200/*1000*60*20*/);
                //stopPreview();
            }
        }else if(keyCode == KeyEvent.KEYCODE_CAMERA){
            mainRelativeLayout.setVisibility(View.GONE);
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * chengrq
     */
    private void stopPreview(){
        MediaSdk.Instant().Preview_Stop();
        MediaSdk.Instant().Preview_Delete();
        MediaSdk.Instant().Camera_Stop();
        MediaSdk.Instant().Clean();
        mainRelativeLayout.setVisibility(View.VISIBLE);
    }

   /* private void startPreview(){
        MediaSdk.Instant().Initial();
        MediaSdk.Instant().Camera_Start(this, m_msgHandler);
        MediaSdk.Instant().Preview_Start();
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        m_msgHandler.sendEmptyMessageDelayed(MSG_LCD_CLOSE,DEFAULT_TIME_SCREEN_OFF);
        m_msgHandler.removeMessages(MSG_START_RECORD);
        m_msgHandler.sendEmptyMessageDelayed(MSG_START_RECORD, MSG_START_RECORD_START_TIME);
        acquireWakeLock();
        checkVersionCode();
    }

    private void checkVersionCode(){
        SharedPreferences versioncode = getSharedPreferences("versioncode",Context.MODE_PRIVATE);
        int versionCode = versioncode.getInt("versionCode",0);
        int currentversionCode = getCurrentVersionCode();
        if(versionCode > 0 && versionCode < currentversionCode){
            Toast.makeText(getApplicationContext(),"更新成功",Toast.LENGTH_SHORT).show();
        }
        if(versionCode != currentversionCode){
            SharedPreferences.Editor editor = versioncode.edit();
            editor.putInt("versionCode",currentversionCode);
            editor.commit();
        }
    }

    private int getCurrentVersionCode(){
        int versioncode = 0;
        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
            versioncode = packInfo.versionCode;
        }catch (Exception e){
            e.printStackTrace();
        }
        return versioncode;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
//        stopRecord();
//        m_msgHandler.removeMessages(MSG_START_RECORD);
//        m_msgHandler.removeMessages(MSG_STOP_RECORD);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
        stopRecord();
        m_msgHandler.removeMessages(MSG_START_RECORD);
        m_msgHandler.removeMessages(MSG_STOP_RECORD);
        unregisterReceiver(mShutdownReceiver);
        /*if(location808 != null) {
            location808.stopLocation();
            location808 = null;
        }*/
        Location808.getInstance().stopLocation();
        releaseWakeLock();
    }

    private void initJTT808() {
        Log.d(TAG, "initJTT808");
        if (manager != null){
            manager.disconnect();
            /*manager.disconnectLocal();
            if(Constants.IsJTT905Enable) {
                manager.disconnect905();
            }*/
        }
        IP = SharePreUtil.getString(this, "IP", Constants.IP);
        PORT = SharePreUtil.getInt(this, "PORT", Constants.PORT);
        /*IP_LOCAL = SharePreUtil.getString(this, "IP_LOCAL", Constants.Local_ip);
        PORT_LOCAL = SharePreUtil.getInt(this, "PORT_LOCAL", Constants.Local_PORT);*/


        if (SharePreUtil.getString(this, "PHONE", Constants.PHONE).equalsIgnoreCase(getString(R.string.NO_SIM))) {
            PHONE = Constants.PHONE;
        } else {
            PHONE = SharePreUtil.getString(this, "PHONE", Constants.PHONE);
        }
        MANUFACTURER_ID = SharePreUtil.getString(this, "MANUFACTURER_ID", Constants.MANUFACTURER_ID);
        TERMINAL_MODEL = SharePreUtil.getString(this, "TERMINAL_MODEL", Constants.TERMINAL_MODEL);
        TERMINAL_ID = SharePreUtil.getString(this, "TERMINAL_ID", Constants.TERMINAL_ID);

        manager = JTT808Manager.getInstance();
        manager.setOnConnectionListener(this);

        manager.init(PHONE, TERMINAL_ID,IP, PORT);
        /*manager.initLocal(PHONE, TERMINAL_ID,IP_LOCAL, PORT_LOCAL);
        if(Constants.IsJTT905Enable) {
            manager.init905(Constants.ISU, TERMINAL_ID, Constants.ip_905, Constants.port_905);
        }*/
    }

    private void initLocalJTT808() {
        if (manager != null){
            manager.disconnectLocal();
        }
        IP_LOCAL = SharePreUtil.getString(this, "IP_LOCAL", Constants.Local_ip);
        PORT_LOCAL = SharePreUtil.getInt(this, "PORT_LOCAL", Constants.Local_PORT);
        if (SharePreUtil.getString(this, "PHONE", Constants.PHONE).equalsIgnoreCase(getString(R.string.NO_SIM))) {
            PHONE = Constants.PHONE;
        } else {
            PHONE = SharePreUtil.getString(this, "PHONE", Constants.PHONE);
        }
        MANUFACTURER_ID = SharePreUtil.getString(this, "MANUFACTURER_ID", Constants.MANUFACTURER_ID);
        TERMINAL_MODEL = SharePreUtil.getString(this, "TERMINAL_MODEL", Constants.TERMINAL_MODEL);
        TERMINAL_ID = SharePreUtil.getString(this, "TERMINAL_ID", Constants.TERMINAL_ID);

        manager = JTT808Manager.getInstance();
        manager.setOnConnectionListener(this);
        manager.initLocal(PHONE, TERMINAL_ID,IP_LOCAL, PORT_LOCAL);
    }

    private void init905JTT808() {
        if (manager != null){
            if(Constants.IsJTT905Enable) {
                manager.disconnect905();
            }
        }
        MANUFACTURER_ID = SharePreUtil.getString(this, "MANUFACTURER_ID", Constants.MANUFACTURER_ID);
        TERMINAL_MODEL = SharePreUtil.getString(this, "TERMINAL_MODEL", Constants.TERMINAL_MODEL);
        TERMINAL_ID = SharePreUtil.getString(this, "TERMINAL_ID", Constants.TERMINAL_ID);
        manager = JTT808Manager.getInstance();
        //manager.setOnConnectionListener(this);
        if(Constants.IsJTT905Enable) {
            manager.setOnConnectionListener(this)
                    .init905(Constants.ISU, TERMINAL_ID, Constants.ip_905, Constants.port_905);
        }
    }

    private void startLocation(){
        location808 = Location808.getInstance()
                .setContext(getApplicationContext())
                .setListener(this);
        location808.getLocation();
    }

    @Override
    public void on905ConnectionSateChange(int state) {
        switch (state) {
            case OnConnectionListener.CONNECTED:
                Log.d(TAG, "start register 905");
                manager.register905(MANUFACTURER_ID, TERMINAL_MODEL);
                break;
            case OnConnectionListener.DIS_CONNECT:
                Log.d(TAG, "905断开连接");
                break;
            case OnConnectionListener.RE_CONNECT:
                Log.d(TAG, "905重连");
                break;
            default:
                break;
        }
    }

    @Override
    public void onLocalConnectionSateChange(int state) {
        switch (state) {
            case OnConnectionListener.CONNECTED:
                Log.d(TAG, "start regist local server");
                manager.registerLocal(MANUFACTURER_ID, TERMINAL_MODEL);
                break;
            case OnConnectionListener.DIS_CONNECT:
                Log.d(TAG, "local server断开连接");
                break;
            case OnConnectionListener.RE_CONNECT:
                Log.d(TAG, "local server重连");
                break;
            default:
                break;
        }
    }

    @Override
    public void onConnectionSateChange(int state) {
        switch (state) {
            case OnConnectionListener.CONNECTED:
                Log.d(TAG, "start regist server");
                manager.register(MANUFACTURER_ID, TERMINAL_MODEL);
                break;
            case OnConnectionListener.DIS_CONNECT:
                Log.d(TAG, "server断开连接");
                break;
            case OnConnectionListener.RE_CONNECT:
                Log.d(TAG, "server重连");
                break;
            default:
                break;
        }
    }

    @Override
    public void receiveData(JTT808Bean jtt808Bean) {
    }

    @Override
    public void terminalParams(List<TerminalParamsBean> params) {
        for (TerminalParamsBean param : params) {
            int id = param.getId();
            if (Integer.class.equals(param.getClz())) {
                int value = (int) param.getValue();
            } else if (String.class.equals(param.getClz())) {
                String value = (String) param.getValue();
            } else if (Byte.class.equals(param.getClz())) {
                Byte value = (Byte) param.getValue();
            }
            switch (id) {
                //最高速度，单位为公里每小时(km/h)
                case 0x0055:
                    break;
                default:
                    break;
            }
        }
    }
    public long m_hAD = 0;
    private boolean startSpeak =false;
    @Override
    public void audioVideoLive(String ip, int port, int channelNum, int dataType) {
        Log.d(TAG, "server ip:" + ip + " port:" + port + " channelNum:" + channelNum + "  dataType:" + dataType);
        /*if(location808 != null) {
            location808.heartAlive(true);
        }*/
        /*if(readLACC() == 0 && SwitchState()){
            return;
        }
        */
        mChannelNum = channelNum;
        if (dataType == 0) {
            if (liveClient == null) liveClient = new LiveClient(ip, port);
            if (liveClient2 == null) liveClient2 = new LiveClient(ip, port);
            if (channelNum == 1) {
                Log.d(TAG, "server前摄像头实时视频");
                startLive(channelNum);
            } else if (channelNum == 2) {
                Log.d(TAG, "server后摄像头实时视频");
                startLive(channelNum);
            }

        } else if (dataType == 2) {
            if (channelNum == 1)
                Log.d(TAG, "server开始对讲");
            if (audioClient1 == null) audioClient1 = new LiveClient(ip, port);
            audioClient1.setLiveClientCallBack(this);
            isStartAudioLive1 = true;
            startSpeak=true;
            m_msgHandler.sendEmptyMessageDelayed(MSG_START_SPEAK,2000);

        }else if(dataType==3){
            //监听
            if (channelNum == 1)
                Log.d(TAG, "server开始监听");
            if (audioClient1 == null) audioClient1 = new LiveClient(ip, port);
            audioClient1.setLiveClientCallBack(this);
            isStartAudioLive1 = true;
            m_msgHandler.sendEmptyMessageDelayed(MSG_MONITER,1000);
        }
    }


    @Override
    public void localAudioVideoLive(String ip, int port, int channelNum, int dataType) {
        Log.d(TAG, "local server ip:" + ip + " port:" + port + " channelNum:" + channelNum + "  dataType:" + dataType);
        /*if(location808 != null) {
            location808.heartAlive(true);
        }*/
        /*if(readLACC() == 0 && SwitchState()){
            return;
        }*/
        mChannelNum = channelNum;
        if (dataType == 0) {
            if (localLiveClient == null) localLiveClient = new LiveClient(ip, port);
            if (localLiveClient2 == null) localLiveClient2 = new LiveClient(ip, port);
            if (channelNum == 1) {
                Log.d(TAG, "local server前摄像头实时视频");
                startLocalLive(channelNum);
            } else if (channelNum == 2) {
                Log.d(TAG, "local server后摄像头实时视频");
                startLocalLive(channelNum);
            }

        } else if (dataType == 2) {
            if (channelNum == 1)
                Log.d(TAG, "local server开始对讲");
            if (audioLocalClient1 == null) audioLocalClient1 = new LiveClient(ip, port);
            audioLocalClient1.setLiveClientCallBack(this);
            isStartLocalAudioLive1 = true;
            startSpeak=true;
            m_msgHandler.sendEmptyMessageDelayed(MSG_START_SPEAK,2000);

        }else if(dataType==3){
            //监听
            if (channelNum == 1)
                Log.d(TAG, "local server开始监听");
            if (audioLocalClient1 == null) audioLocalClient1 = new LiveClient(ip, port);
            audioLocalClient1.setLiveClientCallBack(this);
            isStartLocalAudioLive1 = true;
            m_msgHandler.sendEmptyMessageDelayed(MSG_MONITER,1000);
        }
    }

    @Override
    public void audioVideoLive905(String ip, int port, int channelNum, int dataType) {
        Log.d(TAG, "jtt905 ip:" + ip + " port:" + port + " channelNum:" + channelNum + "  dataType:" + dataType);
        /*if(location808 != null) {
            location808.heartAlive(true);
        }*/
        /*if(readLACC() == 0 && SwitchState()){
            return;
        }*/
        mChannelNum = channelNum;
        if (dataType == 0) {
            if (liveClient905 == null) liveClient905 = new LiveClient(ip, port);
            if (liveClient2905 == null) liveClient2905 = new LiveClient(ip, port);
            if (channelNum == 1) {
                Log.d(TAG, "jtt905前摄像头实时视频");
                start905Live(channelNum);
            } else if (channelNum == 2) {
                Log.d(TAG, "jtt905后摄像头实时视频");
                start905Live(channelNum);
            }

        } else if (dataType == 2) {
            if (channelNum == 1)
                Log.d(TAG, "jtt905开始对讲");
            if (audio905Client1 == null) audio905Client1 = new LiveClient(ip, port);
            audio905Client1.setLiveClientCallBack(this);
            isStar905AudioLive1 = true;
            startSpeak=true;
            m_msgHandler.sendEmptyMessageDelayed(MSG_START_SPEAK,2000);

        }else if(dataType==3){
            //监听
            if (channelNum == 1)
                Log.d(TAG, "jtt905开始监听");
            if (audio905Client1 == null) audio905Client1 = new LiveClient(ip, port);
            audio905Client1.setLiveClientCallBack(this);
            isStar905AudioLive1 = true;
            m_msgHandler.sendEmptyMessageDelayed(MSG_MONITER,1000);
        }
    }

    @Override
    public void audioVideoLiveControl(int channelNum, int control, int closeAudio, int switchStream) {
        Log.d("audioVideoLive", "Control:" + " channelNum:" + channelNum +
                "  control:" + control + "  closeAudio:" + closeAudio + " switchStream:" + switchStream);
        if (control == 0 || control == 3) {
            if (channelNum == 1) {
                Log.d("audioVideoLive", "关闭前摄像头实时视频");
                isStartVideoLive1 = false;
                if (liveClient != null) liveClient.release();
                liveClient = null;
                if (audioClient1 != null) audioClient1.release();
                audioClient1 = null;
            } else if (channelNum == 2) {
                Log.d("audioVideoLive", "关闭后摄像头实时视频");
                isStartVideoLive2 = false;
                if (liveClient2 != null) liveClient2.release();
                liveClient2 = null;
            }
        }
        if (control == 4) {
            if (channelNum == 1) {
                Log.d("audioVideoLive", "关闭对讲");
                isStartAudioLive1 = false;
                if (audioClient1 != null) audioClient1.release();
                audioClient1 = null;
//                audioPlayer.stopPlay();
                pout = null;
                AudioTrackManager.getInstance().stopPlay();
                File file = new File("/data/data/com.example.administrator.carrecorder/files/"+fileName);
                if(file.exists())file.delete();
            }
        }
    }

    @Override
    public void audioVideoLocalLiveControl(int channelNum, int control, int closeAudio, int switchStream) {
        Log.d(TAG, "local Control:" + " channelNum:" + channelNum +
                "  control:" + control + "  closeAudio:" + closeAudio + " switchStream:" + switchStream);
        if (control == 0 || control == 3) {
            if (channelNum == 1) {
                Log.d(TAG, "local关闭前摄像头实时视频");
                isStartLocalVideoLive1 = false;
                if (localLiveClient != null) localLiveClient.release();
                localLiveClient = null;
                if (audioLocalClient1 != null) audioLocalClient1.release();
                audioLocalClient1 = null;
            } else if (channelNum == 2) {
                Log.d(TAG, "local关闭后摄像头实时视频");
                isStartLocalVideoLive2 = false;
                if (localLiveClient2 != null) localLiveClient2.release();
                localLiveClient2 = null;
            }
        }
        if (control == 4) {
            if (channelNum == 1) {
                Log.d(TAG, "local关闭对讲");
                isStartLocalAudioLive1 = false;
                if (audioLocalClient1 != null) audioLocalClient1.release();
                audioLocalClient1 = null;
//                audioPlayer.stopPlay();
                pout = null;
                AudioTrackManager.getInstance().stopPlay();
                File file = new File("/data/data/com.example.administrator.carrecorder/files/"+fileName);
                if(file.exists())
                    file.delete();
            }
        }
    }

    @Override
    public void audioVideo905LiveControl(int channelNum, int control, int closeAudio, int switchStream) {
        Log.d(TAG, "Control:" + " channelNum:" + channelNum +
                "  control:" + control + "  closeAudio:" + closeAudio + " switchStream:" + switchStream);
        if (control == 0 || control == 3) {
            if (channelNum == 1) {
                Log.d(TAG, "jtt905关闭前摄像头实时视频");
                isStart905VideoLive1 = false;
                if (liveClient905 != null) liveClient905.release();
                liveClient905 = null;
                if (audio905Client1 != null) audio905Client1.release();
                audio905Client1 = null;
            } else if (channelNum == 2) {
                Log.d(TAG, "jtt905关闭后摄像头实时视频");
                isStart905VideoLive2 = false;
                if (liveClient2905 != null) liveClient2905.release();
                liveClient2905 = null;
            }
        }
        if (control == 4) {
            if (channelNum == 1) {
                Log.d(TAG, "jtt905关闭对讲");
                isStar905AudioLive1 = false;
                if (audio905Client1 != null) audio905Client1.release();
                audio905Client1 = null;
//                audioPlayer.stopPlay();
                pout = null;
                AudioTrackManager.getInstance().stopPlay();
                File file = new File("/data/data/com.example.administrator.carrecorder/files/"+fileName);
                if(file.exists())
                    file.delete();
            }
        }
    }

    private List<timeBean> timeBeanlist;
    private List<timeBean> subtimeBeanlist;
    private AnalysisViideo analysisViideo;
    private List<vedioBean> vedioBeans;

    @Override
    public void localVideoList(byte[] flowNo, int channelNum, String startTIme, String stoptTIme,
                               String alarmSign, int dataType, int bitstreamype, int storageType) {
        Log.d(TAG, "localVideoList:"+"channelNum:" + channelNum
                + " startTIme:" + startTIme + " stoptTIme:" + stoptTIme + " alarmSign:" + alarmSign + " dataType:" + dataType
                + " bitstreamype:" + bitstreamype + " storageType:" + storageType);
        if (timeBeanlist != null) timeBeanlist.clear();
        if(vedioBeans!=null) vedioBeans.clear();
        if (analysisViideo == null) analysisViideo = new AnalysisViideo(this);
        if (vedioBeans == null) vedioBeans = new ArrayList<vedioBean>();
        vedioBeans = queryDataList();
        Log.d(TAG, "VideoList lists size " + vedioBeans.size());
        for (int i = 0; i < vedioBeans.size(); i++) {
            Log.d("VideoList:", "ok?--?  " + vedioBeans.get(i).getName());
            if (DateFormat.checkDateV(analysisViideo.getVideoBuildTime(vedioBeans.get(i).getName()), "20" + startTIme, "20" + stoptTIme)) {
                if ((channelNum==0)||((Integer.parseInt(vedioBeans.get(i).getName().substring(3, 4)) +1 ) == channelNum)) {
                    timeBean bean = new timeBean();
                    bean.setStartTime(vedioBeans.get(i).getName().substring(7, 19));
                    Log.d("VideoList", "substring:" + vedioBeans.get(i).getSize());
                    bean.setDuration(vedioBeans.get(i).getDuration()/1000);
                    bean.setEndTime(getVideoLongTime(vedioBeans.get(i).getName().substring(7, 19), bean.getDuration()));
                    bean.setPath(vedioBeans.get(i).getPath());
                    bean.setName(vedioBeans.get(i).getName());
                    bean.setSize(vedioBeans.get(i).getSize());
                    bean.setChannelNum(Integer.parseInt(vedioBeans.get(i).getName().substring(3, 4)) +1);
                    Log.d("VideoList", "getPath:" + vedioBeans.get(i).getPath());
                    if (timeBeanlist == null) timeBeanlist = new ArrayList<timeBean>();
                    if (vedioBeans.get(i).getSize() != 0 && bean.getDuration() != 0)
                        timeBeanlist.add(bean);
                }
            }
        }
        if (timeBeanlist != null) {
            manager.uploadVideoList(flowNo, channelNum, false, 0, 0, timeBeanlist);
        }
        else
        {
            manager.uploadVideoList(flowNo, channelNum, false, 0, 0, new ArrayList<timeBean>());
        }
    }

    @Override
    public void localVideoListLocal(byte[] flowNo, int channelNum, String startTIme, String stoptTIme,
                               String alarmSign, int dataType, int bitstreamype, int storageType) {
        Log.d(TAG, "localVideoListLocal:"+"channelNum:" + channelNum
                + " startTIme:" + startTIme + " stoptTIme:" + stoptTIme + " alarmSign:" + alarmSign + " dataType:" + dataType
                + " bitstreamype:" + bitstreamype + " storageType:" + storageType);
        if (timeBeanlist != null) timeBeanlist.clear();
        if(vedioBeans!=null) vedioBeans.clear();
        if (analysisViideo == null) analysisViideo = new AnalysisViideo(this);
        if (vedioBeans == null) vedioBeans = new ArrayList<vedioBean>();
        vedioBeans = queryDataList();
        Log.d(TAG, "VideoList lists size " + vedioBeans.size());
        for (int i = 0; i < vedioBeans.size(); i++) {
            Log.d("VideoList:", "ok?--?  " + vedioBeans.get(i).getName());
            if (DateFormat.checkDateV(analysisViideo.getVideoBuildTime(vedioBeans.get(i).getName()), "20" + startTIme, "20" + stoptTIme)) {
                if ((channelNum==0)||((Integer.parseInt(vedioBeans.get(i).getName().substring(3, 4)) +1 ) == channelNum)) {
                    timeBean bean = new timeBean();
                    bean.setStartTime(vedioBeans.get(i).getName().substring(7, 19));
                    Log.d("VideoList", "substring:" + vedioBeans.get(i).getSize());
                    bean.setDuration(vedioBeans.get(i).getDuration()/1000);
                    bean.setEndTime(getVideoLongTime(vedioBeans.get(i).getName().substring(7, 19), bean.getDuration()));
                    bean.setPath(vedioBeans.get(i).getPath());
                    bean.setName(vedioBeans.get(i).getName());
                    bean.setSize(vedioBeans.get(i).getSize());
                    bean.setChannelNum(Integer.parseInt(vedioBeans.get(i).getName().substring(3, 4)) +1);
                    Log.d("VideoList", "getPath:" + vedioBeans.get(i).getPath());
                    if (timeBeanlist == null) timeBeanlist = new ArrayList<timeBean>();
                    if (vedioBeans.get(i).getSize() != 0 && bean.getDuration() != 0)
                        timeBeanlist.add(bean);
                }
            }
        }
        if (timeBeanlist != null) {
            manager.uploadVideoListLocal(flowNo, channelNum, false, 0, 0, timeBeanlist);
        }
        else
        {
            manager.uploadVideoListLocal(flowNo, channelNum, false, 0, 0, new ArrayList<timeBean>());
        }
    }

    /**
     * 开始实时视频
     */
    private void startLive(int channelNum) {
        if (liveClient == null) return;
        if (liveClient2 == null) return;
        if (channelNum == 1) {
            isStartVideoLive1 = true;
            isStartVideoLiveFistTime1 = true;
        } else {
            isStartVideoLive2 = true;
            isStartVideoLiveFistTime2 = true;
        }
    }

    private void startLocalLive(int channelNum) {
        if (localLiveClient == null) return;
        if (localLiveClient2 == null) return;
        if (channelNum == 1) {
            isStartLocalVideoLive1 = true;
            isStartLocalVideoLiveFistTime1 = true;
        } else {
            isStartLocalVideoLive2 = true;
            isStartLocalVideoLiveFistTime2 = true;
        }
    }

    private void start905Live(int channelNum) {
        if (liveClient905 == null) return;
        if (liveClient2905 == null) return;
        if (channelNum == 1) {
            isStart905VideoLive1 = true;
            isStart905VideoLiveFistTime1 = true;
        } else {
            isStart905VideoLive2 = true;
            isStart905VideoLiveFistTime2 = true;
        }
    }

    @Override
    public int OnStartPlaybackSuccess() {
        Log.d("Playback", "OnStartPlaybackSuccess");
        isPlayBackON = true;
        return 0;
    }

    @Override
    public int OnStartPlaybackFailed(int error) {
        Log.d("Playback", "OnStartPlaybackFailed:" + error);
        stopPlayBack();
        if (playBackClient != null) {
            playBackClient.release();
            playBackClient = null;
        }

        if (playBackLocalClient != null) {
            playBackLocalClient.release();
            playBackLocalClient = null;
        }
        isStartPlayBackVideoTimeFist = false;
        isStartLocalPlayBackVideoTimeFist = false;
        ppsSenOver = false;
        return 0;
    }


    @Override
    public void PlayBackLocalAudioVideoLive(String ip, int port, int channelNum, int dataType, String starttime, String stoptime) {
        playBackLocalClient = new LiveClient(ip, port);
        for (int i = 0; i < timeBeanlist.size(); i++) {
            if (analysisViideo == null) {
                Log.d(TAG, "videoList==null");
                return;
            }
            if (timeBeanlist.get(i).getStartTime().equalsIgnoreCase(starttime)
                    && timeBeanlist.get(i).getName().substring(3,4).equalsIgnoreCase(Integer.toString(channelNum-1))) {
                Log.d(TAG, "getVideoName " + timeBeanlist.get(i).getName());
                isStartLocalPlayBackVideoTimeFist = false;
                playBackStartTime = timeBeanlist.get(i).getStartTime();
                playBackEndTime = timeBeanlist.get(i).getEndTime();
                ppsSenOver = false;
                playBackFileName = timeBeanlist.get(i).getPath();
                if (isPlayBackON) {
                    MediaSdk.Instant().VideoPlayback_Stop(0);
                    isPlayBackON = false;
                }
                m_msgHandler.sendEmptyMessageDelayed(MSG_DELAY_START_PLAYBACK, 2000);
                break;
            }else if(playBackStartTime!=null){
                if(timeBeanlist.get(i).getPath().equalsIgnoreCase(playBackFileName)
                        && timeBeanlist.get(i).getName().substring(3,4).equalsIgnoreCase(Integer.toString(channelNum-1))){
                    if((DateFormat.checkDateV(starttime,getVideoLongTime(playBackStartTime,1),playBackEndTime))) {
                        playBackDurationTime = getPlayBackDurationTime(timeBeanlist.get(i).getStartTime(), starttime);
                        Log.d(TAG, "playBackDurationTime:" + playBackDurationTime);
                        m_msgHandler.sendEmptyMessageDelayed(MSG_DELAY_START_DURATION_PLAYBACK, 2000);
                        break;
                    }
                }
            }
        }
        isStartLocalVideoBackFistTime = true;
        playBackChannelNum = channelNum;
    }

    @Override
    public void PlayBackAudioVideoLive(String ip, int port, int channelNum, int dataType, String starttime, String stoptime) {
        playBackClient = new LiveClient(ip, port);
        for (int i = 0; i < timeBeanlist.size(); i++) {
            if (analysisViideo == null) {
                Log.d(TAG, "videoList==null");
                return;
            }
            if (timeBeanlist.get(i).getStartTime().equalsIgnoreCase(starttime)
                    && timeBeanlist.get(i).getName().substring(3,4).equalsIgnoreCase(Integer.toString(channelNum-1))) {
                Log.d(TAG, "getVideoName " + timeBeanlist.get(i).getName());
                isStartPlayBackVideoTimeFist = false;
                playBackStartTime = timeBeanlist.get(i).getStartTime();
                playBackEndTime = timeBeanlist.get(i).getEndTime();
                ppsSenOver = false;
                playBackFileName = timeBeanlist.get(i).getPath();
                if (isPlayBackON) {
                    MediaSdk.Instant().VideoPlayback_Stop(0);
                    isPlayBackON = false;
                }
                m_msgHandler.sendEmptyMessageDelayed(MSG_DELAY_START_PLAYBACK, 2000);
                break;
            }else if(playBackStartTime!=null){
                if(timeBeanlist.get(i).getPath().equalsIgnoreCase(playBackFileName)
                        && timeBeanlist.get(i).getName().substring(3,4).equalsIgnoreCase(Integer.toString(channelNum-1))){
                    if((DateFormat.checkDateV(starttime,getVideoLongTime(playBackStartTime,1),playBackEndTime))) {
                        playBackDurationTime = getPlayBackDurationTime(timeBeanlist.get(i).getStartTime(), starttime);
                        Log.d(TAG, "playBackDurationTime:" + playBackDurationTime);
                        m_msgHandler.sendEmptyMessageDelayed(MSG_DELAY_START_DURATION_PLAYBACK, 2000);
                        break;
                    }
                }
            }
        }
        isStartVideoBackFistTime = true;
        playBackChannelNum = channelNum;
    }

    private void stopPlayBack() {
        MediaSdk.Instant().VideoPlayback_Stop(0);
        isPlayBackON = false;
    }

    @Override
    public void PlayBackVideoAudioLiveControl(int channelNum, int control, int fastForwardAndBack, String dragPosition) {
        Log.d("PlayControl", "channelNum: " + channelNum + " control:" + control + " fastForwardAndBack:" + fastForwardAndBack + " dragPosition:" + dragPosition);
        playBackChannelNum = channelNum;
        if (control == 0) {
            Log.d(TAG, "开始回放");
        } else if (control == 1) {
            Log.d(TAG, "暂停回放");
        } else if (control == 2) {
            Log.d(TAG, "结束回放");
            stopPlayBack();
            if (playBackClient != null) {
                playBackClient.release();
                playBackClient = null;
            }
            if (playBackLocalClient != null) {
                playBackLocalClient.release();
                playBackLocalClient = null;
            }
            isStartPlayBackVideoTimeFist = false;
            isStartLocalPlayBackVideoTimeFist = false;
            ppsSenOver = false;
        } else if (control == 3) {
            Log.d("PlayControl", "快进回放");
        } else if (control == 4) {
            Log.d("PlayControl", "关键帧快退放");
        } else if (control == 5) {
            Log.d("PlayControl", "拖拽回放");
        } else if (control == 6) {
            Log.d("PlayControl", "关键帧回放");
        }
    }


    @Override
    public void UpLocalVideoFile(String user,String passwor,String ip, int port, int channelNum, int dataType, String startTime, String stopTime,String remotePath) {
        String name = "YJ"+"_"+(channelNum-1)+"_"+"20"+startTime+".mp4";
        Log.d(TAG,"UpVedioFile name:"+ name);
        Map itemMap=mInstanceDBHelper.queryItemMap("select * from user where name=?",new String[]{name});
        if(itemMap==null)return;
        String path=itemMap.get("path").toString();
        Log.d(TAG,"UpVedioFile:"+ path);
        FTPFileUpload ftpFileUpload = new FTPFileUpload(ip,port,user,passwor);
        ftpFileUpload.setRemotePath(remotePath);
        ftpFileUpload.uploadFile(new File(path));
    }

    @Override
    public void UpVedioFile(String user,String passwor,String ip, int port, int channelNum, int dataType, String startTime, String stopTime,String remotePath) {
        String name = "YJ"+"_"+(channelNum-1)+"_"+"20"+startTime+".mp4";
        Log.d(TAG,"UpVedioFile name:"+ name);
        Map itemMap=mInstanceDBHelper.queryItemMap("select * from user where name=?",new String[]{name});
        if(itemMap==null)return;
        String path=itemMap.get("path").toString();
        Log.d(TAG,"UpVedioFile:"+ path);
        FTPFileUpload ftpFileUpload = new FTPFileUpload(ip,port,user,passwor);
        ftpFileUpload.setRemotePath(remotePath);
        ftpFileUpload.uploadFile(new File(path));
    }

    @Override
    public void tcpAlive() {
        Log.d(TAG,"tcp jtt808 alive");
        //20秒没有收到服务器回应，重新初始化
        m_msgHandler.removeMessages(MSG_TCP_NO_ALIVE);
        m_msgHandler.sendEmptyMessageDelayed(MSG_TCP_NO_ALIVE,1000*20);
        Location808.getInstance().RestartUpdateGPS();
    }

    @Override
    public void tcpLocalAlive() {
        Log.d(TAG,"tcp local jtt808 alive");
        //20秒没有收到服务器回应，重新初始化
        m_msgHandler.removeMessages(MSG_LOCAL_TCP_NO_ALIVE);
        m_msgHandler.sendEmptyMessageDelayed(MSG_LOCAL_TCP_NO_ALIVE,1000*20);
    }

    @Override
    public void tcp905Alive() {
        Log.d(TAG,"tcp jt905 alive");
        //20秒没有收到服务器回应，重新初始化
        m_msgHandler.removeMessages(MSG_905_TCP_NO_ALIVE);
        m_msgHandler.sendEmptyMessageDelayed(MSG_905_TCP_NO_ALIVE,1000*20);
    }


    @Override
    public int onPlaybackVideoData(int nIndex, int type, byte[] data, int len, long nTimeStamp) {
        if (playBackClient != null) {
            if (type == 7 && isStartPlayBackVideoTimeFist == false) {
                sps = Arrays.copyOfRange(data, 0, len);
            } else if (type == 8 && isStartPlayBackVideoTimeFist == false) {
                if (sps != null) {
                    pps = Arrays.copyOfRange(data, 0, len);
                    mdata = concat(sps, pps);
                    // manager.videoLive(mdata, playBackChannelNum, playBackClient, mdata.length);
                    //m_msgHandler.sendEmptyMessageDelayed(MSG_SEND_PLAYBACK_SPS, 1000);
                    Log.d(TAG, "playBackSps");
                    isStartPlayBackVideoTimeFist = true;
                    ppsSenOver = true;
                }
            }
            if(isStartDurationPlayBack){
                MediaSdk.Instant().VideoPlayback_Seek(0, playBackDurationTime);
                isStartDurationPlayBack=false;
            }
            if (ppsSenOver) {
                byte NALU = NV21EncoderH264.findNALU(0, data)[0];
                if ((NALU & 0x1F) == 5) {
                    manager.videoLive(concat(mdata,data), playBackChannelNum, playBackClient, len+mdata.length, 5,nTimeStamp);
                }
                else
                    manager.videoLive(data, playBackChannelNum, playBackClient, len, 5,nTimeStamp);
            }
        }

        if (playBackLocalClient != null) {
            if (type == 7 && isStartLocalPlayBackVideoTimeFist == false) {
                sps = Arrays.copyOfRange(data, 0, len);
            } else if (type == 8 && isStartLocalPlayBackVideoTimeFist == false) {
                if (sps != null) {
                    pps = Arrays.copyOfRange(data, 0, len);
                    mdata = concat(sps, pps);
                    // manager.videoLive(mdata, playBackChannelNum, playBackClient, mdata.length);
                    //m_msgHandler.sendEmptyMessageDelayed(MSG_SEND_PLAYBACK_SPS, 1000);
                    Log.d(TAG, "playBackSps");
                    isStartLocalPlayBackVideoTimeFist = true;
                    ppsSenOver = true;
                }
            }
            if(isStartDurationPlayBack){
                MediaSdk.Instant().VideoPlayback_Seek(0, playBackDurationTime);
                isStartDurationPlayBack=false;
            }
            if (ppsSenOver) {
                byte NALU = NV21EncoderH264.findNALU(0, data)[0];
                if ((NALU & 0x1F) == 5) {
                    manager.videoLive(concat(mdata,data), playBackChannelNum, playBackLocalClient, len+mdata.length, 5,nTimeStamp);
                }
                else
                    manager.videoLive(data, playBackChannelNum, playBackLocalClient, len, 5,nTimeStamp);
            }
        }
        return 0;
    }

    @Override
    public int onPlaybackAudioData(int nIndex, byte[] data, int len, long nTimeStamp) {
        if(settingBean.getPlatform()==0) {
            manager.audioLive(data, playBackChannelNum, playBackClient, len, RecorActivity.this, nTimeStamp,0);
            if(playBackLocalClient != null)
                manager.audioLive(data, playBackChannelNum, playBackLocalClient, len, RecorActivity.this, nTimeStamp,0);
        }else if(settingBean.getPlatform()==1){
            manager.audioLive(data, playBackChannelNum, playBackClient, len, RecorActivity.this, nTimeStamp,1);
            if(playBackLocalClient != null)
                manager.audioLive(data, playBackChannelNum, playBackLocalClient, len, RecorActivity.this, nTimeStamp,1);
        }
        return 0;
    }
    @Override
    public void callBackLocation(double latitude, double longitude,float speed) {
        mlatitude = latitude;
        mlongitude=longitude;
        mSpeed=speed;
        acc=readLACC();
        Log.d("onLocation","callBackLocation");
        manager.uploadLocation(latitude, longitude,speed,alarm,acc);
        alarm=0;
        if(!isAllCameraConnected()){
            Log.d(TAG,"not all camera open");
            Intent intent = new Intent("com.carrecorder.media.error");
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            sendBroadcast(intent);
        }
    }

    private byte[][] unPackPkg(byte[] data, byte offset, int len) {
        byte[] NALUB = findNALU(offset, data);
        byte NALU = NALUB[0];
        offset = NALUB[1];
        if ((NALU & 0x1F) == 8) {
            byte[] sps = Arrays.copyOfRange(data, 0, offset - 4);
            byte[] pps = Arrays.copyOfRange(data, offset - 4, len);
            return new byte[][]{sps, pps};
        }
        return new byte[][]{data};
    }


    private static String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
        return dateFormat.format(date);
    }

    private String getVideoLongTime(String curtime, int addtime) {
        Calendar ca = Calendar.getInstance();
        Log.d("getVideoLongTime:", Integer.parseInt(curtime.substring(0, 2)) + " " +
                Integer.parseInt(curtime.substring(2, 4)) + " " +
                Integer.parseInt(curtime.substring(4, 6)) + " " +
                Integer.parseInt(curtime.substring(6, 8)) + " " +
                Integer.parseInt(curtime.substring(8, 10)) + " " +
                Integer.parseInt(curtime.substring(10, 12)) + "");

        ca.set(Integer.parseInt(curtime.substring(0, 2)), Integer.parseInt(curtime.substring(2, 4)) - 1, Integer.parseInt(curtime.substring(4, 6)), Integer.parseInt(curtime.substring(6, 8)),
                Integer.parseInt(curtime.substring(8, 10)), Integer.parseInt(curtime.substring(10, 12)));
        System.out.println("getVideoLongTime修改当前:" + formatDate(ca.getTime()));
        ca.set(Calendar.SECOND, ca.get(Calendar.SECOND) + addtime);
        System.out.println("getVideoLongTime当前修改:" + formatDate(ca.getTime()));
        return formatDate(ca.getTime());
    }

    public static <T> byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public boolean isSDCard1Exit() {
        List<String> list = getExtSDCardPathList();
        for (int i = 0; i < list.size(); i++) {
            Log.d("getSdPath()3:", list.get(i));
            if (list.get(i).equalsIgnoreCase("/storage/sdcard1")) {
                Log.d("getSdPath()3:", "SD卡1存在");
                sdcard1Exist = true;
                return true;
            } else if (list.get(i).equalsIgnoreCase("/storage/sdcard2")) {
                Log.d("getSdPath()3:", "SD卡2存在");
                sdcard2Exist = true;
            } else {
                sdcard1Exist = false;
                sdcard2Exist = false;
            }
        }
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
            Log.d("getSdPath():", sdDir.getAbsolutePath());

        } else {

        }
        return false;
    }

    /**
     * 获取外置SD卡路径以及TF卡的路径
     * <p>
     * 返回的数据：paths.get(0)肯定是外置SD卡的位置，因为它是primary external storage.
     *
     * @return 所有可用于存储的不同的卡的位置，用一个List来保存
     */
    public static List<String> getExtSDCardPathList() {
        List<String> paths = new ArrayList<String>();
        String extFileStatus = Environment.getExternalStorageState();
        File extFile = Environment.getExternalStorageDirectory();
        //首先判断一下外置SD卡的状态，处于挂载状态才能获取的到
        if (extFileStatus.equals(Environment.MEDIA_MOUNTED)
                && extFile.exists() && extFile.isDirectory()
                && extFile.canWrite()) {
            //外置SD卡的路径
            paths.add(extFile.getAbsolutePath());
            //isExist(extFile.getAbsolutePath() + "/CarCamera");
        }
        try {
            // obtain executed result of command line code of 'mount', to judge
            // whether tfCard exists by the result
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("mount");
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            int mountPathIndex = 1;
            while ((line = br.readLine()) != null) {
                // format of sdcard file system: vfat/fuse
                if ((!line.contains("fat") && !line.contains("fuse") && !line
                        .contains("storage"))
                        || line.contains("secure")
                        || line.contains("asec")
                        || line.contains("firmware")
                        || line.contains("shell")
                        || line.contains("obb")
                        || line.contains("legacy") || line.contains("data")) {
                    continue;
                }
                String[] parts = line.split(" ");
                int length = parts.length;
                if (mountPathIndex >= length) {
                    continue;
                }
                String mountPath = parts[mountPathIndex];
                if (!mountPath.contains("/") || mountPath.contains("data")
                        || mountPath.contains("Data")) {
                    continue;
                }
                File mountRoot = new File(mountPath);
                if (!mountRoot.exists() || !mountRoot.isDirectory()
                        || !mountRoot.canWrite()) {
                    continue;
                }
                boolean equalsToPrimarySD = mountPath.equals(extFile
                        .getAbsolutePath());
                if (equalsToPrimarySD) {
                    continue;
                }
                //扩展存储卡即TF卡或者SD卡路径
                if(mountPath.contains("sdcard"))
                    paths.add(mountPath);
                // isExist(mountPath + "/CarCamera");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String path : paths) {
            Log.d("path",path);
        }
        return paths;
    }

    private long getSDAvailableSize() {
        File path = new File("/storage/sdcard1");
        try {
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();
            return blockSize * availableBlocks / 1024 / 1024;
        }catch (IllegalArgumentException e)
        {
            return 0;
        }
        // return Formatter.formatFileSize(RecorActivity.this, blockSize * availableBlocks);
    }

    private boolean AvailableSizeToRecord() {
        if (getSDAvailableSize() > 300) {
            Log.d(TAG, "录像空间充足:" + getSDAvailableSize());
            return true;
        } else {
            Log.d(TAG, "录像空间不足:" + getSDAvailableSize() + "  删除文件");
            List list = listFileSortByModifyTime(RecordFileFactory.strDirector);
            for (int i = 0; i < list.size(); i++) {
                if (i == 0) removeFile(list.get(i).toString());
                if (i == 1) removeFile(list.get(i).toString());
            }
            if (getSDAvailableSize() > 300) {
                Log.d(TAG, "录像空间充足~:" + getSDAvailableSize());
                return true;
            } else {
                Log.d(TAG, "录像空间不足~:" + getSDAvailableSize());
                m_msgHandler.sendEmptyMessageDelayed(MSG_START_RECORD, MSG_START_RECORD_START_TIME);
            }
        }
        return false;
    }

    private void removeFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
            deleteDB(path);
        }
    }

    public static List<File> listFileSortByModifyTime(String path) {
        List<File> list = getFiles(path, new ArrayList<File>());
        if (list != null && list.size() > 0) {
            Collections.sort(list, new Comparator<File>() {
                public int compare(File file, File newFile) {
                    if (file.lastModified() < newFile.lastModified()) {
                        return -1;
                    } else if (file.lastModified() == newFile.lastModified()) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            });
        }
        return list;
    }


    /**
     * *
     * * 获取目录下所有文件
     * *
     * * @param realpath
     * * @param files
     * * @return
     */
    public static List<File> getFiles(String realpath, List<File> files) {
        File realFile = new File(realpath);
        if (realFile.isDirectory()) {
            File[] subfiles = realFile.listFiles();
            if(subfiles!=null) {
                for (File file : subfiles) {
                    if (file.isDirectory()) {
                        getFiles(file.getAbsolutePath(), files);
                    } else {
                        files.add(file);
                    }
                }
            }
        }
        return files;
    }

    private int getPlayBackDurationTime(String beginTime, String endTime) {
        if(beginTime==null||endTime==null)return 0;
        Log.d(TAG, "getPlayBackDurationTime:" + "  beginTime:" + beginTime + "  endTime:" + endTime);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmmss");
        /*计算时间差*/
        Date begin = null;
        Date end = null;
        try {
            begin = simpleDateFormat.parse(beginTime);
            end = simpleDateFormat.parse(endTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long diff = end.getTime() - begin.getTime();
        /*计算天数*/
        long days = diff / (1000 * 60 * 60 * 24);
        /*计算小时*/
        long hours = (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        /*计算分钟*/
        long minutes = (diff % (1000 * 60 * 60)) / (1000 * 60);
        /*计算秒*/
        long seconds = (diff % (1000 * 60)) / 1000;
//        Log.d(TAG,"getPlayBackDurationTime:"+days + "天" + hours + "小时" + minutes + "分" + seconds + "秒");
        String shijiancha = days + "-" + hours + "-" + minutes + "-" + seconds;
        Log.d(TAG, "getPlayBackDurationTime:" + shijiancha);
        return (int) (minutes * 60 + seconds) * 1000;
    }
    public static void closeLCD(int value) {
        String SYS_PATH = "/sys/class/leds/lcd-backlight/brightness";
        BufferedWriter bufWriter = null;
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(SYS_PATH);
            bufWriter = new BufferedWriter(fileWriter);
            bufWriter.write(String.valueOf(value));  // 写操作
            bufWriter.close();
            Log.d(TAG, "write success to value=" + value);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "can't write the " + SYS_PATH);
        } finally {
            try {
                if (null != bufWriter) {
                    bufWriter.close();
                }
                if (null != fileWriter) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return;
    }
    public static int readLCD() {
        String SYS_PATH = "/sys/class/leds/lcd-backlight/brightness";
        String value = "0";
        BufferedReader bufferedReader = null;
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(SYS_PATH);
            bufferedReader = new BufferedReader(fileReader);
            value = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != bufferedReader) {
                    bufferedReader.close();
                }
                if (null != fileReader) {
                    fileReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "read lcd value=" + value);
        return Integer.parseInt(value);
    }

    public static int readLACC() {
        String SYS_PATH = "/sys/class/switch/hall/state";
        String value = "0";
        BufferedReader bufferedReader = null;
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(SYS_PATH);
            bufferedReader = new BufferedReader(fileReader);
            value = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != bufferedReader) {
                    bufferedReader.close();
                }
                if (null != fileReader) {
                    fileReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Integer.parseInt(value);
    }
    public static int readLightdet() {
        String SYS_PATH = "/sys/class/switch/lightdet/state";
        String value = "0";
        BufferedReader bufferedReader = null;
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(SYS_PATH);
            bufferedReader = new BufferedReader(fileReader);
            value = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != bufferedReader) {
                    bufferedReader.close();
                }
                if (null != fileReader) {
                    fileReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Integer.parseInt(value);
    }
    private void insertDB(String name,String path,long durtion){
        try {
            mInstanceDBHelper.insert("user",new String[]{"name","path","duration","size"},
                    new Object[]{name,path+".mp4",(int)durtion,0});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void updateDB(String name,String path,long durtion,long size){
        try {
            mInstanceDBHelper.update("user",new String[]{"name","path","duration","size"},
                    new Object[]{name,path+".mp4",(int)durtion,(int)size},
                    new String[]{"name"},new String[]{name});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void deleteDB(String path){
        Log.d(TAG,"deleteDB:"+path);
        boolean success = mInstanceDBHelper.delete("user",
                new String[]{"path"},new String[]{path});
        Log.d(TAG,"deleteDB:"+success);
    }
    private List<vedioBean> queryDataList(){
        List<Map> list=mInstanceDBHelper.queryListMap("select * from user",null);
        if (vedioBeans == null) vedioBeans = new ArrayList<vedioBean>();
        for(int i=0;i<list.size();i++){
            vedioBean bean = new vedioBean();
            bean.setName(list.get(i).get("name").toString());
            bean.setPath(list.get(i).get("path").toString());
            bean.setSize((int)list.get(i).get("size"));
            bean.setDuration((int)list.get(i).get("duration"));
            vedioBeans.add(bean);
        }
        return vedioBeans;
    }
    public static byte[] convertG711ToPcm(byte[] g711Buffer, int length, byte[] pcmBuffer)
    {
        if (pcmBuffer == null)
        {
            pcmBuffer = new byte[length*2];
        }
        for (int i=0; i<length; i++)
        {
            byte alaw = g711Buffer[i];
            alaw ^= 0xD5;

            int sign     =  alaw & 0x80;
            int exponent = (alaw & 0x70) >> 4;
            // 这个移位多此一举？结果应该一直是8
            int value    = (alaw & 0x0F) >> 4 + 8;
            if (exponent != 0)
            {
                value += 0x0100;
            }
            if (exponent > 1)
            {
                value <<= (exponent - 1);
            }
            value = (char)((sign == 0 ? value : -value) & 0xFFFF);
            pcmBuffer[i*2+0] = (byte) (value      & 0xFF);
            pcmBuffer[i*2+1] = (byte) (value >> 8 & 0xFF);
        }
        return pcmBuffer;
    }
    final String[] item = new String[]{"前摄+后摄", "前摄+USB", "后摄+USB"};
    private void showSecletDialog(){
        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this)

                .setTitle("请选择")//默认为0表示选中第一个项目

                .setSingleChoiceItems(item, 0, new DialogInterface.OnClickListener() {

                    @Override

                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG,"onClick2:"+which);

                        if(which==0){
                            isUSBCameraOpen=false;
                            SetSharedPreferences("isUSBCameraOpen","0");
                            isCamera0Open=true;
                            SetSharedPreferences("isCamera0Open","1");
                            isCamera1Open=true;
                            SetSharedPreferences("isCamera1Open","1");
                        }else if(which==1){
                            isUSBCameraOpen=true;
                            SetSharedPreferences("isUSBCameraOpen","1");
                            isCamera0Open=true;
                            SetSharedPreferences("isCamera0Open","1");
                            isCamera1Open=false;
                            SetSharedPreferences("isCamera1Open","0");
                        }else {
                            isUSBCameraOpen=true;
                            SetSharedPreferences("isUSBCameraOpen","1");
                            isCamera0Open=false;
                            SetSharedPreferences("isCamera0Open","0");
                            isCamera1Open=true;
                            SetSharedPreferences("isCamera1Open","1");
                        }
                    }

                })

                .setPositiveButton("确认", new DialogInterface.OnClickListener() {

                    @Override

                    public void onClick(DialogInterface dialog, int which) {

                        Log.d(TAG,"onClick:"+which);
                        Intent i = new Intent("android.intent.action.REBOOT");
                        // 立即重启：1
                        i.putExtra("nowait", 1);
                        // 重启次数：1
                        i.putExtra("interval", 1);
                        // 不出现弹窗：0
                        i.putExtra("window", 0);
                        startActivity(i);
                    }

                })

                .setNegativeButton("取消", null)

                .create();

        alertDialog.show();
    }
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            Log.d("isNetwork","FALSE");
        } else {
            NetworkInfo[] info = cm.getAllNetworkInfo();

            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        Log.d("isNetwork","TRUE");
                        return true;
                    }
                }
            }else {
                Log.d("isNetwork","info ==null");
            }
        }
        return false;
    }
    @Override
    public void getCmd(int cmd, int DataIndex, long time) {
        Log.i("MAPGOO_IPC", "cmd:" + cmd+"  DataIndex:"+DataIndex+" time:"+time);
        if(cmd == 3){
            IPC_CONNECTED=true;
        }else if (cmd == 4){
            IPC_CONNECTED=false;
        } else if (cmd == 1)
        {
            IPC_CONNECTED=true;
        }
    }

    @Override
    public void connStatusNotif(int status) {
        Log.i("connStatusNotif", "status:" + status);
        if (status == MAPGOO_IPC_CONNECTED){// 获取所有数据源
            IPC_INDEX =  MapgooIPC.getDataIndexByUniqueID("com.example.administrator.mocam_0");
            Log.d("MAPGOO_IPC","MAPGOO_IPC UniqueID :"+IPC_INDEX);
            MapgooIPC.sendMsg(MapgooIPC.MAPGOO_IPC_MSG_CAMERA_STATUS,1,0,0,"");

        }else{

        }
    }

    PowerManager.WakeLock wakeLock;
    PowerManager pm ;

    private void acquireWakeLock() {
        if (null == wakeLock) {
            pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE, getClass()
                    .getCanonicalName());
            if (null != wakeLock) {
                Log.i(TAG, "call acquireWakeLock");
                wakeLock.acquire();
            }
        }
    }
    private void releaseWakeLock() {
        if (null != wakeLock && wakeLock.isHeld()) {
            Log.i(TAG, "call releaseWakeLock");
            wakeLock.release();
            wakeLock = null;
        }
    }

    @Override
    public <T> void textdelivery(T a,String ftpText){
        if(a instanceof JTT808Client) {
            Log.e(TAG,"jtt808 获取文本信息:" + ftpText);
            //JTT808Client.getInstance().writeAndFlush(getTerminalAtribute());
        }else if(a instanceof JTT808ClientLocal){
            Log.e(TAG,"jtt808Local 获取文本信息:" + ftpText);
            //JTT808ClientLocal.getInstance().writeAndFlush(getTerminalAtribute());
        }else if(a instanceof JTT905Client){
            Log.e(TAG,"jtt905 获取文本信息:" + ftpText);
            //#FTPCT:47.100.10.103,2199,admin,20160606,01_5656_5656_7788_M08-V22062511-V22062511-V22062511-TO22070925.16317.sw;
            //JTT905Client.getInstance().writeAndFlush(getTerminalAtribute905());
            if(ftpText.contains("FTPCT:") && ftpText.endsWith(";")){
                String sub = ftpText.split(":")[1];
                String[] details = sub.substring(0,sub.length()-1).split(",");
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            JTT808Manager.getInstance().downloadApk(details[0], Integer.parseInt(details[1]), details[2], details[3], details[4]);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }.start();
            }

        }
    }



    @Override
    public <T> void queryAtrribute(T a) {
        if(a instanceof JTT808Client) {
            Log.e(TAG,"jtt808 获取终端属性");
            JTT808Client.getInstance().writeAndFlush(getTerminalAtribute());
        }else if(a instanceof JTT808ClientLocal){
            Log.e(TAG,"jtt808Local 获取终端属性");
            JTT808ClientLocal.getInstance().writeAndFlush(getTerminalAtribute());
        }else if(a instanceof JTT905Client){
            Log.e(TAG,"jtt905 获取终端属性");
            JTT905Client.getInstance().writeAndFlush(getTerminalAtribute905());
        }
    }

    private JTT808Bean getTerminalAtribute(){
        String versionCode = Constants.SW;
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String iccid = tm.getSimSerialNumber();
        Log.e(TAG,"iccid:" + iccid);
        if(iccid == null){
            iccid = "00000000000000000000";
        }
        try {
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }catch (Exception e){}
        return manager.queryAtrribute(SharePreUtil.getString(this, "MANUFACTURER_ID", Constants.MANUFACTURER_ID),
                SharePreUtil.getString(this, "TERMINAL_MODEL", Constants.TERMINAL_MODEL),
                SharePreUtil.getString(this, "TERMINAL_ID", Constants.TERMINAL_ID),
                iccid,Constants.HW,versionCode
        );
    }

    private JTT905Bean getTerminalAtribute905(){
        String versionCode = Constants.SW;
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String iccid = tm.getSimSerialNumber();
        Log.e(TAG,"iccid:" + iccid);
        if(iccid == null){
            iccid = "00000000000000000000";
        }
        try {
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }catch (Exception e){}
        return manager.queryAtrribute905(SharePreUtil.getString(this, "MANUFACTURER_ID", Constants.MANUFACTURER_ID),
                SharePreUtil.getString(this, "TERMINAL_MODEL", Constants.TERMINAL_MODEL),
                SharePreUtil.getString(this, "TERMINAL_ID", Constants.TERMINAL_ID),
                iccid,Constants.HW,versionCode
        );
    }

    public void terminalControl(int flag){
        Intent intent = new Intent();
        switch (flag){
            case 3: //关机
                intent.setAction(Intent.ACTION_SHUTDOWN);
                sendBroadcast(intent);
                break;
            case 4: //复位
                intent.setAction(Intent.ACTION_REBOOT);
                intent.putExtra("nowait", 1);
                intent.putExtra("interval", 1);
                intent.putExtra("window", 0);
                sendBroadcast(intent);
                break;
            case 5: //恢复出厂设置
                break;
            case 6: //关闭数据通信
                break;
            case 7: //关闭所有无线通信
                break;
            case 104: //重启
                intent.setAction(Intent.ACTION_REBOOT);
                intent.putExtra("nowait", 1);
                intent.putExtra("interval", 1);
                intent.putExtra("window", 0);
                sendBroadcast(intent);
                break;
            default:
                break;

        }
    }

}
