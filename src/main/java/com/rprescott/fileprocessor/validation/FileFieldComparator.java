package com.rprescott.fileprocessor.validation;

import java.util.Comparator;

public class FileFieldComparator implements Comparator<FileField> {
    @Override
    public int compare(FileField o1, FileField o2) {
        return Integer.compare(o1.getPosition(), o2.getPosition());
    }
}
