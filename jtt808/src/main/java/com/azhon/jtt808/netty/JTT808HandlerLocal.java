package com.azhon.jtt808.netty;

import android.content.Intent;
import android.util.Log;

import com.azhon.jtt808.bean.JTT808Bean;
import com.azhon.jtt808.bean.JTT905Bean;
import com.azhon.jtt808.bean.TerminalParamsBean;
import com.azhon.jtt808.listener.OnConnectionListener;
import com.azhon.jtt808.util.ByteUtil;
import com.azhon.jtt808.util.HexUtil;
import com.azhon.jtt808.util.JTT808Util;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt808.netty
 * 文件名:    JTT808Handler
 * 创建时间:  2020/1/2 on 22:49
 * 描述:     TODO
 *
 * @author luozhihao
 */

public class JTT808HandlerLocal extends SimpleChannelInboundHandler<JTT808Bean> {
    private static final String TAG = "JTT808HandlerLocal";
    private JTT808ClientLocal jtt808Client;
    private OnConnectionListener listener;

    public JTT808HandlerLocal(JTT808ClientLocal jtt808Client, OnConnectionListener listener) {
        this.jtt808Client = jtt808Client;
        this.listener = listener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, JTT808Bean bean) throws Exception {
        if (listener != null) {
            listener.receiveData(bean);
        }
        handData(bean, ctx.channel());
    }

    /**
     * 处理事件
     *
     * @param bean
     */
    private void handData(JTT808Bean bean, Channel channel) throws Exception {
        int msgId = ByteUtil.bytes2Int(bean.getMsgHeader().getMsgId());
        Log.d(TAG, "msgId:" + Integer.toHexString(msgId));
        switch (msgId) {
            //注册应答
            case 0x8100:
                registerResult(bean);
                break;
            //平台通用应答
            case 0x8001:
                universalResult(bean);
                break;
            //1078，回放列表
            case 0x9205:
                //   uploadFiles(bean, channel);
                localVideoList(bean);
                Log.d(TAG, "msgId:" + Integer.toHexString(msgId));
                Log.d(TAG, "msgId:" + "录像列表指令");
                break;
            case 0x9202:
                Log.d(TAG, "msgId:" + Integer.toHexString(msgId));
                Log.d(TAG, "msgId:" + "录像回放控制");
                PlayBackVideoAudioLiveControl(bean);
                break;
            case 0x9201:
                Log.d(TAG, "msgId:" + Integer.toHexString(msgId));
                Log.d(TAG, "msgId:" + "远程录像回放请求");
                upLocalVideo(bean);
                break;
            //jtt808，设置终端参数
            case 0x8103:
                terminalParams(bean);
                break;
            case 0x8105:
                terminalControl(bean);
                break;
            case 0x8106:
                JT808_0x0104(bean);
                break;
            //查询终端属性
            case 0x8107:
                queryAtrribute();
                break;
            //terminal update
            case 0x8108:
                update(bean);
                break;
            //jtt1078，实时音视频传输请求
            case 0x9101:
                Log.d(TAG, "msgId:" + Integer.toHexString(msgId));
                audioVideoLive(bean);
                break;
            //jtt1078，音视频实时传输控制
            case 0x9102:
                Log.d(TAG, "msgId:" + Integer.toHexString(msgId));
                audioVideoLiveControl(bean);
                break;
            //jtt1078，音视频实传输状态通知
            case 0x9105:
                Log.d(TAG, "msgId:" + Integer.toHexString(msgId));
                audioVideoLiveState(bean);
                break;
            case 0x9206:
                //上传文件指令
                uploadFiles(bean);
                break;
            case 0x1206:
                //上传文件指令
                Log.d(TAG,"视频上传完成");
                break;
            case 0x9003:
                transferMediaInfo(bean);
                break;
            case 0x8300:
                Log.d(TAG,"msgid: 8300 "+bean.toString());
                textdelivery(bean);
                break;
            default:
                Log.d(TAG, "msgid：" + Integer.toHexString(msgId));
                break;
        }
    }

    private void textdelivery(JTT808Bean bean){
        ByteBuf msgBody = bean.getMsgBody();

        //标志 bit0)紧急 bit2)终端显示 bit3)tts播读 bit4)广告显示 bit5)0导航信息1can故障信息
        byte type = msgBody.readByte();
        Log.d(TAG, "标志： type::"+type);
        String text = new String(msgBody.readBytes(msgBody.readableBytes()-1).array());
        Log.d(TAG, "文本： text::"+text);
        listener.textdelivery(JTT808ClientLocal.getInstance(),text);
    }

