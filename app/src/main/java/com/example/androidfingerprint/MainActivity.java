package com.example.androidfingerprint;

import android.app.Activity;
import android.hardware.fingerprint.FingerprintManager.CryptoObject;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import javax.crypto.Cipher;

public class MainActivity extends Activity
{
    private FingerprintEncryption fingerprintEncryption;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fingerprintEncryption = new FingerprintEncryption();

        Button encryptButton = findViewById(R.id.button_encrypt);
        encryptButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                findViewById(R.id.encryptedMessage).setVisibility(View.GONE);
                fingerprintEncryption.start(getFragmentManager(), Cipher.ENCRYPT_MODE);
            }
        });

        Button decryptButton = findViewById(R.id.button_decrypt);
        decryptButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                findViewById(R.id.encryptedMessage).setVisibility(View.GONE);
                fingerprintEncryption.start(getFragmentManager(), Cipher.DECRYPT_MODE);
            }
        });
    }

    public void onSuccess(CryptoObject cryptoObject)
    {
        byte[] encrypted = fingerprintEncryption.encrypt(cryptoObject, "hello");

        TextView textView = findViewById(R.id.encryptedMessage);
        textView.setVisibility(View.VISIBLE);
        textView.setText(Base64.encodeToString(encrypted, 0));
    }
}