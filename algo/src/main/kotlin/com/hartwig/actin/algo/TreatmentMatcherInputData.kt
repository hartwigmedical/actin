import com.hartwig.actin.PatientPrinter
import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.algo.TreatmentMatcherConfig
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
import com.hartwig.actin.molecular.evidence.ServeLoader
import com.hartwig.actin.trial.serialization.TrialJson
import com.hartwig.serve.datamodel.RefGenome
import com.hartwig.serve.datamodel.ServeRecord
import com.hartwig.serve.datamodel.serialization.ServeJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

data class TreatmentMatcherInputData(
    val patient: PatientRecord,
    val doidModel: DoidModel,
    val icdModel: IcdModel,
    val trials: List<Trial>,
    val atcTree: AtcTree,
    val treatmentDatabase: TreatmentDatabase,
    val serveRecord: ServeRecord
)

object InputDataLoader {
    val LOGGER: Logger = LogManager.getLogger(InputDataLoader::class.java)

    suspend fun load(config: TreatmentMatcherConfig): TreatmentMatcherInputData = coroutineScope {
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

        TreatmentMatcherInputData(
            patient = patient,
            doidModel = doidModel,
            icdModel = icdModel,
            trials = trials,
            atcTree = atcTree,
            treatmentDatabase = treatmentDatabase,
            serveRecord = serveRecord
        )
    }
}