package media.sdk.MediaSurfaceSdk.Display;

import android.app.Activity;
import android.provider.MediaStore;
import android.view.SurfaceView;
import android.view.View;

import media.sdk.MediaSdk;


public class ImageDrawerManager
{
	static ImageDrawerManager m_imgDrawerManager = new ImageDrawerManager();
	public static ImageDrawerManager Instant()
	{
		return m_imgDrawerManager;
	}

	public ImageDrawerManager()
	{

	}

	public int CreateAllDrawer(Activity activity, MediaSdk.PreviewObserver observer)
	{
		m_bStoped = true;
		m_activity = activity;
		if(observer != null)
		{
			for(int i = 0; i < m_imageDrawer.length; i++)
			{
				m_imageDrawer[i] = new ImageDrawer(m_activity, observer, MediaSdk.Instant().WindowSurfaceDraw_Get(i));
				m_imageDrawer[i].Start();
			}
		}
		return 0;
	}
	
	public int DeleteAllDrawer()
	{
		for(int i = 0; i < m_imageDrawer.length; i++)
		{
			if(m_imageDrawer[i] != null)
			{
				m_imageDrawer[i].Stop();
				m_imageDrawer[i] = null;
			}
		}
		return 0;
	}

	synchronized public int Stop()
	{
		m_bStoped = true;
		return 0;
	}

	synchronized public int Start()
	{
		m_bStoped = false;
		return 0;
	}

	public SurfaceView GetView(int nIndex)
	{
		return (SurfaceView)m_imageDrawer[nIndex];
	}

	private boolean m_bStoped = true;
	private Activity m_activity;
	public String m_strCameraID = null;
	public ImageDrawer m_imageDrawer[] = new ImageDrawer[2];
}
