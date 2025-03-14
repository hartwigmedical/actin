package com.hartwig.actin.trial

import com.hartwig.actin.datamodel.trial.Trial
import com.hartwig.actin.molecular.UnparameterisedIhcRule
import org.reflections.Reflections

class IhcProteinEnumeration {
    fun enumerate(trials: List<Trial>): Set<String> {
        return (collectUnparameterised() + EligibilityRuleUsageEvaluator.extractIhcProteinParameters(trials)).toSet()
    }

    private fun collectUnparameterised(): Set<String> {
        val classes =
            Reflections("com.hartwig.actin").getTypesAnnotatedWith(UnparameterisedIhcRule::class.java)
        return classes.map { it.getAnnotation(UnparameterisedIhcRule::class.java).targetProtein }.toSet()
    }
}
