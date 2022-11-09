package com.azhon.jtt808;

import android.content.Context;
import android.util.Log;

import com.azhon.jtt808.VideoList.timeBean;
import com.azhon.jtt808.bean.JTT808Bean;
import com.azhon.jtt808.bean.JTT905Bean;
import com.azhon.jtt808.ftp.FTPFileUpload;
import com.azhon.jtt808.listener.OnConnectionListener;
import com.azhon.jtt808.netty.JTT808Client;
import com.azhon.jtt808.netty.JTT808ClientLocal;
import com.azhon.jtt808.netty.JTT905Client;
import com.azhon.jtt808.netty.live.LiveClient;
import com.azhon.jtt808.util.ByteUtil;
import com.azhon.jtt808.util.JTT808Util;
import java.io.File;
import java.util.List;

import io.netty.buffer.ByteBuf;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt808
 * 文件名:    JTT808Manager
 * 创建时间:  2020/1/4 on 16:35
 * 描述:     TODO
 *
 * @author luozhihao
 */

public class JTT808Manager {
    private static final String TAG = "JTT808Manager";

    private static JTT808Manager manager = new JTT808Manager();
    //标记是否初始化
    private boolean isInit = false;

    private boolean isLocalInit = false;
    //终端手机号
    private String phone;
    //终端ID
    private String terminalId;
    //服务器地址和端口
    private String ip;
    private int port;

    private String ISU;

    private String local_ip;
    private int local_port;

    private String ip_905;
    private int port_905;
    private boolean is905Init = false;

    private OnConnectionListener listener;

    private boolean isNeedLocation = false;
    private int accStatus=0;
    private int alarmStatus = 0;

    public static JTT808Manager getInstance() {
        return manager;
    }

    public static enum Protocol {
        jtt808_2011,
        jtt808_2013,
        jtt808_2019
    }

    Protocol default_protocol = Protocol.jtt808_2013;

    public void setProtocol(Protocol protocol){
        default_protocol = protocol;
    }

    public Protocol getProtocol(){
        return default_protocol;
    }

