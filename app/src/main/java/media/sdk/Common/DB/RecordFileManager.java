package media.sdk.Common.DB;

import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import media.sdk.Common.Record.RecordFileFactory;

public class RecordFileManager implements Runnable
{

	public RecordFileManager()
	{

	}

	final static int RECORD_MESSAGE_DELETE_FILES = 0;

	public interface RecordMessageObserver
	{
		public abstract void OnStep(RecordMessage message);
		public abstract void OnFinish(RecordMessage message);
	}

	public abstract class RecordMessage
	{
		int m_type;
		RecordMessageObserver m_observer;
	}

	public class RecordMessageDeleteFiles extends RecordMessage
	{
		public RecordMessageDeleteFiles(RecordMessageObserver observer)
		{
			m_observer = observer;
			m_type = RECORD_MESSAGE_DELETE_FILES;
		}
		public ArrayList<CenterDB.DBFile> lstFile = new ArrayList<>();
		public String m_strFileDeleting = "";
		public int m_nPercent;
	}

	synchronized public void AddMessage(RecordMessage message)
	{
		m_queueMessage.add(message);
	}

	synchronized public RecordMessage GetMessage()
	{
		if(m_queueMessage.size() <= 0)
		{
			return null;
		}
		RecordMessage message = m_queueMessage.poll();
		return message;
	}

	Handler m_recordMsgHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{

			switch (msg.what)
			{
				case 0:
				{
					RecordMessageDeleteFiles message = (RecordMessageDeleteFiles)msg.obj;
					if(message.m_observer != null)
					{
						message.m_observer.OnFinish(message);
					}
				}
				case 1:
				{
					RecordMessageDeleteFiles message = (RecordMessageDeleteFiles)msg.obj;
					if(message.m_observer != null)
					{
						message.m_observer.OnStep(message);
					}
				}
			}
		}
	};

	public int HandleRecordFile()
	{
		RecordMessage message = GetMessage();
		if(message == null)
		{
			return 0;
		}

		switch (message.m_type)
		{
			case RECORD_MESSAGE_DELETE_FILES:
				{
					RecordMessageDeleteFiles msg = (RecordMessageDeleteFiles)message;
					for(int i = 0; i < msg.lstFile.size(); i++)
					{
						CenterDB.DBFile file = msg.lstFile.get(i);
						CenterDB.Instant().DeleteFile(file.strFileName);
						RecordFileFactory.DeleteFile(file.strFileName + "." + file.GetFileType());
						msg.m_strFileDeleting = file.strFileName + "." + file.GetFileType();
						msg.m_nPercent = (i + 1) * 100 / msg.lstFile.size();
						Message m = new Message();
						m.what = 1;
						m.obj = message;
						m_recordMsgHandler.sendMessage(m);
					}
					Message m = new Message();
					m.what = 0;
					m.obj = message;
					m_recordMsgHandler.sendMessage(m);
				}
				break;
			default:
				break;
		}
		return 1;
	}

	public void run()
	{
		while(!m_isEnd)
		{
			int nActionCount = HandleRecordFile();
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

	int Start()
	{
		m_isRunning = true;
		m_isEnd = false;
		m_thread = new Thread(this);
		m_thread.start();
		return 0;
	}

	public int End()
	{
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

	public int LoadFileList(int nFileType)
	{
		if(nFileType == -1)
		{
			return CenterDB.Instant().LoadFileList(m_lstFile);
		}
		CenterDB.Instant().LoadFileList(nFileType, m_lstFile);
		return 0;
	}

	public int LoadFileList()
	{
		CenterDB.Instant().LoadFileList(m_lstFile);
		return 0;
	}

	public int DeleteSelectFiles(RecordMessageObserver observer)
	{
		RecordMessageDeleteFiles msg = new RecordMessageDeleteFiles(observer);
		for(int i = 0; i < m_lstFile.size(); i++)
		{
			CenterDB.DBFile file = m_lstFile.get(i);
			if(file.bSelected)
			{
				msg.lstFile.add(file);
			}
		}
		if(msg.lstFile.size() > 0)
		{
			AddMessage(msg);
		}
		return 0;
	}

	public boolean hasSelectedFile()
	{
		for(int i = 0; i < m_lstFile.size(); i++)
		{
			CenterDB.DBFile file = m_lstFile.get(i);
			if(file.bSelected)
			{
				return true;
			}
		}
		return false;
	}

	public int UnSelectAllFile(boolean bSelected)
	{
		for(int i = 0; i < m_lstFile.size(); i++)
		{
			CenterDB.DBFile file = m_lstFile.get(i);
			file.bSelected = bSelected;
		}
		return 0;
	}

	public static RecordFileManager Instant()
	{
		return m_recordFile;
	}

	public static RecordFileManager Initial()
	{
		m_recordFile = new RecordFileManager();
		m_recordFile.Start();
		return m_recordFile;
	}

	public static int Clean()
	{
		if(m_recordFile != null)
		{
			m_recordFile.End();
			m_recordFile = null;
		}
		return 0;
	}

	private boolean m_isRunning = false;
	private boolean m_isEnd = true;
	private Thread m_thread = null;
	public Queue<RecordMessage> m_queueMessage = new LinkedList<RecordMessage>();
    public ArrayList<CenterDB.DBFile> m_lstFile = new ArrayList<>();
	public static RecordFileManager m_recordFile = null;
}
