package com.example.damian.digitalclock;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Handler;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Damian on 22/04/2017.
 */
public class MyView extends View {

    private Typeface custom_font;
    private TextPaint textPaintDate = new TextPaint();
    private TextPaint textPaintTime = new TextPaint();
    private TextPaint textPaintInfo = new TextPaint();
    private Paint paint = new Paint();

    private int[] colors = {Color.YELLOW, Color.argb(255,255,165,0), Color.RED, Color.MAGENTA, Color.CYAN,  Color.BLUE,Color.GREEN, Color.WHITE, Color.DKGRAY};
    private int selected_color = 5; // start with blue

    //battery status
    private IntentFilter ifilter;
    private Intent batteryStatus;

    //values
    private String year = "YEAR";
    private String month = "MONTH";
    private String day = "DAY";
    private String hour = "HOUR";
    private String minute = "MINUTE";
    private String second = "SECOND";


    public MyView(Context context) {
        super(context);

        //load the digital font
        custom_font = Typeface.createFromAsset(context.getApplicationContext().getAssets(), "fonts/digital.ttf");

        //used to get battery status
        ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = context.registerReceiver(null, ifilter);

        //timer
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                invalidate(); //invalidate the graphics every 1 second
                //-------------------
                h.postDelayed(this, 1000);
            }
        }, 1000); // 1 second delay (takes millis)

    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:

                if( selected_color < colors.length - 1)
                    selected_color++;
                else
                    selected_color = 0;

        }

        return true;
    }



    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);

        //anti aliasing
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);

        //draw background
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        canvas.drawPaint(paint);

        //get screen size & set widths, heights, others
        int x = getWidth();
        int y = getHeight();
        int center_y = y / 2;
        int battery_bar_width = x / 2;

        //get date & time
        int[] date_time = getDateTime();
        String day_of_week = getDayOfWeek(date_time[3]);

        //show date
        textPaintDate.setTextSize((int) (x / 6.9));
        textPaintDate.setTypeface(custom_font);
        textPaintDate.setColor(colors[selected_color]);

        Rect bounds = new Rect();
        String upper = String.valueOf(date_time[0]) + "   " + String.valueOf(date_time[1] + 1) + "  " + String.valueOf(date_time[2]) + " " + day_of_week;
        textPaintDate.getTextBounds(upper, 0, upper.length(), bounds);
        int x_pos = (x - bounds.width()) / 2;
        canvas.drawText(upper, x_pos, (int) (y / 3.2), textPaintDate);
        int start_pos = x_pos;

        //describe date
        textPaintInfo.setTypeface(Typeface.create("Arial", Typeface.BOLD));
        textPaintInfo.setTextSize(y / 16);
        textPaintInfo.setColor(Color.GRAY);

        canvas.drawText(year, x_pos, (int) (y / 3.2) + (y / 16), textPaintInfo);
        canvas.drawText(month, x_pos + measureString((String.valueOf(date_time[0]) + "o"),textPaintDate).width(),(int) (y / 3.2) + (y / 16),textPaintInfo);
        canvas.drawText(day, x_pos + measureString(String.valueOf(date_time[0]) + "   o" + String.valueOf(date_time[1] + 1)
        + String.valueOf(date_time[2]), textPaintDate).width(), (int) (y / 3.2) + (y / 16), textPaintInfo);

        //show time
        String[] time_str = addLeadingZeros(date_time[4], date_time[5], date_time[6]);

        textPaintTime.setTextSize(x / 4);
        textPaintTime.setTypeface(custom_font);
        textPaintTime.setColor(colors[selected_color]);

        String middle = String.valueOf(time_str[0] + ":" + time_str[1] + ":" + time_str[2]);
        bounds = new Rect();
        textPaintTime.getTextBounds("00:00:00", 0, "00:00:00".length(), bounds);
        //textPaintTime.getTextBounds(middle, 0, middle.length(), bounds); //<- dynamic positioning
        x_pos  = (x - bounds.width()) / 2;
        canvas.drawText(middle, x_pos, center_y + (int) (bounds.height() / 1.2), textPaintTime);

        //describe time
        canvas.drawText(hour, x_pos, center_y + (int) (bounds.height() / 1.2) + (y / 16), textPaintInfo);
        canvas.drawText(minute, x_pos + measureString(String.valueOf(time_str[0]) + "::", textPaintTime).width(), center_y + (int) (bounds.height() / 1.2) + (y / 16), textPaintInfo);
        canvas.drawText(second, x_pos + measureString(String.valueOf(time_str[0] + ":" + time_str[1] + ":::"), textPaintTime).width(), center_y + (int) (bounds.height() / 1.2) + (y / 16), textPaintInfo);

        //get battery level
        float battery_per = getBatteryPercentage();
        int battery_level = (int)(battery_per * 100);

        //battery
        int visual_battery_perc = (int)(battery_bar_width * (battery_level / (float)100.0));

        canvas.drawText("BATTERY: ", start_pos, y - (y / 16), textPaintInfo);
        Rect battery_level_rect = new Rect(start_pos + measureString("BATTERY::",textPaintInfo).width(),(y - ((y / 16) * 2)),start_pos + measureString("BATTERY::",textPaintInfo).width() + (x / 2) ,(y - ((y / 16) * 2)) +  (x / 20));

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(colors[selected_color]);
        Rect battery_percentage_visual = new Rect(start_pos + measureString("BATTERY::",textPaintInfo).width(),(y - ((y / 16) * 2)),start_pos + measureString("BATTERY::",textPaintInfo).width() +  visual_battery_perc ,(y - ((y / 16) * 2)) + (x / 20));
        canvas.drawRect(battery_percentage_visual, paint);

        //show battery percentage
        textPaintInfo.setTextSize(y / 16);
        textPaintInfo.setColor(colors[selected_color]);
        canvas.drawText(String.valueOf(battery_level), start_pos + measureString("BATTERY::", textPaintInfo).width() + (int) (x / 1.9), y - (y / 16), textPaintInfo); //420
        textPaintInfo.setColor(Color.GRAY);
        canvas.drawText("%", start_pos + measureString("BATTERY::", textPaintInfo).width() + (int) (x / 1.86) + measureString(String.valueOf(battery_level), textPaintInfo).width(), y - (y / 16), textPaintInfo); // 430
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(y / 120); //4
        canvas.drawRect(battery_level_rect, paint);


    }


    private String[] addLeadingZeros(int hours, int minutes, int seconds){
        String hours_str, minutes_str, seconds_str;

        if(hours <= 9)
            hours_str = "0" + hours;
        else
            hours_str = String.valueOf(hours);

        if(minutes <= 9)
            minutes_str = "0" + minutes;
        else
            minutes_str = String.valueOf(minutes);

        if(seconds <= 9)
            seconds_str = "0" + seconds;
        else
            seconds_str = String.valueOf(seconds);

        String[] values = {hours_str, minutes_str, seconds_str};
        return values;



    }


    private Rect measureString(String text, TextPaint tp){

        Rect bounds = new Rect();
        tp.getTextBounds(text, 0, text.length(), bounds);

        return bounds; // return width & height of string
    }

    private float getBatteryPercentage(){

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float)scale;

        return batteryPct;
    }



    private int[] getDateTime(){

        //get date & time
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int day_of_week = calendar.get(Calendar.DAY_OF_WEEK);
        int ss = calendar.get(Calendar.SECOND);
        int mm = calendar.get(Calendar.MINUTE);
        int hh = calendar.get(Calendar.HOUR_OF_DAY);

        int[] values = {year,month,day,day_of_week, hh, mm, ss };
        return values;

    }

    private String getDayOfWeek(int day_of_week){
        switch(day_of_week){
            case Calendar.MONDAY:
                return "MON";
            case Calendar.TUESDAY:
                return "TUE";
            case Calendar.WEDNESDAY:
                return "WED";
            case Calendar.THURSDAY:
                return "THU";
            case Calendar.FRIDAY:
                return "FRI";
            case Calendar.SATURDAY:
                return "SAT";
            case Calendar.SUNDAY:
                return "SUN";
        }

        return null;

    }

}