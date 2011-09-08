package com.gentics.cr.lucene.search.query;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

import com.gentics.cr.CRRequest;

/**
 * CRQueryParser allows you to use mnoGoSearch Queries and searching in
 * multiple attributes.
 * @author perhab
 */
public class CRQueryParser extends QueryParser {

	/**
	 * Constant 1.
	 */
	private static final int ONE = 1;
	/**
	 * Constant 2.
	 */
	private static final int TWO = 2;
	/**
	 * Constant 3.
	 */
	private static final int THREE = 3;
	
	/**
	 * attributes to search in.
	 */
	private String[] attributesToSearchIn;

	/**
	 * request to search for additional parameters.
	 */
	private CRRequest request;

	/**
	 * Log4j logger for error and debug messages.
	 */
	private static final Logger LOGGER = Logger.getLogger(CRQueryParser.class);
	
	/**
	 * initialize a CRQeryParser with multiple search attributes.
	 * @param version version of lucene
	 * @param searchedAttributes attributes to search in
	 * @param analyzer analyzer for index
	 */
	public CRQueryParser(final Version version, final String[] searchedAttributes,
			final Analyzer analyzer) {
		super(version, searchedAttributes[0], analyzer);
		attributesToSearchIn = searchedAttributes;
	}
	
 
	/**
	 * initialize a CRQeryParser with multiple search attributes.
	 * @param version version of lucene
	 * @param searchedAttributes attributes to search in
	 * @param analyzer analyzer for index
	 * @param crRequest request to get additional parameters from.
	 */
	public CRQueryParser(final Version version, final String[] searchedAttributes,
			final Analyzer analyzer, final CRRequest crRequest) {
		this(version, searchedAttributes, analyzer);
		this.request = crRequest;
	}

	/**
	 * parse the query for lucene.
	 * @param query as {@link String}
	 * @return parsed lucene query
	 * @throws ParseException when the query cannot be successfully parsed
	 */
	public final Query parse(final String query) throws ParseException {
		String crQuery = query;
		LOGGER.debug("parsing query: " + crQuery);
		crQuery = replaceBooleanMnoGoSearchQuery(crQuery);
		if (attributesToSearchIn.length > ONE) {
			crQuery = addMultipleSearchedAttributes(crQuery);
		}
		crQuery = addWildcardsForWordmatchParameter(crQuery);
		LOGGER.debug("parsed query: " + crQuery);
		return super.parse(crQuery);
	}

	/**
	 * parse given query and prepare it to search in multiple attributes with
	 * lucene, only words are replaced that are not one of AND, OR, NOT and do not
	 * contain a ":" char.
	 * @param query String with query to parse
	 * @return parsed query, in case no searchedAttributes are given the original
	 * query is given back.
	 */
	private String addMultipleSearchedAttributes(final String query) {
		StringBuffer newQuery = new StringBuffer();
		String replacement = "";
		for (String attribute : attributesToSearchIn) {
			if (replacement.length() > 0) {
				replacement += " OR ";
			}
			replacement += attribute + ":$2";
		}
		if (replacement.length() > 0) {
			replacement = "(" + replacement + ")";
			Matcher valueMatcher = getValueMatcher(query);
			while (valueMatcher.find()) {
				String charsBeforeValue = valueMatcher.group(ONE);
				String value = valueMatcher.group(TWO);
				String charsAfterValue = valueMatcher.group(THREE);
				if (!"AND".equalsIgnoreCase(value)
						&& !"OR".equalsIgnoreCase(value)
						&& !"NOT".equalsIgnoreCase(value)
						&& !value.contains(":")) {
					valueMatcher.appendReplacement(newQuery, charsBeforeValue
							+ replacement + charsAfterValue);
				}
			}
			valueMatcher.appendTail(newQuery);
		}
		return newQuery.toString();
	}

	/**
	 * if the wordmatch parameter is give in the crRequest this method adds
	 * wildcards to the words in the queries.
	 * @param crQuery query to replace the words in it
	 * @return query with wildcards in it
	 */
	private String addWildcardsForWordmatchParameter(final String crQuery) {
		String wordmatch = null;
		if (request != null) {
			wordmatch = (String) request.get(CRRequest.WORDMATCH_KEY);
		}
		String appendToWordEnd = "";
		String appendToWordBegin = "";

		if (wordmatch == null || "wrd".equalsIgnoreCase(wordmatch)) {
			return crQuery;
		} else if ("end".equalsIgnoreCase(wordmatch)) {
			super.setAllowLeadingWildcard(true);
			appendToWordBegin = "*";
		} else if ("beg".equalsIgnoreCase(wordmatch)) {
			appendToWordEnd = "*";
		} else if ("sub".equalsIgnoreCase(wordmatch)) {
			super.setAllowLeadingWildcard(true);
			appendToWordBegin = "*";
			appendToWordEnd = "*";
		}
		StringBuffer newQuery = new StringBuffer();
		Matcher valueMatcher = getValueMatcher(crQuery);
		while (valueMatcher.find()) {
			String charsBeforeValue = valueMatcher.group(ONE);
			String value = valueMatcher.group(TWO);
			String charsAfterValue = valueMatcher.group(THREE);
			if (!"AND".equalsIgnoreCase(value)
					&& !"OR".equalsIgnoreCase(value)
					&& !"NOT".equalsIgnoreCase(value)) {
				valueMatcher.appendReplacement(newQuery, charsBeforeValue
						+ value.replaceAll("(.*:)?(.+)", "$1" + appendToWordBegin + "$2"
								+ appendToWordEnd) + charsAfterValue);
			}
		}
		valueMatcher.appendTail(newQuery);
		return newQuery.toString();
	}

	/**
	 * get Value Matcher.
	 * @param query query.
	 * @return matcher.
	 */
	private Matcher getValueMatcher(final String query) {
		String seperatorCharacterClass = " \\(\\)";
		Pattern valuePattern = Pattern.compile(
				"([" + seperatorCharacterClass + "]*)"
				+ "(\"[^\"]+\"|[^" + seperatorCharacterClass + "]+)"
				+ "([" + seperatorCharacterClass + "]*)");
		Matcher valueMatcher = valuePattern.matcher(query);
		return valueMatcher;
	}
	/**
	 * Helper method to replace search parameters from boolean mnoGoSearch query
	 * into their lucene compatible parameters.
	 * @param mnoGoSearchQuery query with mnoGoSearch Syntax in it.
	 * @return query with mnoGoSearch syntax replaced for lucene
	 */
	private String replaceBooleanMnoGoSearchQuery(final String mnoGoSearchQuery) {
		String luceneQuery = mnoGoSearchQuery
			.replaceAll(" ?\\| ?", " OR ")
			.replaceAll(" ?& ?", " AND ")
			.replace('\'', '"');
		luceneQuery = luceneQuery.replaceAll(" ~([a-zA-Z0-9üöäÜÖÄß]+)", " NOT $1");
		return luceneQuery;
	}
}
