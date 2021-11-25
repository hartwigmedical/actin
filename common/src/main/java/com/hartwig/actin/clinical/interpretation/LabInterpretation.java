package com.hartwig.actin.clinical.interpretation;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.hartwig.actin.clinical.datamodel.LabValue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LabInterpretation {

    private static final Logger LOGGER = LogManager.getLogger(LabInterpretation.class);

    @NotNull
    private final Multimap<LabMeasurement, LabValue> labMeasurements;

    public LabInterpretation(@NotNull final Multimap<LabMeasurement, LabValue> labMeasurements) {
        this.labMeasurements = labMeasurements;
    }

    @Nullable
    public LocalDate mostRecentRelevantDate() {
        LocalDate mostRecentDate = null;

        LabValue mostRecentLabValueByName = mostRecent(labMeasurements.values());
        if (mostRecentLabValueByName != null && (mostRecentDate == null || mostRecentLabValueByName.date().isAfter(mostRecentDate))) {
            mostRecentDate = mostRecentLabValueByName.date();
        }

        return mostRecentDate;
    }

    @Nullable
    public List<LabValue> allValues(@NotNull LabMeasurement measurement) {
        if (!labMeasurements.containsKey(measurement)) {
            LOGGER.warn("Could not find measurement for '" + measurement + "'");
            return null;
        }
        Collection<LabValue> values = labMeasurements.get(measurement);
        return values != null ? Lists.newArrayList(values) : null;
    }

    @Nullable
    public LabValue mostRecentValue(@NotNull LabMeasurement measurement) {
        Collection<LabValue> values = labMeasurements.get(measurement);
        return values != null ? mostRecent(values) : null;
    }

    @Nullable
    private static LabValue mostRecent(@NotNull Iterable<LabValue> labValues) {
        LabValue mostRecent = null;
        for (LabValue labValue : labValues) {
            if (mostRecent == null || labValue.date().isAfter(mostRecent.date())) {
                mostRecent = labValue;
            }
        }

        return mostRecent;
    }
}
