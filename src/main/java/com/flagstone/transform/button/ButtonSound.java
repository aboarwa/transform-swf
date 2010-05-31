/*
 * ButtonSound.java
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

package com.flagstone.transform.button;

import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

import com.flagstone.transform.MovieTag;
import com.flagstone.transform.SWF;
import com.flagstone.transform.coder.Coder;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.MovieTypes;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.exception.IllegalArgumentRangeException;
import com.flagstone.transform.sound.SoundInfo;

/**
 * ButtonSound defines the sounds that are played when an event occurs in a
 * button. Sounds are only played for the RollOver, RollOut, Press and Release
 * events.
 *
 * <p>
 * For each event a {@link SoundInfo} object identifies the sound and controls
 * how it is played. For events where no sound should be played simply specify a
 * null value instead of a SoundInfo object.
 * </p>
 *
 * @see DefineButton
 * @see DefineButton2
 */
//TODO(class)
public final class ButtonSound implements MovieTag {

    /** Format string used in toString() method. */
    private static final String FORMAT = "ButtonSound: { identifier=%d;"
            + " table=%s }";

    private static final EnumSet<ButtonEvent>EVENTS = EnumSet.of(
            ButtonEvent.ROLL_OUT, ButtonEvent.ROLL_OVER,
            ButtonEvent.PRESS, ButtonEvent.RELEASE);

    /** The unique identifier of the button. */
    private int identifier;
    private transient Map<ButtonEvent, SoundInfo>table;

    /** The length of the object, minus the header, when it is encoded. */
    private transient int length;

    /**
     * Creates and initialises a ButtonSound object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws IOException
     *             if an error occurs while decoding the data.
     */
    public ButtonSound(final SWFDecoder coder) throws IOException {
        length = coder.readUnsignedShort() & Coder.LENGTH_FIELD;
        if (length == Coder.IS_EXTENDED) {
            length = coder.readInt();
        }
        coder.mark();
        identifier = coder.readUnsignedShort();
        table = new LinkedHashMap<ButtonEvent, SoundInfo>();
        decodeInfo(ButtonEvent.ROLL_OUT, coder);
        decodeInfo(ButtonEvent.ROLL_OVER, coder);
        decodeInfo(ButtonEvent.PRESS, coder);
        decodeInfo(ButtonEvent.RELEASE, coder);
        coder.unmark(length);
    }

    private void decodeInfo(final ButtonEvent event,
            final SWFDecoder coder) throws IOException {
        if (coder.bytesRead() < length) {
            int uid = coder.readUnsignedShort();
            if (uid != 0) {
                table.put(event, new SoundInfo(uid, coder));
            }
        }
    }

    /**
     * Creates a ButtonSound object that defines the sound played for a single
     * button event.
     *
     * @param uid
     *            the unique identifier of the DefineButton or DefineButton2
     *            object that defines the button. Must be in the range 1..65535.
     * @param eventCode
     *            the event that identifies when the sound id played, must be
     *            either ButtonEvent.EventType.rollOver,
     *            ButtonEvent.EventType.rollOut, ButtonEvent.EventType.press or
     *            ButtonEvent.EventType.release.
     * @param aSound
     *            an SoundInfo object that identifies a sound and controls how
     *            it is played.
     */
    public ButtonSound(final int uid, final ButtonEvent eventCode,
            final SoundInfo aSound) {
        setIdentifier(uid);
        setSoundInfo(eventCode, aSound);
    }

    /**
     * Creates and initialises a ButtonSound object using the values copied
     * from another ButtonSound object.
     *
     * @param object
     *            a ButtonSound object from which the values will be
     *            copied.
     */
    public ButtonSound(final ButtonSound object) {

        identifier = object.identifier;
        table = new LinkedHashMap<ButtonEvent, SoundInfo>();

        for (ButtonEvent event : object.table.keySet()) {
            table.put(event, object.table.get(event).copy());
        }
    }

    /**
     * Get the unique identifier of the button that this object applies to.
     *
     * @return the unique identifier of the sound.
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Returns the SoundInfo object for the specified event. Null is returned if
     * there is no SoundInfo object defined for the event code.
     *
     * @param event
     *            The button event, must be one of ButtonEvent.ROLL_OVER,
     *            ButtonEvent.ROLL_OUT, ButtonEvent.PRESS, ButtonEvent.RELEASE.
     * @return the SoundInfo that identifies and controls the sound that will be
     *            played for the event or null if not SoundInfo is defined for
     *            the event.
     */
    public SoundInfo getSoundInfo(final ButtonEvent event) {
         return table.get(event);
    }

    /**
     * Sets the identifier of the button that this object applies to.
     *
     * @param uid
     *            the unique identifier of the button which this object applies
     *            to. Must be in the range 1..65535.
     */
    public void setIdentifier(final int uid) {
        if ((uid < SWF.MIN_IDENTIFIER) || (uid > SWF.MAX_IDENTIFIER)) {
            throw new IllegalArgumentRangeException(
                    SWF.MIN_IDENTIFIER, SWF.MAX_IDENTIFIER, uid);
        }
        identifier = uid;
    }

    /**
     * Sets the SoundInfo object for the specified button event. The argument
     * may be null allowing the SoundInfo object for a given event to be
     * deleted.
     *
     * @param event
     *            the code representing the button event, must be either
     *            ButtonEvent.EventType.RollOver, ButtonEvent.EventType.RollOut,
     *            ButtonEvent.EventType.Press or ButtonEvent.EventType.Release.
     * @param info
     *            an SoundInfo object that identifies and controls how the sound
     *            is played.
     */
    public void setSoundInfo(final ButtonEvent event, final SoundInfo info) {
        table.put(event, info);
    }

    /** {@inheritDoc} */
    public ButtonSound copy() {
        return new ButtonSound(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.format(FORMAT, identifier, table.toString());
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final Context context) {
        length = 2;

        for (ButtonEvent event : EVENTS) {
            if (table.containsKey(event)) {
                length += table.get(event).prepareToEncode(context);
            } else {
                length += 2;
            }
        }
        return (length > Coder.SHORT_HEADER_LIMIT ? Coder.LONG_HEADER
                : Coder.SHORT_HEADER) + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws IOException {

        if (length > Coder.SHORT_HEADER_LIMIT) {
            coder.writeShort((MovieTypes.BUTTON_SOUND
                    << Coder.LENGTH_FIELD_SIZE) | Coder.IS_EXTENDED);
            coder.writeInt(length);
        } else {
            coder.writeShort((MovieTypes.BUTTON_SOUND
                    << Coder.LENGTH_FIELD_SIZE) | length);
        }
        coder.mark();
        coder.writeShort(identifier);

        for (ButtonEvent event : EVENTS) {
            if (table.containsKey(event)) {
                table.get(event).encode(coder, context);
            } else {
                coder.writeShort(0);
            }
        }
        coder.unmark(length);
    }
}
