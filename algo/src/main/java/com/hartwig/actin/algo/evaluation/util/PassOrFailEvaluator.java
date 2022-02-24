package com.hartwig.actin.algo.evaluation.util;

import com.hartwig.actin.PatientRecord;

import org.jetbrains.annotations.NotNull;

public interface PassOrFailEvaluator {

    boolean isPass(@NotNull PatientRecord record);

    @NotNull
    String passMessage();

    @NotNull
    String failMessage();
}
