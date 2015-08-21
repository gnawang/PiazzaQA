package info.ephyra;

import info.ephyra.answerselection.AnswerSelection;
import info.ephyra.answerselection.filters.*;
import info.ephyra.io.Logger;
import info.ephyra.io.MsgPrinter;
import info.ephyra.nlp.LingPipe;
import info.ephyra.nlp.NETagger;
import info.ephyra.nlp.OpenNLP;
import info.ephyra.nlp.SnowballStemmer;
import info.ephyra.nlp.StanfordNeTagger;
import info.ephyra.nlp.StanfordParser;
import info.ephyra.nlp.indices.FunctionWords;
import info.ephyra.nlp.indices.IrregularVerbs;
import info.ephyra.nlp.indices.Prepositions;
import info.ephyra.nlp.indices.WordFrequencies;
import info.ephyra.nlp.semantics.ontologies.Ontology;
import info.ephyra.nlp.semantics.ontologies.WordNet;
import info.ephyra.querygeneration.Query;
import info.ephyra.querygeneration.QueryGeneration;
import info.ephyra.querygeneration.generators.BagOfTermsG;
import info.ephyra.querygeneration.generators.BagOfWordsG;
import info.ephyra.querygeneration.generators.PredicateG;
import info.ephyra.querygeneration.generators.QuestionInterpretationG;
import info.ephyra.querygeneration.generators.QuestionReformulationG;
import info.ephyra.querygeneration.generators.EasyG;
import info.ephyra.questionanalysis.AnalyzedQuestion;
import info.ephyra.questionanalysis.QuestionAnalysis;
import info.ephyra.questionanalysis.QuestionInterpreter;
import info.ephyra.questionanalysis.QuestionNormalizer;
import info.ephyra.search.Result;
import info.ephyra.search.Search;
import info.ephyra.search.searchers.BingKM;
import info.ephyra.search.searchers.IndriKM;
import info.ephyra.search.searchers.IndriDocumentKM;
import info.ephyra.questionsearch.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


/**
 * <code>OpenEphyra</code> is an open framework for question answering (QA).
 * 
 * @author Nico Schlaefer
 * @version 2008-03-23
 */
public class OpenEphyra {
	/** Factoid question type. */
	protected static final String FACTOID = "FACTOID";
	/** Info retrieval type */
	protected static final String INFO = "INFO";
	
	/**Piazza and lecture searcher */
	protected static PiazzaSearch relatedSearch;
	
	/** Maximum number of factoid answers. */
	protected static final int FACTOID_MAX_ANSWERS = 5;
	/** Absolute threshold for factoid answer scores. */
	protected static final float FACTOID_ABS_THRESH = 0;
	/** Maximum number of info answers. */
	protected static final int INFO_MAX_ANSWERS = 5;
	/** Relative threshold for info answer scores. */
	protected static final float INFO_ABS_THRESH = 0;
	
	/** Serialized classifier for score normalization. */
	public static final String NORMALIZER =
		"res/scorenormalization/classifiers/" +
		"AdaBoost70_" +
		"Score+Extractors_" +
		"TREC10+TREC11+TREC12+TREC13+TREC14+TREC15+TREC8+TREC9" +
		".serialized";
	
	/** The directory of Ephyra, required when Ephyra is used as an API. */
	protected String dir;
	
	/**
	 * Entry point of Ephyra. Initializes the engine and starts the command line
	 * interface.
	 * 
	 * @param args command line arguments are ignored
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		// enable output of status and error messages
		MsgPrinter.enableStatusMsgs(false);
		MsgPrinter.enableErrorMsgs(true);
		
		// set log file and enable logging
		Logger.setLogfile("log/OpenEphyra");
		Logger.enableLogging(false);
		
		relatedSearch = new PiazzaSearch();
//		relatedSearch.testPrint();
		
		// initialize Ephyra and start command line interface
		(new OpenEphyra()).commandLine();
	}
	
	/**
	 * <p>Creates a new instance of Ephyra and initializes the system.</p>
	 * 
	 * <p>For use as a standalone system.</p>
	 */
	protected OpenEphyra() {
		this("");
	}
	
