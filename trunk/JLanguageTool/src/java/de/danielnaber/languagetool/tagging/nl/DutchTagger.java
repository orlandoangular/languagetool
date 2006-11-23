/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.tagging.nl;

import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tools.Tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.dawidweiss.stemmers.Lametyzator;

import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.JLanguageTool;

/**
 * Dutch tagger
 * 
 * @author Marcin Milkowski
 */
public class DutchTagger implements Tagger {

  private static final String RESOURCE_FILENAME = "resource" + File.separator + "nl"
      + File.separator + "dutch.dict";

  private Lametyzator morfologik = null;

  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens) throws IOException {
    String[] taggerTokens;
    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<AnalyzedTokenReadings>();
    int pos = 0;
    //caching Lametyzator instance - lazy init
    if (morfologik == null) {
      File resourceFile = JLanguageTool.getAbsoluteFile(RESOURCE_FILENAME);
      //System.setProperty(Lametyzator.PROPERTY_NAME_LAMETYZATOR_DICT, resourceFile.getAbsolutePath());
      morfologik = new Lametyzator(Tools.getInputStream(resourceFile.getAbsolutePath()),
          "iso8859-9", '+');
    }

    for (Iterator<String> iter = sentenceTokens.iterator(); iter.hasNext();) {
      String word = iter.next();
      List<AnalyzedToken> l = new ArrayList<AnalyzedToken>();
      String[] lowerTaggerTokens = null;
      taggerTokens = morfologik.stemAndForm(word);
      if (word != word.toLowerCase()) {
        lowerTaggerTokens = morfologik.stemAndForm(word.toLowerCase());
      }

      if (taggerTokens != null) {
        int i = 0;
        while (i < taggerTokens.length) {
          //Lametyzator returns data as String[]
          //first lemma, then annotations
          l.add(new AnalyzedToken(word, taggerTokens[i + 1], taggerTokens[i]));
          i = i + 2;
        }
      }
      if (lowerTaggerTokens != null) {
        int i = 0;
        while (i < lowerTaggerTokens.length) {
          //Lametyzator returns data as String[]
          //first lemma, then annotations
          l.add(new AnalyzedToken(word, lowerTaggerTokens[i + 1], lowerTaggerTokens[i]));
          i = i + 2;
        }
      }

      if (lowerTaggerTokens == null && taggerTokens == null) {
        l.add(new AnalyzedToken(word, null, pos));
      }
      pos += word.length();
      tokenReadings
          .add(new AnalyzedTokenReadings((AnalyzedToken[]) l.toArray(new AnalyzedToken[0])));
    }

    return tokenReadings;

  }

  /** 
   * @see de.danielnaber.languagetool.tagging.Tagger#createNullToken(java.lang.String, int)
   * @return AnalyzedTokenReadings
   */
  public final Object createNullToken(final String token, final int startPos) {
    return new AnalyzedTokenReadings(new AnalyzedToken(token, null, startPos));
  }

}
