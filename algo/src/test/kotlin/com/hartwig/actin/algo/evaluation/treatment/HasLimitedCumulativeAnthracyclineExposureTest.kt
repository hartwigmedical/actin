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

        // PASS when no information relevant to anthracycline is provided.
        assertEvaluation(EvaluationResult.PASS, function.evaluate(create(null, Lists.newArrayList(), Lists.newArrayList())));

        // PASS with one generic chemo for non-suspicious cancer type
        PriorTumorTreatment genericChemo = TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).build();
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(create(Sets.newHashSet("other cancer type"), Lists.newArrayList(), Lists.newArrayList(genericChemo))));

        String firstSuspiciousCancerType = HasLimitedCumulativeAnthracyclineExposure.CANCER_DOIDS_FOR_ANTHRACYCLINE.iterator().next();
        // PASS when pt has prior second primary with different treatment history
        PriorSecondPrimary suspectTumorTypeWithOther =
                TreatmentTestFactory.priorSecondPrimaryBuilder().addDoids(firstSuspiciousCancerType).treatmentHistory("other").build();
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(create(null, Lists.newArrayList(suspectTumorTypeWithOther), Lists.newArrayList())));

        // UNDETERMINED in case the patient had prior second primary with suspicious prior treatment
        String firstSuspiciousTreatment = HasLimitedCumulativeAnthracyclineExposure.PRIOR_PRIMARY_SUSPICIOUS_TREATMENTS.iterator().next();
        PriorSecondPrimary suspectTumorTypeWithSuspectTreatment = TreatmentTestFactory.priorSecondPrimaryBuilder()
                .addDoids(firstSuspiciousCancerType)
                .treatmentHistory(firstSuspiciousTreatment)
                .build();
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(create(null, Lists.newArrayList(suspectTumorTypeWithSuspectTreatment), Lists.newArrayList())));

        // UNDETERMINED in case the patient had prior second primary with no prior treatment recorded
        PriorSecondPrimary suspectTumorTypeWithoutKnownTreatment =
                TreatmentTestFactory.priorSecondPrimaryBuilder().addDoids(firstSuspiciousCancerType).build();
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(create(null, Lists.newArrayList(suspectTumorTypeWithoutKnownTreatment), Lists.newArrayList())));

        // UNDETERMINED when chemo with no type is provided and tumor type is suspicious.
        PriorTumorTreatment priorChemoWithoutType = TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).build();
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(create(Sets.newHashSet(firstSuspiciousCancerType),
                        Lists.newArrayList(),
                        Lists.newArrayList(priorChemoWithoutType))));

        // UNDETERMINED when actual anthracycline is provided regardless of tumor type
        PriorTumorTreatment priorAnthracycline = TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .chemoType(HasLimitedCumulativeAnthracyclineExposure.ANTHRACYCLINE_CHEMO_TYPE)
                .build();
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(create(null, Lists.newArrayList(), Lists.newArrayList(priorAnthracycline))));
    }

    @NotNull
    private static PatientRecord create(@Nullable Set<String> tumorDoids, @NotNull List<PriorSecondPrimary> priorSecondPrimaries,
            @NotNull List<PriorTumorTreatment> priorTumorTreatments) {
        PatientRecord base = TestDataFactory.createMinimalTestPatientRecord();
        return ImmutablePatientRecord.builder()
                .from(base)
                .clinical(ImmutableClinicalRecord.builder()
                        .from(base.clinical())
                        .tumor(ImmutableTumorDetails.builder().from(base.clinical().tumor()).doids(tumorDoids).build())
                        .priorTumorTreatments(priorTumorTreatments)
                        .priorSecondPrimaries(priorSecondPrimaries)
                        .build())
                .build();
    }
}