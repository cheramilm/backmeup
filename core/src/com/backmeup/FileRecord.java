package com.backmeup;

import java.io.Serializable;
import java.util.Date;

public class FileRecord implements Serializable {
        private static final long serialVersionUID = -1215714280867795408L;
        private Date lastModifyDate;
        private long size;
        private Date lastRecordDate;

        public Date getLastModifyDate() {
            return lastModifyDate;
        }

        public void setLastModifyDate(Date lastModifyDate) {
            this.lastModifyDate = lastModifyDate;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public Date getLastRecordDate() {
            return lastRecordDate;
        }

        public void setLastRecordDate(Date lastRecordDate) {
            this.lastRecordDate = lastRecordDate;
        }
}
