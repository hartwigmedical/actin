package com.hartwig.actin.algo.evaluation.bloodtransfusion;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.BloodTransfusion;
import com.hartwig.actin.clinical.datamodel.ImmutableBloodTransfusion;

import org.junit.Test;

public class HasHadRecentBloodTransfusionTest {

    @Test
    public void canEvaluate() {
        LocalDate minDate = LocalDate.of(2020, 3, 30);
        HasHadRecentBloodTransfusion function = new HasHadRecentBloodTransfusion(TransfusionProduct.THROMBOCYTE, minDate);

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(BloodTransfusionTestFactory.withBloodTransfusions(Lists.newArrayList())));

        BloodTransfusion tooOld =
                ImmutableBloodTransfusion.builder().product(TransfusionProduct.THROMBOCYTE.display()).date(minDate.minusWeeks(4)).build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(BloodTransfusionTestFactory.withBloodTransfusion(tooOld)));

        BloodTransfusion wrongProduct =
                ImmutableBloodTransfusion.builder().product(TransfusionProduct.ERYTHROCYTE.display()).date(minDate.plusWeeks(2)).build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(BloodTransfusionTestFactory.withBloodTransfusion(wrongProduct)));

        BloodTransfusion correct =
                ImmutableBloodTransfusion.builder().product(TransfusionProduct.THROMBOCYTE.display()).date(minDate.plusWeeks(2)).build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(BloodTransfusionTestFactory.withBloodTransfusion(correct)));
    }
}