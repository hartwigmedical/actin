package com.hartwig.actin.molecular

import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.configuration.MolecularConfiguration
import com.hartwig.actin.datamodel.clinical.ClinicalRecord
import com.hartwig.actin.datamodel.clinical.Gender
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.SequencingTest
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.actin.molecular.evidence.EvidenceAnnotator
import com.hartwig.actin.molecular.evidence.EvidenceAnnotatorFactory
import com.hartwig.actin.molecular.evidence.ServeLoader
import com.hartwig.actin.molecular.evidence.known.KnownEventResolverFactory
import com.hartwig.actin.molecular.orange.MolecularRecordAnnotator
import com.hartwig.actin.molecular.orange.OrangeExtractor
import com.hartwig.actin.molecular.panel.IhcAnnotator
import com.hartwig.actin.molecular.panel.IhcExtractor
import com.hartwig.actin.molecular.panel.PanelAnnotator
import com.hartwig.actin.molecular.panel.PanelCopyNumberAnnotator
import com.hartwig.actin.molecular.panel.PanelDriverAttributeAnnotator
import com.hartwig.actin.molecular.panel.PanelFusionAnnotator
import com.hartwig.actin.molecular.panel.PanelImmunologyAnnotator
import com.hartwig.actin.molecular.panel.PanelSpecifications
import com.hartwig.actin.molecular.panel.PanelVariantAnnotator
import com.hartwig.actin.molecular.panel.PanelVirusAnnotator
import com.hartwig.actin.molecular.paver.PaveRefGenomeVersion
import com.hartwig.actin.molecular.paver.Paver
import com.hartwig.actin.molecular.util.MolecularTestPrinter
import com.hartwig.actin.tools.pave.PaveLite
import com.hartwig.actin.tools.transvar.TransvarVariantAnnotatorFactory
import com.hartwig.actin.util.DatamodelPrinter
import com.hartwig.hmftools.datamodel.orange.OrangeRefGenomeVersion
import com.hartwig.serve.datamodel.ServeDatabase
import com.hartwig.serve.datamodel.ServeRecord
import kotlin.system.exitProcess
import kotlinx.coroutines.runBlocking
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import com.hartwig.actin.tools.ensemblcache.RefGenome as EnsemblRefGenome

