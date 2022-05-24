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

public class HasHadLimitedSpecificTreatments implements EvaluationFunction {

    @NotNull
    private final Set<String> names;
    @Nullable
    private final TreatmentCategory warnCategory;
    private final int maxTreatmentLines;

    HasHadLimitedSpecificTreatments(@NotNull final Set<String> names, @Nullable final TreatmentCategory warnCategory,
            final int maxTreatmentLines) {
        this.names = names;
        this.warnCategory = warnCategory;
        this.maxTreatmentLines = maxTreatmentLines;
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

        if (matchTreatments.size() > maxTreatmentLines) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.FAIL)
                    .addFailSpecificMessages(
                            "Patient has received " + Format.concat(matchTreatments) + " " + matchTreatments.size() + " times")
                    .addFailGeneralMessages("Nr of specific treatments")
                    .build();
        } else if (warnTreatments.size() > maxTreatmentLines) {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.UNDETERMINED)
                    .addUndeterminedSpecificMessages(
                            "Patient has received " + warnCategory.display() + " treatment " + warnTreatments.size() + " times")
                    .addUndeterminedGeneralMessages("Nr of specific treatments")
                    .build();
        } else {
            return EvaluationFactory.unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Patient has received " + Format.concat(names) + " less than " + maxTreatmentLines + " times")
                    .addPassGeneralMessages("Nr of specific treatments")
                    .build();

        }
    }
}
