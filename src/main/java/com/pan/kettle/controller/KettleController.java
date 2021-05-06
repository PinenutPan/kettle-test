package com.pan.kettle.controller;

import com.pan.kettle.service.KettleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/kettle")
@Api(tags = "kettle水壶",value = "KettleController")
public class KettleController {
    @Resource
    private KettleService kettleService;

    @PostMapping("patientKt")
    @ApiOperation("跑转换文件")
    public String patientKt (@RequestParam String fileName) {
        return kettleService.runKtr(fileName, null);
    }

    @PostMapping("patientJob")
    @ApiOperation("跑作业文件")
    public String patientJob (@RequestParam String fileName) {
        return kettleService.runKjb(fileName, null);
    }

}
