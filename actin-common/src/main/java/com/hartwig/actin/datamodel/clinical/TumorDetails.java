package com.hartwig.actin.datamodel.clinical;

import java.util.List;
import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class TumorDetails {

    @Nullable
    public abstract String primaryTumorLocation();

    @Nullable
    public abstract String primaryTumorSubLocation();

    @Nullable
    public abstract String primaryTumorType();

    @Nullable
    public abstract String primaryTumorSubType();

    @Nullable
    public abstract String primaryTumorExtraDetails();

    @Nullable
    public abstract Set<String> doids();

    @Nullable
    public abstract TumorStage stage();

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
    public abstract Boolean hasOtherLesions();

    @Nullable
    public abstract List<String> otherLesions();

    @Nullable
    public abstract String biopsyLocation();

}
