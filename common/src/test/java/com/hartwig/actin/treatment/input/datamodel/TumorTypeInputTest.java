package com.hartwig.actin.treatment.input.datamodel;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TumorTypeInputTest {

    @Test
    public void canConvertAllTumorTypeInputs() {
        for (TumorTypeInput category : TumorTypeInput.values()) {
            assertEquals(category, TumorTypeInput.fromString(category.display()));
        }
    }
}