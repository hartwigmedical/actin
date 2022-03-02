package com.hartwig.actin.algo.evaluation.laboratory;

import com.hartwig.actin.clinical.datamodel.LabUnit;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class LabUnitConverter {

    private static final Logger LOGGER = LogManager.getLogger(LabUnitConverter.class);

    private LabUnitConverter() {
    }

    @Nullable
    public static Double convert(@NotNull LabValue labValue, @NotNull LabUnit targetUnit) {
        if (labValue.unit() == targetUnit) {
            return labValue.value();
        }

        Double conversionFactor = LabUnitConversionTable.findConversionFactor(labValue.unit(), targetUnit);
        if (conversionFactor == null) {
            LOGGER.warn("No conversion factor defined from '{}' to '{}'", labValue.unit(), targetUnit);
            return null;
        }

        return labValue.value() * conversionFactor;
    }
}
