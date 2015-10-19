package com.lovejoy777.rroandlayersmanager.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bitsyko.liblayers.Layer;
import com.lovejoy777.rroandlayersmanager.R;

import java.lang.ref.WeakReference;

public class ScreenshotAdapter extends RecyclerView.Adapter<ScreenshotAdapter.ScreenshotViewHolder> {

    Context context;
    Layer layer;
    float windowHeight;

    public ScreenshotAdapter(Context context, Layer layer, int windowHeight) {
        this.context = context;
        this.layer = layer;
        this.windowHeight = windowHeight;
    }

    @Override
    public ScreenshotViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.adapter_screenshot, parent, false);

        return new ScreenshotViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ScreenshotViewHolder holder, int position) {
        //  holder.image.setImageBitmap(resizeBitmap(layer.getScreenshot(position + 1)));

        loadBitmap(position, holder.image);

    }

    private void loadBitmap(int position, ImageView imageView) {

        if (cancelPotentialWork(position, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(null, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(position);
        }

    }

    @Override
    public int getItemCount() {
        return layer.getScreenShotsNumber() - 1;
    }

    public static class ScreenshotViewHolder extends RecyclerView.ViewHolder {

        protected ImageView image;

        public ScreenshotViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.iv_themeImage);
        }
    }

    private Bitmap resizeBitmap(Drawable drawable) {
        return resizeBitmap(((BitmapDrawable) drawable).getBitmap());
    }

    private Bitmap resizeBitmap(Bitmap bitmap) {

        if (bitmap.getWidth() < windowHeight / 2) {
            return bitmap;
        }

        float scale = (windowHeight / 2) / bitmap.getHeight();

        Bitmap resized = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * scale), (int) (bitmap.getHeight() * scale), false);

        bitmap.recycle();

        return resized;
    }

    public static boolean cancelPotentialWork(int data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final int bitmapData = bitmapWorkerTask.data;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == 0 || bitmapData != data) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private int data = 0;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params) {
            data = params[0];
            return resizeBitmap(layer.getScreenshot(data + 1));
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask =
                        getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }

    }
}
