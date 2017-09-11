package com.example.androidfingerprint;

import android.app.Activity;
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
                fingerprintEncryption.start(getFragmentManager(), Cipher.ENCRYPT_MODE, iv);
            }
        });

        Button decryptButton = findViewById(R.id.button_decrypt);
        decryptButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                findViewById(R.id.encryptedMessage).setVisibility(View.GONE);
                fingerprintEncryption.start(getFragmentManager(), Cipher.DECRYPT_MODE, iv);
            }
        });
    }

    byte[] encrypted = null;
    byte[] iv = null;

    public void onSuccess(Cipher cipher)
    {
        try
        {
            if (encrypted == null)
            {
                encrypted = cipher.doFinal("hello".getBytes());
                iv = cipher.getIV();
                showText(Base64.encodeToString(encrypted, 0));
            }
            else
            {
                byte[] decrypted = cipher.doFinal(encrypted);
                String message = new String(decrypted, "UTF-8");
                showText(message);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void showText(String text)
    {
        TextView textView = findViewById(R.id.encryptedMessage);
        textView.setVisibility(View.VISIBLE);
        textView.setText(text);
    }
}