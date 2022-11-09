package media.sdk.Common.Playback;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.dell.carrecorder.bean.SettingBean;

import car.recorder.carrecorder;
import media.sdk.MediaSdk;

public class LocalPlayback implements Runnable
{
	public LocalPlayback()
	{

	}

	class PlaybackFile
	{
		Handler m_msgHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				if(msg.what == 0)
				{
					m_observer.OnStartPlaybackSuccess();
				}
				else
				{
					m_observer.OnStartPlaybackFailed(msg.what);
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

		public boolean isStarted()
		{
			return m_bStart;
		}

		public int StartPlayback(String strName)
		{
			m_strName = strName;
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
				carrecorder.Mp4FRClose(m_hMP4File);
				carrecorder.Mp4FRDelete(m_hMP4File);
				m_hMP4File = 0;
				PlaybackFileFactory.DeleteFile(m_hFile);
				m_hFile = -1;
			}
			return 0;
		}

		private int CreateFile(String strName)
		{
			//PlaybackFileFactory.PlaybackFile playbackFile = new PlaybackFileFactory.DocumentPlaybackFile();
			PlaybackFileFactory.PlaybackFile playbackFile = new PlaybackFileFactory.BasePlaybackFile();
			m_hFile = PlaybackFileFactory.AddFile(playbackFile, m_activity, strName, PlaybackFileFactory.FILETYPE_MP4);
			if(m_hFile < 0)
			{
				return m_hFile;
			}
			m_hMP4File = carrecorder.Mp4FRCreate(m_hFile);
			playbackFile.m_hMP4File = m_hMP4File;
			int res = carrecorder.Mp4FROpen(m_hMP4File);

			int nTimeOffset = 0;
			//int nTotalDuration = carrecorder.Mp4FRGetDuration(m_hMP4File);
			//nTimeOffset = nTotalDuration / 2;
			//carrecorder.Mp4FRSeek(m_hMP4File, nTimeOffset);
			m_nBeginTimeStamp = nTimeOffset;
			m_nVideoTimeStamp = nTimeOffset;
			m_nAudioTimeStamp = nTimeOffset;
			m_nAudioLength = 0;
			m_bWaitIFrame = true;
			return res;
		}

		private int CreateFile()
		{
			if(m_hMP4File != 0)
			{
				return 0;
			}
			int res = CreateFile(m_strName);
			if(res == 0)
			{
				SendResult(0);
			}
			else
			{
				SendResult(PlaybackFileFactory.m_error);
			}
			return 0;
		}

		public int HandlePCM(byte[] pcm, int offset, int len, long nTimeStamp)
		{
			if(SettingBean.getInstance().getaudioType()==1) {
				carrecorder.AEEncode(m_hAEG711, pcm, offset, len, m_g711Buffer);
				m_observer.onPlaybackAudioData(m_nChannel, m_g711Buffer, len / 2, nTimeStamp);
			}
			else
			{
				int length = carrecorder.AEEncode(m_hAEG711, pcm, offset, len, m_aacBuffer);
				if(length>0)
					m_observer.onPlaybackAudioData(m_nChannel, m_aacBuffer, length, nTimeStamp);
			}
			//Log.d("Playback", "trackType:" + "audio" + " length:" + len +
			//		" timestamp:" + nTimeStamp);
			return 0;
		}

		public int HandleFrame()
		{
			if(m_trackType == 2)
			{
				if(m_nBeginTimeStamp == 0)
				{
					m_nBeginTimeStamp = System.currentTimeMillis();
				}
				else
				{
					long span = System.currentTimeMillis() - m_nBeginTimeStamp;
					if(span < m_nVideoTimeStamp)
					{
						return 0;
					}
					if((span - m_nVideoTimeStamp) >  200)
					{
						m_nBeginTimeStamp = System.currentTimeMillis() - m_nVideoTimeStamp;
					}
				}

				int type = 0;
				if(m_codecID == 13)
				{
					type = (int)(m_mp4Buffer[4] & 0x1F);
					if(type != 7 && m_bWaitIFrame)
					{
						m_nMp4BufferLength = 0;
						return 0;
					}
				}
				else
				{
					m_nMp4BufferLength = 0;
					return 0;
				}

				m_bWaitIFrame = false;
				m_observer.onPlaybackVideoData(m_nChannel, type, m_mp4Buffer, m_nMp4BufferLength, m_nVideoTimeStamp);
//				Log.d("Playback", "trackType:" + "video" + " codecID:" + m_codecID + " duration:" + m_duration + " length:" + m_nMp4BufferLength +
//						" type:" + type + " timestamp:" + m_nVideoTimeStamp);
				m_nVideoTimeStamp += m_duration;
				m_nMp4BufferLength = 0;
			}
			else
			{
				if(m_bWaitIFrame)
				{
					m_nMp4BufferLength = 0;
					return 0;
				}

				int len = carrecorder.ADDecode(m_hAD, m_mp4Buffer, 0, m_nMp4BufferLength, m_audioBuffer, m_nAudioLength);
				if(len > 0)
				{
					m_nAudioLength += len;
					int count = m_nAudioLength / 640;
//					HandlePCM(m_audioBuffer, 0, m_nAudioLength, m_nAudioTimeStamp );
//					m_nAudioLength = 0;
					if(count > 0)
					{
						int durationPacket = m_duration / count;
						for(int i = 0; i < count; i++)
						{
							HandlePCM(m_audioBuffer, i * 640, 640, m_nAudioTimeStamp + durationPacket * i);
						}

						int offset = count * 640;
						for(int i = offset; i < m_nAudioLength; i++)
						{
							m_mp4Buffer[i - offset] = m_mp4Buffer[i];
						}
						m_nAudioLength -= offset;
					}
//					Log.d("Playback", "trackType:" + "audio" + " codecID:" + m_codecID + " duration:" + m_duration + " length:" + len +
//							" timestamp:" + m_nAudioTimeStamp);
				}
				else
				{
					Log.d("Playback", "Audio is ignored!");
				}
				m_nAudioTimeStamp += m_duration;
				m_nMp4BufferLength = 0;
			}

			return 0;
		}

