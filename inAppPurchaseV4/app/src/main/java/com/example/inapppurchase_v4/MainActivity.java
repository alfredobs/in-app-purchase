package com.example.inapppurchase_v4;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener{

    private BillingClient billingClient;
    private List skuList = new ArrayList();
    private String sku = "compracap8";
    private Button buttonBuyProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonBuyProduct = findViewById(R.id.btnbuy);
        buttonBuyProduct.setEnabled(true);

        Boolean b = getBoolFromPref( this, "myPref", sku);
        if(b == true){

            buttonBuyProduct.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "ya tienes todos los capitulos para disfrutarlos", Toast.LENGTH_LONG).show();
        }
        else {
            skuList.add(sku);

            setupBillingClient();
        }
        /*PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
                // To be implemented in a later section.
            }
        };
        bc = BillingClient.newBuilder(MainActivity.this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        bc.startConnection(new BillingClientStateListener(){

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

            }

            @Override
            public void onBillingServiceDisconnected() {

            }
        });


        skuList.add(sku);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);

        bc.querySkuDetailsAsync(params.build(), (billingResult, skuDetailsList) -> {
            if (bc.isReady()){
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    for (Object skuDetailObjec: skuDetailsList){
                        final SkuDetails skuDetails = (SkuDetails) skuDetailObjec;
                        if (skuDetails.getSku().equals(sku)){
                            btnComprarProducto.setEnabled(true);
                            btnComprarProducto.setOnClickListener((v)->{
                                BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                        .setSkuDetails(skuDetails)
                                        .build();
                                bc.launchBillingFlow(MainActivity.this, billingFlowParams);
                                int responseCode = bc.launchBillingFlow(activity, billingFlowParams).getResponseCode();
                            });
                        }
                    }
                }

            }
        });
        */




    }
    private void setupBillingClient() {
        billingClient = BillingClient.newBuilder( this).enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    loadAllSkus();

                }
            }

            @Override
            public void onBillingServiceDisconnected() {

            }
        });
    }

    private void loadAllSkus() {

        if(billingClient.isReady()){
            final SkuDetailsParams params = SkuDetailsParams.newBuilder()
                    .setSkusList(skuList)
                    .setType(BillingClient.SkuType.INAPP)
                    .build();

            billingClient.querySkuDetailsAsync(params, new SkuDetailsResponseListener() {
                @Override
                public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                    if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){

                        for (Object skuDetailsObject : skuDetailsList){
                            final SkuDetails skuDetails = (SkuDetails) skuDetailsObject;
                            if( skuDetails.getSku().equals(sku)){
                                buttonBuyProduct.setEnabled(true);
                                buttonBuyProduct.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        BillingFlowParams params = BillingFlowParams
                                                .newBuilder()
                                                .setSkuDetails(skuDetails)
                                                .build();
                                        billingClient.launchBillingFlow(MainActivity.this, params);
                                    }
                                });

                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {

        int responseCode = billingResult.getResponseCode();

        if ( responseCode == BillingClient.BillingResponseCode.OK && purchases != null){

            for (Purchase purchase : purchases){
                handlePurchase(purchase);
            }

        }
        else if (responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED){

            setBoolInPref( this, "myPref", sku, true);

        }
        else if (responseCode == BillingClient.BillingResponseCode.USER_CANCELED){


        }


    }

    private void handlePurchase(Purchase purchase) {

        if(purchase.getSku().equals(sku)){
            setBoolInPref( this, "myPref", sku, true);
            Toast.makeText(this, "compra hecha, disfruta los ultimos capitulos", Toast.LENGTH_LONG).show();
        }
    }

    private boolean getBoolFromPref(Context context, String prefName, String constanName){
        SharedPreferences pref = context.getSharedPreferences( prefName, 0);

        return pref.getBoolean(constanName, false);
    }

    private void setBoolInPref (Context context, String prefName, String constanName, Boolean val){
        SharedPreferences pref = context.getSharedPreferences(prefName, 0); // 0 = modo privado

        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(constanName, val);
        editor.commit();
    }
    /* PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                    && purchases != null) {
                for (Purchase purchase : purchases) {
                    handlePurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
                Toast.makeText(MainActivity.this, "Ya valio la compra", Toast.LENGTH_LONG).show();
            }
            else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED){
                setBoolInPref(MainActivity.this, "myPref", sku, true);
            } else {
                // Handle any other error codes.
            }
        }
    }; */

    /* void setupBillingCliente(){
        bc = BillingClient.newBuilder(MainActivity.this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        bc.startConnection(new BillingClientStateListener(){

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    loadAllSkus();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {

            }
        });
    }

    void loadAllSkus(){
        if (bc.isReady()){
            skuList.add(sku);
            SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
            params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);

            bc.querySkuDetailsAsync(params.build(), new SkuDetailsResponseListener() {
                @Override
                public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                        for (Object skuDetailObjec: list){
                            final SkuDetails skuDetails = (SkuDetails) skuDetailObjec;
                            if (skuDetails.getSku().equals(sku)){
                                btnComprarProducto.setEnabled(true);
                                btnComprarProducto.setOnClickListener((v)->{
                                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                            .setSkuDetails(skuDetails)
                                            .build();
                                    bc.launchBillingFlow(MainActivity.this, billingFlowParams);
                                    int responseCode = bc.launchBillingFlow(activity, billingFlowParams).getResponseCode();
                                });
                            }
                        }
                    }
                }
            });
        }
    }

    void handlePurchase(Purchase p){
        if (p.getSku().equals(sku)){
            setBoolInPref(this, "myPref", sku, true);
            Toast.makeText(this, "Compra hecha", Toast.LENGTH_LONG).show();
        }
    }

    private boolean getBoolFromPref(Context context, String prefName, String constanName){
        SharedPreferences pref = context.getSharedPreferences(prefName, 0);
        return pref.getBoolean(constanName, false);
    }

    void setBoolInPref (Context context, String prefName, String constanName, Boolean val){
        SharedPreferences pref = context.getSharedPreferences(prefName, 0);

        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(constanName, val);
        editor.commit();
    } */

    /*private void setupBillingCliente(){
        bc = BillingClient.newBuilder(this).enablePendingPurchases().setListener((PurchasesUpdatedListener) this).build();
        bc.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    loadAllSkus();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {

            }
        });
    }

    private void loadAllSkus(){
        if (bc.isReady()){
            final SkuDetailsParams params = SkuDetailsParams.newBuilder().setSkusList(skuList).setType(BillingClient.SkuType.INAPP).build();
            bc.querySkuDetailsAsync(params, (billingResult, list) -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                    for (Object skuDetailObjec: list){
                        final SkuDetails skuDetails = (SkuDetails) skuDetailObjec;
                        if (skuDetails.getSku().equals(sku)){
                            btnComprarProducto.setEnabled(true);
                            btnComprarProducto.setOnClickListener((v)->{
                                BillingFlowParams p = BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build();
                                bc.launchBillingFlow(MainActivity.this, p);
                            });
                        }
                    }
                }
            });
        }
    }

    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases){
        int responseCode = billingResult.getResponseCode();
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null){
            for (Purchase p: purchases){
                handlePurchase(p);
            }
        }
        else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED){
            setBoolInPref(this, "myPref", sku, true);
        }
        else if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED){
            Toast.makeText(this, "Compra cancelada", Toast.LENGTH_LONG).show();
        }
    }

    private void handlePurchase(Purchase p){
        if (p.getSku().equals(sku)){
            setBoolInPref(this, "myPref", sku, true);
            Toast.makeText(this, "Compra hecha", Toast.LENGTH_LONG).show();
        }
    }

    private void setBoolInPref (Context context, String prefName, String constanName, Boolean val){
        SharedPreferences pref = context.getSharedPreferences(prefName, 0);

        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(constanName, val);
        editor.commit();
    }

    private boolean getBoolFromPref(Context context, String prefName, String constanName){
        SharedPreferences pref = context.getSharedPreferences(prefName, 0);
        return pref.getBoolean(constanName, false);
    }*/
}