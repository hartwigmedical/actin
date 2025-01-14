package com.hartwig.actin.datamodel.clinical

import java.lang.reflect.Type

enum class ComorbidityClass(val treatmentClass: Type) {
    OTHER_CONDITION(OtherCondition::class.java),
    COMPLICATION(Complication::class.java),
    TOXICITY(Toxicity::class.java),
    INTOLERANCE(Intolerance::class.java);
}