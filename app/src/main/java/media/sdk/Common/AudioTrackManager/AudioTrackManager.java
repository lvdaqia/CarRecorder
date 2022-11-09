package media.sdk.Common.AudioTrackManager;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioTrackManager {
    public static final String TAG = "AudioTrackManager";
    private AudioTrack audioTrack;
    private DataInputStream dis;
    private Thread recordThread;
    private boolean isStart = false;
    private static AudioTrackManager mInstance;
    private int bufferSize;
    private int mBufferSizeInBytes;

    private static final int mSampleRateInHz = 8000;
    private static final int mChannelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO; //单声道
    private static final int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private AudioTrack maudioTrack;



    private final static int SIGN_BIT = 0x80;
    private final static int QUANT_MASK = 0xf;
    private final static int SEG_SHIFT = 4;
    private final static int SEG_MASK = 0x70;
    static short[] seg_end = {0xFF, 0x1FF, 0x3FF, 0x7FF, 0xFFF, 0x1FFF, 0x3FFF, 0x7FFF};
    public AudioTrackManager() {
        bufferSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize * 2, AudioTrack.MODE_STREAM,0);
    }

    /**
     * 获取单例引用
     *
     * @return
     */
    public static AudioTrackManager getInstance() {
        if (mInstance == null) {
            synchronized (AudioTrackManager.class) {
                if (mInstance == null) {
                    mInstance = new AudioTrackManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 销毁线程方法
     */
    private void destroyThread() {
        try {
            isStart = false;
            if (null != recordThread && Thread.State.RUNNABLE == recordThread.getState()) {
                try {
                    Thread.sleep(500);
                    recordThread.interrupt();
                } catch (Exception e) {
                    recordThread = null;
                }
            }
            recordThread = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            recordThread = null;
        }
    }

    /**
     * 启动播放线程
     */
    private void startThread() {
        destroyThread();
        isStart = true;
        if (recordThread == null) {
            recordThread = new Thread(recordRunnable);
            recordThread.start();
        }
    }

    /**
     * 播放线程
     */
    Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                byte[] tempBuffer = new byte[bufferSize];
                int readCount = 0;
                while (dis.available() > 0) {
                    readCount= dis.read(tempBuffer);
                    if (readCount == AudioTrack.ERROR_INVALID_OPERATION || readCount == AudioTrack.ERROR_BAD_VALUE) {
                        continue;
                    }
                    if (readCount != 0 && readCount != -1) {
                        audioTrack.play();
                        audioTrack.write(tempBuffer, 0, readCount);
                    }
                }
                stopPlay();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    /**
     * 播放文件
     *
     * @param path
     * @throws Exception
     */
    private void setPath(String path) throws Exception {
        File file = new File(path);
        dis = new DataInputStream(new FileInputStream(file));
    }

    /**
     * 启动播放
     *
     * @param path
     */
    public void startPlay(String path) {
        try {
            setPath(path);
            startThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止播放
     */
    public void stopPlay() {
      try {
            destroyThread();
            if (audioTrack != null) {
                if (audioTrack.getState() == AudioRecord.STATE_INITIALIZED) {
                    audioTrack.stop();
                }
                if (audioTrack != null) {
                    audioTrack.release();
              }
            }
            if (dis != null) {
                dis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mInstance=null;
    }
    public void playPCM_STREAM(String path) throws FileNotFoundException {
        if (maudioTrack != null){
            maudioTrack.stop();
            maudioTrack.release();
            maudioTrack = null;
        }
//先估算最小缓冲区大小
        mBufferSizeInBytes = AudioRecord.getMinBufferSize(mSampleRateInHz,mChannelConfig,mAudioFormat);
//创建AudioTrack
        maudioTrack = new AudioTrack(
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                new AudioFormat.Builder()
                        .setSampleRate(mSampleRateInHz)
                        .setEncoding(mAudioFormat)
                        .setChannelMask(mChannelConfig)
                        .build(),
                mBufferSizeInBytes,
                AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE
        );
        maudioTrack.play();
        File file = new File(path); //原始pcm文件
        final FileInputStream fileInputStream;
        if (file.exists()){
            fileInputStream = new FileInputStream(file);
            new Thread(){
                @Override
                public void run() {
                    try {
                        byte[] buffer = new byte[mBufferSizeInBytes];
                        while(fileInputStream.available() > 0){
                            int readCount = fileInputStream.read(buffer); //一次次的读取
                            //检测错误就跳过
                            if (readCount == AudioTrack.ERROR_INVALID_OPERATION|| readCount == AudioTrack.ERROR_BAD_VALUE){
                                continue;
                            }
                            if (readCount != -1 && readCount != 0){
//可以在这个位置用play()
                                //输出音频数据
                                maudioTrack.write(buffer,0,readCount); //一次次的write输出播放
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.i("TAG","STREAM模式播放完成");
                }
            }.start();
        }
    }
    static short search(short val, short[] table, short size)
    {

        for (short i = 0; i < size; i++)
        {
            if (val <= table[i])
            {
                return i;
            }
        }
        return size;
    }

    static short alaw2linear(byte a_val)
    {
        short t;
        short seg;

        a_val ^= 0x55;

        t = (short) ((a_val & QUANT_MASK) << 4);
        seg = (short) ((a_val & SEG_MASK) >> SEG_SHIFT);
        switch (seg)
        {
            case 0:
                t += 8;
                break;
            case 1:
                t += 0x108;
                break;
            default:
                t += 0x108;
                t <<= seg - 1;
        }
        return (a_val & SIGN_BIT) != 0 ? t : (short) -t;
    }


    static byte linear2alaw(short pcm_val)
    {
        short mask;
        short seg;
        char aval;
        if (pcm_val >= 0)
        {
            mask = 0xD5;   //* sign (7th) bit = 1 二进制的11010101
        } else
        {
            mask = 0x55; //* sign bit = 0  二进制的01010101
            pcm_val = (short) (-pcm_val - 1);//负数转换为正数计算
            if (pcm_val < 0)
            {
                pcm_val = 32767;
            }
        }

        /* Convert the scaled magnitude to segment number. */
        seg = search(pcm_val, seg_end, (short) 8); //查找采样值对应哪一段折线

        /* Combine the sign, segment, and quantization bits. */

        if (seg >= 8)       /* out of range, return maximum value. */
            return (byte) (0x7F ^ mask);
        else
        {
            //以下按照表格第一二列进行处理，低4位是数据，5~7位是指数，最高位是符号
            aval = (char) (seg << SEG_SHIFT);
            if (seg < 2)
                aval |= (pcm_val >> 4) & QUANT_MASK;
            else
                aval |= (pcm_val >> (seg + 3)) & QUANT_MASK;
            return (byte) (aval ^ mask);
        }
    }



    public static void G711aEncoder(short[] pcm, byte[] code, int size)
    {
        for (int i = 0; i < size; i++)
        {
            code[i] = linear2alaw(pcm[i]);
        }
    }
    public static byte [] G711aDecoder(short[] pcm, byte[] code, int size)
    {
        for (int i = 0; i < size; i++)
        {
            pcm[i] = alaw2linear(code[i]);
        }
        return shortArr2byteArr(pcm,pcm.length);
    }

    public static byte[] shortArr2byteArr(short[] shortArr, int shortArrLen){
        byte[] byteArr = new byte[shortArrLen * 2];
        ByteBuffer.wrap(byteArr).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shortArr);
        return byteArr;
    }
    public byte[] getBytes(short s, boolean bBigEnding) {
        byte[] buf = new byte[2];
        if (bBigEnding)
            for (int i = buf.length - 1; i >= 0; i--) {
                buf[i] = (byte) (s & 0x00ff);
                s >>= 8;
            }
        else
            for (int i = 0; i < buf.length; i++) {
                buf[i] = (byte) (s & 0x00ff);
                s >>= 8;
            }
        return buf;
    }

}