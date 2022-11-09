package media.sdk.MediaSurfaceSdk.Opengles;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import car.recorder.carrecorder;

public class CharBitmapFactoryCanvas implements CharBitmapFactory
{
    public static class CharVertex1
    {
    	//字符在点阵位图中的坐标
        public float left = 0;
        public float right = 0;
        public float bottom = 0;
        public float top = 0;

        //归一后的字符点阵位图中的坐标
        public float vWidth = 0;
        public float vHeight = 0;
        public float tLeft = 0;
        public float tRight = 0;
    }

	private int textureId = 0;
	private char start = '!';
	private char end = '~';
	private int size = end - start + 1;
	private int maxWidth = 0;
	private int maxHeight = 0;
	private static Paint mPaint;
	public float vertexWidth = 0.02f;
	public float vertexHeight = 0;
    private CharVertex1[] cts = new CharVertex1[size];

	public CharBitmapFactoryCanvas()
	{

	}

	public void release()
	{
		GLES20.glDeleteTextures(1, new int[] { textureId }, 0);
		textureId = 0;
	}

	public CharVertex getCharVertex(char ch)
	{
		if (ch < start || ch > end)
		{
			return null;
		}
        CharVertex vertex = new CharVertex();
		int offset = ch - start;
        vertex.tLeft = cts[offset].tLeft;
        vertex.tRight = cts[offset].tRight;
        vertex.vWidth = cts[offset].vWidth;
        vertex.vHeight = cts[offset].vHeight;
		return vertex;
	}

	private Bitmap createCharBitmap()
	{
		StringBuilder sb = new StringBuilder(size);
		for (char c = start; c <= end; c++)
		{
			sb.append(c);
		}

		float fWidth = 25.0f;
		// first, set the paint style. and get the char width and height.
		Paint p = getTexturePaint();
		Paint.FontMetrics fr = p.getFontMetrics();
		for (int i = 0; i < size; i++)
		{
			String tmp = sb.substring(i, i + 1);
			cts[i] = new CharVertex1();
			cts[i].left = maxWidth;
			cts[i].right = fWidth + maxWidth;
			cts[i].bottom = fr.bottom;
			cts[i].top = fr.top;
			maxWidth = (int) Math.ceil(cts[i].right + 5);
			maxHeight = (int) Math.ceil(Math.max(cts[i].bottom - cts[i].top, maxHeight));
		//	Log.d("Font", tmp + " Size:"  + (cts[i].bottom - cts[i].top) + "," + (cts[i].right - cts[i].left));
		}

		Bitmap bitmap = Bitmap.createBitmap(maxWidth, maxHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);

		for (int i = 0; i < size; i++)
		{
			String tmp = sb.substring(i, i + 1);
			canvas.drawText(tmp, cts[i].left, Math.abs(cts[i].top), p);
			cts[i].vWidth = vertexWidth;
			cts[i].vHeight = vertexHeight;
			cts[i].tLeft = cts[i].left * 1f / maxWidth;
			cts[i].tRight = cts[i].right * 1f / maxWidth;
		//	Log.d("Font", tmp + " Vetex:"  + (cts[i].vWidth) + "," + (cts[i].vHeight) + "," + (cts[i].tLeft) + "," + (cts[i].tRight));
		}

		canvas.save();
		canvas.restore();

		int bytes = bitmap.getByteCount();
		ByteBuffer buf = ByteBuffer.allocate(bytes);
		bitmap.copyPixelsToBuffer(buf);
		byte[] rgb = buf.array();
		carrecorder.FontDrawBorder(rgb, bitmap.getWidth(), bitmap.getHeight());
		bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(rgb));
		//saveBitmap(bitmap, "test.png");
		return bitmap;
	}

	/** for testing */
	private void saveBitmap(Bitmap bmpTemp, String name)
	{
		// save the bitmap to file.
		File file = new File("/sdcard/Download/", name);
		FileOutputStream os = null;
		try
		{
			os = new FileOutputStream(file);
			bmpTemp.compress(Bitmap.CompressFormat.PNG, 100, os);
			os.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static Paint getTexturePaint()
	{
		Paint p = new Paint();
		p.setStyle(Style.FILL);
		p.setShadowLayer(2f, 0f, 0f, Color.GRAY);
		p.setAntiAlias(true);
		p.setDither(true);
		Typeface font = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);// MONOSPACE
		p.setTypeface(font);
		p.setColor(Color.WHITE);
		p.setTextSize(40);
		p.setTextAlign(Align.LEFT);
		p.setSubpixelText(true);
		return p;
	}

	public int Create(int textureId)
	{
		if (textureId > 0)
		{
			this.textureId = textureId;
			Bitmap bitmap = createCharBitmap();
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
			bitmap.recycle();
			bitmap = null;
		}
		else
		{
			throw new RuntimeException("init TextTextureManager failed. cause texture id : " + textureId);
		}
		return 0;
	}

	public int setVertexHeight(float fHeight)
	{
		vertexHeight = fHeight;
		return 0;
	}

	public int setVertexWidth(float fWidth)
	{
		vertexWidth = fWidth;
		return 0;
	}

	public float getVertexWidth()
	{
		return vertexWidth;
	}

	public float getVertexHeight()
	{
		return vertexHeight;
	}

	public int Delete()
	{
		this.release();
		return 0;
	}

}
