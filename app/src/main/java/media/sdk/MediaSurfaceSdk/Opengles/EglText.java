package media.sdk.MediaSurfaceSdk.Opengles;

import android.opengl.GLES20;
import android.text.TextUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import media.sdk.MediaSurfaceSdk.Opengles.grafika.GlUtil;

public class EglText
{
	public float vertices[];
	public short indices[];
	public float uvs[];
	public FloatBuffer vertexBuffer;
	public ShortBuffer drawListBuffer;
	public FloatBuffer uvBuffer;
	
	private int textureId;
	public CharBitmapFactory m_bmpFactory;
	private MyTexture2dProgram mProgram;

	private String textNow = "";
	int m_x = 0;
	int m_y = 0;
	int m_nCount = 2;
	int m_nIndex = 1;

	private volatile boolean sizeChanged = false;
	private volatile boolean textChanged = false;
	private volatile boolean drawFlag = false;

	public EglText(int textureId, CharBitmapFactory bmpFactory)
	{
		this.textureId = textureId;
		m_bmpFactory = bmpFactory;
	}

	public void release()
	{

	}
	
	public void setProgram(MyTexture2dProgram program)
	{
		this.mProgram = program;
	}

	private  float getTotalWidth()
    {
        float offset_x = 0.0f;
        char ch;
        int length = textNow.length();
        for(int i = 0; i < length; i++)
        {
            ch = textNow.charAt(i);
            CharBitmapFactory.CharVertex vertex = m_bmpFactory.getCharVertex(ch);
            if (vertex != null)
            {
                offset_x += m_bmpFactory.getVertexWidth();
                continue;
            }
            offset_x += m_bmpFactory.getVertexWidth();
        }
        return offset_x;
    }

	private void setupTriangles(float fXScale, float fYScale)
	{
	    float fTotalWidth = 0.0f;
	    float fTotalHeight = m_bmpFactory.getVertexHeight();
		float offset_x;
		float offset_y;
		char ch;
		int length = textNow.length();

		vertices = new float[length * 4 * 3];
		indices = new short[length * 6];
		uvs = new float[length * 8];

        fTotalWidth = getTotalWidth();
        offset_x = -1.0f + (m_x / 100.f) * (2.0f - fTotalWidth);
        offset_y = -1.0f + ((100 - m_y) / 100.f) * (2.0f - fTotalHeight * m_nCount);
		offset_y += fTotalHeight * m_nIndex;

		for(int i = 0; i < length; i++)
		{
			ch = textNow.charAt(i);
			CharBitmapFactory.CharVertex vertex = m_bmpFactory.getCharVertex(ch);
			if(vertex == null)
			{
				offset_x += m_bmpFactory.getVertexWidth();
				continue;
			}

			vertices[(i * 12) + 0] = offset_x * fXScale;
			vertices[(i * 12) + 1] = (offset_y + vertex.vHeight * fXScale)  * fYScale;
			vertices[(i * 12) + 2] = 0f;
			vertices[(i * 12) + 3] = offset_x * fXScale;
			vertices[(i * 12) + 4] = offset_y  * fYScale;
			vertices[(i * 12) + 5] = 0f;
			vertices[(i * 12) + 6] = (offset_x + vertex.vWidth * fYScale)  * fXScale;
			vertices[(i * 12) + 7] = offset_y * fYScale;
			vertices[(i * 12) + 8] = 0f;
			vertices[(i * 12) + 9] = (offset_x + vertex.vWidth * fXScale)  * fXScale;
			vertices[(i * 12) + 10] = (offset_y + vertex.vHeight * fYScale) * fYScale;
			vertices[(i * 12) + 11] = 0f;

			indices[(i * 6) + 0] = (short) (i * 4 + 0);
			indices[(i * 6) + 1] = (short) (i * 4 + 1);
			indices[(i * 6) + 2] = (short) (i * 4 + 2);
			indices[(i * 6) + 3] = (short) (i * 4 + 0);
			indices[(i * 6) + 4] = (short) (i * 4 + 2);
			indices[(i * 6) + 5] = (short) (i * 4 + 3);
			
			uvs[i * 8 + 0] = vertex.tLeft;
			uvs[i * 8 + 1] = 0f;
			uvs[i * 8 + 2] = vertex.tLeft;
			uvs[i * 8 + 3] = 1f;
			uvs[i * 8 + 4] = vertex.tRight;
			uvs[i * 8 + 5] = 1f;
			uvs[i * 8 + 6] = vertex.tRight;
			uvs[i * 8 + 7] = 0f;
			
			offset_x += vertex.vWidth;
		}
				
		//顶点缓冲
		ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
		bb.order(ByteOrder.nativeOrder());
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);

		ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
		dlb.order(ByteOrder.nativeOrder());
		drawListBuffer = dlb.asShortBuffer();
		drawListBuffer.put(indices);
		drawListBuffer.position(0);
		
		uvBuffer = ByteBuffer.allocateDirect(uvs.length * 4)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer();
		uvBuffer.put(uvs);
		uvBuffer.position(0);
		
		sizeChanged = false;
		textChanged = false;
	}

	public void draw(float[] m, float fXScale, float fYScale)
	{
		if(!drawFlag)
		{
			return;
		}
		if(textChanged || sizeChanged)
		{
		    setupTriangles(fXScale, fYScale);
		}
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		mProgram.draw(GlUtil.IDENTITY_MATRIX,
        		vertexBuffer, 0, 3, 0,
        		drawListBuffer, indices.length, 
        		GlUtil.IDENTITY_MATRIX, uvBuffer, textureId, 0);
	}

	public void setText(String text)
	{
		if(TextUtils.isEmpty(text))
		{
			textNow = text;
			drawFlag = false;
			return;
		}
		else if(TextUtils.equals(textNow, text))
		{
			return;
		}
		else
		{
            textNow = text;
			drawFlag = true;
			textChanged = true;
		}
	}

	public void setCount(int nCount)
	{
		m_nCount = nCount;
		sizeChanged = true;
	}

	public void setIndex(int nIndex)
	{
		m_nIndex = nIndex;
		sizeChanged = true;
	}
	
	/**range: [-1, 1]*/
	public void setPosition(int x, int y)
	{
        m_x = x;
        m_y = y;
		sizeChanged = true;
	}
}
