package com.hartwig.actin.molecular

import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.datamodel.clinical.ClinicalRecord
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.SequencingTest
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.PanelSpecifications
import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.actin.doid.datamodel.DoidEntry
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.molecular.driverlikelihood.DndsDatabase
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.EvidenceAnnotatorFactory
import com.hartwig.actin.molecular.evidence.EvidenceDatabaseFactory
import com.hartwig.actin.molecular.evidence.EvidenceRegressionReporter
import com.hartwig.actin.molecular.evidence.ServeLoader
import com.hartwig.actin.molecular.filter.GeneFilterFactory
import com.hartwig.actin.molecular.orange.MolecularRecordAnnotator
import com.hartwig.actin.molecular.orange.OrangeExtractor
import com.hartwig.actin.molecular.panel.IhcAnnotator
import com.hartwig.actin.molecular.panel.IhcExtractor
import com.hartwig.actin.molecular.panel.PanelAnnotator
import com.hartwig.actin.molecular.panel.PanelCopyNumberAnnotator
import com.hartwig.actin.molecular.panel.PanelDriverAttributeAnnotator
import com.hartwig.actin.molecular.panel.PanelEvidenceAnnotator
import com.hartwig.actin.molecular.panel.PanelFusionAnnotator
import com.hartwig.actin.molecular.panel.PanelSpecificationsFile
import com.hartwig.actin.molecular.panel.PanelVariantAnnotator
import com.hartwig.actin.molecular.paver.PaveRefGenomeVersion
import com.hartwig.actin.molecular.paver.Paver
import com.hartwig.actin.molecular.util.MolecularHistoryPrinter
import com.hartwig.actin.tools.ensemblcache.EnsemblDataLoader
import com.hartwig.actin.tools.pave.PaveLite
import com.hartwig.actin.tools.transvar.TransvarVariantAnnotatorFactory
import com.hartwig.hmftools.common.fusion.KnownFusionCache
import com.hartwig.hmftools.datamodel.OrangeJson
import com.hartwig.hmftools.datamodel.orange.OrangeRefGenomeVersion
import com.hartwig.serve.datamodel.ServeDatabase
import com.hartwig.serve.datamodel.ServeRecord
import com.hartwig.serve.datamodel.serialization.ServeJson
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

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("Loading clinical json from {}", config.clinicalJson)
        val clinical = ClinicalRecordJson.read(config.clinicalJson)

        val tumorDoids = clinical.tumor.doids.orEmpty().toSet()
        if (tumorDoids.isEmpty()) {
            LOGGER.warn(" No tumor DOIDs configured in ACTIN clinical data for {}!", clinical.patientId)
        } else {
            LOGGER.info(" Tumor DOIDs determined to be: {}", tumorDoids.joinToString(", "))
        }

        LOGGER.info("Loading DOID tree from {}", config.doidJson)
        val doidEntry = DoidJson.readDoidOwlEntry(config.doidJson)
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes.size)

        val serveJsonFilePath = ServeJson.jsonFilePath(config.serveDirectory)
        LOGGER.info("Loading SERVE database from {}", serveJsonFilePath)
        val serveDatabase = ServeLoader.loadServeDatabase(serveJsonFilePath)
        LOGGER.info(" Loaded evidence and known events from SERVE version {}", serveDatabase.version())

        LOGGER.info("Loading panel specifications from {}", config.panelSpecificationsFilePath)
        val panelSpecifications =
            config.panelSpecificationsFilePath?.let { PanelSpecificationsFile.create(it) } ?: PanelSpecifications(emptyMap())

        val orangeMolecularTests = interpretOrangeRecord(config, serveDatabase, doidEntry, tumorDoids, panelSpecifications)
        val clinicalMolecularTests =
            interpretClinicalMolecularTests(config, clinical, serveDatabase, doidEntry, tumorDoids, panelSpecifications)

        val history = MolecularHistory(orangeMolecularTests + clinicalMolecularTests)
        MolecularHistoryPrinter.print(history)

        val patientRecord = PatientRecordFactory.fromInputs(clinical, history)
        PatientRecordJson.write(patientRecord, config.outputDirectory)

        LOGGER.info("Done!")
    }

    private fun interpretOrangeRecord(
        config: MolecularInterpreterConfig,
        serveDatabase: ServeDatabase,
        doidEntry: DoidEntry,
        tumorDoids: Set<String>,
        panelSpecifications: PanelSpecifications
    ): List<MolecularTest> {
        return if (config.orangeJson != null) {
            LOGGER.info("Reading ORANGE json from {}", config.orangeJson)
            val orange = OrangeJson.getInstance().read(config.orangeJson)
            val orangeRefGenomeVersion = fromOrangeRefGenomeVersion(orange.refGenomeVersion())

            val serveRecord = selectForRefGenomeVersion(serveDatabase, orangeRefGenomeVersion)
            val evidenceDatabase = EvidenceDatabaseFactory.create(serveRecord, doidEntry, tumorDoids)
            val evidenceAnnotator = EvidenceAnnotatorFactory.create(serveRecord, doidEntry, tumorDoids)

            LOGGER.info("Interpreting ORANGE record")
            val geneFilter = GeneFilterFactory.createFromKnownGenes(serveRecord.knownEvents().genes())
            val orangeRecordMolecularRecordMolecularInterpreter =
                MolecularInterpreter(OrangeExtractor(geneFilter, panelSpecifications), MolecularRecordAnnotator(evidenceDatabase))

            val orangeMolecularTests = orangeRecordMolecularRecordMolecularInterpreter.run(listOf(orange))
            val testsWithUpdatedEvidence = orangeMolecularTests.map { evidenceAnnotator.annotate(it) }

            orangeMolecularTests.zip(testsWithUpdatedEvidence).map { (oldTest, newTest) -> EvidenceRegressionReporter.report(oldTest, newTest) }
            testsWithUpdatedEvidence
        } else {
            emptyList()
        }
    }

    private fun interpretClinicalMolecularTests(
        config: MolecularInterpreterConfig,
        clinical: ClinicalRecord,
        serveDatabase: ServeDatabase,
        doidEntry: DoidEntry,
        tumorDoids: Set<String>,
        panelSpecifications: PanelSpecifications
    ): List<MolecularTest> {
        LOGGER.info(
            "Creating evidence database for clinical molecular tests "
                    + "assuming ref genome version '$CLINICAL_TESTS_REF_GENOME_VERSION'"
        )
        val serveRecord = selectForRefGenomeVersion(serveDatabase, CLINICAL_TESTS_REF_GENOME_VERSION)
        val evidenceDatabase = EvidenceDatabaseFactory.create(serveRecord, doidEntry, tumorDoids)

        val ensemblRefGenomeVersion = toEnsemblRefGenomeVersion(CLINICAL_TESTS_REF_GENOME_VERSION)
        LOGGER.info("Loading ensemble cache from ${config.ensemblCachePath}")
        val ensemblDataCache = EnsemblDataLoader.load(config.ensemblCachePath, ensemblRefGenomeVersion)

        LOGGER.info(
            "Loading dnds database for driver likelihood annotation from " +
                    "${config.oncoDndsDatabasePath} and ${config.tsgDndsDatabasePath}"
        )
        val dndsDatabase = DndsDatabase.create(config.oncoDndsDatabasePath, config.tsgDndsDatabasePath)

        LOGGER.info("Loading known fusions from " + config.knownFusionsPath)
        val knownFusionCache = KnownFusionCache()
        if (!knownFusionCache.loadFromFile(config.knownFusionsPath)) {
            throw IllegalArgumentException("Failed to load known fusions from ${config.knownFusionsPath}")
        }

        LOGGER.info("Interpreting {} prior sequencing test(s)", clinical.sequencingTests.size)
        val geneDriverLikelihoodModel = GeneDriverLikelihoodModel(dndsDatabase)
        val variantAnnotator = TransvarVariantAnnotatorFactory.withRefGenome(
            ensemblRefGenomeVersion, config.referenceGenomeFastaPath, ensemblDataCache
        )
        val paveRefGenomeVersion = toPaveRefGenomeVersion(CLINICAL_TESTS_REF_GENOME_VERSION)
        val paver = Paver(
            config.ensemblCachePath, config.referenceGenomeFastaPath, paveRefGenomeVersion, config.driverGenePanelPath, config.tempDir
        )
        val paveLite = PaveLite(ensemblDataCache, false)

        val panelVariantAnnotator = PanelVariantAnnotator(variantAnnotator, paver, paveLite)
        val panelFusionAnnotator = PanelFusionAnnotator(knownFusionCache, ensemblDataCache)
        val panelCopyNumberAnnotator = PanelCopyNumberAnnotator(ensemblDataCache)
        val panelDriverAttributeAnnotator = PanelDriverAttributeAnnotator(evidenceDatabase, geneDriverLikelihoodModel)
        val panelEvidenceAnnotator = PanelEvidenceAnnotator(evidenceDatabase)
        val evidenceAnnotator = EvidenceAnnotatorFactory.create(serveRecord, doidEntry, tumorDoids)

        val sequencingMolecularTests = interpretSequencingMolecularTests(
            clinical.sequencingTests,
            panelVariantAnnotator,
            panelFusionAnnotator,
            panelCopyNumberAnnotator,
            panelDriverAttributeAnnotator,
            panelEvidenceAnnotator,
            panelSpecifications
        )

        val ihcMolecularTests = interpretIhcMolecularTests(
            clinical.ihcTests,
            panelFusionAnnotator
        )

        val allTests = sequencingMolecularTests + ihcMolecularTests
        val molecularTestsWithUpdatedEvidence = allTests.map { evidenceAnnotator.annotate(it) }

        allTests.zip(molecularTestsWithUpdatedEvidence).forEach { (oldTest, newTest) ->
            EvidenceRegressionReporter.report(oldTest, newTest)
        }

        LOGGER.info("Completed interpretation of {} clinical molecular test(s)", sequencingMolecularTests.size)
        return molecularTestsWithUpdatedEvidence
    }

    private fun interpretSequencingMolecularTests(
        sequencingTests: List<SequencingTest>,
        panelVariantAnnotator: PanelVariantAnnotator,
        panelFusionAnnotator: PanelFusionAnnotator,
        panelCopyNumberAnnotator: PanelCopyNumberAnnotator,
        panelDriverAttributeAnnotator: PanelDriverAttributeAnnotator,
        panelEvidenceAnnotator: PanelEvidenceAnnotator,
        panelSpecifications: PanelSpecifications
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
                panelDriverAttributeAnnotator,
                panelEvidenceAnnotator,
                panelSpecifications
            ),
        ).run(sequencingTests)
    }

    private fun interpretIhcMolecularTests(
        ihcTests: List<IhcTest>,
        panelFusionAnnotator: PanelFusionAnnotator
    ): List<MolecularTest> {
        return MolecularInterpreter(
            extractor = IhcExtractor(),
            annotator = IhcAnnotator(panelFusionAnnotator),
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

    private fun toEnsemblRefGenomeVersion(refGenomeVersion: RefGenomeVersion): EnsemblRefGenome {
        return when (refGenomeVersion) {
            RefGenomeVersion.V37 -> {
                EnsemblRefGenome.V37
            }

            RefGenomeVersion.V38 -> {
                EnsemblRefGenome.V38
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
