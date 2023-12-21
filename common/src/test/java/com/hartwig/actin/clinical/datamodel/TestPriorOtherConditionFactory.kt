package com.hartwig.actin.clinical.datamodel

import org.apache.logging.log4j.util.Strings

object TestPriorOtherConditionFactory {
    fun builder(): ImmutablePriorOtherCondition.Builder {
        return ImmutablePriorOtherCondition.builder().name(Strings.EMPTY).category(Strings.EMPTY).isContraindicationForTherapy(false)
    }
}
