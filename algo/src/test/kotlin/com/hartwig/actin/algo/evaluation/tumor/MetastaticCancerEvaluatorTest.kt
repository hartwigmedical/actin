package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.tumor.MetastaticCancerEvaluator.STAGE_II_POTENTIALLY_METASTATIC_CANCER_DOIDS
import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.doid.TestDoidModelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MetastaticCancerEvaluatorTest {

    private val doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child")

    @Test
    fun `Should return METASTATIC for stage IV`() {
        assertThat(MetastaticCancerEvaluator.isMetastatic(TumorTestFactory.withTumorStage(TumorStage.IV), doidModel)).isEqualTo(
            MetastaticCancerEvaluation.METASTATIC
        )
    }

    @Test
    fun `Should return UNDETERMINED for tumor stage III`() {
        assertThat(MetastaticCancerEvaluator.isMetastatic(TumorTestFactory.withTumorStage(TumorStage.III), doidModel)).isEqualTo(
            MetastaticCancerEvaluation.UNDETERMINED
        )
    }

    @Test
    fun `Should return UNDETERMINED for tumor stage II in cancer type with possible metastatic disease in stage II`() {
        assertThat(
            MetastaticCancerEvaluator.isMetastatic(
                TumorTestFactory.withTumorStageAndDoid(TumorStage.II, STAGE_II_POTENTIALLY_METASTATIC_CANCER_DOIDS.first()), doidModel
            )
        ).isEqualTo(MetastaticCancerEvaluation.UNDETERMINED)
    }

    @Test
    fun `Should return NON_METASTATIC for tumor stage II in cancer type without possible metastatic disease in stage II`() {
        assertThat(
            MetastaticCancerEvaluator.isMetastatic(
                TumorTestFactory.withTumorStageAndDoid(TumorStage.II, DoidConstants.COLORECTAL_CANCER_DOID), doidModel
            )
        ).isEqualTo(MetastaticCancerEvaluation.NON_METASTATIC)
    }

    @Test
    fun `Should return NON_METASTATIC for tumor stage I`() {
        assertThat(
            MetastaticCancerEvaluator.isMetastatic(
                TumorTestFactory.withTumorStageAndDoid(TumorStage.I, STAGE_II_POTENTIALLY_METASTATIC_CANCER_DOIDS.first()), doidModel
            )
        ).isEqualTo(MetastaticCancerEvaluation.NON_METASTATIC)
    }

    @Test
    fun `Should return DATA_MISSING when no tumor stage provided`() {
        assertThat(
            MetastaticCancerEvaluator.isMetastatic(TumorTestFactory.withTumorStage(null), doidModel)
        ).isEqualTo(MetastaticCancerEvaluation.DATA_MISSING)
    }
}