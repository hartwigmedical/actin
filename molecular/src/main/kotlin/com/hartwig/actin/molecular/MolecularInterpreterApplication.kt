package com.hartwig.actin.molecular

import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.datamodel.clinical.ClinicalRecord
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.SequencingTest
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.PanelRecord
import com.hartwig.actin.datamodel.molecular.PanelSpecifications
import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.EvidenceAnnotator
import com.hartwig.actin.molecular.evidence.EvidenceAnnotatorFactory
import com.hartwig.actin.molecular.evidence.known.KnownEventResolverFactory
import com.hartwig.actin.molecular.filter.GeneFilterFactory
import com.hartwig.actin.molecular.orange.MolecularRecordAnnotator
import com.hartwig.actin.molecular.orange.OrangeExtractor
import com.hartwig.actin.molecular.panel.IhcAnnotator
import com.hartwig.actin.molecular.panel.IhcExtractor
import com.hartwig.actin.molecular.panel.PanelAnnotator
import com.hartwig.actin.molecular.panel.PanelCopyNumberAnnotator
import com.hartwig.actin.molecular.panel.PanelDriverAttributeAnnotator
import com.hartwig.actin.molecular.panel.PanelFusionAnnotator
import com.hartwig.actin.molecular.panel.PanelVariantAnnotator
import com.hartwig.actin.molecular.panel.PanelVirusAnnotator
import com.hartwig.actin.molecular.paver.PaveRefGenomeVersion
import com.hartwig.actin.molecular.paver.Paver
import com.hartwig.actin.molecular.util.MolecularHistoryPrinter
import com.hartwig.actin.tools.pave.PaveLite
import com.hartwig.actin.tools.transvar.TransvarVariantAnnotatorFactory
import com.hartwig.hmftools.datamodel.orange.OrangeRefGenomeVersion
import com.hartwig.serve.datamodel.ServeDatabase
import com.hartwig.serve.datamodel.ServeRecord
import kotlinx.coroutines.runBlocking
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.system.exitProcess
import com.hartwig.actin.tools.ensemblcache.RefGenome as EnsemblRefGenome
import com.hartwig.serve.datamodel.RefGenome as ServeRefGenome

private val CLINICAL_TESTS_REF_GENOME_VERSION = RefGenomeVersion.V37

class MolecularInterpreterApplication(private val config: MolecularInterpreterConfig) {
    fun run() = runBlocking {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("resource load starting")
        val inputData = InputDataLoader.load(config, CLINICAL_TESTS_REF_GENOME_VERSION)
        LOGGER.info("resource load complete")

        val tumorDoids = inputData.clinical.tumor.doids.orEmpty().toSet()
        if (tumorDoids.isEmpty()) {
            LOGGER.warn(" No tumor DOIDs configured in ACTIN clinical data for {}!", inputData.clinical.patientId)
        } else {
            LOGGER.info(" Tumor DOIDs determined to be: {}", tumorDoids.joinToString(", "))
        }

        val orangeMolecularTests = interpretOrangeRecord(tumorDoids, inputData)
        val clinicalMolecularTests =
            interpretClinicalMolecularTests(config, inputData.clinical, tumorDoids, inputData)

        val history = MolecularHistory(orangeMolecularTests + clinicalMolecularTests)
        MolecularHistoryPrinter.print(history)

        val patientRecord = PatientRecordFactory.fromInputs(inputData.clinical, history)
        PatientRecordJson.write(patientRecord, config.outputDirectory)

        LOGGER.info("Done!")
    }

