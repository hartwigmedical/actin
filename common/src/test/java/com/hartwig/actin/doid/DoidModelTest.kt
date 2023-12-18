package com.hartwig.actin.doid

import com.google.common.collect.Maps
import com.hartwig.actin.doid.config.ImmutableAdenoSquamousMapping
import org.junit.Assert
import org.junit.Test

class DoidModelTest {
    @Test
    fun canAddParents() {
        val model = createTestModel(TestDoidManualConfigFactory.createWithOneAdditionalDoid("300", "301"))
        val withParents200 = model.doidWithParents("200")
        Assert.assertEquals(4, withParents200.size.toLong())
        Assert.assertTrue(withParents200.contains("200"))
        Assert.assertTrue(withParents200.contains("300"))
        Assert.assertTrue(withParents200.contains("301"))
        Assert.assertTrue(withParents200.contains("400"))
        val withParents300 = model.doidWithParents("300")
        Assert.assertEquals(3, withParents300.size.toLong())
        Assert.assertFalse(withParents300.contains("200"))
        Assert.assertTrue(withParents300.contains("300"))
        Assert.assertTrue(withParents300.contains("301"))
        Assert.assertTrue(withParents300.contains("400"))
        val withParents400 = model.doidWithParents("400")
        Assert.assertEquals(1, withParents400.size.toLong())
        Assert.assertFalse(withParents400.contains("200"))
        Assert.assertFalse(withParents400.contains("300"))
        Assert.assertFalse(withParents400.contains("301"))
        Assert.assertTrue(withParents400.contains("400"))
        Assert.assertEquals(1, model.doidWithParents("500").size.toLong())
    }

    @Test
    fun canResolveMainCancerTypes() {
        val doidModel = createTestModel(TestDoidManualConfigFactory.createWithOneMainCancerDoid("123"))
        Assert.assertFalse(doidModel.mainCancerDoids("123").isEmpty())
        Assert.assertTrue(doidModel.mainCancerDoids("does not exist").isEmpty())
    }

    @Test
    fun canResolveAdenoSquamousMappings() {
        val mapping: AdenoSquamousMapping =
            ImmutableAdenoSquamousMapping.builder().adenoSquamousDoid("1").squamousDoid("2").adenoDoid("3").build()
        val doidModel = createTestModel(TestDoidManualConfigFactory.createWithOneAdenoSquamousMapping(mapping))
        Assert.assertEquals(0, doidModel.adenoSquamousMappingsForDoid("1").size.toLong())
        Assert.assertEquals(1, doidModel.adenoSquamousMappingsForDoid("2").size.toLong())
        Assert.assertEquals(1, doidModel.adenoSquamousMappingsForDoid("3").size.toLong())
        Assert.assertEquals(0, doidModel.adenoSquamousMappingsForDoid("4").size.toLong())
    }

    @Test
    fun canResolveTerms() {
        val model = createTestModel(TestDoidManualConfigFactory.createMinimalTestDoidManualConfig())
        Assert.assertEquals("tumor A", model.resolveTermForDoid("200"))
        Assert.assertNull(model.resolveTermForDoid("300"))
    }

    @Test
    fun canResolveDoids() {
        val model = createTestModel(TestDoidManualConfigFactory.createMinimalTestDoidManualConfig())
        Assert.assertEquals("200", model.resolveDoidForTerm("tumor a"))
        Assert.assertEquals("200", model.resolveDoidForTerm("tumor A"))
        Assert.assertEquals("200", model.resolveDoidForTerm("Tumor A"))
        Assert.assertNull(model.resolveDoidForTerm("tumor B"))
    }

    companion object {
        private fun createTestModel(manualConfig: DoidManualConfig): DoidModel {
            val childToParentsMap: Multimap<String, String> = ArrayListMultimap.create<String, String>()
            childToParentsMap.put("200", "300")
            childToParentsMap.put("300", "400")
            val termPerDoidMap: MutableMap<String, String> = Maps.newHashMap()
            termPerDoidMap["200"] = "tumor A"
            val doidPerLowerCaseTermMap: MutableMap<String, String> = Maps.newHashMap()
            doidPerLowerCaseTermMap["tumor a"] = "200"
            return DoidModel(childToParentsMap, termPerDoidMap, doidPerLowerCaseTermMap, manualConfig)
        }
    }
}