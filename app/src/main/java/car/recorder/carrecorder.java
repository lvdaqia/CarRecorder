package car.recorder;


import media.sdk.Common.Playback.PlaybackFileFactory;
import media.sdk.Common.Record.RecordFileFactory;

public class carrecorder
{
    static final public int AUDIOTYPE_PCM = 0;
    static final public int AUDIOTYPE_G711 = 1;
    static final public int AUDIOTYPE_AAC = 2;

    static public carrecorder m_carrecorder = new carrecorder();
    static private int m_bLoaded = 0;

    static
    {
        try
        {
            System.loadLibrary("carrecorder");
            m_bLoaded = 1;
        }
        catch(UnsatisfiedLinkError e)
        {
            e.printStackTrace(System.out);
            m_bLoaded = 0;
        }
    }

    static public String m_strDeviceID = "YJ";
    static public int m_nVideoEncodeType[] = new int[2];
    static public int m_nBitRate[] = new int[2];
    static public int m_nWidth[] = new int[2];
    static public int m_nHeight[] = new int[2];
    static public int m_nFrameRate[] = new int[2];
    static public int   m_width = 1920;
    static public int m_height = 1080;
    static public int m_bOsdTimeShow = 0;
    static public int m_bOsdLocationShow = 0;
    static public int m_bOsdDeviceIDShow = 0;
    static public int m_bOsdResolutionShow = 0;
    static public int m_nOsdXPos = 1;
    static public int m_nOsdYPos = 1;
    static public int m_nCropXOffset = 0;
    static public int m_nCropYOffset = 0;
    static public byte[] m_fft = null;
    static public int m_nLocationType = 0;
    static public double m_latitude = 0.0;    //获取纬度信息
    static public double m_longitude = 0.0;    //获取经度信息
    static public int m_nAudioCaptureFlag = 0;

    static public int CheckID(String strDeviceID)
    {
        if(strDeviceID.length() != 8)
        {
            return -1;
        }
        for (int i = 0; i < strDeviceID.length(); i++)
        {
            char c = strDeviceID.charAt(i);
            switch (c)
            {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                    break;
                default:
                    return -1;
            }
        }

        return 0;
    }

    static public int CheckNumber(String strNumber)
    {
        for (int i = 0; i < strNumber.length(); i++)
        {
            char c = strNumber.charAt(i);
            switch (c)
            {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
                default:
                    return -1;
            }
        }
        return 0;
    }


    public int OnFileWrite(int type, int hFile, int pos, int nWriteLen, int nTotalWriteLen, int nTotalLen)
    {
        if(type != 0)
        {
            return 0;
        }
        int nIndex = (int)hFile;
        carrecorder.Mp4FWGetFileData(RecordFileFactory.m_recFile[nIndex].m_hMP4File, RecordFileFactory.m_recFile[nIndex].m_buffer, nWriteLen);
        int res = RecordFileFactory.m_recFile[nIndex].Write(RecordFileFactory.m_recFile[nIndex].m_buffer, pos, nWriteLen, nTotalWriteLen, nTotalLen);
        return res;
    }

    public int OnFileRead(int type, int hFile, int nMaxLen)
    {
        if(type == 0)
        {
            return 0;
        }
        int nIndex = (int)hFile;
        byte[] data = PlaybackFileFactory.m_playbackFile[nIndex].Read(nMaxLen);
        if(data == null)
        {
            return 0;
        }
        carrecorder.Mp4FRSetFileData(PlaybackFileFactory.m_playbackFile[nIndex].m_hMP4File, data, nMaxLen);
        return nMaxLen;
    }

    public int OnFileSeek(int type, int hFile, int pos)
    {
        int res;
        int nIndex = (int)hFile;
        if(type == 0)
        {
            res = RecordFileFactory.m_recFile[nIndex].Seek(pos);
        }
        else
        {
            res = PlaybackFileFactory.m_playbackFile[nIndex].Seek(pos);
        }

        return res;
    }

    public int OnFileTell(int type, int hFile)
    {
        int res;
        int nIndex = (int) hFile;
        if(type == 0)
        {
            res = RecordFileFactory.m_recFile[nIndex].Tell();
        }
        else
        {
            res = PlaybackFileFactory.m_playbackFile[nIndex].Tell();
        }
        return res;
    }

    static public int CarRecordInitial()
    {
        Mp4Initial(m_carrecorder);
        return 0;
    }

    static public native int yuv422888tonv21(byte[] yuv422888, int width, int height);
    static public native int nv21toyv12(byte[] nv21, byte[] yv12, int width, int height, int bUV);
    static public native int yv12tonv21(byte[] yv12, byte[] uv12, int width, int height, int bUV);
    static public native int nv21tonv12(byte[] nv21, byte[] nv12, int width, int height);
    static public native int rgb32toyv12(byte[] rgb, byte[] yv12, int width, int height, int bInv);
    static public native int yv12torgb32(byte[] yv12, int[] rgb, int width, int height, int bInv);
    static public native int ylum(byte[] yv12, int width, int height, int offset);
    static public native long yv12scalercreate(int nDstWidth, int nDstHeight, int nSrcWidth, int nSrcHeight);
    static public native int yuv420pscale(long hScaler, byte[] src, byte[] dst);
    static public native int yv12scalerdelete(long hScaler);

