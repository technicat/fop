/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.fonts;

//Java
import java.util.Map;


/**
 * Generic MultiByte (CID) font
 */
public class MultiByteFont extends CIDFont {

    private String ttcName = null;
    private String encoding = "Identity-H";

    private int defaultWidth = 0;
    private CIDFontType cidType = CIDFontType.CIDTYPE2;

    private CIDSubset subset = new CIDSubset();

    /** A map from Unicode indices to glyph indices */
    private BFEntry[] bfentries = null;

    /**
     * Default constructor
     */
    public MultiByteFont() {
        subset.setupFirstGlyph();
        setFontType(FontType.TYPE0);
    }

    /** {@inheritDoc} */
    public int getDefaultWidth() {
        return defaultWidth;
    }

    /** {@inheritDoc} */
    public String getRegistry() {
        return "Adobe";
    }

    /** {@inheritDoc} */
    public String getOrdering() {
        return "UCS";
    }

    /** {@inheritDoc} */
    public int getSupplement() {
        return 0;
    }

    /** {@inheritDoc} */
    public CIDFontType getCIDType() {
        return cidType;
    }

    /**
     * Sets the CIDType.
     * @param cidType The cidType to set
     */
    public void setCIDType(CIDFontType cidType) {
        this.cidType = cidType;
    }

    /** {@inheritDoc} */
    public String getEmbedFontName() {
        if (isEmbeddable()) {
            return FontUtil.stripWhiteSpace(super.getFontName());
        } else {
            return super.getFontName();
        }
    }

    /** {@inheritDoc} */
    public boolean isEmbeddable() {
        return !(getEmbedFileName() == null && getEmbedResourceName() == null);
    }

    /** {@inheritDoc} */
    public boolean isSubsetEmbedded() {
        return true;
    }

    /** {@inheritDoc} */
    public CIDSubset getCIDSubset() {
        return this.subset;
    }

    /** {@inheritDoc} */
    public String getEncodingName() {
        return encoding;
    }

    /** {@inheritDoc} */
    public int getWidth(int i, int size) {
        if (isEmbeddable()) {
            int glyphIndex = subset.getGlyphIndexForSubsetIndex(i);
            return size * width[glyphIndex];
        } else {
            return size * width[i];
        }
    }

    /** {@inheritDoc} */
    public int[] getWidths() {
        int[] arr = new int[width.length];
        System.arraycopy(width, 0, arr, 0, width.length);
        return arr;
    }

    /**
     * Returns the glyph index for a Unicode character. The method returns 0 if there's no
     * such glyph in the character map.
     * @param c the Unicode character index
     * @return the glyph index (or 0 if the glyph is not available)
     */
    private int findGlyphIndex(char c) {
        int idx = (int)c;
        int retIdx = SingleByteEncoding.NOT_FOUND_CODE_POINT;

        for (int i = 0; (i < bfentries.length) && retIdx == 0; i++) {
            if (bfentries[i].getUnicodeStart() <= idx
                    && bfentries[i].getUnicodeEnd() >= idx) {

                retIdx = bfentries[i].getGlyphStartIndex()
                    + idx
                    - bfentries[i].getUnicodeStart();
            }
        }
        return retIdx;
    }

    /** {@inheritDoc} */
    public char mapChar(char c) {
        notifyMapOperation();
        int glyphIndex = findGlyphIndex(c);
        if (glyphIndex == SingleByteEncoding.NOT_FOUND_CODE_POINT) {
            warnMissingGlyph(c);
            glyphIndex = findGlyphIndex(Typeface.NOT_FOUND);
        }
        if (isEmbeddable()) {
            glyphIndex = subset.mapSubsetChar(glyphIndex, c);
        }
        return (char)glyphIndex;
    }

    /** {@inheritDoc} */
    public boolean hasChar(char c) {
        return (findGlyphIndex(c) != SingleByteEncoding.NOT_FOUND_CODE_POINT);
    }

    /**
     * Sets the array of BFEntry instances which constitutes the Unicode to glyph index map for
     * a font. ("BF" means "base font")
     * @param entries the Unicode to glyph index map
     */
    public void setBFEntries(BFEntry[] entries) {
        this.bfentries = entries;
    }

    /**
     * Sets the defaultWidth.
     * @param defaultWidth The defaultWidth to set
     */
    public void setDefaultWidth(int defaultWidth) {
        this.defaultWidth = defaultWidth;
    }

    /**
     * Returns the TrueType Collection Name.
     * @return the TrueType Collection Name
     */
    public String getTTCName() {
        return ttcName;
    }

    /**
     * Sets the the TrueType Collection Name.
     * @param ttcName the TrueType Collection Name
     */
    public void setTTCName(String ttcName) {
        this.ttcName = ttcName;
    }

    /**
     * Sets the width array.
     * @param wds array of widths.
     */
    public void setWidthArray(int[] wds) {
        this.width = wds;
    }

    /**
     * Returns a Map of used Glyphs.
     * @return Map Map of used Glyphs
     */
    public Map<Integer, Integer> getUsedGlyphs() {
        return subset.getSubsetGlyphs();
    }

    /** @return an array of the chars used */
    public char[] getCharsUsed() {
        if (!isEmbeddable()) {
            return null;
        }
        return subset.getSubsetChars();
    }
}

