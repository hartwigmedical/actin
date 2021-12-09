package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.junit.Test;

public class HasSufficientAlbuminTest {

    @Test
    public void canEvaluate() {
        HasSufficientAlbumin function = new HasSufficientAlbumin(3D);

        ImmutableLabValue.Builder albumin = LabTestFactory.forMeasurement(LabMeasurement.ALBUMIN);

        assertEquals(Evaluation.FAIL, function.evaluate(albumin.value(20D).unit(LabUnit.G_PER_L.display()).build()));
        assertEquals(Evaluation.PASS, function.evaluate(albumin.value(20D).unit(LabUnit.G_PER_DL.display()).build()));
        assertEquals(Evaluation.PASS, function.evaluate(albumin.value(40D).unit(LabUnit.G_PER_L.display()).build()));
    }
}