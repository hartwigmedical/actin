package com.hartwig.actin.algo

import com.hartwig.actin.PatientPrinter
import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.algo.calendar.ReferenceDateProviderFactory
import com.hartwig.actin.algo.ckb.EfficacyEntryFactory
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.algo.soc.ResistanceEvidenceMatcher
import com.hartwig.actin.algo.util.TreatmentMatchPrinter
import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.actin.datamodel.trial.Trial
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.icd.serialization.CsvReader
import com.hartwig.actin.icd.serialization.IcdDeserializer
import com.hartwig.actin.medication.AtcTree
import com.hartwig.actin.medication.MedicationCategories
import com.hartwig.actin.molecular.evidence.ServeLoader
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatcher
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import com.hartwig.actin.trial.input.FunctionInputResolver
import com.hartwig.actin.trial.serialization.TrialJson
import com.hartwig.serve.datamodel.RefGenome
import com.hartwig.serve.datamodel.ServeRecord
import com.hartwig.serve.datamodel.serialization.ServeJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.time.Period
import kotlin.system.exitProcess

private data class DataResources(
    val patient: PatientRecord,
    val doidModel: DoidModel,
    val icdModel: IcdModel,
    val trials: List<Trial>,
    val atcTree: AtcTree,
    val treatmentDatabase: TreatmentDatabase,
    val serveRecord: ServeRecord
)

class TreatmentMatcherApplication(private val config: TreatmentMatcherConfig) {

    fun run() = runBlocking {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("Loading data")
        val dataResources = loadParallel(config)
        LOGGER.info("Loading complete")

        val referenceDateProvider = ReferenceDateProviderFactory.create(dataResources.patient, config.runHistorically)
        LOGGER.info("Matching patient to available trials")

        // We assume we never check validity of a gene inside algo.
        val molecularInputChecker = MolecularInputChecker.createAnyGeneValid()
        val treatmentDatabase = TreatmentDatabaseFactory.createFromPath(config.treatmentDirectory)
        val functionInputResolver =
            FunctionInputResolver(dataResources.doidModel, dataResources.icdModel, molecularInputChecker,
                treatmentDatabase, MedicationCategories.create(dataResources.atcTree))
        val configuration = EnvironmentConfiguration.create(config.overridesYaml).algo
        LOGGER.info(" Loaded algo config: $configuration")

        val maxMolecularTestAge = configuration.maxMolecularTestAgeInDays?.let { referenceDateProvider.date().minus(Period.ofDays(it)) }
        val resources = RuleMappingResources(
            referenceDateProvider = referenceDateProvider,
            doidModel = dataResources.doidModel,
            icdModel = dataResources.icdModel,
            functionInputResolver = functionInputResolver,
            atcTree = dataResources.atcTree,
            treatmentDatabase = treatmentDatabase,
            personalizationDataPath = config.personalizationDataPath,
            treatmentEfficacyPredictionJson = config.treatmentEfficacyPredictionJson,
            algoConfiguration = configuration,
            maxMolecularTestAge = maxMolecularTestAge
        )
        val evidenceEntries = EfficacyEntryFactory(treatmentDatabase).extractEfficacyEvidenceFromCkbFile(config.extendedEfficacyJson)

        val resistanceEvidenceMatcher =
            ResistanceEvidenceMatcher.create(
                doidModel = dataResources.doidModel,
                tumorDoids = dataResources.patient.tumor.doids.orEmpty().toSet(),
                evidences = dataResources.serveRecord.evidences(),
                treatmentDatabase = treatmentDatabase,
                molecularHistory = dataResources.patient.molecularHistory,
                actionabilityMatcher = ActionabilityMatcher(dataResources.serveRecord.evidences(), dataResources.serveRecord.trials())
            )

        val treatmentMatcher = TreatmentMatcher.create(resources, dataResources.trials, evidenceEntries, resistanceEvidenceMatcher, maxMolecularTestAge)
        val treatmentMatch = treatmentMatcher.run(dataResources.patient)

        LOGGER.info("Printing treatment match")
        TreatmentMatchPrinter.printMatch(treatmentMatch)
        TreatmentMatchJson.write(treatmentMatch, config.outputDirectory)
        LOGGER.info("Done!")
    }

