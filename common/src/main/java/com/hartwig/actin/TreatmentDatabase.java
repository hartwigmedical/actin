package com.hartwig.actin;

import java.util.Map;

import com.hartwig.actin.clinical.datamodel.treatment.Drug;
import com.hartwig.actin.clinical.datamodel.treatment.Treatment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TreatmentDatabase {

    @NotNull
    private final Map<String, Drug> drugsByName;
    @NotNull
    private final Map<String, Treatment> treatmentsByName;

    public TreatmentDatabase(@NotNull Map<String, Drug> drugsByName, @NotNull Map<String, Treatment> treatmentsByName) {
        this.drugsByName = drugsByName;
        this.treatmentsByName = treatmentsByName;
    }

    @Nullable
    public Treatment findTreatmentByName(@NotNull String treatmentName) {
        return treatmentsByName.get(treatmentName.toLowerCase());
    }

    @Nullable
    public Drug findDrugByName(@NotNull String drugName) {
        return drugsByName.get(drugName.toLowerCase());
    }
}
