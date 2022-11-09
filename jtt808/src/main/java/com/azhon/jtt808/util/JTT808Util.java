package com.azhon.jtt808.util;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

import com.azhon.jtt808.JTT808Manager;
import com.azhon.jtt808.VideoList.timeBean;
import com.azhon.jtt808.bean.JTT808Bean;
import com.azhon.jtt808.bean.JTT905Bean;
import com.azhon.jtt808.netty.JTT808Client;
import com.azhon.jtt808.netty.JTT808ClientLocal;
import com.azhon.jtt808.netty.JTT905Client;
import com.azhon.jtt808.netty.live.LiveClient;
import com.azhon.jtt808.video.NV21EncoderH264;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt808.util
 * 文件名:    JTT808Util
 * 创建时间:  2020/1/4 on 17:23
 * 描述:     TODO
 *
 * @author luozhihao
 */

public class JTT808Util {
    private static final String TAG = "JTT808Util";

    private static int FLOW_NUM = 0;
    //分包
    public static int SUB_PACKAGE_YES = 1;
    //不分包
    public static int SUB_PACKAGE_NO = 0;
    public static Map<String, List<File>> ALARM_MAP = new LinkedHashMap<>();

    //实时视频数据包序号
    private static int RTP_VIDEO_COUNT = 0;
    private static int AUDIO_RTP_VIDEO_COUNT = 0;
    //计算这一I帧距离上一I帧的间隔
    private static long LAST_I_FRAME_TIME;
    private static long AUDIO_LAST_I_FRAME_TIME;
    //计算这一帧与上一帧的间隔
    private static long LAST_FRAME_TIME;
    private static long AUDIO_LAST_FRAME_TIME;
    private static double everyPkgSize = 950.d;
    private static byte [] flags = {0x30, 0x31, 0x63, 0x64};

    /**
     * 注册
     *
     * @param manufacturerId 制造商 ID
     * @param terminalModel  终端型号
     * @param terminalId     终端 ID
     */
    public static JTT808Bean register(String manufacturerId, String terminalModel, String terminalId) {
        boolean isJTT808_2019 = false;
        if(JTT808Manager.getInstance().getProtocol() == JTT808Manager.Protocol.jtt808_2019){
            isJTT808_2019 = true;
        }
        ByteBuf register = Unpooled.buffer();
        //省域 ID
        register.writeShort(0);
        //省域 市县域 ID
        register.writeShort(0);
        //制造商 ID
        if(JTT808Manager.Protocol.jtt808_2019 == JTT808Manager.getInstance().getProtocol()) {
            //                  行政区代码+终端id
            register.writeBytes(("000000" + manufacturerId).getBytes());
        }else{
            register.writeBytes(manufacturerId.getBytes());
        }
        //终端型号
        register.writeBytes(ByteUtil.addZeroForNum(terminalModel,isJTT808_2019?30:20));
        //终端 ID
        register.writeBytes(ByteUtil.addZeroForNum(terminalId,isJTT808_2019?30:7));
        //车牌颜色(车牌颜色，按照 JT/T415-2006 的 5.4.12。 未上牌时，取值为 0)
        register.writeByte(0);
        //车辆标识(车牌颜色为 0 时，表示车辆 VIN; 否则，表示公安交通管理部门颁发的机动车号牌。)
        register.writeByte(0);

        JTT808Bean bean = new JTT808Bean();

        JTT808Bean.MsgHeader msgHeader = getMsgHeader(0x0100, register, SUB_PACKAGE_NO, 0, 0);

        bean.setMsgHeader(msgHeader);
        bean.setMsgBody(register);

        return generateData(bean);
    }

    /**
     * 鉴权
     *
     * @param authCode 鉴权码
     */
    public static JTT808Bean auth(byte[] authCode) {
        ByteBuf auth = Unpooled.buffer(authCode.length);
        if(JTT808Manager.getInstance().getProtocol() == JTT808Manager.Protocol.jtt808_2019){
            auth.writeByte(authCode.length);
            auth.writeBytes(authCode);
            String imei = "123456789012345";
            auth.writeBytes(imei.getBytes());
            String version = "1234567890abcdefghij";
            auth.writeBytes(version.getBytes());
        }else {
            auth.writeBytes(authCode);
        }
        JTT808Bean bean = new JTT808Bean();
        JTT808Bean.MsgHeader msgHeader = getMsgHeader(0x0102, auth, SUB_PACKAGE_NO, 0, 0);
        bean.setMsgHeader(msgHeader);
        bean.setMsgBody(auth);
        return generateData(bean);
    }

    /**
     * 心跳
     */
    public static JTT808Bean heartBeat() {
        ByteBuf heart = Unpooled.EMPTY_BUFFER;
        JTT808Bean bean = new JTT808Bean();
        JTT808Bean.MsgHeader msgHeader = getMsgHeader(0x0002, heart, SUB_PACKAGE_NO, 0, 0);
        bean.setMsgHeader(msgHeader);
        bean.setMsgBody(heart);
        return generateData(bean);
    }

    /**
     * 终端通用应答
     */
    public static JTT808Bean universalResponse(byte[] flowNum, byte[] msgId) {
        ByteBuf universal = Unpooled.buffer(5);
        universal.writeBytes(flowNum);
        universal.writeBytes(msgId);
        //0:成功/确认;1:失败;2:消息有误;3:不支持
        universal.writeByte(0);
        JTT808Bean bean = new JTT808Bean();
        JTT808Bean.MsgHeader msgHeader = getMsgHeader(0x0001, universal, SUB_PACKAGE_NO, 0, 0);
        bean.setMsgHeader(msgHeader);
        bean.setMsgBody(universal);
        return generateData(bean);
    }

