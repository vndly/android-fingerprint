package com.example.androidfingerprint;

import android.app.DialogFragment;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.CryptoObject;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FingerprintDialog extends DialogFragment implements FingerprintUiHelper.Callback
{
    private CryptoObject cryptoObject;
    private FingerprintUiHelper fingerprintUiHelper;
    private MainActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getDialog().setTitle(getString(R.string.dialog_signIn));
        View view = inflater.inflate(R.layout.dialog_fingerprint, container, false);

        view.findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dismiss();
            }
        });

        fingerprintUiHelper = new FingerprintUiHelper(
                activity.getSystemService(FingerprintManager.class),
                (ImageView) view.findViewById(R.id.fingerprint_icon),
                (TextView) view.findViewById(R.id.fingerprint_status), this);

        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        fingerprintUiHelper.startListening(cryptoObject);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        fingerprintUiHelper.stopListening();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        activity = (MainActivity) getActivity();
    }

    public void setCryptoObject(CryptoObject cryptoObject)
    {
        this.cryptoObject = cryptoObject;
    }

    @Override
    public void onAuthenticated()
    {
        activity.onSuccess(cryptoObject);
        dismiss();
    }

    @Override
    public void onError()
    {
        Toast.makeText(getContext(), "ERROR", Toast.LENGTH_SHORT).show();
    }
}