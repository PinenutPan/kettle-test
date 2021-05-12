package com.pan.kettle;

import com.jcraft.jsch.SftpException;
import com.pan.kettle.service.KettleService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;

@SpringBootTest
class KettleApplicationTests {
    @Resource
    private KettleService kettleService;

    @Test
    void contextLoads() throws IOException, SftpException {
        kettleService.test();
    }

    @Test
    void test() {
        kettleService.runKjb("C:\\Users\\pjp20\\AppData\\Local\\Temp\\kettleFile\\202105111713042\\kt.kjb",null);
    }

}