    private suspend fun loadParallel(config: TreatmentMatcherConfig): DataResources = coroutineScope {
        val deferredPatient = async {
            withContext(Dispatchers.IO) {
                LOGGER.info("Loading patient record from {}", config.patientRecordJson)
                val patient = PatientRecordJson.read(config.patientRecordJson)
                PatientPrinter.printRecord(patient)
                patient
            }
        }
        val deferredTrials = async {
            withContext(Dispatchers.IO) {
                LOGGER.info("Loading trials from {}", config.trialDatabaseDirectory)
                val trials = TrialJson.readFromDir(config.trialDatabaseDirectory)
                LOGGER.info(" Loaded {} trials", trials.size)
                trials
            }
        }
        val deferredDoidEntry = async {
            withContext(Dispatchers.IO) {
                LOGGER.info("Loading DOID tree from {}", config.doidJson)
                val doidEntry = DoidJson.readDoidOwlEntry(config.doidJson)
                LOGGER.info(" Loaded {} nodes from DOID tree", doidEntry.nodes.size)
                doidEntry
            }
        }
        val deferredIcdNodes = async {
            withContext(Dispatchers.IO) {
                LOGGER.info("Creating ICD-11 tree from file {}", config.icdTsv)
                val icdNodes = IcdDeserializer.deserialize(CsvReader.readFromFile(config.icdTsv))
                LOGGER.info(" Loaded {} nodes from ICD-11 tree", icdNodes.size)
                icdNodes
            }
        }
        val deferredAtcTree = async {
            withContext(Dispatchers.IO) {
                LOGGER.info("Creating ATC tree from file {}", config.atcTsv)
                AtcTree.createFromFile(config.atcTsv)
            }
        }
        val deferredTreatmentDatabase = async {
            withContext(Dispatchers.IO) {
                TreatmentDatabaseFactory.createFromPath(config.treatmentDirectory)
            }
        }
        val deferredServeDatabase = async {
            withContext(Dispatchers.IO) {
                val serveJsonFilePath = ServeJson.jsonFilePath(config.serveDirectory)
                LOGGER.info("Loading SERVE database for resistance evidence from {}", serveJsonFilePath)
                val serveDatabase = ServeLoader.loadServeDatabase(serveJsonFilePath)
                LOGGER.info(" Loaded SERVE version {}", serveDatabase.version())
                serveDatabase
            }
        }

        val patient = deferredPatient.await()
        val trials = deferredTrials.await()
        val doidEntry = deferredDoidEntry.await()
        val icdNodes = deferredIcdNodes.await()
        val atcTree = deferredAtcTree.await()
        val treatmentDatabase = deferredTreatmentDatabase.await()
        val serveDatabase = deferredServeDatabase.await()

        val doidModel = DoidModelFactory.createFromDoidEntry(doidEntry)
        val icdModel = IcdModel.create(icdNodes)

        val refGenomeVersion = patient.molecularHistory.latestOrangeMolecularRecord()?.refGenomeVersion ?: RefGenomeVersion.V37
        val serveRefGenomeVersion = when (refGenomeVersion) {
            RefGenomeVersion.V37 -> {
                RefGenome.V37
            }

            RefGenomeVersion.V38 -> {
                RefGenome.V38
            }
        }
        val serveRecord = serveDatabase.records()[serveRefGenomeVersion]
            ?: throw IllegalStateException("No serve record for ref genome version $serveRefGenomeVersion")
        LOGGER.info(" Loaded {} evidences from SERVE", serveRecord.evidences().size)

        DataResources(
            patient = patient,
            doidModel = doidModel,
            icdModel = icdModel,
            trials = trials,
            atcTree = atcTree,
            treatmentDatabase = treatmentDatabase,
            serveRecord = serveRecord
        )
    }

    companion object {
        const val APPLICATION = "ACTIN Treatment Matcher"

        val LOGGER: Logger = LogManager.getLogger(TreatmentMatcherApplication::class.java)
        private val VERSION = TreatmentMatcherApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

fun main(args: Array<String>) {
    val options: Options = TreatmentMatcherConfig.createOptions()
    val config: TreatmentMatcherConfig
    try {
        config = TreatmentMatcherConfig.createConfig(DefaultParser().parse(options, args))
    } catch (exception: ParseException) {
        TreatmentMatcherApplication.LOGGER.error(exception)
        HelpFormatter().printHelp(TreatmentMatcherApplication.APPLICATION, options)
        exitProcess(1)
    }

    TreatmentMatcherApplication(config).run()
}