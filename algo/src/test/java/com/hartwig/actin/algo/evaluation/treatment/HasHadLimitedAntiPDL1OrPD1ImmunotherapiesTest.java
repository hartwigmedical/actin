package com.hartwig.actin.algo.evaluation.treatment;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class HasHadLimitedAntiPDL1OrPD1ImmunotherapiesTest {

    @Test
    public void canEvaluate() {
        HasHadLimitedAntiPDL1OrPD1Immunotherapies function = new HasHadLimitedAntiPDL1OrPD1Immunotherapies(1);

        // Empty list
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        assertEquals(Evaluation.PASS, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));

        ImmutablePriorTumorTreatment.Builder builder = ImmutablePriorTumorTreatment.builder().name(Strings.EMPTY).isSystemic(true);

        // Add one immunotherapy with null type
        priorTumorTreatments.add(builder.categories(Sets.newHashSet(TreatmentCategory.IMMUNOTHERAPY)).build());
        assertEquals(Evaluation.PASS, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));

        // Add one immunotherapy with another type
        priorTumorTreatments.add(builder.categories(Sets.newHashSet(TreatmentCategory.IMMUNOTHERAPY)).immunoType("Anti-CTLA-4").build());
        assertEquals(Evaluation.PASS, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));

        // Add one immunotherapy with PD1 type
        priorTumorTreatments.add(builder.categories(Sets.newHashSet(TreatmentCategory.IMMUNOTHERAPY))
                .immunoType(HasHadLimitedAntiPDL1OrPD1Immunotherapies.PD1_TYPE)
                .build());
        assertEquals(Evaluation.PASS, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));

        // Add one immunotherapy with PDL1 type
        priorTumorTreatments.add(builder.categories(Sets.newHashSet(TreatmentCategory.IMMUNOTHERAPY))
                .immunoType(HasHadLimitedAntiPDL1OrPD1Immunotherapies.PDL1_TYPE)
                .build());
        assertEquals(Evaluation.FAIL, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));
    }
}