package media.sdk.MediaSurfaceSdk;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.view.View;

import car.recorder.carrecorder;
import media.sdk.Common.Playback.LocalPlayback;
import media.sdk.Common.Record.LocalRecord;
import media.sdk.Common.audio.AudioCapture.AudioCatpure;
import media.sdk.MediaSdk;
import media.sdk.MediaSurfaceSdk.Display.ImageDrawerManager;
import media.sdk.MediaSurfaceSdk.Opengles.KWindowSurfaceDrawer;
import media.sdk.MediaSurfaceSdk.VideoCapture.Camera21SurfaceSource;
import media.sdk.MediaSurfaceSdk.VideoCapture.CameraSurfaceSource;
import media.sdk.MediaSurfaceSdk.VideoEncoder.MediaSurfaceEncoder;
import media.sdk.MediaSurfaceSdk.VideoEncoder.VideoSurfaceEncoderInterface;

public class MediaSurfaceSdk extends MediaSdk implements MediaSdk.VideoEncoderObserver, MediaSdk.AudioEncodedObserver
{
    public int Initial()
    {
        for(int i = 0; i < m_drawer.length; i++)
        {
            m_drawer[i] = new KWindowSurfaceDrawer();
            m_drawer[i].Initial();
        }
        return 0;
    }

    public int Clean()
    {
        for(int i = 0; i < m_drawer.length; i++)
        {
            m_drawer[i].Clean();
            m_drawer[i] = null;
        }
        return 0;
    }

    public boolean OnAudioEncodedCheck(int nAudioType, int param, int nSampleRate, int nChannels)
    {
        return m_observerAudioInput.OnAudioEncodedCheck(nAudioType, param, nSampleRate, nChannels);
    }

    public int OnAudioEncoded(byte[] data, int offset, int len, int nAudioType, int param, int nSampleRate, int nChannels)
    {
        if(nAudioType == carrecorder.AUDIOTYPE_AAC)
        {
            long nTimeStamp = System.nanoTime() / 1000 / 1000;
            m_localRecord.PushAudio(0, data, offset, len, nTimeStamp);
            m_localRecord.PushAudio(1, data, offset, len, nTimeStamp);
        }
        m_observerAudioInput.OnAudioEncoded(data, offset, len, nAudioType, param, nSampleRate, nChannels);
        return 0;
    }

    public int AudioInput_Start(AudioEncodedObserver observer)
    {
        m_observerAudioInput = observer;
        AudioCatpure.Instant().Start(this, carrecorder.m_nAudioCaptureFlag, 1);
        return 0;
    }

    public int AudioInput_Stop()
    {
        AudioCatpure.Instant().Stop();
        return 0;
    }

    //摄像机相关
    public int Camera_Start(Activity activity, Handler msgHandler)
    {

        SurfaceTexture[] surfaceTextures = new SurfaceTexture[2];
        surfaceTextures[0] = m_drawer[0].GetMainSurfaceTexture();
        surfaceTextures[1] = m_drawer[1].GetMainSurfaceTexture();
        m_cameraSource.createCamera(activity, surfaceTextures);
        m_cameraSource.startCamera();
        return 0;
    }

    public int Camera_Stop()
    {
        m_cameraSource.stopCamera();
        m_cameraSource.destroyCamera();
        return 0;
    }
    public int setCameraMode(int state){
        m_cameraSource.setCameraMode(state);
        return 0;
    }
    public int Preview_Create(Activity activity, PreviewObserver observer)
    {
        ImageDrawerManager.Instant().CreateAllDrawer(activity, observer);
        return 0;
    }

    public int Preview_Delete()
    {
        ImageDrawerManager.Instant().DeleteAllDrawer();
        return 0;
    }

    public int Preview_Start()
    {
        ImageDrawerManager.Instant().Start();
        return 0;
    }

    public int Preview_Stop()
    {
        ImageDrawerManager.Instant().Stop();
        return 0;
    }

