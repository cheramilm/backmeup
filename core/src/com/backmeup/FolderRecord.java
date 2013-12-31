package com.backmeup;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FolderRecord implements Serializable {
        private static final long serialVersionUID = 3684121494170921938L;
        private Map<String, FileRecord> files = new HashMap<String, FileRecord>();

        public Map<String, FileRecord> getFiles() {
            return files;
        }

        public void setFiles(Map<String, FileRecord> files) {
            this.files = files;
        }

        public void addFileRecord(String relativeFilePath, FileRecord fileRecord) {
            files.put(relativeFilePath, fileRecord);
        }

        public FileRecord getFileRecord(String relativeFilePath) {
            return files.get(relativeFilePath);
        }
}
