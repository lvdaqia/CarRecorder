package com.example.dell.carrecorder.decompose.mp4;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Environment;

import com.azhon.jtt808.listener.OnConnectionListener;
import com.azhon.jtt808.netty.JTT808Client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class DecomposeMp4 {
    //提取的文件为什么是aac后面会说明
    private String pcmPath = Environment.getExternalStorageDirectory().getAbsoluteFile()
            .getPath() + "/demo/test.aac";

    //为什么是.h264 后面会说明
    private String mp4Path = Environment.getExternalStorageDirectory().getAbsoluteFile()
            .getPath() + "/demo/test.h264";
    private String dirPath = Environment.getExternalStorageDirectory().getAbsoluteFile()
            .getPath() + "/demo";

    private MediaExtractor mediaExtractor;
    private OnMp4VideAudioData listener;

    public DecomposeMp4(OnMp4VideAudioData listener) {
        this.listener = listener;
    }

    public void initMediaDecode() {
        String srcPath = Environment.getExternalStorageDirectory().getAbsoluteFile().getPath() + "/hh/Record/88888888_0_20150101031120817.mp4";
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdir();
        }

        File file1 = new File(pcmPath);
        File file2 = new File(mp4Path);
        try {
            if (file1.exists()) {
                file1.delete();

            }
            file1.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (file2.exists()) {
                file2.delete();
            }
            file2.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            mediaExtractor = new MediaExtractor();//此类可分离视频文件的音轨和视频轨道
            mediaExtractor.setDataSource(srcPath);//媒体文件的位置
            System.out.println("==========getTrackCount()===================" + mediaExtractor.getTrackCount());
            for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {//遍历媒体轨道，包括视频和音频轨道
                MediaFormat format = mediaExtractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio")) {//获取音频轨道
                    mediaExtractor.selectTrack(i);//选择此音频轨道
                    System.out.println("====audio=====KEY_MIME=========" + format.getString(MediaFormat.KEY_MIME));
                    System.out.println("====audio=====KEY_CHANNEL_COUNT=======" + format.getInteger(MediaFormat.KEY_CHANNEL_COUNT) + "");
                    System.out.println("====audio=====KEY_SAMPLE_RATE===========" + format.getInteger(MediaFormat.KEY_SAMPLE_RATE) + "");
                    System.out.println("====audio=====KEY_DURATION===========" + format.getLong(MediaFormat.KEY_DURATION) + "");
                    System.out.println("====audio=====getSampleFlags===========" + mediaExtractor.getSampleFlags() + "");
                    System.out.println("====audio=====getSampleTime===========" + mediaExtractor.getSampleTime() + "");
                    System.out.println("====audio=====getSampleTrackIndex===========" + mediaExtractor.getSampleTrackIndex() + "");

                    try {
                        ByteBuffer inputBuffer = ByteBuffer.allocate(100 * 1024);
                        FileOutputStream fe = new FileOutputStream(file1, true);
                        while (true) {
                            int readSampleCount = mediaExtractor.readSampleData(inputBuffer, 0);
                            if (readSampleCount < 0) {
                                break;
                            }
                            byte[] buffer = new byte[readSampleCount];
                            inputBuffer.get(buffer);
                            fe.write(buffer);
                            // listener.mp4AudioData(buffer);
                            inputBuffer.clear();
                            mediaExtractor.advance();
                        }

                        fe.flush();
                        fe.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                    }
                }

                if (mime.startsWith("video")) {
                    mediaExtractor.selectTrack(i);//选择此视频轨道
                    System.out.println("====video=====KEY_MIME===========" + format.getString(MediaFormat.KEY_MIME));
                    System.out.println("====video=====KEY_DURATION===========" + format.getLong(MediaFormat.KEY_DURATION) + "");
                    System.out.println("====video=====KEY_WIDTH===========" + format.getInteger(MediaFormat.KEY_WIDTH) + "");
                    System.out.println("====video=====KEY_HEIGHT===========" + format.getInteger(MediaFormat.KEY_HEIGHT) + "");
                    System.out.println("====video=====getSampleFlags===========" + mediaExtractor.getSampleFlags() + "");
                    System.out.println("====video=====getSampleTime===========" + mediaExtractor.getSampleTime() + "");
                    System.out.println("====video=====getSampleTrackIndex===========" + mediaExtractor.getSampleTrackIndex() + "");
                    try {
                        ByteBuffer inputBuffer = ByteBuffer.allocate(100 * 1024);
                        FileOutputStream fe = new FileOutputStream(file2, true);
                        while (true) {
                            int readSampleCount = mediaExtractor.readSampleData(inputBuffer, 0);
                            if (readSampleCount < 0) {
                                break;
                            }
                            byte[] buffer = new byte[readSampleCount];
                            inputBuffer.get(buffer);
                            fe.write(buffer);
                            //   listener.mp4VideoDta(buffer);
                            inputBuffer.clear();
                            mediaExtractor.advance();
                        }
                        fe.flush();
                        fe.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mediaExtractor.release();
            mediaExtractor = null;
        }
    }

    public interface OnMp4VideAudioData {
        void mp4VideoDta(byte[] data);

        void mp4AudioData(byte[] data);
    }
}
