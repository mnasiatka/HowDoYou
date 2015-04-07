package com.ythogh.howdoyou;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class LoginActivity extends ActionBarActivity implements ViewPager.OnPageChangeListener {

    public static int PAGE_NUMBER = 0;
    private static final int NUM_PAGES = 7;
    TextView tvPage, tvFragmentPage;
    ViewPager vfPager;
    PagePagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tvPage = (TextView) findViewById(R.id.LOGIN_TEXTVIEW_PAGE);

        vfPager = (ViewPager) findViewById(R.id.viewPager);
        mPagerAdapter = new PagePagerAdapter(getSupportFragmentManager());
        vfPager.setAdapter(mPagerAdapter);
        vfPager.setOnPageChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPageScrolled(int position, float offset, int offsetPixels) {
        PAGE_NUMBER = position + 1;
    }

    @Override
    public void onPageSelected(int position) {
        PAGE_NUMBER = position + 1;
        System.out.println("Page: " + (position + 1));
        tvPage.setText("Page " + (position + 1));
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class PagePagerAdapter extends FragmentStatePagerAdapter {

        public PagePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            System.out.println("Item: " + i);
            PagePagerFragment ppf = new PagePagerFragment();
            Bundle extras = new Bundle();
            extras.putInt("position", i);
            ppf.setArguments(extras);
            return ppf;
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