    /**
     * 上报经纬度
     *
     * @param lat 纬度
     * @param lng 经度
     * @return
     */
    public static JTT808Bean uploadLocation(long lat, long lng,int speed,int alarm,int acc) {
        byte[] info = locationInfo(lat, lng,speed,alarm,acc);
        ByteBuf location = Unpooled.buffer(info.length);
        location.writeBytes(info);
        JTT808Bean bean = new JTT808Bean();
        JTT808Bean.MsgHeader msgHeader = getMsgHeader(0x0200, location, SUB_PACKAGE_NO, 0, 0);
        bean.setMsgHeader(msgHeader);
        bean.setMsgBody(location);
        return generateData(bean);
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
     * @param files     附件
     */
    public static JTT808Bean uploadAlarmInfoYB(long lat, long lng, int alarmType, int level, int degree,
                                               List<File> files, String terminalId) {
        int count = files.size();
        ByteBuf alarm = Unpooled.buffer();
        //位置基本信息
        byte[] info = locationInfo(lat, lng,0,0,0);
        alarm.writeBytes(info);
        //位置附加信息项列表
        alarm.writeByte(0x65);

//===================附加信息==========================
        ByteBuf alarm2 = Unpooled.buffer();
        //报警 ID
        alarm2.writeBytes(new byte[]{0, 0, 0, 0});
        //标志状态
        alarm2.writeByte(0);
        //报警/事件类型
        if (alarmType == 1) {
            alarm2.writeByte(0x03);
        } else if (alarmType == 2) {
            alarm2.writeByte(0x02);
        } else if (alarmType == 3) {
            alarm2.writeByte(0x04);
        } else if (alarmType == 4) {
            alarm2.writeByte(0x01);
        } else if (alarmType == 5) {
            alarm2.writeByte(0x05);
        }
        alarm2.writeByte(level);
        //疲劳程度
        alarm2.writeByte(degree);

        alarm2.writeBytes(new byte[]{0, 0, 0, 0});
        //车速
        alarm2.writeByte(0);
        //高程
        alarm2.writeBytes(new byte[]{0, 0});
        //经纬度
        alarm2.writeBytes(ByteUtil.longToDword(lat));
        alarm2.writeBytes(ByteUtil.longToDword(lng));
        //bcd时间
        byte[] bcdTime = TimeUtil.getBcdTime();
        alarm2.writeBytes(bcdTime);
        //车辆状态
        alarm2.writeBytes(new byte[]{0, 0});
        //报警标识号
        byte[] alarmFlag = createAlarmFlag(terminalId, bcdTime, count);
        alarm2.writeBytes(alarmFlag);
        //附加信息长度
        alarm.writeByte(alarm2.readableBytes());

        //附加信息
        alarm.writeBytes(alarm2);


        JTT808Bean bean = new JTT808Bean();
        JTT808Bean.MsgHeader msgHeader = getMsgHeader(0x0200, alarm, SUB_PACKAGE_NO, 0, 0);
        bean.setMsgHeader(msgHeader);
        bean.setMsgBody(alarm);
        if (count > 0) {
            String key = HexUtil.byte2HexStrNoSpace(alarmFlag);
            ALARM_MAP.put(key, files);
            Log.d(TAG, ">>>>保存报警附件，报警标识号：" + key + "，附件数量：" + count);
        }
        return generateData(bean);
    }

    /**
     * 生成报警标识号
     */
    private static byte[] createAlarmFlag(String terminalId, byte[] bcdTime, int count) {
        byte[] terminalIdB = terminalId.getBytes();
        byte[] b = {0, (byte) count, 0};
        return ByteUtil.byteMergerAll(terminalIdB, bcdTime, b);
    }


    /**
     * 报警附件信息消息（渝标）
     * 重庆车检院平台通讯协议（3.6.2 报警附件信息消息）
     *
     * @param alarmIDNumber  报警标识号
     * @param alarmNumber    报警编号
     * @param alarmNumberStr 文件的报警编号
     * @param files          附件
     */
    public static JTT808Bean uploadFJMsg(byte[] alarmIDNumber, byte[] alarmNumber, String alarmNumberStr, List<File> files) {
        JTT808Bean bean = new JTT808Bean();

        ByteBuf fjMsg = Unpooled.buffer();
        fjMsg.writeBytes(JTT808Manager.getInstance().getTerminalId().getBytes());
        fjMsg.writeBytes(alarmIDNumber);
        fjMsg.writeBytes(alarmNumber);
        //0x00:正常报警文件信息 0x01:补传报警文件信息
        fjMsg.writeByte(0x00);
        //附件数量
        fjMsg.writeByte(files.size());
        // 附件信息列表
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            //<文件类型>_<通道号>_<报警类型>_<序号>_<报警编号>.<后缀名>
            String fileType = Util.getFileType(file);
            String fileNameType = Util.getFileNameType(fileType);
            String fileName = fileNameType + "_0_0_" + i + "_" + alarmNumberStr + fileType;
            //文件名称长度
            fjMsg.writeByte(fileName.getBytes().length);
            //文件名称
            fjMsg.writeBytes(fileName.getBytes());
            //文件大小
            fjMsg.writeBytes(ByteUtil.longToDword(file.length()));
        }
        JTT808Bean.MsgHeader msgHeader = getMsgHeader(0x1210, fjMsg, SUB_PACKAGE_NO, 0, 0);
        bean.setMsgHeader(msgHeader);
        bean.setMsgBody(fjMsg);
        return generateData(bean);
    }

