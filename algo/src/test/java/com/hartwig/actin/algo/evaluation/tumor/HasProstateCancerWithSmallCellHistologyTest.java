package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.doid.TestDoidModelFactory;

import org.junit.Test;

public class HasProstateCancerWithSmallCellHistologyTest {

    @Test
    public void canEvaluate() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneParentChild(HasProstateCancerWithSmallCellHistology.PROSTATE_CANCER_DOID,
                HasProstateCancerWithSmallCellHistology.PROSTATE_SMALL_CELL_CARCINOMA);
        HasProstateCancerWithSmallCellHistology function = new HasProstateCancerWithSmallCellHistology(doidModel);

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids((List<String>) null)));

        PatientRecord exact = TumorTestFactory.withDoids(HasProstateCancerWithSmallCellHistology.PROSTATE_SMALL_CELL_CARCINOMA);
        assertEvaluation(EvaluationResult.PASS, function.evaluate(exact));

        PatientRecord smallCellHistology =
                TumorTestFactory.withDoidAndDetails(HasProstateCancerWithSmallCellHistology.PROSTATE_CANCER_DOID, "small cell");
        assertEvaluation(EvaluationResult.PASS, function.evaluate(smallCellHistology));

        PatientRecord prostateCancer = TumorTestFactory.withDoids(HasProstateCancerWithSmallCellHistology.PROSTATE_CANCER_DOID);
        assertEvaluation(EvaluationResult.WARN, function.evaluate(prostateCancer));

        PatientRecord somethingElse = TumorTestFactory.withDoids("something else");
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(somethingElse));
    }
}