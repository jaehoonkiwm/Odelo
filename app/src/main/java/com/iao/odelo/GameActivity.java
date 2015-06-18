package com.iao.odelo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iao.bluetooth.BluetoothService;
import com.iao.bluetooth.Constants;
import com.iao.bluetooth.DeviceListActivity;


public class GameActivity extends ActionBarActivity {
    private static final String TAG = "GameActivity";

    FrameLayout boardLayout, frameBlack, frameTurn, frameWhite;
    BoardView boardView;
    TextView tvBlack, tvWhite, tvTurn;
    AlertDialog dialog;

    int arrLength;
    int scoreBlack, scoreWhite;
    int stone;
    int myTurn;
    boolean isGameOver = false;
    boolean isTouch = true;


    String gameMode;
    SegmentTimer timer;

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothService mChatService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        arrLength = getIntent().getIntExtra("arr", 0);
        stone = getIntent().getIntExtra("stone", 0);
        gameMode = getIntent().getStringExtra("vs");
        myTurn = boardView.BLACKSTONE;
        if(!gameMode.equals("bluetooth"))
            getSupportActionBar().hide();
        else {
            getSupportActionBar().setTitle("");
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            // If the adapter is null, then Bluetooth is not supported
            if (mBluetoothAdapter == null) {
                FragmentActivity activity = this;
                Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
                activity.finish();
            }
        }

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

    public class TouchListener implements View.OnTouchListener{
        private static final String TAG = "TouchListener";
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.i(TAG, event.getX() + " " + event.getY() + " " + isTouch);
            int x = (int) event.getX();
            int y = (int) event.getY();

            if (isTouch) {
                if (boardView.drawStone(x, y)) {

                    nextTurn();
                    if (gameMode.equals("bluetooth")) {
                        String points = x + " " + y;
                        sendMessage(points);
                        isTouch = false;
                    }
                }

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
            if (gameMode.equals("bluetooth"))
                sendMessage("상대방이 돌을 놓을 수 없어 다시 턴이 돌아왔습니다.");
            Toast.makeText(getApplicationContext(), "돌을 놓을 수 없으므로 턴을 넘깁니다.", Toast.LENGTH_SHORT).show();
            isTouch = false;
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
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (gameMode.equals("bluetooth")) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                // Otherwise, setup the chat session
            } else if (mChatService == null) {
                setupChat();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (timer.isTiming())
            timer.stop();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");


        // Initialize the compose field with a listener for the return key
//        et.setOnEditorActionListener(mWriteListener);

        // Initialize the BluetoothService to perform bluetooth connections
        mChatService = new BluetoothService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {

            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
//            tv2.setText(mOutStringBuffer);
        }
    }

    private void setStatus(int resId) {
        final android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    private void setStatus(CharSequence subTitle) {
        final android.support.v7.app.ActionBar actionBar = this.getSupportActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            if (myTurn == boardView.BLACKSTONE) {
                                Toast.makeText(getApplicationContext(), "흑입니다.", Toast.LENGTH_SHORT).show();
                                getSupportActionBar().setTitle("흑");
                            }else {
                                Toast.makeText(getApplicationContext(), "백입니다.", Toast.LENGTH_SHORT).show();
                                getSupportActionBar().setTitle("백");
                            }
                            //mConversationArrayAdapter.clear();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:

                        case BluetoothService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Toast.makeText(getApplicationContext(), mConnectedDeviceName + ":  " + readMessage, Toast.LENGTH_SHORT).show();
                    String[] points = readMessage.split(" ");
                    if (boardView.drawStone(Integer.parseInt(points[0]), Integer.parseInt(points[1]))) {
                        nextTurn();
                        findNextLocation();

                        if (stone != 0 && !isGameOver)
                            compute();
                    }
                    isTouch = true;
                    Log.d(TAG, "istouch " + isTouch);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    // if (null != activity) {
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    //}
                    break;
                case Constants.MESSAGE_TOAST:
                    // if (null != activity) {
                    myTurn = boardView.BLACKSTONE;
//                    isBlack = true;             // 블랙으로 초기화
                    Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST),
                            Toast.LENGTH_SHORT).show();
                    finish();
                    //}
                    break;
            }
            return false;
        }
    });

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    myTurn = boardView.WHITESTONE;
                    isTouch = false;
//                    isBlack = false;                                            // 백으로설정
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    //getActivity().finish();
                }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bluetooth_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }
        }
        return false;
    }

}
