package media.sdk.MediaSurfaceSdk.VideoCapture;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.view.Surface;


public interface CameraSurfaceSource
{
    abstract public int createCamera(Activity activity, SurfaceTexture[] surfaceTextures);
    abstract public int destroyCamera();
    abstract public int startCamera();
    abstract public int stopCamera();
    abstract public int setCameraMode(int state);
}
