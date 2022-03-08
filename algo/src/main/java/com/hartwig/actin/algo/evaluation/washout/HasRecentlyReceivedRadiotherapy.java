package com.hartwig.actin.algo.evaluation.washout;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;

public class HasRecentlyReceivedRadiotherapy implements PassOrFailEvaluator {

    private final int referenceYear;
    private final int referenceMonth;

    HasRecentlyReceivedRadiotherapy(final int referenceYear, final int referenceMonth) {
        this.referenceYear = referenceYear;
        this.referenceMonth = referenceMonth;
    }

    @Override
    public boolean isPass(@NotNull PatientRecord record) {
        for (PriorTumorTreatment priorTumorTreatment : record.clinical().priorTumorTreatments()) {
            if (priorTumorTreatment.categories().contains(TreatmentCategory.RADIOTHERAPY)) {
                Integer year = priorTumorTreatment.year();
                Integer month = priorTumorTreatment.month();

                if ((year == null) || (year == referenceYear && (month == null || month == referenceMonth))) {
                    return true;
                }
            }
        }

        return false;
    }

    @NotNull
    @Override
    public String passMessage() {
        return "Patient has recently received radiotherapy";
    }

    @NotNull
    @Override
    public String failMessage() {
        return "Patient has not recently received radiotherapy";
    }
}
