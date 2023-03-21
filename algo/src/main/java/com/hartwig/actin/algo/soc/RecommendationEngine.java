package com.hartwig.actin.algo.soc;

import static java.util.stream.Collectors.groupingBy;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.calendar.ReferenceDateProvider;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.algo.evaluation.EvaluationFunctionFactory;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.treatment.datamodel.Treatment;

public class RecommendationEngine {

    private final DoidModel doidModel;
    private final EvaluationFunctionFactory evaluationFunctionFactory;

    public RecommendationEngine(DoidModel doidModel, ReferenceDateProvider referenceDateProvider) {
        this.doidModel = doidModel;
        this.evaluationFunctionFactory = EvaluationFunctionFactory.create(doidModel, referenceDateProvider);
    }

    public Stream<Treatment> determineAvailableTreatments(PatientRecord patientRecord, Stream<Treatment> treatments) {
        Set<String> expandedTumorDoids = Optional.ofNullable(patientRecord.clinical().tumor().doids())
                .map(doids -> doids.stream()
                        .flatMap(doid -> doidModel.doidWithParents(doid).stream())
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());

        if (!expandedTumorDoids.contains(DoidConstants.COLORECTAL_CANCER_DOID)) {
            throw new IllegalArgumentException("No colorectal cancer reported in patient clinical record. SOC recommendation not supported");
        }

        return treatments.filter(treatment -> treatment.lines().contains(determineTreatmentLineForPatient(patientRecord)))
                .filter(treatment -> treatment.eligibilityFunctions().stream()
                        .map(eligibilityFunction -> evaluationFunctionFactory.create(eligibilityFunction).evaluate(patientRecord))
                        .noneMatch(evaluation -> evaluation.result() == EvaluationResult.FAIL)
                )
                .filter(treatment -> treatment.score() >= 0).sorted(Comparator.comparing(Treatment::score).reversed());
    }

    public Map<Integer, List<Treatment>> availableTreatmentsByScore(PatientRecord patientRecord, Stream<Treatment> treatments) {
        return determineAvailableTreatments(patientRecord, treatments).collect(groupingBy(Treatment::score));
    }

    public int determineTreatmentLineForPatient(PatientRecord patientRecord) {
        List<PriorTumorTreatment> priorTumorTreatments = patientRecord.clinical().priorTumorTreatments();
        if (priorTumorTreatments.stream().anyMatch(prior -> prior.categories().contains(TreatmentCategory.CHEMOTHERAPY)
                || prior.categories().contains(TreatmentCategory.IMMUNOTHERAPY))) {
            return priorTumorTreatments.stream().anyMatch(prior -> prior.categories().contains(TreatmentCategory.TARGETED_THERAPY)) ? 3 : 2;
        } else {
            return 1;
        }
    }

    public Boolean patientHasExhaustedStandardOfCare(PatientRecord patientRecord, Stream<Treatment> treatments) {
        return determineAvailableTreatments(patientRecord, treatments).allMatch(Treatment::isOptional);
    }
}
