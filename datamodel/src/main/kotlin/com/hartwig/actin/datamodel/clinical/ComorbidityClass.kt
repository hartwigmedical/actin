package com.hartwig.actin.datamodel.clinical

import java.lang.reflect.Type

enum class ComorbidityClass(val comorbidityClass: Type) {
    OTHER_CONDITION(OtherCondition::class.java),
    COMPLICATION(OtherCondition::class.java),
    TOXICITY(Toxicity::class.java),
    INTOLERANCE(Intolerance::class.java),
    ECG(Ecg::class.java),
    INFECTION(OtherCondition::class.java);
}