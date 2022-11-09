package media.sdk.Common;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import java.lang.reflect.Method;

public class UiUtil
{
	public UiUtil()
	{

	}
	//获取虚拟按键的高度
	public static int getNavigationBarHeight(Context context)
	{
		int result = 0;
		if (hasNavBar(context))
		{
			Resources res = context.getResources();
			int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
			if (resourceId > 0)
			{
				result = res.getDimensionPixelSize(resourceId);
			}
		}
		return result;
	}

	/**
	 * 检查是否存在虚拟按键栏
	 *
	 * @param context
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public static boolean hasNavBar(Context context)
	{
		Resources res = context.getResources();
		int resourceId = res.getIdentifier("config_showNavigationBar", "bool", "android");
		if (resourceId != 0)
		{
			boolean hasNav = res.getBoolean(resourceId);
			// check override flag
			String sNavBarOverride = getNavBarOverride();
			if ("1".equals(sNavBarOverride))
			{
				hasNav = false;
			}
			else if ("0".equals(sNavBarOverride))
			{
				hasNav = true;
			}
			return hasNav;
		}
		else
		{
			// fallback
			return !ViewConfiguration.get(context).hasPermanentMenuKey();
		}
	}

	/**
	 * 判断虚拟按键栏是否重写
	 *
	 * @return
	 */
	private static String getNavBarOverride()
	{
		String sNavBarOverride = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
		{
			try
			{
				Class c = Class.forName("android.os.SystemProperties");
				Method m = c.getDeclaredMethod("get", String.class);
				m.setAccessible(true);
				sNavBarOverride = (String) m.invoke(null, "qemu.hw.mainkeys");
			}
			catch (Throwable e)
			{
			}
		}
		return sNavBarOverride;
	}

	//获取屏幕原始尺寸高度，包括虚拟功能键高度
	public static int getDpi(Context context)
	{
		int dpi = 0;
		WindowManager windowManager = (WindowManager)
				context.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		@SuppressWarnings("rawtypes")
		Class c;
		try
		{
			c = Class.forName("android.view.Display");
			@SuppressWarnings("unchecked")
			Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
			method.invoke(display, displayMetrics);
			dpi = displayMetrics.heightPixels;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return dpi;
	}

	public static int getScreenWidth(Context context)
	{
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		return dm.widthPixels;
	}

	//获取屏幕高度 不包含虚拟按键=
	public static int getScreenHeight(Context context)
	{
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		return dm.heightPixels;
	}

	public static int getBottomStatusHeight(Context context)
	{
		int totalHeight = getDpi(context);
		int contentHeight = getScreenHeight(context);
		return totalHeight - contentHeight;
	}
};