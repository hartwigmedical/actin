package com.hartwig.actin.molecular.datamodel.characteristics;

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class MolecularCharacteristics {

    @Nullable
    public abstract Double purity();

    @Nullable
    public abstract Double ploidy();

    @Nullable
    public abstract PredictedTumorOrigin predictedTumorOrigin();

    @Nullable
    public abstract Boolean isMicrosatelliteUnstable();

    @Nullable
    public abstract ActionableEvidence microsatelliteEvidence();

    @Nullable
    public abstract Double homologousRepairScore();

    @Nullable
    public abstract Boolean isHomologousRepairDeficient();

    @Nullable
    public abstract ActionableEvidence homologousRepairEvidence();

    @Nullable
    public abstract Double tumorMutationalBurden();

    @Nullable
    public abstract Boolean hasHighTumorMutationalBurden();

    @Nullable
    public abstract ActionableEvidence tumorMutationalBurdenEvidence();

    @Nullable
    public abstract Integer tumorMutationalLoad();

    @Nullable
    public abstract Boolean hasHighTumorMutationalLoad();

    @Nullable
    public abstract ActionableEvidence tumorMutationalLoadEvidence();
}
