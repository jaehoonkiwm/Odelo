package com.iao.odelo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by iao on 15. 6. 11.
 */
public class BoardView extends View {
    private static final String TAG = "BoardView";
    public static final int WHITESTONE = -1;
    public static final int BLACKSTONE = 1;
    public int turn;

    private int arrLength;
    private Paint paint;

    private Bitmap imageWhiteStone;
    private Bitmap imageBlackStone;
    private Bitmap imageTmp;

    private int [][] positions;        // 0은 빈공간, 1은 흑돌, -1은 백돌
    private int [][] signs = {{-1, -1}, {-1, 0}, {-1, 1},
                              {0, -1},           {0, 1},
                              {1, -1},  {1, 0},  {1, 1}};
    private int [] directions;

    private int boardWidth;
    private int boardHeight;
    private int width;

    public BoardView(Context context, int arrLength) {
        super(context);
        //setBackgroundColor(Color.rgb(55, 125, 63));
        this.arrLength = arrLength;
        positions = new int[arrLength+2][arrLength+2];
        positions[2][2] = positions[3][3] = -1;
        positions[2][3] = positions[3][2] = 1;
        directions = new int[8];
        paint = new Paint();


        this.turn = BLACKSTONE;
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setBoardSize(int width, int height){
        this.boardWidth = width;
        this.boardHeight = height;
        this.width = (boardWidth - 100) / arrLength;
        imageTmp = BitmapFactory.decodeResource(getResources(), R.drawable.blackstone);
        imageBlackStone = Bitmap.createScaledBitmap(imageTmp, this.width, this.width, false);
        imageTmp = BitmapFactory.decodeResource(getResources(), R.drawable.whitestone);
        imageWhiteStone = Bitmap.createScaledBitmap(imageTmp, this.width, this.width, false);
    }

    public boolean drawStone(int x, int y) {
        int positionX = getPositionX(x);
        int positionY = getPositionY(y);

        Log.d(TAG, "x : " + positionX + " y : " + positionY);
        if (positionX > 0 && positionX < positions[0].length - 1 &&
                 positionY > 0 && positionY < positions.length - 1 &&
                positions[positionY][positionX] == 0) {
            if (canLocatestone(turn, positionX, positionY)) {
                reverseStones(turn, positionX, positionY);
                resetDirection();
                invalidate();
                printPositions();
                return true;
            }
        }
        return false;
    }

    private int getPositionX(int positionX) {
        for (int i = 0; i < arrLength; ++i)
            if (positionX >= (50 + (i * width)) && positionX <= (200 + (i * width)))
                    return i + 1;

        return -1;
    }

    private int getPositionY(int positionY) {
        for (int i = 0; i < arrLength; ++i)
            if (positionY >= (200 + (i * width)) && positionY <= ((200 + width) + (i * width)))
                return i + 1;
        return -1;
    }

    private boolean canLocatestone(int colorOfStone, int positionX, int positionY) {
        checkPosition(colorOfStone, positionX, positionY);

        for (int i = 0; i < directions.length; ++i) {
            if (directions[i] > 0)
                return true;
        }
        return false;
    }

    private void checkPosition(int colorOfStone, int positionX, int positionY) {
        int x, y;
        for (int i = 0; i < signs.length; ++i) {
            x = positionX + signs[i][1];
            y = positionY + signs[i][0];
            while (positions[y][x] != colorOfStone) {
                if (positions[y][x] == 0){
                    directions[i] = 0;
                    break;
                }
                ++directions[i];
                x += signs[i][1];
                y += signs[i][0];
            }
        }
    }

    private int getOppositeColor(int colorOfStone) {
        if (colorOfStone == BLACKSTONE)
            return WHITESTONE;
        else
            return BLACKSTONE;
    }

    private void reverseStones(int color, int positionX, int positionY){
        Log.d(TAG, "reverseStones()");
        int x = 0, y = 0;
        for (int i = 0; i < directions.length; ++i) {
            for (int j = 0; j < directions[i]; ++j) {
                x = positionX + signs[i][1];
                y = positionY + signs[i][0];
                while (positions[y][x] != color){
                    positions[y][x] = color;
                    x += signs[i][1];
                    y += signs[i][0];
                }
            }
        }
        positions[positionY][positionX] = color;

    }

    private void resetDirection() {
        for (int i = 0; i < directions.length; ++i) {
            directions[i] = 0;
        }
    }

    public boolean canLocateAllStone(int colorOfStone){
        Log.d(TAG, "canLocateAllStone()");
        for (int i = 1; i < positions.length - 1; ++i) {
            for (int j = 1; j < positions[i].length - 1; ++j) {
                if (positions[i][j] == 0) {
                    Log.d(TAG, i+","+j+"탐색");
                    if(canLocatestone(colorOfStone, j, i)) {
                        printPositions();
                        Log.d(TAG, "TRUE : y:" + i + ", x:" + j);
                        resetDirection();
                        return true;
                    }
                    resetDirection();
                }
            }
        }
        return false;
    }

    void printPositions(){
        for (int i = 0; i < positions.length; ++i)
            Log.d (TAG, positions[i][0] + " " + positions[i][1] + " " + positions[i][2] + " " + positions[i][3] + " " + positions[i][4] + " " + positions[i][5]);
    }

    public int getScore(int colorOfStone) {
        int score = 0;
        for (int i = 0; i < positions.length; ++i)
            for (int j = 0; j < positions[i].length; ++j)
                if (positions[i][j] == colorOfStone)
                    ++score;

        return score;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw()");
        Log.d(TAG, getWidth() + " " + boardWidth);

        paint.setColor(Color.rgb(55, 125, 63));
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < arrLength; ++i)
            for (int j = 0; j < arrLength; ++j)
                canvas.drawRect(50 + (j * width), 200 + (i * width), (50 + width) + (j * width), (200 + width) + (i * width), paint);

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

            for (int i = 0; i < arrLength; ++i)
                for (int j = 0; j < arrLength; ++j) {
                    canvas.drawRect(50 + (j * width), 200 + (i * width), (50 + width) + (j * width), (200 + width) + (i * width), paint);

                    if (positions[i + 1][j + 1] == 1)
                        canvas.drawBitmap(imageBlackStone, 50 + (j * width), 200 + (i * width), null);
                    else if (positions[i + 1][j + 1] == -1)
                        canvas.drawBitmap(imageWhiteStone, 50 + (j * width), 200 + (i * width), null);
                }
    }
}
