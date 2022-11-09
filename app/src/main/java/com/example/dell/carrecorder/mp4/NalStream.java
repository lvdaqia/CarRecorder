package com.example.dell.carrecorder.mp4;

import android.util.Log;

import com.example.dell.carrecorder.mp4.joggle.SteamType;

import java.util.Random;


/**
 * nal流
 */
public abstract class NalStream extends FlowStream<NalPacket> {


    private int ssrc = new Random().nextInt();
    private RtcpPacket rtcpPacket;

    /**
     * 创建NalSteam
     */
    public NalStream(SteamType type) {
        super(type);
        rtcpPacket = new RtcpPacket(ssrc);
    }


    public int getSSRC() {
        return ssrc;
    }

    public RtcpPacket getRtcpPacket() {
        return rtcpPacket;
    }

    /**
     * 输出 nal 数据
     *
     * @param packet
     */
    protected void pushNalPacket(NalPacket packet) {
        Log.d("pushNalPacket","pushNalPacket");
        if (pushListener != null) {
            pushListener.onStreamDataPush(packet);
        }
    }

}
