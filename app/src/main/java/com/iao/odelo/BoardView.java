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

import java.util.ArrayList;

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
    private int [][] priorities;
    private ArrayList<Data> dList;


    private int boardWidth;
    private int boardHeight;
    private int width;

    public BoardView(Context context, int arrLength) {
        super(context);
        this.arrLength = arrLength;
        positions = new int[arrLength+2][arrLength+2];
        if (arrLength == 4) {
            positions[2][2] = positions[3][3] = -1;
            positions[2][3] = positions[3][2] = 1;
        } else if (arrLength == 8) {
            positions[4][4] = positions[5][5] = -1;
            positions[4][5] = positions[5][4] = 1;
        }
        directions = new int[8];
        setPriorities();
        paint = new Paint();
        dList = new ArrayList<Data>();
        this.turn = BLACKSTONE;
        canLocateAllStone(turn);


    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void setPriorities() {
        priorities = new int[arrLength+2][arrLength+2];

        if (arrLength == 4){
            priorities[1][1] = priorities[1][4] = priorities[4][1] = priorities[4][4] = 1;
            priorities[1][2] = priorities[1][3] = priorities[2][1] = priorities[2][4] = 2;
            priorities[3][1] = priorities[3][4] = priorities[4][2] = priorities[4][3] = 2;
        } else if (arrLength == 8) {
            priorities[1][1] = priorities[1][8] = priorities[8][1] = priorities[8][8] = 1;

            for (int i = 2; i < 8; ++i){
                priorities[1][i] = 2;
                priorities[i][1] = 2;
                priorities[i][8] = 2;
                priorities[8][i] = 2;
            }

            for (int i = 3; i < 7; ++i){
                priorities[3][i] = 3;
                priorities[i][3] = 3;
                priorities[i][6] = 3;
                priorities[6][i] = 3;

                priorities[2][i] = 4;
                priorities[i][2] = 4;
                priorities[i][7] = 4;
                priorities[7][i] = 4;
            }

            priorities[2][2] = priorities[2][7] = priorities[7][2] = priorities[7][7] = 5;
            for (int i = 1; i < 9; ++i){
                    Log.d(TAG,
                            priorities[i][1] + " " +
                            priorities[i][2] + " " +
                            priorities[i][3] + " " +
                            priorities[i][4] + " " +
                            priorities[i][5] + " " +
                            priorities[i][6] + " " +
                            priorities[i][7] + " " +
                            priorities[i][8] +"");
            }
        }
    }

    public void setBoardSize(int width, int height){
        this.boardWidth = width;
        this.boardHeight = height;
        this.width = (boardWidth - 100) / arrLength;
        Log.d(TAG, "width : "+ this.width);
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
            /*if (canLocatestone(turn, positionX, positionY)) {
                reverseStones(turn, positionX, positionY);
                resetDirection();
                invalidate();
               // printPositions();
                return true;
            }*/

            for (int i = 0; i < dList.size(); ++i) {
                if(dList.get(i).positionX == positionX && dList.get(i).positionY == positionY) {
                    reverseStones(turn, positionX, positionY, dList.get(i).directions);
                    invalidate();
                    return true;
                }
            }
        }
        return false;
    }

    private int getPositionX(int positionX) {
        for (int i = 0; i < arrLength; ++i)
            if (positionX >= (50 + (i * width)) && positionX <= ((50+ width) + (i * width)))
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

    private void reverseStones(int color, int positionX, int positionY, int [] directions){
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
                    //Log.d(TAG, i+","+j+"탐색");;
                    if(canLocatestone(colorOfStone, j, i)) {
                       // printPositions();
                        //Log.d(TAG, "TRUE : y:" + i + ", x:" + j);
                        Data d = new Data(j, i, directions);
                        d.printData();
                        dList.add(d);
                        resetDirection();
                    }
                }
            }
        }

        if (dList.size() > 0) {
            return true;
        }
        else
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

    public void clearArrayList() {
        dList.clear();
    }

    public void computerTurn(int colorOfStone) {
        int indexX, indexY;
        int x, y;
        int index;

        indexX = dList.get(0).positionX;            // arraylist가 아무것도 없을때를 고려해야함
        indexY = dList.get(0).positionY;
        index = 0;

        for (int i = 1; i < dList.size(); ++i) {
            x = dList.get(i).positionX;
            y = dList.get(i).positionY;
            if (priorities[y][x] < priorities[indexY][indexX]) {
                indexX = x;
                indexY = y;
                index = i;
            }
        }
        reverseStones(colorOfStone, indexX, indexY, dList.get(index).directions);
        invalidate();
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