    private void JT808_0x0104(JTT808Bean bean) {
        JTT808Util.JT808_0x0104(bean);
    }

    private void queryAtrribute(){
        listener.queryAtrribute(JTT808Client.getInstance());
    }

    private void update(JTT808Bean bean){
        ByteBuf msgBody = bean.getMsgBody();

        //升级类型
        byte updateType = msgBody.readByte();
        Log.d(TAG, "升级类型： updateType::"+updateType);
        //制造商id
        String terminalId = new String(msgBody.readBytes(5).array());
        Log.d(TAG, "制造商id： terminalId::"+terminalId);
        //版本号长度
        byte VersionLength = msgBody.readByte();
        Log.d(TAG, "版本号长度： VersionLength::"+VersionLength);
        //版本号
        String version = new String(msgBody.readBytes(VersionLength).array());
        Log.d(TAG, "版本号： version::"+version);
        //升级包长度
        int packageLength = msgBody.readInt();
        Log.d(TAG, "升级包长度： packageLength::"+packageLength);

    }

    private void transferMediaInfo(JTT808Bean bean) {
        JTT808Bean mediaInfo = JTT808Util.getMediaInfo();
        JTT808ClientLocal.getInstance().writeAndFlush(mediaInfo);
    }


    /**
     * 注册结果
     *
     * @param bean
     */
    private void registerResult(JTT808Bean bean) {
        ByteBuf msgBody = bean.getMsgBody();
        int length = msgBody.readableBytes();
        short flowNum = msgBody.readShort();
        byte result = msgBody.readByte();
        Log.d(TAG, "local 注册结果：" + result + "（0:成功;1:车辆已被注册;2:数据库中无该车辆; 3:终端已被注册;4:数据库中无该终端）");
        if (result != 0) return;
        byte[] authCode = msgBody.readBytes(length - 3).array();
        Log.d(TAG, "鉴权码: " + new String(authCode));
        JTT808Bean authBean = JTT808Util.auth(authCode);
        Log.d(TAG, "发送鉴权: " + authBean.toString());
        JTT808ClientLocal.getInstance().writeAndFlush(authBean);
    }

    /**
     * 平台通用应答
     *
     * @param bean
     */
    private void universalResult(JTT808Bean bean) {
        ByteBuf msgBody = bean.getMsgBody();
        short flowNum = msgBody.readShort();
        byte[] msgId = msgBody.readBytes(2).array();
        byte result = msgBody.readByte();
        String reply = HexUtil.byte2HexStrNoSpace(msgId);
        Log.d(TAG, "平台通用应答 回复=" + "flowNum " + flowNum + " 消息ID " + reply + " 结果：" + result + "（0:成功/确认;1:失败;2:消息有误;3:不支持;4:报警 处理确认）");
        listener.tcpLocalAlive();
    }

