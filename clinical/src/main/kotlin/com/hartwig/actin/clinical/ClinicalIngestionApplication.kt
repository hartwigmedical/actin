package com.hartwig.actin.clinical

import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.clinical.correction.QuestionnaireCorrection
import com.hartwig.actin.clinical.correction.QuestionnaireRawEntryMapper
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.CurationValidator
import com.hartwig.actin.clinical.curation.extraction.BloodTransfusionsExtractor
import com.hartwig.actin.clinical.curation.extraction.ClinicalStatusExtractor
import com.hartwig.actin.clinical.curation.extraction.ComplicationsExtractor
import com.hartwig.actin.clinical.curation.extraction.IntoleranceExtractor
import com.hartwig.actin.clinical.curation.extraction.LabValueExtractor
import com.hartwig.actin.clinical.curation.extraction.MedicationExtractor
import com.hartwig.actin.clinical.curation.extraction.PriorMolecularTestsExtractor
import com.hartwig.actin.clinical.curation.extraction.PriorOtherConditionsExtractor
import com.hartwig.actin.clinical.curation.extraction.PriorSecondPrimaryExtractor
import com.hartwig.actin.clinical.curation.extraction.ToxicityExtractor
import com.hartwig.actin.clinical.curation.extraction.TreatmentHistoryExtractor
import com.hartwig.actin.clinical.curation.extraction.TumorDetailsExtractor
import com.hartwig.actin.clinical.curation.translation.TranslationDatabaseReader
import com.hartwig.actin.clinical.feed.ClinicalFeedReader
import com.hartwig.actin.clinical.feed.FeedModel
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.util.json.GsonSerializer
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

class ClinicalIngestionApplication(private val config: ClinicalIngestionConfig) {

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)
        LOGGER.info("Loading DOID tree from {}", config.doidJson)
        val doidEntry = DoidJson.readDoidOwlEntry(config.doidJson)
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes().size)

        val treatmentDatabase = TreatmentDatabaseFactory.createFromPath(config.treatmentDirectory)

        LOGGER.info("Creating clinical curation database from directory {}", config.curationDirectory)
        val curationValidator = CurationValidator(DoidModelFactory.createFromDoidEntry(doidEntry))

        LOGGER.info("Creating ATC model from file {}", config.atcTsv)
        val atcModel = WhoAtcModel.createFromFile(config.atcTsv)

        LOGGER.info("Creating clinical feed model from directory {}", config.feedDirectory)
        val clinicalFeed = ClinicalFeedReader.read(config.feedDirectory)
        val feedModel = FeedModel(
            clinicalFeed.copy(
                questionnaireEntries = QuestionnaireCorrection.correctQuestionnaires(
                    clinicalFeed.questionnaireEntries, QuestionnaireRawEntryMapper.createFromCurationDirectory(config.curationDirectory)
                )
            )
        )
        val curationDatabaseReader = CurationDatabaseReader(config.curationDirectory, curationValidator, treatmentDatabase)
        val translationDatabaseReader = TranslationDatabaseReader(config.curationDirectory)
        val clinicalIngestion = ClinicalIngestion(
            feed = feedModel,
            priorSecondPrimaryExtractor = PriorSecondPrimaryExtractor(
                curationDatabaseReader.secondPrimary(),
                curationDatabaseReader.treatment()
            ),
            tumorDetailsExtractor = TumorDetailsExtractor(
                curationDatabaseReader.lesionLocation(),
                curationDatabaseReader.primaryTumor()
            ),
            complicationsExtractor = ComplicationsExtractor(
                curationDatabaseReader.complication()
            ),
            clinicalStatusExtractor = ClinicalStatusExtractor(
                curationDatabaseReader.ecg(),
                curationDatabaseReader.infection(),
                curationDatabaseReader.nonOncologicalHistory(curationValidator)
            ),
            treatmentHistoryExtractor = TreatmentHistoryExtractor(
                curationDatabaseReader.treatment(),
                curationDatabaseReader.secondPrimary()
            ),
            bloodTransfusionsExtractor = BloodTransfusionsExtractor(translationDatabaseReader.bloodTransfusions()),
            priorMolecularTestsExtractor = PriorMolecularTestsExtractor(
                curationDatabaseReader.molecularTest()
            ),
            toxicityExtractor = ToxicityExtractor(
                curationDatabaseReader.toxicity(),
                translationDatabaseReader.toxicity()
            ),
            intoleranceExtractor = IntoleranceExtractor(
                curationDatabaseReader.intolerance(curationValidator)
            ),
            priorOtherConditionExtractor = PriorOtherConditionsExtractor(
                curationDatabaseReader.nonOncologicalHistory(curationValidator)
            ),
            medicationExtractor = MedicationExtractor(
                curationDatabaseReader.medicationName(),
                curationDatabaseReader.medicationDosage(),
                curationDatabaseReader.periodBetweenUnit(),
                curationDatabaseReader.cypInteraction(),
                curationDatabaseReader.qtProlongating(),
                translationDatabaseReader.administrationRoute(),
                translationDatabaseReader.dosageUnit(),
                atcModel
            ),
            labValueExtractor = LabValueExtractor(
                translationDatabaseReader.labratoryTranslation()
            )
        )


        val patientResults =
            clinicalIngestion.run()
        val outputDirectory = config.outputDirectory
        LOGGER.info("Writing {} clinical records to {}", patientResults.size, outputDirectory)
        ClinicalRecordJson.write(patientResults.map { it.clinicalRecord }, outputDirectory)
        LOGGER.info("Done!")

        writeIngestionResults(
            outputDirectory,
            IngestionResult(status = IngestionStatus.PASS, curationValidationErrors = emptyList(), patientResults = patientResults)
        )

        if (patientResults.any { it.curationResults.isNotEmpty() }) {
            LOGGER.warn("Summary of warnings:")
            patientResults.forEach {
                if (it.curationResults.isNotEmpty()) {
                    LOGGER.warn("Curation warnings for patient ${it.patientId}")
                    it.curationResults.flatMap { result -> result.requirements }.forEach { requirement ->
                        LOGGER.warn(requirement.message)
                    }
                }
            }
            LOGGER.warn("Summary complete.")
        }

    }

    private fun writeIngestionResults(outputDirectory: String, results: IngestionResult) {
        val resultsJson = Paths.get(outputDirectory).resolve("clinical_ingestion_results.json")
        LOGGER.info("Writing {} ingestion results to {}", results.patientResults.size, resultsJson)
        Files.write(
            resultsJson,
            GsonSerializer.create().toJson(results).toByteArray()
        )
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(ClinicalIngestionApplication::class.java)
        const val APPLICATION = "ACTIN Clinical Ingestion"
        private val VERSION = ClinicalIngestionApplication::class.java.getPackage().implementationVersion
    }
}

fun main(args: Array<String>) {
    val options: Options = ClinicalIngestionConfig.createOptions()
    val config: ClinicalIngestionConfig

    try {
        config = ClinicalIngestionConfig.createConfig(DefaultParser().parse(options, args))
    } catch (exception: ParseException) {
        ClinicalIngestionApplication.LOGGER.warn(exception)
        HelpFormatter().printHelp(ClinicalIngestionApplication.APPLICATION, options)
        exitProcess(1)
    }

    try {
        ClinicalIngestionApplication(config).run()
    } catch (e: Exception) {
        ClinicalIngestionApplication.LOGGER.error("${ClinicalIngestionApplication.APPLICATION} failed on an unrecoverable error", e)
        exitProcess(1)
    }
}
