package media.sdk.MediaSurfaceSdk.VideoCapture;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;


public class Camera22SurfaceSource implements CameraSurfaceSource
{
    private CameraManager m_cameraManager = null;
    private CameraDevice m_cameraDevice = null;
    private Handler m_mainHandler = null;
    private Handler m_cameraHandler = null;
    private HandlerThread m_cameraHandlerThread = null;
    private CaptureRequest.Builder m_requestBuilder = null;
    private Activity m_activity = null;
    private Surface m_surface[] = null;
    private DualCamera m_dualCamera = null;

    private final CameraCaptureSession.StateCallback m_captureSessionStateCallback = new CameraCaptureSession.StateCallback()
    {
        @Override
        public void onConfigured(CameraCaptureSession session)
        {
            try
            {
                session.setRepeatingRequest(m_requestBuilder.build(), null, m_cameraHandler);
            }
            catch (CameraAccessException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session)
        {

        }
    };



    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback()
    {
        @Override
        public void onOpened(CameraDevice camera)
        {
            try
            {
                m_cameraDevice = camera;
                List<OutputConfiguration> configurations = new ArrayList<>();
                m_requestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

                OutputConfiguration outputConfiguration = new OutputConfiguration(m_surface[0]);
                outputConfiguration.setPhysicalCameraId(m_dualCamera.getPhysicsCameraId1());
                configurations.add(outputConfiguration);
                m_requestBuilder.addTarget(Objects.requireNonNull(outputConfiguration.getSurface()));

                OutputConfiguration outputConfiguration2 = new OutputConfiguration(m_surface[1]);
                outputConfiguration2.setPhysicalCameraId(m_dualCamera.getPhysicsCameraId2());
                configurations.add(outputConfiguration2);
                m_requestBuilder.addTarget(Objects.requireNonNull(outputConfiguration2.getSurface()));

                SessionConfiguration sessionConfiguration = new SessionConfiguration(
                        SessionConfiguration.SESSION_REGULAR,
                        configurations,
                        AsyncTask.SERIAL_EXECUTOR,
                        m_captureSessionStateCallback);
                camera.createCaptureSession(sessionConfiguration);
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
            m_cameraManager.openCamera(m_dualCamera.getLogicCameraId(), stateCallback, m_mainHandler);
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
        return 0;
    }

    @Override
    public int setCameraMode(int state) {
        return 0;
    }

    public static DualCamera getDualCamera(Context context)
    {
        DualCamera dualCamera = new DualCamera();
        //获取管理类
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        assert manager != null;
        try
        {
            //获取所有逻辑ID
            String[] cameraIdList = manager.getCameraIdList();

            for (String id : cameraIdList)
            {
                CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(id);
                Set<String> physicalCameraIds = cameraCharacteristics.getPhysicalCameraIds();
                String strText = "DualCamera(" + id + "):";
                if(physicalCameraIds.size() >= 1)
                {
                    Object[] objects = physicalCameraIds.toArray();
                    for(int j = 0; j < objects.length; j++)
                    {
                        strText += "" + objects[j] + ",";
                    }
                }
                Log.d("DualCamera", strText);
            }

            //获取逻辑摄像头下拥有多个物理摄像头的类 作为双镜类
            for (String id : cameraIdList)
            {
                try
                {
                    CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(id);
                    Set<String> physicalCameraIds = cameraCharacteristics.getPhysicalCameraIds();
                    if (physicalCameraIds.size() >= 2)
                    {
                        dualCamera.setLogicCameraId(id);
                        Object[] objects = physicalCameraIds.toArray();
                        //获取前两个物理摄像头作为双镜头
                        dualCamera.setPhysicsCameraId1(objects[0].toString());
                        dualCamera.setPhysicsCameraId2(objects[1].toString());
                        return dualCamera;
                    }
                }
                catch (CameraAccessException e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (CameraAccessException e)
        {
            e.printStackTrace();
        }
        return null;
    }


    public int createCamera(Activity activity, SurfaceTexture[] surfaceTextures)
    {
        m_dualCamera = getDualCamera(activity);
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
