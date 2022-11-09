package media.sdk.MediaSurfaceSdk.VideoCapture;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.Surface;

public class Camera1SurfaceSource implements CameraSurfaceSource, Camera.AutoFocusCallback
{
    private Activity m_activity = null;
    private Camera[] m_camera = new Camera[2];     // Camera对象，相机预览

    public int openCamera(int nIndex, int angle, SurfaceTexture surfaceTexture, int nCameraID)
    {
        try
        {
            m_camera[nIndex] = Camera.open(nCameraID);
            if(m_camera[nIndex] != null)
            {
                Camera.Parameters parameters = m_camera[nIndex].getParameters();
                Camera.CameraInfo camInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(nCameraID, camInfo);
                parameters.setPreviewFrameRate(16);
                int cameraRotationOffset = camInfo.orientation;
                int  displayRotation = (cameraRotationOffset - angle + 360) % 360;
                m_camera[nIndex].setDisplayOrientation(displayRotation);
                m_camera[nIndex].setPreviewTexture(surfaceTexture);
                m_camera[nIndex].setParameters(parameters);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public int createCamera(Activity activity, SurfaceTexture[] surfaceTexture)
    {
        m_activity = activity;
        openCamera(0, 0, surfaceTexture[0], Camera.CameraInfo.CAMERA_FACING_BACK);
        openCamera(1, 180, surfaceTexture[1], Camera.CameraInfo.CAMERA_FACING_FRONT);
        return 0;
    }

    public void onAutoFocus(boolean success, Camera camera)
    {
        return;
    }

    public int destroyCamera()
    {
        if (m_camera == null)
        {
            return -1;
        }

        for(int i = 0; i < m_camera.length; i++)
        {
            if(m_camera[i] == null)
            {
                continue;
            }
            m_camera[i].release();
        }
        return 0;
    }

    public int startCamera()
    {
        if (m_camera == null)
        {
            return -1;
        }
        for(int i = 0; i < m_camera.length; i++)
        {
            if(m_camera[i] == null)
            {
                continue;
            }
            m_camera[i].startPreview();
        }
        return 0;
    }

    public int stopCamera()
    {
        if (m_camera == null)
        {
            return -1;
        }
        for(int i = 0; i < m_camera.length; i++)
        {
            if(m_camera[i] == null)
            {
                continue;
            }
            m_camera[i].stopPreview();
        }
        return 0;
    }

    @Override
    public int setCameraMode(int state) {
        return 0;
    }
}