    /**
     * 1078上传附件
     *
     * @param bean
     */
    private void uploadFiles(JTT808Bean bean) {
        //响应平台
        JTT808Bean.MsgHeader msgHeader = bean.getMsgHeader();
        JTT808Bean authBean = JTT808Util.universalResponse(msgHeader.getFlowNum(), msgHeader.getMsgId());
        JTT808Client.getInstance().writeAndFlush(authBean);

        ByteBuf body = bean.getMsgBody();

        //ip地址
        byte ipLength = body.readByte();
        byte[] ipBytes = new byte[ipLength];
        body.readBytes(ipBytes);
        String ip = new String(ipBytes);
        Log.d("uploadFiles", "收到了数据： ip::"+ip);

        //tcp端口
        byte[] portBytes = body.readBytes(2).array();
        int port = ByteUtil.bytes2Int(portBytes);
        Log.d("uploadFiles", "收到了数据： port::"+port);

        byte userNameLen = body.readByte();
        byte[] userBytes = new byte[userNameLen];
        body.readBytes(userBytes);
        String user = new String(userBytes);
        Log.d("uploadFiles", "收到了数据： user::"+user);

        byte passwordLen = body.readByte();
        byte[] passworBytes = new byte[passwordLen];
        body.readBytes(passworBytes);
        String passwor = new String(passworBytes);
        Log.d("uploadFiles", "收到了数据： passwor::"+passwor);

        byte filePathLen = body.readByte();
        byte[] filePathBytes = new byte[filePathLen];
        body.readBytes(filePathBytes);
        String filePath = new String(filePathBytes);
        Log.d("uploadFiles", "收到了数据： filePath::"+filePath);

        byte channelNum = body.readByte();
        Log.d("uploadFiles", "收到了数据： channelNum::"+channelNum);

        String starttime = ByteUtil.bcdToString(body.readBytes(6).array());
        String stoptime = ByteUtil.bcdToString(body.readBytes(6).array());
        Log.d("uploadFiles", "收到了数据： start stop time::"+"  starttime:"+starttime+"  stoptime:"+stoptime);

        byte[] alarmIDNumber = body.readBytes(8).array();
        String alarm = new String(alarmIDNumber);
        Log.d("uploadFiles", "收到了数据： alarmIDNumber::"+alarm);

        byte vediotype = body.readByte();
        Log.d("uploadFiles", "收到了数据： vediotype::"+vediotype);


        byte streamtype = body.readByte();
        Log.d("uploadFiles", "收到了数据： streamtype::"+streamtype);

        byte storePostion = body.readByte();
        Log.d("uploadFiles", "收到了数据： storePostion::"+storePostion);

        byte execute = body.readByte();
        Log.d("uploadFiles", "收到了数据： execute::"+execute);
        listener.UpLocalVideoFile(user,passwor,ip,port,channelNum,vediotype,starttime,stoptime, filePath);
//        //udp端口用不到，先忽略
//        body.readBytes(2);
//        //报警标识号
//        byte[] alarmIDNumber = body.readBytes(16).array();
//        //报警编号
//        byte[] alarmNumber = body.readBytes(32).array();
//        //预留
//        byte[] yl = body.readBytes(16).array();
//        //连接附件服务器
//        JTT1078Client jtt1078Client = new JTT1078Client(ip, port);
//        //获取需要上传的附件列表
//        String key = HexUtil.byte2HexStrNoSpace(alarmIDNumber);
//        List<File> files = JTT808Util.ALARM_MAP.get(key);
//        if (files == null) {
//            Log.e(TAG, "获取报警附件失败：" + key);
//            return;
//        }
//        Log.d(TAG, "<<<<收到了附件服务器的信息，开始上传报警标识号：" + key + "的附件");
//        JTT808Util.ALARM_MAP.remove(key);
//        jtt1078Client.setFiles(files);
//        jtt1078Client.setAlarmIDNumber(alarmIDNumber);
//        jtt1078Client.setAlarmNumber(alarmNumber);
//        jtt1078Client.connect();
    }


    /**
     * jtt808，设置终端参数
     *
     * @param bean
     */
    private void terminalParams(JTT808Bean bean) throws Exception {
        List<TerminalParamsBean> params = new ArrayList<>();
        ByteBuf body = bean.getMsgBody();
        //参数总数
        byte paramsCount = body.readByte();
        for (byte i = 0; i < paramsCount; i++) {
            //DWORD 读取4个字节
            int id = body.readInt();
            byte paramsLength = body.readByte();
            byte[] data = body.readBytes(paramsLength).array();
            switch (id) {
                //DWORD
                case 0x0055:
                case 0x0056:
                case 0x0057:
                case 0x0058:
                case 0x0059:
                case 0x005A:
                case 0x0018:
                case 0x0019:
                case 0x0020:
                case 0x0021:
                case 0x0022:
                case 0x0027:
                case 0x0028:
                case 0x0029:
                case 0x002C:
                case 0x002D:
                case 0x002E:
                case 0x002F:
                case 0x0030:
                    params.add(new TerminalParamsBean(id, Integer.class, ByteUtil.fourBytes2Int(data)));
                    break;
                //WORD
                case 0x005B:
                case 0x005C:
                case 0x0081:
                case 0x0082:
                    params.add(new TerminalParamsBean(id, Integer.class, ByteUtil.bytes2Int(data)));
                    break;
                //STRING
                case 0x0010:
                case 0x0011:
                case 0x0012:
                case 0x0013:
                case 0x0014:
                case 0x0015:
                case 0x0016:
                case 0x0017:
                case 0x0083:
                    params.add(new TerminalParamsBean(id, String.class, new String(data, "GBK")));
                    break;
                //BYTE
                case 0x0084:
                    params.add(new TerminalParamsBean(id, Byte.class, data[0]));
                    break;

                default:
                    break;
            }
        }
        if (listener != null) {
            listener.terminalParams(params);
        }
    }

