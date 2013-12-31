package com.backmeup;

import org.junit.Assert;
import org.junit.Test;

public class TestSchema {
    @Test
    public void testSchema() {
        PropertyBasedObject object = new PropertyBasedObject();
        try {
            object.loadFromResource("/test_ftp_schema.properties");
        } catch (ApplicationException e) {
            e.printStackTrace();
            Assert.fail("Validate failed.");
        }
    }
}
