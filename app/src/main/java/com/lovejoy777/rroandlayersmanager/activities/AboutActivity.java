package com.lovejoy777.rroandlayersmanager.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.adapters.CustomListAdapter;

/**
 * Created by lovejoy777 on 14/11/13.
 */
public class AboutActivity extends AppCompatActivity {



    ListView list1, list2, list3, list4, list5;
    ImageButton moreButton1, moreButton2, moreButton3;

    Integer[] listImage2 = {
            R.drawable.steve,
    };
    Integer[] listImage3 = {
            R.drawable.niklas,
    };
    Integer[] listImage1 = {
            R.drawable.bitsyko,
    };
    Integer[] listImage4 = {
            R.drawable.xda,
    };
    Integer[] listImage5 = {
            R.drawable.stefano,
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        String[] ListContent2 = {
                "Steve Lovejoy"
        } ;
        String[] ListContent3 = {
                "Niklas Schnettler"
        } ;
        String[] ListContent4 = {
                "Layers on XDA"
        } ;
        String[] ListContent5 = {
                this.getString(R.string.ThanksTo3)
        } ;

        String[] ListContent1 = {
                "Bitsyko Development Team"
        };


        //set Toolbar
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar4);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        setSupportActionBar(toolbar);


        //List 1
        CustomListAdapter adapter3 = new CustomListAdapter(AboutActivity.this, ListContent1, listImage1);
        list1=(ListView)findViewById(R.id.listView_ThemeDeveloper);
        list1.setAdapter(adapter3);
        list1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/communities/102261717366580091389")));
            }
        });

        //List 2
        CustomListAdapter adapter = new CustomListAdapter(AboutActivity.this, ListContent2, listImage2);
        list2=(ListView)findViewById(R.id.listView_AppDeveloper);
        list2.setAdapter(adapter);
        list2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/+SteveLovejoy/posts")));
            }
        });


        //List3
        CustomListAdapter adapter2 = new
                CustomListAdapter(AboutActivity.this, ListContent3, listImage3);
        list3=(ListView)findViewById(R.id.listView_ThanksTo1);
        list3.setAdapter(adapter2);
        list3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/+NiklasSchnettler/posts")));
            }
        });

        //List4
        CustomListAdapter adapter4 = new
                CustomListAdapter(AboutActivity.this, ListContent4, listImage4);
        list4=(ListView)findViewById(R.id.listView_ThanksTo2);
        list4.setAdapter(adapter4);
        list4.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/android/apps-games/official-layers-bitsyko-apps-rro-t3012172")));
            }
        });




        //app version textView
        TextView tv_version = (TextView) findViewById(R.id.tv_Version);
        try {
            String versionName = AboutActivity.this.getPackageManager()
                    .getPackageInfo(AboutActivity.this.getPackageName(), 0).versionName;
            tv_version.setText("Version " + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //i Buttons
        moreButton1 = (ImageButton) findViewById(R.id.imBu_more1);
        moreButton2 = (ImageButton) findViewById(R.id.imBu_more2);
        moreButton3 = (ImageButton) findViewById(R.id.imBu_more3);
        moreButton1.setOnClickListener(onclicklistener);
        moreButton2.setOnClickListener(onclicklistener);
        moreButton3.setOnClickListener(onclicklistener);



    }

    View.OnClickListener onclicklistener = new View.OnClickListener() {
        public void onClick(View v) {
            String dialogText = "";
            String dialogTitleText = "";
            LayoutInflater li = LayoutInflater.from(AboutActivity.this);
            View view3 = li.inflate(R.layout.dialog_license, null);
            final TextView tv_license = (TextView) view3.findViewById(R.id.tv_license);
            final AlertDialog.Builder dialog = new AlertDialog.Builder(AboutActivity.this);
            dialog.setView(view3);
            switch(v.getId()) {
                case R.id.imBu_more1:
                    dialogText = getResources().getString(R.string.License1more);
                    dialogTitleText = getResources().getString(R.string.License1);
                    dialog.setPositiveButton(R.string.VisitGithub, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.License1github)));
                            startActivity(browserIntent);
                        }
                    });
                    break;
                case R.id.imBu_more2:
                    dialogText = getResources().getString(R.string.License2more);
                    dialogTitleText = getResources().getString(R.string.License2);
                    dialog.setPositiveButton(R.string.VisitGithub, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.License2github)));
                            startActivity(browserIntent);
                        }
                    });
                    break;
                case R.id.imBu_more3:
                    dialogText = getResources().getString(R.string.License3more);
                    dialogTitleText = getResources().getString(R.string.License3);
                    dialog.setPositiveButton(R.string.VisitGithub, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.License3github)));
                            startActivity(browserIntent);
                        }
                    });
                    break;
            }
            dialog.setTitle(dialogTitleText);
            tv_license.setText(dialogText);
            dialog.setNegativeButton(R.string.Close, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.show();
        }
    };


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.back2, R.anim.back1);
    }



}