    private fun interpretOrangeRecord(
        tumorDoids: Set<String>,
        inputData: MolecularInterpreterInputData
    ): List<MolecularTest> {
        return if (inputData.orange != null) {
            val orangeRefGenomeVersion = fromOrangeRefGenomeVersion(inputData.orange.refGenomeVersion())
            val serveRecord = selectForRefGenomeVersion(inputData.serveDatabase, orangeRefGenomeVersion)

            LOGGER.info("Interpreting ORANGE record")
            val geneFilter = GeneFilterFactory.createFromKnownGenes(serveRecord.knownEvents().genes())
            MolecularInterpreter(
                OrangeExtractor(geneFilter, inputData.panelSpecifications),
                MolecularRecordAnnotator(KnownEventResolverFactory.create(serveRecord.knownEvents())),
                listOf(EvidenceAnnotatorFactory.createMolecularRecordAnnotator(serveRecord, inputData.doidEntry, tumorDoids))
            ).run(listOf(inputData.orange))
        } else {
            emptyList()
        }
    }

    private fun interpretClinicalMolecularTests(
        config: MolecularInterpreterConfig,
        clinical: ClinicalRecord,
        tumorDoids: Set<String>,
        inputData: MolecularInterpreterInputData
    ): List<MolecularTest> {
        LOGGER.info(
            "Creating evidence database for clinical molecular tests "
                    + "assuming ref genome version '$CLINICAL_TESTS_REF_GENOME_VERSION'"
        )
        val serveRecord = selectForRefGenomeVersion(inputData.serveDatabase, CLINICAL_TESTS_REF_GENOME_VERSION)

        LOGGER.info("Interpreting {} prior sequencing test(s)", clinical.sequencingTests.size)
        val geneDriverLikelihoodModel = GeneDriverLikelihoodModel(inputData.dndsDatabase)
        val variantAnnotator = TransvarVariantAnnotatorFactory.withRefGenome(
            toEnsemblRefGenomeVersion(CLINICAL_TESTS_REF_GENOME_VERSION), config.referenceGenomeFastaPath, inputData.ensemblDataCache
        )
        val paveRefGenomeVersion = toPaveRefGenomeVersion(CLINICAL_TESTS_REF_GENOME_VERSION)
        val paver = Paver(
            config.ensemblCachePath, config.referenceGenomeFastaPath, paveRefGenomeVersion, config.driverGenePanelPath, config.tempDir
        )
        val paveLite = PaveLite(inputData.ensemblDataCache, false)

        val panelVariantAnnotator = PanelVariantAnnotator(variantAnnotator, paver, paveLite)
        val panelFusionAnnotator = PanelFusionAnnotator(inputData.knownFusionCache, inputData.ensemblDataCache)
        val panelCopyNumberAnnotator = PanelCopyNumberAnnotator(inputData.ensemblDataCache)
        val panelVirusAnnotator = PanelVirusAnnotator()
        val panelDriverAttributeAnnotator =
            PanelDriverAttributeAnnotator(KnownEventResolverFactory.create(serveRecord.knownEvents()), geneDriverLikelihoodModel)
        val evidenceAnnotator = EvidenceAnnotatorFactory.createPanelRecordAnnotator(serveRecord, inputData.doidEntry, tumorDoids)

        val sequencingMolecularTests = interpretSequencingMolecularTests(
            clinical.sequencingTests,
            panelVariantAnnotator,
            panelFusionAnnotator,
            panelCopyNumberAnnotator,
            panelVirusAnnotator,
            panelDriverAttributeAnnotator,
            inputData.panelSpecifications,
            evidenceAnnotator
        )

        val ihcMolecularTests = interpretIhcMolecularTests(
            clinical.ihcTests,
            panelFusionAnnotator,
            evidenceAnnotator
        )

        LOGGER.info(
            "Completed interpretation of {} clinical molecular test(s) and {} IHC molecular tests",
            sequencingMolecularTests.size,
            ihcMolecularTests.size
        )
        return sequencingMolecularTests + ihcMolecularTests
    }

