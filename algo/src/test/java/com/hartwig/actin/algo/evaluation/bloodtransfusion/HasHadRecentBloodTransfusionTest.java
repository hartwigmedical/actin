package com.hartwig.actin.algo.evaluation.bloodtransfusion;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.BloodTransfusion;
import com.hartwig.actin.clinical.datamodel.ImmutableBloodTransfusion;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasHadRecentBloodTransfusionTest {

    private static final LocalDate REFERENCE_DATE = LocalDate.of(2020, 3, 30);

    @Test
    public void canEvaluate() {
        HasHadRecentBloodTransfusion function =
                new HasHadRecentBloodTransfusion(TransfusionProduct.THROMBOCYTE, REFERENCE_DATE.minusWeeks(4));

        List<BloodTransfusion> transfusions = Lists.newArrayList();
        assertEquals(EvaluationResult.FAIL, function.evaluate(withBloodTransfusions(transfusions)));

        transfusions.add(ImmutableBloodTransfusion.builder()
                .product(TransfusionProduct.THROMBOCYTE.display())
                .date(REFERENCE_DATE.minusWeeks(8))
                .build());
        assertEquals(EvaluationResult.FAIL, function.evaluate(withBloodTransfusions(transfusions)));

        transfusions.add(ImmutableBloodTransfusion.builder()
                .product(TransfusionProduct.ERYTHROCYTE.display())
                .date(REFERENCE_DATE.minusWeeks(2))
                .build());
        assertEquals(EvaluationResult.FAIL, function.evaluate(withBloodTransfusions(transfusions)));

        transfusions.add(ImmutableBloodTransfusion.builder()
                .product(TransfusionProduct.THROMBOCYTE.display())
                .date(REFERENCE_DATE.minusWeeks(2))
                .build());
        assertEquals(EvaluationResult.PASS, function.evaluate(withBloodTransfusions(transfusions)));
    }

    @NotNull
    private static PatientRecord withBloodTransfusions(@NotNull List<BloodTransfusion> transfusions) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .bloodTransfusions(transfusions)
                        .build())
                .build();
    }
}