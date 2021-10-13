package com.hartwig.actin.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class FileUtilTest {

    @Test
    public void canAppendFileSeparator() {
        assertTrue(FileUtil.appendFileSeparator("hi").endsWith(File.separator));

        String dir = "this" + File.separator + "dir" + File.separator;
        assertEquals(dir, FileUtil.appendFileSeparator(dir));
    }
}