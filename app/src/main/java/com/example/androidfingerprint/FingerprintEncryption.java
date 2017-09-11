package com.example.androidfingerprint;

import android.app.FragmentManager;
import android.hardware.fingerprint.FingerprintManager.CryptoObject;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class FingerprintEncryption
{
    private static final String DEFAULT_KEY_NAME = "encryption.key";

    private KeyStore keyStore;

    private boolean initCipher(Cipher cipher, int mode)
    {
        try
        {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(DEFAULT_KEY_NAME, null);
            cipher.init(mode, key);

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public byte[] encrypt(CryptoObject cryptoObject, String message)
    {
        try
        {
            Cipher cipher = cryptoObject.getCipher();

            return cipher.doFinal(message.getBytes());
        }
        catch (Exception e)
        {
            Log.e(getClass().getName(), "Failed to encrypt the data with the generated key." + e.getMessage());

            return null;
        }
    }

    private void createKey() throws Exception
    {
        keyStore.load(null);

        KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(DEFAULT_KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT);
        builder.setBlockModes(KeyProperties.BLOCK_MODE_CBC);
        builder.setUserAuthenticationRequired(true);
        builder.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        keyGenerator.init(builder.build());
        keyGenerator.generateKey();
    }

    public void start(FragmentManager fragmentManager, int mode)
    {
        try
        {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            createKey();

            // Set up the crypto object for later. The object will be authenticated by use
            // of the fingerprint.
            if (initCipher(cipher, mode))
            {
                // Show the fingerprint dialog. The user has the option to use the fingerprint with
                // crypto, or you can fall back to using a server-side verified password.
                FingerprintDialog fragment = new FingerprintDialog();
                fragment.setCryptoObject(new CryptoObject(cipher));
                fragment.show(fragmentManager, null);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}