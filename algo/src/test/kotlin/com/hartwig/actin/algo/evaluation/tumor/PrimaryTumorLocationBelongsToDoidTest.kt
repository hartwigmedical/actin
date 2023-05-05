package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.doid.config.AdenoSquamousMapping
import com.hartwig.actin.doid.config.DoidManualConfig
import com.hartwig.actin.doid.config.ImmutableAdenoSquamousMapping
import com.hartwig.actin.doid.config.TestDoidManualConfigFactory
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class PrimaryTumorLocationBelongsToDoidTest {
    @Test
    fun canEvaluate() {
        val doidModel: DoidModel = TestDoidModelFactory.createWithOneParentChild("100", "200")
        val function100 = PrimaryTumorLocationBelongsToDoid(doidModel, "100")
        assertEvaluation(EvaluationResult.PASS, function100.evaluate(TumorTestFactory.withDoids("100")))
        assertEvaluation(EvaluationResult.PASS, function100.evaluate(TumorTestFactory.withDoids("200")))
        assertEvaluation(EvaluationResult.PASS, function100.evaluate(TumorTestFactory.withDoids("10", "100")))
        assertEvaluation(EvaluationResult.FAIL, function100.evaluate(TumorTestFactory.withDoids("50", "250")))
        assertEvaluation(EvaluationResult.UNDETERMINED, function100.evaluate(TumorTestFactory.withDoids(null)))
        val function200 = PrimaryTumorLocationBelongsToDoid(doidModel, "200")
        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(TumorTestFactory.withDoids("100")))
        assertEvaluation(EvaluationResult.PASS, function200.evaluate(TumorTestFactory.withDoids("200")))
        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(TumorTestFactory.withDoids("10", "100")))
        assertEvaluation(EvaluationResult.FAIL, function200.evaluate(TumorTestFactory.withDoids("50", "250")))
        assertEvaluation(EvaluationResult.UNDETERMINED, function200.evaluate(TumorTestFactory.withDoids(emptySet())))
    }

    @Test
    fun canEvaluateUndeterminateMainCancerType() {
        val cancer = "1"
        val stomachCancer = "2"
        val stomachCarcinoma = "3"
        val stomachAdenocarcinoma = "4"
        val stomachLymphoma = "5"
        val childToParentMap: Map<String, String> = mapOf(
            stomachAdenocarcinoma to stomachCarcinoma, stomachCarcinoma to stomachCancer,
            stomachLymphoma to stomachCancer, stomachCancer to cancer
        )
        val doidModel: DoidModel = TestDoidModelFactory.createWithMainCancerTypeAndChildToParentMap(stomachCancer, childToParentMap)
        val function = PrimaryTumorLocationBelongsToDoid(doidModel, stomachCarcinoma)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids("something else")))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids(cancer)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids(stomachLymphoma)))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids(stomachCancer)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withDoids(stomachCarcinoma)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withDoids(stomachAdenocarcinoma)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withDoids("something else", stomachAdenocarcinoma)))
    }

    @Test
    fun canResolveToAdenoSquamousType() {
        val mapping: AdenoSquamousMapping =
            ImmutableAdenoSquamousMapping.builder().adenoSquamousDoid("1").squamousDoid("2").adenoDoid("3").build()
        val config: DoidManualConfig = TestDoidManualConfigFactory.createWithOneAdenoSquamousMapping(mapping)
        val doidModel: DoidModel = TestDoidModelFactory.createWithDoidManualConfig(config)
        val function = PrimaryTumorLocationBelongsToDoid(doidModel, "2")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids("4")))
        assertEvaluation(EvaluationResult.WARN, function.evaluate(TumorTestFactory.withDoids("1")))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withDoids("2")))
    }
}