package com.iao.odelo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class SelectActivity extends Activity {
    String gameMode;
    Intent sendIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        gameMode = getIntent().getStringExtra("vs");
        sendIntent = new Intent(this, GameActivity.class);

    }

    public void onArrButtonClicked(View v) {

        switch (v.getId()) {
            case R.id.btnByFour :
                sendIntent.putExtra("vs", gameMode);
                sendIntent.putExtra("arr", 4);
                break;
            case R.id.btnByEight :
                sendIntent.putExtra("vs", gameMode);
                sendIntent.putExtra("arr", 8);
                break;
        }

        if (gameMode.equals("btncomputer")){
            AlertDialog dialog = createDialogBox();
            dialog.show();
        } else {
            startActivity(sendIntent);
        }
    }

    private AlertDialog createDialogBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("돌을 선택하세요.");

        builder.setNegativeButton("흑", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendIntent.putExtra("stone", 1);
                startActivity(sendIntent);
            }
        });

        builder.setPositiveButton("백", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendIntent.putExtra("stone", -1);
                startActivity(sendIntent);
            }
        });

        AlertDialog dialog = builder.create();
        return dialog;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