	/**
	 * <p>Creates a new instance of Ephyra and initializes the system.</p>
	 * 
	 * <p>For use as an API.</p>
	 * 
	 * @param dir directory of Ephyra
	 */
	public OpenEphyra(String dir) {
		this.dir = dir;
		System.out.println("Initializing PiazzaQA...");
		MsgPrinter.printInitializing();
		
		// create tokenizer
		MsgPrinter.printStatusMsg("Creating tokenizer...");
		if (!OpenNLP.createTokenizer(dir +
				"res/nlp/tokenizer/opennlp/EnglishTok.bin.gz"))
			MsgPrinter.printErrorMsg("Could not create tokenizer.");
//		LingPipe.createTokenizer();
		
		// create sentence detector
		MsgPrinter.printStatusMsg("Creating sentence detector...");
		if (!OpenNLP.createSentenceDetector(dir +
				"res/nlp/sentencedetector/opennlp/EnglishSD.bin.gz"))
			MsgPrinter.printErrorMsg("Could not create sentence detector.");
		LingPipe.createSentenceDetector();
		
		// create stemmer
		MsgPrinter.printStatusMsg("Creating stemmer...");
		SnowballStemmer.create();
		
		// create part of speech tagger
		MsgPrinter.printStatusMsg("Creating POS tagger...");
		if (!OpenNLP.createPosTagger(
				dir + "res/nlp/postagger/opennlp/tag.bin.gz",
				dir + "res/nlp/postagger/opennlp/tagdict"))
			MsgPrinter.printErrorMsg("Could not create OpenNLP POS tagger.");
//		if (!StanfordPosTagger.init(dir + "res/nlp/postagger/stanford/" +
//				"wsj3t0-18-bidirectional/train-wsj-0-18.holder"))
//			MsgPrinter.printErrorMsg("Could not create Stanford POS tagger.");
		
		// create chunker
		MsgPrinter.printStatusMsg("Creating chunker...");
		if (!OpenNLP.createChunker(dir +
				"res/nlp/phrasechunker/opennlp/EnglishChunk.bin.gz"))
			MsgPrinter.printErrorMsg("Could not create chunker.");
		
		// create syntactic parser
		MsgPrinter.printStatusMsg("Creating syntactic parser...");
//		if (!OpenNLP.createParser(dir + "res/nlp/syntacticparser/opennlp/"))
//			MsgPrinter.printErrorMsg("Could not create OpenNLP parser.");
		try {
			StanfordParser.initialize();
		} catch (Exception e) {
			MsgPrinter.printErrorMsg("Could not create Stanford parser.");
		}
		
		// create named entity taggers
		MsgPrinter.printStatusMsg("Creating NE taggers...");
		NETagger.loadListTaggers(dir + "res/nlp/netagger/lists/");
		NETagger.loadRegExTaggers(dir + "res/nlp/netagger/patterns.lst");
		MsgPrinter.printStatusMsg("  ...loading models");
//		if (!NETagger.loadNameFinders(dir + "res/nlp/netagger/opennlp/"))
//			MsgPrinter.printErrorMsg("Could not create OpenNLP NE tagger.");
		if (!StanfordNeTagger.isInitialized() && !StanfordNeTagger.init())
			MsgPrinter.printErrorMsg("Could not create Stanford NE tagger.");
		MsgPrinter.printStatusMsg("  ...done");
		
		// create linker
//		MsgPrinter.printStatusMsg("Creating linker...");
//		if (!OpenNLP.createLinker(dir + "res/nlp/corefresolver/opennlp/"))
//			MsgPrinter.printErrorMsg("Could not create linker.");
		
		// create WordNet dictionary
		MsgPrinter.printStatusMsg("Creating WordNet dictionary...");
		if (!WordNet.initialize(dir +
				"res/ontologies/wordnet/file_properties.xml"))
			MsgPrinter.printErrorMsg("Could not create WordNet dictionary.");
		
		// load function words (numbers are excluded)
		MsgPrinter.printStatusMsg("Loading function verbs...");
		if (!FunctionWords.loadIndex(dir +
				"res/indices/functionwords_nonumbers"))
			MsgPrinter.printErrorMsg("Could not load function words.");
		
		// load prepositions
		MsgPrinter.printStatusMsg("Loading prepositions...");
		if (!Prepositions.loadIndex(dir +
				"res/indices/prepositions"))
			MsgPrinter.printErrorMsg("Could not load prepositions.");
		
		// load irregular verbs
		MsgPrinter.printStatusMsg("Loading irregular verbs...");
		if (!IrregularVerbs.loadVerbs(dir + "res/indices/irregularverbs"))
			MsgPrinter.printErrorMsg("Could not load irregular verbs.");
		
		// load word frequencies
		MsgPrinter.printStatusMsg("Loading word frequencies...");
		if (!WordFrequencies.loadIndex(dir + "res/indices/wordfrequencies"))
			MsgPrinter.printErrorMsg("Could not load word frequencies.");
		
		// load query reformulators
		MsgPrinter.printStatusMsg("Loading query reformulators...");
		if (!QuestionReformulationG.loadReformulators(dir +
				"res/reformulations/"))
			MsgPrinter.printErrorMsg("Could not load query reformulators.");
		
		// load answer types
//		MsgPrinter.printStatusMsg("Loading answer types...");
//		if (!AnswerTypeTester.loadAnswerTypes(dir +
//				"res/answertypes/patterns/answertypepatterns"))
//			MsgPrinter.printErrorMsg("Could not load answer types.");
		
		// load question patterns
		MsgPrinter.printStatusMsg("Loading question patterns...");
		if (!QuestionInterpreter.loadPatterns(dir +
				"res/patternlearning/questionpatterns/"))
			MsgPrinter.printErrorMsg("Could not load question patterns.");
		
		// load answer patterns
		MsgPrinter.printStatusMsg("Loading answer patterns...");
		if (!AnswerPatternFilter.loadPatterns(dir +
				"res/patternlearning/answerpatterns/"))
			MsgPrinter.printErrorMsg("Could not load answer patterns.");
	}
	
