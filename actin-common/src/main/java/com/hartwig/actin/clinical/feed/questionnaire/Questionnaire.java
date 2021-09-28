package com.hartwig.actin.clinical.feed.questionnaire;

import java.util.List;

import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Questionnaire {

    @Nullable
    public abstract String tumorLocation();

    @Nullable
    public abstract String tumorType();

    @Nullable
    public abstract TumorStage stage();

    @Nullable
    public abstract List<String> treatmentHistoryCurrentTumor();

    @Nullable
    public abstract List<String> otherOncologicalHistory();

    @Nullable
    public abstract List<String> nonOncologicalHistory();

    @Nullable
    public abstract Boolean hasMeasurableLesionRecist();

    @Nullable
    public abstract Boolean hasBrainLesions();

    @Nullable
    public abstract Boolean hasActiveBrainLesions();

    @Nullable
    public abstract Boolean hasSymptomaticBrainLesions();

    @Nullable
    public abstract Boolean hasCnsLesions();

    @Nullable
    public abstract Boolean hasActiveCnsLesions();

    @Nullable
    public abstract Boolean hasSymptomaticCnsLesions();

    @Nullable
    public abstract Boolean hasBoneLesions();

    @Nullable
    public abstract Boolean hasLiverLesions();

    @Nullable
    public abstract Integer whoStatus();

    @Nullable
    public abstract Boolean hasSignificantCurrentInfection();

    @Nullable
    public abstract Boolean hasSignificantAberrationLatestECG();

    @Nullable
    public abstract String significantAberrationLatestECG();

    @Nullable
    public abstract List<String> cancerRelatedComplications();

}
