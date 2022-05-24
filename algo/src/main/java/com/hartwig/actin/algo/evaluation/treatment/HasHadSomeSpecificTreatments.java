package com.hartwig.actin.algo.evaluation.treatment;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFactory;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.algo.evaluation.util.Format;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HasHadSomeSpecificTreatments implements EvaluationFunction {

    @NotNull
    private final Set<String> names;
    @Nullable
    private final TreatmentCategory warnCategory;
    private final int minTreatmentLines;

    HasHadSomeSpecificTreatments(@NotNull final Set<String> names, @Nullable final TreatmentCategory warnCategory,
            final int minTreatmentLines) {
        this.names = names;
        this.warnCategory = warnCategory;
        this.minTreatmentLines = minTreatmentLines;
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        List<String> matchTreatments = Lists.newArrayList();
        List<String> warnTreatments = Lists.newArrayList();
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (warnCategory != null && treatment.categories().contains(warnCategory)) {
                warnTreatments.add(treatment.name());
            }

            for (String name : names) {
                if (treatment.name().toLowerCase().contains(name.toLowerCase())) {
                    matchTreatments.add(treatment.name());
                }
            }
        }

        if (matchTreatments.size() >= minTreatmentLines) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages(
                            "Patient has received " + Format.concat(matchTreatments) + " " + matchTreatments.size() + " times")
                    .addPassGeneralMessages("Nr of specific treatments")
                    .build();
        } else if (warnTreatments.size() >= minTreatmentLines) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                            "Patient has received " + warnCategory.display() + " treatment " + warnTreatments.size() + " times")
                    .addUndeterminedGeneralMessages("Nr of specific treatments")
                    .build();
        } else {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages(
                            "Patient has not received " + Format.concat(names) + " at least " + minTreatmentLines + " times")
                    .addFailGeneralMessages("Nr of specific treatments")
                    .build();
        }
    }
}
