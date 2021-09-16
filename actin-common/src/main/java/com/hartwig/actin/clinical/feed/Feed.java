package com.hartwig.actin.clinical.feed;

import java.util.List;

import com.hartwig.actin.clinical.feed.bloodpressure.BloodPressureEntry;
import com.hartwig.actin.clinical.feed.complication.ComplicationEntry;
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntry;
import com.hartwig.actin.clinical.feed.lab.LabEntry;
import com.hartwig.actin.clinical.feed.medication.MedicationEntry;
import com.hartwig.actin.clinical.feed.patient.PatientEntry;
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireEntry;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Feed {

    @NotNull
    public abstract List<PatientEntry> patientEntries();

    @NotNull
    public abstract List<QuestionnaireEntry> questionnaireEntries();

    @NotNull
    public abstract List<MedicationEntry> medicationEntries();

    @NotNull
    public abstract List<LabEntry> labEntries();

    @NotNull
    public abstract List<BloodPressureEntry> bloodPressureEntries();

    @NotNull
    public abstract List<ComplicationEntry> complicationEntries();

    @NotNull
    public abstract List<IntoleranceEntry> intoleranceEntries();
}
