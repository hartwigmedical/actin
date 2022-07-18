package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class HasLimitedCumulativeAnthracyclineExposureTest {

    @Test
    public void canEvaluate() {
        HasLimitedCumulativeAnthracyclineExposure function =
                new HasLimitedCumulativeAnthracyclineExposure(TestDoidModelFactory.createMinimalTestDoidModel());

        List<PriorTumorTreatment> priorTumorTreatments = Lists.newArrayList();
        List<PriorSecondPrimary> priorSecondPrimaries = Lists.newArrayList();

        assertEvaluation(EvaluationResult.PASS, function.evaluate(create(null, priorSecondPrimaries, priorTumorTreatments)));

        // Add one generic chemo
        priorTumorTreatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(create(null, priorSecondPrimaries, priorTumorTreatments)));

        // Raise undetermined in case patient has suspicious cancer type
        String firstSuspiciousCancerType = HasLimitedCumulativeAnthracyclineExposure.CANCER_DOIDS_FOR_ANTHRACYCLINE.iterator().next();
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(create(Sets.newHashSet(firstSuspiciousCancerType), priorSecondPrimaries, priorTumorTreatments)));

        // Same when prior tumor is suspicious
        priorSecondPrimaries.add(TreatmentTestFactory.priorSecondPrimaryBuilder().addDoids(firstSuspiciousCancerType).build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(create(null, priorSecondPrimaries, priorTumorTreatments)));

        // Same when prior tumor is suspicious
        priorSecondPrimaries.add(TreatmentTestFactory.priorSecondPrimaryBuilder().addDoids(firstSuspiciousCancerType).build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(create(null, priorSecondPrimaries, priorTumorTreatments)));

        // Also raise undetermined when actual anthracycline is provided.
        priorTumorTreatments.add(TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .chemoType(HasLimitedCumulativeAnthracyclineExposure.ANTHRACYCLINE_CHEMO_TYPE)
                .build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(create(null, Lists.newArrayList(), priorTumorTreatments)));
    }

    @NotNull
    private static PriorSecondPrimary priorPrimaryWithDoid(@NotNull String doid) {
        return TreatmentTestFactory.priorSecondPrimaryBuilder().addDoids(doid).build();
    }

    @NotNull
    private static PatientRecord create(@Nullable Set<String> primaryTumorDoids, @NotNull List<PriorSecondPrimary> priorSecondPrimaries,
            @NotNull List<PriorTumorTreatment> priorTumorTreatments) {
        PatientRecord base = TestDataFactory.createMinimalTestPatientRecord();
        return ImmutablePatientRecord.builder()
                .from(base)
                .clinical(ImmutableClinicalRecord.builder()
                        .from(base.clinical())
                        .tumor(ImmutableTumorDetails.builder().from(base.clinical().tumor()).doids(primaryTumorDoids).build())
                        .priorTumorTreatments(priorTumorTreatments)
                        .priorSecondPrimaries(priorSecondPrimaries)
                        .build())
                .build();
    }
}