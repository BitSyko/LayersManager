package com.lovejoy777.rroandlayersmanager.interfaces;

import android.os.AsyncTask;

public abstract class StoppableAsyncTask<A,B,C> extends AsyncTask<A,B,C> {
    public abstract void stop();
}
