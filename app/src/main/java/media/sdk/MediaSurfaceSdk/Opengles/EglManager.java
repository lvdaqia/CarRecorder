package media.sdk.MediaSurfaceSdk.Opengles;


import media.sdk.MediaSurfaceSdk.Opengles.grafika.EglCore;

public class EglManager
{
	private static EglCore mEglCore;
	private EglManager()
	{
		
	}
	public static EglCore getEglCore()
	{
		synchronized (EglCore.class)
		{
			if(mEglCore == null)
			{
				mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
			}
		}
		return mEglCore;
	}
}
