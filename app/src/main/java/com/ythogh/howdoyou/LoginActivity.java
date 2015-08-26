package com.ythogh.howdoyou;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;


public class LoginActivity extends ActionBarActivity {

    private ImageSwitcher imageSwitcher;
    private EditText etUsername, etPassword;
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mContext = getApplicationContext();

        imageSwitcher = (ImageSwitcher)findViewById(R.id.imageswitcher);
        etUsername = (EditText) findViewById(R.id.username);

        etUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
                    public View makeView() {
                        ImageView myView = new ImageView(getApplicationContext());
                        return myView;
                    }
                });
                imageSwitcher.setImageResource(R.drawable.ic_launcher);
                Animation in = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
                Animation out = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_out_right);
                imageSwitcher.setInAnimation(in);
                imageSwitcher.setOutAnimation(out);
                imageSwitcher.animate();
            }
        });

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

}
