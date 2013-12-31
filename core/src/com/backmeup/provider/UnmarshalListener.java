package com.backmeup.provider;

import javax.xml.bind.Unmarshaller;

public class UnmarshalListener extends Unmarshaller.Listener {
    @Override
    public void afterUnmarshal(Object target, Object parent) {
        if (target instanceof Property) {
            Property property = (Property) target;
            property.init();
        }
    }
}
