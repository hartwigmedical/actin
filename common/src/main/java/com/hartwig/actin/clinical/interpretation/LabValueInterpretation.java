package com.hartwig.actin.clinical.interpretation;

import java.util.List;

import com.hartwig.actin.clinical.datamodel.LabValue;

import org.jetbrains.annotations.NotNull;

public class LabValueInterpretation {

    @NotNull
    private final List<LabValue> labValues;

    public LabValueInterpretation(@NotNull final List<LabValue> labValues) {
        this.labValues = labValues;
    }
}
