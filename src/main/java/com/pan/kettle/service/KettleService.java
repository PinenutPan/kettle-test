package com.pan.kettle.service;

import lombok.extern.slf4j.Slf4j;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

@Service
@Slf4j
public class KettleService {
    @Value("${kettle.ip}")
    private String ip;
    @Value("${kettle.port}")
    private String port;
    @Value("${kettle.username}")
    private String username;
    @Value("${kettle.password}")
    private String password;

    /**
     * 执行ktr文件
     *
     * @param filename
     * @param params
     * @return
     */
    public String runKtr(String filename, Map<String, String> params) {
        try {
            KettleEnvironment.init();

            String path = this.getClass().getResource("/") + File.separator + "kettle" + File.separator + filename;
            JobMeta jobMeta = new JobMeta(path, null);
            jobMeta.setParameterValue("ip",ip);
            jobMeta.setParameterValue("port",port);
            jobMeta.setParameterValue("username",username);
            jobMeta.setParameterValue("password",password);

            TransMeta tm = new TransMeta(path);
            Trans trans = new Trans(tm);
            if (params != null) {
                Iterator<Map.Entry<String, String>> entries = params.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry<String, String> entry = entries.next();
                    trans.setParameterValue(entry.getKey(), entry.getValue());
                }
            }
            trans.execute(null);
            trans.waitUntilFinished();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return "success";
    }

    /**
     * 执行kjb文件
     *
     * @param filename
     * @param params
     * @return
     */
    public String runKjb(String filename, Map<String, String> params) {
        try {
            KettleEnvironment.init();

            String path = this.getClass().getResource("/") + File.separator + "kettle" + File.separator + filename;
            JobMeta jobMeta = new JobMeta(path, null);
            jobMeta.setParameterValue("ip",ip);
            jobMeta.setParameterValue("port",port);
            jobMeta.setParameterValue("username",username);
            jobMeta.setParameterValue("password",password);

            Job job = new Job(null, jobMeta);
            if (params != null) {
                Iterator<Map.Entry<String, String>> entries = params.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry<String, String> entry = entries.next();
                    job.setVariable(entry.getKey(), entry.getValue());
                }
            }

            job.start();
            job.waitUntilFinished();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return "success";
    }

    public static void main(String[] args) {
    }

}
