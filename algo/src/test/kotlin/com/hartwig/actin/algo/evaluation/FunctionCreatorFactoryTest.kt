package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.algo.calendar.ReferenceDateProviderTestFactory.createCurrentDateProvider
import com.hartwig.actin.algo.evaluation.FunctionCreatorFactory.create
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.treatment.datamodel.EligibilityRule
import com.hartwig.actin.treatment.datamodel.TestFunctionInputResolveFactory
import com.hartwig.actin.treatment.input.ParameterizedFunctionTestFactory
import com.hartwig.actin.treatment.input.composite.CompositeRules
import org.junit.Assert
import org.junit.Test

class FunctionCreatorFactoryTest {
    @Test
    fun everyFunctionCanBeCreated() {
        val doidTerm = "term 1"
        val doidModel = TestDoidModelFactory.createWithOneDoidAndTerm("doid 1", doidTerm)
        val referenceDateProvider = createCurrentDateProvider()
        val functionInputResolver = TestFunctionInputResolveFactory.createResolverWithDoidModel(doidModel)
        val map = create(referenceDateProvider, doidModel, functionInputResolver)
        val factory = ParameterizedFunctionTestFactory(doidTerm)
        // TODO: remove filter after merging function implementations
        EligibilityRule.values().filterNot {
            it in setOf(
                EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X,
                EligibilityRule.HAS_HAD_CATEGORY_X_TREATMENT_IGNORING_DRUGS_Y,
                EligibilityRule.HAS_HAD_NON_INTERNAL_RADIOTHERAPY,
                EligibilityRule.HAS_PREVIOUSLY_PARTICIPATED_IN_TRIAL
            )
        }.forEach { rule ->
            val function = factory.create(rule)
            if (!CompositeRules.isComposite(rule)) {
                val creator = map[rule]
                Assert.assertNotNull("$rule has no creator configured", creator)
                Assert.assertNotNull("$rule creator could not create function", creator!!.create(function))
            }
        }
    }
}