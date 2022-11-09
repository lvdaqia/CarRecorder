package com.example.dell.carrecorder.decompose.mp4;

import android.os.Environment;
import android.util.Log;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import static com.azhon.jtt808.video.NV21EncoderH264.findNALU;

/**
 * 描述：用来将本地h264数据分割成一帧一帧的数据
 * 作者：chezi008 on 2017/6/29 16:50
 * 邮箱：chezi008@163.com
 */

public class H264ReadRunable implements Runnable {
    private static final int READ_BUFFER_SIZE = 1024 * 5;
    private static final int BUFFER_SIZE = 1024 * 1024;

    private String TAG = getClass().getSimpleName();
    private H264ReadListener h264ReadListener;
    private DataInputStream mInputStream;

    public void setH264ReadListener(H264ReadListener h264ReadListener) {
        this.h264ReadListener = h264ReadListener;
    }

    private byte[] buffer;

    @Override
    public void run() {
        try {
            mInputStream = new DataInputStream(new FileInputStream(
                    Environment.getExternalStorageDirectory().getAbsoluteFile()
                            .getPath() + "/demo/test.h264"));
            buffer = new byte[BUFFER_SIZE];

            int readLength;
            int naluIndex = 0;
            int bufferLength = 0;

            while ((readLength = mInputStream.read(buffer, bufferLength , READ_BUFFER_SIZE)) > 0) {

                bufferLength += readLength;
//                byte[] NALUB = findNALU(0, buffer);
////              byte NALU = NALUB[0];
//                byte offset = NALUB[1];
//                if ((buffer[4] & 0x1F) == 7) {
//                    byte[][] sps1 = unPackPkg(buffer, offset, buffer.length);
//                    for (byte[] aByte : sps1) {
//                        h264ReadListener.onFrameData(aByte, aByte.length);
//                        Log.d("m_msgHandler", "MSG_SEND_SPS_CAN1");
//                    }
//                }
                    for (int i = 5; i < bufferLength - 4; i++) {
                        if (buffer[i] == 0x00 &&
                                buffer[i + 1] == 0x00 &&
                                buffer[i + 2] == 0x00 &&
                                buffer[i + 3] == 0x01) {
                            naluIndex = i;
                            Log.d(TAG, "run: naluIndex:" + naluIndex);
                            byte[] naluBuffer = new byte[naluIndex];
                            System.arraycopy(buffer, 0, naluBuffer, 0, naluIndex);
                            h264ReadListener.onFrameData(naluBuffer, naluBuffer.length);
                            bufferLength -= naluIndex;
                            System.arraycopy(buffer, naluIndex, buffer, 0, bufferLength);
                            i = 5;
                            Thread.sleep(40);
                        }
                    }
                }
            h264ReadListener.onStopRead();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
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
    public interface H264ReadListener {
        void onFrameData(byte[] datas,int len);

        void onStopRead();
    }
}