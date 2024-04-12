package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasHistoryOfSecondMalignancyIgnoringDoidTermsTest{

    private val matchDoid = "match"
    private val parentDoid = "parent"
    private val doidModel = TestDoidModelFactory.createWithOneParentChildAndTerms(
        mapOf(parentDoid to DoidConstants.LUNG_CANCER_DOID),
        mapOf(matchDoid to DoidConstants.LUNG_SMALL_CELL_CARCINOMA_DOID))
    private val function = HasHistoryOfSecondMalignancyIgnoringDoidTerms(doidModel, listOf(DoidConstants.LUNG_SMALL_CELL_CARCINOMA_DOID))

    @Test
    fun `Should fail when no prior tumors present`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(emptyList())))
    }

    @Test
    fun `Should fail when prior tumors present in history but with doid to ignore`(){
        val priorTumors = listOf(PriorTumorTestFactory.priorSecondPrimary(doid = matchDoid))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))
        println(function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)).passSpecificMessages)
    }

    @Test
    fun `Should fail when prior tumors present in history but doid is child of doid to ignore`(){
        val priorTumors = listOf(PriorTumorTestFactory.priorSecondPrimary(doid = matchDoid))
        val function = HasHistoryOfSecondMalignancyIgnoringDoidTerms(doidModel, listOf(parentDoid))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))
        println(function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)).passSpecificMessages)
    }

    @Test
    fun `Should pass when prior tumors present in history with doid term not to ignore`(){
        val priorTumors = listOf(PriorTumorTestFactory.priorSecondPrimary(doid = "other"))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))
        println(function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)).passSpecificMessages)
    }
}
