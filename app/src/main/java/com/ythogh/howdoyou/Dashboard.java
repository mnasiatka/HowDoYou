package com.ythogh.howdoyou;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class Dashboard extends ActionBarActivity implements View.OnClickListener {

    Button btBrowse, btCreate, btUsers;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        btBrowse = (Button) findViewById(R.id.DASHBOARD_BUTTON_BROWSE);
        btCreate = (Button) findViewById(R.id.DASHBOARD_BUTTON_CREATE);
        btUsers = (Button) findViewById(R.id.DASHBOARD_BUTTON_USERS);
        btBrowse.setOnClickListener(this);
        btCreate.setOnClickListener(this);
        btUsers.setOnClickListener(this);

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
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
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.DASHBOARD_BUTTON_BROWSE:
                break;
            case R.id.DASHBOARD_BUTTON_CREATE:
                intent = new Intent(getApplicationContext(), CreateTutorial.class);
                startActivity(intent);
                break;
            case R.id.DASHBOARD_BUTTON_USERS:
                break;
            default:
                break;
        }
    }
}
