package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.algo.calendar.ReferenceDateProviderTestFactory.createCurrentDateProvider
import com.hartwig.actin.algo.evaluation.FunctionCreatorFactory.create
import com.hartwig.actin.algo.evaluation.medication.AtcTestFactory
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.trial.datamodel.EligibilityRule
import com.hartwig.actin.trial.datamodel.TestFunctionInputResolveFactory
import com.hartwig.actin.trial.input.ParameterizedFunctionTestFactory
import com.hartwig.actin.trial.input.composite.CompositeRules
import org.junit.Assert
import org.junit.Test

class FunctionCreatorFactoryTest {
    @Test
    fun everyFunctionCanBeCreated() {
        val doidTerm = "term 1"
        val doidModel = TestDoidModelFactory.createWithOneDoidAndTerm("doid 1", doidTerm)
        val atcTree = AtcTestFactory.createProperAtcTree()
        val referenceDateProvider = createCurrentDateProvider()
        val functionInputResolver = TestFunctionInputResolveFactory.createResolverWithDoidModel(doidModel)
        val map = create(referenceDateProvider, doidModel, functionInputResolver, atcTree)
        val factory = ParameterizedFunctionTestFactory(doidTerm)
        EligibilityRule.values().forEach { rule ->
            val function = factory.create(rule)
            if (!CompositeRules.isComposite(rule)) {
                val creator = map[rule]
                Assert.assertNotNull("$rule has no creator configured", creator)
                Assert.assertNotNull("$rule creator could not create function", creator!!.create(function))
            }
        }
    }
}