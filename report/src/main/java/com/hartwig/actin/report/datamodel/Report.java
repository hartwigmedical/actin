package com.hartwig.actin.report.datamodel;

import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Report {

    @NotNull
    public abstract String sampleId();

    @NotNull
    public abstract ClinicalRecord clinical();

    @NotNull
    public abstract MolecularRecord molecular();

    @NotNull
    public abstract TreatmentMatch treatmentMatch();

}
