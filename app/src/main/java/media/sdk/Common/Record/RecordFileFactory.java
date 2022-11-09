package media.sdk.Common.Record;

import android.app.Activity;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import car.recorder.carrecorder;
import media.sdk.Common.DB.CenterDB;

public class RecordFileFactory
{
	static public int FILETYPE_MP4 = 0;
	static public int FILETYPE_AAC = 1;
	/**
	 * 录像路径
	 */
	public static final String strDirector = "/mnt/sdcard2/CarRecord/";
	static public abstract class RecordFile
	{
		RecordFile()
		{

		}

		public abstract int Open(int nIndex, Activity activity, String strName, int nFileType);
		public abstract int Open(int nIndex, Activity activity, String strName, int nFileType,int channel);
		public abstract int Close();
		public abstract int Write(byte[] data, int pos, int nWriteLen, int nTotalWriteLen, int nTotalLen);
		public abstract int Read(byte[] data, int nMaxLen);
		public abstract int Seek(int pos);
		public abstract int Tell();
		public byte[] m_buffer = new byte[512];
		public long m_hMP4File = 0;
		public long m_nMdatPos = 0;
	};

	static public class BaseRecordFile extends RecordFile
	{
		public int Open(int nIndex, Activity activity, String strName, int nFileType)
		{
			File path = new File(strDirector);
			if (!path.exists())
			{
				path.mkdirs();
			}
			//m_hFile = carrecorder.FileCreate();
			//carrecorder.FileOpen(m_hFile, carrecorder.OPENFLAG_OPENALWAYS | carrecorder.OPENFLAG_WRITE | carrecorder.OPENFLAG_READ, strDirector + strName + ".mp4");
			return  0;
		}

		@Override
		public int Open(int nIndex, Activity activity, String strName, int nFileType, int channel) {
			File path ;
			if(channel == 0)
				path = new File(strDirector+"front/");
			else
				path = new File(strDirector+"back/");
			if(path==null){
				Log.e(" path","录音 path==null");
				return 0;
			}
			if (!path.exists())
			{
				path.mkdirs();
				Log.d("path",path.getPath());
			}
			m_hFile = carrecorder.FileCreate();
			carrecorder.FileOpen(m_hFile, carrecorder.OPENFLAG_OPENALWAYS | carrecorder.OPENFLAG_WRITE | carrecorder.OPENFLAG_READ, path.getPath()+"/" + strName + ".mp4");
			return  0;
		}

		public int Write(byte[] data, int pos, int nWriteLen, int nTotalWriteLen, int nTotalLen)
		{
			return carrecorder.FileWrite(m_hFile, data, nWriteLen);
		}

