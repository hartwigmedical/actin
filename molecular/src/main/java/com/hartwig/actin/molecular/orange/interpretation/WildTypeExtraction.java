package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class WildTypeExtraction {

    private static final Logger LOGGER = LogManager.getLogger(WildTypeExtraction.class);

    private WildTypeExtraction() {
    }

    @Nullable
    public static Set<String> extract(@NotNull OrangeRecord record) {
        if (!record.purple().hasSufficientQuality() || !record.purple().containsTumorCells()) {
            return null;
        }

        if (record.wildTypeGenes().isEmpty()) {
            LOGGER.warn("No wild-type genes found even though purple has both reliable quality and purity!");
        }

        return Sets.newHashSet(record.wildTypeGenes().iterator());
    }
}
