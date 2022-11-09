package media.sdk.MediaSurfaceSdk.Opengles;

import android.graphics.SurfaceTexture;
import android.view.Surface;

import media.sdk.MediaSurfaceSdk.Opengles.grafika.WindowSurface;

public class KWindowSurface extends WindowSurface
{
	public boolean watermark;

	/**
	 * Associates an EGL surface with the native window surface.
	 * <p>
	 * Set releaseSurface to true if you want the Surface to be released when
	 * release() is called. This is convenient, but can interfere with framework
	 * classes that expect to manage the Surface themselves (e.g. if you release a
	 * SurfaceView's Surface, the surfaceDestroyed() callback won't fire).
	 */
	public KWindowSurface(Surface surface, boolean watermark)
	{
		super(EglManager.getEglCore(), surface, true);
		this.watermark = watermark;
	}

	/**
	 * Associates an EGL surface with the SurfaceTexture.
	 */
	public KWindowSurface(SurfaceTexture surfaceTexture, boolean watermark)
	{
		super(EglManager.getEglCore(), surfaceTexture);
		this.watermark = watermark;
	}

	public int Draw(float[] m, float fXScale, float fYScale)
	{
		return m_surfaceText.Draw(m, fXScale, fYScale);
	}

	public SurfaceText m_surfaceText = null;
}
