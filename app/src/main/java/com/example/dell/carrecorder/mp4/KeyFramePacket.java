package com.example.dell.carrecorder.mp4;

/**
 * 关键帧数据
 * Created by dell on 9/12/2017.
 */
public class KeyFramePacket extends NalPacket {
    public KeyFramePacket(byte[] sps, byte[] pps, int headerLength) {
        super(sps, pps, headerLength);
    }
}
