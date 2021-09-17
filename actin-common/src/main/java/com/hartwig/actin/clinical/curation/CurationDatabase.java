package com.hartwig.actin.clinical.curation;

import java.util.Map;

import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class CurationDatabase {

    @NotNull
    public abstract Map<String, PriorTumorTreatment> priorTumorTreatmentMap();

}
