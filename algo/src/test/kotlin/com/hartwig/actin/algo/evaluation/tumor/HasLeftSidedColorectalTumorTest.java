package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.stream.Stream;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class HasLeftSidedColorectalTumorTest {

    @Test
    public void shouldReturnUndeterminedWhenNoTumorDoidsConfigured() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function().evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }

    @Test
    public void shouldFailWhenTumorIsNotColorectal() {
        assertEvaluation(EvaluationResult.FAIL, function().evaluate(TumorTestFactory.withDoids(DoidConstants.PROSTATE_CANCER_DOID)));
    }

    @Test
    public void shouldReturnUndeterminedWhenTumorSubLocationIsUnknownOrMissing() {
        Stream.of(null, "", "unknown")
                .forEach(subLocation -> assertEvaluation(EvaluationResult.UNDETERMINED,
                        function().evaluate(patientWithTumorSubLocation(subLocation))));
    }

    @Test
    public void shouldPassWhenLeftTumorSubLocationProvided() {
        Stream.of("Rectum", "Descending Colon", "COLON sigmoid", "colon descendens", "rectosigmoid", "Colon sigmoideum")
                .forEach(subLocation -> assertEvaluation(EvaluationResult.PASS,
                        function().evaluate(patientWithTumorSubLocation(subLocation))));
    }

    @Test
    public void shouldFailWhenRightTumorSubLocationProvided() {
        Stream.of("Ascending colon",
                        "Colon ascendens",
                        "caecum",
                        "cecum",
                        "transverse COLON",
                        "colon transversum",
                        "flexura hepatica",
                        "hepatic flexure")
                .forEach(subLocation -> assertEvaluation(EvaluationResult.FAIL,
                        function().evaluate(patientWithTumorSubLocation(subLocation))));
    }

    private static PatientRecord patientWithTumorSubLocation(@Nullable String subLocation) {
        return TumorTestFactory.withDoidAndSubLocation(DoidConstants.COLORECTAL_CANCER_DOID, subLocation);
    }

    private static HasLeftSidedColorectalTumor function() {
        DoidModel doidModel = TestDoidModelFactory.createWithOneDoidAndTerm(DoidConstants.COLORECTAL_CANCER_DOID, "colorectal cancer");
        return new HasLeftSidedColorectalTumor(doidModel);
    }
}