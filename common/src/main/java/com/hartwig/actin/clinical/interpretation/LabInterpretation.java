package com.hartwig.actin.clinical.interpretation;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.sort.LabValueComparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LabInterpretation {

    private static final Logger LOGGER = LogManager.getLogger(LabInterpretation.class);

    @NotNull
    private final Multimap<String, LabValue> labValuesByName;
    @NotNull
    private final Multimap<String, LabValue> labValuesByCode;

    public LabInterpretation(@NotNull final Multimap<String, LabValue> labValuesByName,
            @NotNull final Multimap<String, LabValue> labValuesByCode) {
        this.labValuesByName = labValuesByName;
        this.labValuesByCode = labValuesByCode;
    }

    @Nullable
    public LocalDate mostRecentRelevantDate() {
        LocalDate mostRecentDate = null;

        LabValue mostRecentLabValueByName = mostRecent(labValuesByName.values());
        if (mostRecentLabValueByName != null && (mostRecentDate == null || mostRecentLabValueByName.date().isAfter(mostRecentDate))) {
            mostRecentDate = mostRecentLabValueByName.date();
        }

        LabValue mostRecentLabValueByCode = mostRecent(labValuesByCode.values());
        if (mostRecentLabValueByCode != null && (mostRecentDate == null || mostRecentLabValueByCode.date().isAfter(mostRecentDate))) {
            mostRecentDate = mostRecentLabValueByCode.date();
        }

        return mostRecentDate;
    }

    @Nullable
    public List<LabValue> allValuesSortedDescending(@NotNull LabValue reference) {
        List<LabValue> values = null;
        if (labValuesByName.values().contains(reference)) {
            values = Lists.newArrayList(labValuesByName.get(reference.name()));
        } else if (labValuesByCode.values().contains(reference)) {
            values = Lists.newArrayList(labValuesByCode.get(reference.code()));
        }

        if (values != null) {
            values.sort(new LabValueComparator());
        }
        return values;
    }

    @Nullable
    public LabValue mostRecentByName(@NotNull String name) {
        if (!LabInterpretationFactory.RELEVANT_LAB_NAMES.contains(name)) {
            LOGGER.warn("Lab value with name '{}' has not been configured as a relevant lab value!", name);
        }
        return mostRecentFromMultimap(labValuesByName, name);
    }

    @Nullable
    public LabValue mostRecentByCode(@NotNull String code) {
        if (!LabInterpretationFactory.RELEVANT_LAB_CODES.contains(code)) {
            LOGGER.warn("Lab value with code '{}' has not been configured as a relevant lab value!", code);
        }
        return mostRecentFromMultimap(labValuesByCode, code);
    }

    @Nullable
    private static LabValue mostRecentFromMultimap(@NotNull Multimap<String, LabValue> multimap, @NotNull String key) {
        Collection<LabValue> values = multimap.get(key);
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
