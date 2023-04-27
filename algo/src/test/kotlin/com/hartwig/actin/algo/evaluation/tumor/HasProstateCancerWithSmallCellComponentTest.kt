package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.Set;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.junit.Test;

public class HasProstateCancerWithSmallCellComponentTest {

    @Test
    public void canEvaluate() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild(DoidConstants.PROSTATE_CANCER_DOID,
                DoidConstants.PROSTATE_SMALL_CELL_CARCINOMA_DOID);
        HasProstateCancerWithSmallCellComponent function = new HasProstateCancerWithSmallCellComponent(doidModel);

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids((Set<String>) null)));

        PatientRecord exact = TumorTestFactory.withDoids(DoidConstants.PROSTATE_SMALL_CELL_CARCINOMA_DOID);
        assertEvaluation(EvaluationResult.PASS, function.evaluate(exact));

        PatientRecord smallCellHistology = TumorTestFactory.withDoidAndDetails(DoidConstants.PROSTATE_CANCER_DOID,
                HasProstateCancerWithSmallCellComponent.SMALL_CELL_DETAILS);
        assertEvaluation(EvaluationResult.PASS, function.evaluate(smallCellHistology));

        PatientRecord warnProstateCancer =
                TumorTestFactory.withDoids(HasProstateCancerWithSmallCellComponent.PROSTATE_WARN_DOID_SETS.iterator().next());
        assertEvaluation(EvaluationResult.WARN, function.evaluate(warnProstateCancer));

        PatientRecord prostateCancer = TumorTestFactory.withDoids(DoidConstants.PROSTATE_CANCER_DOID);
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(prostateCancer));

        PatientRecord somethingElse = TumorTestFactory.withDoids("something else");
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(somethingElse));
    }
}