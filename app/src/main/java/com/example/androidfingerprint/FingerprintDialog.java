package com.example.androidfingerprint;

import android.app.DialogFragment;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FingerprintDialog extends DialogFragment implements FingerprintUiHelper.Callback
{
    private Button mCancelButton;
    private View mFingerprintContent;

    private Stage mStage = Stage.FINGERPRINT;

    private FingerprintManager.CryptoObject mCryptoObject;
    private FingerprintUiHelper mFingerprintUiHelper;
    private MainActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Do not create a new Fragment when the Activity is re-created such as orientation changes.
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getDialog().setTitle(getString(R.string.sign_in));
        View v = inflater.inflate(R.layout.fingerprint_dialog, container, false);
        mCancelButton = v.findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dismiss();
            }
        });

        mFingerprintContent = v.findViewById(R.id.fingerprint_container);
        mFingerprintUiHelper = new FingerprintUiHelper(
                mActivity.getSystemService(FingerprintManager.class),
                (ImageView) v.findViewById(R.id.fingerprint_icon),
                (TextView) v.findViewById(R.id.fingerprint_status), this);
        updateStage();

        // If fingerprint authentication is not available, switch immediately to the backup (password) screen.
        if (!mFingerprintUiHelper.isFingerprintAuthAvailable())
        {
            Toast.makeText(getContext(), "FINGERPRINT NOT AVAILABLE", Toast.LENGTH_SHORT).show();
        }

        return v;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (mStage == Stage.FINGERPRINT)
        {
            mFingerprintUiHelper.startListening(mCryptoObject);
        }
    }

    public void setStage(Stage stage)
    {
        mStage = stage;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mFingerprintUiHelper.stopListening();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        mActivity = (MainActivity) getActivity();
    }

    /**
     * Sets the crypto object to be passed in when authenticating with fingerprint.
     */
    public void setCryptoObject(FingerprintManager.CryptoObject cryptoObject)
    {
        mCryptoObject = cryptoObject;
    }

    private void updateStage()
    {
        switch (mStage)
        {
            case FINGERPRINT:
                mCancelButton.setText(R.string.cancel);
                mFingerprintContent.setVisibility(View.VISIBLE);
                break;
            case NEW_FINGERPRINT_ENROLLED:
                // Intentional fall through
        }
    }

    @Override
    public void onAuthenticated()
    {
        // Callback from FingerprintUiHelper. Let the activity know that authentication was
        // successful.
        mActivity.onPurchased(true /* withFingerprint */, mCryptoObject);
        dismiss();
    }

    @Override
    public void onError()
    {
        Toast.makeText(getContext(), "ERROR", Toast.LENGTH_SHORT).show();
    }

    /**
     * Enumeration to indicate which authentication method the user is trying to authenticate with.
     */
    public enum Stage
    {
        FINGERPRINT,
        NEW_FINGERPRINT_ENROLLED,
    }
}