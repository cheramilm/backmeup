package com.backmeup.task;

import com.backmeup.Utils;
import org.apache.log4j.Logger;

public class Progress {
    static Logger logger = Logger.getLogger(Progress.class);
    long startTime = 0;
    long endTime = 0;
    long totalLength;
    long completedLength;
    long lastCalculateTime = 0;
    long lastCalculateLength = 0;

    float minSpeed = Float.MAX_VALUE;
    float maxSpeed = Float.MIN_VALUE;
    float averageSpeed = 0;
    float currentSpeed =0;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
        lastCalculateTime = startTime;
    }

    public boolean isCompleted() {
        return endTime!=0;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
        averageSpeed = Utils.getSpeedKBS(totalLength, endTime - startTime);
    }

    public long getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }

    public long getCompletedLength() {
        return completedLength;
    }

    public void addCompletedLength(long length) {
        completedLength += length;
        long now = System.currentTimeMillis();
        long lastInterval = now - lastCalculateTime;
        if (now - lastCalculateTime >= 1000) {
            currentSpeed = Utils.getSpeedKBS(completedLength - lastCalculateLength, lastInterval);
            lastCalculateLength = completedLength;
            lastCalculateTime = now;
            if (currentSpeed < minSpeed) {
                minSpeed = currentSpeed;
            }
            if (currentSpeed > maxSpeed) {
                maxSpeed = currentSpeed;
            }
            averageSpeed=Utils.getSpeedKBS(completedLength,now-startTime);
            logger.debug("Speed:"+currentSpeed+"KB/S");
        }
    }

    public void setCompletedLength(long completedLength) {
        this.completedLength = completedLength;
    }

    public float getMinSpeed() {
        return minSpeed;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public float getAverageSpeed() {
        return averageSpeed;
    }

    public float getCompletedPercentage() {
        return ((float)completedLength)/totalLength;
    }

    public int getEstimatedSeconds() {
        return Math.round((totalLength-completedLength)/1024/averageSpeed);
    }

    public int getTotalSeconds() {
        return (int)(endTime-startTime)/1000;
    }

    public float getCurrentSpeed() {
        return currentSpeed;
    }
}