    private void terminalControl(JTT808Bean bean){
        JTT808Bean.MsgHeader msgHeader = bean.getMsgHeader();
        JTT808Bean jtt808Bean = JTT808Util.universalResponse(msgHeader.getFlowNum(), msgHeader.getMsgId());
        JTT808ClientLocal.getInstance().writeAndFlush(jtt808Bean);

        ByteBuf body = bean.getMsgBody();
        int length = body.readableBytes();
        //命令字
        byte cmd = body.readByte();
        Log.d(TAG, "cmd = " + cmd);
        if(cmd == 1 || cmd == 2){
            //命令参数
            byte[] cmdParams = body.readBytes(length - 1).array();
        }else{
            if (listener != null) {
                listener.terminalControl(cmd);
            }
            /*switch (cmd){
                case 3: //关机
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SHUTDOWN);
                    sendBroadcast(intent);
                    break;
                case 4: //复位
                    break;
                case 5: //恢复出厂设置
                    break;
                case 6: //关闭数据通信
                    break;
                case 7: //关闭所有无线通信
                    break;
                default:
                    break;

            }*/
        }
    }

    /**
     * 实时音视频传输请求
     *
     * @param bean
     */
    private void audioVideoLive(JTT808Bean bean) {

        ByteBuf body = bean.getMsgBody();
        JTT808Bean.MsgHeader msgHeader = bean.getMsgHeader();

        byte ipLength = body.readByte();
        String ip = new String(body.readBytes(ipLength).array());
        //tcp端口号
        byte[] tcpPortBytes = body.readBytes(2).array();
        int tcpPort = ByteUtil.bytes2Int(tcpPortBytes);
        //udp端口号
        byte[] udpPortBytes = body.readBytes(2).array();
        int udpPort = ByteUtil.bytes2Int(udpPortBytes);
        //逻辑通道号
        byte channelNum = body.readByte();
        //数据类型  0 音视频 1 视频 2 双向对讲  3 监听   4 中心广播  5 透传
        byte dataType = body.readByte();
        //码流类型 0 主码流 1 子码流
        byte codeStream = body.readByte();

        JTT808Bean jtt808Bean = JTT808Util.universalResponse(msgHeader.getFlowNum(), msgHeader.getMsgId());
        JTT808Client.getInstance().writeAndFlush(jtt808Bean);
        Log.d(TAG, "响应了服务器的实时音视频请求：dataType = " + dataType+"channelNum:"+channelNum);

        if (listener != null) {
            listener.localAudioVideoLive(ip, tcpPort, channelNum, dataType);
        }
    }

    /**
     * jtt1078，音视频实时传输控制
     *
     * @param bean
     */
    private void audioVideoLiveControl(JTT808Bean bean) {
        ByteBuf body = bean.getMsgBody();
        byte channelNum = body.readByte();
        byte control = body.readByte();
        byte closeAudio = body.readByte();
        byte switchStream = body.readByte();
        Log.d(TAG, "实时音视频控制：channelNum=" + channelNum + " control=" + control);
        if (listener != null) {
            listener.audioVideoLocalLiveControl(channelNum, control, closeAudio, switchStream);
        }
    }

