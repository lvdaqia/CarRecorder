package media.sdk.Common;

public class FrameMemoryFactory
{
	public class CMemory
	{
		public byte[] m_buffer = null;
		public int m_size = 0;
		public boolean m_bUsed = false;
		int Create(int size)
		{
			m_buffer = new byte[size];
			return 0;
		}
		
		int Delete()
		{
			m_buffer = null;
			return 0;
		}
	}
	
	public FrameMemoryFactory(int size)
	{
		m_size = size;
	}
	
	public int Create(int nMemoryCount)
	{
		m_nMemoryCount = nMemoryCount;
		m_memory = new CMemory[m_nMemoryCount];
		for(int i = 0; i < m_nMemoryCount; i++)
		{
			m_memory[i] = new CMemory();
			m_memory[i].Create(m_size);
		}
		return 0;
	}
	
	public int Delete()
	{
		for(int i = 0; i < m_nMemoryCount; i++)
		{
			m_memory[i].Delete();
		}			
		m_nMemoryCount = 0;
		m_memory = null;
		return 0;
	}
	
	public int GetMemory()
	{
		for(int i = 0; i < m_nMemoryCount; i++)
		{
			if(!m_memory[i].m_bUsed)
			{
				m_memory[i].m_bUsed = true;
				return i;
			}
		}
		return -1;
	}
	
	public int FreeMemory(int nIndex)
	{
		if(nIndex < 0 || nIndex >= m_nMemoryCount)
		{
			return 0;
		}
		if(!m_memory[nIndex].m_bUsed)
		{
			return 0;
		}
		m_memory[nIndex].m_bUsed = false;			
		return 0;
	}
	
	public int FreeAllMemory()
	{
		for(int i = 0; i < m_nMemoryCount; i++)
		{
			m_memory[i].m_bUsed = false;
		}
		return 0;
	}
	
	public CMemory[] m_memory = null;
	private int m_nMemoryCount;
	private int m_size = 0;
	
};

