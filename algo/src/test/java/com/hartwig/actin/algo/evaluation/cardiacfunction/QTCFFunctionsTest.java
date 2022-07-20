package com.hartwig.actin.algo.evaluation.cardiacfunction;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class QTCFFunctionsTest {

    @Test
    public void canDetermineIfECGHasQTCF() {
        assertFalse(QTCFFunctions.hasQTCF(null));
        assertFalse(QTCFFunctions.hasQTCF(CardiacFunctionTestFactory.builder().qtcfValue(1).build()));
        assertTrue(QTCFFunctions.hasQTCF(CardiacFunctionTestFactory.builder().qtcfValue(1).qtcfUnit("unit").build()));
    }

    @Test
    public void canDetermineIfUnitIsAsExpected() {
        assertFalse(QTCFFunctions.isExpectedQTCFUnit("some random unit"));
        assertTrue(QTCFFunctions.isExpectedQTCFUnit(QTCFFunctions.EXPECTED_QTCF_UNIT));
    }
}