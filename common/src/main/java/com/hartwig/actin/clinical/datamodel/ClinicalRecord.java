package com.hartwig.actin.clinical.datamodel;

import java.util.List;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ClinicalRecord {

    @NotNull
    public abstract String sampleId();

    @NotNull
    public abstract PatientDetails patient();

    @NotNull
    public abstract TumorDetails tumor();

    @NotNull
    public abstract ClinicalStatus clinicalStatus();

    @NotNull
    public abstract List<PriorTumorTreatment> priorTumorTreatments();

    @NotNull
    public abstract List<PriorSecondPrimary> priorSecondPrimaries();

    @NotNull
    public abstract List<PriorOtherCondition> priorOtherConditions();

    @NotNull
    public abstract List<PriorMolecularTest> priorMolecularTests();

    @NotNull
    public abstract List<CancerRelatedComplication> cancerRelatedComplications();

    @NotNull
    public abstract List<LabValue> labValues();

    @NotNull
    public abstract List<Toxicity> toxicities();

    @NotNull
    public abstract List<Allergy> allergies();

    @NotNull
    public abstract List<Surgery> surgeries();

    @NotNull
    public abstract List<BodyWeight> bodyWeights();

    @NotNull
    public abstract List<VitalFunction> vitalFunctions();

    @NotNull
    public abstract List<BloodTransfusion> bloodTransfusions();

    @NotNull
    public abstract List<Medication> medications();

}
