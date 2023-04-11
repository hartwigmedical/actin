package com.hartwig.actin.molecular.orange.datamodel;

import java.time.LocalDate;
import java.util.Optional;

import com.hartwig.actin.molecular.datamodel.ExperimentType;
import com.hartwig.actin.molecular.orange.datamodel.chord.ChordRecord;
import com.hartwig.actin.molecular.orange.datamodel.cuppa.CuppaRecord;
import com.hartwig.actin.molecular.orange.datamodel.lilac.LilacRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.peach.PeachRecord;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleRecord;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterRecord;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class OrangeRecord {

    @NotNull
    public abstract String sampleId();

    @NotNull
    public abstract LocalDate experimentDate();

    @NotNull
    public abstract ExperimentType experimentType();

    @NotNull
    public abstract OrangeRefGenomeVersion refGenomeVersion();

    @NotNull
    public abstract PurpleRecord purple();

    @NotNull
    public abstract LinxRecord linx();

    public abstract Optional<PeachRecord> peach();

    public abstract Optional<CuppaRecord> cuppa();

    public abstract Optional<VirusInterpreterRecord> virusInterpreter();

    @NotNull
    public abstract LilacRecord lilac();

    public abstract Optional<ChordRecord> chord();

}
