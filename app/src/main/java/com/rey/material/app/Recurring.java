package com.rey.material.app;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Rey on 2/4/2015.
 */
public class Recurring {

    public static final int REPEAT_NONE     = 0;
    public static final int REPEAT_DAILY    = 1;
    public static final int REPEAT_WEEKLY   = 2;
    public static final int REPEAT_MONTHLY  = 3;
    public static final int REPEAT_YEARLY   = 4;

    public static final int END_FOREVER     = 0;
    public static final int END_UNTIL_DATE  = 1;
    public static final int END_FOR_EVENT   = 2;

    public static final int MONTH_SAME_DAY = 0;
    public static final int MONTH_SAME_WEEKDAY = 1;

    private static final int[] WEEKDAY_MASK = {
            0x01,0x02, 0x04, 0x08, 0x10, 0x20, 0x40
    };

    private static final int DAY_TIME = 86400000;

    private long mStartTime;

    private int mRepeatMode;
    private int mPeriod = 1;
    private int mRepeatSetting;

    private int mEndMode;
    private long mEndSetting;

    public Recurring(){}

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(Recurring.class.getSimpleName())
                .append("[mode=");
        switch (mRepeatMode){
            case REPEAT_NONE:
                sb.append("none");
                break;
            case REPEAT_DAILY:
                sb.append("daily")
                        .append("; period=")
                        .append(mPeriod);
                break;
            case REPEAT_WEEKLY:
                sb.append("weekly")
                        .append("; period=")
                        .append(mPeriod)
                        .append("; setting=");

                for(int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++){
                    if(isEnabledWeekday(i))
                        sb.append(i);
                }
                break;
            case REPEAT_MONTHLY:
                sb.append("monthly")
                        .append("; period=")
                        .append(mPeriod)
                        .append("; setting=")
                        .append(getMonthRepeatType() == MONTH_SAME_DAY ? "same_day" : "same_weekday");
                break;
            case REPEAT_YEARLY:
                sb.append("yearly")
                        .append("; period=")
                        .append(mPeriod);
                break;
        }

        if(mRepeatMode != REPEAT_NONE){
            switch (mEndMode){
                case END_FOREVER:
                    sb.append("; end=forever");
                    break;
                case END_UNTIL_DATE:
                    sb.append("; end=until ");
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(mEndSetting);
                    sb.append(cal.get(Calendar.DAY_OF_MONTH))
                            .append('/')
                            .append(cal.get(Calendar.MONTH) + 1)
                            .append('/')
                            .append(cal.get(Calendar.YEAR));
                    break;
                case END_FOR_EVENT:
                    sb.append("; end=for ")
                            .append(mEndSetting)
                            .append(" events");
                    break;
            }
        }