	/**
	 * Reads a line from the command prompt.
	 * 
	 * @return user input
	 */
	protected String readLine() {
		try {
			return new java.io.BufferedReader(new
				java.io.InputStreamReader(System.in)).readLine();
		}
		catch(java.io.IOException e) {
			return new String("");
		}
	}
	
	/**
	 * Initializes the pipeline for factoid questions.
	 */
	protected void initFactoid() {
		// question analysis
		Ontology wordNet = new WordNet();
		// - dictionaries for term extraction
		QuestionAnalysis.clearDictionaries();
		QuestionAnalysis.addDictionary(wordNet);
		// - ontologies for term expansion
		QuestionAnalysis.clearOntologies();
		QuestionAnalysis.addOntology(wordNet);
		
		// query generation
		QueryGeneration.clearQueryGenerators();
		QueryGeneration.addQueryGenerator(new BagOfWordsG());
		QueryGeneration.addQueryGenerator(new BagOfTermsG());
		QueryGeneration.addQueryGenerator(new PredicateG());
		QueryGeneration.addQueryGenerator(new QuestionInterpretationG());
		QueryGeneration.addQueryGenerator(new QuestionReformulationG());
		QueryGeneration.addQueryGenerator(new EasyG());
		
		// search
		// - knowledge miners for unstructured knowledge sources
		Search.clearKnowledgeMiners();	
		String[] INDRI_PASS = {"index/passages"};
		String[] INDRI_DOC = {"index/documents"};
		Search.addKnowledgeMiner(new IndriKM(INDRI_PASS, false));
		Search.addKnowledgeMiner(new IndriDocumentKM(INDRI_DOC, false));
		Search.clearKnowledgeAnnotators();
		
		// answer extraction and selection
		// (the filters are applied in this order)
		AnswerSelection.clearFilters();
		// - answer extraction filters
		AnswerSelection.addFilter(new SentenceExtractionFilter());
		AnswerSelection.addFilter(new CutStatementProviderFilter());
		AnswerSelection.addFilter(new NumberOfKeywordsFilter());
		AnswerSelection.addFilter(new AnswerTypeFilter());
		AnswerSelection.addFilter(new AnswerPatternFilter());
//		AnswerSelection.addFilter(new PredicateExtractionFilter());
//		AnswerSelection.addFilter(new FactoidsFromPredicatesFilter());
//		AnswerSelection.addFilter(new TruncationFilter());
		// - answer selection filters
		AnswerSelection.addFilter(new StopwordFilter());
		AnswerSelection.addFilter(new PreferNamedEntitiesFilter());
		AnswerSelection.addFilter(new ScoreNormalizationFilter(NORMALIZER));
		AnswerSelection.addFilter(new ScoreCombinationFilter());
		AnswerSelection.addFilter(new FactoidSubsetFilter());
		AnswerSelection.addFilter(new DuplicateFilter());
		AnswerSelection.addFilter(new ScoreSorterFilter());
//		AnswerSelection.addFilter(new SerializationFilter());
	}
	
