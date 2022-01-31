## ACTIN-Molecular

ACTIN-Molecular interprets molecular results and maps to the datamodel described below. This molecular model is written to a per-sample 
json file. Currently, ACTIN-Molecular only supports interpretation of [ORANGE](https://github.com/hartwigmedical/hmftools/tree/master/orange) 
results as produced by [HMF Platinum](https://github.com/hartwigmedical/platinum) 

This application requires Java 11+ and can be run as follows: 

```
java -cp actin.jar com.hartwig.actin.molecular.orange.OrangeInterpreterApplication \
   -orange_json /path/to/orange.json
   -output_directory /path/to/where/molecular_json_file_is_written
```
## Molecular Datamodel

The following general fields about a molecular experiment can be populated per sample

Field | Example Value | Details
---|---|---
sampleId | ACTN01029999T | Unique identifier for the sample / biopsy.
type | WGS | The type of molecular experiment done. Currently only 'WGS' is supported.
date | 2022-01-14 | The date on which the molecular results were obtained (optional field).
hasReliabilityQuality | 1 | Indicates whether the molecular results can be trusted or need to be interpreted with caution. 
 
The following data is used for matching against ACTIN's treatment eligibility criteria.
Do note that ACTIN itself does not make assumptions about the exact definition of the terms below. 
These decisions are all up to the algorithm interpreting the molecular data and converting this interpretation to the datamodel 
that is described here.

Field | Example Value | Details
---|---|---
mutations | BRAF V600E, EGFR exon 19 deletion | A list of gene-specific mutations.
activatedGenes | KRAS, NRAS | A list of genes considered to be activated.
inactivatedGenes | TP53, RB1 | A list of genes considered to be inactivated (along with a boolean whether they have been deleted completely).
amplifiedGenes | MYC | A list of genes considered to be amplified.
wildtypeGenes | BRAF | A list of genes considered to be wildtype. 
fusions | EML4-ALK | A list of genes considered to be fused together. 
isMicrosatelliteUnstable | 0 | If 1, sample is considered microsatellite unstable
isHomologousRepairDeficient | 0 | If 1, sample is considered homologous repair deficient.
tumorMutationalBurden | 14.2 | Number of mutations in the genome per Mb.
tumorMutationalLoad | 115 | Number of missense mutations across the genome.

The following data is not used in ACTIN's treatment matching but can be used to provide additional context in the ACTIN report. 
Along with evidence matched against the ACTIN treatment database, the datamodel can hold additional trial and general evidence from 
external sources.

Field | Example Value | Details
---|---|---
actinTreatmentEvidence | BRAF V600E -> Trial A | A list of mutations along with the treatment they are associated with within the ACTIN treatment database.
generalTrialSource | CKB | The name of the source that has been used for general trial evidence
generalTrialEvidence | High TMB -> Trial B | A list of mutations along with the trial they are associated with.
generalEvidenceSource | CKB | The name of the source used for general evidence
generalResponsiveEvidence | PIK3CA E545K -> Alpelisib | A list of mutations along with responsive evidence for treatment.
generalResistanceEvidence | KRAS amp -> Erlotinib | A list of mutations along with resistance evidence for treatment.  

### Interpretation of ORANGE results

### Version History and Download Links
 - Upcoming (first release) 
