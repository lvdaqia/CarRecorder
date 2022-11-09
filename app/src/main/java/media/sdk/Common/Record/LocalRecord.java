package media.sdk.Common.Record;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import car.recorder.carrecorder;
import media.sdk.Common.BufferIn;
import media.sdk.Common.BufferOut;
import media.sdk.Common.DB.CenterDB;
import media.sdk.MediaSdk;

public class LocalRecord implements Runnable
{
	public LocalRecord()
	{

	}

	class RecordFile
	{
		Handler m_msgHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				if(msg.what == 0)
				{
					m_observer.OnStartRecordSuccess(m_strName);
				}
				else
				{
					m_observer.OnStartRecordFailed(msg.what);
				}

				super.handleMessage(msg);
			}
		};

		int SendResult(int result)
		{
			Message message = new Message();
			message.what = result;
			m_msgHandler.sendMessage(message);
			return 0;
		}

		public class CBaseFrame
		{
			long nTimeStamp;
			int type;
		}

		public class CAudioFrame extends CBaseFrame
		{
			public byte[] data;
			int offset;
			int length;
		}

		public class CVideoFrame extends CBaseFrame
		{
			int bKeyFrame;
			public byte[] data;
			int offset;
			int length;
		}

		public int GetRecordTime()
		{
			long nRecordTime = System.currentTimeMillis();
			int diff = (int)(nRecordTime - m_nFirstRecordTimeStamp);
			return diff;
		}

		public boolean isStarted()
		{
			return m_bStart;
		}

		public int StartRecord(int nChannel, int nVideoEncodeType, int width, int height)
		{
			m_nChannel = nChannel;
			m_nVideoEncodeType = nVideoEncodeType;
			m_width = width;
			m_height = height;
			m_bStart = true;
			return 0;
		}

		public int StopRecord()
		{
			m_bStart = false;
			return 0;
		}

		private int DeleteFile()
		{
			if(m_hMP4File != 0)
			{
				carrecorder.Mp4FWClose(m_hMP4File);
				carrecorder.Mp4FWDelete(m_hMP4File);
				m_hMP4File = 0;
				RecordFileFactory.DeleteFile(m_hFile);
				m_hFile = -1;
			}
			return 0;
		}

		private int CreateFile(String strName)
		{
           // RecordFileFactory.RecordFile recFile = new RecordFileFactory.DocumentRecordFile();
			try {
				RecordFileFactory.RecordFile recFile = new RecordFileFactory.BaseRecordFile();
				m_hFile = RecordFileFactory.AddFile(recFile, m_activity, strName, RecordFileFactory.FILETYPE_MP4, m_nChannel);

				if (m_hFile < 0) {
					return m_hFile;
				}
				m_hMP4File = carrecorder.Mp4FWCreate(m_hFile);
				recFile.m_hMP4File = m_hMP4File;
				carrecorder.Mp4FWOpen(m_hMP4File, m_nVideoEncodeType, m_width, m_height, 8000, 60 * 25, 60 * 60 * 25);
				CenterDB.DBFile dbFile = new CenterDB.DBFile();
				dbFile.strDeviceID = carrecorder.m_strDeviceID;
				dbFile.strFileName = strName;
				dbFile.nFileType = 0;
				dbFile.nMediaType = 0;
				dbFile.nFileSize = 0;
				dbFile.nTimeStamp = m_nFirstRecordTimeStamp;
				CenterDB.Instant().AddFile(dbFile);
				recFile.m_nMdatPos = carrecorder.Mp4FWGetMdatPos(m_hMP4File) + 4;
			}catch (Exception e){
				e.printStackTrace();
				return -2;
			}
			return 0;
		}

		private int CreateFile()
		{
			if(m_hMP4File != 0)
			{
				return 0;
			}
			m_nFileIndex = 0;
			m_nFirstRecordTimeStamp = System.currentTimeMillis();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			Date curDate = new Date(m_nFirstRecordTimeStamp);//获取当前时间
			String strName = carrecorder.m_strDeviceID + "_" + m_nChannel + "_" + formatter.format(curDate);
			int res = CreateFile(strName);
			if(res == 0)
			{
				SendResult(0);
				m_nFileIndex++;
				m_strName = strName;
			}else if(res == -2) {
				SendResult(-2);
				m_bStart = false;
			}else
			{
				SendResult(RecordFileFactory.m_error);
				m_bStart = false;
			}
			return 0;
		}


		public int HandleRecord()
		{
			CBaseFrame frame = GetAV();
			if(frame == null)
			{
				return 0;
			}
			if(m_hMP4File == 0)
			{
				m_bWaitIFrame = true;
				m_nFirstFileTimeStamp = 0;
				return 1;
			}

			if(frame.type == 1)
			{
				if(m_bWaitIFrame)
				{
					return 1;
				}
				CAudioFrame audioFrame = (CAudioFrame)frame;
				int nTimeStamp = GetTimeStamp(audioFrame.nTimeStamp, 1000);
				//Log.d("Mp4", "Audio len:" + audioFrame.length + " TimeStamp:" + nTimeStamp);
				try {
					carrecorder.Mp4FWAddAudioData(m_hMP4File, carrecorder.AUDIOTYPE_AAC, audioFrame.data, audioFrame.offset, audioFrame.length, nTimeStamp);
				}catch (Exception e){
					SendResult(-1);
				}
			}
			else if(frame.type == 0)
			{
				CVideoFrame videoFrame = (CVideoFrame)frame;
				if(videoFrame.bKeyFrame != 0)
				{
					int nFileSize = carrecorder.Mp4FWGetFileSize(m_hMP4File);
					int diff;
					if(m_nFirstFileTimeStamp == 0)
					{
						m_nFirstFileTimeStamp = videoFrame.nTimeStamp;
						diff = 0;
					}
					else
					{
						diff = (int)(videoFrame.nTimeStamp - m_nFirstFileTimeStamp);
					}
					if((nFileSize > RecordFileFactory.m_nFileSize * 1024 * 1024) || (diff > RecordFileFactory.m_nFileTime * 60 * 1000))
					{
						DeleteFile();
						int result = CreateFile(m_strName + "_" + m_nFileIndex);
						if(result == -2){
							SendResult(-2);
						}
						Log.d("filename",m_strName + "_" + m_nFileIndex);
						m_nFirstFileTimeStamp = 0;
						m_nFileIndex++;
					}
				}
				if(m_bWaitIFrame && videoFrame.bKeyFrame == 0)
				{
					return 1;
				}
				m_bWaitIFrame = false;
				int nTimeStamp = GetTimeStamp(videoFrame.nTimeStamp, 1000);
				try {
					carrecorder.Mp4FWAddVideoData(m_hMP4File, videoFrame.data, videoFrame.offset, videoFrame.length, nTimeStamp);
				}catch (Exception e){
					SendResult(-1);
				}
			}
			return 1;
		}

		public int HandleStartRecord()
		{
			if(m_bStart)
			{
				CreateFile();
			}
			else
			{
				DeleteFile();
			}
			return 0;
		}

		public int Heartbeat()
		{
			int nActionCount = HandleStartRecord();
			nActionCount += HandleRecord();
			return nActionCount;
		}

		public int GetTimeStamp(long nTimeStamp, int diff)
		{
			if(m_nFirstFileTimeStamp == 0)
			{
				m_nFirstFileTimeStamp = nTimeStamp;
				return 0;
			}
			return (int)(nTimeStamp - m_nFirstFileTimeStamp) + diff;
		}


		synchronized public CBaseFrame GetAV()
		{
			if(RecordFileFactory.m_bEnablePreRecord != 0)
			{
				if(m_hMP4File == 0)
				{
					long nTimeStamp = FrontTimeStamp();
					if(nTimeStamp == 0)
					{
						return null;
					}
					int diff = (int)(m_nTimeStampRear - nTimeStamp);
					if(diff < (RecordFileFactory.m_nPreRecordTime * 1000))
					{
						//Log.d("Mp4", "Pre Record:" + diff);
						return null;
					}
				}
			}

			return PopFrame();
		}

		public int PushVideo(int bKeyFrame, byte[] data, int offset, int length, long nTimeStamp)
		{
			if(m_hRingBuffer == 0)
			{
				m_bWaitIFrame = true;
				return 0;
			}
			BufferOut bufferOut = new BufferOut(20);
			bufferOut.SetIntToByteBuffer(0);
			bufferOut.SetIntToByteBuffer((int)(nTimeStamp >> 32));
			bufferOut.SetIntToByteBuffer((int)(nTimeStamp));
			bufferOut.SetIntToByteBuffer(bKeyFrame);
			m_nTimeStampRear = nTimeStamp;
			int res = carrecorder.RingBufferPush(m_hRingBuffer, bufferOut.m_pBuffer, 0, bufferOut.m_nPos, data, offset, length);
			while (res <= 0)
			{
				carrecorder.RingBufferPopNull(m_hRingBuffer);
				res = carrecorder.RingBufferPush(m_hRingBuffer, bufferOut.m_pBuffer, 0, bufferOut.m_nPos, data, offset, length);
				m_bWaitIFrame = true;
			}
			//Log.d("Mp4", "PushVideo:" + nTimeStamp);
			return 0;
		}

		synchronized public int PushVideo(byte[] data, int offset, int length, long nTimeStamp)
		{
			if(carrecorder.m_nVideoEncodeType[0] != 0)
			{
				int nFrameType = (data[4] & 0x7F) >> 1;
				switch (nFrameType)
				{
					case 32:
					{
						m_sps = new byte[length];
						System.arraycopy(data, offset, m_sps, 0, length);
						return 0;
					}
					case 33:
					{
						m_pps = new byte[length];
						System.arraycopy(data, offset, m_sps, 0, length);
						return 0;
					}
					case 34:
					{
						m_vps = new byte[length];
						System.arraycopy(data, offset, m_vps, 0, length);
						return 0;
					}
					case 19:
					{
						int len = 0;
						if(m_sps != null)
						{
							System.arraycopy(m_sps, 0, m_buffer, len, m_sps.length);
							len += m_sps.length;
						}

						if(m_pps != null)
						{
							System.arraycopy(m_pps, 0, m_buffer, len, m_pps.length);
							len += m_pps.length;
						}

						if(m_vps != null)
						{
							System.arraycopy(m_vps, 0, m_buffer, len, m_vps.length);
							len += m_vps.length;
						}

						System.arraycopy(data, offset, m_buffer, len, length);
						len += length;

						PushVideo(1, m_buffer, 0, len, nTimeStamp);
					}
					break;
					case 1:
					{
						PushVideo(0, data, offset, length, nTimeStamp);
					}
					break;
					default:
						break;
				}
				return 0;
			}
			int nFrameType = (data[4] & 0x1F);
			switch (nFrameType)
			{
				case 7:
				{
					m_sps = new byte[length];
					System.arraycopy(data, offset, m_sps, 0, length);
					return 0;
				}
				case 8:
				{
					m_pps = new byte[length];
					System.arraycopy(data, offset, m_sps, 0, length);
					return 0;
				}
				case 5:
				{
					int len = 0;
					if(m_sps != null)
					{
						System.arraycopy(m_sps, 0, m_buffer, len, m_sps.length);
						len += m_sps.length;
					}

					if(m_pps != null)
					{
						System.arraycopy(m_pps, 0, m_buffer, len, m_pps.length);
						len += m_pps.length;
					}

					System.arraycopy(data, offset, m_buffer, len, length);
					len += length;

					PushVideo(1, m_buffer, 0, len, nTimeStamp);
				}
				break;
				case 1:
				{
					PushVideo(0, data, offset, length, nTimeStamp);
				}
				break;
				default:
					break;
			}

			return 0;
		}

		synchronized public int PushAudio(byte[] data, int offset, int length, long nTimeStamp)
		{
			if(m_hRingBuffer == 0)
			{
				return 0;
			}
			BufferOut bufferOut = new BufferOut(12);
			bufferOut.SetIntToByteBuffer(1);
			bufferOut.SetIntToByteBuffer((int)(nTimeStamp >> 32));
			bufferOut.SetIntToByteBuffer((int)(nTimeStamp));
			m_nTimeStampRear = nTimeStamp;
			int res = carrecorder.RingBufferPush(m_hRingBuffer, bufferOut.m_pBuffer, 0, bufferOut.m_nPos, data, offset, length);
			while (res <= 0)
			{
				carrecorder.RingBufferPopNull(m_hRingBuffer);
				res = carrecorder.RingBufferPush(m_hRingBuffer, bufferOut.m_pBuffer, 0, bufferOut.m_nPos, data, offset, length);
			}
			return 0;
		}

		public CBaseFrame PopFrame()
		{
			int res =0;
			if(m_bufferIn==null){
				Log.e("","m_bufferIn==null");
				return null;
			}
			try {
				 res = carrecorder.RingBufferPop(m_hRingBuffer, m_bufferIn.m_pBuffer, 0, m_bufferIn.m_pBuffer.length);

			}catch (Exception e){
				Log.e("","m_bufferIn==null");
			}
			if(res <= 0)
			{
				return null;
			}

			m_bufferIn.Reset();
			int type = m_bufferIn.GetIntFromByteBuffer();
			if(type == 0)
			{
				CVideoFrame frame = new CVideoFrame();
				frame.type = 0;
				frame.nTimeStamp = m_bufferIn.GetIntFromByteBuffer();
				frame.nTimeStamp <<= 32;
				frame.nTimeStamp |= m_bufferIn.GetIntFromByteBuffer();
				frame.bKeyFrame = m_bufferIn.GetIntFromByteBuffer();
				frame.data = m_bufferIn.m_pBuffer;
				frame.offset = m_bufferIn.m_nPos;
				frame.length = res - frame.offset;
				return frame;
			}
			else if(type == 1)
			{
				CAudioFrame frame = new CAudioFrame();
				frame.type = 1;
				frame.nTimeStamp = m_bufferIn.GetIntFromByteBuffer();
				frame.nTimeStamp <<= 32;
				frame.nTimeStamp |= m_bufferIn.GetIntFromByteBuffer();
				frame.data = m_bufferIn.m_pBuffer;
				frame.offset = m_bufferIn.m_nPos;
				frame.length = res - frame.offset;
				return frame;
			}
			else
			{
				return null;
			}
		}

		public long FrontTimeStamp()
		{
			if(m_bufferIn==null)m_bufferIn = new BufferIn(500 * 1024);
			int res = carrecorder.RingBufferFront(m_hRingBuffer, m_bufferIn.m_pBuffer, 0, 12);
			if(res <= 0)
			{
				return 0;
			}

			m_bufferIn.Reset();
			int type = m_bufferIn.GetIntFromByteBuffer();
			long nTimeStamp = 0;
			nTimeStamp = m_bufferIn.GetIntFromByteBuffer();
			nTimeStamp <<= 32;
			nTimeStamp |= m_bufferIn.GetIntFromByteBuffer();
			return nTimeStamp;
		}

		public int Create(Activity activity, MediaSdk.LocalRecordObserver observer)
		{
			m_observer = observer;
			m_activity = activity;
			m_hRingBuffer = carrecorder.RingBufferCreate(5 * 1024 * 1024);
			m_bufferIn = new BufferIn(500 * 1024);
			return 0;
		}

		public int Delete()
		{
			if(m_hRingBuffer != 0)
			{
				carrecorder.RingBufferDelete(m_hRingBuffer);
				m_hRingBuffer = 0;
			}
			return 0;
		}

		private long m_nTimeStampRear = 0;
		private boolean m_bWaitIFrame = true;
		private byte[] m_sps = null;
		private byte[] m_pps = null;
		private byte[] m_vps = null;
		private byte[] m_buffer = new byte[1024 * 1024];
		private int m_nFileIndex = 0;
		private String m_strName = "";
		private int m_hFile = 0;
		private long m_hMP4File = 0;
		private long m_nFirstFileTimeStamp = 0;
		private long m_nFirstRecordTimeStamp = 0;
		private boolean m_bStart = false;
		private int m_nVideoEncodeType = 0;
		private int m_width = 1280;
		private int m_height = 720;
		private int m_nChannel = 0;
		private long m_hRingBuffer = 0;
		private BufferIn m_bufferIn = null;
		private MediaSdk.LocalRecordObserver m_observer = null;
		private Activity m_activity = null;
	};

	synchronized public int PushVideo(int nChannel, byte[] data, int offset, int length, long nTimeStamp)
    {
        return m_recordFile[nChannel].PushVideo(data, offset, length, nTimeStamp);
    }

	synchronized public int PushAudio(int nChannel, byte[] data, int offset, int length, long nTimeStamp)
	{
		return m_recordFile[nChannel].PushAudio( data, offset, length, nTimeStamp);
	}

	public int StartRecord(int nChannel, int nVideoEncodeType, int width, int height)
	{
		return m_recordFile[nChannel].StartRecord(nChannel, nVideoEncodeType, width, height);
	}

	public int StopRecord(int nChannel)
	{
		return m_recordFile[nChannel].StopRecord();
	}

	public boolean isStarted(int nChannel)
	{
		return m_recordFile[nChannel].isStarted();
	}

	public int GetRecordTime(int nChannel)
	{
		return m_recordFile[nChannel].GetRecordTime();
	}

	public void run()
	{
		while(!m_isEnd)
		{
			int nActionCount = 0;
			for(int i = 0; i < m_recordFile.length; i++)
			{
				nActionCount += m_recordFile[i].Heartbeat();
			}
			if(nActionCount <= 0)
			{
				try
				{
					Thread.sleep(10);
				}
				catch (InterruptedException e)
				{

				}
			}
		}
		m_isRunning = false;
	}

	public int Create(Activity activity, MediaSdk.LocalRecordObserver observer)
	{
		for(int i = 0; i < m_recordFile.length; i++)
		{
			m_recordFile[i] = new RecordFile();
			m_recordFile[i].Create(activity, observer);
		}
		m_isRunning = true;
		m_isEnd = false;
		m_thread = new Thread(this);
		m_thread.start();
		return 0;
	}

	public int Delete()
	{
		if(m_isEnd)
		{
			return 0;
		}
		m_isEnd = true;
		try
		{
			m_thread.join();
			while(m_isRunning)
			{
				Thread.sleep(10);
			}
		}
		catch (InterruptedException e)
		{

		}
		for(int i = 0; i < m_recordFile.length; i++)
		{
			m_recordFile[i].Delete();
			m_recordFile[i] = null;
		}
		return 0;
	}

	RecordFile[] m_recordFile = new RecordFile[2];
	private boolean m_isRunning = false;
	private boolean m_isEnd = true;
	private Thread m_thread = null;
}
