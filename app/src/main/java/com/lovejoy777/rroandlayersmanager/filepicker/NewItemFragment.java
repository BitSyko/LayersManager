/*
 * Copyright (c) 2014 Jonas Kalderstam
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.lovejoy777.rroandlayersmanager.filepicker;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.lovejoy777.rroandlayersmanager.R;

public abstract class NewItemFragment extends DialogFragment {

    private String itemName = null;
    private View okButton = null;
    private OnNewFolderListener listener = null;

    public NewItemFragment() {
        super();
    }

    public void setListener(final OnNewFolderListener listener) {
        this.listener = listener;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        getDialog().setTitle(getDialogTitle());

        @SuppressLint("InflateParams") final View view =
                inflater.inflate(R.layout.dialog_new_item, null);

        okButton = view.findViewById(R.id.button_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (listener != null) {
                    listener.onNewFolder(itemName);
                }
                dismiss();
            }
        });

        view.findViewById(R.id.button_cancel)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        dismiss();
                    }
                });

        final EditText editText = (EditText) view.findViewById(R.id.edit_text);
        if (itemName == null) {
            okButton.setEnabled(false);
        } else {
            editText.setText(itemName);
            validateItemName();
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start,
                    final int count, final int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start,
                    final int before, final int count) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
                itemName = s.toString();
                validateItemName();
            }
        });

        return view;
    }

    protected abstract int getDialogTitle();

    private void validateItemName() {
        if (okButton != null) {
            okButton.setEnabled(validateName(itemName));
        }
    }

    protected abstract boolean validateName(final String itemName);

    public interface OnNewFolderListener {
        /**
         * Name is validated to be non-null, non-empty and not containing any
         * slashes.
         *
         * @param name The name of the folder the user wishes to create.
         */
        public void onNewFolder(final String name);
    }
}
