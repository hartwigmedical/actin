package com.hartwig.actin.molecular.clinical

class ClinicalMolecularTest{

/*    val evidenceDatabase = mockk<EvidenceDatabase>()
    val clinicalMolecular = ClinicalMolecular.create()

   @Test
    fun `Should return undated molecular record when no dated records exist`() {

        val molecularRecords = listOf(
            TestMolecularFactory.createMinimalTestMolecularRecord().copy(date = null),
        )
        val molecularHistory = MolecularHistory.fromInputs(molecularRecords, emptyList())
        assertThat(molecularHistory.latestOrangeMolecularRecord()).isEqualTo(molecularRecords[0])
    }

    @Test
    fun `Should return all priorMolecular records`() {

        val molecularRecords = listOf(
            TestMolecularFactory.createMinimalTestMolecularRecord().copy(date = null),
        )

        val priorMolecularTests = listOf(
            PriorMolecularTest("IHC", item = "protein1", impliesPotentialIndeterminateStatus = false),
            PriorMolecularTest("IHC", item = "protein2", impliesPotentialIndeterminateStatus = false),
            PriorMolecularTest("Future-Panel", item = "gene", impliesPotentialIndeterminateStatus = false)
        )

        val molecularHistory = MolecularHistory.fromInputs(molecularRecords, priorMolecularTests)
        assertThat(molecularHistory.latestOrangeMolecularRecord()).isEqualTo(molecularRecords.first())

        assertThat(molecularHistory.allIHCTests().sortedBy { it.item })
            .isEqualTo(priorMolecularTests.filter { it.test == "IHC" }.sortedBy { it.item })
    }

    @Test
    fun `Should classify IHC tests`() {
        assertThat(
            MolecularTestFactory.classify(
                PriorMolecularTest("IHC", item = "protein1", impliesPotentialIndeterminateStatus = false)
            )
        ).isEqualTo(ExperimentType.IHC)
    }

    @Test
    fun `Should classify PD-L1 tests`() {
        assertThat(
            MolecularTestFactory.classify(
                PriorMolecularTest("", item = "PD-L1", impliesPotentialIndeterminateStatus = false)
            )
        ).isEqualTo(ExperimentType.IHC)
    }

    @Test
    fun `Should classify unsupported tests as other`() {
        assertThat(
            MolecularTestFactory.classify(
                PriorMolecularTest("Future-Panel", item = "gene", impliesPotentialIndeterminateStatus = false)
            )
        ).isEqualTo(ExperimentType.OTHER)
    }

    @Test
    fun `Should classify Archer tests`() {
        assertThat(
            MolecularTestFactory.classify(
                PriorMolecularTest(ARCHER_FP_LUNG_TARGET, item = "gene", impliesPotentialIndeterminateStatus = false)
            )
        ).isEqualTo(ExperimentType.ARCHER)
    }

    @Test
    fun `Should classify AvL Panels`() {
        assertThat(
            MolecularTestFactory.classify(
                PriorMolecularTest(AVL_PANEL, impliesPotentialIndeterminateStatus = false)
            )
        ).isEqualTo(ExperimentType.GENERIC_PANEL)
    }

    @Test
    fun `Should classify Free text curated Panels`() {
        assertThat(
            MolecularTestFactory.classify(
                PriorMolecularTest(FREE_TEXT_PANEL, impliesPotentialIndeterminateStatus = false)
            )
        ).isEqualTo(ExperimentType.GENERIC_PANEL)
    }

    @Test
    fun `Should distinguish generic panel types`() {
        val genericPanelTests = listOf(
            TestMolecularFactory.avlPanelPriorMolecularNoMutationsFoundRecord(),
            TestMolecularFactory.freetextPriorMolecularFusionRecord("geneUp", "geneDown")
        )
        val molecularTests = MolecularTestFactory.fromPriorMolecular(genericPanelTests)
        assertThat(molecularTests).hasSize(2)
        assertThat(molecularTests.filter { it.type == ExperimentType.GENERIC_PANEL }).hasSize(2)

    }

    @Test
    fun `Should construct AvL panel from prior molecular`() {
        val priorMolecularTests = listOf(
            TestMolecularFactory.avlPanelPriorMolecularNoMutationsFoundRecord(),
            TestMolecularFactory.avlPanelPriorMolecularVariantRecord("gene", "c1A>T")
        )
        val molecularTests = GenericPanelMolecularTest.fromPriorMolecularTest(priorMolecularTests)

        val expected = GenericPanelMolecularTest(
            date = null,
            result = GenericPanel(GenericPanelType.AVL, variants = listOf(GenericVariant("gene", "c1A>T")))
        )
        assertThat(molecularTests).containsExactly(expected)
    }

    @Test
    fun `Should construct Freetext panel from prior molecular`() {
        val priorMolecularTests = listOf(TestMolecularFactory.freetextPriorMolecularFusionRecord("geneUp", "geneDown"))
        val molecularTests = GenericPanelMolecularTest.fromPriorMolecularTest(priorMolecularTests)

        val expected = GenericPanelMolecularTest(
            date = null,
            result = GenericPanel(
                GenericPanelType.FREE_TEXT,
                variants = emptyList(),
                fusions = listOf(GenericFusion("geneUp", "geneDown"))
            )
        )
        assertThat(molecularTests).containsExactly(expected)
    }

    @Test
    fun `Should throw exception on unextractable freetext record`() {
        val record = PriorMolecularTest(
            test = "Freetext",
            item = "KRAS A1Z",
            measure = null,
            impliesPotentialIndeterminateStatus = false
        )
        val priorMolecularTests = listOf(record)
        Assertions.assertThatThrownBy {
            GenericPanelMolecularTest.fromPriorMolecularTest(priorMolecularTests)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `Should convert and group all prior molecular tests`() {

        val IHCTests = listOf(
            PriorMolecularTest("IHC", item = "protein1", impliesPotentialIndeterminateStatus = false),
            PriorMolecularTest("IHC", item = "protein2", impliesPotentialIndeterminateStatus = false),
        )

        val genericPanelTests = listOf(
            TestMolecularFactory.avlPanelPriorMolecularNoMutationsFoundRecord(),
            TestMolecularFactory.freetextPriorMolecularFusionRecord("geneUp", "geneDown")
        )

        val otherTests = listOf(
            PriorMolecularTest("Future-Panel", item = "gene", impliesPotentialIndeterminateStatus = false)
        )

        val priorMolecularTests = IHCTests + genericPanelTests + otherTests

        val molecularTests = MolecularTestFactory.fromPriorMolecular(priorMolecularTests)
        assertThat(molecularTests).hasSize(5)
        assertThat(molecularTests.filter { it.type == ExperimentType.IHC }).hasSize(2)
        assertThat(molecularTests.filter { it.type == ExperimentType.GENERIC_PANEL }).hasSize(2)
        assertThat(molecularTests.filter { it.type == ExperimentType.OTHER }).hasSize(1)
    }*/
}