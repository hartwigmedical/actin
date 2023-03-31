package com.hartwig.actin.algo.evaluation.treatment;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.DateComparison;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks implements EvaluationFunction {

    static final String PD_LABEL = "PD";

    @NotNull
    private final TreatmentCategory category;
    @NotNull
    private final List<String> types;
    @Nullable
    private final Integer minCycles;
    @Nullable
    private final Integer minWeeks;

    HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(@NotNull final TreatmentCategory category,
            @NotNull final List<String> types, @Nullable final Integer minCycles, @Nullable final Integer minWeeks) {
        this.category = category;
        this.types = types;
        this.minCycles = minCycles;
        this.minWeeks = minWeeks;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        boolean hasHadTreatment = false;
        boolean hasPotentiallyHadTreatment = false;
        boolean hasHadTreatmentWithPDAndCyclesOrWeeks = false;
        boolean hasHadTreatmentWithPDAndUnclearCycles = false;
        boolean hasHadTreatmentWithPDAndUnclearWeeks = false;
        boolean hasHadTreatmentWithUnclearPDStatus = false;
        boolean hasHadTreatmentWithUnclearPDStatusAndUnclearCycles = false;
        boolean hasHadTreatmentWithUnclearPDStatusAndUnclearWeeks = false;

        boolean hasHadTrial = false;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                if (hasValidType(treatment)) {
                    hasHadTreatment = true;
                    String stopReason = treatment.stopReason();
                    String bestResponse = treatment.bestResponse();
                    Integer cycles = treatment.cycles();
                    Optional<Long> weeksOption = DateComparison.minWeeksBetweenDates(treatment.startYear(),
                            treatment.startMonth(),
                            treatment.stopYear(),
                            treatment.stopMonth());

                    if (stopReason != null || bestResponse != null) {
                        boolean meetsMinCycles = minCycles == null || (cycles != null && cycles >= minCycles);
                        boolean meetsMinWeeks = minWeeks == null || weeksOption.map(weeks -> weeks >= minWeeks).orElse(false);

                        if (PD_LABEL.equalsIgnoreCase(stopReason) || PD_LABEL.equalsIgnoreCase(bestResponse)) {
                            if (meetsMinCycles && meetsMinWeeks) {
                                hasHadTreatmentWithPDAndCyclesOrWeeks = true;
                            } else if (minCycles != null && cycles == null) {
                                hasHadTreatmentWithPDAndUnclearCycles = true;
                            } else if (minWeeks != null && weeksOption.isEmpty()) {
                                hasHadTreatmentWithPDAndUnclearWeeks = true;
                            }
                        }
                    } else if (minCycles == null && minWeeks == null) {
                        hasHadTreatmentWithUnclearPDStatus = true;
                    } else if (minCycles != null && cycles == null) {
                        hasHadTreatmentWithUnclearPDStatusAndUnclearCycles = true;
                    } else if (minWeeks != null && weeksOption.isEmpty()) {
                        hasHadTreatmentWithUnclearPDStatusAndUnclearWeeks = true;
                    }
                } else if (!TreatmentTypeResolver.hasTypeConfigured(treatment, category)) {
                    hasPotentiallyHadTreatment = true;
                }
            }

            if (treatment.categories().contains(TreatmentCategory.TRIAL)) {
                hasHadTrial = true;
            }
        }

        if (hasHadTreatmentWithPDAndCyclesOrWeeks) {
            if (minCycles == null && minWeeks == null) {
                return EvaluationFactory.pass(hasTreatmentSpecificMessage(" with PD"), hasTreatmentGeneralMessage(" with PD"));
            } else if (minCycles != null) {
                return EvaluationFactory.pass(hasTreatmentSpecificMessage(" with PD and at least " + minCycles + " cycles"),
                        hasTreatmentGeneralMessage(" with PD and sufficient cycles"));
            } else {
                return EvaluationFactory.pass(hasTreatmentSpecificMessage(" with PD for at least " + minWeeks + " weeks"),
                        hasTreatmentGeneralMessage(" with PD for sufficient weeks"));
            }
        } else if (hasHadTreatmentWithPDAndUnclearCycles) {
            return undetermined(" with PD but unknown nr of cycles");
        } else if (hasHadTreatmentWithPDAndUnclearWeeks) {
            return undetermined(" with PD but unknown nr of weeks");
        } else if (hasHadTreatmentWithUnclearPDStatus) {
            return undetermined(" with unclear PD status");
        } else if (hasHadTreatmentWithUnclearPDStatusAndUnclearCycles) {
            return undetermined(" with unclear PD status & nr of cycles");
        } else if (hasHadTreatmentWithUnclearPDStatusAndUnclearWeeks) {
            return undetermined(" with unclear PD status & nr of weeks");
        } else if (hasPotentiallyHadTreatment || hasHadTrial) {
            return EvaluationFactory.undetermined("Unclear whether patient has received " + treatment(),
                    "Unclear if received " + category.display());
        } else if (hasHadTreatment) {
            return EvaluationFactory.fail("Patient has received " + treatment() + " but not with PD", "No PD after " + category.display());
        } else {
            return EvaluationFactory.fail("No " + treatment() + " treatment with PD", "No " + category.display());
        }

    }

    private String hasTreatmentSpecificMessage(String suffix) {
        return "Patient has received " + treatment() + suffix;
    }

    private String hasTreatmentGeneralMessage(String suffix) {
        return category.display() + suffix;
    }

    private Evaluation undetermined(String suffix) {
        return EvaluationFactory.undetermined(hasTreatmentSpecificMessage(suffix), hasTreatmentGeneralMessage(suffix));
    }

    private boolean hasValidType(@NotNull PriorTumorTreatment treatment) {
        for (String type : types) {
            if (TreatmentTypeResolver.isOfType(treatment, category, type)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    private String treatment() {
        return Format.concat(types) + " " + category.display() + " treatment";
    }
}