	/**
	 * Initializes the pipeline for info questions.
	 */
	protected void initInfo() {
		// question analysis
		Ontology wordNet = new WordNet();
		// - dictionaries for term extraction
		QuestionAnalysis.clearDictionaries();
		QuestionAnalysis.addDictionary(wordNet);
		// - ontologies for term expansion
		QuestionAnalysis.clearOntologies();
		QuestionAnalysis.addOntology(wordNet);
		
		// query generation
		QueryGeneration.clearQueryGenerators();
		QueryGeneration.addQueryGenerator(new BagOfWordsG());
		QueryGeneration.addQueryGenerator(new BagOfTermsG());
		QueryGeneration.addQueryGenerator(new PredicateG());
		QueryGeneration.addQueryGenerator(new QuestionInterpretationG());
		QueryGeneration.addQueryGenerator(new QuestionReformulationG());
		QueryGeneration.addQueryGenerator(new EasyG());
		
		// search
		// - knowledge miners for unstructured knowledge sources
		Search.clearKnowledgeMiners();	
		String[] INDRI_PASS = {"index/passages"};
//		String[] INDRI_DOC = {"index/documents"};
		Search.addKnowledgeMiner(new IndriKM(INDRI_PASS, false));
//		Search.addKnowledgeMiner(new IndriDocumentKM(INDRI_DOC, false));
		Search.clearKnowledgeAnnotators();
		
		// answer extraction and selection
		// (the filters are applied in this order)
		AnswerSelection.clearFilters();
		// - answer extraction filters
		AnswerSelection.addFilter(new ScoreResetterFilter());
		AnswerSelection.addFilter(new SentenceExtractionFilter());
		AnswerSelection.addFilter(new NumberOfKeywordsFilter());
//		AnswerSelection.addFilter(new AdaptiveNumberOfKeywordsFilter());
//		AnswerSelection.addFilter(new TermImportanceFilter());
		// - answer selection filters
		AnswerSelection.addFilter(new CutStatementProviderFilter());
		AnswerSelection.addFilter(new StopwordFilter());
		AnswerSelection.addFilter(new UnnecessaryCharactersFilter());
		AnswerSelection.addFilter(new FunctionWordsFilter());
		AnswerSelection.addFilter(new ScoreNormalizationFilter(NORMALIZER));
		AnswerSelection.addFilter(new ScoreCombinationFilter());
		AnswerSelection.addFilter(new DuplicateFilter());
		AnswerSelection.addFilter(new ScoreSorterFilter());
//		AnswerSelection.addFilter(new SerializationFilter());
	}
	
	/**
	 * Runs the pipeline and returns an array of up to <code>maxAnswers</code>
	 * results that have a score of at least <code>absThresh</code>.
	 * 
	 * @param aq analyzed question
	 * @param maxAnswers maximum number of answers
	 * @param absThresh absolute threshold for scores
	 * @return array of results
	 */
	protected Result[] runPipeline(AnalyzedQuestion aq, int maxAnswers,
								  float absThresh) {
		// query generation
		MsgPrinter.printGeneratingQueries();
		Query[] queries = QueryGeneration.getQueries(aq);
		
		// search
		MsgPrinter.printSearching();
		Result[] results = Search.doSearch(queries);
		
//		for(Result r : results){
//			System.out.println(r.getAnswer());
//		}
		
		// answer selection
		MsgPrinter.printSelectingAnswers();
		results = AnswerSelection.getResults(results, maxAnswers, absThresh);
		
		return results;
	}
	
	/**
	 * Returns the directory of Ephyra.
	 * 
	 * @return directory
	 */
	public String getDir() {
		return dir;
	}
	
