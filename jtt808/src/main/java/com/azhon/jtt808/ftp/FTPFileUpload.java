package com.azhon.jtt808.ftp;

import android.os.Environment;
import android.util.Log;

import com.azhon.jtt808.JTT808Manager;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 项目名:    JTTProtocol
 * 包名       com.azhon.jtt808.ftp
 * 文件名:    FTPFileUpload
 * 创建时间:  2020/2/20 on 12:02
 * 描述:     TODO
 *
 * @author luozhihao
 */

public class FTPFileUpload {
    private static final String TAG = "FTPFileUpload";
    private String ip;
    private int port;
    private String username;
    private String password;
    private String remotePath;

    public FTPFileUpload(String ip, int port, String username, String password) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public FTPFileUpload(){}

    private static FTPFileUpload manager = new FTPFileUpload();
    public static FTPFileUpload getInstance() {
        return manager;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public boolean uploadFile(File file) {
        Log.d(TAG, "开始上传：" + file);
        //创建FTPClient对象
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip, port);
            boolean loginResult = ftpClient.login(username, password);
            int returnCode = ftpClient.getReplyCode();
            // 登录成功
            if (loginResult && FTPReply.isPositiveCompletion(returnCode)) {
                // 设置上传目录
                ftpClient.changeWorkingDirectory(remotePath);
                ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                FileInputStream fis = new FileInputStream(file);
                //写入文件
                ftpClient.storeFile(file.getName(), fis);
                fis.close();
                Log.d(TAG, "文件上传成功：" + file);
                return true;
            } else {
                Log.e(TAG, "FTP服务器登录失败...loginResult=" + loginResult + " returnCode=" + returnCode);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "FTP服务器登录失败..." + e.getMessage());
            return false;
        }
    }

    public synchronized void downloadApk(String ip,int port,String name,String passwrod,String fileName) throws IOException {
        boolean success = false;
        FTPClient client = new FTPClient();
        client.connect(ip,port);
        client.login(name,passwrod);
        int reply = client.getReplyCode();
        if(!FTPReply.isPositiveCompletion(reply)){
            Log.e(TAG,"无法连接到FTP服务器，错误码： " + reply);
            client.disconnect();
            return;
        }
       /* String remotePath = "";
        String charSet = "UTF-8";
        if (!FTPReply.isPositiveCompletion(client.sendCommand("OPTS UTF8", "ON"))) {    //向服务器请求使用"UTF-8"编码
            charSet = "GBK";
        }
        //如果路径是文件，只会返回一个长度为1的数组。
        FTPFile[] files = client.listFiles(new String(remotePath.getBytes(charSet), "ISO-8859-1")); //对remotePath进行编码转换
        FTPFile file = files[0];  //文件信息
        long size = file.getSize();
        String fileaName = new String(fileName.getBytes(), Charset.forName(charSet));*/

        File localFile = new File(Environment.getExternalStorageDirectory()+fileName);
        OutputStream ous=null;
        try{
            ous = new FileOutputStream(localFile);
            success = client.retrieveFile(fileName,ous);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            ous.close();
        }
        client.disconnect();
        if(success){
            //Demo.getInstance().run(getApplicationContext().getPackageManager(),localFile.getAbsolutePath());
        }
    }
}
