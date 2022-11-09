package media.sdk.MediaSurfaceSdk.VideoCapture;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.HandlerThread;

import android.util.Log;
import android.view.Surface;

import androidx.core.app.ActivityCompat;

import com.example.dell.carrecorder.RecorActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class Camera21SurfaceSource implements CameraSurfaceSource
{
    private CameraManager m_cameraManager = null;
    private CameraDevice m_cameraDevice = null;
    private CameraDevice m_cameraDevice1 = null; //chengrq
    private Handler m_mainHandler = null;
    private Handler m_cameraHandler = null;
    private HandlerThread m_cameraHandlerThread = null;
    private CaptureRequest.Builder[] m_requestBuilder = new CaptureRequest.Builder[2];
    private Activity m_activity = null;
    private Surface m_surface[] = null;
    private CameraCaptureSession m_session1;
    int Preview(CameraCaptureSession session, Surface surface)
    {
        try
        {
            CaptureRequest.Builder requestBuilder = session.getDevice().createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            requestBuilder.addTarget(surface);
            session.setRepeatingRequest(requestBuilder.build(), null, m_cameraHandler);
            m_requestBuilder[1] = requestBuilder;

        }
        catch (CameraAccessException e)
        {
            Log.d("Camera", "createCaptureSession threw CameraAccessException.");
        }
        return 0;
    }

    private final CameraCaptureSession.StateCallback m_captureSessionStateCallback = new CameraCaptureSession.StateCallback()
    {
        @Override
        public void onConfigured(CameraCaptureSession session)
        {
            Preview(session, m_surface[0]);
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session)
        {

        }
    };

    private final CameraCaptureSession.StateCallback m_captureSessionStateCallback1 = new CameraCaptureSession.StateCallback()
    {
        @Override
        public void onConfigured(CameraCaptureSession session)
        {
            Preview(session, m_surface[1]);
            m_session1=session;

        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session)
        {

        }
    };

    private CameraDevice.StateCallback stateCallback1 = new CameraDevice.StateCallback()
    {
        @Override
        public void onOpened(CameraDevice camera)
        {
            List<Surface> surfaceList = new ArrayList<>();
            surfaceList.add(m_surface[1]);
            try
            {
                m_cameraDevice1 = camera;
                camera.createCaptureSession(surfaceList, m_captureSessionStateCallback1, m_cameraHandler);
            }
            catch (CameraAccessException e)
            {
                Log.e("Camera", "createCaptureSession threw CameraAccessException.", e);
            }
            Log.d("Camera", "onOpened");
        }

        @Override
        public void onDisconnected(CameraDevice camera)
        {
            Log.d("Camera", "onDisconnected");
        }

        @Override
        public void onError(CameraDevice camera, int error)
        {
            Log.d("Camera", "onError");
        }
    };

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback()
    {
        @Override
        public void onOpened(CameraDevice camera)
        {
            List<Surface> surfaceList = new ArrayList<>();
            surfaceList.add(m_surface[0]);
            try
            {
                m_cameraDevice = camera;
                camera.createCaptureSession(surfaceList, m_captureSessionStateCallback, m_cameraHandler);
            }
            catch (CameraAccessException e)
            {
                Log.e("Camera", "createCaptureSession threw CameraAccessException.", e);
            }
            Log.d("Camera", "onOpened");
        }

        @Override
        public void onDisconnected(CameraDevice camera)
        {
            Log.d("Camera", "onDisconnected");
        }

        @Override
        public void onError(CameraDevice camera, int error)
        {
            Log.d("Camera", "onError");
        }
    };

    public int startCamera()
    {
        if (ActivityCompat.checkSelfPermission(m_activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            return -1;
        }

        try
        {
            if(RecorActivity.isCamera0Open==true&&RecorActivity.isCamera1Open==true&&RecorActivity.isUSBCameraOpen==false){
                Log.d("luozhihao","0");
                m_cameraManager.openCamera("" + CameraCharacteristics.LENS_FACING_FRONT, stateCallback, m_mainHandler);
                m_cameraManager.openCamera("" + CameraCharacteristics.LENS_FACING_BACK, stateCallback1, m_mainHandler);
            }else if(RecorActivity.isCamera0Open==false&&RecorActivity.isCamera1Open==true&&RecorActivity.isUSBCameraOpen==true){
                Log.d("luozhihao","1");
                m_cameraManager.openCamera("" + CameraCharacteristics.LENS_FACING_FRONT, stateCallback, m_mainHandler);
                m_cameraManager.openCamera("" + CameraCharacteristics.LENS_FACING_EXTERNAL, stateCallback1, m_mainHandler);
            }else if(RecorActivity.isCamera0Open==true&&RecorActivity.isCamera1Open==false&&RecorActivity.isUSBCameraOpen==true){
                Log.d("luozhihao","2");
                m_cameraManager.openCamera("" + CameraCharacteristics.LENS_FACING_BACK, stateCallback1, m_mainHandler);
                m_cameraManager.openCamera("" + CameraCharacteristics.LENS_FACING_EXTERNAL, stateCallback, m_mainHandler);
            }
        }
        catch (CameraAccessException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    public int stopCamera()
    {
        if(m_cameraDevice != null)
        {
            m_cameraDevice.close();
            m_cameraDevice = null;
        }
        //chengrq start
        if(m_cameraDevice1 != null)
        {
            m_cameraDevice1.close();
            m_cameraDevice1 = null;
        }
        //end
        return 0;
    }

    @Override
    public int setCameraMode(int state) {
        Log.e("setCameraMode","state:"+state);
        if(state==0){
            m_requestBuilder[1].set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_OFF);
            if(m_session1!=null) {
                try {
                    m_session1.setRepeatingRequest(m_requestBuilder[1].build(),null,m_cameraHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        }else {
            m_requestBuilder[1].set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_MONO);
            if(m_session1!=null) {
                try {
                    m_session1.setRepeatingRequest(m_requestBuilder[1].build(),null,m_cameraHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

        }
        return 0;
    }

    public int createCamera(Activity activity, SurfaceTexture[] surfaceTextures)
    {
        m_activity = activity;
        m_surface = new Surface[surfaceTextures.length];
        for(int i = 0; i < m_surface.length; i++)
        {
            m_surface[i] = new Surface(surfaceTextures[i]);
        }
        m_cameraHandlerThread = new HandlerThread("mCameraHandlerThread");
        m_cameraHandlerThread.start();
        m_cameraHandler = new Handler(m_cameraHandlerThread.getLooper());
        m_mainHandler = new Handler(m_activity.getMainLooper());
        m_cameraManager = (CameraManager)m_activity.getSystemService(Context.CAMERA_SERVICE);
        return 0;
    }

    public int destroyCamera()
    {
        if(m_cameraHandler == null)
        {
            return 0;
        }
        m_cameraHandlerThread.quit();
        m_cameraHandlerThread = null;
        m_cameraHandler = null;
        m_surface = null;
        return 0;
    }
}
