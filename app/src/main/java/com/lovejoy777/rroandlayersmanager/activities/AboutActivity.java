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

    ListView list1, list2, list3, list4, list5,list6,list7,list12;
    ImageButton moreButton1, moreButton2;

    Integer[] developerImage1 = {
            R.drawable.about_syko,
    };
    Integer[] developerImage12 = {
            R.drawable.about_reinhard,
    };
    Integer[] developerImage2 = {
            R.drawable.about_brian,
    };
    Integer[] developerImage3 = {
            R.drawable.about_aldrin,
    };
    Integer[] developerImage4 = {
            R.drawable.about_steve,
    };
    Integer[] developerImage5 = {
            R.drawable.about_niklas,
    };
    Integer[] linkImage1 = {
            R.drawable.about_bitsyko,
    };
    Integer[] linkImage2 = {
            R.drawable.about_xda,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        String[] ListContent1 = {
                "Bitsyko Development Team"
        };

        String[] Developer1 = {
                "Syko Pompos"
        } ;

        String[] Developer12 = {
                "Reinhard Strauch"
        } ;

        String[] Developer2 = {
                "Brian Gill"
        } ;
        String[] Developer3 = {
                "Aldrin Holmes"
        } ;
        String[] Developer4 = {
                "Steve Lovejoy"
        } ;
        String[] Developer5 = {
                "Niklas Schnettler"
        } ;
        String[] Link1 = {
                "Layers on Google Plus"
        } ;
        String[] Link2 = {
                "Layers on XDA"
        } ;
        String[] LeadDeveloper = {
                "Layers Lead Developer"
        };
        String[] Developer = {
                "Layers Developer"
        };
        String[] AppDeveloper = {
                "App Developer"
        };
        String[] Nothing1 = {
                "Find out whats new"
        };
        String[] Nothing2 = {
                "Join the chat"
        };

        //set Toolbar
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar4);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        setSupportActionBar(toolbar);

        //Developer1
        CustomListAdapter adapter = new CustomListAdapter(AboutActivity.this, Developer1,LeadDeveloper, developerImage1);
        list1=(ListView)findViewById(R.id.listView_Developer1);
        list1.setAdapter(adapter);
        list1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/+SykoPompos/posts")));
            }
        });

        //Developer1,5
        CustomListAdapter adapter12 = new CustomListAdapter(AboutActivity.this, Developer12,LeadDeveloper, developerImage12);
        list12=(ListView)findViewById(R.id.listView_Developer1_5);
        list12.setAdapter(adapter12);
        list12.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/101549242713977412381/posts")));
            }
        });

        //Developer2
        CustomListAdapter adapter2 = new
                CustomListAdapter(AboutActivity.this, Developer2, Developer,developerImage2);
        list2=(ListView)findViewById(R.id.listView_Developer2);
        list2.setAdapter(adapter2);
        list2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/+BrianGill55/posts")));
            }
        });


        //Developer3
        CustomListAdapter adapter3 = new
                CustomListAdapter(AboutActivity.this, Developer3,Developer, developerImage3);
        list3=(ListView)findViewById(R.id.listView_Developer3);
        list3.setAdapter(adapter3);
        list3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/+AldrinHolmes20/posts")));
            }
        });

        //4
        CustomListAdapter adapter4 = new
                CustomListAdapter(AboutActivity.this, Developer4,AppDeveloper, developerImage4);
        list4=(ListView)findViewById(R.id.listView_Developer4);
        list4.setAdapter(adapter4);
        list4.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/+SteveLovejoy/posts")));
            }
        });

        //Developer5
        CustomListAdapter adapter5 = new
                CustomListAdapter(AboutActivity.this, Developer5,AppDeveloper, developerImage5);
        list5=(ListView)findViewById(R.id.listView_Developer5);
        list5.setAdapter(adapter5);
        list5.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/+NiklasSchnettler/posts")));
            }
        });

        //Link1
        CustomListAdapter adapter6 = new
                CustomListAdapter(AboutActivity.this, Link1,Nothing1, linkImage1);
        list6=(ListView)findViewById(R.id.listView_link1);
        list6.setAdapter(adapter6);
        list6.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/communities/102261717366580091389")));
            }
        });

        //Link1
        CustomListAdapter adapter7 = new
                CustomListAdapter(AboutActivity.this, Link2,Nothing2, linkImage2);
        list7=(ListView)findViewById(R.id.listView_link2);
        list7.setAdapter(adapter7);
        list7.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        moreButton1.setOnClickListener(onclicklistener);
        moreButton2 = (ImageButton) findViewById(R.id.imBu_more2);
        moreButton2.setOnClickListener(onclicklistener);

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
