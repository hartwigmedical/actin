package com.hartwig.actin.clinical;

import java.util.List;

import com.hartwig.actin.clinical.datamodel.Allergy;
import com.hartwig.actin.clinical.datamodel.BloodPressure;
import com.hartwig.actin.clinical.datamodel.BloodTransfusion;
import com.hartwig.actin.clinical.datamodel.CancerRelatedComplication;
import com.hartwig.actin.clinical.datamodel.ClinicalStatus;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.PatientDetails;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.Surgery;
import com.hartwig.actin.clinical.datamodel.Toxicity;
import com.hartwig.actin.clinical.datamodel.TumorDetails;

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
    public abstract List<CancerRelatedComplication> cancerRelatedComplications();

    @NotNull
    public abstract List<Complication> otherComplications();

    @NotNull
    public abstract List<LabValue> labValues();

    @NotNull
    public abstract List<Toxicity> toxicities();

    @NotNull
    public abstract List<Allergy> allergies();

    @NotNull
    public abstract List<Surgery> surgeries();

    @NotNull
    public abstract List<BloodPressure> bloodPressures();

    @NotNull
    public abstract List<BloodTransfusion> bloodTransfusions();

    @NotNull
    public abstract List<Medication> medications();

}
