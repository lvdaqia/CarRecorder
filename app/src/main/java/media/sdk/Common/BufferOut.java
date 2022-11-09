package media.sdk.Common;

import java.io.UnsupportedEncodingException;

public class BufferOut
{
	
	public BufferOut(int size)
	{
		m_pBuffer = new byte[size];
		m_nPos = 0;
	}		
	
	public void int2byte(int n, byte buf[], int offset) 
	{
		buf[offset + 3] = (byte) (n >> 24);
		buf[offset + 2] = (byte) (n >> 16);
		buf[offset + 1] = (byte) (n >> 8);
		buf[offset] = (byte) n;
	}

	public void short2byte(int n, byte buf[], int offset) 
	{
		buf[offset + 1] = (byte) (n >> 8);
		buf[offset] = (byte) n;
	}

    public void float2byte(float x, byte[] bb, int index) 
    {   
        int l = Float.floatToIntBits(x);  
        for (int i = 0; i < 4; i++) 
        {  
            bb[index + i] = new Integer(l).byteValue();  
            l = l >> 8;  
        }  
    }

    public void SetByteArrayToByteBuffer(byte[] value)
	{
		for(int i = 0; i < value.length; i++)
		{
			m_pBuffer[m_nPos++] = value[i];
		}
	}

	public int Reset()
	{
		m_nPos = 0;
		return 0;
	}

	public void SetByteArrayToByteBuffer(byte[] value, int offset, int length)
	{
		System.arraycopy(value, offset, m_pBuffer, m_nPos, length);
		m_nPos += length;
	}
	
	public void SetIntToByteBuffer(int nValue)
	{
		int2byte(nValue, m_pBuffer, m_nPos);
		m_nPos += 4;
	}
	
	public void SetShortToByteBuffer(short nValue)
	{
		short2byte(nValue, m_pBuffer, m_nPos);
		m_nPos += 2;
	}

	public void SetbyteToByteBuffer(byte nValue)
	{
		m_pBuffer[m_nPos] = nValue;
		m_nPos += 1;

	}
	
	public void SetfloatToByteBuffer(float fValue)
	{
		float2byte(fValue, m_pBuffer, m_nPos);
		m_nPos += 4;
	}	

	public void SetStringToByteBuffer(String strValue, int nMaxLen)
	{
		byte[] pBuf = null;
		
		try   
		{ 
			pBuf = strValue.getBytes("UTF-8");
		} 
		catch   (UnsupportedEncodingException ex)   
		{ 
	           
		}
		finally
		{ 
			for(int i = 0; i < pBuf.length; i++)
			{
				m_pBuffer[m_nPos + i] = pBuf[i];
			}
			m_pBuffer[m_nPos + pBuf.length] = 0;
			if(nMaxLen == 0)
			{
				m_nPos += pBuf.length + 1;
			}
			else
			{
				m_nPos += nMaxLen;
			}
		}
	}
	
	public byte[] m_pBuffer = null;
	public int m_nPos;
}
