package media.sdk.Common;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;


public class BufferIn
{
	public BufferIn(int nBufferSize)
	{
		m_pBuffer = new byte[nBufferSize];
	}
	
	public int FillBuffer(byte[] buf, int size)
	{
		for(int i = 0; i < size; i++)
		{
			m_pBuffer[i] = buf[i];
		}
		m_nPos = 0;
		return 0;
	}

	public int FillBuffer(byte[] buf, int pos, int size)
	{
		for(int i = 0; i < size; i++)
		{
			m_pBuffer[i] = buf[pos + i];
		}
		m_nPos = 0;
		return 0;
	}

	static public int byteToInt(byte[] b) 
	{   
		  
        int mask = 0xff;   
        int temp = 0;   
        int n = 0;   
        for(int i = 3; i >= 0; i--)
        {   
        	n <<= 8;   
        	temp = b[i] & mask;   
        	n |= temp;   
        }   
        return n;   
	}  
	static public short byteToShort(byte[] b) 
	{   
		
		
        int mask = 0xff;   
        int temp = 0;   
        short n = 0;   
        for(int i = 1; i >= 0; i--)
        {   
        	n <<= 8;   
        	temp = b[i] & mask;   
        	n |= temp;   
        }   
        return n;   
	}
	static public float byteToFloat(byte[] b) 
	{ 
        // 4 bytes
        int accum = 0; 
        for ( int shiftBy = 0; shiftBy < 4; shiftBy++ ) { 
                accum |= (b[shiftBy] & 0xff) << shiftBy * 8; 
        } 
        return Float.intBitsToFloat(accum); 
	}
	static byte[] GetBytesFromByteBuffer(byte[] pBuffer, int nStartPos, int nLen)
	{
		byte[] pBufferTemp = new byte[nLen];
		for(int i = 0; i < nLen; i++)
		{
			pBufferTemp[i] = pBuffer[nStartPos + i];
		}
		return pBufferTemp;
	}
	
	public BufferIn()
	{
		m_nPos = 0;
	}		
	
	public int GetIntFromByteBuffer()
	{
		int nValue = byteToInt(GetBytesFromByteBuffer(m_pBuffer, m_nPos, 4));
		m_nPos += 4;
		return nValue;
	}
	
	public byte[] GetBufferFromByteBuffer(int nLen)
	{
		byte[] buf = GetBytesFromByteBuffer(m_pBuffer, m_nPos, nLen);
		m_nPos += nLen;
		return buf;
	}
	
	public short GetShortFromByteBuffer()
	{
		short nValue = byteToShort(GetBytesFromByteBuffer(m_pBuffer, m_nPos, 2));
		m_nPos += 2;
		return nValue;
	}

	public byte GetbyteFromByteBuffer()
	{
		byte byValue = m_pBuffer[m_nPos];
		m_nPos += 1;
		return byValue;
	}
	public float GetfloatFromByteBuffer()
	{
		float fValue = byteToFloat(GetBytesFromByteBuffer(m_pBuffer, m_nPos, 4));
		m_nPos += 4;
		return fValue;
	}	
	public char byteToChar(byte[] b)
	{
		int s=0;
		if(b[1] > 0)
		{
			s += b[1];
		}
		else
		{
			s += 256 + b[1];

		}	
		s *= 256;
		if(b[0]>0)
		{
			s+=b[0];
		}
		else
		{
			s+=256+b[0];
		}
		char ch=(char)s;
		return ch;
	}
	
	String GetCharsFromByteBuffer(byte[] pBuffer, int nMaxLen)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int i = 0;
		while(true)
		{
			if(i >= nMaxLen)
			{
				break;
			}
			byte high = m_pBuffer[m_nPos + i];
			if(high == 0)
			{
				break;
			}
		    baos.write(high); 
		    i += 1;
		}  
		String str = null;
		 try   
		 { 
			 str = baos.toString("UTF-8");
		 } 
		 catch   (UnsupportedEncodingException ex)   
		 { 
	           
		 }
		 finally
		 { 
			 if(nMaxLen <= 0)
			 {
				 m_nPos += i + 1;
			 }
			 else
			 {
				 m_nPos += nMaxLen;				 
			 }
			 return str;
		 } 

	}

	public String GetStringFromByteBuffer(int nMaxLen)
	{
		String strValue = GetCharsFromByteBuffer(m_pBuffer, nMaxLen);
		return strValue;
	}	

	public int Reset()
	{
		m_nPos = 0;
		return 0;
	}
	
	public int Move(int pos)
	{
		m_nPos = pos;
		return 0;
	}
	public byte[] m_pBuffer = null;
	public int m_nPos;
}
