package com.hartwig.actin.doid

import com.hartwig.actin.doid.config.AdenoSquamousMapping
import com.hartwig.actin.doid.config.DoidManualConfig
import com.hartwig.actin.doid.config.TestDoidManualConfigFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DoidModelTest {

    @Test
    fun `Should add parents`() {
        val model = createTestModel(TestDoidManualConfigFactory.createWithOneAdditionalDoid("300", "301"))
        assertThat(model.doidWithParents("200")).containsExactlyInAnyOrder("200", "300", "301", "400")
        assertThat(model.doidWithParents("300")).containsExactlyInAnyOrder("300", "301", "400")
        assertThat(model.doidWithParents("400")).containsExactly("400")
        assertThat(model.doidWithParents("500")).hasSize(1)
    }

    @Test
    fun `Should resolve main cancer types`() {
        val doidModel = createTestModel(TestDoidManualConfigFactory.createWithOneMainCancerDoid("123"))
        assertThat(doidModel.mainCancerDoids("123")).isNotEmpty()
        assertThat(doidModel.mainCancerDoids("does not exist")).isEmpty()
    }

    @Test
    fun `Should resolve adeno squamous mappings`() {
        val mapping = AdenoSquamousMapping(adenoSquamousDoid = "1", squamousDoid = "2", adenoDoid = "3")
        val doidModel = createTestModel(TestDoidManualConfigFactory.createWithOneAdenoSquamousMapping(mapping))
        assertThat(doidModel.adenoSquamousMappingsForDoid("1")).hasSize(0)
        assertThat(doidModel.adenoSquamousMappingsForDoid("2")).hasSize(1)
        assertThat(doidModel.adenoSquamousMappingsForDoid("3")).hasSize(1)
        assertThat(doidModel.adenoSquamousMappingsForDoid("4")).hasSize(0)
    }

    @Test
    fun `Should resolve terms`() {
        val model = createTestModel(TestDoidManualConfigFactory.createMinimalTestDoidManualConfig())
        assertThat(model.resolveTermForDoid("200")).isEqualTo("tumor A")
        assertThat(model.resolveTermForDoid("300")).isNull()
    }

    @Test
    fun `Should resolve doids`() {
        val model = createTestModel(TestDoidManualConfigFactory.createMinimalTestDoidManualConfig())
        assertThat(model.resolveDoidForTerm("tumor a")).isEqualTo("200")
        assertThat(model.resolveDoidForTerm("tumor A")).isEqualTo("200")
        assertThat(model.resolveDoidForTerm("Tumor A")).isEqualTo("200")
        assertThat(model.resolveDoidForTerm("tumor B")).isNull()
    }

    private fun createTestModel(manualConfig: DoidManualConfig): DoidModel {
        val childToParentsMap = mapOf(
            "200" to listOf("300"),
            "300" to listOf("400")
        )
        val termPerDoidMap = mapOf("200" to "tumor A")
        val doidPerLowerCaseTermMap = mapOf("tumor a" to "200")
        return DoidModel(childToParentsMap, termPerDoidMap, doidPerLowerCaseTermMap, manualConfig)
    }
}