    /**
     * 文件信息上传（渝标）
     * 重庆车检院平台通讯协议（3.6.3 文件信息上传）
     *
     * @param file     附件
     * @param fileType 0x00:图片 0x01:音频 0x02:视频 0x03:文本 0x04:其它
     */
    public static JTT808Bean uploadFileMsg(int fileType, String fileName, File file) {
        JTT808Bean bean = new JTT808Bean();
        ByteBuf fileMsg = Unpooled.buffer();
        //文件名称长度
        fileMsg.writeByte(fileName.getBytes().length);
        //文件名称
        fileMsg.writeBytes(fileName.getBytes());
        //文件类型
        fileMsg.writeByte(fileType);
        //文件大小
        fileMsg.writeBytes(ByteUtil.longToDword(file.length()));

        JTT808Bean.MsgHeader msgHeader = getMsgHeader(0x1211, fileMsg, SUB_PACKAGE_NO, 0, 0);
        bean.setMsgHeader(msgHeader);
        bean.setMsgBody(fileMsg);
        return generateData(bean);
    }

    /**
     * 文件上传完成消息（渝标）
     * 重庆车检院平台通讯协议（3.6.5 文件上传完成消息）
     *
     * @param fileType 0x00:图片 0x01:音频 0x02:视频 0x03:文本 0x04:其它
     */
    public static JTT808Bean uploadFileDone(int fileType, String fileName, File file) {
        JTT808Bean bean = new JTT808Bean();

        ByteBuf done = Unpooled.buffer();
        //文件名称长度
        done.writeByte(fileName.getBytes().length);
        //文件名称
        done.writeBytes(fileName.getBytes());
        //文件类型
        done.writeByte(fileType);
        //文件大小
        done.writeBytes(ByteUtil.longToDword(file.length()));

        JTT808Bean.MsgHeader msgHeader = getMsgHeader(0x1212, done, SUB_PACKAGE_NO, 0, 0);
        bean.setMsgHeader(msgHeader);
        bean.setMsgBody(done);
        return generateData(bean);
    }

    public static JTT808Bean queryAtrribute(String manufacturerId,String terminalModel,String terminalId,String iccid,
                                    String HW,String firmware){
        JTT808Bean bean = new JTT808Bean();
        ByteBuf attribute = Unpooled.buffer();
        //终端类型
        String type = "11110010".replace(" ", "");
        attribute.writeByte(Integer.parseInt(type, 2));
        //制造商 ID
        attribute.writeBytes(manufacturerId.getBytes());
        //终端型号
        attribute.writeBytes(ByteUtil.addZeroForNum(terminalModel,20));
        //终端 ID
        attribute.writeBytes(ByteUtil.addZeroForNum(terminalId,7));
        //ICCID
        attribute.writeBytes(ByteUtil.string2Bcd(iccid));
        //硬件版本号长度
        attribute.writeByte(HW.length());
        //硬件版本号
        attribute.writeBytes(HW.getBytes());
        //固件版本号长度
        attribute.writeByte(firmware.length());
        //固件版本号
        attribute.writeBytes(firmware.getBytes());
        //GNSS属性
        String gnss = "11110000".replace(" ", "");
        attribute.writeByte(Integer.parseInt(gnss, 2));
        //通信模块属性
        String network = "11111111".replace(" ", "");
        attribute.writeByte(Integer.parseInt(network, 2));

        JTT808Bean.MsgHeader msgHeader = getMsgHeader(0x0107, attribute, SUB_PACKAGE_NO, 0, 0);
        bean.setMsgHeader(msgHeader);
        bean.setMsgBody(attribute);
        return generateData(bean);
        //JTT808Client.getInstance().writeAndFlush(generateData(bean));
    }

    public static JTT905Bean queryAtrribute905(String manufacturerId,String terminalModel,String terminalId,String iccid,
                                             String HW,String firmware){
        JTT905Bean bean = new JTT905Bean();
        ByteBuf attribute = Unpooled.buffer();
        //终端类型
        String type = "11110010".replace(" ", "");
        attribute.writeByte(Integer.parseInt(type, 2));
        //制造商 ID
        attribute.writeBytes(manufacturerId.getBytes());
        //终端型号
        attribute.writeBytes(ByteUtil.addZeroForNum(terminalModel,20));
        //终端 ID
        attribute.writeBytes(ByteUtil.addZeroForNum(terminalId,7));
        //ICCID
        attribute.writeBytes(ByteUtil.string2Bcd(iccid));
        //硬件版本号长度
        attribute.writeByte(HW.length());
        //硬件版本号
        attribute.writeBytes(HW.getBytes());
        //固件版本号长度
        attribute.writeByte(firmware.length());
        //固件版本号
        attribute.writeBytes(firmware.getBytes());
        //GNSS属性
        String gnss = "11110000".replace(" ", "");
        attribute.writeByte(Integer.parseInt(gnss, 2));
        //通信模块属性
        String network = "11111111".replace(" ", "");
        attribute.writeByte(Integer.parseInt(network, 2));

        JTT905Bean.MsgHeader msgHeader = getMsgHeader(0x0107, attribute);
        bean.setMsgHeader(msgHeader);
        bean.setMsgBody(attribute);
        return generateData(bean);
        //JTT808Client.getInstance().writeAndFlush(generateData(bean));
    }



