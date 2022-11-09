package com.azhon.jtt808.bean;

import com.azhon.jtt808.util.ByteBufUtil;
import com.azhon.jtt808.util.ByteUtil;
import com.azhon.jtt808.util.HexUtil;

import java.util.Arrays;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt808.bean
 * 文件名:    JTT808Bean
 * 创建时间:  2020/1/2 on 23:24
 * 描述:     TODO
 *
 * @author luozhihao
 */

public class JTT905Bean {
    //开始标识位
    private byte startFlag = 0x7E;
    //消息头
    private MsgHeader msgHeader;
    //消息体
    private ByteBuf msgBody;
    //校验码
    private byte checkCode;
    //结束标识位
    private byte endFlag = startFlag;
    //一条消息的所有字节
    private byte[] data;

    public void setStartFlag(byte startFlag) {
        this.startFlag = startFlag;
    }

    public byte getStartFlag() {
        return startFlag;
    }

    public MsgHeader getMsgHeader() {
        return msgHeader;
    }

    public void setMsgHeader(MsgHeader msgHeader) {
        this.msgHeader = msgHeader;
    }

    public ByteBuf getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(ByteBuf msgBody) {
        this.msgBody = msgBody;
    }

    public byte getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(byte checkCode) {
        this.checkCode = checkCode;
    }

    public byte getEndFlag() {
        return endFlag;
    }

    public void setEndFlag(byte endFlag) {
        this.endFlag = endFlag;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "JTT905Bean{" +
                "startFlag=" + startFlag +
                ", msgHeader=" + msgHeader +
                ", msgBody=" + HexUtil.byte2HexStr(ByteBufUtil.toArray(msgBody)) +
                ", checkCode=" + checkCode +
                ", endFlag=" + endFlag +
                '}';
    }

    public static class MsgHeader {
        //消息id 2个字节
        private byte[] msgId;
        //消息体属性 2个字节
        private byte[] msgAttributes;
        //ISU标识 6个字节
        private byte[] ISU;
        //流水号 2个字节
        private byte[] flowNum;

        public byte[] getMsgId() {
            return msgId;
        }

        public void setMsgId(byte[] msgId) {
            this.msgId = msgId;
        }

        public byte[] getMsgAttributes() {
            return msgAttributes;
        }

        public void setMsgAttributes(byte[] msgAttributes) {
            this.msgAttributes = msgAttributes;
        }

        public byte[] getISU() {
            return ISU;
        }

        public void setISU(byte[] terminalPhone) {
            this.ISU = terminalPhone;
        }

        public byte[] getFlowNum() {
            return flowNum;
        }

        public void setFlowNum(byte[] flowNum) {
            this.flowNum = flowNum;
        }


        public byte[] all() {
            ByteBuf all = Unpooled.buffer();
            all.writeBytes(getMsgId());
            all.writeBytes(getMsgAttributes());
            all.writeBytes(getISU());
            all.writeBytes(getFlowNum());
            return ByteBufUtil.toArray(all);
        }

        @Override
        public String toString() {
            return "MsgHeader{" +
                    "msgId=" + HexUtil.byte2HexStrNoSpace(msgId) +
                    ", msgAttributes=" + ByteUtil.bytes2Int(msgAttributes) +
                    ", ISU=" + HexUtil.ByteToString(ISU) +
                    ", flowNum=" + ByteUtil.bytes2Int(flowNum) +
                    '}';
        }

    }
}
