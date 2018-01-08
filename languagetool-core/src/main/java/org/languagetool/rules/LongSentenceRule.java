/* LanguageTool, a natural language style checker
 * Copyright (C) 2011 Michael Bryant
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.rules;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;

/**
 * A rule that warns on long sentences. Note that this rule is off by default.
 */
public class LongSentenceRule extends Rule {

  private static final int DEFAULT_MAX_WORDS = 40;
  private static final Pattern NON_WORD_REGEX = Pattern.compile("[.?!…:;,~’'\"„“”»«‚‘›‹()\\[\\]\\-–—\\*×∗·\\+÷:\\/=]");
  private static final boolean DEFAULT_INACTIVE = false;

  protected static int maxWords = DEFAULT_MAX_WORDS;
  protected static boolean firstSet = true;

  /**
   * @param defaultActive allows default granularity
   * @since 3.7
   */
  public LongSentenceRule(ResourceBundle messages, int maxSentenceLength, boolean defaultActive) {
    super(messages);
    super.setCategory(Categories.STYLE.getCategory(messages));
    if (maxSentenceLength <= 0) {
      throw new IllegalArgumentException("maxSentenceLength must be > 0: " + maxSentenceLength);
    }
    if (firstSet) {
      if (!defaultActive) {             //  since 4.1 maxWords is only set once and only if defaultActive == true
        setDefaultOff();                //  all changes after the first call of the rule are left to the option panel
      } else {                          //  and to the configuration file
        maxWords = maxSentenceLength;   //  so a special default value for an active rule can be defined but it can
        firstSet = false;               //  be overridden by the user
      }
    }
    setLocQualityIssueType(ITSIssueType.Style);
  }

  /**
   * @param maxSentenceLength the maximum sentence length that does not yet trigger a match
   * @since 2.4
   */
  public LongSentenceRule(ResourceBundle messages, int maxSentenceLength) {
    this(messages, maxSentenceLength, DEFAULT_INACTIVE);
  }

  /**
   * Creates a rule with the default maximum sentence length (40 words).
   */
  public LongSentenceRule(ResourceBundle messages) {
    this(messages, DEFAULT_MAX_WORDS, DEFAULT_INACTIVE);
  }

  @Override
  public String getDescription() {
    return MessageFormat.format(messages.getString("long_sentence_rule_desc"), maxWords);
  }

  /**
   * Override this ID by adding a language acronym (e.g. TOO_LONG_SENTENCE_DE)
   * to use adjustment of maxWords by option panel
   * @since 4.1
   */   
  @Override
  public String getId() {
    return "TOO_LONG_SENTENCE";
  }

  /*
   * set maximal Distance of words in number of sentences
   * @since 4.1
   */
  @Override
  public void setDefaultValue(int numWords) {
    maxWords = numWords;
  }
  
  /*
   * get maximal Distance of words in number of sentences
   * @since 4.1
   */
  @Override
  public int getDefaultValue() {
    return maxWords;
  }
  
  public String getMessage() {
		return MessageFormat.format(messages.getString("long_sentence_rule_msg"), maxWords);
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    String msg = getMessage();
    int numWords = 0;
    int pos = sentence.getText().length() - 1;   //  marks the whole sentence
    if (tokens.length < maxWords + 1) {   // just a short-circuit
      return toRuleMatchArray(ruleMatches);
    } else {
      for (AnalyzedTokenReadings aToken : tokens) {
        String token = aToken.getToken();
//        pos += token.length();  // won't match the whole offending sentence, but much of it
        if (!aToken.isSentenceStart() && !aToken.isSentenceEnd() && !NON_WORD_REGEX.matcher(token).matches()) {
          numWords++;
        }
      }
    }
    if (numWords > maxWords) {
      RuleMatch ruleMatch = new RuleMatch(this, sentence, 0, pos, msg);
      ruleMatches.add(ruleMatch);
    }
    return toRuleMatchArray(ruleMatches);
  }

}