        sb.append(']');
        return sb.toString();
    }

    public void setStartTime(long time){
        mStartTime = time;
    }

    public long getStartTime(){
        return mStartTime;
    }

    /**
     * Set repeat mode of this recurring obj.
     */
    public void setRepeatMode(int mode){
        mRepeatMode = mode;
    }

    public int getRepeatMode(){
        return mRepeatMode;
    }

    /**
     * Set the period of this recurring obj. Depend on repeat mode, the unit can be days, weeks, months or years.
     */
    public void setPeriod(int period){
        mPeriod = period;
    }

    public int getPeriod(){
        return mPeriod;
    }

    public int getRepeatSetting(){
        return mRepeatSetting;
    }

    public void setRepeatSetting(int setting){
        mRepeatSetting = setting;
    }

    public void clearWeekdaySetting(){
        if(mRepeatMode != REPEAT_WEEKLY)
            return;

        mRepeatSetting = 0;
    }

    /**
     * Enable repeat on a dayOfWeek. Only apply it repeat mode is REPEAT_WEEKLY.
     * @param dayOfWeek value of dayOfWeek, take from Calendar obj.
     * @param enable Enable this dayOfWeek or not.
     */
    public void setEnabledWeekday(int dayOfWeek, boolean enable){
        if(mRepeatMode != REPEAT_WEEKLY)
            return;

        if(enable)
            mRepeatSetting = mRepeatSetting | WEEKDAY_MASK[dayOfWeek - 1];
        else
            mRepeatSetting = mRepeatSetting & (~WEEKDAY_MASK[dayOfWeek - 1]);
    }

    public boolean isEnabledWeekday(int weekday){
        if(mRepeatMode != REPEAT_WEEKLY)
            return false;

        return (mRepeatSetting & WEEKDAY_MASK[weekday - 1]) != 0;
    }

    public void setMonthRepeatType(int type){
        if(mRepeatMode != REPEAT_MONTHLY)
            return;

        mRepeatSetting = type;
    }

    public int getMonthRepeatType(){
        return mRepeatSetting;
    }

    public void setEndMode(int mode){
        mEndMode = mode;
    }

    public int getEndMode(){
        return mEndMode;
    }

    public long getEndSetting(){
        return mEndSetting;
    }

    public void setEndSetting(long setting){
        mEndSetting = setting;
    }

    public void setEndDate(long date){
        if(mEndMode != END_UNTIL_DATE)
            return;
        mEndSetting = date;
    }

    public long getEndDate(){
        if(mEndMode != END_UNTIL_DATE)
            return 0;
        return mEndSetting;
    }

    public void setEventNumber(int number){
        if(mEndMode != END_FOR_EVENT)
            return;
        mEndSetting = number;
    }

    public int getEventNumber(){
        if(mEndMode != END_FOR_EVENT)
            return 0;
        return (int)mEndSetting;
    }

    public long getNextEventTime(){
        return getNextEventTime(System.currentTimeMillis());
    }

    public long getNextEventTime(long now){
        if(mStartTime >= now)
            return mStartTime;

        Calendar cal = Calendar.getInstance();

        switch (mRepeatMode){
            case REPEAT_DAILY:
                return getNextDailyEventTime(cal, mStartTime, now);
            case REPEAT_WEEKLY:
                return getNextWeeklyEventTime(cal, mStartTime, now);
            case REPEAT_MONTHLY:
                return getNextMonthlyEventTime(cal, mStartTime, now);
            case REPEAT_YEARLY:
                return getNextYearlyEventTime(cal, mStartTime, now);
            default:
                return 0;
        }
    }

    private long getNextDailyEventTime(Calendar cal, long start, long now){
        long period = mPeriod * DAY_TIME;
        long time = start + ((now - start) / period) * period;

        do{
            if(time >= now)
                return time;
            time += period;
        }
        while(true);
    }

    private static long gotoFirstDayOfWeek(Calendar cal){
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int firstDayOfWeek = cal.getFirstDayOfWeek();
        int shift = dayOfWeek >= firstDayOfWeek ? (dayOfWeek - firstDayOfWeek) : (dayOfWeek + 7 - firstDayOfWeek);

        cal.add(Calendar.DAY_OF_MONTH, -shift);
        return cal.getTimeInMillis();
    }

    private long getNextWeeklyEventTime(Calendar cal, long start, long now){
        if(mRepeatSetting == 0)
            return 0;

        //daily case
        if(mRepeatSetting == 0x7F && mPeriod == 1){
            long period = DAY_TIME;
            long time = start + ((now - start) / period) * period;

            do{
                if(time >= now)
                    return time;
                time += period;
            }
            while(true);
        }

        long period = mPeriod * 7 * DAY_TIME;

        cal.setTimeInMillis(now);
        long nowFirstDayTime = gotoFirstDayOfWeek(cal);

        cal.setTimeInMillis(start);
        long startFirstDayTime = gotoFirstDayOfWeek(cal);

        long time = startFirstDayTime + ((nowFirstDayTime - startFirstDayTime) / period) * period;
        do{
            cal.setTimeInMillis(time);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

            for(int i = 0; i < 7; i++){
                int nextDayOfWeek = dayOfWeek + i;
                if(nextDayOfWeek > Calendar.SATURDAY)
                    nextDayOfWeek = Calendar.SUNDAY;

                if(isEnabledWeekday(nextDayOfWeek)){
                    long nextTime = time + i * DAY_TIME;
                    if(nextTime >= now)
                        return nextTime;
                }
            }

            time += period;
        }
        while(true);
    }

    /**
     * Get the order number of weekday. 0 mean the first, -1 mean the last.
     */
    public static int getWeekDayOrderNum(Calendar cal){
        return cal.get(Calendar.DAY_OF_MONTH) + 7 > cal.getActualMaximum(Calendar.DAY_OF_MONTH) ? - 1 : (cal.get(Calendar.DAY_OF_MONTH) - 1) / 7;
    }

    /**
     * Get the day in month of the current month of Calendar.
     * @param cal
     * @param dayOfWeek The day of week.
     * @param orderNum The order number, 0 mean the first, -1 mean the last.
     * @return The day int month
     */
    private static int getDay(Calendar cal, int dayOfWeek, int orderNum){
        int day = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        cal.set(Calendar.DAY_OF_MONTH, day);
        int lastWeekday = cal.get(Calendar.DAY_OF_WEEK);
        int shift = lastWeekday >= dayOfWeek ? (lastWeekday - dayOfWeek) : (lastWeekday + 7 - dayOfWeek);

        //find last dayOfWeek of this month
        day -= shift;

        if(orderNum < 0)
            return day;

        cal.set(Calendar.DAY_OF_MONTH, day);
        int lastOrderNum = (cal.get(Calendar.DAY_OF_MONTH) - 1) / 7;

        if(orderNum >= lastOrderNum)
            return day;

        return day - (lastOrderNum - orderNum) * 7;
    }

    private long getNextMonthlyEventTime(Calendar cal, long start, long now){
        if(mRepeatSetting == MONTH_SAME_DAY){
            cal.setTimeInMillis(now);
            int nowMonthYear = cal.get(Calendar.MONTH) + cal.get(Calendar.YEAR) * 12;

            cal.setTimeInMillis(start);
            int startMonthYear = cal.get(Calendar.MONTH) + cal.get(Calendar.YEAR) * 12;
            int startDay = cal.get(Calendar.DAY_OF_MONTH);

            int monthYear = startMonthYear + ((nowMonthYear - startMonthYear) / mPeriod) * mPeriod;
            do{
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.YEAR, monthYear / 12);
                cal.set(Calendar.MONTH, monthYear % 12);
                cal.set(Calendar.DAY_OF_MONTH, Math.min(startDay, cal.getActualMaximum(Calendar.DAY_OF_MONTH)));

                if(cal.getTimeInMillis() >= now)
                    return cal.getTimeInMillis();

                monthYear += mPeriod;
            }
            while(true);
        }
        else{
            cal.setTimeInMillis(now);
            int nowMonthYear = cal.get(Calendar.MONTH) + cal.get(Calendar.YEAR) * 12;

            cal.setTimeInMillis(start);
            int startMonthYear = cal.get(Calendar.MONTH) + cal.get(Calendar.YEAR) * 12;
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int orderNum = getWeekDayOrderNum(cal);

            int monthYear = startMonthYear + ((nowMonthYear - startMonthYear) / mPeriod) * mPeriod;
            do{
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.YEAR, monthYear / 12);
                cal.set(Calendar.MONTH, monthYear % 12);
                cal.set(Calendar.DAY_OF_MONTH, getDay(cal, dayOfWeek, orderNum));

                if(cal.getTimeInMillis() >= now)
                    return cal.getTimeInMillis();

                monthYear += mPeriod;
            }
            while(true);
        }
    }

    private long getNextYearlyEventTime(Calendar cal, long start, long now){
        cal.setTimeInMillis(now);
        int nowYear = cal.get(Calendar.YEAR);

        cal.setTimeInMillis(start);
        int startYear = cal.get(Calendar.YEAR);

        int year = startYear + ((nowYear - startYear) / mPeriod) * mPeriod;
        do{
            cal.setTimeInMillis(start);
            cal.set(Calendar.YEAR, year);
            if(cal.getTimeInMillis() >= now)
                return cal.getTimeInMillis();

            year += mPeriod;
        }
        while(true);
    }
}
