package com.azhon.jtt808.netty;

import android.util.Log;

import com.azhon.jtt808.bean.JTT808Bean;
import com.azhon.jtt808.bean.JTT905Bean;
import com.azhon.jtt808.util.ByteUtil;
import com.azhon.jtt808.util.JTT808Util;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt808.netty
 * 文件名:    JTT808Decoder
 * 创建时间:  2020/1/4 on 19:43
 * 描述:     TODO
 *
 * @author luozhihao
 */

public class JTT905Decoder extends ByteToMessageDecoder {

    private static final String TAG = "JTT905Decoder";

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        JTT905Bean jtt905Bean = resolve(byteBuf);
        list.add(jtt905Bean);
        Log.d("JTT905Decoder:","");
    }

    /**
     * 解析数据
     *
     * @param byteBuf
     */
    public static JTT905Bean resolve(ByteBuf byteBuf) {
        ByteBuf escapeBuf = escape7D(byteBuf);

        JTT905Bean jtt905Bean = new JTT905Bean();
        //解析消息头
        JTT905Bean.MsgHeader msgHeader = new JTT905Bean.MsgHeader();
        byte[] msgId = escapeBuf.readBytes(2).array();
        byte[] msgAttributes = escapeBuf.readBytes(2).array();
        byte[] ISU = escapeBuf.readBytes(6).array();
        byte[] flowNum = escapeBuf.readBytes(2).array();

        msgHeader.setMsgId(msgId);
        msgHeader.setMsgAttributes(msgAttributes);
        msgHeader.setISU(ISU);
        msgHeader.setFlowNum(flowNum);

        //消息体长度
        int msgBodyAttr = ByteUtil.bytes2Int(msgAttributes);
       /* int[] msgBodyAttr = resolveMsgBodyLength(msgAttributes);
        if (msgBodyAttr[0] == JTT808Util.SUB_PACKAGE_YES) {
            //TODO 分包
            escapeBuf.readBytes(4);
        }*/
        //消息体
        ByteBuf msgBody = escapeBuf.readBytes(msgBodyAttr);
        //校验码
        byte checkCode = escapeBuf.readByte();

        jtt905Bean.setMsgHeader(msgHeader);
        jtt905Bean.setMsgBody(msgBody);
        jtt905Bean.setCheckCode(checkCode);

        return jtt905Bean;
    }

    /**
     * 转义 7D 02->7E  7D 01->7D
     *
     * @param byteBuf
     */
    private static ByteBuf escape7D(ByteBuf byteBuf) {
        ByteBuf escapeBuf = Unpooled.buffer();
        int length = byteBuf.readableBytes();
        for (; byteBuf.readerIndex() < length; ) {
            byte b = byteBuf.readByte();
            if (b == 0x7D) {
                byte nextB = byteBuf.readByte();
                if (nextB == 0x02) {
                    escapeBuf.writeByte(0x7E);
                } else if (nextB == 0x01) {
                    escapeBuf.writeByte(0x7D);
                } else {
                    escapeBuf.writeByte(b);
                }
            } else {
                escapeBuf.writeByte(b);
            }
        }
        return escapeBuf;
    }

    /**
     * 解析消息体属性
     *
     * @return
     */
    private static int[] resolveMsgBodyLength(byte[] msgAttributes) {
        ByteBuf msgAttr = Unpooled.buffer(16);
        for (byte attribute : msgAttributes) {
            msgAttr.writeBytes(ByteUtil.byteToBit(attribute));
        }
        //保留位
        msgAttr.readBytes(2);
        //是否分包
        byte subpackage = msgAttr.readByte();
        //加密方式
        byte[] encrypt = msgAttr.readBytes(3).array();
        //消息体长度
        byte[] bodyLength = msgAttr.readBytes(10).array();
        String bits = "";
        for (byte b : bodyLength) {
            bits += b;
        }
        int msgBodyLength = Integer.parseInt(bits, 2);
        return new int[]{subpackage, msgBodyLength};
    }
}
