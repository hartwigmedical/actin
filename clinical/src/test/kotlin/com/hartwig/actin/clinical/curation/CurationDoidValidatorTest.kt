package com.hartwig.actin.clinical.curation

import com.hartwig.actin.doid.TestDoidModelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CurationDoidValidatorTest {

    @Test
    fun `Should identify invalid cancer DOID sets`() {
        val doidModel = TestDoidModelFactory.createWithOneParentChild(CurationDoidValidator.DISEASE_OF_CELLULAR_PROLIFERATION_DOID, "child")
        val curationDoidValidator = CurationDoidValidator(doidModel)

        val valid = setOf("child")
        assertThat(curationDoidValidator.isValidCancerDoidSet(valid)).isTrue()

        val generic = setOf(CurationDoidValidator.DISEASE_DOID)
        assertThat(curationDoidValidator.isValidCancerDoidSet(generic)).isFalse()

        val notAllValid = setOf("child", "other")
        assertThat(curationDoidValidator.isValidCancerDoidSet(notAllValid)).isFalse()

        assertThat(curationDoidValidator.isValidCancerDoidSet(emptySet())).isFalse()
    }

    @Test
    fun `Should identify invalid disease DOID sets`() {
        val doidModel = TestDoidModelFactory.createWithOneParentChild(CurationDoidValidator.DISEASE_DOID, "child")
        val curationDoidValidator = CurationDoidValidator(doidModel)

        val valid = setOf("child")
        assertThat(curationDoidValidator.isValidDiseaseDoidSet(valid)).isTrue()

        val cancer = setOf(CurationDoidValidator.DISEASE_OF_CELLULAR_PROLIFERATION_DOID)
        assertThat(curationDoidValidator.isValidDiseaseDoidSet(cancer)).isFalse()

        val notAllValid = setOf("child", "other")
        assertThat(curationDoidValidator.isValidDiseaseDoidSet(notAllValid)).isFalse()

        assertThat(curationDoidValidator.isValidDiseaseDoidSet(emptySet())).isFalse()
    }
}