    /**
     * jtt1078，音视频实传输状态通知
     */
    private void audioVideoLiveState(JTT808Bean bean) {
        ByteBuf body = bean.getMsgBody();
        byte channelNum = body.readByte();
        byte losePkg = body.readByte();
        Log.d(TAG, "音视频实传输状态通知：逻辑通道号=" + channelNum + " 丢包率：" + losePkg);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        if (listener != null) {
            listener.onLocalConnectionSateChange(OnConnectionListener.CONNECTED);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Log.e(TAG, "断开了连接");
        jtt808Client.reConnect();
        if (listener != null) {
            listener.onLocalConnectionSateChange(OnConnectionListener.DIS_CONNECT);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            if(((IdleStateEvent)evt).state() == IdleState.READER_IDLE) {
                Log.e(TAG,"长期没收到服务器推送的数据");
                //reconnect
                //jtt808Client.reConnect();
            }else if (((IdleStateEvent) evt).state() == IdleState.WRITER_IDLE) {
                JTT808Bean heartBeatBean = JTT808Util.heartBeat();
                Log.d(TAG, "发送心跳: " + heartBeatBean.toString());
                ctx.writeAndFlush(heartBeatBean.getData());
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 向服务器上传视频列表 1078
     *
     * @param bean
     */
    private void localVideoList(JTT808Bean bean) {
        ByteBuf body = bean.getMsgBody();
        int channelNum = body.readByte();
        String starttime = ByteUtil.bcdToString(body.readBytes(6).array());
        String stoptime = ByteUtil.bcdToString(body.readBytes(6).array());
        String alarmSign = ByteUtil.bcdToString(body.readBytes(7).array());
        int dataType = body.readByte();
        int bitstreamType = body.readByte();
        int storageType = body.readByte();
        bean.getMsgHeader().getFlowNum();
        listener.localVideoListLocal(bean.getMsgHeader().getFlowNum(), channelNum, starttime, stoptime, alarmSign, dataType, bitstreamType, storageType);
    }

    /**服务器请求回放视 1078
     *
     * @param bean
     */
    private void upLocalVideo(JTT808Bean bean) {
        ByteBuf body = bean.getMsgBody();
        JTT808Bean.MsgHeader msgHeader = bean.getMsgHeader();

        byte ipLength = body.readByte();
        String ip = new String(body.readBytes(ipLength).array());
        Log.d("upLocalVideo","ip:"+ip);
        //tcp端口号
        byte[] tcpPortBytes = body.readBytes(2).array();
        int tcpPort = ByteUtil.bytes2Int(tcpPortBytes);
        Log.d("upLocalVideo","tcp端口号:"+tcpPort);
        //udp端口号
        byte[] udpPortBytes = body.readBytes(2).array();
        int udpPort = ByteUtil.bytes2Int(udpPortBytes);
        Log.d("upLocalVideo","udpPort端口号:"+udpPort);
        //逻辑通道号
        byte channelNum = body.readByte();
        Log.d("upLocalVideo","逻辑通道号:"+channelNum);
        //数据类型  0 音视频
        byte dataType = body.readByte();
        Log.d("upLocalVideo","数据类型:"+dataType);
        //码流类型 0 主码流 1 子码流
        byte codeStream = body.readByte();
        Log.d("upLocalVideo","码流类型:"+codeStream);
        //0 主存储器或灾备存储器 1主存储器 0 灾备存储器
        byte storageTbody=body.readByte();
        Log.d("upLocalVideo","主存储器或灾备存储器:"+storageTbody);
        //回放方式  0 正常 1 快进 2关键帧快退 3关键帧播放 4关键帧 上传
        byte playbackMode= body.readByte();
        Log.d("upLocalVideo","回放方式:"+playbackMode);
        //快进快退倍数  回放方式为1和2，此字段有效，否则置0   0 无效，1 一倍 2 二倍
        byte forwardAndBack= body.readByte();
        Log.d("upLocalVideo","快进快退倍数:"+forwardAndBack);
//        //快进快退倍数  3 四倍  4 八倍 5 十六倍
//        byte forwardAndBack2= body.readByte();
//        Log.d("upLocalVideo","快进快退倍数:"+forwardAndBack2);
        //开始时间
        String starttime = ByteUtil.bcdToString(body.readBytes(6).array());
        Log.d("upLocalVideo","开始时间:"+starttime);

        //结束时间
        String stoptime = ByteUtil.bcdToString(body.readBytes(6).array());
        Log.d("upLocalVideo","结束时间:"+stoptime);

        JTT808Bean jtt808Bean = JTT808Util.universalResponse(msgHeader.getFlowNum(), msgHeader.getMsgId());
        JTT808ClientLocal.getInstance().writeAndFlush(jtt808Bean);
        if (listener != null) {
            Log.d(TAG, "响应了服务器的回放音视频请求：dataType = " + dataType+"channelNum:"+channelNum+" starttime:"+starttime+" stoptime "+stoptime);
            listener.PlayBackLocalAudioVideoLive(ip, tcpPort, channelNum, dataType,starttime,stoptime);
        }
    }
    /**
     * jtt1078，音视频实时传输控制
     *
     * @param bean
     */
    private void PlayBackVideoAudioLiveControl(JTT808Bean bean) {
        ByteBuf body = bean.getMsgBody();
        byte channelNum = body.readByte();
        byte control = body.readByte();
        byte fastForwardAndBack = body.readByte();
        String dragPosition = ByteUtil.bcdToString(body.readBytes(6).array());
        Log.d(TAG, "实时音视频控制：channelNum=" + channelNum + " control=" + control+" dragPosition:"+dragPosition);
        if (listener != null) {
            listener.PlayBackVideoAudioLiveControl(channelNum, control, fastForwardAndBack, dragPosition);
        }
    }
}
