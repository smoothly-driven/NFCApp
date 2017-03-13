package processors;

import android.content.Intent;

import com.example.robot_server.nfcapp.ScanResult;

/**
 * Created by robot-server on 13.03.17.
 */

public interface IntentProcessor {
    void process(Intent intent, ScanResult.ScanResultBuilder builder);
}
