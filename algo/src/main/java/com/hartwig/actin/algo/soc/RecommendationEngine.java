package com.hartwig.actin.algo.soc;

import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.calendar.ReferenceDateProvider;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.treatment.datamodel.Treatment;

public class RecommendationEngine {

    private final DoidModel doidModel;
    private final EvaluationFunctionFactory evaluationFunctionFactory;

    public RecommendationEngine(final DoidModel doidModel, ReferenceDateProvider referenceDateProvider) {
        this.doidModel = doidModel;
        this.evaluationFunctionFactory = EvaluationFunctionFactory.create(doidModel, referenceDateProvider);
    }

    public Optional<Stream<Treatment>> determineAvailableTreatments(PatientRecord patientRecord, Stream<Treatment> treatments) {
        Set<String> expandedTumorDoids = Optional.ofNullable(patientRecord.clinical().tumor().doids())
                .map(doids -> doids.stream()
                        .flatMap(doid -> doidModel.doidWithParents(doid).stream())
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());

        if (expandedTumorDoids.contains(DoidConstants.COLORECTAL_CANCER_DOID)) {
            return Optional.of(treatments.filter(treatment -> treatment.eligibilityFunctions().stream()
                            .map(eligibilityFunction -> evaluationFunctionFactory.create(eligibilityFunction).evaluate(patientRecord))
                            .noneMatch(evaluation -> evaluation.result() == EvaluationResult.FAIL)
                    )
                    .filter(treatment -> treatment.score() >= 0).sorted(Comparator.comparing(Treatment::score).reversed()));
        }

        return Optional.empty();
    }

    public Optional<Boolean> patientHasExhaustedStandardOfCare(PatientRecord patientRecord, Stream<Treatment> treatments) {
        return determineAvailableTreatments(patientRecord, treatments).map(t -> t.allMatch(Treatment::isOptional));
    }
}
