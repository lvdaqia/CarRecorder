package media.sdk.MediaSurfaceSdk.Opengles;


import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Message;
import android.view.Surface;

import java.util.WeakHashMap;

import car.recorder.carrecorder;
import media.sdk.MediaSurfaceSdk.Opengles.grafika.EglCore;
import media.sdk.MediaSurfaceSdk.Opengles.grafika.FullFrameRect;
import media.sdk.MediaSurfaceSdk.Opengles.grafika.OffscreenSurface;
import media.sdk.MediaSurfaceSdk.Opengles.grafika.Texture2dProgram;
import media.sdk.MediaSurfaceSdk.Opengles.grafika.Transformation;

public class KWindowSurfaceDrawer implements SurfaceTexture.OnFrameAvailableListener
{
	private void tryScale(int viewWidth, int viewHeight, int previewWidth, int previewHeight)
	{
		int nScaleType;
		Transformation.Size previewSize;

		previewSize = new Transformation.Size(previewHeight, previewWidth);
		nScaleType = Transformation.SCALE_TYPE_FIT_XY;

		mTransformation.setScale(previewSize, new Transformation.Size(viewWidth, viewHeight), nScaleType);
		mFullFrameBlit.setTransformation(mTransformation);
	}

	public int Initial()
	{
		mTransformation.setRotation(0);

		mEglCore = EglManager.getEglCore();
		new OffscreenSurface(mEglCore, carrecorder.m_width, carrecorder.m_height).makeCurrent();

		MyTexture2dProgram t2p = new MyTexture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT);
		mFullFrameBlit = new FullFrameRect(t2p);

		mTextureId = mFullFrameBlit.createTextureObject();
		mCameraTexture = new SurfaceTexture(mTextureId);

		mCameraTexture.setDefaultBufferSize(carrecorder.m_width, carrecorder.m_height);
		mCameraTexture.setOnFrameAvailableListener(this, moHandler);

		mt2p = new MyTexture2dProgram(Texture2dProgram.ProgramType.TEXTURE_2D);
		int textTexture = mt2p.createTextureObject();
		m_bmpFactory = new CharBitmapFactoryCanvas();

		float nWidth = 0.0f;
		float nHeight = 0.0f;


		if(carrecorder.m_height >= 1920)
		{
			nWidth = 32.0f;
		}
		else if(carrecorder.m_height >= 1280)
		{
			nWidth = 24.0f;
		}
		else if(carrecorder.m_height >= 640)
		{
			nWidth = 16.0f;
		}
		nHeight = nWidth * 2.4f;
		m_bmpFactory.setVertexWidth(nWidth * 2.0f / carrecorder.m_height);
		m_bmpFactory.setVertexHeight(nHeight * 2.0f / carrecorder.m_width);
		m_bmpFactory.Create(textTexture);

		for(int i = 0; i < m_surfaceText.length; i++)
		{
			m_surfaceText[i] = new SurfaceText();
			m_surfaceText[i].Create(textTexture, mt2p, m_bmpFactory);
		}

		return 0;
	}

	public int Clean()
	{
		for(int i = 0; i < m_surfaceText.length; i++)
		{
			m_surfaceText[i].Delete();
			m_surfaceText[i] = null;
		}

		if (mt2p != null)
		{
			mt2p.release();
			mt2p = null;
		}

		if (m_bmpFactory != null)
		{
			m_bmpFactory.Delete();
			m_bmpFactory = null;
		}

		if (mFullFrameBlit != null)
		{
			mFullFrameBlit.release(true);
			mFullFrameBlit = null;
		}

		if(mCameraTexture != null)
		{
			mCameraTexture.setOnFrameAvailableListener(null);
			mCameraTexture.release();
			mCameraTexture = null;
		}

		mEglCore.makeNothingCurrent();
		return 0;
	}

	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture)
	{
		if(mCameraTexture == null)
		{
			return;
		}
		mCameraTexture.updateTexImage();
		mCameraTexture.getTransformMatrix(mTmpMatrix);
		synchronized (kwindowSurfaces)
		{
			for (KWindowSurface ws : kwindowSurfaces.keySet()) {
				if (ws == null) {
					continue;
				}
				ws.makeCurrent();
				int viewWidth = ws.getWidth();
				int viewHeight = ws.getHeight();
				GLES20.glViewport(0, 0, viewWidth, viewHeight);
				tryScale(viewWidth, viewHeight, carrecorder.m_width, carrecorder.m_height);
				mFullFrameBlit.drawFrame(mTextureId, mTmpMatrix);
				if (ws.watermark) {
					ws.Draw(mTmpMatrix, mFullFrameBlit.getXScale(), mFullFrameBlit.getYScale());
				}
				ws.swapBuffers();
			}
		}
	}

	public WeakHashMap<KWindowSurface, Boolean> kwindowSurfaces = new WeakHashMap<>();

	public void addRecorder(KWindowSurface surface, boolean preview)
	{
		if (surface == null)
		{
			return;
		}
		synchronized (kwindowSurfaces)
		{
			Boolean checkFlag = false;
			checkFlag = kwindowSurfaces.put(surface, preview);
			if (checkFlag == null || checkFlag != preview)
			{
				moHandler.sendEmptyMessage(OpenglHandler.CHECK);
			}
		}
	}

	public void removeRecorder(KWindowSurface surface)
	{
		if (surface == null)
		{
			return;
		}
		synchronized (kwindowSurfaces)
		{
			Boolean checkFlag = kwindowSurfaces.remove(surface);
			// recordSurfaces.remove(surface);
			if (checkFlag != null)
			{
				moHandler.sendEmptyMessage(OpenglHandler.CHECK);
			}
		}
	}


	class OpenglHandler extends Handler
	{
		static final int CHECK = 0;
		static final int INIT_THREAD = 1;
		static final int INIT_CAMERA = 2;
		static final int DO = 3;
		static final int RELEASE_CAMERA = 4;
		static final int RELEASE_THREAD = 5;
		static final int TAKE_PHOTO = 6;
		static final int UPDATE_PARAMETERS = 7;
		static final int TAKE_PHOTO_FRONT = 8;
		static final int RELEASE_AND_CHECK = 9;

		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case DO:
				{


				}
				break;
			}
		}
	};

	public SurfaceTexture GetMainSurfaceTexture()
	{
		return mCameraTexture;
	}

	public EglCore mEglCore = null;
	private FullFrameRect mFullFrameBlit;
	private final float[] mTmpMatrix = new float[16];
	private int mTextureId;
	private MyTexture2dProgram mt2p;
	public CharBitmapFactory m_bmpFactory;
	private SurfaceTexture mCameraTexture;
	private Transformation mTransformation = new Transformation();
	OpenglHandler moHandler = new OpenglHandler();
	public SurfaceText m_surfaceText[] = new SurfaceText[2];
}
