package com.hartwig.actin.algo.soc;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.calendar.ReferenceDateProvider;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory;
import com.hartwig.actin.algo.soc.datamodel.EvaluatedTreatment;
import com.hartwig.actin.algo.soc.datamodel.ImmutableEvaluatedTreatment;
import com.hartwig.actin.algo.soc.datamodel.Treatment;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.doid.DoidModel;

public class RecommendationEngine {

    private final DoidModel doidModel;
    private final EvaluationFunctionFactory evaluationFunctionFactory;

    public RecommendationEngine(DoidModel doidModel, ReferenceDateProvider referenceDateProvider) {
        this.doidModel = doidModel;
        this.evaluationFunctionFactory = EvaluationFunctionFactory.create(doidModel, referenceDateProvider);
    }

    public Stream<EvaluatedTreatment> determineAvailableTreatments(PatientRecord patientRecord, Stream<Treatment> treatments) {
        Set<String> expandedTumorDoids = Optional.ofNullable(patientRecord.clinical().tumor().doids())
                .map(doids -> doids.stream().flatMap(doid -> doidModel.doidWithParents(doid).stream()).collect(Collectors.toSet()))
                .orElse(Collections.emptySet());

        if (!expandedTumorDoids.contains(DoidConstants.COLORECTAL_CANCER_DOID)) {
            throw new IllegalArgumentException("No colorectal cancer reported in patient clinical record. SOC recommendation not supported.");
        } else if (Stream.of("5777", "169", "1800").anyMatch(expandedTumorDoids::contains)) {
            throw new IllegalArgumentException("SOC recommendation only supported for colorectal carcinoma");
        }

        return treatments.filter(treatment -> treatment.lines().contains(determineTreatmentLineForPatient(patientRecord)))
                .map(treatment -> {
                    List<Evaluation> evaluations = treatment.eligibilityFunctions()
                            .stream()
                            .map(eligibilityFunction -> evaluationFunctionFactory.create(eligibilityFunction).evaluate(patientRecord))
                            .collect(Collectors.toList());
                    return (EvaluatedTreatment) ImmutableEvaluatedTreatment.builder()
                            .treatment(treatment)
                            .evaluations(evaluations)
                            .score(treatment.score())
                            .build();
                })
                .filter(evaluatedTreatment -> evaluatedTreatment.evaluations()
                        .stream()
                        .noneMatch(evaluation -> evaluation.result() == EvaluationResult.FAIL && !evaluation.recoverable()))
                .filter(evaluatedTreatment -> evaluatedTreatment.score() >= 0)
                .sorted(Comparator.comparing(EvaluatedTreatment::score).reversed());
    }

    public EvaluatedTreatmentInterpreter provideRecommendations(PatientRecord patientRecord, Stream<Treatment> treatments) {
        return new EvaluatedTreatmentInterpreter(determineAvailableTreatments(patientRecord, treatments).collect(Collectors.toList()));
    }

    private int determineTreatmentLineForPatient(PatientRecord patientRecord) {
        List<PriorTumorTreatment> priorTumorTreatments = patientRecord.clinical().priorTumorTreatments();
        if (priorTumorTreatments.stream()
                .anyMatch(prior -> prior.categories().contains(TreatmentCategory.CHEMOTHERAPY) || prior.categories()
                        .contains(TreatmentCategory.IMMUNOTHERAPY))) {
            return priorTumorTreatments.stream().anyMatch(prior -> prior.categories().contains(TreatmentCategory.TARGETED_THERAPY)) ? 3 : 2;
        } else {
            return 1;
        }
    }

    public Boolean patientHasExhaustedStandardOfCare(PatientRecord patientRecord, Stream<Treatment> treatments) {
        return determineAvailableTreatments(patientRecord, treatments).allMatch(evaluatedTreatment -> evaluatedTreatment.treatment()
                .isOptional());
    }
}