		public int HandlePlayback()
		{
			if(m_hMP4File == 0)
			{
				return 0;
			}

			if(m_nMp4BufferLength > 0)
			{
				HandleFrame();
				return 0;
			}

			int[] trackType = new int[1];
			int[] codecID = new int[1];
			int[] duration = new int[1];
			int res = carrecorder.Mp4FRReadFrame(m_hMP4File, trackType, codecID, duration, m_mp4Buffer, 0, m_mp4Buffer.length);
			if(res > 0)
			{
				m_duration = duration[0];
				m_trackType = trackType[0];
				m_codecID = codecID[0];
				m_nMp4BufferLength = res;
				HandleFrame();
			}
//			else
//			{
//				HandleFrame();
//			}

			return 0;
		}

		public int HandleStartPlayback()
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

		public int HandleSeek()
		{
			if(m_bNeedSeek)
			{
				if(m_hMP4File != 0)
				{
					carrecorder.Mp4FRSeek(m_hMP4File, m_nTimeOffset);
					m_bWaitIFrame = true;
					m_nBeginTimeStamp = m_nTimeOffset;
					m_nVideoTimeStamp = m_nTimeOffset;
					m_nAudioTimeStamp = m_nTimeOffset;
					m_nAudioLength = 0;
				}
				m_bNeedSeek = false;
			}
			return 0;
		}

		public int Heartbeat()
		{
			int nActionCount = HandleStartPlayback();
			nActionCount += HandlePlayback();
			nActionCount += HandleSeek();
			return nActionCount;
		}

		public int Seek(int nChannel, int nTimeOffset)
		{
			m_nTimeOffset = nTimeOffset;
			m_bNeedSeek = true;
			return 0;
		}

		public int Create(Activity activity, MediaSdk.LocalPlaybackObserver observer, int nChannel)
		{
			m_hAD = carrecorder.ADCreate(2, 0, 8000, 1, 4096);
			if(SettingBean.getInstance().getaudioType()==1)
				m_hAEG711 = carrecorder.AECreate(SettingBean.getInstance().getaudioType(), 0, 8000, 1, 1024);
			else
				m_hAEG711 = carrecorder.AECreate(SettingBean.getInstance().getaudioType(), 0, 8000, 1, 4096);
			m_nChannel = nChannel;
			m_observer = observer;
			m_activity = activity;
			return 0;
		}

		public int Delete()
		{
			if(m_hAEG711 != 0)
			{
				carrecorder.AEDelete(m_hAEG711);
				m_hAEG711 = 0;
			}
			if(m_hAD != 0)
			{
				carrecorder.ADDelete(m_hAD);
				m_hAD = 0;
			}
			return 0;
		}

		public byte[] m_audioBuffer = new byte[4096];
		public byte[] m_aacBuffer = new byte[4096];
		public byte[] m_g711Buffer = new byte[640];
		int m_nAudioLength = 0;
		public long m_hAD = 0;
		public long m_hAEG711 = 0;
		public int m_nChannel = 0;
		public long m_nBeginTimeStamp = 0;
		public long m_nVideoTimeStamp = 0;
		public long m_nAudioTimeStamp = 0;
		public boolean m_bWaitIFrame = false;
		public int m_nTimeOffset = 0;
		public boolean m_bNeedSeek = false;
		private String m_strName = "";
		private int m_hFile = 0;
		private long m_hMP4File = 0;
		private boolean m_bStart = false;
		private byte[] m_mp4Buffer = new byte[500 * 1024];
		private int m_nMp4BufferLength = 0;
		private int m_duration = 0;
		private int m_trackType = 0;
		private int m_codecID = 0;
		private MediaSdk.LocalPlaybackObserver m_observer = null;
		private Activity m_activity = null;
	};

	public int Seek(int nChannel, int nTimeOffset)
	{
		return m_playbackFile[nChannel].Seek(nChannel, nTimeOffset);
	}

	public int StartPlayback(int nChannel, String strFileName)
	{
		return m_playbackFile[nChannel].StartPlayback(strFileName);
	}

	public int StopPlayback(int nChannel)
	{
		return m_playbackFile[nChannel].StopRecord();
	}

	public boolean isStarted(int nChannel)
	{
		return m_playbackFile[nChannel].isStarted();
	}

	public void run()
	{
		while(!m_isEnd)
		{
			int nActionCount = 0;
			for(int i = 0; i < m_playbackFile.length; i++)
			{
				nActionCount += m_playbackFile[i].Heartbeat();
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

	public int Create(Activity activity, MediaSdk.LocalPlaybackObserver observer)
	{
		for(int i = 0; i < m_playbackFile.length; i++)
		{
			m_playbackFile[i] = new PlaybackFile();
			m_playbackFile[i].Create(activity, observer, i);
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
		for(int i = 0; i < m_playbackFile.length; i++)
		{
			m_playbackFile[i].Delete();
			m_playbackFile[i] = null;
		}
		return 0;
	}

	PlaybackFile[] m_playbackFile = new PlaybackFile[2];
	private boolean m_isRunning = false;
	private boolean m_isEnd = true;
	private Thread m_thread = null;
}
