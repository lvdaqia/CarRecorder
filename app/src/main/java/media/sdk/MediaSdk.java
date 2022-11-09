package media.sdk;

import android.app.Activity;
import android.os.Handler;
import android.view.View;

import media.sdk.MediaSurfaceSdk.Opengles.KWindowSurfaceDrawer;
import media.sdk.MediaSurfaceSdk.MediaSurfaceSdk;

public abstract class MediaSdk
{
    public interface LocalPlaybackObserver
    {
        public abstract int OnStartPlaybackSuccess();
        public abstract int OnStartPlaybackFailed(int error);
        public abstract int onPlaybackVideoData(int nIndex, int type, byte[] data, int len, long nTimeStamp);
        public abstract int onPlaybackAudioData(int nIndex, byte[] data, int len, long nTimeStamp);
    }

    public interface LocalRecordObserver
    {
        public abstract int OnStartRecordSuccess(String fileName);
        public abstract int OnStartRecordFailed(int error);
    }

    public interface LocalAudioRecordObserver
    {
        public abstract int OnStartAudioRecordSuccess();
        public abstract int OnStartAudioRecordFailed(int error);
    }

    public interface VideoEncoderObserver
    {
        public abstract int onEncodeData(int nIndex, byte[] data, int len, long nTimeStamp);
    }

    public interface AudioEncodedObserver
    {
        public abstract boolean OnAudioEncodedCheck(int nAudioType, int param, int nSampleRate, int nChannels);
        public abstract int OnAudioEncoded(byte[] data, int offset, int len, int nAudioType, int param, int nSampleRate, int nChannels);
    }

    public interface AudioInputObserver
    {
        public abstract int OnAudioInput(byte[] data, int offset, int len, int nSampleRate, int nChannels);
    }

    public interface PreviewObserver
    {
        abstract int OnTouch();
    }

    public abstract int Initial();
    public abstract int Clean();

    //音频输入
    public abstract int AudioInput_Start(AudioEncodedObserver observer);
    public abstract int AudioInput_Stop();

    //摄像机相关
    public abstract int Camera_Start(Activity activity, Handler msgHandler);
    public abstract int Camera_Stop();
    public abstract int setCameraMode(int state);

    //预览相关
    public abstract int Preview_Create(Activity activity, PreviewObserver observer);
    public abstract int Preview_Delete();
    public abstract int Preview_Start();
    public abstract int Preview_Stop();
    public abstract View Preview_GetView(int nIndex);

    //视频编码
    public abstract int VideoEncoder_Start(VideoEncoderObserver observer);
    public abstract int VideoEncoder_Stop();
    public abstract boolean VideoEncoder_IsSupported(int nEncodeType);
    public abstract int VideoEncoder_Restart(int nIndex);
    public abstract int VideoEncoder_GetFrameRate(int nIndex);
    public abstract int VideoEncoder_GetBitRate(int nIndex);

    //音视频回放
    public abstract int VideoPlayback_Create(Activity activity, LocalPlaybackObserver observer);
    public abstract int VideoPlayback_Delete();
    public abstract int VideoPlayback_Start(int nChannel, String strFileName);
    public abstract int VideoPlayback_Stop(int nChannel);
    public abstract int VideoPlayback_Seek(int nChannel,int nTimeOffset);
    //音视频录像
    public abstract int VideoRecord_Create(Activity activity, LocalRecordObserver observer);
    public abstract int VideoRecord_Delete();
    public abstract int VideoRecord_Start(int nChannel);
    public abstract int VideoRecord_Stop(int nChannel);
    public abstract int VideoRecord_GetTime(int nChannel);
    public abstract boolean VideoRecord_IsStarted(int nChannel);

    public abstract KWindowSurfaceDrawer WindowSurfaceDraw_Get(int nIndex);

    static public MediaSdk Instant()
    {
        return m_mediaSdk;
    }
    static public MediaSdk m_mediaSdk = new MediaSurfaceSdk();
}
