package com.example.dell.carrecorder;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;

import com.example.administrator.mocam.R;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class UsbCamrea extends Activity {

    private TextureView mTextureView;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;
    private Surface mPreviewSurface;
    //private String mCameraId;
    //private Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usb_camrea);
        //预览用的surface
        mTextureView = (TextureView) this.findViewById(R.id.mtextureView);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {

            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture arg0, int arg1, int arg2) {
                // TODO 自动生成的方法存根
                mPreviewSurface = new Surface(arg0);
                CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                try {
                    manager.openCamera("2", new CameraDevice.StateCallback() {

                        @Override
                        public void onOpened(CameraDevice arg0) {
                            // TODO 自动生成的方法存根s
                            mCameraDevice = arg0;
                            try {
                                mCameraDevice.createCaptureSession(Arrays.asList(mPreviewSurface), new CameraCaptureSession.StateCallback() {

                                    @Override
                                    public void onConfigured(CameraCaptureSession arg0) {
                                        // TODO 自动生成的方法存根
                                        mCameraCaptureSession = arg0;
                                        try {
                                            CaptureRequest.Builder builder;
                                            builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                            builder.addTarget(mPreviewSurface);
                                            mCameraCaptureSession.setRepeatingRequest(builder.build(), null, null);
                                        } catch (CameraAccessException e1) {
                                            // TODO 自动生成的 catch 块
                                            e1.printStackTrace();
                                        }


                                    }

                                    @Override
                                    public void onConfigureFailed(CameraCaptureSession arg0) {
                                        // TODO 自动生成的方法存根

                                    }
                                }, null);
                            } catch (CameraAccessException e) {
                                // TODO 自动生成的 catch 块
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onError(CameraDevice arg0, int arg1) {
                            // TODO 自动生成的方法存根

                        }

                        @Override
                        public void onDisconnected(CameraDevice arg0) {
                            // TODO 自动生成的方法存根

                        }
                    }, null);
                } catch (CameraAccessException e) {
                    // TODO 自动生成的 catch 块
                    e.printStackTrace();
                }
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture arg0) {
                // TODO 自动生成的方法存根
                return false;
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture arg0, int arg1, int arg2) {
                // TODO 自动生成的方法存根

            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture arg0) {
                // TODO 自动生成的方法存根

            }

        });

    }

}