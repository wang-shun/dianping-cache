package com.dianping.cache.entity;

/**
 * Created by thunder on 16/1/5.
 */
public class DeamonTask {
    int id;
    int stat;
    int statMin;
    int statMax;
    int type;
    long commitTime;
    long startTime;
    long endTime;
    String description;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStat() {
        return stat;
    }

    public void setStat(int stat) {
        this.stat = stat;
    }

    public int getStatMin() {
        return statMin;
    }

    public void setStatMin(int statMin) {
        this.statMin = statMin;
    }

    public int getStatMax() {
        return statMax;
    }

    public void setStatMax(int statMax) {
        this.statMax = statMax;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getCommitTime() {
        return commitTime;
    }

    public void setCommitTime(long commitTime) {
        this.commitTime = commitTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
