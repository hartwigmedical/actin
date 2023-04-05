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

import org.jetbrains.annotations.NotNull;

class RecommendationEngine {

    private static final Set<String> EXCLUDED_TUMOR_DOIDS = Set.of("5777", "169", "1800");

    @NotNull
    private final DoidModel doidModel;
    @NotNull
    private final EvaluationFunctionFactory evaluationFunctionFactory;

    private RecommendationEngine(@NotNull DoidModel doidModel, @NotNull EvaluationFunctionFactory evaluationFunctionFactory) {
        this.doidModel = doidModel;
        this.evaluationFunctionFactory = evaluationFunctionFactory;
    }

    @NotNull
    static RecommendationEngine create(@NotNull DoidModel doidModel, @NotNull ReferenceDateProvider referenceDateProvider) {
        return new RecommendationEngine(doidModel, EvaluationFunctionFactory.create(doidModel, referenceDateProvider));
    }

    @NotNull
    Stream<EvaluatedTreatment> determineAvailableTreatments(@NotNull PatientRecord patientRecord, @NotNull Stream<Treatment> treatments) {
        Set<String> expandedTumorDoids = Optional.ofNullable(patientRecord.clinical().tumor().doids())
                .map(doids -> doids.stream().flatMap(doid -> doidModel.doidWithParents(doid).stream()).collect(Collectors.toSet()))
                .orElse(Collections.emptySet());

        if (!expandedTumorDoids.contains(DoidConstants.COLORECTAL_CANCER_DOID)) {
            throw new IllegalArgumentException("No colorectal cancer reported in patient clinical record. SOC recommendation not supported.");
        } else if (!Collections.disjoint(EXCLUDED_TUMOR_DOIDS, expandedTumorDoids)) {
            throw new IllegalArgumentException("SOC recommendation only supported for colorectal carcinoma");
        }

        return treatments.filter(treatment -> treatment.lines().contains(determineTreatmentLineForPatient(patientRecord)))
                .map(treatment -> evaluateTreatmentForPatient(treatment, patientRecord))
                .filter(RecommendationEngine::treatmentHasNoFailedEvaluations)
                .filter(evaluatedTreatment -> evaluatedTreatment.score() >= 0)
                .sorted(Comparator.comparing(EvaluatedTreatment::score).reversed());
    }

    @NotNull
    EvaluatedTreatmentInterpreter provideRecommendations(@NotNull PatientRecord patientRecord, @NotNull Stream<Treatment> treatments) {
        return new EvaluatedTreatmentInterpreter(determineAvailableTreatments(patientRecord, treatments).collect(Collectors.toList()));
    }


    boolean patientHasExhaustedStandardOfCare(PatientRecord patientRecord, Stream<Treatment> treatments) {
        return determineAvailableTreatments(patientRecord, treatments).allMatch(evaluatedTreatment -> evaluatedTreatment.treatment()
                .isOptional());
    }

    private EvaluatedTreatment evaluateTreatmentForPatient(Treatment treatment, PatientRecord patientRecord) {
        List<Evaluation> evaluations = treatment.eligibilityFunctions()
                .stream()
                .map(eligibilityFunction -> evaluationFunctionFactory.create(eligibilityFunction).evaluate(patientRecord))
                .collect(Collectors.toList());
        return ImmutableEvaluatedTreatment.builder()
                .treatment(treatment)
                .evaluations(evaluations)
                .score(treatment.score())
                .build();
    }

    private static boolean treatmentHasNoFailedEvaluations(EvaluatedTreatment evaluatedTreatment) {
        return evaluatedTreatment.evaluations().stream().noneMatch(evaluation -> evaluation.result() == EvaluationResult.FAIL);
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
}
