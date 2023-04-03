package com.hartwig.actin.algo.evaluation.treatment;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.jetbrains.annotations.NotNull;

class HasHadCombinedTreatmentNamesWithCycles implements EvaluationFunction {

    private static final String GENERAL_FAIL_MESSAGE = "No treatments with cycles";
    private final List<String> treatmentNames;
    private final int minCycles;
    private final int maxCycles;

    HasHadCombinedTreatmentNamesWithCycles(List<String> treatmentNames, int minCycles, int maxCycles) {
        this.treatmentNames = treatmentNames;
        this.minCycles = minCycles;
        this.maxCycles = maxCycles;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        Map<EvaluationResult, List<Evaluation>> evaluationsByResult = treatmentNames.stream()
                .map(treatmentName -> evaluatePriorTreatmentsMatchingName(record.clinical().priorTumorTreatments(), treatmentName))
                .collect(Collectors.groupingBy(Evaluation::result));

        ImmutableEvaluation.Builder builder = EvaluationFactory.unrecoverable();
        if (evaluationsByResult.containsKey(EvaluationResult.FAIL)) {
            List<Evaluation> failEvaluations = evaluationsByResult.get(EvaluationResult.FAIL);
            return builder.result(EvaluationResult.FAIL)
                    .addAllFailSpecificMessages(getMessagesForEvaluations(failEvaluations, Evaluation::failSpecificMessages))
                    .addAllFailGeneralMessages(getMessagesForEvaluations(failEvaluations, Evaluation::failGeneralMessages))
                    .build();
        } else if (evaluationsByResult.containsKey(EvaluationResult.UNDETERMINED)) {
            List<Evaluation> undeterminedEvaluations = evaluationsByResult.get(EvaluationResult.UNDETERMINED);
            return builder.result(EvaluationResult.UNDETERMINED)
                    .addAllUndeterminedSpecificMessages(getMessagesForEvaluations(undeterminedEvaluations,
                            Evaluation::undeterminedSpecificMessages))
                    .addAllUndeterminedGeneralMessages(getMessagesForEvaluations(undeterminedEvaluations,
                            Evaluation::undeterminedGeneralMessages))
                    .build();
        } else if (evaluationsByResult.containsKey(EvaluationResult.PASS) && evaluationsByResult.size() == 1) {
            List<Evaluation> passEvaluations = evaluationsByResult.get(EvaluationResult.PASS);
            return builder.result(EvaluationResult.PASS)
                    .addAllPassSpecificMessages(getMessagesForEvaluations(passEvaluations, Evaluation::passSpecificMessages))
                    .addPassGeneralMessages("Found matching treatments")
                    .build();
        } else {
            throw new IllegalStateException("At least one treatment name should be provided, and all results should be PASS, FAIL, or UNDETERMINED");
        }
    }

    private Evaluation evaluatePriorTreatmentsMatchingName(List<PriorTumorTreatment> priorTumorTreatments, String treatmentName) {
        String query = treatmentName.toLowerCase();
        Map<EvaluationResult, List<PriorTumorTreatment>> matchingPriorTreatments = priorTumorTreatments.stream()
                .filter(prior -> prior.name().toLowerCase().contains(query))
                .collect(Collectors.groupingBy(prior -> Optional.ofNullable(prior.cycles())
                        .map(cycles -> cycles >= minCycles && cycles <= maxCycles ? EvaluationResult.PASS : EvaluationResult.FAIL)
                        .orElse(EvaluationResult.UNDETERMINED)));

        if (matchingPriorTreatments.isEmpty()) {
            return EvaluationFactory.fail("No prior treatments found matching " + treatmentName, GENERAL_FAIL_MESSAGE);
        } else if (matchingPriorTreatments.containsKey(EvaluationResult.PASS)) {
            return EvaluationFactory.pass(
                    "Found matching treatments: " + formatTreatmentList(matchingPriorTreatments.get(EvaluationResult.PASS), true),
                    "Found matching treatments");
        } else if (matchingPriorTreatments.containsKey(EvaluationResult.UNDETERMINED)) {
            return EvaluationFactory.undetermined("Unknown cycles for matching prior treatments: " + formatTreatmentList(
                    matchingPriorTreatments.get(EvaluationResult.UNDETERMINED),
                    false), "Unknown treatment cycles");
        } else {
            return EvaluationFactory.fail(String.format("Matching prior treatments did not have between %d and %d cycles: %s",
                    minCycles,
                    maxCycles,
                    formatTreatmentList(matchingPriorTreatments.get(EvaluationResult.FAIL), true)), GENERAL_FAIL_MESSAGE);
        }
    }

    private static String formatTreatmentList(List<PriorTumorTreatment> treatments, boolean includeCycles) {
        return treatments.stream()
                .map(prior -> prior.name() + (includeCycles ? String.format(" (%d cycles)", prior.cycles()) : ""))
                .collect(Collectors.joining(", "));
    }

    private static Set<String> getMessagesForEvaluations(List<Evaluation> evaluations, Function<Evaluation, Set<String>> messageExtractor) {
        return evaluations.stream().map(messageExtractor).flatMap(Set::stream).collect(Collectors.toSet());
    }
}
