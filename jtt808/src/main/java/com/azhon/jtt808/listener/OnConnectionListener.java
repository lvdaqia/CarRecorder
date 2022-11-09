package com.azhon.jtt808.listener;

import com.azhon.jtt808.bean.JTT808Bean;
import com.azhon.jtt808.bean.TerminalParamsBean;

import java.util.List;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt808.listener
 * 文件名:    OnConnectionListener
 * 创建时间:  2020/1/2 on 22:45
 * 描述:     TODO
 *
 * @author luozhihao
 */

public interface OnConnectionListener {
    //连接成功
    int CONNECTED = 1000;
    //断开连接
    int DIS_CONNECT = 1001;
    //重连
    int RE_CONNECT = 1002;

    void onLocalConnectionSateChange(int state);

    void onConnectionSateChange(int state);

    void on905ConnectionSateChange(int state);

    //收到服务器的数据
    void receiveData(JTT808Bean jtt808Bean);

    //平台设置终端参数
    void terminalParams(List<TerminalParamsBean> params);

    /**
     * 实时监控
     *
     * @param ip         服务器ip
     * @param port       服务器端口
     * @param channelNum 逻辑通道号
     * @param dataType   数据类型  0 音视频 1 视频 2 双向对讲  3 监听   4 中心广播  5 透传
     */
    void audioVideoLive(String ip, int port, int channelNum, int dataType);

    void localAudioVideoLive(String ip, int port, int channelNum, int dataType);

    void audioVideoLive905(String ip, int port, int channelNum, int dataType);

    /**
     * 音视频实时传输控制
     *
     * @param channelNum   逻辑通道号
     * @param control      0   关闭音视频传输指令
     *                     1   切换码流（增加暂停和继续）
     *                     2   暂停该通道所有流的发送
     *                     3   恢复暂停前流的发，与暂停前的流类型一致
     *                     4   关闭双向对讲
     * @param closeAudio   0   关闭该通道上有关的音视频数据
     *                     1   只关闭该通道有关的音频，保留该通道有关的视频
     *                     2   只关闭该通道有关的视频，保留该通道有关的视频
     * @param switchStream 0   主码流
     *                     1   子码流
     */
    void audioVideoLiveControl(int channelNum, int control, int closeAudio, int switchStream);

    void audioVideoLocalLiveControl(int channelNum, int control, int closeAudio, int switchStream);
    void audioVideo905LiveControl(int channelNum, int control, int closeAudio, int switchStream);
    /**
     *
     * @param channelNum
     * @param control
     * @param closeAudio
     * @param switchStream
     */
    void localVideoList(byte[] flowNo,int channelNum, String startTIme, String stoptTIme, String alarmSign,
                        int dataType,int bitstreamype,int storageType);

    void localVideoListLocal(byte[] flowNo,int channelNum, String startTIme, String stoptTIme, String alarmSign,
                        int dataType,int bitstreamype,int storageType);
    void PlayBackAudioVideoLive(String ip, int port, int channelNum, int dataType,String starttime,String stoptime);

    void PlayBackLocalAudioVideoLive(String ip, int port, int channelNum, int dataType,String starttime,String stoptime);
    /**
     *
     * @param channelNum 通号
     * @param control    0 开始回放 1 暂停回放 2 结束回放 3 快进回放 4关键帧快退回放 5 拖拽回放 6 关键帧播放
     * @param fastForwardAndBack 回放控制为3和4时，此字段有效，否则置0  0无效 1 1倍 2 2倍 3 4倍 4 8倍 5 16倍
     * @param dragPosition YY-MM-DD-HH-MM-SS 回放控制为5时此字段有效
     */
    void PlayBackVideoAudioLiveControl(int channelNum, int control, int fastForwardAndBack, String dragPosition);


    void UpVedioFile(String user,String passwor,String ip, int port, int channelNum, int dataType,String startTime,String stopTime,String remotePath);

    void UpLocalVideoFile(String user,String passwor,String ip, int port, int channelNum, int dataType,String startTime,String stopTime,String remotePath);

    void tcpAlive();

    void tcpLocalAlive();

    void tcp905Alive();

    <T> void queryAtrribute(T a);

    <T> void textdelivery(T a,String ftpText);

    void terminalControl(int flag);
}
