package com.example.dell.carrecorder.mp4.joggle;

public interface IFlowPushStream<T> {

    void addVideoClient(T client);

    void addAudioClient(T client);

    void removeVideoClient(T client);

    void removeAudioClient(T client);

    void release();
}
