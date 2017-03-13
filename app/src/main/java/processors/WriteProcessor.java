package processors;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.util.Log;

import com.example.robot_server.nfcapp.NFCUtils;
import com.example.robot_server.nfcapp.ScanResult;
import com.example.robot_server.nfcapp.StringWrapper;

/**
 * Created by robot-server on 13.03.17.
 */

public class WriteProcessor implements IntentProcessor {

    private StringWrapper mText;

    public WriteProcessor(StringWrapper text) {
        mText = text;
    }

    public void setText(StringWrapper text) {
        mText = text;
    }

    @Override
    public void process(Intent intent, ScanResult.ScanResultBuilder builder) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        int opStatus = NFCUtils.writeTag(NFCUtils.getMessageAsNdef(mText.get()), tag);
        Log.v("NFCTAG", "writing operation returned a code " + opStatus);
        if (opStatus == NFCUtils.CODE_SUCCESS) {
            builder.cardContent(mText.get());
        }
    }
}
