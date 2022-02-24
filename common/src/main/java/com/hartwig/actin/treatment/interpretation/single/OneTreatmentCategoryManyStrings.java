package com.hartwig.actin.treatment.interpretation.single;

import java.util.List;

import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class OneTreatmentCategoryManyStrings {

    @NotNull
    public abstract TreatmentCategory treatmentCategory();

    @NotNull
    public abstract List<String> strings();
}
