package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.clinical.datamodel.ImmutableAtcClassification
import com.hartwig.actin.clinical.datamodel.ImmutableAtcLevel
import org.apache.logging.log4j.util.Strings

internal object AtcTestFactory {

    fun atcClassificationBuilder(): ImmutableAtcClassification.Builder {
        return ImmutableAtcClassification.builder().anatomicalMainGroup(atcLevelBuilder().build())
            .chemicalSubGroup(atcLevelBuilder().build())
            .chemicalSubstance(atcLevelBuilder().build())
            .pharmacologicalSubGroup(atcLevelBuilder().build())
            .therapeuticSubGroup(atcLevelBuilder().build())
    }

    fun atcLevelBuilder(): ImmutableAtcLevel.Builder {
        return ImmutableAtcLevel.builder().name(Strings.EMPTY).code(Strings.EMPTY)
    }
}