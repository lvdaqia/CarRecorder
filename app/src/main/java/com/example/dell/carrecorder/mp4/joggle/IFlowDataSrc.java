package com.example.dell.carrecorder.mp4.joggle;


import com.example.dell.carrecorder.mp4.FlowStream;

public interface IFlowDataSrc {

    <T extends FlowStream> T getVideoStream();

    <T extends FlowStream> T getAudioStream();

    void startStream();

    void stopStream();

    boolean isStreamRun();

    void release();

}