	/**
	 * <p>A command line interface for Ephyra.</p>
	 * 
	 * <p>Repeatedly queries the user for a question, asks the system the
	 * question and prints out and logs the results.</p>
	 * 
	 * <p>The command <code>exit</code> can be used to quit the program.</p>
	 */
	public void commandLine() {
		while (true) {
			// query user for question, quit if user types in "exit"
			String question = "";
			do {
				MsgPrinter.printQuestionPrompt();
				question = readLine().trim();
				if (question.equalsIgnoreCase("exit")) System.exit(0);
			} while(question.length() == 0);
			
			// determine question type and extract question string
			String type;
			if (question.matches("(?i)" + FACTOID + ":.*+")) {
				// factoid question
				type = FACTOID;
				question = question.split(":", 2)[1].trim();
			} else {
				// question type unspecified
				type = INFO;  // default type
			}
			MsgPrinter.printStatusMsg("Question Type = " + type);
			
			// ask question
			Result[] results = new Result[0];
			if (type.equals(FACTOID)) {
				Logger.logFactoidStart(question);
				results = askFactoid(question, FACTOID_MAX_ANSWERS,
						FACTOID_ABS_THRESH);
				Logger.logResults(results);
				Logger.logFactoidEnd();
			} else if (type.equals(INFO)) {
				Logger.logListStart(question);
				results = askInfo(question, INFO_MAX_ANSWERS,
						INFO_ABS_THRESH);
				Logger.logResults(results);
				Logger.logListEnd();
			}
			
			// print answers
			MsgPrinter.printAnswers(results, relatedSearch.getRelatedLec(), relatedSearch.getRelatedPosts());
		}
	}
	
	/**
	 * Asks Ephyra a factoid question and returns up to <code>maxAnswers</code>
	 * results that have a score of at least <code>absThresh</code>.
	 * 
	 * @param question factoid question
	 * @param maxAnswers maximum number of answers
	 * @param absThresh absolute threshold for scores
	 * @return array of results
	 */
	public Result[] askFactoid(String question, int maxAnswers,
							   float absThresh) {
		// initialize pipeline
		initFactoid();
		
		// analyze question
		MsgPrinter.printAnalyzingQuestion();
		AnalyzedQuestion aq = QuestionAnalysis.analyze(question);
		
		// get related posts and lectures
		relatedSearch.SearchLectures(aq.getKeywords());
		relatedSearch.SearchPiazza(aq.getKeywords());
		
		// get answers
		Result[] results = runPipeline(aq, maxAnswers, absThresh);
		
		return results;
	}
	
	/**
	 * Asks Ephyra an info question and returns up to <code>maxAnswers</code>
	 * results that have a score of at least <code>absThresh</code>.
	 * 
	 * @param question info question
	 * @param maxAnswers maximum number of answers
	 * @param absThresh absolute threshold for scores
	 * @return array of results
	 */
	public Result[] askInfo(String question, int maxAnswers,
							   float absThresh) {
		// initialize pipeline
		initInfo();
		
		// analyze question
		MsgPrinter.printAnalyzingQuestion();
		AnalyzedQuestion aq = QuestionAnalysis.analyze(question);
		
		// get related posts and lectures
		relatedSearch.SearchLectures(aq.getKeywords());
		relatedSearch.SearchPiazza(aq.getKeywords());
		
		// get answers
		Result[] results = runPipeline(aq, maxAnswers, absThresh);
		
		return results;
	}
	
	/**
	 * Asks Ephyra a factoid question and returns a single result or
	 * <code>null</code> if no answer could be found.
	 * 
	 * @param question factoid question
	 * @return single result or <code>null</code>
	 */
	public Result askFactoid(String question) {
		Result[] results = askFactoid(question, 1, 0);
		
		return (results.length > 0) ? results[0] : null;
	}
	
	/**
	 * Asks Ephyra a list question and returns results that have a score of at
	 * least <code>relThresh * top score</code>.
	 * 
	 * @param question list question
	 * @param relThresh relative threshold for scores
	 * @return array of results
	 */
	public Result[] askList(String question, float relThresh) {
		question = QuestionNormalizer.transformList(question);
		
		Result[] results = askFactoid(question, Integer.MAX_VALUE, 0);
		
		// get results with a score of at least relThresh * top score
		ArrayList<Result> confident = new ArrayList<Result>();
		if (results.length > 0) {
			float topScore = results[0].getScore();
			
			for (Result result : results)
				if (result.getScore() >= relThresh * topScore)
					confident.add(result);
		}
		
		return confident.toArray(new Result[confident.size()]);
	}
	
	/**
	* Parses query strings in individual strings for each sentence
	* 
	* @param question list question
	* @param relThresh relative threshold for scores
	* @return array of results
//	*/
//	public String[] questionParser(String question){
//		InputStream modelIn = null;
//		SentenceModel model = null;
//		try {
//			modelIn = new FileInputStream("en-sen.bin");
//			model = new SentenceModel(modelIn);
//		} catch (InvalidFormatException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
//		String sent[] = sentenceDetector.sentDetect(question);
//		
//		return sent;
//	}
}