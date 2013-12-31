package com.backmeup.provider;

import java.util.Comparator;

public class PropertyIdComparator implements Comparator<Property> {
    public int compare(Property o1, Property o2) {
        return o1.getIdPosition()-o2.getIdPosition();
    }
}