    /**
     * 打包实时视频RTP包
     *
     * @param data       H.264一帧的视频数据
     * @param phone      SIM卡号
     * @param liveClient 与服务器的连接
     */
    private static List<byte[]> videoDataList;
    public static long videoTotalPkg;
    public static byte[] spsByte;
    public static ByteBuf videoBuffer;
    public static long difIFrame, difFrame;
    public static synchronized void videoLive(byte[] data, int channelNum, String phone, LiveClient liveClient, int len,long timeStamp) {
        long current_timestamp=0;
        if (videoDataList == null) videoDataList = new ArrayList<>();
        if (len > everyPkgSize) {
            //分包的总包数
            videoTotalPkg = Math.round(Math.ceil(len / everyPkgSize));
          //  Log.d("videoLive", "totalPkg:" + videoTotalPkg);
            for (int i = 1; i <= videoTotalPkg; i++) {
                int end = (int) (i * everyPkgSize);
                if (end >= len) {
                    end = len;
                }
                spsByte = Arrays.copyOfRange(data, (int) ((i - 1) * everyPkgSize), end);
                videoDataList.add(spsByte);
            }
        } else {
            Log.d("videoLive", "totalPkg: 1");
            videoDataList.add(data);
        }
        for (int i = 0; i < videoDataList.size(); i++) {

            if(timeStamp>0)
                current_timestamp = timeStamp;
            else
                current_timestamp = System.currentTimeMillis();
            byte[] pkgData = videoDataList.get(i);

            videoBuffer = Unpooled.buffer();
            videoBuffer.writeBytes(flags);
            //              V  P X  CC
            String vpxcc = "10 0 0 0001".replace(" ", "");
            videoBuffer.writeByte(Integer.parseInt(vpxcc, 2));

            //            M    PT
            String mpt = "0 1100010".replace(" ", "");
            videoBuffer.writeByte(Integer.parseInt(mpt, 2));
            //包序号
            videoBuffer.writeBytes(ByteUtil.int2Word(RTP_VIDEO_COUNT));
            //SIM
            videoBuffer.writeBytes(ByteUtil.string2Bcd(phone));
            //逻辑通道号
            videoBuffer.writeByte(channelNum);

            String dataType = "";
            byte NALU = NV21EncoderH264.findNALU(0, data)[0];
            if (((NALU & 0x1F) == 5)||((NALU & 0x1F) == 7)) {
                //0000 视频I帧 0001 视频P帧 0010 视频B帧 0011 音频帧 0100 透传数据
                dataType = "0000";
                LAST_I_FRAME_TIME = current_timestamp;
             } else {
                dataType = "0001";
              }
            //分包标记
            if (videoDataList.size() == 1) {
                //不分包
                dataType += "0000";
            } else if (i == 0) {
                //第一个包
                dataType += "0001";
            } else if (i == videoDataList.size() - 1) {
                //最后一个包
                dataType += "0010";
            } else {
                //中间包
                dataType += "0011";
            }
            //数据类型 分分包标记
            videoBuffer.writeByte(Integer.parseInt(dataType, 2));
            //时间戳
            videoBuffer.writeBytes(ByteUtil.long2Bytes(current_timestamp));
            //Last I Frame Interval
            difIFrame = current_timestamp - LAST_I_FRAME_TIME;
            videoBuffer.writeBytes(ByteUtil.int2Word(difIFrame));
            //Last Frame Interval
            difFrame = current_timestamp - LAST_FRAME_TIME;
            videoBuffer.writeBytes(ByteUtil.int2Word(difFrame));
            //数据体长度
            videoBuffer.writeBytes(ByteUtil.int2Word(pkgData.length));
            //数据体
            videoBuffer.writeBytes(pkgData);
            //发送数据
            if (liveClient != null) {
                liveClient.sendData(ByteBufUtil.toArray(videoBuffer));
            }
            RTP_VIDEO_COUNT++;
        }
        LAST_FRAME_TIME = current_timestamp;
        videoDataList.clear();
        videoBuffer.clear();
        videoBuffer.release();
        videoBuffer=null;

    }


    /**
     * 打包实时音频RTP包
     *
     * @param data       音频数据
     * @param phone      SIM
     * @param liveClient
     */
    private static List<byte[]> audioDataList;
    private static ByteBuf audioBuffer;

    public static synchronized void audioLive(byte[] data, int channelNum, String phone, LiveClient liveClient, int len, Context context,long timeStamp,int audioType) {
        if (audioDataList == null) audioDataList = new ArrayList<>();
        if(audioBuffer==null)audioBuffer = Unpooled.buffer();

        audioBuffer.writeBytes(flags);
        //              V  P X  CC

        String vpxcc = "10 0 0 0001".replace(" ", "");
        audioBuffer.writeByte(Integer.parseInt(vpxcc, 2));
        //            M    PT
        String mpt = "0 0000110";
        if(audioType==0) {
            //g711a
             mpt = "0 0000110".replace(" ", "");
        }else if(audioType==1){
            //aac
             mpt = "0 0010011".replace(" ", "");
        }

        audioBuffer.writeByte(Integer.parseInt(mpt, 2));
        //包序号
        audioBuffer.writeBytes(ByteUtil.int2Word(RTP_VIDEO_COUNT));
        //SIM
        audioBuffer.writeBytes(ByteUtil.string2Bcd(phone));
        //逻辑通道号
        audioBuffer.writeByte(channelNum);
        //音频帧
        String dataType = "0011";
        //不分包标记
        dataType += "0000";
        audioBuffer.writeByte(Integer.parseInt(dataType, 2));
        //时间戳
        if(timeStamp>0)
            audioBuffer.writeBytes(ByteUtil.long2Bytes(System.currentTimeMillis()));
        else
            audioBuffer.writeBytes(ByteUtil.long2Bytes(System.currentTimeMillis()));
        audioBuffer.writeBytes(ByteUtil.int2Word(len));
        //数据体
        audioBuffer.writeBytes(data, 0, len);
        //发送数据
       // Log.d("audiolive","len:"+len+" data:"+ByteUtil.bytesToHex(ByteBufUtil.toArray(audioBuffer)));
        if (liveClient != null) {
            liveClient.sendData(ByteBufUtil.toArray(audioBuffer));
        }
        RTP_VIDEO_COUNT++;
        audioBuffer.clear();
        audioBuffer.release();
        audioBuffer=null;
    }