    private fun interpretSequencingMolecularTests(
        sequencingTests: List<SequencingTest>,
        panelVariantAnnotator: PanelVariantAnnotator,
        panelFusionAnnotator: PanelFusionAnnotator,
        panelCopyNumberAnnotator: PanelCopyNumberAnnotator,
        panelVirusAnnotator: PanelVirusAnnotator,
        panelDriverAttributeAnnotator: PanelDriverAttributeAnnotator,
        panelSpecifications: PanelSpecifications,
        panelRecordEvidenceAnnotator: EvidenceAnnotator<PanelRecord>
    ): List<MolecularTest> {
        return MolecularInterpreter(
            extractor = object : MolecularExtractor<SequencingTest, SequencingTest> {
                override fun extract(input: List<SequencingTest>): List<SequencingTest> {
                    return input
                }
            },
            annotator = PanelAnnotator(
                panelVariantAnnotator,
                panelFusionAnnotator,
                panelCopyNumberAnnotator,
                panelVirusAnnotator,
                panelDriverAttributeAnnotator,
                panelSpecifications
            ),
            postAnnotators = listOf(panelRecordEvidenceAnnotator)
        ).run(sequencingTests)
    }

    private fun interpretIhcMolecularTests(
        ihcTests: List<IhcTest>,
        panelFusionAnnotator: PanelFusionAnnotator,
        panelRecordEvidenceAnnotator: EvidenceAnnotator<PanelRecord>
    ): List<MolecularTest> {
        return MolecularInterpreter(
            extractor = IhcExtractor(),
            annotator = IhcAnnotator(panelFusionAnnotator),
            postAnnotators = listOf(panelRecordEvidenceAnnotator)
        ).run(ihcTests)
    }

    private fun selectForRefGenomeVersion(serveDatabase: ServeDatabase, refGenomeVersion: RefGenomeVersion): ServeRecord {
        return serveDatabase.records()[toServeRefGenomeVersion(refGenomeVersion)]
            ?: throw IllegalStateException("No serve record for ref genome version $refGenomeVersion")
    }

    private fun fromOrangeRefGenomeVersion(orangeRefGenomeVersion: OrangeRefGenomeVersion): RefGenomeVersion {
        return when (orangeRefGenomeVersion) {
            OrangeRefGenomeVersion.V37 -> {
                RefGenomeVersion.V37
            }

            OrangeRefGenomeVersion.V38 -> {
                RefGenomeVersion.V38
            }
        }
    }
    
    companion object {
        const val APPLICATION: String = "ACTIN Molecular Interpreter"

        val LOGGER: Logger = LogManager.getLogger(MolecularInterpreterApplication::class.java)
        private val VERSION = MolecularInterpreterApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

private fun toServeRefGenomeVersion(refGenomeVersion: RefGenomeVersion): ServeRefGenome {
    return when (refGenomeVersion) {
        RefGenomeVersion.V37 -> {
            ServeRefGenome.V37
        }

        RefGenomeVersion.V38 -> {
            ServeRefGenome.V38
        }
    }
}

private fun toPaveRefGenomeVersion(refGenomeVersion: RefGenomeVersion): PaveRefGenomeVersion {
    return when (refGenomeVersion) {
        RefGenomeVersion.V37 -> {
            PaveRefGenomeVersion.V37
        }

        RefGenomeVersion.V38 -> {
            PaveRefGenomeVersion.V38
        }
    }
}

fun toEnsemblRefGenomeVersion(refGenomeVersion: RefGenomeVersion): EnsemblRefGenome {
    return when (refGenomeVersion) {
        RefGenomeVersion.V37 -> {
            EnsemblRefGenome.V37
        }

        RefGenomeVersion.V38 -> {
            EnsemblRefGenome.V38
        }
    }
}

fun main(args: Array<String>) {
    val options: Options = MolecularInterpreterConfig.createOptions()
    val config: MolecularInterpreterConfig
    try {
        config = MolecularInterpreterConfig.createConfig(DefaultParser().parse(options, args))
    } catch (exception: ParseException) {
        MolecularInterpreterApplication.LOGGER.warn(exception)
        HelpFormatter().printHelp(MolecularInterpreterApplication.APPLICATION, options)
        exitProcess(1)
    }

    MolecularInterpreterApplication(config).run()
}
