package com.hartwig.actin.clinical.datamodel;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SexTest {

    @Test
    public void canParseSex() {
        assertEquals(Sex.MALE, Sex.parseSex("Male"));
        assertEquals(Sex.FEMALE, Sex.parseSex("Female"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionOnInvalidSex() {
        Sex.parseSex("no sex");
    }
}