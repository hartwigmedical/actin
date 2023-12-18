package com.hartwig.actin.clinical.curation

import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Assert
import org.junit.Test

class CurationDoidValidatorTest {
    @Test
    fun shouldIdentifyInvalidCancerDoidSets() {
        val doidModel = TestDoidModelFactory.createWithOneParentChild(CurationDoidValidator.DISEASE_OF_CELLULAR_PROLIFERATION_DOID, "child")
        val curationDoidValidator = CurationDoidValidator(doidModel)
        val valid: Set<String> = setOf("child")
        Assert.assertTrue(curationDoidValidator.isValidCancerDoidSet(valid))
        val generic: Set<String> = setOf(CurationDoidValidator.DISEASE_DOID)
        Assert.assertFalse(curationDoidValidator.isValidCancerDoidSet(generic))
        val notAllValid: Set<String> = setOf("child", "other")
        Assert.assertFalse(curationDoidValidator.isValidCancerDoidSet(notAllValid))
        val empty: Set<String> = emptySet()
        Assert.assertFalse(curationDoidValidator.isValidCancerDoidSet(empty))
    }

    @Test
    fun shouldIdentifyInvalidDiseaseDoidSets() {
        val doidModel = TestDoidModelFactory.createWithOneParentChild(CurationDoidValidator.DISEASE_DOID, "child")
        val curationDoidValidator = CurationDoidValidator(doidModel)
        val valid: Set<String> = setOf("child")
        Assert.assertTrue(curationDoidValidator.isValidDiseaseDoidSet(valid))
        val cancer: Set<String> = setOf(CurationDoidValidator.DISEASE_OF_CELLULAR_PROLIFERATION_DOID)
        Assert.assertFalse(curationDoidValidator.isValidDiseaseDoidSet(cancer))
        val notAllValid: Set<String> = setOf("child", "other")
        Assert.assertFalse(curationDoidValidator.isValidDiseaseDoidSet(notAllValid))
        Assert.assertFalse(curationDoidValidator.isValidDiseaseDoidSet(emptySet()))
    }
}