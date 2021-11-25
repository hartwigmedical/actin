package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class SecondMalignancyHasBeenCuredRecentlyTest {

    @Test
    public void canEvaluate() {
        SecondMalignancyHasBeenCuredRecently function = new SecondMalignancyHasBeenCuredRecently();

        List<PriorSecondPrimary> priorSecondPrimaries = Lists.newArrayList();
        assertEquals(Evaluation.PASS, function.evaluate(TreatmentEvaluationTestUtil.withPriorSecondPrimaries(priorSecondPrimaries)));

        PriorSecondPrimary secondPrimaryInactive = ImmutablePriorSecondPrimary.builder()
                .tumorLocation("Skin")
                .tumorSubLocation(Strings.EMPTY)
                .tumorType("Melanoma")
                .tumorSubType(Strings.EMPTY)
                .treatmentHistory(Strings.EMPTY)
                .isActive(false)
                .build();

        priorSecondPrimaries.add(secondPrimaryInactive);
        assertEquals(Evaluation.PASS, function.evaluate(TreatmentEvaluationTestUtil.withPriorSecondPrimaries(priorSecondPrimaries)));

        PriorSecondPrimary secondPrimaryActive = ImmutablePriorSecondPrimary.builder().from(secondPrimaryInactive).isActive(true).build();
        priorSecondPrimaries.add(secondPrimaryActive);

        assertEquals(Evaluation.FAIL, function.evaluate(TreatmentEvaluationTestUtil.withPriorSecondPrimaries(priorSecondPrimaries)));
    }
}