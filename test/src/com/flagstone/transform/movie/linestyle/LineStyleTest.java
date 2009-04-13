/*
 * LineStyleTest.java
 * Transform
 *
 * Copyright (c) 2009 Flagstone Software Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of Flagstone Software Ltd. nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.flagstone.transform.movie.linestyle;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import com.flagstone.transform.coder.CoderException;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.movie.DefineData;
import com.flagstone.transform.movie.Types;
import com.flagstone.transform.movie.datatype.Color;

@SuppressWarnings( { 
	"PMD.LocalVariableCouldBeFinal",
	"PMD.JUnitAssertionsShouldIncludeMessage" 
})
public final class LineStyleTest {
	
	private transient final int width = 1;
	private transient final Color color = new Color(2,3,4);
	
	private transient LineStyle fixture;
	
	private transient final byte[] encoded = new byte[] { 
			0x01, 0x00, 0x02, 0x03, 0x04 };

	@Test(expected=IllegalArgumentException.class)
	public void checkAccessorForWidthWithLowerBound() {
		fixture = new LineStyle();
		fixture.setWidth(-1);
	}

	@Test(expected=IllegalArgumentException.class)
	public void checkAccessorForWidthWithUpperBound() {
		fixture = new LineStyle();
		fixture.setWidth(65536);
	}

	@Test(expected=IllegalArgumentException.class)
	public void checkAccessorForColorWithNull() {
		fixture = new LineStyle();
		fixture.setColor(null);
	}
	
	@Test
	public void checkCopy() {
		fixture = new LineStyle(width, color);
		LineStyle copy = fixture.copy();

		assertNotSame(fixture, copy);
		assertNotSame(fixture.getColor(), copy.getColor());
		assertEquals(fixture.toString(), copy.toString());
	}
	
	@Test
	public void encode() throws CoderException {		
		SWFEncoder encoder = new SWFEncoder(encoded.length);		
		
		fixture = new LineStyle(width, color);
		assertEquals(encoded.length, fixture.prepareToEncode(encoder));
		fixture.encode(encoder);
		
		assertTrue(encoder.eof());
		assertArrayEquals(encoded, encoder.getData());
	}

	@Test
	public void decode() throws CoderException {
		SWFDecoder decoder = new SWFDecoder(encoded);
		
		fixture = new LineStyle();
		fixture.decode(decoder);
		
		assertTrue(decoder.eof());
		assertEquals(width, fixture.getWidth());
		assertEquals(color.getRed(), fixture.getColor().getRed());
		assertEquals(color.getGreen(), fixture.getColor().getGreen());
		assertEquals(color.getBlue(), fixture.getColor().getBlue());
		assertEquals(color.getAlpha(), fixture.getColor().getAlpha());
	}
}
