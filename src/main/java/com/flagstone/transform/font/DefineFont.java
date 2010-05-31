/*
 * DefineFont.java
 * Transform
 *
 * Copyright (c) 2001-2010 Flagstone Software Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of Flagstone Software Ltd. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.flagstone.transform.font;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.flagstone.transform.DefineTag;
import com.flagstone.transform.SWF;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.MovieTypes;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.exception.IllegalArgumentRangeException;
import com.flagstone.transform.shape.Shape;
import com.flagstone.transform.shape.ShapeData;

/**
 * DefineFont defines the glyphs that are drawn when text characters are
 * rendered in a particular font.
 *
 * <p>
 * A complete definition of a font is created using the DefineFont object for
 * the glyphs along with an FontInfo or FontInfo2 object which contains the name
 * of the font, whether the font face is bold or italics and a table that maps
 * character codes to the glyphs that is drawn to represent the character.
 * </p>
 *
 * <p>
 * When defining a font only the glyphs used from a particular font are
 * included. Unused glyphs can be omitted greatly reducing the amount of
 * information that is encoded.
 * </p>
 *
 * @see FontInfo
 * @see FontInfo2
 */
//TODO(class)
public final class DefineFont implements DefineTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "DefineFont: { identifier=%d;"
    		+ " shapes=%s }";

    /** The unique identifier for this object. */
    private int identifier;
    /** The set of glyphs for this font. */
    private List<Shape> shapes;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;
    private transient int[] table;

    // TODO(optimise)
    /**
     * Creates and initialises a DefineFont object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public DefineFont(final SWFDecoder coder) throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        identifier = coder.readUnsignedShort();
        shapes = new ArrayList<Shape>();

        final int first = coder.readUnsignedShort();
        final int count = first >> 1;
        final int[] table = new int[count + 1];

        table[0] = first;
        for (int i = 1; i < count; i++) {
            table[i] = coder.readUnsignedShort();
        }

        table[count] = length - 2;

        Shape shape;
        byte[] data;

        for (int i = 0; i < count; i++) {
            shape = new Shape();
            data = new byte[table[i + 1] - table[i]];
            shape.add(new ShapeData(coder.readBytes(data)));
            shapes.add(shape);
        }
        coder.unmark(length);
    }

    /**
     * Creates a DefineFont object setting the unique identifier for the object
     * and the array of glyphs used to render the characters used from the font.
     *
     * @param uid
     *            the unique identifier for this object.
     * @param anArray
     *            an array of Shape objects that define the outlines for each
     *            glyph in the font.
     */
    public DefineFont(final int uid, final List<Shape> anArray) {
        setIdentifier(uid);
        setShapes(anArray);
    }

    /**
     * Creates and initialises a DefineFont object using the values copied
     * from another DefineFont object.
     *
     * @param object
     *            a DefineFont object from which the values will be
     *            copied.
     */
    public DefineFont(final DefineFont object) {
        identifier = object.identifier;
        shapes = new ArrayList<Shape>(object.shapes.size());
        for (final Shape shape : object.shapes) {
            shapes.add(shape.copy());
        }
    }

    /** {@inheritDoc} */
    public int getIdentifier() {
        return identifier;
    }

    /** {@inheritDoc} */
    public void setIdentifier(final int uid) {
        if ((uid < SWF.MIN_IDENTIFIER) || (uid > SWF.MAX_IDENTIFIER)) {
            throw new IllegalArgumentRangeException(
                    SWF.MIN_IDENTIFIER, SWF.MAX_IDENTIFIER, uid);
        }
        identifier = uid;
    }

    /**
     * Add a shape to the array of shapes that represent the glyphs for the
     * font.
     *
     * @param obj
     *            a shape which must not be null.
     * @return this object.
     */
    public DefineFont add(final Shape obj) {
        if (obj == null) {
            throw new IllegalArgumentException();
        }
        shapes.add(obj);
        return this;
    }

    /**
     * Get the array of shapes that define the outline for each glyph.
     *
     * @return the glyphs for this font.
     */
    public List<Shape> getShapes() {
        return shapes;
    }

    /**
     * Sets the array of shapes that describe each glyph.
     *
     * @param anArray
     *            an array of Shape objects that define the outlines for each
     *            glyph in the font. Must not be null.
     */
    public void setShapes(final List<Shape> anArray) {
        if (anArray == null) {
            throw new IllegalArgumentException();
        }
        shapes = anArray;
    }

    /** {@inheritDoc} */
    public DefineFont copy() {
        return new DefineFont(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, identifier, shapes);
    }

    // TODO(optimise)
    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        length = 2;

        context.put(Context.FILL_SIZE, 1);
        context.put(Context.LINE_SIZE, context.contains(Context.POSTSCRIPT) ? 1
                : 0);

        int index = 0;
        int count = shapes.size();
        int shapeLength;
        int tableEntry = count << 1;
        table = new int[count+1];

        length += count << 1;

        table[index++] = tableEntry;

        for (final Shape shape : shapes) {
            shapeLength = shape.prepareToEncode(context);
            tableEntry += shapeLength;
            table[index++] = tableEntry;
            length += shapeLength;
        }

        context.put(Context.FILL_SIZE, 0);
        context.put(Context.LINE_SIZE, 0);

        return (length > Coder.SHORT_HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
    }

    // TODO(optimise)
    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {
        if (length > Coder.SHORT_HEADER_LIMIT) {
            coder.writeShort((MovieTypes.DEFINE_FONT
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.DEFINE_FONT
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        coder.mark();
        coder.writeShort(identifier);

        context.put(Context.FILL_SIZE, 1);
        context.put(Context.LINE_SIZE, context.contains(Context.POSTSCRIPT) ? 1
                : 0);

        for (int i = 0; i < table.length - 1; i++) {
            coder.writeShort(table[i]);
        }

        for (final Shape shape : shapes) {
            shape.encode(coder, context);
        }

        context.put(Context.FILL_SIZE, 0);
        context.put(Context.LINE_SIZE, 0);
        coder.unmark(length);
    }
}
