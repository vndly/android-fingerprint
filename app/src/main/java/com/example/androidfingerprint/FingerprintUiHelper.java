package com.example.androidfingerprint;

import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationCallback;
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult;
import android.hardware.fingerprint.FingerprintManager.CryptoObject;
import android.os.CancellationSignal;
import android.widget.ImageView;
import android.widget.TextView;

public class FingerprintUiHelper extends AuthenticationCallback
{
    private static final long ERROR_TIMEOUT_MILLIS = 1600;

    private final FingerprintManager fingerprintManager;
    private final ImageView icon;
    private final TextView errorTextView;
    private final Callback callback;
    private CancellationSignal cancellationSignal;
    private boolean selfCancelled;

    public FingerprintUiHelper(FingerprintManager fingerprintManager, ImageView icon, TextView errorTextView, Callback callback)
    {
        this.fingerprintManager = fingerprintManager;
        this.icon = icon;
        this.errorTextView = errorTextView;
        this.callback = callback;
    }

    @SuppressWarnings("MissingPermission")
    public void startListening(CryptoObject cryptoObject)
    {
        cancellationSignal = new CancellationSignal();
        selfCancelled = false;
        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
        icon.setImageResource(R.drawable.ic_fingerprint);
    }

    public void stopListening()
    {
        if (cancellationSignal != null)
        {
            selfCancelled = true;
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString)
    {
        if (!selfCancelled)
        {
            showError(errString);

            icon.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    callback.onError();
                }
            }, ERROR_TIMEOUT_MILLIS);
        }
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString)
    {
        showError(helpString);
    }

    @Override
    public void onAuthenticationFailed()
    {
        showError(icon.getResources().getString(R.string.dialog_fingerprint_notRecognized));
    }

    @Override
    public void onAuthenticationSucceeded(AuthenticationResult result)
    {
        errorTextView.removeCallbacks(resetErrorTextRunnable);
        icon.setImageResource(R.drawable.ic_fingerprint_success);
        errorTextView.setTextColor(errorTextView.getResources().getColor(R.color.success_color, null));
        errorTextView.setText(errorTextView.getResources().getString(R.string.dialog_fingerprint_success));

        callback.onAuthenticated(result.getCryptoObject());
    }

    private void showError(CharSequence error)
    {
        icon.setImageResource(R.drawable.ic_fingerprint_error);
        errorTextView.setText(error);
        errorTextView.setTextColor(errorTextView.getResources().getColor(R.color.warning_color, null));
        errorTextView.removeCallbacks(resetErrorTextRunnable);
        errorTextView.postDelayed(resetErrorTextRunnable, ERROR_TIMEOUT_MILLIS);
    }

    private Runnable resetErrorTextRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            errorTextView.setTextColor(errorTextView.getResources().getColor(R.color.hint_color, null));
            errorTextView.setText(errorTextView.getResources().getString(R.string.dialog_fingerprint_hint));
            icon.setImageResource(R.drawable.ic_fingerprint);
        }
    };

    public interface Callback
    {
        void onAuthenticated(CryptoObject cryptoObject);

        void onError();
    }
}