package media.sdk.Common.audio.AudioCapture;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import car.recorder.carrecorder;
import media.sdk.MediaSdk;

public class AudioCatpure implements MediaSdk.AudioInputObserver
{
	public AudioCatpure()
	{

	}

	public abstract class AudioSource implements Runnable
	{

		public void run()
		{
			OnStart(m_nSampleRate, m_nChannels, m_observer);
			while(!m_isEnd)
			{
				int nActionCount = HandleRead();
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
			OnStop();
			m_isRunning = false;
		}

		public int Start(int nSampleRate, int nChannels, MediaSdk.AudioInputObserver observer)
		{
			m_nSampleRate = nSampleRate;
			m_nChannels = nChannels;
			m_observer = observer;
			m_isRunning = true;
			m_isEnd = false;
			m_thread = new Thread(this);
			m_thread.start();
			return 0;
		}

		public int Stop()
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
			return 0;
		}

		public abstract int OnStart(int nSampleRate, int nChannels, MediaSdk.AudioInputObserver observer);
		public abstract int OnStop();
		public abstract int HandleRead();
		public int m_nSampleRate = 8000;
		public int m_nChannels = 1;
		public MediaSdk.AudioInputObserver m_observer = null;
	}

	public class JavaAudioSource extends AudioSource
	{
		public int OnStart(int nSampleRate, int nChannels, MediaSdk.AudioInputObserver observer)
		{
			m_observer = observer;
			m_recBufSize = AudioRecord.getMinBufferSize(nSampleRate,
					AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
//			m_AudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
//					nSampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
//					AudioFormat.ENCODING_PCM_16BIT, m_recBufSize * 4);
			m_AudioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,
					nSampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
					AudioFormat.ENCODING_PCM_16BIT, m_recBufSize * 4);
			m_buffer = new byte[m_recBufSize];
			m_AudioRecord.startRecording();
			return 0;
		}

		public int OnStop()
		{
			m_AudioRecord.stop();
			if(m_AudioRecord != null)
			{
				m_AudioRecord.release();
				m_AudioRecord = null;
			}
			return 0;
		}

		public int HandleRead()
		{
			int length = m_AudioRecord.read(m_buffer, 0, 640);
			if(length <= 0)
			{
				return 0;
			}

			m_observer.OnAudioInput(m_buffer, 0, 640, m_nSampleRate, m_nChannels);
			return length;
		}

		private AudioRecord m_AudioRecord = null;
		private int m_recBufSize = 0;
		private byte[] m_buffer = null;
		MediaSdk.AudioInputObserver m_observer = null;
	}

	public int OnAudioInput(byte[] data, int offset, int len, int nSampleRate, int nChannels)
	{
		int result = 0;

		//PCM 8000
		result = len;
		m_observer.OnAudioEncoded(data, offset, result, carrecorder.AUDIOTYPE_PCM, 0, nSampleRate, nChannels);

		if(m_observer.OnAudioEncodedCheck(carrecorder.AUDIOTYPE_G711, 0, 8000, nChannels))
		{
			//G711A 8000
			result = carrecorder.AEEncode(m_hG711AEncoder, data, offset, len, m_audio);
			m_observer.OnAudioEncoded(m_audio, 0, result, carrecorder.AUDIOTYPE_G711, 0, 8000, nChannels);
		}

		if(m_observer.OnAudioEncodedCheck(carrecorder.AUDIOTYPE_G711, 1, 8000, nChannels))
		{
			//G711Mu 8000
			result = carrecorder.AEEncode(m_hG711MuEncoder, data, offset, len, m_audio);
			m_observer.OnAudioEncoded(m_audio, 0, result, carrecorder.AUDIOTYPE_G711, 1, 8000, nChannels);
		}

		if(m_observer.OnAudioEncodedCheck(carrecorder.AUDIOTYPE_AAC, 0, 8000, nChannels))
		{
			//AAC 8000
			result = carrecorder.AEEncode(m_hAACEncoder, data, offset, len, m_audio);
			if(result > 0)
			{
				m_observer.OnAudioEncoded(m_audio, 0, result, carrecorder.AUDIOTYPE_AAC, 0, 8000, nChannels);
			}
		}

		return 0;
	}

	public int Start(MediaSdk.AudioEncodedObserver observer, int flag, int nChannels)
	{
		m_audioSource = new JavaAudioSource();
		m_hG711AEncoder = carrecorder.AECreate(carrecorder.AUDIOTYPE_G711, 0, 8000, 1, 1024);
		m_hG711MuEncoder = carrecorder.AECreate(carrecorder.AUDIOTYPE_G711, 1, 8000, 1, 1024);
		m_hAACEncoder = carrecorder.AECreate(carrecorder.AUDIOTYPE_AAC, 0, 8000, 1, 4096);
		m_observer = observer;
		m_audioSource.Start(8000, nChannels, this);
		return 0;
	}

	public int Stop()
	{
		m_audioSource.Stop();
		if(m_hG711MuEncoder != 0)
		{
			carrecorder.AEDelete(m_hG711MuEncoder);
			m_hG711MuEncoder = 0;
		}
		if(m_hG711AEncoder != 0)
		{
			carrecorder.AEDelete(m_hG711AEncoder);
			m_hG711AEncoder = 0;
		}
		if(m_hAACEncoder != 0)
		{
			carrecorder.AEDelete(m_hAACEncoder);
			m_hAACEncoder = 0;
		}
		return 0;
	}

	AudioSource m_audioSource = null;
	long m_hAACEncoder = 0;
	long m_hG711AEncoder = 0;
	long m_hG711MuEncoder = 0;
	private byte[] m_audio = new byte[2048];
	public MediaSdk.AudioEncodedObserver m_observer = null;
	private boolean m_isRunning = false;
	private boolean m_isEnd = true;
	private Thread m_thread = null;
	static public AudioCatpure m_audioCapture = new AudioCatpure();
	static public AudioCatpure Instant()
	{
		return m_audioCapture;
	}
}
