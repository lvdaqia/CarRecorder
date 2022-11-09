package media.sdk.MediaSurfaceSdk.Opengles.grafika;

/**
 * Created by Piasy{github.com/Piasy} on 28/09/2017.
 */

public class Transformation
{
    public static final Rect FULL_RECT = new Rect(0, 0, 1, 1);

    public static final int FLIP_NONE = 2001;
    public static final int FLIP_HORIZONTAL = 2002;
    public static final int FLIP_VERTICAL = 2003;
    public static final int FLIP_HORIZONTAL_VERTICAL = 2004;

    public static final int ROTATION_0 = 0;
    public static final int ROTATION_90 = 90;
    public static final int ROTATION_180 = 180;
    public static final int ROTATION_270 = 270;

    public static final int SCALE_TYPE_FIT_XY = 1001; //自适应到整个窗口，会导致图像拉升
    public static final int SCALE_TYPE_CENTER_CROP = 1002; //按比例绘制到窗口，填满整个窗口，多出的部分裁剪
    public static final int SCALE_TYPE_CENTER_INSIDE = 1003; //按比例绘制到窗口，保证窗口能显示所有图像，窗口多余部分留黑

    Rect cropRect = FULL_RECT;
    int flip = FLIP_NONE;
    int rotation = ROTATION_90;
    Size inputSize;
    Size outputSize;
    int scaleType;

    public void setCrop(Rect cropRect)
    {
        this.cropRect = cropRect;
    }

    public void setFlip(int flip)
    {
        this.flip = flip;
    }

    public void setRotation(int rotation)
    {
        this.rotation = rotation;
    }

    public void setScale(Size inputSize, Size outputSize, int scaleType)
    {
        this.inputSize = inputSize;
        this.outputSize = outputSize;
        this.scaleType = scaleType;
    }

    public static class Rect
    {
        final float x;
        final float y;
        final float width;
        final float height;
        public Rect(final float x, final float y, final float width, final float height)
        {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    public static class Size
    {
        final int width;
        final int height;
        public Size(final int width, final int height)
        {
            this.width = width;
            this.height = height;
        }
    }
}
