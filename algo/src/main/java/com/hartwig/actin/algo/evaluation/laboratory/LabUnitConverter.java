package com.hartwig.actin.algo.evaluation.laboratory;

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
        LabUnit measuredUnit = LabUnit.fromString(labValue.unit());

        if (measuredUnit == null) {
            LOGGER.warn("Could not map lab unit '{}'", labValue.unit());
            return null;
        }

        if (measuredUnit == targetUnit) {
            return labValue.value();
        }

        Double conversionFactor = LabUnitConversionTable.findConversionFactor(measuredUnit, targetUnit);
        if (conversionFactor == null) {
            LOGGER.warn("No conversion factor defined from '{}' to '{}'", measuredUnit, targetUnit);
            return null;
        }

        return labValue.value() * conversionFactor;
    }
}
