package media.sdk.MediaSurfaceSdk.Display;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

import media.sdk.MediaSdk;
import media.sdk.MediaSurfaceSdk.Opengles.KWindowSurface;
import media.sdk.MediaSurfaceSdk.Opengles.KWindowSurfaceDrawer;

public class ImageDrawer extends SurfaceView implements SurfaceHolder.Callback,
												OnTouchListener,
												GestureDetector.OnGestureListener,
												GestureDetector.OnDoubleTapListener
{

	public ImageDrawer(Context context, MediaSdk.PreviewObserver observer, KWindowSurfaceDrawer kWindowSurface)
	{
		super(context);
		m_context = context;
		System.gc();
		m_kWindowSurface = kWindowSurface;
		m_observer = observer;
		m_GestureDetector = new GestureDetector(this);
		setOnTouchListener(this);
		setLongClickable(true);
		m_SensorManager = (SensorManager)context.getSystemService(context.SENSOR_SERVICE);
        m_Gyroscope = m_SensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        m_SensorManager.registerListener(mySensorListener, 		//??????
										m_Gyroscope, 		//??????????
										m_SensorManager.SENSOR_DELAY_GAME);	//?????????????????
		SurfaceHolder holder = getHolder();
		holder.addCallback(this); //设置Surface生命周期回调
	}

	public int Start()
	{

		return 0;
	}
	
	public int Stop()
	{

		return 0;
	}

	public boolean onDrag(MotionEvent event)
	{

		return true;
	}
	
	public boolean onTouch(View view, MotionEvent event)
	{
		
		if (event.getAction() == MotionEvent.ACTION_DOWN) 
		{
			m_xDown = event.getX(0);
			m_yDown = event.getY(0);
			m_observer.OnTouch();
		}
		
		if (event.getAction() == MotionEvent.ACTION_UP)
		{
			if (m_isScaleMode)
			{
				m_isScaleMode = false;
				return false;
			}
		}
		
		if (event.getAction() == MotionEvent.ACTION_MOVE) 
		{
			
			if (event.getPointerCount() == 1) 
			{
				onDrag(event);
				//��ֹ��ͬʱ�뿪��Ļִ��onScroll�¼�
				if (m_isScaleMode)
				{
					return false;
				}
			}
			
			if (event.getPointerCount() == 2)
			{
				m_isScaleMode = true;
				return false; // ���Ų���ʱ��ִ������
			}
		}
        
        
		return m_GestureDetector.onTouchEvent(event);
	}
	
	public boolean onDown(MotionEvent e)
	{
		return true;
	}
	
	
	@Override
	public boolean onDoubleTap(MotionEvent e)
	{
		return false;
	}
	
	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) 
	{
		return false;
	}
	
	@Override
	public boolean onDoubleTapEvent(MotionEvent e)
	{
		return false;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
	{
		if(Math.abs(velocityX) > Math.abs(velocityY))
		{
			return false;
		}

		return false;
	}

	public void onLongPress(MotionEvent e) 
	{
		
	}


	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
	{
		
		return true;
	}


	public void onShowPress(MotionEvent e)
	{
		
	}

	public boolean onSingleTapUp(MotionEvent e)
	{
		
		return false;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		Surface surface = holder.getSurface();
		m_previewSurface = new KWindowSurface(surface, true);
		m_previewSurface.m_surfaceText = m_kWindowSurface.m_surfaceText[0];
		m_kWindowSurface.addRecorder(m_previewSurface, true);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
							   int height)
	{

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder)
	{
		m_kWindowSurface.removeRecorder(m_previewSurface);
		m_previewSurface.release();
		m_previewSurface = null;
	}

	private SensorEventListener mySensorListener = new SensorEventListener()
	{
		//����ʵ����SensorEventListener�ӿڵĴ�����������
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy)
		{
			
		}
		
		@Override
		public void onSensorChanged(SensorEvent event)
		{
			//�ֻ�����б�Ƕ�
			float anglex = 0, angley = 0, anglez = 0;
			
			//��ȡ�����᷽���ϵļ��ٶ�ֵ
			float []values = event.values;
			
			//���ݼ��ٶ�������G-Sensor����ת�Ƕ�
		    if (m_timestamp != 0)
		    {
		        // event.timesamp��ʾ��ǰ��ʱ�䣬��λ�����루1�����֮һ���룩
		        final float dT = (event.timestamp - m_timestamp) * NS2S;
		        m_angle[0] += event.values[0] * dT;
		        m_angle[1] += event.values[1] * dT;
		        m_angle[2] += event.values[2] * dT;
		        
				anglex = (float) Math.toDegrees(m_angle[0]);
				angley = (float) Math.toDegrees(m_angle[1]);
				anglez = (float) Math.toDegrees(m_angle[2]);
		    }
		    
		    m_timestamp = event.timestamp;
				
		    //�����ֻ��Զ���תѡ��������G-Sensor����ת�Ƕ�
		    WindowManager wm = (WindowManager) getContext()
	                .getSystemService(Context.WINDOW_SERVICE);
		    int uiRot = wm.getDefaultDisplay().getRotation();
		    if (1 == uiRot)
		    {
		    	float tmp = anglex;
		    	anglex = -angley;
		    	angley = tmp;
		    }
		    else if (2 == uiRot)
		    {
		    	anglex = -anglex;
		    	angley = -angley;
		    }
		    else if (3 == uiRot)
		    {
		    	float tmp = anglex;
		    	anglex = angley;
		    	angley = tmp;
		    }

		    //Log.d("OPENGL", "uiRot = " + uiRot + " x��"+anglex + "  y: " + angley + "  z: " + anglez);
		}
	};

	public Context m_context = null;
	public KWindowSurface m_previewSurface = null;
	KWindowSurfaceDrawer m_kWindowSurface = null;

	//�����϶�����
	private float m_xDown = 0.0f;
	private float m_yDown = 0.0f;
	private boolean m_isScaleMode = false;
	private float m_timestamp;
	private float m_angle[] = new float[3];
	private static final float NS2S = 1.0f / 1000000000.0f;
	private GestureDetector m_GestureDetector = null;
	MediaSdk.PreviewObserver m_observer = null;
	SensorManager m_SensorManager;	//SensorManager��������
	Sensor m_Gyroscope; 	//����������

}
