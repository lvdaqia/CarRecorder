package com.azhon.jtt808.netty.live;

import android.util.Log;

import com.azhon.jtt808.bean.JTT808Bean;
import com.azhon.jtt808.listener.OnConnectionListener;
import com.azhon.jtt808.netty.JTT808Client;
import com.azhon.jtt808.netty.JTT808Decoder;
import com.azhon.jtt808.netty.JTT808Handler;
import com.azhon.jtt808.util.ByteBufUtil;
import com.azhon.jtt808.util.ByteUtil;

import java.net.InetSocketAddress;
import java.util.List;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt808.netty.live
 * 文件名:    LiveClient
 * 创建时间:  2020/2/26 on 20:51
 * 描述:     TODO 视频监控推流
 *
 * @author luozhihao
 */

public class LiveClient {

    private static final String TAG = "LiveClient";
    private String ip;
    private int port;
    private Channel channel;
    private byte[] mdata;
    private byte[] head;
    private byte[] vpx;
    private byte[] packageNum;
    private byte[] sim;
    private byte[] channleNum;
    private byte[] dataType;
    private byte[] time;
    private byte[] dataLen;
    private ReceiveLiveClientDataCallBack callBack;
    public void setLiveClientCallBack(ReceiveLiveClientDataCallBack callBack){
        this.callBack = callBack;
        Log.d(TAG, "callBack");
    }
    public interface ReceiveLiveClientDataCallBack{
        void receiveData(byte[] head,byte[] vpx,byte[] packageNum,byte[] sim,byte[] channleNum,
                         byte[] dataType,byte[] time,byte[] dataLen,byte[] mdata);
    }
    public LiveClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
        connect();
    }

    /**
     * 初始化连接
     */
    private void connect() {
        try {
            NioEventLoopGroup group = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap()
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(65535))
                    .channel(NioSocketChannel.class)
                    .group(group)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            //解码器
                            pipeline.addLast(
                                    new ByteToMessageDecoder() {
                                        @Override
                                        protected void decode(ChannelHandlerContext channelHandlerContext,
                                                              ByteBuf byteBuf, List<Object> list) throws Exception {
                                            Log.d("RecorActivity", "收到了数据： "+"\n"+ByteUtil.bytesToHex(ByteBufUtil.toArray(byteBuf)));

//30316364     81e2          35e7   018923779737   01         30      0000017b8557477b 0140cbcdde59e1dacdc6d8cbd4e741dc5e42c6dc4c56d6c751ccf2f8fffee5fec0dbd2c2f0d450d7d4497c5e7b7b67d7547d4b47cc5f475b55d2cbc3c9def3cef3de4fd15dcf55daddd1f15545f35ad8d3dcfdd157c3fdfbdad64958d7794a4c434f7275747373655b677a77487051c5def95e4df5f7ccf541cdc3d0d9c5c8d4537c42f0564154ced840dad5f4d264c7f5cef0deccc1f7cdc7ce4350f5d553667d4a5d4877c1dad7caddc6c7d1faf7f3c4f4c5c3cd51c350677c64607b626661707873407552d6ccc4d2f1f0d1f0cbfcdbdbd8775648537446d0c759d4c9f3dac7d8c0f571f5e5c7dd59c1c7425a4e4e4a75737b4264637d7e70637350d3c256cadfd349cffbf3c0cec7c04e58c9c2f77558f0e6da54cde0e551cffbf8ccc0f0f9f9ca5afae4cff1e4f9c4575cda704853c3e5241c2cd59dbc7c85cf5fefadcfdc9
// 帧头标识  V P X CC M PT    包序号     SIM       逻辑通道号    数据类型   分包处理标志   长度                byteBuf.clear();
                                            if(head==null)  head = new byte[4];
                                            if(vpx==null)   vpx = new byte[2];
                                            if(packageNum==null)  packageNum = new byte[2];
                                            if(sim==null)   sim = new byte[6];
                                            if(channleNum==null)   channleNum = new byte[1];
                                            if(dataType==null)  dataType = new byte[1];
                                            if(time==null)  time = new byte[8];
                                            if(dataLen==null) dataLen = new byte[2];
                                            head = byteBuf.readBytes(4).array();
                                            vpx = byteBuf.readBytes(2).array();
                                            packageNum = byteBuf.readBytes(2).array();
                                            sim = byteBuf.readBytes(6).array();
                                            channleNum = byteBuf.readBytes(1).array();
                                            dataType = byteBuf.readBytes(1).array();
                                            time = byteBuf.readBytes(8).array();
                                            dataLen = byteBuf.readBytes(2).array();
                                            mdata = byteBuf.readBytes(byteBuf.readableBytes()).array();
                                          //  if(mdata==null)mdata = new byte[byteBuf.readableBytes()];
                                          //  byteBuf.readBytes(mdata,0,byteBuf.readableBytes());
                                            callBack.receiveData(head,vpx,packageNum,sim,channleNum,dataType,time,dataLen,mdata);
                                            byteBuf.clear();
                                        }
                                    }
                                    //编码器
                            ).addLast(
                                    new MessageToByteEncoder<byte[]>() {
                                        @Override
                                        protected void encode(ChannelHandlerContext channelHandlerContext,
                                                              byte[] data, ByteBuf byteBuf) throws Exception {
                                            byteBuf.writeBytes(data);

                                        }
                                    }
                            );
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(ip, port));
            channel = channelFuture.sync().channel();
            Log.d(TAG, "实时监控服务器连接成功");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "实时监控服务器连接失败：" + e.getMessage());
        }
    }
//53dbd2c9f6cdfbf6f5cbc7f7f5c55551d857584c734271434e45
//3031636481e200c401892377973701300000017b9a40db9a0140
    /**
     * 发送数据
     *
     * @param data
     */
    public synchronized void sendData(byte[] data) {
        if (channel != null) {
            channel.writeAndFlush(data);
        }
    }

    public void release() {
        if (channel != null) {
            Log.d("LiveClient","release");
            channel.disconnect();
        }
    }

}
