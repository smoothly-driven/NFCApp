package com.example.robot_server.nfcapp;

import com.example.robot_server.nfcapp.profiles.TestProfile;

public interface NfcTestController {

    void startTest();

    void pauseTest();

    void resumeTest();

    void stopTest();

    void updateUi();

    void updateUi(TestProfile profile);

}
