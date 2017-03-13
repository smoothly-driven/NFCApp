package processors;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.util.Log;

import com.example.robot_server.nfcapp.NFCUtils;
import com.example.robot_server.nfcapp.ScanResult;
import com.example.robot_server.nfcapp.StringWrapper;

/**
 * Created by robot-server on 13.03.17.
 */

public class ReadProcessor implements IntentProcessor {

    private StringWrapper mString;

    public ReadProcessor() {
    }

    public ReadProcessor(StringWrapper string) {
        mString = string;
    }

    @Override
    public void process(Intent intent, ScanResult.ScanResultBuilder builder) {
        Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMessages != null) {
            NdefMessage[] messages = new NdefMessage[rawMessages.length];
            for (int i = 0; i < rawMessages.length; i++) {
                messages[i] = (NdefMessage) rawMessages[i];
            }
            String cardContents = NFCUtils.readMessageContents(messages);
            builder.cardContent(cardContents);
            if (mString != null) mString.set(cardContents);
            Log.v("NFCTAG", "messages were successfully decoded : " + cardContents);
        }
    }
}
