package media.sdk.Common;


public class FrameCount
{
	public FrameCount(String strName)
	{
		m_strName = strName;
		m_cycle = 1000;
		m_nLastTime = 0;
	}

	public int Reset()
	{
		m_nFrameCount = 0;
		m_nFrameRate = 0;
		m_nLastTime = 0;
		return 0;
	}

	public int Add(int length)
	{
		m_nFrameCount += length;
		long time = System.currentTimeMillis();
		long nTimeSpan = time - m_nLastTime;
		if(nTimeSpan < 0)
		{
			m_nLastTime = time;
		}
		else if(nTimeSpan > m_cycle)
		{
			//Log.i("FRINFO", m_strName + ":" + Long.toString(m_nFrameCount) + "-" + m_nFrameRate);
			m_nFrameRate = (int)((float)m_nFrameCount * 1000.0 / (float)nTimeSpan + 0.9f);
			m_nFrameCount = 0;
			m_nLastTime = time;
			return 1;
		}
		return 0;
	}

	public int GetFrameRate()
	{
		Add(0);
		return m_nFrameRate;
	}

	private int m_cycle = 1000;
	private int m_nFrameRate = 0;
	private long m_nLastTime = 0;
	private int m_nFrameCount = 0;
	private String m_strName = null;
}
