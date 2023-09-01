package com.hartwig.actin.clinical.datamodel.treatment;

import java.util.Set;

import com.hartwig.actin.Displayable;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Drug implements Displayable {

    @NotNull
    public abstract String name();

    @NotNull
    public abstract Set<DrugType> drugTypes();

    @NotNull
    public abstract TreatmentCategory category();

    @Nullable
    public abstract String displayOverride();

    @Override
    @NotNull
    public String display() {
        String alternateDisplay = displayOverride();
        return (alternateDisplay != null) ? alternateDisplay : name().replace("_", " ").toLowerCase();
    }
}
