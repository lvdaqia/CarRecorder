package media.sdk.MediaSurfaceSdk.Opengles;

public interface CharBitmapFactory
{
    public static class CharVertex
    {
        public float vWidth = 0; //字符宽度，以图像的宽度为1.0
        public float vHeight = 0; //字符高度，以图像的高度为1.0
        public float tLeft = 0; //字符左边位置，以字符位图的宽度为1.0
        public float tRight = 0; //字符右边位置，以字符位图的高度为1.0
    }
    public abstract int Create(int textTexture);
    public abstract int Delete();
    public abstract CharVertex getCharVertex(char ch);
    public abstract int setVertexWidth(float fWidth);
    public abstract int setVertexHeight(float fHeight);
    public abstract float getVertexWidth();
    public abstract float getVertexHeight();
}
