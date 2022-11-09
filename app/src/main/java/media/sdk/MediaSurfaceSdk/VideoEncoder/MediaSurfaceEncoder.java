package media.sdk.MediaSurfaceSdk.VideoEncoder;


import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

import car.recorder.carrecorder;
import media.sdk.Common.FrameCount;
import media.sdk.MediaSdk;
import media.sdk.MediaSurfaceSdk.Opengles.KWindowSurface;

public class MediaSurfaceEncoder implements VideoSurfaceEncoderInterface, Runnable
{
    private MediaCodecInfo getCodecInfo(String mimeType)
    {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++)
        {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder())
            {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (String type : types)
            {
                if (mimeType.equalsIgnoreCase(type))
                {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    public String GetVideoMime(int nEncodeType)
    {
        if(nEncodeType == 0)
        {
            return "video/avc";
        }
        return "video/hevc";
    }

    public boolean isEncodeSupported(int nEncodeType)
    {
        MediaCodecInfo mediaCodecInfo = getCodecInfo(GetVideoMime(nEncodeType));
        if(mediaCodecInfo != null)
        {
            return true;
        }
        return false;
    }

    public int HandleOutput()
    {
        if(m_mediaCodec == null)
        {
            return 0;
        }
        //初始化输出缓存区
        ByteBuffer[] outputBuffers = m_mediaCodec.getOutputBuffers();
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        //从解码器中获取数据索引值
        int outputBufferIndex = m_mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
        //判断是否获取数据
        while (outputBufferIndex >= 0)
        {
            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
            if(bufferInfo.size < m_buffer.length)
            {
                outputBuffer.get(m_buffer, 0, bufferInfo.size);
                m_frameCount.Add(1);
                m_bitRateCount.Add(bufferInfo.size);
                m_observer.onEncodeData(m_nIndex, m_buffer, bufferInfo.size, bufferInfo.presentationTimeUs / 1000);
                m_mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = m_mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            }
            else
            {
                break;
            }
        }
        return 0;
    }

    public int HandleStart()
    {
        if(m_bStart)
        {
            return 0;
        }
        stopEncoder();
        startEncoder();
        m_bStart = true;
        return 0;
    }

    public int Heartbeat()
    {
        int nActionCount = 0;
        nActionCount += HandleStart();
        nActionCount += HandleOutput();
        return nActionCount;
    }

    public void run()
    {
        while(!m_isEnd)
        {
            int nActionCount = Heartbeat();
            if(nActionCount <= 0)
            {
                try
                {
                    Thread.sleep(10);
                }
                catch (InterruptedException e)
                {

                }
            }

        }
        m_isRunning = false;
    }

    public int getBitrate()
    {
        return m_bitRateCount.GetFrameRate() * 8 / 1024;
    }

    public int getFrameRate()
    {
        return m_frameCount.GetFrameRate();
    }

    public int getWidth()
    {
        return m_width;
    }

    public int getHeight()
    {
        return m_height;
    }

    public int getEncodeType()
    {
        return m_nVideoEncodeType;
    }

    public int recreateEncoder()
    {
        m_bStart = false;
        return 0;
    }

    public int startEncoder()
    {
        Log.d("MediaCodec","startEncode:" + m_nIndex);
        m_width = carrecorder.m_nWidth[m_nIndex];
        m_height = carrecorder.m_nHeight[m_nIndex];
        m_nVideoEncodeType = carrecorder.m_nVideoEncodeType[m_nIndex];
        m_bitRate = carrecorder.m_nBitRate[m_nIndex];
        m_frameRate = carrecorder.m_nFrameRate[m_nIndex];
        String strVideoMime = GetVideoMime(m_nVideoEncodeType);
        try
        {
            MediaCodecInfo mediaCodecInfo = getCodecInfo(strVideoMime);
            m_mediaCodec = MediaCodec.createByCodecName(mediaCodecInfo.getName());
            MediaFormat mediaFormat;
            mediaFormat = MediaFormat.createVideoFormat(strVideoMime, m_width, m_height);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, m_bitRate * 1024);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, m_frameRate);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);
            m_mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            Surface surface = m_mediaCodec.createInputSurface();
            m_surface = new KWindowSurface(surface, true);
            m_surface.m_surfaceText = MediaSdk.Instant().WindowSurfaceDraw_Get(m_nIndex).m_surfaceText[1];
            MediaSdk.Instant().WindowSurfaceDraw_Get(m_nIndex).addRecorder(m_surface, true);
            m_mediaCodec.start();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return 0;
    }

    public int stopEncoder()
    {
        if(m_mediaCodec == null)
        {
            return 0;
        }
        if(m_surface != null)
        {
            MediaSdk.Instant().WindowSurfaceDraw_Get(m_nIndex).removeRecorder(m_surface);
            m_surface.release();
            m_surface = null;
        }
        m_mediaCodec.stop();
        m_mediaCodec.release();
        m_mediaCodec = null;
        Log.d("MediaCodec","stopEncoder:" + m_nIndex);
        return 0;
    }

    public int createEncoder(int nIndex, MediaSdk.VideoEncoderObserver observer)
    {
        m_frameCount = new FrameCount("Encode-" + nIndex);
        m_bitRateCount = new FrameCount("Encode-" + nIndex);
        m_bStart = false;
        m_nIndex = nIndex;
        m_observer = observer;
        m_isRunning = true;
        m_isEnd = false;
        m_thread = new Thread(this);
        m_thread.start();
        return 0;
    }

    public int destroyEncoder()
    {
        if(m_isEnd)
        {
            return 0;
        }
        m_isEnd = true;
        try
        {
            m_thread.join();
            while(m_isRunning)
            {
                Thread.sleep(10);
            }
        }
        catch (InterruptedException e)
        {

        }
        return 0;
    }

    KWindowSurface m_surface = null;
    boolean m_bStart = false;
    FrameCount m_frameCount = null;
    FrameCount m_bitRateCount = null;
    int m_width = 0;
    int m_height = 0;
    int m_nVideoEncodeType = 0;
    int m_bitRate = 0;
    int m_frameRate = 0;
    private boolean m_isRunning = false;
    private boolean m_isEnd = true;
    private Thread m_thread = null;
    byte[] m_buffer = new byte[4 * 1024 * 1024];
    private int m_nIndex = 0;
    private MediaSdk.VideoEncoderObserver m_observer = null;
    private MediaCodec m_mediaCodec = null;
}
