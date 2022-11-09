package media.sdk.Common.Playback;

import android.app.Activity;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.channels.FileChannel;

import car.recorder.carrecorder;
import media.sdk.Common.Record.RecordFileFactory;

public class PlaybackFileFactory
{
	static public int FILETYPE_MP4 = 0;
	static public int FILETYPE_AAC = 1;
	static public abstract class PlaybackFile
	{
		PlaybackFile()
		{

		}

		public abstract int Open(int nIndex, Activity activity, String strName, int nFileType);
		public abstract int Close();
		public abstract byte[] Read(int nMaxLen);
		public abstract int Seek(int pos);
		public abstract int Tell();
		public byte[] m_buffer = new byte[1024 * 1024];
		public long m_hMP4File = 0;
		public long m_nMdatPos = 0;
	};

	static public class BasePlaybackFile extends PlaybackFile
	{
		public int Open(int nIndex, Activity activity, String strName, int nFileType)
		{
			String strDirector = RecordFileFactory.strDirector;
			File path = new File(strDirector);
			if (!path.exists())
			{
				path.mkdirs();
			}
			Log.d("playback",strName);
			m_hFile = carrecorder.FileCreate();
			carrecorder.FileOpen(m_hFile, carrecorder.OPENFLAG_OPENALWAYS | carrecorder.OPENFLAG_WRITE | carrecorder.OPENFLAG_READ, strName);
			return  0;
		}

		public byte[] Read(int nMaxLen)
		{
			int res = carrecorder.FileRead(m_hFile, m_buffer, nMaxLen);
			if(res <= 0)
			{
				return null;
			}
			return m_buffer;
		}

		public int Seek(int pos)
		{
			return carrecorder.FileSeek(m_hFile, pos, carrecorder.PFILESEEK_SET);
		}

		public int Tell()
		{
			return carrecorder.FileTell(m_hFile);
		}

		public int Close()
		{
			carrecorder.FileClose(m_hFile);
			carrecorder.FileDelete(m_hFile);
			m_hFile = 0;
			return 0;
		}

		long m_hFile;
	}

	static public class DocumentPlaybackFile extends PlaybackFile
	{
		String GetDescString(int nFileType)
		{
			if(nFileType == 0)
			{
				return "video/mp4";
			}
			return "audio/aac";
		}
		String GetTypeString(int nFileType)
		{
			if(nFileType == 0)
			{
				return ".mp4";
			}
			return ".aac";
		}
		public int Open(int nIndex, Activity activity, String strName, int nFileType)
		{
			if(m_docDir == null)
			{
				m_error = -2;
				return -1;
			}

			DocumentFile file = m_docDir.findFile(strName);
			if(file == null)
			{
				m_error = -1;
				return -1;
			}

			Uri uriFile = file.getUri();
			try
			{
				m_nIndex = nIndex;
				m_descriptor = activity.getContentResolver().openFileDescriptor(uriFile, "rw");
				m_fd = m_descriptor.getFileDescriptor();
				m_fis = new FileInputStream(m_fd);
				m_channel = m_fis.getChannel();

			}
			catch (FileNotFoundException e)
			{

			}
			catch (Exception e)
			{

			}
			m_error = 0;
			return  0;
		}

		public byte[] Read(int nMaxLen)
		{
			try
			{
				m_fis.read(m_buffer, 0, nMaxLen);
			}
			catch (FileNotFoundException e)
			{
				return null;
			}
			catch (Exception e)
			{
				return null;
			}
			return m_buffer;
		}

		public int Seek(int pos)
		{
			try
			{
				m_channel.position((long)pos);
			}
			catch (FileNotFoundException e)
			{
				return 0;
			}
			catch (Exception e)
			{
				return 0;
			}
			return pos;
		}

		public int Tell()
		{
			long pos = 0;
			try
			{
				pos = m_channel.position();
			}
			catch (FileNotFoundException e)
			{
				return 0;
			}
			catch (Exception e)
			{
				return 0;
			}
			return (int)pos;
		}

		public int Close()
		{
			try
			{
				m_fis.close();
				m_channel.close();
			}
			catch (FileNotFoundException e)
			{
				return 0;
			}
			catch (Exception e)
			{
				return 0;
			}
			return 0;
		}

		long m_nPos = 0;
		FileDescriptor m_fd;
		ParcelFileDescriptor m_descriptor;
		FileChannel m_channel;
		FileInputStream m_fis;
		int m_nIndex = 0;
	}

	static public DocumentFile m_docDir = null;
	static public int m_error = 0;
	static public PlaybackFile[] m_playbackFile = null;
	static public String m_strUri = "";
	static public String m_strPath = "";

	static public int LoadDirector(Activity activity, Uri uri)
	{
		DocumentFile docTree = DocumentFile.fromTreeUri(activity, uri);
		if(docTree == null)
		{
			m_error = -2;
			return -1;
		}

		String directoryName = "Record";
		if(docTree.findFile(directoryName) == null)
		{
			m_docDir = docTree.createDirectory(directoryName);
		}
		else
		{
			m_docDir = docTree.findFile(directoryName);
		}

		if(m_docDir == null)
		{
			m_error = -1;
			return -1;
		}

		return 0;
	}

	static public int LoadDirector(Activity activity)
	{
		if(m_strUri.isEmpty())
		{
			m_error = -3;
			return -1;
		}

		Uri uri = Uri.parse(PlaybackFileFactory.m_strUri);
		return LoadDirector(activity, uri);
	}

	static public int Initial(Activity activity, int nMaxFileCount)
	{
		LoadDirector(activity);
		m_playbackFile = new PlaybackFile[nMaxFileCount];
		return 0;
	}

	static int AddFile(PlaybackFile playbackFile, Activity activity, String strName, int nFileType)
	{
		for(int i = 0; i < m_playbackFile.length; i++)
		{
			if(m_playbackFile[i] == null)
			{
				int res = playbackFile.Open(i, activity, strName, nFileType);
				if(res != 0)
				{
					return -1;
				}
				m_playbackFile[i] = playbackFile;
				return i;
			}
		}
		return -1;
	}

	static int DeleteFile(int nIndex)
	{
		if(m_playbackFile[nIndex] != null)
		{
			m_playbackFile[nIndex].Close();
			m_playbackFile[nIndex] = null;
		}
		return 0;
	}

	public PlaybackFileFactory(int size)
	{

	}


}