val CLINICAL_TESTS_REF_GENOME_VERSION = RefGenomeVersion.V37

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

        val patientGender = inputData.clinical.patient.gender
        val orangeMolecularTests = interpretOrangeRecord(tumorDoids, inputData, patientGender)

        val clinicalMolecularTests = interpretClinicalMolecularTests(config, inputData.clinical, tumorDoids, inputData)

        val allTests = orangeMolecularTests + clinicalMolecularTests
        MolecularTestPrinter(DatamodelPrinter.withDefaultIndentation()).print(allTests)

        val patientRecord = PatientRecordFactory.fromInputs(inputData.clinical, allTests)
        PatientRecordJson.write(patientRecord, config.outputDirectory)

        LOGGER.info("Done!")
    }

    private fun interpretOrangeRecord(
        tumorDoids: Set<String>,
        inputData: MolecularInterpreterInputData,
        patientGender: Gender?
    ): List<MolecularTest> {
        return if (inputData.orange != null) {
            val orangeRefGenomeVersion = fromOrangeRefGenomeVersion(inputData.orange.refGenomeVersion())
            val serveRecord = selectForRefGenomeVersion(inputData.serveDatabase, orangeRefGenomeVersion)

            LOGGER.info("Interpreting ORANGE record")
            MolecularInterpreter(
                OrangeExtractor(inputData.geneFilter, inputData.panelSpecifications),
                MolecularRecordAnnotator(KnownEventResolverFactory.create(serveRecord.knownEvents())),
                listOf(EvidenceAnnotatorFactory.createMolecularRecordAnnotator(serveRecord, inputData.doidEntry, tumorDoids, patientGender))
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
        val variantAnnotator = TransvarVariantAnnotatorFactory.withRefGenome(
            toEnsemblRefGenomeVersion(CLINICAL_TESTS_REF_GENOME_VERSION), config.referenceGenomeFastaPath, inputData.ensemblDataCache
        )
        val paveRefGenomeVersion = toPaveRefGenomeVersion(CLINICAL_TESTS_REF_GENOME_VERSION)
        val paver = Paver(
            config.ensemblCachePath, config.referenceGenomeFastaPath, paveRefGenomeVersion, config.driverGenePanelPath, config.tempDir
        )
        val paveLite = PaveLite(inputData.ensemblDataCache, false)

        val configuration = MolecularConfiguration.create(config.overridesYaml)
        LOGGER.info("Loaded molecular config: $configuration")

        val panelVariantAnnotator = PanelVariantAnnotator(variantAnnotator, paver, paveLite)
        val panelFusionAnnotator = PanelFusionAnnotator(inputData.knownFusionCache, inputData.ensemblDataCache, configuration)
        val panelCopyNumberAnnotator = PanelCopyNumberAnnotator(inputData.ensemblDataCache)
        val panelVirusAnnotator = PanelVirusAnnotator(configuration)

        val panelDriverAttributeAnnotator = PanelDriverAttributeAnnotator(
            KnownEventResolverFactory.create(serveRecord.knownEvents()),
            inputData.dndsDatabase,
            configuration
        )

        val patientGender = clinical.patient.gender
        val evidenceAnnotator =
            EvidenceAnnotatorFactory.createPanelRecordAnnotator(serveRecord, inputData.doidEntry, tumorDoids, patientGender)

        val sequencingMolecularTests = interpretSequencingMolecularTests(
            clinical,
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
        clinical: ClinicalRecord,
        panelVariantAnnotator: PanelVariantAnnotator,
        panelFusionAnnotator: PanelFusionAnnotator,
        panelCopyNumberAnnotator: PanelCopyNumberAnnotator,
        panelVirusAnnotator: PanelVirusAnnotator,
        panelDriverAttributeAnnotator: PanelDriverAttributeAnnotator,
        panelSpecifications: PanelSpecifications,
        panelRecordEvidenceAnnotator: EvidenceAnnotator
    ): List<MolecularTest> {
        return MolecularInterpreter(
            extractor = object : MolecularExtractor<SequencingTest, SequencingTest> {
                override fun extract(input: List<SequencingTest>): List<SequencingTest> {
                    return input
                }
            },
            annotator = PanelAnnotator(
                clinical.patient.registrationDate,
                panelVariantAnnotator,
                panelFusionAnnotator,
                panelCopyNumberAnnotator,
                panelVirusAnnotator,
                PanelImmunologyAnnotator(),
                panelDriverAttributeAnnotator,
                panelSpecifications
            ),
            postAnnotators = listOf(panelRecordEvidenceAnnotator)
        ).run(clinical.sequencingTests)
    }

    private fun interpretIhcMolecularTests(
        ihcTests: List<IhcTest>,
        panelRecordEvidenceAnnotator: EvidenceAnnotator
    ): List<MolecularTest> {
        return MolecularInterpreter(
            extractor = IhcExtractor(),
            annotator = IhcAnnotator(),
            postAnnotators = listOf(panelRecordEvidenceAnnotator)
        ).run(ihcTests)
    }

    private fun selectForRefGenomeVersion(serveDatabase: ServeDatabase, refGenomeVersion: RefGenomeVersion): ServeRecord {
        return serveDatabase.records()[ServeLoader.toServeRefGenomeVersion(refGenomeVersion)]
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

    companion object {
        const val APPLICATION: String = "ACTIN Molecular Interpreter"

        val LOGGER: Logger = LogManager.getLogger(MolecularInterpreterApplication::class.java)
        private val VERSION = MolecularInterpreterApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
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
