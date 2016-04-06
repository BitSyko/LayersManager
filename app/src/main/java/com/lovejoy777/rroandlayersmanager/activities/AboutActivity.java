package com.lovejoy777.rroandlayersmanager.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.adapters.AboutAdapter;
import com.lovejoy777.rroandlayersmanager.beans.DeveloperBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AboutActivity extends AppCompatActivity {

    @Bind(R.id.toolbar_about) Toolbar toolbar;
    @Bind(R.id.elv_about_developers) ExpandableListView elv_developers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        DeveloperBean[] developers = {
                new DeveloperBean("Syko Pompos", getString(R.string.LayersLeadDeveloper), getDrawable(R.drawable.about_syko), getString(R.string.linkSyko)),
                new DeveloperBean("Reinhard Strauch", getString(R.string.LayersLeadDeveloper), getDrawable(R.drawable.about_reinhard), getString(R.string.linkReinhard)),
                new DeveloperBean("Brian Gill", getString(R.string.LayersDeveloper), getDrawable(R.drawable.about_brian), getString(R.string.linkBrian)),
                new DeveloperBean("Aldrin Holmes", getString(R.string.LayersDeveloper), getDrawable(R.drawable.about_aldrin), getString(R.string.linkAldrin)),
                new DeveloperBean("Branden Manibusan", getString(R.string.LayersDeveloper), getDrawable(R.drawable.about_branden), getString(R.string.linkBranden)),
                new DeveloperBean("Steve Lovejoy", getString(R.string.AppDeveloper), getDrawable(R.drawable.about_steve), getString(R.string.linkSteve)),
                new DeveloperBean("Niklas Schnettler", getString(R.string.AppDeveloper), getDrawable(R.drawable.about_niklas), getString(R.string.linkNiklas)),
                new DeveloperBean("Andrzej Ressel", getString(R.string.AppDeveloper), getDrawable(R.drawable.about_andrzej), getString(R.string.linkAndrzej)),
                new DeveloperBean("Denis Suarez", getString(R.string.ShowcaseDeveloper), getDrawable(R.drawable.about_denis), getString(R.string.linkDenis)),

        };

        DeveloperBean[] usefulLinks = {
                new DeveloperBean("Layers on Google Plus", getString(R.string.findOutWhatsNew), getDrawable(R.drawable.about_bitsyko), getString(R.string.linkCommunity)),
                new DeveloperBean("Layers on XDA", getString(R.string.joinTheChat), getDrawable(R.drawable.about_xda), getString(R.string.linkXda))
        };


        //set Toolbar
        toolbar.setNavigationIcon(R.drawable.ic_menu_back_white_24dp);
        setSupportActionBar(toolbar);

        final HashMap<String, List<DeveloperBean>> listDataChild = new HashMap<>();
        final ArrayList<String> listDataHeader = new ArrayList<>();
        listDataHeader.add(getResources().getString(R.string.developedby));
        listDataHeader.add(getResources().getString(R.string.usefullinks));

        listDataChild.put(listDataHeader.get(0), Arrays.asList(developers));
        listDataChild.put(listDataHeader.get(1), Arrays.asList(usefulLinks));

        elv_developers.setGroupIndicator(null);
        elv_developers.setAdapter(new AboutAdapter(this, listDataHeader, listDataChild));

        for (int i = 0; i < elv_developers.getExpandableListAdapter().getGroupCount(); i++) {
            elv_developers.expandGroup(i);
        }

        elv_developers.setDividerHeight(26);


        elv_developers.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                if (groupPosition <= 1) {
                    String url = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).getWebpage();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } else {
                    throw new IllegalArgumentException();
                }

                return true;
            }
        });

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
