package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class HasHistoryOfSecondMalignancyTest {

    @Test
    public void canEvaluate() {
        HasHistoryOfSecondMalignancy function = new HasHistoryOfSecondMalignancy();

        List<PriorSecondPrimary> priorSecondPrimaries = Lists.newArrayList();
        assertEquals(Evaluation.FAIL, function.evaluate(TreatmentEvaluationTestUtil.withPriorSecondPrimaries(priorSecondPrimaries)));

        priorSecondPrimaries.add(ImmutablePriorSecondPrimary.builder()
                .tumorLocation("Skin")
                .tumorSubLocation(Strings.EMPTY)
                .tumorType("Melanoma")
                .tumorSubType(Strings.EMPTY)
                .treatmentHistory(Strings.EMPTY)
                .isActive(false)
                .build());

        assertEquals(Evaluation.PASS, function.evaluate(TreatmentEvaluationTestUtil.withPriorSecondPrimaries(priorSecondPrimaries)));
    }
}