package com.rprescott.fileprocessor.events;

import java.io.File;

public class FileCompletedProcessingEvent {

    private File file;
    private long recordsRead;

    public FileCompletedProcessingEvent(File inputFile, long recordsRead) {
        this.file = inputFile;
        this.recordsRead = recordsRead;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public long getRecordsRead() {
        return recordsRead;
    }

    public void setRecordsRead(long recordsRead) {
        this.recordsRead = recordsRead;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((file == null) ? 0 : file.hashCode());
        result = prime * result + (int) (recordsRead ^ (recordsRead >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FileCompletedProcessingEvent other = (FileCompletedProcessingEvent) obj;
        if (file == null) {
            if (other.file != null)
                return false;
        } else if (!file.equals(other.file))
            return false;
        if (recordsRead != other.recordsRead)
            return false;
        return true;
    }

}
