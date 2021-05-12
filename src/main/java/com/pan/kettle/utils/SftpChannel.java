package com.pan.kettle.utils;


import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class SftpChannel {
    private Session session = null;
    private Channel channel = null;
    public ChannelSftp getChannel(Map<String, String> sftpDetails, int timeout) throws JSchException {
        String ftpHost = sftpDetails.get("host");
        String port = sftpDetails.get("port");
        String ftpUserName = sftpDetails.get("username");
        String ftpPassword = sftpDetails.get("password");
        int ftpPort = 22;
        if (port != null && !port.equals("")) {
            ftpPort = Integer.valueOf(port).intValue();
        }
        JSch jsch = new JSch();
        this.session = jsch.getSession(ftpUserName, ftpHost, ftpPort);
        log.debug("Session created.");
        if (ftpPassword != null) {
            this.session.setPassword(ftpPassword);
        }
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        this.session.setConfig(config);
        this.session.setTimeout(timeout);
        this.session.connect();
        log.debug("Session connected.");
        log.debug("Opening Channel.");
        this.channel = this.session.openChannel("sftp");
        this.channel.connect();
        log.debug("Connected successfully to ftpHost = " + ftpHost + ",as ftpUserName = " + ftpUserName + ", returning: " + this.channel);
        return (ChannelSftp) this.channel;
    }

    public void closeChannel() throws Exception {
        if (this.channel != null)
            this.channel.disconnect();
        if (this.session != null)
            this.session.disconnect();
    }

    public static int getMaxDayOfDate(Date date) {
        Calendar a = Calendar.getInstance();
        a.setTime(date);
        a.set(5, 1);
        a.roll(5, -1);
        int maxDate = a.get(5);
        return maxDate;
    }

    public static Date getNewDayOfMon(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.add(2, -1);
        return c.getTime();
    }
}
