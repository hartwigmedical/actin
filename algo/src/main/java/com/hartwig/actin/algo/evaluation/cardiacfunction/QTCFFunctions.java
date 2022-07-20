package com.hartwig.actin.algo.evaluation.cardiacfunction;

import com.hartwig.actin.clinical.datamodel.ECG;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class QTCFFunctions {

    static final String EXPECTED_QTCF_UNIT = "ms";

    private QTCFFunctions() {
    }

    public static boolean hasQTCF(@Nullable ECG ecg) {
        return ecg != null && ecg.qtcfValue() != null && ecg.qtcfUnit() != null;
    }

    public static boolean isExpectedQTCFUnit(@NotNull String qtcfUnit) {
        return qtcfUnit.equalsIgnoreCase(EXPECTED_QTCF_UNIT);
    }
}