    /**
     * 位置信息汇报
     *
     * @param lat 纬度
     * @param lng 经度
     * @return
     */
    public static byte[] locationInfo(long lat, long lng,int speed,int alarm,int acc) {
        ByteBuf locationInfo = Unpooled.buffer();
        //报警标志
        locationInfo.writeInt(alarm);
        //32 位二进制 从高到低位
        String radix2State="00000000000000000000000000000010";
        if(acc==0) {
            radix2State="00000000000000000000000000000010";
        }else if(acc==1){
         //   locationInfo.writeInt(acc);
            radix2State="00000000000000000000000000000011";
        }
        byte[] state = ByteUtil.int2Bytes(Integer.parseInt(radix2State, 2));
        //状态
        locationInfo.writeBytes(state);
        //经纬度
        locationInfo.writeBytes(ByteUtil.longToDword(lat));
        locationInfo.writeBytes(ByteUtil.longToDword(lng));
        //高程
        locationInfo.writeShort(0);
        //速度
        locationInfo.writeShort(speed);
        //方向
        locationInfo.writeShort(0);
        //bcd时间
        byte[] bcdTime = TimeUtil.getBcdTime();
        locationInfo.writeBytes(bcdTime);
        return ByteBufUtil.toArray(locationInfo);
    }

    /**
     * 生成消息头
     *
     * @param body       消息体
     * @param subpackage 是否分包 0:不分包 1:分包
     */
    private static JTT808Bean.MsgHeader getMsgHeader(int msgId, ByteBuf body, int subpackage,
                                                     long totalPkg, long pkgNo) {
        JTT808Bean.MsgHeader msgHeader = new JTT808Bean.MsgHeader();
        msgHeader.setMsgId(ByteUtil.int2Word(msgId));
        msgHeader.setMsgAttributes(msgBodyAttributes(body.readableBytes(), subpackage));

        String phone = JTT808Manager.getInstance().getPhone();
        if(JTT808Manager.getInstance().getProtocol() == JTT808Manager.Protocol.jtt808_2019){
            byte[] temp = {0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0};
            byte[] phonebcd = ByteUtil.string2Bcd(phone);
            int start = temp.length - phonebcd.length;
            for(int i=0;i<phonebcd.length;i++){
                temp[start + i] = phonebcd[i];
            }
            msgHeader.setTerminalPhone(temp);
        }else {
            msgHeader.setTerminalPhone(ByteUtil.string2Bcd(phone));
        }

        msgHeader.setFlowNum(ByteUtil.int2Word(FLOW_NUM++));
        //分包
        if (subpackage == SUB_PACKAGE_YES) {
            byte[] totalPkgB = ByteUtil.int2Word(totalPkg);
            byte[] pkgNoB = ByteUtil.int2Word(pkgNo);
            msgHeader.setSubpackage(ByteUtil.byteMergerAll(totalPkgB, pkgNoB));
        }
        if(msgId!=0x0200)
            Log.d(TAG, "msgId:" + Integer.toHexString(msgId));
        return msgHeader;
    }

    /**
     * 生成完整的808数据
     *
     * @param bean
     * @return
     */
    private static JTT808Bean generateData(JTT808Bean bean) {
        //计算校验码(使用消息头和消息体去计算)
        ByteBuf all = Unpooled.buffer();
        all.writeBytes(bean.getMsgHeader().all());
        all.writeBytes(bean.getMsgBody());
        //消息头 消息体
        byte[] headAndBody = ByteBufUtil.toArray(all);
        //校验码
        byte checkCode = HexUtil.getBCC(headAndBody);
        bean.setCheckCode(checkCode);
        all.writeByte(bean.getCheckCode());

        //消息头 消息体 校验码
        byte[] data = ByteBufUtil.toArray(all);
        //转义 7E和7D
        ByteBuf escapeBuf = escape7E7D(data);

        //最终传输给服务器的数据
        all.clear();
        all.writeByte(bean.getStartFlag());
        all.writeBytes(escapeBuf);
        all.writeByte(bean.getEndFlag());
        bean.setData(ByteBufUtil.toArray(all));
        return bean;
    }

    /**
     * 生成消息体属性
     *
     * @param subpackage [13]是否分包 0:不分包 1:分包
     */
    private static byte[] msgBodyAttributes(int msgLength, int subpackage) {
        //[1,10]消息体长度
        String binary = Integer.toBinaryString(msgLength);
        StringBuilder msgBody = new StringBuilder();
        for (int i = 0; i < 10 - binary.length(); i++) {
            msgBody.append("0");
        }
        msgBody.append(binary);
        //[10,12]数据加密方式 0 表示不加密
        String encryption = "000";
        //[13]分包
        String subpackageB = String.valueOf(subpackage);
        //[14,15]保留位
        String reserve = "00";
        if(JTT808Manager.getInstance().getProtocol() == JTT808Manager.Protocol.jtt808_2019){
            reserve = "01";
        }
        String msgAttributes = reserve + subpackageB + encryption + msgBody;
        // 消息体属性
        int msgBodyAttr = Integer.parseInt(msgAttributes, 2);
        return ByteUtil.int2Word(msgBodyAttr);
    }

