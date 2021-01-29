package com.example.ijkplayerdemo;

import java.util.List;

public class RecordListData {
    private String result;
    private int totals;
    private int endflag;
    private List<ListInfoBean> ListInfo;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getTotals() {
        return totals;
    }

    public void setTotals(int totals) {
        this.totals = totals;
    }

    public int getEndflag() {
        return endflag;
    }

    public void setEndflag(int endflag) {
        this.endflag = endflag;
    }

    public List<ListInfoBean> getListInfo() {
        return ListInfo;
    }

    public void setListInfo(List<ListInfoBean> ListInfo) {
        this.ListInfo = ListInfo;
    }

    public static class ListInfoBean {
        /**
         * startTime : 1607875201
         * recordeDuration : 1201
         * index : 0
         */

        private long startTime;
        private int recordeDuration;
        private int index;

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public int getRecordeDuration() {
            return recordeDuration;
        }

        public void setRecordeDuration(int recordeDuration) {
            this.recordeDuration = recordeDuration;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }
}
