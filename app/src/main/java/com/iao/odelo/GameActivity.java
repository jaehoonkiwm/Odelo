package com.iao.odelo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;


public class GameActivity extends Activity {
    private static final String TAG = "GameActivity";

    FrameLayout boardLayout, frameBlack, frameTurn, frameWhite;
    BoardView boardView;
    TextView tvBlack, tvWhite, tvTurn;

    int arrLength;
    int scoreBlack, scoreWhite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        arrLength = getIntent().getIntExtra("arr", 0);

        tvBlack = (TextView) findViewById(R.id.tvBlack);
        tvWhite = (TextView) findViewById(R.id.tvWhite);
        tvTurn = (TextView) findViewById(R.id.tvTurn);

        frameBlack = (FrameLayout) findViewById(R.id.frameBlack);
        frameTurn = (FrameLayout) findViewById(R.id.frameTurn);
        frameWhite = (FrameLayout) findViewById(R.id.frameWhite);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d(TAG, "1");
        boardView = new BoardView(this, arrLength);
        Log.d(TAG, "2");
        boardLayout = (FrameLayout) findViewById(R.id.frameLayout);
        Log.d(TAG, "3");
        boardLayout.addView(boardView);
        boardLayout.setOnTouchListener(new TouchListener());

        boardView.setBoardSize(boardLayout.getWidth(), boardLayout.getHeight());
        Log.i(TAG, boardLayout.getWidth() + " " + boardLayout.getHeight()); // 736 1113
        Log.i(TAG, boardView.getWidth() + " " + boardView.getHeight());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
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

    public class TouchListener implements View.OnTouchListener{
        private static final String TAG = "TouchListener";
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.i(TAG, event.getX() + " " + event.getY());
            if (boardView.drawStone((int) event.getX(), (int) event.getY())) {
                    nextTurn();
            }

            if(setScore() == arrLength * arrLength)
                gameOver();

            if (!boardView.canLocateAllStone(boardView.turn)){
                Log.d(TAG, "next");
                Toast.makeText(getApplicationContext(), "돌을 놓을 수 없으므로 턴을 넘깁니다.", Toast.LENGTH_SHORT).show();
                nextTurn();
            }
            return false;
        }
    }

    private void gameOver() {
        Dialog dialog = new Dialog(this);
        dialog.setTitle("게임 종료");
        dialog.show();
    }

    private void nextTurn(){
        boardView.turn *= -1;
        if (boardView.turn == boardView.WHITESTONE)
            tvTurn.setText("백의 차례");
        else
            tvTurn.setText("흑의 차례");
    }

    private int setScore() {
        scoreBlack = boardView.getScore(boardView.BLACKSTONE);
        scoreWhite = boardView.getScore(boardView.WHITESTONE);

        tvBlack.setText("흑 : " + scoreBlack);
        tvWhite.setText("백 : " + scoreWhite);

        return scoreBlack + scoreWhite;
    }
}
