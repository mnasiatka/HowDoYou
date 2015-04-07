package com.ythogh.howdoyou;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Widget that lets users select a minimum and maximum value on a given numerical range. The range value types can be one of Long, Double, Integer, Float, Short, Byte or BigDecimal.<br />
 * <br />
 * Improved {@link android.view.MotionEvent} handling for smoother use, anti-aliased painting for improved aesthetics.
 *
 * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
 * @author Peter Sinnott (psinnott@gmail.com)
 * @author Thomas Barrasso (tbarrasso@sevenplusandroid.org)
 *
 * @param <T>
 *            The Number type of the range values. One of Long, Double, Integer, Float, Short, Byte or BigDecimal.
 */
public class SlidingProgression<T extends Number> extends ImageView {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //private final Bitmap thumbImage = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_normal_a);
    //private final Bitmap thumbPressedImage = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_pressed_a);
    private final Bitmap thumbImage = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_normal);
    private final Bitmap thumbPressedImage = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_pressed);
    private final float thumbWidth = thumbImage.getWidth();
    private final float thumbHalfWidth = 0.5f * thumbWidth;
    private final float thumbHalfHeight = 0.5f * thumbImage.getHeight();
    private final float lineHeight = 0.3f * thumbHalfHeight;
    private final float padding = thumbHalfWidth;
    private double normalizedMinValue = 0d;
    private double normalizedMaxValue = 1d;
    private Thumb pressedThumb = null;
    private boolean notifyWhileDragging = false;
    private OnSlidingProgressionChangeListener<T> listener;
    private ArrayList<Thumb> mThumbs;
    private static int PRESSED_ID = -1;
    private static int NUM_ROWS = 5;
    private static float PRESSED_ROW = -1;
    private static float intervals[] = new float[NUM_ROWS];
    private static int rowA = -1, rowB = -1;


    public static final int DEFAULT_COLOR = Color.argb(0xFF, 0x33, 0xB5, 0xE5);
    public static final int INVALID_POINTER_ID = 255;
    public static final int ACTION_POINTER_UP = 0x6, ACTION_POINTER_INDEX_MASK = 0x0000ff00, ACTION_POINTER_INDEX_SHIFT = 8;

    private float mDownMotionX, mDownMotionY;
    private int mActivePointerId = INVALID_POINTER_ID;

    float mTouchProgressOffset;

    private int mScaledTouchSlop;
    private boolean mIsDragging;

    public SlidingProgression(ArrayList<Bitmap> arr, Context context) throws IllegalArgumentException {
        super(context);
        mThumbs = new ArrayList<Thumb>();
        mThumbs.add(new Thumb());

        setFocusable(true);
        setFocusableInTouchMode(true);
        init();
    }

    public SlidingProgression(ArrayList<Bitmap> arr, Bitmap on, Bitmap off, Context context) throws IllegalArgumentException {
        super(context);
        mThumbs = new ArrayList<Thumb>();
        mThumbs.add(new Thumb(on, off));

        setFocusable(true);
        setFocusableInTouchMode(true);
        init();
    }

    private final void init() {
        mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        for (int i = 0; i < NUM_ROWS; i++) {
            intervals[i] = 1f / (NUM_ROWS - 1) * (float) i;
            System.out.println(i + " - " + intervals[i]);
        }
    }

    public void addThumb() {
        mThumbs.add(new Thumb());
        invalidate();
    }

    public void addThumb(Bitmap on, Bitmap off) {
        mThumbs.add(new Thumb(on, off));
        invalidate();
    }

    public void addThumb(Bitmap bmp) {
        mThumbs.add(new Thumb(bmp));
        invalidate();
    }

    public Thumb removeThumb() {
        return removeThumb(mThumbs.size() - 1);
    }

    public Thumb removeThumb(int i) {
        if (mThumbs.size() == 0) {
            return null;
        }
        Thumb t = mThumbs.remove(i);
        invalidate();
        return t;
    }

    public boolean isNotifyWhileDragging() {
        return notifyWhileDragging;
    }

    /**
     * Should the widget notify the listener callback while the user is still dragging a thumb? Default is false.
     *
     * @param flag
     */
    public void setNotifyWhileDragging(boolean flag) {
        this.notifyWhileDragging = flag;
    }

    /**
     * Registers given listener callback to notify about changed selected values.
     *
     * @param listener The listener to notify about changed selected values.
     */
    public void setOnSlidingProgressionChangeListener(OnSlidingProgressionChangeListener<T> listener) {
        this.listener = listener;
    }

    /**
     * Handles thumb selection and movement. Notifies listener callback on certain events.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!isEnabled())
            return false;

        int pointerIndex;

        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                // Remember where the motion event started
                mActivePointerId = event.getPointerId(event.getPointerCount() - 1);
                pointerIndex = event.findPointerIndex(mActivePointerId);
                mDownMotionX = event.getX(pointerIndex);
                mDownMotionY = event.getY(pointerIndex);

                //pressedThumb = evalPressedThumb(mDownMotionX);
                pressedThumb = evalPressedThumb(mDownMotionX, mDownMotionY);
                // Only handle thumb presses.
                if (pressedThumb == null)
                    return super.onTouchEvent(event);

                pressedThumb.isPressed = true;
                setPressed(true);
                invalidate();
                onStartTrackingTouch();
                trackTouchEvent(event);
                attemptClaimDrag();

                break;
            case MotionEvent.ACTION_MOVE:
                if (pressedThumb != null) {

                    if (mIsDragging) {
                        trackTouchEvent(event);
                    } else {
                        // Scroll to follow the motion event
                        pointerIndex = event.findPointerIndex(mActivePointerId);
                        final float x = event.getX(pointerIndex);
                        final float y = event.getY(pointerIndex);

                        if ((Math.abs(x - mDownMotionX) > mScaledTouchSlop) && (Math.abs(y - mDownMotionY) > mScaledTouchSlop)) {
                            setPressed(true);
                            pressedThumb.isPressed = true;
                            invalidate();
                            onStartTrackingTouch();
                            trackTouchEvent(event);
                            attemptClaimDrag();
                        }
                    }

                    if (notifyWhileDragging && listener != null) {
                        listener.onSlidingProgressionValuesChanged(this, 0, 100);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsDragging) {
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                    setPressed(false);
                    pressedThumb.isPressed = false;
                    PRESSED_ID = -1;
                } else {
                    // Touch up when we never crossed the touch slop threshold
                    // should be interpreted as a tap-seek to that location.
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                }

                pressedThumb = null;
                invalidate();
                if (listener != null) {
                    listener.onSlidingProgressionValuesChanged(this, 0, 100);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = event.getPointerCount() - 1;
                // final int index = ev.getActionIndex();
                mDownMotionX = event.getX(index);
                mDownMotionY = event.getY(index);
                mActivePointerId = event.getPointerId(index);
                invalidate();
                break;
            }
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsDragging) {
                    onStopTrackingTouch();
                    setPressed(false);
                    if (pressedThumb != null) {
                        pressedThumb.isPressed = false;
                        PRESSED_ID = -1;
                    }
                }
                invalidate(); // see above explanation
                break;
        }
        return true;
    }

    private final void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & ACTION_POINTER_INDEX_MASK) >> ACTION_POINTER_INDEX_SHIFT;

        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose
            // a new active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mDownMotionX = ev.getX(newPointerIndex);
            mDownMotionY = ev.getY(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    private final void trackTouchEvent(MotionEvent event) {
        final int pointerIndex = event.findPointerIndex(mActivePointerId);
        final float x = event.getX(pointerIndex);
        final float y = event.getY(pointerIndex);

        // check if can move vertically or horizontally
        System.out.println(screenToNormalizedY(y) + " - " + pressedThumb.normalizedValueY);
        System.out.println("Moving down: " + (screenToNormalizedY(y) > pressedThumb.normalizedValueY));
        System.out.println("Moving up: " + (screenToNormalizedY(y) < pressedThumb.normalizedValueY));
        System.out.println("row: " + PRESSED_ROW);
        System.out.println("*********************");

        if (canMoveHorizontally(x, y)) {
            setNormalizedValueX(screenToNormalizedX(x), PRESSED_ID);
        }

        // moving up
        if ((screenToNormalizedY(y) < pressedThumb.normalizedValueY) && canMoveVerticallyUp(x, y)) {
            setNormalizedValueY(screenToNormalizedY(y), PRESSED_ID);
        }

        // moving down
        if ((screenToNormalizedY(y) > pressedThumb.normalizedValueY) && canMoveVerticallyDown(x, y)) {
            setNormalizedValueY(screenToNormalizedY(y), PRESSED_ID);
        }
    }

    private boolean canMoveHorizontally(float x, float y) {
        for (int i = 0; i < intervals.length; i++) {
            if (Math.abs(mThumbs.get(PRESSED_ID).normalizedValueY - intervals[i]) < .01) {
                PRESSED_ROW = i;
                System.out.println(i + "-" + intervals[i]);
                return true;
            }
        }
        PRESSED_ROW = -1;
        return false;
    }

    private boolean canMoveVertically(float x, float y) {
        System.out.println("PRESSED_ROW=" + PRESSED_ROW);
        if (canMoveVerticallyUp(x, y) || canMoveVerticallyDown(x, y)) {
            return true;
        }
        return false;
    }

    private boolean canMoveVerticallyUp(float x, float y) {
        System.out.println("up, normalized x: " + mThumbs.get(PRESSED_ID).normalizedValueX);
        if (((PRESSED_ROW & 1) == 1) && mThumbs.get(PRESSED_ID).normalizedValueX >= .99) { // if it's an odd row, must be on right side to move up
            return true;
        }
        if (((PRESSED_ROW & 1) == 0) && mThumbs.get(PRESSED_ID).normalizedValueX <= .01) { // if it's an even row, must be on left side to move up
            return true;
        }
        return false;
    }

    private boolean canMoveVerticallyDown(float x, float y) {
        System.out.println("down, normalized x: " + mThumbs.get(PRESSED_ID).normalizedValueX);
        if (((PRESSED_ROW & 1) == 1) && mThumbs.get(PRESSED_ID).normalizedValueX <= .01) { // if it's an odd row, must be on left side to move down
            return true;
        }
        if (((PRESSED_ROW & 1) == 0) && mThumbs.get(PRESSED_ID).normalizedValueX >= .99) { // if it's an even row, must be on right side to move down
            return true;
        }
        return false;
    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any ancestors from stealing events in the drag.
     */
    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    /**
     * This is called when the user has started touching this widget.
     */
    void onStartTrackingTouch() {
        mIsDragging = true;
    }

    /**
     * This is called when the user either releases his touch or the touch is canceled.
     */
    void onStopTrackingTouch() {
        mIsDragging = false;
    }

    /**
     * Ensures correct size of the widget.
     */
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 200;
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }

        int height = thumbImage.getHeight();
        if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
            height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
        }
        System.out.println("height:" + (height * NUM_ROWS));
        setMeasuredDimension(width, height * NUM_ROWS);

    }

    /**
     * Draws the widget on the given canvas.
     */
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setStyle(Style.FILL);
        paint.setColor(Color.GRAY);
        paint.setAntiAlias(true);

        // draw seek bar background line
        RectF rect;

        //System.out.println("*******************************");
        // horizontal lines
        for (int i = 0; i < NUM_ROWS; i++) {
            float j = (float) (NUM_ROWS - 2) * (i + 1); // correct spacing
            float k = (float) Math.sqrt(NUM_ROWS) * (i + 1); // correct line height
            rect = new RectF(
                    padding,
                    .5f * ((i + 1) * (getHeight() - lineHeight / k)) - (j * thumbHalfHeight) - thumbHalfHeight,
                    getWidth() - padding,
                    .5f * ((i + 1) * (getHeight() + lineHeight / k)) - (j * thumbHalfHeight) - thumbHalfHeight
            );
            /*
             -- NUM_ROWS = 2 --
            i=0
                .5f * (1 * (getHeight() - lineHeight / (1.414 * 1))) - ((0 * 1) * thumbHalfHeight) - thumbHalfHeight
                .5f * (1 * (getHeight() + lineHeight / (1.414 * 1))) - ((0 * 1) * thumbHalfHeight) - thumbHalfHeight
            i=1
                .5f * (2 * (getHeight() - lineHeight / (1.414 * 2))) - ((0 * 2) * thumbHalfHeight) - thumbHalfHeight
                .5f * (2 * (getHeight() + lineHeight / (1.414 * 2))) - ((0 * 2) * thumbHalfHeight) - thumbHalfHeight

            -- NUM_ROWS = 3 --
            i=0
                .5f * (1 * (getHeight() - lineHeight / (1.732 * 1))) - ((1 * 1) * thumbHalfHeight) - thumbHalfHeight
                .5f * (1 * (getHeight() + lineHeight / (1.732 1))) - ((1 * 1) * thumbHalfHeight) - thumbHalfHeight
            i=1
                .5f * (2 * (getHeight() - lineHeight / (1.732 * 2))) - ((1 * 2) * thumbHalfHeight) - thumbHalfHeight
                .5f * (2 * (getHeight() + lineHeight / (1.732 * 2))) - ((1 * 2) * thumbHalfHeight) - thumbHalfHeight
            i=2
                .5f * (3 * (getHeight() - lineHeight / (1.732 * 3))) - ((1 * 3) * thumbHalfHeight) - thumbHalfHeight
                .5f * (3 * (getHeight() + lineHeight / (1.732 * 3))) - ((1 * 3) * thumbHalfHeight) - thumbHalfHeight

            -- NUM_ROWS = 4 --
            i=0
                .5f * (1 * (getHeight() - lineHeight / (2 * 1))) - ((2 * 1) * thumbHalfHeight) - thumbHalfHeight
                .5f * (1 * (getHeight() + lineHeight / (2 * 1))) - ((2 * 1) * thumbHalfHeight) - thumbHalfHeight
            i=1
                .5f * (2 * (getHeight() - lineHeight / (2 * 2))) - ((2 * 2) * thumbHalfHeight) - thumbHalfHeight
                .5f * (2 * (getHeight() + lineHeight / (2 * 2))) - ((2 * 2) * thumbHalfHeight) - thumbHalfHeight
            i=2
                .5f * (3 * (getHeight() - lineHeight / (2 * 3))) - ((2 * 3) * thumbHalfHeight) - thumbHalfHeight
                .5f * (3 * (getHeight() + lineHeight / (2 * 3))) - ((2 * 3) * thumbHalfHeight) - thumbHalfHeight
            i=3
                .5f * (4 * (getHeight() - lineHeight / (2 * 4))) - ((2 * 4) * thumbHalfHeight) - thumbHalfHeight
                .5f * (4 * (getHeight() + lineHeight / (2 * 4))) - ((2 * 4) * thumbHalfHeight) - thumbHalfHeight
             */
            canvas.drawRect(rect, paint);
        }
        /*for (int i = 0; i < NUM_ROWS; i++) {
            float j = (float) 2 * (i);
            rect = new RectF(
                    padding,
                    .5f * (i * (getHeight() - lineHeight / j)) - (j * thumbHalfHeight) - thumbHalfHeight,
                    getWidth() - padding,
                    .5f * (i * (getHeight() + lineHeight / j)) - (j * thumbHalfHeight) - thumbHalfHeight
            );
            canvas.drawRect(rect, paint);
        }
        */
        // vertical lines
        // right
        //rect = new RectF(getWidth() - padding - lineHeight, 0.5f * (getHeight() + lineHeight/4 - 2 *thumbHalfHeight), getWidth() - padding, 0.5f * (NUM_ROWS * (getHeight() - lineHeight/4) - 2*thumbHalfHeight));
        //canvas.drawRect(rect, paint);

        for (int i = 0; i < mThumbs.size(); i++) {
            drawThumb(normalizedToScreenX(mThumbs.get(i).normalizedValueX), normalizedToScreenY(mThumbs.get(i).normalizedValueY), mThumbs.get(i).isPressed, canvas, i);
        }
    }

    private void drawThumb(float screenCoordX, float screenCoordY, boolean pressed, Canvas canvas, int i) {
        canvas.drawBitmap(pressed ? mThumbs.get(i).thumbOn : mThumbs.get(i).thumbOff, screenCoordX - thumbHalfWidth, screenCoordY - thumbHalfHeight, paint);
    }

    /**
     * Decides which (if any) thumb is touched by the given x, y-coordinate.
     *
     * @param touchX The x-coordinate of a touch event in screen space.
     * @return The pressed thumb or null if none has been touched.
     */
    private Thumb evalPressedThumb(float touchX, float touchY) {
        for (int i = 0; i < mThumbs.size(); i++) {
            if (isInThumbRange(touchX, touchY, mThumbs.get(i).normalizedValueX, mThumbs.get(i).normalizedValueY)) {
                PRESSED_ID = i;
                return mThumbs.get(i);
            }
        }
        return null;
    }

    private boolean isInThumbRange(float touchX, float touchY, double normalizedThumbValueX, double normalizedThumbValueY) {
        return (Math.abs(touchX - normalizedToScreenX(normalizedThumbValueX)) <= thumbHalfWidth) && (Math.abs(touchY - normalizedToScreenY(normalizedThumbValueY)) <= thumbHalfHeight);
    }

    public void setNormalizedValueX(double value, int i) {
        mThumbs.get(i).normalizedValueX = Math.max(0d, Math.min(1d, value));
        invalidate();
    }

    public void setNormalizedValueY(double value, int i) {
        mThumbs.get(i).normalizedValueY = Math.max(0d, Math.min(1d, value));
        invalidate();
    }

    /**
     * Converts a normalized value into screen space.
     *
     * @param normalizedCoord The normalized value to convert.
     * @return The converted value in screen space.
     */
    private float normalizedToScreenX(double normalizedCoord) {
        return (float) (padding + normalizedCoord * (getWidth() - 2 * padding));
    }

    private float normalizedToScreenY(double normalizedCoord) {
        return (float) (padding + normalizedCoord * (getHeight() - 2 * padding));
    }

    /**
     * Converts screen space x-coordinates into normalized values.
     *
     * @param screenCoord The x-coordinate in screen space to convert.
     * @return The normalized value.
     */
    private double screenToNormalizedX(float screenCoord) {
        int width = getWidth();
        if (width <= 2 * padding) {
            // prevent division by zero, simply return 0.
            return 0d;
        } else {
            double result = (screenCoord - padding) / (width - 2 * padding);
            return Math.min(1d, Math.max(0d, result));
        }
    }

    private double screenToNormalizedY(float screenCoord) {
        int height = getHeight();
        if (height <= 2 * padding) {
            // prevent division by zero, simply return 0.
            return 0d;
        } else {
            double result = (screenCoord - padding) / (height - 2 * padding);
            return Math.min(1d, Math.max(0d, result));
        }
    }

    /**
     * Callback listener interface to notify about changed range values.
     *
     * @param <T> The Number type the RangeSeekBar has been declared with.
     * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
     */
    public interface OnSlidingProgressionChangeListener<T> {
        public void onSlidingProgressionValuesChanged(SlidingProgression<?> bar, Integer minValue, Integer maxValue);
    }

    class Thumb implements View.OnClickListener {
        double normalizedValueX, normalizedValueY;
        boolean isPressed;
        String value = "";
        Bitmap thumbOn, thumbOff;

        public Thumb() {
            this(thumbPressedImage, thumbImage);
        }

        public Thumb(Bitmap bmp) {
            this(bmp, bmp);
        }

        public Thumb(Bitmap on, Bitmap off) {
            normalizedValueX = 0;
            normalizedValueY = 0;
            isPressed = false;
            value = "Thumb at " + mThumbs.size();
            thumbOn = on;
            thumbOff = off;
        }

        @Override
        public void onClick(View v) {
            System.out.println(this.value);
        }
    }

}