    /**
     * 传视频资源列表  JTT1078
     */
    public static JTT808Bean uploadVideoList(byte[] flowNo, int channelNum,boolean isSub, int totlePackage,int packageNum,List<timeBean> list) {
        Log.d("upload","list:"+list.size());
        long totalPackage=0;
        List<byte[]> dataList = new ArrayList<>();
        byte[] data=null;
        JTT808Bean bean = new JTT808Bean();
        ByteBuf videoList = Unpooled.buffer();
        //流水号
        videoList.writeBytes(flowNo);
        //视频总数
        videoList.writeBytes(ByteUtil.longToDword(list.size()));
        for (int i = 0; i < list.size(); i++) {
            //通道号
            videoList.writeByte(ByteUtil.int2Byte(list.get(i).getChannelNum()));
            //开始时间
            videoList.writeBytes(ByteUtil.string2Bcd(list.get(i).getStartTime()));
            //结束时间
            videoList.writeBytes(ByteUtil.string2Bcd(list.get(i).getEndTime()));
            //报警标志 0 没有报警
            videoList.writeBytes(ByteUtil.long2Bytes(0));
            //音视频资源类型 0： 音视频 1：音频 2：视频
            videoList.writeByte(0);
            //码流类型 1主码流 2子码流
            videoList.writeByte(1);
            //存储类型 1主存储器 2灾备存储器
            videoList.writeByte(1);
            //文件大小
            videoList.writeBytes(ByteUtil.longToDword(list.get(i).getSize()));
        }
        int len = videoList.readableBytes();
        byte[] dstBytes = new byte[len];
        videoList.readBytes(dstBytes,0,videoList.readableBytes());
        if (len > everyPkgSize) {
            //分包的总包数
            totalPackage = Math.round(Math.ceil(len / everyPkgSize));
            Log.d("upload", "totalPkg:" + totalPackage);
            for (int i = 1; i <= totalPackage; i++) {
                int end = (int) (i * everyPkgSize);
                if (end >= len) {
                    end = len;
                }
                data = Arrays.copyOfRange(dstBytes,(int) ((i - 1) * everyPkgSize), end);
              dataList.add(data);
            }
        }
        if(dataList.size()>1){
            for (int i=0;i<dataList.size();i++){
                ByteBuf videoList2 = Unpooled.buffer();
                videoList2.writeBytes(dataList.get(i));
                JTT808Bean.MsgHeader msgHeader = getMsgHeader(0x1205,videoList2, SUB_PACKAGE_YES, totalPackage, i+1);
                bean.setMsgHeader(msgHeader);
                bean.setMsgBody(videoList2);
                JTT808Client.getInstance().writeAndFlush(generateData(bean));
            }
        }else {
            videoList.writeBytes(dstBytes);
            JTT808Bean.MsgHeader msgHeader = getMsgHeader(0x1205, videoList, SUB_PACKAGE_NO, 0, 0);
            bean.setMsgHeader(msgHeader);
            bean.setMsgBody(videoList);
            JTT808Client.getInstance().writeAndFlush(generateData(bean));
        }
        return generateData(bean);
    }

    public static JTT808Bean uploadVideoListLocal(byte[] flowNo, int channelNum,boolean isSub, int totlePackage,int packageNum,List<timeBean> list) {
        Log.d("upload","list:"+list.size());
        long totalPackage=0;
        List<byte[]> dataList = new ArrayList<>();
        byte[] data=null;
        JTT808Bean bean = new JTT808Bean();
        ByteBuf videoList = Unpooled.buffer();
        //流水号
        videoList.writeBytes(flowNo);
        //视频总数
        videoList.writeBytes(ByteUtil.longToDword(list.size()));
        for (int i = 0; i < list.size(); i++) {
            //通道号
            videoList.writeByte(ByteUtil.int2Byte(list.get(i).getChannelNum()));
            //开始时间
            videoList.writeBytes(ByteUtil.string2Bcd(list.get(i).getStartTime()));
            //结束时间
            videoList.writeBytes(ByteUtil.string2Bcd(list.get(i).getEndTime()));
            //报警标志 0 没有报警
            videoList.writeBytes(ByteUtil.long2Bytes(0));
            //音视频资源类型 0： 音视频 1：音频 2：视频
            videoList.writeByte(0);
            //码流类型 1主码流 2子码流
            videoList.writeByte(1);
            //存储类型 1主存储器 2灾备存储器
            videoList.writeByte(1);
            //文件大小
            videoList.writeBytes(ByteUtil.longToDword(list.get(i).getSize()));
        }
        int len = videoList.readableBytes();
        byte[] dstBytes = new byte[len];
        videoList.readBytes(dstBytes,0,videoList.readableBytes());
        if (len > everyPkgSize) {
            //分包的总包数
            totalPackage = Math.round(Math.ceil(len / everyPkgSize));
            Log.d("upload", "totalPkg:" + totalPackage);
            for (int i = 1; i <= totalPackage; i++) {
                int end = (int) (i * everyPkgSize);
                if (end >= len) {
                    end = len;
                }
                data = Arrays.copyOfRange(dstBytes,(int) ((i - 1) * everyPkgSize), end);
                dataList.add(data);
            }
        }
        if(dataList.size()>1){
            for (int i=0;i<dataList.size();i++){
                ByteBuf videoList2 = Unpooled.buffer();
                videoList2.writeBytes(dataList.get(i));
                JTT808Bean.MsgHeader msgHeader = getMsgHeader(0x1205,videoList2, SUB_PACKAGE_YES, totalPackage, i+1);
                bean.setMsgHeader(msgHeader);
                bean.setMsgBody(videoList2);
                JTT808ClientLocal.getInstance().writeAndFlush(generateData(bean));
            }
        }else {
            videoList.writeBytes(dstBytes);
            JTT808Bean.MsgHeader msgHeader = getMsgHeader(0x1205, videoList, SUB_PACKAGE_NO, 0, 0);
            bean.setMsgHeader(msgHeader);
            bean.setMsgBody(videoList);
            JTT808ClientLocal.getInstance().writeAndFlush(generateData(bean));
        }
        return generateData(bean);
    }