    static public native long FontCreateByFile(String szTTFPath, int nMaxCharWidth, int nMaxCharHeigh, int nMaxCharCount);
    static public native long FontCreateByMemory(byte[] ttf, int size, int nMaxCharWidth, int nMaxCharHeigh, int nMaxCharCount);
    static public native int FontDelete(long hFont);
    static public native int FontGetImageWidth(long hFont);
    static public native int FontGetImageHeight(long hFont);
    static public native int FontGetImageData(long hFont, byte[] pData);
    static public native int FontLoadImageA(long hFont, byte[] szText, int offset, int nTextLen, float fAngle, int nCharWidth, int nCharHeight, int yOffset);
    static public native int FontLoadImageW(long hFont, byte[] szText, int offset, int nTextLen, float fAngle, int nCharWidth, int nCharHeight, int yOffset);
    static public native int FontDrawImage(long hFont, byte[] imageData, int width, int height, int xScale, int yScale, int mode);
    static public native int FontDrawBorder(byte[] imageData, int width, int height);

    static public native int Mp4Initial(carrecorder carrecorder);

    static public native long Mp4FWCreate(int hFile);
    static public native int Mp4FWDelete(long hMp4File);
    static public native int Mp4FWClose(long hMp4File);
    static public native int Mp4FWOpen(long hMp4File, int nEncodeType, int nWidth, int nHeight, int nSampleRate, int nUpdateIndexCount, int nMaxIndexCount);
    static public native int Mp4FWAddAudioData(long hMp4File, int nAudioType, byte[] data, int offset, int len, int nTimeStamp);
    static public native int Mp4FWAddVideoData(long hMp4File, byte[] data, int offset, int len, int nTimeStamp);
    static public native int Mp4FWGetFileData(long hMp4File, byte[] data, int len);
    static public native int Mp4FWGetFileSize(long hMp4File);
    static public native int Mp4FWGetMdatPos(long hMp4File);
    static public native int Mp4FWGetIndexCount(long hMp4File);
    static public native int Mp4FWUpdate(long hMp4File);

    static public native long Mp4FRCreate(int hFile);
    static public native int Mp4FRDelete(long hMp4File);
    static public native int Mp4FROpen(long hMp4File);
    static public native int Mp4FRClose(long hMp4File);
    static public native int Mp4FRGetDuration(long hMp4File);
    static public native int Mp4FRSeek(long hMp4File, int nTimeOffset);
    static public native int Mp4FRSetFileData(long hMp4File, byte[] data, int size);
    static public native int Mp4FRReadFrame(long hMp4File, int[] trackType, int[] codecID, int[] duration, byte[] data, int offset, int size);

    static public final int OPENFLAG_READ = 0x1;
    static public final int OPENFLAG_WRITE = 0x2;
    static public final int OPENFLAG_OPENALWAYS = 0x4;

    static public final int PFILESEEK_CUR = 0;
    static public final int PFILESEEK_SET = 1;
    static public final int PFILESEEK_END = 2;

    static public native long FileCreate();
    static public native int FileDelete(long hFile);
    static public native int FileOpen(long hFile, int flag, String szPath);
    static public native int FileClose(long hFile);
    static public native int FileWrite(long hFile, byte[] data, int len);
    static public native int FileRead(long hFile, byte[] data, int nMaxLen);
    static public native int FileSeek(long hFile, int pos, int flag);
    static public native int FileTell(long hFile);
    static public native int FileFlush(long hFile);

    static public native long RingBufferCreate(int nBufferSize);
    static public native int RingBufferDelete(long hRingBffer);
    static public native int RingBufferPush(long hRingBuffer, byte[] header, int nHeaderOffset, int nHeaderSize, byte[] data, int nDataOffset, int nDataSize);
    static public native int RingBufferPop(long hRingBffer, byte[] data, int nDataOffset, int nDataSize);
    static public native int RingBufferPopNull(long hRingBffer);
    static public native int RingBufferFront(long hRingBffer, byte[] data, int nDataOffset, int nDataSize);

    static public native long AECreate(int nType, int param, int nSampleRate, int nChannels, int nBufSize);
    static public native int AEDelete(long ae);
    static public native int AEEncode(long ae, byte[] dataIn, int offset, int size, byte[] dataOut);

    static public native long ADCreate(int nType, int param, int nSampleRate, int nChannels, int nBufSize);
    static public native int ADDelete(long ad);
    static public native int ADDecode(long ad, byte[] dataIn, int offsetIn, int size, byte[] dataOut, int offsetOut);

    static public native long ARCreate(int nSrcSampleRate, int nDstSampleRate);
    static public native int ARDelete(long ar);
    static public native int ARResample(long ar, byte[] dataIn, int offset, int size);
    static public native int ARGetOutput(long ar, byte[] dataOut);
}
