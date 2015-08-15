package com.lovejoy777.rroandlayersmanager.views;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;

public class CheckBoxHolder extends FrameLayout {

    public CheckBoxHolder(Context context, final CheckBox checkBox, final CheckBoxHolderCallback callback) {
        super(context);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkBox.isEnabled()) {
                    checkBox.performClick();
                }

                callback.onClick();
            }
        });

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (!checkBox.isEnabled()) {
                    checkBox.performClick();
                }

                callback.onClick();

                return true;
            }
        });

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }


    public interface CheckBoxHolderCallback {
        void onClick();
    }

}
