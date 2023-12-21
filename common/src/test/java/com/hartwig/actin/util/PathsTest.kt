package com.hartwig.actin.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class PathsTest {

    @Test
    public void canAppendFileSeparator() {
        assertTrue(Paths.forceTrailingFileSeparator("hi").endsWith(File.separator));

        String dir = "this" + File.separator + "dir" + File.separator;
        assertEquals(dir, Paths.forceTrailingFileSeparator(dir));
    }
}