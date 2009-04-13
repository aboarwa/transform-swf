/*
 * FillStyle.java
 * Transform
 * 
 * Copyright (c) 2001-2009 Flagstone Software Ltd. All rights reserved.
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

package com.flagstone.transform.movie.fillstyle;

import com.flagstone.transform.movie.Codeable;
import com.flagstone.transform.movie.Copyable;

/**
 * The FillStyle interface identifies the fill styles that can be added to a 
 * shape.
 */
public interface FillStyle extends Codeable, Copyable<FillStyle> {
	/** The type for creating a solid fill style. */
	int SOLID = 0;
	/** The type for creating a linear gradient fill style. */
	int LINEAR = 16;
	/** The type for creating for a radial gradient fill style. */
	int RADIAL = 18;
	/** The type for creating a tiled bitmap fill style. */
	int TILED = 64;
	/** The type for creating a clipped bitmap fill style. */
	int CLIPPED = 65;
	/**
	 * The type for creating an unsmoothed tiled bitmap fill style - added in
	 * Flash 7 to improve performance compared with TILED fill styles.
	 */
	int UNSMOOTHED_TILED = 66; // NOPMD
	/**
	 * The type for creating an unsmoothed clipped bitmap fill style - added in
	 * Flash 7 to improve performance compared with CLIPPED fill styles.
	 */
	int UNSMOOTHED_CLIPPED = 67; // NOPMD
}
