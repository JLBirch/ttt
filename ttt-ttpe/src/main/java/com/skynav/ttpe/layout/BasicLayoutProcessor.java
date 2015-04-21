/*
 * Copyright 2014-15 Skynav, Inc. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY SKYNAV, INC. AND ITS CONTRIBUTORS “AS IS” AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SKYNAV, INC. OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.skynav.ttpe.layout;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.skynav.ttpe.area.Area;
import com.skynav.ttpe.area.AreaNode;
import com.skynav.ttpe.area.BlockArea;
import com.skynav.ttpe.area.BlockFillerArea;
import com.skynav.ttpe.area.LineArea;
import com.skynav.ttpe.area.ReferenceArea;
import com.skynav.ttpe.fonts.FontCache;
import com.skynav.ttpe.geometry.Extent;
import com.skynav.ttpe.geometry.Point;
import com.skynav.ttpe.geometry.TransformMatrix;
import com.skynav.ttpe.geometry.WritingMode;
import com.skynav.ttpe.style.BlockAlignment;
import com.skynav.ttpe.style.StyleCollector;
import com.skynav.ttpe.text.LineBreaker;
import com.skynav.ttpe.text.LineBreakIterator;
import com.skynav.ttpe.text.Paragraph;
import com.skynav.ttpe.text.ParagraphCollector;
import com.skynav.ttv.app.InvalidOptionUsageException;
import com.skynav.ttv.app.MissingOptionArgumentException;
import com.skynav.ttv.app.OptionSpecification;
import com.skynav.ttv.util.Reporter;
import com.skynav.ttx.transformer.TransformerContext;
import com.skynav.xml.helpers.Documents;

import static com.skynav.ttpe.text.Constants.*;

public class BasicLayoutProcessor extends LayoutProcessor {

    public static final String NAME                             = "basic";

    public static final String defaultLineBreakerName           = "uax14";
    public static final String defaultCharacterBreakerName      = "scalar";

    // option and usage info
    private static final String[][] longOptionSpecifications = new String[][] {
        { "line-breaker",               "NAME",     "specify line breaker name (default: " + defaultLineBreakerName + ")" },
        { "font",                       "FILE",     "specify font configuration file" },
        { "font-directory",             "DIRECTORY","specify path to directory where font configuration files are located" },
    };
    private static final Map<String,OptionSpecification> longOptions;
    static {
        longOptions = new java.util.TreeMap<String,OptionSpecification>();
        for (String[] spec : longOptionSpecifications) {
            longOptions.put(spec[0], new OptionSpecification(spec[0], spec[1], spec[2]));
        }
    }

    // options state
    private String fontSpecificationDirectoryPath;
    private List<String> fontSpecificationFileNames;
    private String lineBreakerName;
    private String charBreakerName;

    // derived state
    private FontCache fontCache;
    private LineBreaker lineBreaker;
    private LineBreaker charBreaker;

    protected BasicLayoutProcessor(TransformerContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Collection<OptionSpecification> getLongOptionSpecs() {
        return longOptions.values();
    }

    @Override
    public int parseLongOption(String args[], int index) {
            String option = args[index];
            assert option.length() > 2;
            option = option.substring(2);
            if (option.equals("font")) {
                if (index + 1 > args.length)
                    throw new MissingOptionArgumentException("--" + option);
                if (fontSpecificationFileNames == null)
                    fontSpecificationFileNames = new java.util.ArrayList<String>();
                fontSpecificationFileNames.add(args[++index]);
            } else if (option.equals("font-directory")) {
                if (index + 1 > args.length)
                    throw new MissingOptionArgumentException("--" + option);
                fontSpecificationDirectoryPath = args[++index];
            } else if (option.equals("line-breaker")) {
                if (index + 1 > args.length)
                    throw new MissingOptionArgumentException("--" + option);
                lineBreakerName = args[++index];
            } else
                index = index - 1;
            return index + 1;
    }

    @Override
    public void processDerivedOptions() {
        File fontSpecificationDirectory = null;
        if (fontSpecificationDirectoryPath != null) {
            fontSpecificationDirectory = new File(fontSpecificationDirectoryPath);
            if (!fontSpecificationDirectory.exists())
                throw new InvalidOptionUsageException("font-directory", "directory does not exist: " + fontSpecificationDirectoryPath);
            else if (!fontSpecificationDirectory.isDirectory())
                throw new InvalidOptionUsageException("font-directory", "not a directory: " + fontSpecificationDirectoryPath);
        }
        List<File> fontSpecificationFiles = null;
        if ((fontSpecificationFileNames != null) && !fontSpecificationFileNames.isEmpty()) {
            for (String name : fontSpecificationFileNames) {
                File fontSpecificationFile = new File(name);
                if (!fontSpecificationFile.exists())
                    throw new InvalidOptionUsageException("font", "file does not exist: " + name);
                else if (!fontSpecificationFile.isFile())
                    throw new InvalidOptionUsageException("font", "not a file: " + name);
                else {
                    if (fontSpecificationFiles == null)
                        fontSpecificationFiles = new java.util.ArrayList<File>();
                    fontSpecificationFiles.add(fontSpecificationFile);
                }
            }
        }
        Reporter reporter = context.getReporter();
        this.fontCache = new FontCache(fontSpecificationDirectory, fontSpecificationFiles, reporter);
        if (lineBreakerName == null)
            lineBreakerName = defaultLineBreakerName;
        LineBreaker lb = LineBreaker.getInstance(lineBreakerName);
        this.lineBreaker = lb;
        if (charBreakerName == null)
            charBreakerName = defaultCharacterBreakerName;
        LineBreaker cb = LineBreaker.getInstance(charBreakerName);
        this.charBreaker = cb;
    }

    @Override
    public List<Area> layout(Document d) {
        if (d != null) {
            Element root = d.getDocumentElement();
            if (root != null) {
                LayoutState ls = makeLayoutState();
                if (isElement(root, isdSequenceElementName))
                    return layoutISDSequence(root, ls);
                else if (isElement(root, isdInstanceElementName))
                    return layoutISDInstance(root, ls);
            }
        }
        return new java.util.ArrayList<Area>();
    }

    @Override
    public void clear(boolean all) {
        if (all) {
            if (fontCache != null)
                fontCache.clear();
            if (lineBreaker != null)
                lineBreaker.clear();
            if (charBreaker != null)
                 charBreaker.clear();
        }
    }


    protected LayoutState makeLayoutState() {
        return initializeLayoutState(createLayoutState());
    }

    protected LayoutState createLayoutState() {
        return new BasicLayoutState(context);
    }

    protected LayoutState initializeLayoutState(LayoutState ls) {
        return ls.initialize(fontCache, getLineBreakIterator(), getCharacterBreakIterator());
    }

    private LineBreakIterator getLineBreakIterator() {
        LineBreaker lb = lineBreaker;
        return (lb != null) ? lb.getIterator(context.getReporter()) : null;
    }

    private LineBreakIterator getCharacterBreakIterator() {
        LineBreaker cb = charBreaker;
        return (cb != null) ? cb.getIterator(context.getReporter()) : null;
    }

    protected List<Area> layoutISDSequence(Element e, LayoutState ls) {
        List<Area> areas = new java.util.ArrayList<Area>();
        for (Element c : getChildElements(e)) {
            if (isElement(c, isdInstanceElementName))
                areas.addAll(layoutISDInstance(c, ls));
        }
        return areas;
    }

    protected List<Area> layoutISDInstance(Element e, LayoutState ls) {
        List<Area> areas = new java.util.ArrayList<Area>();
        try {
            double begin = Double.parseDouble(e.getAttribute("begin"));
            double end = Double.parseDouble(e.getAttribute("end"));
            ls.pushCanvas(e, begin, end);
            Extent extent = ls.getExternalExtent();
            double w = extent.getWidth();
            double h = extent.getHeight();
            boolean clip = ls.getExternalOverflow().clips();
            ls.pushViewport(e, w, h, clip);
            WritingMode wm = ls.getExternalWritingMode();
            TransformMatrix ctm = ls.getExternalTransform();
            ls.pushReference(e, 0, 0, w, h, wm, ctm);
            for (Element c : getChildElements(e)) {
                if (isElement(c, isdRegionElementName))
                    layoutRegion(c, ls);
                else if (isElement(c, isdComputedStyleSetElementName))
                    ls.saveStyles(c);
            }
            ls.pop();
            ls.pop();
            areas.add(ls.pop());
        } catch (NumberFormatException x) {
        }
        return areas;
    }

    protected void layoutRegion(Element e, LayoutState ls) {
        Extent extent = ls.getExtent(e);
        double w = extent.getWidth();
        double h = extent.getHeight();
        Point origin = ls.getPosition(e, extent);
        double x = origin.getX();
        double y = origin.getY();
        boolean clip = ls.getOverflow(e).clips();
        ls.pushViewport(e, w, h, clip);
        WritingMode wm = ls.getWritingMode(e);
        TransformMatrix ctm = ls.getTransform(e);
        ls.pushReference(e, x, y, w, h, wm, ctm);
        for (Element c : getChildElements(e)) {
            if (isElement(c, ttBodyElementName))
                layoutBody(c, ls);
        }
        AreaNode r = ls.peek();
        if (r instanceof ReferenceArea)
            alignBlockAreas((ReferenceArea) r, ls.getReferenceAlignment());
        ls.pop();
        ls.pop();
    }

    protected void layoutBody(Element e, LayoutState ls) {
        ls.pushBlock(e);
        for (Element c : getChildElements(e)) {
            if (isElement(c, ttDivisionElementName))
                layoutDivision(c, ls);
        }
        ls.pop();
    }

    protected void layoutDivision(Element e, LayoutState ls) {
        ls.pushBlock(e);
        for (Element c : getChildElements(e)) {
            if (isElement(c, ttDivisionElementName)) {
                layoutDivision(c, ls);
            } else if (isElement(c, ttParagraphElementName)) {
                layoutParagraph(c, ls);
            }
        }
        ls.pop();
    }

    protected void layoutParagraph(Element e, LayoutState ls) {
        layoutParagraphs(e, new ParagraphCollector(newStyleCollector(ls)).collect(e), ls);
    }

    private StyleCollector newStyleCollector(LayoutState ls) {
        return new StyleCollector(context, ls.getFontCache(), ls.getExternalExtent(), ls.getReferenceExtent(), ls.getWritingMode(), ls.getLanguage(), ls.getFont(), ls.getStyles());
    }

    protected void layoutParagraphs(Element e, List<Paragraph> paragraphs, LayoutState ls) {
        for (Paragraph p : paragraphs) {
            layoutParagraph(p, ls);
        }
    }

    protected void layoutParagraph(Paragraph p, LayoutState ls) {
        ls.pushBlock(p.getElement());
        for (LineArea l : new ParagraphLayout(p, ls).layout()) {
            ls.addLine(l);
        }
        AreaNode b = ls.peek();
        if (b instanceof BlockArea)
            alignLineAreas((BlockArea) b, ls);
        ls.pop();
    }

    protected static List<Element> getChildElements(Element e) {
        return Documents.getChildElements(e);
    }

    protected static boolean isElement(Element e, QName qn) {
        return Documents.isElement(e, qn);
    }

    private void alignBlockAreas(ReferenceArea r, BlockAlignment alignment) {
        double measure = r.isVertical() ? r.getWidth() : r.getHeight();
        double consumed = 0;
        for (AreaNode c : r.getChildren()) {
            consumed += c.getBPD();
        }
        double available = measure - consumed;
        if (available > 0) {
            if (alignment == BlockAlignment.BEFORE) {
                AreaNode a = new BlockFillerArea(r.getElement(), 0, available);
                r.addChild(a, null);
            } else if (alignment == BlockAlignment.AFTER) {
                AreaNode a = new BlockFillerArea(r.getElement(), 0, available);
                r.insertChild(a, r.firstChild(), null);
            } else if (alignment == BlockAlignment.CENTER) {
                double half = available / 2;
                AreaNode a1 = new BlockFillerArea(r.getElement(), 0, half);
                AreaNode a2 = new BlockFillerArea(r.getElement(), 0, half);
                r.insertChild(a1, r.firstChild(), null);
                r.insertChild(a2, null, null);
            } else {
                // no-op
            }
        } else if (available < 0) {
            r.setOverflow(-available);
        }
    }

    private void alignLineAreas(BlockArea b, LayoutState ls) {
        BlockAlignment alignment = ls.getReferenceAlignment();
        ReferenceArea r = ls.getReferenceArea();
        double measure = r.isVertical() ? r.getWidth() : r.getHeight();
        double consumed = 0;
        int numChildren = 0;
        for (AreaNode c : b.getChildren()) {
            consumed += c.getBPD();
            ++numChildren;
        }
        double available = measure - consumed;
        if (available > 0) {
            if (alignment == BlockAlignment.BEFORE) {
                // no-op
            } else if (alignment == BlockAlignment.AFTER) {
                // no-op
            } else if (alignment == BlockAlignment.CENTER) {
                // no-op
            } else {
                justifyLineAreas(b, measure, consumed, numChildren, alignment);
            }
        } else if (available < 0) {
            b.setOverflow(-available);
        }
    }

    private void justifyLineAreas(BlockArea b, double measure, double consumed, int numChildren, BlockAlignment alignment) {
        double available = measure - consumed;
        if (alignment == BlockAlignment.JUSTIFY)
            alignment = BlockAlignment.SPACE_BETWEEN;
        int numFillers;
        if (alignment == BlockAlignment.SPACE_AROUND) {
            numFillers = numChildren + 1;
        } else if (alignment == BlockAlignment.SPACE_BETWEEN) {
            numFillers = numChildren - 1;
        } else
            numFillers = 0;
        double fill;
        if (numFillers > 0)
            fill = available / numFillers;
        else
            fill = 0;
        if (fill > 0) {
            List<AreaNode> children = new java.util.ArrayList<AreaNode>(b.getChildren());
            for (AreaNode c : children) {
                AreaNode f = new BlockFillerArea(b.getElement(), 0, fill);
                if ((c == children.get(0)) && (alignment == BlockAlignment.SPACE_BETWEEN))
                    continue;
                else
                    b.insertChild(f, c, null);
            }
            if (alignment == BlockAlignment.SPACE_AROUND) {
                AreaNode f = new BlockFillerArea(b.getElement(), 0, fill);
                b.insertChild(f, null, null);
            }
        }
    }

}