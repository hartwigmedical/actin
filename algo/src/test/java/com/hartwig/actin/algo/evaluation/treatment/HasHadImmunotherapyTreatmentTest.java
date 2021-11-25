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

public class HasHadImmunotherapyTreatmentTest {

    @Test
    public void canEvaluate() {
        HasHadImmunotherapyTreatment function = new HasHadImmunotherapyTreatment();

        // Empty list
        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        assertEquals(Evaluation.FAIL, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));

        ImmutablePriorTumorTreatment.Builder builder = ImmutablePriorTumorTreatment.builder().name(Strings.EMPTY).isSystemic(true);

        // Add one non-immunotherapy
        priorTumorTreatments.add(builder.categories(Sets.newHashSet(TreatmentCategory.RADIOTHERAPY)).build());
        assertEquals(Evaluation.FAIL, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));

        // Add one immunotherapy
        priorTumorTreatments.add(builder.categories(Sets.newHashSet(TreatmentCategory.IMMUNOTHERAPY)).build());
        assertEquals(Evaluation.PASS, function.evaluate(TreatmentEvaluationTestUtil.withPriorTumorTreatments(priorTumorTreatments)));
    }

}