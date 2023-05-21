package com.riteshknayak.cleo;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.PurchaseInfo;
import com.anjlab.android.iab.v3.SkuDetails;
import com.riteshknayak.cleo.databinding.ActivityPurchasePremiumBinding;

import java.util.List;

public class PurchasePremiumActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    private BillingProcessor bp;

    private static final String premiumMonthly = "monthly_premium";

    ActivityPurchasePremiumBinding binding;

    private final String TAG = "monthlySubscription";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPurchasePremiumBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bp = new BillingProcessor(this, getResources().getString(R.string.play_console_license), this);
        bp.initialize();



    }

    @Override
    public void onBillingInitialized() {
        Log.d(TAG, "onBillingInitialized: ");


        String productId = getResources().getString(R.string.product_id);

        binding.premiumMonthlyBtn.setOnClickListener(v -> {

            if(bp.isSubscriptionUpdateSupported()){
                bp.subscribe(this, productId);
            }else {
                Toast.makeText(getApplicationContext(),"Subscription Not supported" , Toast.LENGTH_LONG).show();
            }

        });



    }


    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable PurchaseInfo details) {
        Log.d(TAG, "onProductPurchased: ");
    }

    @Override
    public void onPurchaseHistoryRestored() {
        Log.d(TAG, "onPurchaseHistoryRestored: ");
    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
        Log.d(TAG, "onBillingError: " );
    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }


}

