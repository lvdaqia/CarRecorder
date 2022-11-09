package media.sdk.MediaSurfaceSdk.Opengles;
import java.text.SimpleDateFormat;
import java.util.Date;

import car.recorder.carrecorder;

public class SurfaceText
{
    public int Create(int textTexture, MyTexture2dProgram mt2p, CharBitmapFactory bmpFactory)
    {
        TextTime = new EglText(textTexture, bmpFactory);
        TextTime.setProgram(mt2p);

        TextLongitude = new EglText(textTexture, bmpFactory);
        TextLongitude.setProgram(mt2p);

        TextLatitude = new EglText(textTexture, bmpFactory);
        TextLatitude.setProgram(mt2p);

        TextDeviceID = new EglText(textTexture, bmpFactory);
        TextDeviceID.setProgram(mt2p);

        TextResolution = new EglText(textTexture, bmpFactory);
        TextResolution.setProgram(mt2p);
        return 0;
    }

    public int UpdateParam()
    {
        int nCount = 0;
        int nIndex = 0;

        if(carrecorder.m_bOsdTimeShow != 0)
        {
            TextTime.setIndex(nIndex);
            nIndex++;
            nCount++;
        }

        if(carrecorder.m_bOsdLocationShow != 0 && carrecorder.m_nLocationType != 0)
        {
            TextLongitude.setIndex(nIndex);
            nIndex++;
            nCount++;

            TextLatitude.setIndex(nIndex);
            nIndex++;
            nCount++;
        }

        if(carrecorder.m_bOsdDeviceIDShow != 0)
        {
            TextDeviceID.setIndex(nIndex);
            nIndex++;
            nCount++;
        }

        if(carrecorder.m_bOsdResolutionShow != 0)
        {
            TextResolution.setIndex(nIndex);
            nIndex++;
            nCount++;
        }

        TextTime.setCount(nCount);
        TextLongitude.setCount(nCount);
        TextLatitude.setCount(nCount);
        TextDeviceID.setCount(nCount);
        TextResolution.setCount(nCount);
        return 0;
    }

    public int Draw(float[] m, float fXScale, float fYScale)
    {
        if(m_nOsdXPos != carrecorder.m_nOsdXPos ||
        m_nOsdYPos != carrecorder.m_nOsdYPos ||
        m_bOsdLocationShow != carrecorder.m_bOsdLocationShow ||
        m_bOsdTimeShow != carrecorder.m_bOsdTimeShow ||
        m_bOsdDeviceIDShow != carrecorder.m_bOsdDeviceIDShow ||
        m_bOsdResolutionShow != carrecorder.m_bOsdResolutionShow ||
        m_nLocationType != carrecorder.m_nLocationType)
        {
            m_nOsdXPos = carrecorder.m_nOsdXPos;
            m_nOsdYPos = carrecorder.m_nOsdYPos;
            m_bOsdLocationShow = carrecorder.m_bOsdLocationShow;
            m_bOsdTimeShow = carrecorder.m_bOsdTimeShow;
            m_bOsdDeviceIDShow = carrecorder.m_bOsdDeviceIDShow;
            m_bOsdResolutionShow = carrecorder.m_bOsdResolutionShow;
            m_nLocationType = carrecorder.m_nLocationType;

            UpdateParam();

            if(carrecorder.m_bOsdTimeShow != 0)
            {
                TextTime.setPosition(m_nOsdXPos, m_nOsdYPos);
            }

            if(carrecorder.m_bOsdLocationShow != 0 && carrecorder.m_nLocationType != 0)
            {
                TextLatitude.setPosition(m_nOsdXPos, m_nOsdYPos);
                TextLongitude.setPosition(m_nOsdXPos, m_nOsdYPos);
            }

            if(carrecorder.m_bOsdDeviceIDShow != 0)
            {
                TextDeviceID.setPosition(m_nOsdXPos, m_nOsdYPos);
            }

            if(carrecorder.m_bOsdResolutionShow != 0)
            {
                TextResolution.setPosition(m_nOsdXPos, m_nOsdYPos);
            }
        }

        if(carrecorder.m_bOsdTimeShow != 0)
        {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date curDate = new Date(System.currentTimeMillis());//获取当前时间
            String strTime = formatter.format(curDate);
            TextTime.setText(strTime);
            TextTime.draw(m, fXScale, fYScale);
        }

        if(carrecorder.m_bOsdLocationShow != 0 && carrecorder.m_nLocationType != 0)
        {
            TextLatitude.setText("LAT:" + carrecorder.m_latitude);
            TextLatitude.draw(m, fXScale, fYScale);
            TextLongitude.setText("LON:" + carrecorder.m_longitude);
            TextLongitude.draw(m, fXScale, fYScale);
        }

        if(carrecorder.m_bOsdDeviceIDShow != 0)
        {
            TextDeviceID.setText(carrecorder.m_strDeviceID);
            TextDeviceID.draw(m, fXScale, fYScale);
        }

        if(carrecorder.m_bOsdResolutionShow != 0)
        {
            TextResolution.setText(carrecorder.m_width + "x" + carrecorder.m_height);
            TextResolution.draw(m, fXScale, fYScale);
        }

        return 0;
    }

    public int Delete()
    {
        if (TextTime != null)
        {
            TextTime.release();
            TextTime = null;
        }
        if (TextLongitude != null)
        {
            TextLongitude.release();
            TextLongitude = null;
        }
        if (TextLatitude != null)
        {
            TextLatitude.release();
            TextLatitude = null;
        }
        if (TextDeviceID != null)
        {
            TextDeviceID.release();
            TextDeviceID = null;
        }
        if (TextResolution != null)
        {
            TextResolution.release();
            TextResolution = null;
        }
        return 0;
    }

    public int m_nOsdXPos = 1;
    public int m_nOsdYPos = 100;
    public int m_bOsdDeviceIDShow = 0;
    public int m_bOsdResolutionShow = 0;
    public int m_bOsdTimeShow = 0;
    public int m_bOsdLocationShow = 0;
    public int m_nLocationType = 0;

    public EglText TextTime;
    public EglText TextLatitude;
    public EglText TextLongitude;
    public EglText TextDeviceID;
    public EglText TextResolution;
}
