package media.sdk.MediaSurfaceSdk.VideoEncoder;


import media.sdk.MediaSdk;

public interface VideoSurfaceEncoderInterface
{
    abstract public int createEncoder(int nIndex, MediaSdk.VideoEncoderObserver observer);
    abstract public int destroyEncoder();
    abstract public int recreateEncoder();
    abstract public int getBitrate();
    abstract public int getFrameRate();
    abstract public int getWidth();
    abstract public int getHeight();
    abstract public int getEncodeType();
    abstract public boolean isEncodeSupported(int nEncodeType);

}