    /**
     * 设置监听
     *
     * @param listener
     * @return
     */
    public JTT808Manager setOnConnectionListener(OnConnectionListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * 初始化
     *
     * @param phone 终端手机号 12位
     * @param ip    服务器地址
     * @param port  服务器端口
     */
    public void init(String phone, String terminalId, String ip, int port) {
        if (isInit) return;
        this.phone = phone;
        this.terminalId = terminalId;
        this.ip = ip;
        this.port = port;
        if (this.phone.length() != 12) {
            Log.e(TAG, "终端手机号的长度必须为12位");
        }
        isInit = true;
        connectServer();
    }

    public void initLocal(String phone, String terminalId, String local_ip, int local_port) {
        if (isLocalInit) return;
        this.phone = phone;
        this.terminalId = terminalId;
        this.local_ip = local_ip;
        this.local_port = local_port;
        if (this.phone.length() != 12) {
            Log.e(TAG, "Local终端手机号的长度必须为12位");
        }
        isLocalInit = true;
        connectLocalServer();
    }

    public void init905(String ISU, String terminalId, String local_ip, int local_port) {
        Log.e(TAG,"init905:" + is905Init);
        if (is905Init) return;
        this.ISU = ISU;
        this.terminalId = terminalId;
        this.ip_905 = local_ip;
        this.port_905 = local_port;

        is905Init = true;
        connect905Server();
    }

    private void connect905Server(){
        JTT905Client client = JTT905Client.getInstance();
        client.setServerInfo(ip_905, port_905);
        client.setConnectionListener(listener);
        client.connect();
    }

    /**
     * 连接服务器
     */
    private void connectServer() {
        JTT808Client client = JTT808Client.getInstance();
        client.setServerInfo(ip, port);
        client.setConnectionListener(listener);
        client.connect();
    }

    private void connectLocalServer() {
        JTT808ClientLocal client = JTT808ClientLocal.getInstance();
        client.setServerInfo(local_ip, local_port);
        client.setConnectionListener(listener);
        client.connect();
    }

    /**
     * 主动断开与服务器的连接
     */
    public void disconnect() {
        isInit = false;
        JTT808Client client = JTT808Client.getInstance();
        client.disconnect();
    }

    public void disconnectLocal() {
        isLocalInit = false;
        JTT808ClientLocal client = JTT808ClientLocal.getInstance();
        client.disconnect();
    }

    public void disconnect905() {
        is905Init = false;
        JTT905Client client = JTT905Client.getInstance();
        client.disconnect();
    }
    //========================协议方法===========================================

    /**
     * 注册
     *
     * @param manufacturerId 制造商 ID
     * @param terminalModel  终端型号
     */
    public void register(String manufacturerId, String terminalModel) {
        JTT808Bean register = JTT808Util.register(manufacturerId, terminalModel, terminalId);
        JTT808Client.getInstance().writeAndFlush(register);
        Log.d(TAG, "发送注册: " + ByteUtil.bytesToHex(register.getData())+" terminalId:"+terminalId);
    }


    public void registerLocal(String manufacturerId, String terminalModel) {
        JTT808Bean register = JTT808Util.register(manufacturerId, terminalModel, terminalId);
        JTT808ClientLocal.getInstance().writeAndFlush(register);
        Log.d(TAG, "发送注册: " + ByteUtil.bytesToHex(register.getData())+" terminalId:"+terminalId);
    }

    public void register905(String manufacturerId, String terminalModel) {
        JTT905Bean register = JTT808Util.register(manufacturerId, terminalModel, terminalId,false);
        JTT905Client.getInstance().writeAndFlush(register);
        Log.d(TAG, "发送注册: " + ByteUtil.bytesToHex(register.getData())+" terminalId:"+terminalId);
    }

    /**
     * 上传经纬度
     *
     * @param lat 纬度，乘以10的6次方
     * @param lng 经度，乘以10的6次方
     */
    double pow106 ;
    double lat106 ;
    double lng106 ;
    public void uploadLocation(double lat, double lng,float speed,int alarm,int acc) {
            pow106 = Math.pow(10, 6);
            lat106 = lat * pow106;
            lng106 = lng * pow106;
            JTT808Bean location = JTT808Util.uploadLocation(Math.round(lat106), Math.round(lng106),Math.round(speed), alarm, acc);
            Log.d(TAG, "上传位置信息 lng:" + lat + "" + " lng:" + lng+" acc :"+acc+"  alarm:"+alarm);
            if(isInit) {
                JTT808Client.getInstance().writeAndFlush(location);
            }
            if(isLocalInit) {
                JTT808ClientLocal.getInstance().writeAndFlush(location);
            }
            //--------------------------------905--------------------------------
            if(is905Init) {
                JTT905Bean location_ = JTT808Util.uploadLocation(Math.round(lat * Math.pow(10, 4)), Math.round(lng * Math.pow(10, 4)), Math.round(speed), alarm, acc, false);
                JTT905Client.getInstance().writeAndFlush(location_);
            }
    }

    /**
     * 上传报警信息（渝标）
     * 重庆车检院平台通讯协议（3.4.2驾驶员行为监测功能报警）
     *
     * @param lat       纬度，乘以10的6次方
     * @param lng       经度，乘以10的6次方
     * @param alarmType 1：抽烟，2：打电话，3：未注视前方，4：疲劳驾驶，5：未在驾驶位
     * @param level     1：一级报警，2：二级报警
     * @param degree    范围 1~10。数值越大表示疲劳程度越严重
     * @param files     附件集合
     */
    public void uploadAlarmInfoYB(long lat, long lng, int alarmType, int level, int degree, List<File> files) {
        JTT808Bean alarm = JTT808Util.uploadAlarmInfoYB(lat, lng, alarmType, level, degree, files, terminalId);
        Log.d(TAG, "上传报警信息（渝标）: " + alarm.toString() + "\n" + alarmType
                + "(1：抽烟，2：打电话，3：未注视前方，4：疲劳驾驶，5：未在驾驶位)，"
                + degree + "(1~10,数值越大表示疲劳程度越严重)，附件数量：" + files.size());
        JTT808Client.getInstance().writeAndFlush(alarm);
        JTT808ClientLocal.getInstance().writeAndFlush(alarm);
    }

    /**
     * 实时音视频（jtt1078）
     * 5.5.3 实时音视频流及透传数据传输
     */
    public void videoLive(byte[] data, int channelNum, LiveClient liveClient,int len,int frome,long timeStamp) {
        JTT808Util.videoLive(data, channelNum, phone, liveClient, len,timeStamp);
    }

    public void videoLive905(byte[] data, int channelNum, LiveClient liveClient,int len,int frome,long timeStamp) {
        JTT808Util.videoLive(data, channelNum, ISU, liveClient, len,timeStamp);
    }

    /**
     * 实时音视频（jtt1078）
     * 5.5.3 实时音视频流及透传数据传输
     */
    public void audioLive(byte[] data, int channelNum, LiveClient liveClient, int len, Context context,long timeStamp,int audioType) {
        JTT808Util.audioLive(data, channelNum, phone, liveClient,len,context,timeStamp,audioType);
    }

    //========================get set===========================================

    /**上传回放视频列表
     *JTT1078
     * @param channelNum 通道号
     */
    public void uploadVideoList(byte[] flowNo, int channelNum,boolean isSub, int totlePackage,int packageNum,List<timeBean> list){
        JTT808Bean bean = JTT808Util.uploadVideoList(flowNo, channelNum,isSub,totlePackage, packageNum,list);
        ByteBuf bodyBuf = bean.getMsgBody();
        Log.d("uploadVideo",bodyBuf.readableBytes()+"");
        //JTT808Client.getInstance().writeAndFlush(bean);
    }

    public void uploadVideoListLocal(byte[] flowNo, int channelNum,boolean isSub, int totlePackage,int packageNum,List<timeBean> list){
        JTT808Bean bean = JTT808Util.uploadVideoListLocal(flowNo, channelNum,isSub,totlePackage, packageNum,list);
        ByteBuf bodyBuf = bean.getMsgBody();
        Log.d("uploadVideo",bodyBuf.readableBytes()+"");
        //JTT808Client.getInstance().writeAndFlush(bean);
    }

    public String getPhone() {
        return phone;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public String getISU() {
        return ISU;
    }

    public JTT808Bean queryAtrribute(String manufacturerId,String terminalModel,String terminalId,String iccid,
                               String HW,String firmware){
        return JTT808Util.queryAtrribute(manufacturerId,terminalModel,terminalId,iccid,HW,firmware);
    }

    public JTT905Bean queryAtrribute905(String manufacturerId,String terminalModel,String terminalId,String iccid,
                                     String HW,String firmware){
        return JTT808Util.queryAtrribute905(manufacturerId,terminalModel,terminalId,iccid,HW,firmware);
    }

    public void downloadApk(String ip,int port,String name,String passwrod,String fileName){
        try {
            FTPFileUpload.getInstance().downloadApk(ip, port, name, passwrod, fileName);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