    public View Preview_GetView(int nIndex)
    {
        return ImageDrawerManager.Instant().m_imageDrawer[nIndex];
    }

    public int onEncodeData(int nIndex, byte[] data, int len, long nTimeStamp)
    {
        m_localRecord.PushVideo(nIndex, data, 0, len, nTimeStamp);
        return m_observerEncoder.onEncodeData(nIndex, data, len, nTimeStamp);
    }

    //视频编码
    public int VideoEncoder_Start(VideoEncoderObserver observer)
    {
        m_observerEncoder = observer;
        for(int i = 0; i < m_encoder.length; i++)
        {
            m_encoder[i] = new MediaSurfaceEncoder();
            m_encoder[i].createEncoder(i, this);
        }
        return 0;
    }

    public int VideoEncoder_Stop()
    {
        for(int i = 0; i < m_encoder.length; i++)
        {
            m_encoder[i].destroyEncoder();
            m_encoder[i] = null;
        }
        return 0;
    }

    public boolean VideoEncoder_IsSupported(int nEncodeType)
    {
        return m_encoder[0].isEncodeSupported(nEncodeType);
    }

    public int VideoEncoder_Restart(int nIndex)
    {
        return m_encoder[nIndex].recreateEncoder();
    }

    public int VideoEncoder_GetFrameRate(int nIndex)
    {
        return m_encoder[nIndex].getFrameRate();
    }

    public int VideoEncoder_GetBitRate(int nIndex)
    {
        return m_encoder[nIndex].getBitrate();
    }

    public int VideoPlayback_Create(Activity activity, LocalPlaybackObserver observer)
    {
        return m_localPlayback.Create(activity, observer);
    }

    public int VideoPlayback_Delete()
    {
        return m_localPlayback.Delete();
    }

    public int VideoPlayback_Start(int nChannel, String strFileName)
    {
        return m_localPlayback.StartPlayback(nChannel, strFileName);
    }

    public int VideoPlayback_Stop(int nChannel)
    {
        return m_localPlayback.StopPlayback(nChannel);
    }
    public int VideoPlayback_Seek(int nChannel,int nTimeOffset)
    {
        return m_localPlayback.Seek(nChannel,nTimeOffset);
    }
    public int VideoRecord_Create(Activity activity, LocalRecordObserver observer)
    {
        return m_localRecord.Create(activity, observer);
    }

    public int VideoRecord_Delete()
    {
        return m_localRecord.Delete();
    }

    public int VideoRecord_Start(int nChannel)
    {
        int width = m_encoder[nChannel].getWidth();
        int height = m_encoder[nChannel].getHeight();
        int nEncodeType = m_encoder[nChannel].getEncodeType();
        return m_localRecord.StartRecord(nChannel, nEncodeType, width, height);
    }

    public int VideoRecord_Stop(int nChannel)
    {
        return m_localRecord.StopRecord(nChannel);
    }

    public int VideoRecord_GetTime(int nChannel)
    {
        return m_localRecord.GetRecordTime(nChannel);
    }

    public boolean VideoRecord_IsStarted(int nChannel)
    {
        return m_localRecord.isStarted(nChannel);
    }

    public KWindowSurfaceDrawer WindowSurfaceDraw_Get(int nIndex)
    {
        return m_drawer[nIndex];
    }

    public VideoEncoderObserver m_observerEncoder = null;
    public VideoSurfaceEncoderInterface m_encoder[] = new MediaSurfaceEncoder[2];
    public KWindowSurfaceDrawer[] m_drawer = new KWindowSurfaceDrawer[2];
    public CameraSurfaceSource m_cameraSource = new Camera21SurfaceSource();
    public MediaSdk.AudioEncodedObserver m_observerAudioInput = null;
    public LocalRecord m_localRecord = new LocalRecord();
    public LocalPlayback m_localPlayback = new LocalPlayback();

}
