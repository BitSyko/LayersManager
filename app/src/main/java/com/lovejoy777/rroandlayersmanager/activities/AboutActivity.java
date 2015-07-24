package com.lovejoy777.rroandlayersmanager.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.adapters.AboutAdapter;
import com.lovejoy777.rroandlayersmanager.beans.DeveloperBean;
import com.lovejoy777.rroandlayersmanager.beans.DeveloperFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        DeveloperFactory factory = new DeveloperFactory(this);

        DeveloperBean[] developers = {
                factory.createDeveloper("Syko Pompos", getString(R.string.LayersLeadDeveloper), R.drawable.about_syko, R.string.linkSyko),
                factory.createDeveloper("Reinhard Strauch", getString(R.string.LayersLeadDeveloper), R.drawable.about_reinhard, R.string.linkReinhard),
                factory.createDeveloper("Brian Gill", getString(R.string.LayersDeveloper), R.drawable.about_brian, R.string.linkBrian),
                factory.createDeveloper("Aldrin Holmes", getString(R.string.LayersDeveloper), R.drawable.about_aldrin, R.string.linkAldrin),
                factory.createDeveloper("Steve Lovejoy", getString(R.string.AppDeveloper), R.drawable.about_steve, R.string.linkSteve),
                factory.createDeveloper("Niklas Schnettler", getString(R.string.AppDeveloper), R.drawable.about_niklas, R.string.linkNiklas),
                factory.createDeveloper("Branden Manibusan", getString(R.string.AditionalLayersDev), R.drawable.about_branden, R.string.linkBranden),
                factory.createDeveloper("Denis Suarez", getString(R.string.ShowcaseDeveloper), R.drawable.about_denis, R.string.linkDenis),
        };

        DeveloperBean[] usefulLinks = {
                factory.createDeveloper("Layers on Google Plus", getString(R.string.findOutWhatsNew), R.drawable.about_bitsyko, R.string.linkCommunity),
                factory.createDeveloper("Layers on XDA", getString(R.string.joinTheChat), R.drawable.about_xda, R.string.linkXda)
        };

        DeveloperBean[] libraries = {
                factory.createDeveloper(getString(R.string.License1), getString(R.string.License1about), R.drawable.ic_drawer_about, R.string.linkCommunity),
                factory.createDeveloper(getString(R.string.License2), getString(R.string.License2about), R.drawable.ic_drawer_about, R.string.linkCommunity),
                factory.createDeveloper(getString(R.string.License3), getString(R.string.License3about), R.drawable.ic_drawer_about, R.string.linkCommunity)
        };

        //set Toolbar
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar4);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        setSupportActionBar(toolbar);


        TextView tv_version = (TextView) findViewById(R.id.tv_Version);
        try {
            String versionName = AboutActivity.this.getPackageManager()
                    .getPackageInfo(AboutActivity.this.getPackageName(), 0).versionName;
            tv_version.setText(getResources().getString(R.string.version) + " " + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        ExpandableListView devlist = (ExpandableListView) findViewById(R.id.developers);

        final HashMap<String, List<DeveloperBean>> listDataChild = new HashMap<String, List<DeveloperBean>>();

        final ArrayList<String> listDataHeader = new ArrayList<String>();
        listDataHeader.add(getResources().getString(R.string.developedby));
        listDataHeader.add(getResources().getString(R.string.usefullinks));
        listDataHeader.add(getResources().getString(R.string.OpenSourceLicenses));

        listDataChild.put(listDataHeader.get(0), Arrays.asList(developers));
        listDataChild.put(listDataHeader.get(1), Arrays.asList(usefulLinks));
        listDataChild.put(listDataHeader.get(2), Arrays.asList(libraries));

        devlist.setGroupIndicator(null);
        devlist.setAdapter(new AboutAdapter(this, listDataHeader, listDataChild));

        for (int i = 0; i < devlist.getExpandableListAdapter().getGroupCount(); i++) {
            devlist.expandGroup(i);
        }

        devlist.setDividerHeight(26);

        devlist.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                if (groupPosition <=1) {

                    String url = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).getWebpage();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else if (groupPosition == 2) {

                    showLicenceAlert(childPosition);

                    return true;
                } else {
                    throw new IllegalArgumentException();
                }
            }
        });

    }


    private void showLicenceAlert(int position) {

        String dialogText = "";
        String dialogTitleText = "";
        LayoutInflater li = LayoutInflater.from(AboutActivity.this);
        View view3 = li.inflate(R.layout.dialog_license, null);
        final TextView tv_license = (TextView) view3.findViewById(R.id.tv_license);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(AboutActivity.this);
        dialog.setView(view3);
        switch (position) {
            case 0:
                dialogText = getResources().getString(R.string.License1more);
                dialogTitleText = getResources().getString(R.string.License1);
                dialog.setPositiveButton(R.string.VisitGithub, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.License1github)));
                        startActivity(browserIntent);
                    }
                });

                break;
            case 1:
                dialogText = getResources().getString(R.string.License2more);
                dialogTitleText = getResources().getString(R.string.License2);
                dialog.setPositiveButton(R.string.VisitGithub, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.License2github)));
                        startActivity(browserIntent);
                    }
                });
                break;
            case 2:
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
        dialog.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.back2, R.anim.back1);
    }
}
