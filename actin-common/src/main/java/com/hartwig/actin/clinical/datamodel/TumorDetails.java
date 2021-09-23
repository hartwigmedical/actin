package com.hartwig.actin.clinical.datamodel;

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

    @NotNull
    public abstract String stage();

    public abstract boolean hasMeasurableLesionRecist();

    public abstract boolean hasBrainLesions();

    public abstract boolean hasActiveBrainLesions();

    public abstract boolean hasSymptomaticBrainLesions();

    public abstract boolean hasCnsLesions();

    public abstract boolean hasActiveCnsLesions();

    public abstract boolean hasSymptomaticCnsLesions();

    public abstract boolean hasBoneLesions();

    public abstract boolean hasLiverLesions();

    public abstract boolean hasOtherLesions();

    @Nullable
    public abstract String otherLesions();

}
