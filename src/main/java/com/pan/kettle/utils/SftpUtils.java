package com.pan.kettle.utils;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Properties;
import java.util.Vector;

@Slf4j
public class SftpUtils {

    private ChannelSftp sftp;

    private Session session;

    private String username;

    private String password;

    private String privateKey;

    private String host;

    private int port;

    public SftpUtils(String username, String password, String host, int port) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    public SftpUtils(String username, String host, int port, String privateKey) {
        this.username = username;
        this.host = host;
        this.port = port;
        this.privateKey = privateKey;
    }

    public SftpUtils() {
    }

    public void login() {
        try {
            JSch jsch = new JSch();
            if (this.privateKey != null) {
                jsch.addIdentity(this.privateKey);
                this.log.info("sftp connect,path of private key file{}", this.privateKey);
            }
            this.log.info("sftp connect by host:{} username:{}", this.host, this.username);
            this.session = jsch.getSession(this.username, this.host, this.port);
            this.log.info("Session is build");
            if (this.password != null) {
                this.session.setPassword(this.password);
            }
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            this.session.setConfig(config);
            this.session.connect();
            this.log.info("Session is connected");
            Channel channel = this.session.openChannel("sftp");
            channel.connect();
            this.log.info("channel is connected");
            this.sftp = (ChannelSftp) channel;
            this.log.info(String.format("sftp server host:[%s] port:[%s] is connect successfull", new Object[]{this.host, Integer.valueOf(this.port)}));
        } catch (JSchException e) {
            this.log.error("Cannot connect to specified sftp server : {}:{} \n Exception message is: {}", new Object[]{this.host, Integer.valueOf(this.port), e.getMessage()});
        }
    }

    /**
     * 递归根据路径创建文件夹
     *
     * @param dirs     根据 / 分隔后的数组文件夹名称
     * @param tempPath 拼接路径
     * @param length   文件夹的格式
     * @param index    数组下标
     * @return
     */
    public void mkdirDir(String[] dirs, String tempPath, int length, int index) {
        // 以"/a/b/c/d"为例按"/"分隔后,第0位是"";顾下标从1开始
        index++;
        if (index < length) {
            // 目录不存在，则创建文件夹
            tempPath += "/" + dirs[index];
        }
        try {
            log.info("检测目录[" + tempPath + "]");
            sftp.cd(tempPath);
            if (index < length) {
                mkdirDir(dirs, tempPath, length, index);
            }
        } catch (SftpException ex) {
            log.warn("创建目录[" + tempPath + "]");
            try {
                sftp.mkdir(tempPath);
                sftp.cd(tempPath);
            } catch (SftpException e) {
                log.error("创建目录[" + tempPath + "]失败,异常信息[" + e.getMessage() + "]");

            }
            log.info("进入目录[" + tempPath + "]");
            mkdirDir(dirs, tempPath, length, index);
        }
    }

    public void logout() {
        if (this.sftp != null &&
                this.sftp.isConnected()) {
            this.sftp.disconnect();
            this.log.info("sftp is closed already");
        }
        if (this.session != null &&
                this.session.isConnected()) {
            this.session.disconnect();
            this.log.info("sshSession is closed already");
        }
    }

    public void upload(String directory, String sftpFileName, InputStream input) throws SftpException {
        try {
            this.sftp.cd("/");
            String[] dirs = null;
            if (directory.contains("\\")) {
                dirs = directory.split("\\\\");
            } else if (directory.contains("/")) {
                dirs = directory.split("/");
            }
            if(null != dirs){
                String tempPath = "";
                int index = 0;
                mkdirDir(dirs, tempPath, dirs.length, index);
            }else {
                this.sftp.cd(directory);
            }
            this.sftp.put(input, sftpFileName);
            this.log.info("file:{} is upload successful", sftpFileName);
        }catch (Exception e){
            log.error(e.getMessage() , e);
        }finally {
            if(null != input){
                try {
                    input.close();
                }catch (Exception e){
                }
            }
        }

    }

    public void upload(String directory, String uploadFile) throws FileNotFoundException, SftpException {
        File file = new File(uploadFile);
        upload(directory, file.getName(), new FileInputStream(file));
    }

    public void upload(String directory, String sftpFileName, byte[] byteArr) throws SftpException {
        upload(directory, sftpFileName, new ByteArrayInputStream(byteArr));
    }

    public void upload(String directory, String sftpFileName, String dataStr, String charsetName) throws UnsupportedEncodingException, SftpException {
        upload(directory, sftpFileName, new ByteArrayInputStream(dataStr.getBytes(charsetName)));
    }

    public void download(String directory, String downloadFile, String saveFile) throws SftpException, FileNotFoundException {
        cdPaths(directory);
        File file = new File(saveFile);
        this.sftp.get(downloadFile, new FileOutputStream(file));
        this.log.info("file:{} is download successful", downloadFile);
    }

    public void cdPaths(String directory) throws SftpException {
        this.sftp.cd("/");
        if (directory != null && !"".equals(directory)) {
            String[] dirs = null;
            if (directory.contains("\\")) {
                dirs = directory.split("\\\\");
            } else if (directory.contains("/")) {
                dirs = directory.split("/");
            }
            if(null != dirs){
                String tempPath = "";
                int index = 0;
                for(String dir: dirs){
                    if(!dir.trim().equals("")){
                        this.sftp.cd(dir);
                    }
                }
            }else{
                this.sftp.cd(directory);
            }
        }
    }

    public byte[] download(String directory, String downloadFile) throws SftpException, IOException {
        cdPaths(directory);
        InputStream is = this.sftp.get(downloadFile);
        byte[] fileData = IOUtils.toByteArray(is);
        this.log.info("file:{} is download successful", downloadFile);
        return fileData;
    }

    public void delete(String directory, String deleteFile) throws SftpException {
        this.sftp.cd(directory);
        this.sftp.rm(deleteFile);
    }

    public void mkdir(String directory) throws SftpException {
        this.sftp.mkdir(directory);
    }

    public void deletedir(String directory) throws SftpException {
        this.sftp.rmdir(directory);
    }

    public Vector<?> listFiles(String directory) throws SftpException {
        return this.sftp.ls(directory);
    }

    public boolean isExistDir(String path) {
        boolean isExist = false;
        try {
            SftpATTRS sftpATTRS = this.sftp.lstat(path);
            isExist = true;
            return sftpATTRS.isDir();
        } catch (Exception e) {
            if (e.getMessage().toLowerCase().equals("no such file")) {
                isExist = false;
            }
            return isExist;
        }
    }

    public static void main(String[] args) throws SftpException, IOException {
        SftpUtils sftp = new SftpUtils("sftp", "sftp", "10.1.41.81", 22);
        sftp.login();
        File file = new File("D:\\mk\\zipTest.zip");
        InputStream is = new FileInputStream(file);
        sftp.upload("/data/test/qqq", "zipTest.zip", is);
        sftp.logout();
    }
}
