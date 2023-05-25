package com.riteshknayak.cleo;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class Constants  {

    public static RewardedAd rewardedAd;
    public static Boolean adLoaded = false;



    public static void loadRewardedAd(Context context){
        RewardedAd.load(context,
                "ca-app-pub-9592447067025157/6227800060",
                new AdRequest.Builder().build()
                , new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d("TAG", loadAdError.toString());
                        rewardedAd = null;

                        loadRewardedAd(context);
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd ad) {
                        rewardedAd = ad;
                        Log.d("TAG", "Ad was loaded.");

                        adLoaded = true;
                    }
                });
    }
}
