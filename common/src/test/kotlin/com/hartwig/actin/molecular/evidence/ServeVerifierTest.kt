package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.molecular.evidence.TestServeFactory.createServeDatabase
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatIllegalStateException
import org.junit.Test

val SINGLE_PROFILE_1 = TestServeMolecularFactory.createHotspotCriterium()
val SINGLE_PROFILE_2 = TestServeMolecularFactory.createGeneCriterium()
val COMBINED_PROFILE = TestServeMolecularFactory.createCombinedCriterium()

class ServeVerifierTest {

    @Test
    fun `Should pass on single profile evidence and trial`() {
        val evidence = TestServeEvidenceFactory.create(molecularCriterium = SINGLE_PROFILE_1)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(SINGLE_PROFILE_1, SINGLE_PROFILE_2))

        val database = createServeDatabase(evidence, trial)
        assertThatCode { ServeVerifier.verifyServeDatabase(database) }.doesNotThrowAnyException()
    }

    @Test
    fun `Should throw exception on trial with at least one combined profile`() {
        val evidence = TestServeEvidenceFactory.create(molecularCriterium = SINGLE_PROFILE_1)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(SINGLE_PROFILE_1, COMBINED_PROFILE))

        val database = createServeDatabase(evidence, trial)

        assertThatIllegalStateException().isThrownBy { ServeVerifier.verifyServeDatabase(database) }
    }

    @Test
    fun `Should throw exception on evidence based on a combined profile`() {
        val evidence = TestServeEvidenceFactory.create(molecularCriterium = COMBINED_PROFILE)
        val trial = TestServeTrialFactory.create(anyMolecularCriteria = setOf(SINGLE_PROFILE_1, SINGLE_PROFILE_2))

        val database = createServeDatabase(evidence, trial)

        assertThatIllegalStateException().isThrownBy { ServeVerifier.verifyServeDatabase(database) }
    }

    @Test
    fun `Should throw exception for hotspot with inconsistent genes in efficacy evidence`() {
        val evidence = TestServeEvidenceFactory.create(
            molecularCriterium = TestServeMolecularFactory.createHotspotCriterium(
                variants = setOf(
                    TestServeMolecularFactory.createVariantAnnotation(gene = "gene1"),
                    TestServeMolecularFactory.createVariantAnnotation(gene = "gene2")
                )
            )
        )

        val database = createServeDatabase(evidence, TestServeTrialFactory.create())
        assertThatIllegalStateException().isThrownBy { ServeVerifier.verifyServeDatabase(database) }
    }


    @Test
    fun `Should throw exception for hotspot with inconsistent genes in actionable trial`() {
        val trial = TestServeTrialFactory.create(
            anyMolecularCriteria = setOf(
                TestServeMolecularFactory.createHotspotCriterium(
                    variants = setOf(
                        TestServeMolecularFactory.createVariantAnnotation(gene = "gene1"),
                        TestServeMolecularFactory.createVariantAnnotation(gene = "gene2")
                    )
                )
            )
        )

        val database = createServeDatabase(TestServeEvidenceFactory.create(), trial)
        assertThatIllegalStateException().isThrownBy { ServeVerifier.verifyServeDatabase(database) }
    }

    @Test
    fun `Should throw exception for hotspot with no variants`() {
        val trial = TestServeTrialFactory.create(
            anyMolecularCriteria =
                setOf(TestServeMolecularFactory.createHotspotCriterium(variants = emptySet()))
        )

        val database = createServeDatabase(TestServeEvidenceFactory.create(), trial)
        assertThatIllegalStateException().isThrownBy { ServeVerifier.verifyServeDatabase(database) }
    }
}