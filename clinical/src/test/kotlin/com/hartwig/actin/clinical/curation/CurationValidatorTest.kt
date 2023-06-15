package com.hartwig.actin.clinical.curation

import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Assert
import org.junit.Test

class CurationValidatorTest {
    @Test
    fun shouldIdentifyInvalidCancerDoidSets() {
        val doidModel = TestDoidModelFactory.createWithOneParentChild(CurationValidator.DISEASE_OF_CELLULAR_PROLIFERATION_DOID, "child")
        val curationValidator = CurationValidator(doidModel)
        val valid: Set<String> = setOf("child")
        Assert.assertTrue(curationValidator.isValidCancerDoidSet(valid))
        val generic: Set<String> = setOf(CurationValidator.DISEASE_DOID)
        Assert.assertFalse(curationValidator.isValidCancerDoidSet(generic))
        val notAllValid: Set<String> = setOf("child", "other")
        Assert.assertFalse(curationValidator.isValidCancerDoidSet(notAllValid))
        val empty: Set<String> = emptySet()
        Assert.assertFalse(curationValidator.isValidCancerDoidSet(empty))
    }

    @Test
    fun shouldIdentifyInvalidDiseaseDoidSets() {
        val doidModel = TestDoidModelFactory.createWithOneParentChild(CurationValidator.DISEASE_DOID, "child")
        val curationValidator = CurationValidator(doidModel)
        val valid: Set<String> = setOf("child")
        Assert.assertTrue(curationValidator.isValidDiseaseDoidSet(valid))
        val cancer: Set<String> = setOf(CurationValidator.DISEASE_OF_CELLULAR_PROLIFERATION_DOID)
        Assert.assertFalse(curationValidator.isValidDiseaseDoidSet(cancer))
        val notAllValid: Set<String> = setOf("child", "other")
        Assert.assertFalse(curationValidator.isValidDiseaseDoidSet(notAllValid))
        Assert.assertFalse(curationValidator.isValidDiseaseDoidSet(emptySet()))
    }
}