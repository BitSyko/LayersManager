package com.lovejoy777.rroandlayersmanager.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.widget.ImageView;
import com.lovejoy777.rroandlayersmanager.R;

public class FullScreenActivity extends Activity {

    private ImageView image;

    //FIXME
    private static Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_screen);

        image = (ImageView) findViewById(R.id.image);
        image.setVisibility(View.VISIBLE);
        image.setBackgroundColor(getResources().getColor(R.color.accent));

        image.setImageBitmap(bitmap);
    }

    public static void launch(Activity activity, ImageView transitionView, String id) {

        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity, transitionView, id);

        bitmap = ((BitmapDrawable) transitionView.getDrawable()).getBitmap();
        transitionView.setTransitionName(id);
        Intent intent = new Intent(activity, FullScreenActivity.class);
        ActivityCompat.startActivity(activity, intent, options.toBundle());

    }

    public void click(View view) {
        this.finishAfterTransition();
    }

    @Override
    public void onDestroy() {
        image.setImageBitmap(null);
        image.setTransitionName("");
        image.setVisibility(View.GONE);
        super.onDestroy();
    }

}