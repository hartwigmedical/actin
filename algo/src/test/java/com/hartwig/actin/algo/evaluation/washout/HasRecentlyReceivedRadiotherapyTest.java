package com.hartwig.actin.algo.evaluation.washout;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasRecentlyReceivedRadiotherapyTest {

    @Test
    public void canEvaluate() {
        int year = 2020;
        int month = 5;
        HasRecentlyReceivedRadiotherapy function = new HasRecentlyReceivedRadiotherapy(year, month);

        // No prior tumor treatments
        assertFalse(function.isPass(withPriorTumorTreatments(Lists.newArrayList())));

        // Wrong category
        assertFalse(function.isPass(withPriorTumorTreatment(builder().addCategories(TreatmentCategory.IMMUNOTHERAPY).build())));

        // Right category but no date
        assertTrue(function.isPass(withPriorTumorTreatment(radiotherapy().build())));

        // Right category but old date
        assertFalse(function.isPass(withPriorTumorTreatment(radiotherapy().year(year - 1).build())));

        // Right category but old month
        assertFalse(function.isPass(withPriorTumorTreatment(radiotherapy().year(year).month(month - 1).build())));

        // Right category and recent year
        assertTrue(function.isPass(withPriorTumorTreatment(radiotherapy().year(year).build())));

        // Right category and recent year and month
        assertTrue(function.isPass(withPriorTumorTreatment(radiotherapy().year(year).month(month).build())));

        assertNotNull(function.passMessage());
        assertNotNull(function.failMessage());
    }

    @NotNull
    private static ImmutablePriorTumorTreatment.Builder radiotherapy() {
        return builder().addCategories(TreatmentCategory.RADIOTHERAPY);
    }

    @NotNull
    private static ImmutablePriorTumorTreatment.Builder builder() {
        return ImmutablePriorTumorTreatment.builder().name(Strings.EMPTY).isSystemic(true);
    }

    @NotNull
    private static PatientRecord withPriorTumorTreatment(@NotNull PriorTumorTreatment treatment) {
        return withPriorTumorTreatments(Lists.newArrayList(treatment));
    }

    @NotNull
    private static PatientRecord withPriorTumorTreatments(@NotNull List<PriorTumorTreatment> treatments) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .priorTumorTreatments(treatments)
                        .build())
                .build();
    }
}