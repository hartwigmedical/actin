package com.hartwig.actin.ckb.datamodel.treatmentapproaches;

import java.time.LocalDate;
import java.util.List;

import com.hartwig.actin.ckb.datamodel.drug.DrugClass;
import com.hartwig.actin.ckb.datamodel.reference.Reference;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class RelevantTreatmentApproaches {

    public abstract int id();

    @Nullable
    public abstract DrugClass drugClass();

    @NotNull
    public abstract List<Reference> references();

    @NotNull
    public abstract LocalDate createDate();

    @Nullable
    public abstract LocalDate updateDate();
}
