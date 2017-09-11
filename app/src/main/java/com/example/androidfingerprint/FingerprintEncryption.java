package com.example.androidfingerprint;

import android.app.FragmentManager;
import android.hardware.fingerprint.FingerprintManager.CryptoObject;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class FingerprintEncryption
{
    private static final String KEY_ALIAS = "encryption.key";

    private boolean initCipher(KeyStore keyStore, Cipher cipher, int mode)
    {
        try
        {
            keyStore.load(null);

            SecretKey key = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
            cipher.init(mode, key);

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private void createKey(KeyStore keyStore) throws Exception
    {
        keyStore.load(null);

        if (!keyStore.containsAlias(KEY_ALIAS))
        {
            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT);
            builder.setBlockModes(KeyProperties.BLOCK_MODE_CBC);
            builder.setUserAuthenticationRequired(true);
            builder.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyGenerator.init(builder.build());
            keyGenerator.generateKey();
        }
    }

    public void start(FragmentManager fragmentManager, int mode)
    {
        try
        {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            createKey(keyStore);

            Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);

            // Set up the crypto object for later. The object will be authenticated by use
            // of the fingerprint.
            if (initCipher(keyStore, cipher, mode))
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