package media.sdk.Common.Record;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import car.recorder.carrecorder;

public class OsdOverlay
{
	public OsdOverlay()
	{

	}

	public int Create(int width, int height)
	{
		m_width = width;
		m_height = height;
		m_hFont = carrecorder.FontCreateByMemory(carrecorder.m_fft, carrecorder.m_fft.length, 64, 128, 32);
		return 0;
	}

	public int Delete()
	{
		if(m_hFont != 0)
		{
			carrecorder.FontDelete(m_hFont);
			m_hFont = 0;
		}
		return 0;
	}

	public int WriteOSD(byte[] yuv, int nCharWidth, int nCharHeight)
	{
		if(carrecorder.m_bOsdTimeShow == 0)
		{
			return 0;
		}
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());//获取当前时间
		String strText = formatter.format(curDate);
		if(m_strText.compareTo(strText) != 0)
		{
			m_strText = strText;
			byte[] text = strText.getBytes();
			carrecorder.FontLoadImageA(m_hFont, text, 0, strText.length(), 0.0f, nCharWidth, nCharHeight, 4);
			int nWidth = carrecorder.FontGetImageWidth(m_hFont);
			int nHeight = carrecorder.FontGetImageHeight(m_hFont);
			Log.d("OSD", "Load:" + nWidth + "," + nHeight);
		}
		else
		{
			Log.d("OSD", "No Load");
		}
		carrecorder.FontDrawImage(m_hFont, yuv, m_width, m_height, carrecorder.m_nOsdXPos, carrecorder.m_nOsdYPos, 1);
		return 0;
	}

	public int WriteOSD(byte[] yuv)
	{
		int nCharWidth = 16;
		int nCharHeight = 32;
		if(m_width >= 1920)
		{
			nCharWidth = 32;
			nCharHeight = 64;
		}
		else if(m_width >= 1280)
		{
			nCharWidth = 24;
			nCharHeight = 48;
		}
		else if(m_width >= 640)
		{
			nCharWidth = 16;
			nCharHeight = 32;
		}
		WriteOSD(yuv, nCharWidth, nCharHeight);
		return 0;
	}

	private String m_strText = "";
	private long m_hFont = 0;
	private int m_width = 0;
	private int m_height = 0;

};