		public int Read(byte[] data, int nMaxLen)
		{
			return carrecorder.FileRead(m_hFile, data, nMaxLen);
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

	static public class DocumentRecordFile extends RecordFile
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

			DocumentFile file = m_docDir.createFile(GetDescString(nFileType), strName + GetTypeString(nFileType));
			if(file == null)
			{
				file = m_docDir.findFile(strName);
				if(file == null)
				{
					m_error = -1;
					return -1;
				}
			}

			Uri uriFile = file.getUri();
			try
			{
				m_nIndex = nIndex;
				m_descriptor = activity.getContentResolver().openFileDescriptor(uriFile, "rw");
				m_fd = m_descriptor.getFileDescriptor();
				m_fos = new FileOutputStream(m_fd);
				m_channel = m_fos.getChannel();

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

		@Override
		public int Open(int nIndex, Activity activity, String strName, int nFileType, int channel) {
			return 0;
		}

		public int Write(byte[] data, int pos, int nWriteLen, int nTotalWriteLen, int nTotalLen)
		{
			try
			{
				long lPos = m_channel.position();
				if(m_nMdatPos > 0)
				{
					if(lPos >= m_nMdatPos)
					{
						if(m_nPos == 0)
						{
							m_nPos = lPos;
						}
						else if(m_nPos != lPos)
						{
							if(m_nIndex == 1)
							{
								Log.d("oslog", "1:" + lPos);
							}
						}
						m_nPos += nWriteLen;
						if(nTotalWriteLen == 0 && nWriteLen == 512)
						{
							if(lPos != pos)
							{
								if(m_nIndex == 1)
								{
									Log.d("oslog", "" + lPos + "****" + pos);
								}
							}
							//int type = (data[4] & 0x1F);
							//Log.d("oslog", "2:" + nTotalWriteLen + "，" + nTotalLen + "---------" + type);
							//if(type != 5 && type != 1)
							//{
							//	Log.d("oslog", "Error");
							//}
						}
						else
						{
							//Log.d("oslog", "2:" + nTotalWriteLen + "，" + nTotalLen);
						}
					}
					else
					{
						//Log.d("oslog", "3:" + pos + "+" + nWriteLen);
					}
				}
				m_fos.write(data, 0, nWriteLen);
			}
			catch (FileNotFoundException e)
			{
				return 0;
			}
			catch (Exception e)
			{
				return 0;
			}
			return nWriteLen;
		}

		public int Read(byte[] data, int nMaxLen)
		{
			return 0;
		}

		public int Seek(int pos)
		{
			try
			{
				m_fos.flush();
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
				m_fos.close();
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
		FileOutputStream m_fos;
		int m_nIndex = 0;
	}

	static public DocumentFile m_docDir = null;
	static public int m_error = 0;
	static public RecordFile[] m_recFile = null;
	static public String m_strUri = "";
	static public String m_strPath = "";
	static public int m_nFileTime = 30;
	static public int m_nFileSize = 1024;
    static public int m_bEnablePreRecord = 0;
    static public int m_bEnablePostRecord = 0;
    static public int m_nPreRecordTime = 10;
    static public int m_nPostRecordTime = 10;

    static public String FindType(String strFileName)
	{
		String strType = "";
		for(int i = strFileName.length() - 1; i >= 0; i--)
		{
			if(strFileName.charAt(i) == '.')
			{
				strType = strFileName.substring(i, strFileName.length());
				return strType;
			}
		}
		return "";
	}

	static public int Parser(String strFileName)
	{
		int nFileType = 0;
		int nMediaType = 0;
		String strType = FindType(strFileName);
		int nTypeSize = 0;
		if(strType.compareTo(".mp4") == 0)
		{
			nFileType = 0;
			nMediaType = 0;
			nTypeSize = 3;
		}
		else if(strType.compareTo(".jpeg") == 0)
		{
			nFileType = 1;
			nMediaType = 0;
			nTypeSize = 4;
		}
		else if(strType.compareTo(".aac") == 0)
		{
			nFileType = 2;
			nMediaType = 0;
			nTypeSize = 3;
		}
		else
		{
			return 0;
		}

		int pos = 0;
		int state = 0;
		String strDeviceID = "";
		String strTime = "";
		String strIndex = "";
		String strText = "";
		for(int i = 0; i < strFileName.length() - nTypeSize; i++)
		{
			if(strFileName.charAt(i) == '_' || strFileName.charAt(i) == '.')
			{
				strText = strFileName.substring(pos, i);
				switch (state)
				{
					case 0:
					{
						strDeviceID = strText;
						state = 1;
					}
					break;
					case 1:
					{
						strTime = strText;
						state = 2;
					}
					break;
					case 2:
					{
						strIndex = strText;
					}
					break;
				}
				pos = (i + 1);
			}
		}

		if(strIndex.isEmpty())
        {
            strIndex = "0";
        }

		if(carrecorder.CheckID(strDeviceID) != 0)
        {
            return -1;
        }

        if(carrecorder.CheckNumber(strTime) != 0)
        {
            return -1;
        }

        if(carrecorder.CheckNumber(strIndex) != 0)
        {
            return -1;
        }

		Date date;
        try
		{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			date = formatter.parse(strTime);
		}
		catch (ParseException e)
		{
			return 0;
		}
		catch (Exception e)
		{
			return 0;
		}

		CenterDB.DBFile dbFile = new CenterDB.DBFile();
		dbFile.strDeviceID = strDeviceID;
		dbFile.strFileName = strFileName.substring(0, strFileName.length() - nTypeSize - 1);
		dbFile.nFileType = nFileType;
		dbFile.nMediaType = nMediaType;
		dbFile.nFileSize = 0;
		dbFile.nTimeStamp = date.getTime();
		CenterDB.Instant().AddFile(dbFile);
		return 0;
	}

    static public int Restore()
	{
		if(m_docDir == null)
		{
			return -1;
		}
		CenterDB.Instant().DeleteAllFile();
		DocumentFile[] file = m_docDir.listFiles();
		for(int i = 0; i < file.length; i++)
		{
			String strFileName = file[i].getName();
			Parser(strFileName);
		}
		return 0;
	}

	static public int DeleteFile(String strName)
	{
		if(m_docDir == null)
		{
			return 0;
		}
		DocumentFile file = m_docDir.findFile(strName);
		if(file == null)
		{
			return 0;
		}
		file.delete();
		return 0;
	}

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

		Uri uri = Uri.parse(RecordFileFactory.m_strUri);
		return LoadDirector(activity, uri);
	}

	static public int Initial(Activity activity, int nMaxFileCount)
	{
		LoadDirector(activity);
		m_recFile = new RecordFile[nMaxFileCount];
		return 0;
	}

	static int AddFile(RecordFile recFile, Activity activity, String strName, int nFileType)
	{
		for(int i = 0; i < m_recFile.length; i++)
		{
			if(m_recFile[i] == null)
			{
				int res = recFile.Open(i, activity, strName, nFileType);
				if(res != 0)
				{
					return -1;
				}
				m_recFile[i] = recFile;
				return i;
			}
		}
		return -1;
	}
	static int AddFile(RecordFile recFile, Activity activity, String strName, int nFileType,int channel)
	{
		for(int i = 0; i < m_recFile.length; i++)
		{
			if(m_recFile[i] == null)
			{
				int res = recFile.Open(i, activity, strName, nFileType,channel);
				if(res != 0)
				{
					return -1;
				}
				m_recFile[i] = recFile;
				return i;
			}
		}
		return -1;
	}
	static int DeleteFile(int nIndex)
	{
		if(m_recFile[nIndex] != null)
		{
			m_recFile[nIndex].Close();
			m_recFile[nIndex] = null;
		}
		return 0;
	}

	public RecordFileFactory(int size)
	{

	}


}
