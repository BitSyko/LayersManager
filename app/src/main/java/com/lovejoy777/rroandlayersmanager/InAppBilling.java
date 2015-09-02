package com.lovejoy777.rroandlayersmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import util.IabHelper;
import util.IabResult;
import util.Inventory;
import util.Purchase;


public class InAppBilling extends AppCompatActivity {

    IabHelper mHelper;
    static final String ITEM_SKU1 = "donate1";
    static final String ITEM_SKU2 = "donate2";
    static final String ITEM_SKU3 = "donate5";
    static final String ITEM_SKU4 = "donate10";


    private Button donBtn1, donBtn2, donBtn3, donBtn4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inappbilling);

        //set Toolbar
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar4);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        setSupportActionBar(toolbar);

        donBtn1 = (Button)findViewById(R.id.donBtn1);
        donBtn2 = (Button)findViewById(R.id.donBtn2);
        donBtn3 = (Button)findViewById(R.id.donBtn3);
        donBtn4 = (Button)findViewById(R.id.donBtn4);


        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxhTS1N1U8E0p07cx5Y8Fu4M++5YO6CBEVISXGab10xlXQD14J2LgeCmH7P1614RiWQ8meo3piaklcjripvB/cEjfdfx0F07iijfogz2NWC85XSEitwjngZjFXF5T6b7fRTfO4YYqLaC+fz1UNFpwzecJ2exfhQE/lTquTsAGu5ZRZq7eyyKGvWSbaoX+JrDIrB+aI73NYj8WVCmneu45GXfma8j92Ry1WAdWxaf3Q84t50azyCx2guyc0KMcXnyGfq//YceNJq/UUEnuQzZrnkc1UxjpBNOl6P0ehdXaL1XFgmljiYRehIZ2UmxJTC2LGfMd2ETsM1Iy8jVTMxTrdwIDAQAB";

        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result)
            {

            }
        });
    }

    public void buyClick1(View view) {
        mHelper.launchPurchaseFlow(this, ITEM_SKU1, 10001,
                mPurchaseFinishedListener, "");
    }

    public void buyClick2(View view) {
        mHelper.launchPurchaseFlow(this, ITEM_SKU2, 10002,
                mPurchaseFinishedListener, "");
    }

    public void buyClick3(View view) {
        mHelper.launchPurchaseFlow(this, ITEM_SKU3, 10003,
                mPurchaseFinishedListener, "");
    }

    public void buyClick4(View view) {
        mHelper.launchPurchaseFlow(this, ITEM_SKU4, 10004,
                mPurchaseFinishedListener, "");
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {
        if (!mHelper.handleActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result,
                                          Purchase purchase)
        {
            if (result.isFailure()) {
                // Handle error
                return;
            }
            else if (purchase.getSku().equals(ITEM_SKU1)) {
                consumeItem();
                donBtn1.setEnabled(false);
            }

            else if (purchase.getSku().equals(ITEM_SKU2)) {
                consumeItem2();
                donBtn2.setEnabled(false);
            }

            else if (purchase.getSku().equals(ITEM_SKU3)) {
                consumeItem3();
                donBtn3.setEnabled(false);
            }

            else if (purchase.getSku().equals(ITEM_SKU4)) {
                consumeItem4();
                donBtn4.setEnabled(false);
            }

        }
    };

    public void consumeItem() {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }
    public void consumeItem2() {
        mHelper.queryInventoryAsync(m2ReceivedInventoryListener);
    }
    public void consumeItem3() {
        mHelper.queryInventoryAsync(m3ReceivedInventoryListener);
    }
    public void consumeItem4() {
        mHelper.queryInventoryAsync(m4ReceivedInventoryListener);
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {


            if (result.isFailure()) {
                // Handle failure
            }
            else{
                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU1 ),
                        mConsumeFinishedListener);
            }

        }
    };

    IabHelper.QueryInventoryFinishedListener m2ReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {


            if (result.isFailure()) {
                // Handle failure
            }
            else{
                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU2 ),
                        m2ConsumeFinishedListener);
            }

        }
    };

    IabHelper.QueryInventoryFinishedListener m3ReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {


            if (result.isFailure()) {
                // Handle failure
            }
            else{
                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU3 ),
                        m3ConsumeFinishedListener);
            }

        }
    };

    IabHelper.QueryInventoryFinishedListener m4ReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {


            if (result.isFailure()) {
                // Handle failure
            }
            else{
                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU4 ),
                        m4ConsumeFinishedListener);
            }

        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {

                    if (result.isSuccess()) {
                        Toast.makeText(InAppBilling.this, "Thank You", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(InAppBilling.this, "Payment unsuccessful", Toast.LENGTH_LONG).show();
                    }
                }
            };

    IabHelper.OnConsumeFinishedListener m2ConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {

                    if (result.isSuccess()) {
                        Toast.makeText(InAppBilling.this, "Thank You", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(InAppBilling.this, "Payment unsuccessful", Toast.LENGTH_LONG).show();
                    }
                }
            };

    IabHelper.OnConsumeFinishedListener m3ConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {

                    if (result.isSuccess()) {
                        Toast.makeText(InAppBilling.this, "Thank You", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(InAppBilling.this, "Payment unsuccessful", Toast.LENGTH_LONG).show();
                    }
                }
            };

    IabHelper.OnConsumeFinishedListener m4ConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {

                    if (result.isSuccess()) {
                        Toast.makeText(InAppBilling.this, "Thank You", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(InAppBilling.this, "Payment unsuccessful", Toast.LENGTH_LONG).show();
                    }
                }
            };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }
}