    /**
     * 转义 7E  ——> 7D 02
     * 转义 7D ——> 7D 01
     */
    private static ByteBuf escape7E7D(byte[] bytes) {
        ByteBuf buffer = Unpooled.buffer();
        for (byte b : bytes) {
            if (b == 0x7E) {
                buffer.writeByte(0x7D);
                buffer.writeByte(0x02);
            } else if (b == 0x7D) {
                buffer.writeByte(0x7D);
                buffer.writeByte(0x01);
            } else {
                buffer.writeByte(b);
            }
        }
        return buffer;
    }

    public static JTT808Bean getMediaInfo() {
        long totalPackage=0;
        List<byte[]> dataList = new ArrayList<>();
        byte[] data=null;
        JTT808Bean bean = new JTT808Bean();
        ByteBuf msgBody = Unpooled.buffer();

        msgBody.writeByte(19);
        msgBody.writeByte(1);
        msgBody.writeByte(0);
        msgBody.writeByte(1);

        int frameSize;
        frameSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_AAC_LC);

        msgBody.writeShort(frameSize>800?800:frameSize);

        msgBody.writeByte(1);
        msgBody.writeByte(98);
        msgBody.writeByte(1);
        msgBody.writeByte(2);


        JTT808Bean.MsgHeader msgHeader = getMsgHeader(0x1003, msgBody, SUB_PACKAGE_NO, 0, 0);
        bean.setMsgHeader(msgHeader);
        bean.setMsgBody(msgBody);

