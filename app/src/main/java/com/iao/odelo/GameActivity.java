package com.iao.odelo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
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
    AlertDialog dialog;

    int arrLength;
    int scoreBlack, scoreWhite;
    int stone;
    boolean isGameOver = false;
    boolean isTouch = true;

    SegmentTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        arrLength = getIntent().getIntExtra("arr", 0);
        stone = getIntent().getIntExtra("stone", 0);

        Log.d(TAG, arrLength + " " + stone);

        tvBlack = (TextView) findViewById(R.id.tvBlack);
        tvWhite = (TextView) findViewById(R.id.tvWhite);
        tvTurn = (TextView) findViewById(R.id.tvTurn);

        frameBlack = (FrameLayout) findViewById(R.id.frameBlack);
        frameTurn = (FrameLayout) findViewById(R.id.frameTurn);
        frameWhite = (FrameLayout) findViewById(R.id.frameWhite);

        timer = new SegmentTimer();
        timer.start();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        boardView = new BoardView(this, arrLength);
        boardLayout = (FrameLayout) findViewById(R.id.frameLayout);
        boardLayout.addView(boardView);
        boardLayout.setOnTouchListener(new TouchListener());
        boardView.setBoardSize(boardLayout.getWidth(), boardLayout.getHeight());
        if (stone == 0 || stone == 1) {
            tvTurn.setTextColor(Color.WHITE);
            frameTurn.setBackgroundColor(Color.BLACK);
        } else if (stone == -1) {
            frameTurn.setBackgroundColor(Color.WHITE);
            compute();
        }


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
            //if (stone != 0 && boardView.turn != stone) {
            if (isTouch) {
                if (boardView.drawStone((int) event.getX(), (int) event.getY())) {
                    nextTurn();
                }
//            } else if (stone != 0 && boardView.turn != stone) {
//                Log.d(TAG, "***************************computer Turn");
//                boardView.computerTurn(boardView.turn);
//                nextTurn();
//            }

                findNextLocation();

                if (stone != 0 && !isGameOver)
                    compute();
            }
            return false;
        }
    }

    private void findNextLocation(){
        boardView.clearArrayList();
        setScore();
        Log.d(TAG, "scoreBlack : " + scoreBlack + " scoreWhite : " + scoreWhite);
        if ((scoreBlack + scoreWhite) == arrLength * arrLength || scoreBlack == 0 || scoreWhite == 0)
            gameOver();
        else if (!boardView.canLocateAllStone(boardView.turn)){
            Log.d(TAG, "next");
            Toast.makeText(getApplicationContext(), "돌을 놓을 수 없으므로 턴을 넘깁니다.", Toast.LENGTH_SHORT).show();
            nextTurn();
        }
    }

    private void gameOver() {
        isGameOver = true;
        Log.d(TAG, "gameOver()");
        dialog = createDialogBox();
        dialog.setCancelable(false);
        if (timer.isTiming())
            timer.stop();
        dialog.show();
    }

    private AlertDialog createDialogBox() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String message = timer.getTime() + "\n흑 : " + scoreBlack + " 백 : " + scoreWhite + "\n";
        if (scoreBlack > scoreWhite)
            builder.setTitle("흑의 승리입니다.");
        else if(scoreWhite > scoreBlack)
            builder.setTitle("백의 승리입니다.");
        else
            builder.setTitle("무승부 입니다. ");
        message += "다시 하시겠습니까?\n";

        builder.setMessage(message);

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                initGame();
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        return dialog;
    }
    private void initGame(){
        setScore();
        timer.start();
        boardView.turn = 1;
        tvTurn.setText("흑의 차례");
    }

    private void nextTurn(){
        boardView.turn *= -1;
        if (boardView.turn == boardView.WHITESTONE) {
            frameTurn.setBackgroundColor(Color.WHITE);
            tvTurn.setTextColor(Color.BLACK);
            tvTurn.setText("백의 차례");
        } else {
            frameTurn.setBackgroundColor(Color.BLACK);
            tvTurn.setTextColor(Color.WHITE);
            tvTurn.setText("흑의 차례");
        }


    }

    private void compute(){
        if (boardView.turn != stone) {
            Log.d(TAG, "***************************computer Turn");
            isTouch = false;
            handler.sendEmptyMessageDelayed(0, 1000);
        }
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            boardView.computerTurn(boardView.turn);

            nextTurn();
            findNextLocation();
            isTouch = true;
            return false;
        }
    });

    private void setScore() {
        scoreBlack = boardView.getScore(boardView.BLACKSTONE);
        scoreWhite = boardView.getScore(boardView.WHITESTONE);

        tvBlack.setText("흑 : " + scoreBlack);
        tvWhite.setText("백 : " + scoreWhite);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer.isTiming())
            timer.stop();
    }
}
