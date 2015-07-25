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
import com.lovejoy777.rroandlayersmanager.beans.LicenceBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        DeveloperBean[] developers = {
                new DeveloperBean("Syko Pompos", getString(R.string.LayersLeadDeveloper), getDrawable(R.drawable.about_syko), getString(R.string.linkSyko)),
                new DeveloperBean("Reinhard Strauch", getString(R.string.LayersLeadDeveloper), getDrawable(R.drawable.about_reinhard), getString(R.string.linkReinhard)),
                new DeveloperBean("Brian Gill", getString(R.string.LayersDeveloper), getDrawable(R.drawable.about_brian), getString(R.string.linkBrian)),
                new DeveloperBean("Aldrin Holmes", getString(R.string.LayersDeveloper), getDrawable(R.drawable.about_aldrin), getString(R.string.linkAldrin)),
                new DeveloperBean("Steve Lovejoy", getString(R.string.AppDeveloper), getDrawable(R.drawable.about_steve), getString(R.string.linkSteve)),
                new DeveloperBean("Niklas Schnettler", getString(R.string.AppDeveloper), getDrawable(R.drawable.about_niklas), getString(R.string.linkNiklas)),
                new DeveloperBean("Branden Manibusan", getString(R.string.AditionalLayersDev), getDrawable(R.drawable.about_branden), getString(R.string.linkBranden)),
                new DeveloperBean("Denis Suarez", getString(R.string.ShowcaseDeveloper), getDrawable(R.drawable.about_denis), getString(R.string.linkDenis)),
        };

        DeveloperBean[] usefulLinks = {
                new DeveloperBean("Layers on Google Plus", getString(R.string.findOutWhatsNew), getDrawable(R.drawable.about_bitsyko), getString(R.string.linkCommunity)),
                new DeveloperBean("Layers on XDA", getString(R.string.joinTheChat), getDrawable(R.drawable.about_xda), getString(R.string.linkXda))
        };

        DeveloperBean[] libraries = {
                new LicenceBean(getString(R.string.License1), getString(R.string.License1about), getDrawable(R.drawable.ic_opensource), getString(R.string.License1github),getString(R.string.License1more)),
                new LicenceBean(getString(R.string.License2), getString(R.string.License2about), getDrawable(R.drawable.ic_opensource), getString(R.string.License2github),getString(R.string.License2more)),
                new LicenceBean(getString(R.string.License3), getString(R.string.License3about), getDrawable(R.drawable.ic_opensource), getString(R.string.License3github),getString(R.string.License3more))
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

                if (groupPosition <= 1) {
                    String url = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).getWebpage();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } else if (groupPosition == 2) {
                    showLicenceAlert((LicenceBean) parent.getExpandableListAdapter().getChild(groupPosition, childPosition));
                } else {
                    throw new IllegalArgumentException();
                }

                return true;
            }
        });

    }


    private void showLicenceAlert(final LicenceBean licenceBean) {

        LayoutInflater li = LayoutInflater.from(AboutActivity.this);
        View view3 = li.inflate(R.layout.dialog_license, null);
        final TextView tv_license = (TextView) view3.findViewById(R.id.tv_license);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(AboutActivity.this);
        dialog.setView(view3);

        dialog.setTitle(licenceBean.getTitle());
        tv_license.setText(licenceBean.getLongDescription());

        dialog.setPositiveButton(R.string.VisitGithub, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(licenceBean.getWebpage()));
                startActivity(browserIntent);
            }
        });

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
