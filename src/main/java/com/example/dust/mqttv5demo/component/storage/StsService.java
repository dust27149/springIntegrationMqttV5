package com.example.dust.mqttv5demo.component.storage;

import com.example.dust.mqttv5demo.component.storage.model.StsInfo;

import java.io.FileInputStream;

public interface StsService {

    StsInfo getStsInfo();

    void uploadFile(FileInputStream stream,String filePath);

}
