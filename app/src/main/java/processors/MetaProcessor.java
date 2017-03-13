package processors;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.util.Log;

import com.example.robot_server.nfcapp.NFCUtils;
import com.example.robot_server.nfcapp.ScanResult;

/**
 * Created by robot-server on 13.03.17.
 */

public class MetaProcessor implements IntentProcessor {

    @Override
    public void process(Intent intent, ScanResult.ScanResultBuilder builder) {
        Log.v("NFCTAG", intent.getAction());
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        builder.cardTechnology(tag.getTechList())
                .cardUid(NFCUtils.byteArrayToHex(tag.getId()));
    }
}
