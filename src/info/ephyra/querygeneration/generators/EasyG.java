package info.ephyra.querygeneration.generators;

import info.ephyra.answerselection.filters.AnswerPatternFilter;
import info.ephyra.answerselection.filters.AnswerTypeFilter;
import info.ephyra.answerselection.filters.FactoidsFromPredicatesFilter;
import info.ephyra.querygeneration.Query;
import info.ephyra.questionanalysis.AnalyzedQuestion;

/**
 * @author Nico Schlaefer
 * @version 2007-07-11
 */
public class EasyG extends QueryGenerator {
	private static final float SCORE = 1;
	private static final String[] EXTRACTION_TECHNIQUES = {
		AnswerTypeFilter.ID,
		AnswerPatternFilter.ID,
		FactoidsFromPredicatesFilter.ID
	};
	public Query[] generateQueries(AnalyzedQuestion aq) {
		String[] kw = aq.getKeywords();
		// create query, set answer types
		Query[] queries = new Query[1];
		String q = "\"" + aq.getNormalized() + "\"";
//		String q = "";
		for(String word : kw) q += " " + word;
		queries[0] = new Query(q, aq, SCORE);
		queries[0].setExtractionTechniques(EXTRACTION_TECHNIQUES);
		return queries;
	}
}