        return generateData(bean);
    }
    public static void JT808_0x0104(JTT808Bean bean)
    {
        ByteBuf msgBody = Unpooled.buffer();
        JTT808Bean result = new JTT808Bean();
        JTT808Bean.MsgHeader msgHeader = getMsgHeader(0x0104, msgBody, SUB_PACKAGE_NO, 0, 0);
        msgBody.writeBytes(ByteUtil.int2Word(FLOW_NUM));
        msgBody.writeByte(0);
        result.setMsgHeader(msgHeader);
        bean.setMsgBody(msgBody);
        JTT808Client.getInstance().writeAndFlush(result);
    }


    /**
     * 鉴权
     *
     * @param authCode 鉴权码
     */
    public static JTT905Bean auth(byte[] authCode,boolean isJtt808) {
        ByteBuf auth = Unpooled.buffer(authCode.length);
        auth.writeBytes(authCode);
        JTT905Bean bean = new JTT905Bean();
        JTT905Bean.MsgHeader msgHeader = getMsgHeader(0x0102, auth);
        bean.setMsgHeader(msgHeader);
        bean.setMsgBody(auth);
        return generateData(bean);
    }


    /**
     * 生成消息头
     *
     * @param body       消息体
     */
    private static JTT905Bean.MsgHeader getMsgHeader(int msgId, ByteBuf body) {
        JTT905Bean.MsgHeader msgHeader = new JTT905Bean.MsgHeader();
        msgHeader.setMsgId(ByteUtil.int2Word(msgId));
        msgHeader.setMsgAttributes(ByteUtil.int2Word(body.readableBytes()));

        String isu = JTT808Manager.getInstance().getISU();
        msgHeader.setISU(ByteUtil.string2Bcd(isu));

        msgHeader.setFlowNum(ByteUtil.int2Word(FLOW_NUM++));

        if(msgId!=0x0200)
            Log.d(TAG, "msgId:" + Integer.toHexString(msgId));
        return msgHeader;
    }

    /**
     * 生成完整的905数据
     *
     * @param bean
     * @return
     */
    private static JTT905Bean generateData(JTT905Bean bean) {
        //计算校验码(使用消息头和消息体去计算)
        ByteBuf all = Unpooled.buffer();
        all.writeBytes(bean.getMsgHeader().all());
        Log.e("JTT905","鉴权码01：" + HexUtil.byte2HexStr(ByteBufUtil.toArray(bean.getMsgBody())));
        all.writeBytes(bean.getMsgBody());
        Log.e("JTT905","鉴权码02：" + HexUtil.byte2HexStr(ByteBufUtil.toArray(bean.getMsgBody())));
        //消息头 消息体
        byte[] headAndBody = ByteBufUtil.toArray(all);
        //校验码
        byte checkCode = HexUtil.getBCC(headAndBody);
        bean.setCheckCode(checkCode);
        all.writeByte(bean.getCheckCode());
        Log.e("JTT905","鉴权码03：" + HexUtil.byte2HexStr(ByteBufUtil.toArray(bean.getMsgBody())));
        //消息头 消息体 校验码
        byte[] data = ByteBufUtil.toArray(all);
        //转义 7E和7D
        ByteBuf escapeBuf = escape7E7D(data);

        //最终传输给服务器的数据
        all.clear();
        all.writeByte(bean.getStartFlag());
        all.writeBytes(escapeBuf);
        all.writeByte(bean.getEndFlag());
        Log.e("JTT905","鉴权码04：" + HexUtil.byte2HexStr(ByteBufUtil.toArray(bean.getMsgBody())));
        bean.setData(ByteBufUtil.toArray(all));
        Log.e("JTT905","鉴权码05：" + HexUtil.byte2HexStr(ByteBufUtil.toArray(bean.getMsgBody())));
        return bean;
    }

    public static void JT905_0x0104(JTT905Bean bean)
    {
        ByteBuf msgBody = Unpooled.buffer();
        JTT905Bean result = new JTT905Bean();
        JTT905Bean.MsgHeader msgHeader = getMsgHeader(0x0104, msgBody);
        msgBody.writeBytes(ByteUtil.int2Word(FLOW_NUM));
        //msgBody.writeByte(0);
        result.setMsgHeader(msgHeader);
        bean.setMsgBody(msgBody);
        JTT905Client.getInstance().writeAndFlush(result);
    }

    /**
     * 注册
     *
     * @param manufacturerId 制造商 ID
     * @param terminalModel  终端型号
     * @param terminalId     终端 ID
     */
    public static JTT905Bean register(String manufacturerId, String terminalModel, String terminalId,boolean isGtt808) {
        ByteBuf register = Unpooled.buffer();
        //省域 ID
        register.writeShort(0);
        //省域 市县域 ID
        register.writeShort(0);
        //制造商 ID
        register.writeBytes(manufacturerId.getBytes());
        //终端型号
        register.writeBytes(ByteUtil.addZeroForNum(terminalModel,20));
        //终端 ID
        register.writeBytes(ByteUtil.addZeroForNum(terminalId,7));
        //车牌颜色(车牌颜色，按照 JT/T415-2006 的 5.4.12。 未上牌时，取值为 0)
        register.writeByte(0);
        //车辆标识(车牌颜色为 0 时，表示车辆 VIN; 否则，表示公安交通管理部门颁发的机动车号牌。)
        register.writeByte(0);

        JTT905Bean bean = new JTT905Bean();

        JTT905Bean.MsgHeader msgHeader = getMsgHeader(0x0100, register);

        bean.setMsgHeader(msgHeader);
        bean.setMsgBody(register);

        return generateData(bean);
    }

    public static JTT905Bean uploadLocation(long lat, long lng,int speed,int alarm,int acc,boolean isJtt808) {
        byte[] info = locationInfo(lat, lng,speed,alarm,acc,false);
        ByteBuf location = Unpooled.buffer(info.length);
        location.writeBytes(info);
        JTT905Bean bean = new JTT905Bean();
        JTT905Bean.MsgHeader msgHeader = getMsgHeader(0x0200, location);
        bean.setMsgHeader(msgHeader);
        bean.setMsgBody(location);
        return generateData(bean);
    }

    public static JTT905Bean heartBeat(boolean isJtt808) {
        ByteBuf heart = Unpooled.EMPTY_BUFFER;
        JTT905Bean bean = new JTT905Bean();
        JTT905Bean.MsgHeader msgHeader = getMsgHeader(0x0002, heart);
        bean.setMsgHeader(msgHeader);
        bean.setMsgBody(heart);
        return generateData(bean);
    }

    /**
     * 位置信息汇报
     *
     * @param lat 纬度
     * @param lng 经度
     * @return
     */
    public static byte[] locationInfo(long lat, long lng,int speed,int alarm,int acc,boolean isJtt808) {
        ByteBuf locationInfo = Unpooled.buffer();
        //报警标志
        locationInfo.writeInt(alarm);
        //32 位二进制 从高到低位
        String radix2State="00000000000000000000000000000000";
        if(acc==0) {
            radix2State="00000000000000000000000000000000";
        }else if(acc==1){
            radix2State="00000000000000000000001100000000";
        }
        byte[] state = ByteUtil.int2Bytes(Integer.parseInt(radix2State, 2));
        //状态
        locationInfo.writeBytes(state);
        //经纬度

        locationInfo.writeBytes(ByteUtil.longToDword(lat));
        locationInfo.writeBytes(ByteUtil.longToDword(lng));
        //高程
        //locationInfo.writeShort(0);
        //速度
        locationInfo.writeShort(speed);
        //方向
        locationInfo.writeShort(0);
        //bcd时间
        byte[] bcdTime = TimeUtil.getBcdTime();
        locationInfo.writeBytes(bcdTime);
        return ByteBufUtil.toArray(locationInfo);
    }

    public static byte[] floatToByteArray(float f) {

        return ByteBuffer.allocate(4).putFloat(f).array();

    }

    public static JTT905Bean universalResponse(byte[] flowNum, byte[] msgId,boolean isJtt808) {
        ByteBuf universal = Unpooled.buffer(5);
        universal.writeBytes(flowNum);
        universal.writeBytes(msgId);
        //0:成功/确认;1:失败;2:消息有误;3:不支持
        universal.writeByte(0);
        JTT905Bean bean = new JTT905Bean();
        JTT905Bean.MsgHeader msgHeader = getMsgHeader(0x0001, universal);
        bean.setMsgHeader(msgHeader);
        bean.setMsgBody(universal);
        return generateData(bean);
    }
}
