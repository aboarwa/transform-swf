/*
 * BMPDecoder.java
 * Transform
 *
 * Copyright (c) 2009-2010 Flagstone Software Ltd. All rights reserved.
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

package com.flagstone.transform.util.image;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;

import com.flagstone.transform.coder.LittleDecoder;
import com.flagstone.transform.image.DefineImage;
import com.flagstone.transform.image.DefineImage2;
import com.flagstone.transform.image.ImageFormat;
import com.flagstone.transform.image.ImageTag;

/**
 * BMPDecoder decodes Bitmap images (BMP) so they can be used in a Flash file.
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class BMPDecoder implements ImageProvider, ImageDecoder {

    /** Level used to indicate an opaque colour. */
    private static final int OPAQUE = 255;
    /** Mask for reading unsigned 8-bit values. */
    private static final int UNSIGNED_BYTE = 255;
    /** Message used to signal that the image cannot be decoded. */
    private static final String BAD_FORMAT = "Unsupported Format";
    /** The signature identifying BMP files. */
    private static final int[] SIGNATURE = {66, 77};
    /** An uncompressed indexed image. */
    private static final int BI_RGB = 0;
    /** A run-length compressed image with 8 bits per pixel. */
    private static final int BI_RLE8 = 1;
    /** A run-length compressed image with 4 bits per pixel. */
    private static final int BI_RLE4 = 2;
    /** A true-colour image. */
    private static final int BI_BITFIELDS = 3;

    /** The format of the decoded image. */
    private transient ImageFormat format;
    /** The width of the image in pixels. */
    private transient int width;
    /** The height of the image in pixels. */
    private transient int height;
    /** The colour table for indexed images. */
    private transient byte[] table;
    /** The image data. */
    private transient byte[] image;

    /** The number of bits per pixel. */
    private transient int bitDepth;
    /** The method used to compress the image. */
    private transient int compressionMethod;
    /** The bit mask used to extract the red channel from the pixel word. */
    private transient int redMask;
    /** The bit mask used to extract the green channel from the pixel word. */
    private transient int greenMask;
    /** The bit mask used to extract the blue channel from the pixel word. */
    private transient int blueMask;

    /** {@inheritDoc} */
    public void read(final File file) throws IOException, DataFormatException {
        read(new FileInputStream(file), (int) file.length());
    }

    /** {@inheritDoc} */
    public void read(final URL url) throws IOException, DataFormatException {
        final URLConnection connection = url.openConnection();

        if (!connection.getContentType().equals("image/bmp")) {
            throw new DataFormatException(BAD_FORMAT);
        }

        final int length = connection.getContentLength();

        if (length < 0) {
            throw new FileNotFoundException(url.getFile());
        }

        read(url.openStream(), length);
    }

    /** {@inheritDoc} */
    public ImageTag defineImage(final int identifier) {
        ImageTag object = null;

        switch (format) {
        case IDX8:
            object = new DefineImage(identifier, width, height,
                    table.length / 4,
                    zip(merge(adjustScan(width, height, image), table)));
            break;
        case IDXA:
            object = new DefineImage2(identifier, width, height,
                    table.length / 4,
                    zip(mergeAlpha(adjustScan(width, height, image), table)));
            break;
        case RGB5:
            object = new DefineImage(identifier, width, height,
                    zip(packColours(width, height, image)), 16);
            break;
        case RGB8:
            orderAlpha(image);
            object = new DefineImage(identifier, width, height, zip(image), 24);
            break;
        case RGBA:
            applyAlpha(image);
            object = new DefineImage2(identifier, width, height, zip(image));
            break;
        default:
            throw new AssertionError(BAD_FORMAT);
        }
        return object;
    }

    /** {@inheritDoc} */
    public ImageDecoder newDecoder() {
        return new BMPDecoder();
    }

    /** {@inheritDoc} */
    public int getWidth() {
        return width;
    }

    /** {@inheritDoc} */
    public int getHeight() {
        return height;
    }

    /** {@inheritDoc} */
    public byte[] getImage() {
        return Arrays.copyOf(image, image.length);
    }

    /** {@inheritDoc} */
    public void read(final InputStream stream, final int length)
                    throws DataFormatException, IOException {

        final byte[] bytes = new byte[length];
        final BufferedInputStream buffer = new BufferedInputStream(stream);

        buffer.read(bytes);
        buffer.close();

        final LittleDecoder coder = new LittleDecoder(bytes);

        for (int i = 0; i < 2; i++) {
            if (coder.readByte() != SIGNATURE[i]) {
                throw new DataFormatException(BAD_FORMAT);
            }
        }

        coder.readUI32(); // fileSize
        coder.readUI32(); // reserved

        final int offset = coder.readUI32();
        final int headerSize = coder.readUI32();

        int bitsPerPixel;
        int coloursUsed;

        switch (headerSize) {
        case 12:
            width = coder.readUI16();
            height = coder.readUI16();
            coder.readUI16(); // bitPlanes
            bitsPerPixel = coder.readUI16();
            coloursUsed = 0;
            break;
        case 40:
            width = coder.readUI32();
            height = coder.readUI32();
            coder.readUI16(); // bitPlanes
            bitsPerPixel = coder.readUI16();
            compressionMethod = coder.readUI32();
            coder.readUI32(); // imageSize
            coder.readUI32(); // horizontalResolution
            coder.readUI32(); // verticalResolution
            coloursUsed = coder.readUI32();
            coder.readUI32(); // importantColours
            break;
        default:
            bitsPerPixel = 0;
            coloursUsed = 0;
            break;
        }

        if (compressionMethod == BI_BITFIELDS) {
            redMask = coder.readUI32();
            greenMask = coder.readUI32();
            blueMask = coder.readUI32();
        }

        switch (bitsPerPixel) {
        case 1:
            format = ImageFormat.IDX8;
            bitDepth = 1;
            break;
        case 2:
            format = ImageFormat.IDX8;
            bitDepth = 2;
            break;
        case 4:
            format = ImageFormat.IDX8;
            bitDepth = 4;
            break;
        case 8:
            format = ImageFormat.IDX8;
            bitDepth = 8;
            break;
        case 16:
            format = ImageFormat.RGB5;
            bitDepth = 5;
            break;
        case 24:
            format = ImageFormat.RGB8;
            bitDepth = 8;
            break;
        case 32:
            format = ImageFormat.RGBA;
            bitDepth = 8;
            break;
        default:
            throw new DataFormatException(BAD_FORMAT);
        }

        if (format == ImageFormat.IDX8) {
            coloursUsed = 1 << bitsPerPixel;
            table = new byte[coloursUsed * 4];
            image = new byte[height * width];

            int index = 0;

            if (headerSize == 12) {
                for (int i = 0; i < coloursUsed; i++, index += 4) {
                    table[index + 3] = (byte) OPAQUE;
                    table[index + 2] = (byte) coder.readByte();
                    table[index + 1] = (byte) coder.readByte();
                    table[index] = (byte) coder.readByte();
                }
            } else {
                for (int i = 0; i < coloursUsed; i++, index += 4) {
                    table[index] = (byte) coder.readByte();
                    table[index + 1] = (byte) coder.readByte();
                    table[index + 2] = (byte) coder.readByte();
                    table[index + 3] = (byte) (coder.readByte()
                            | UNSIGNED_BYTE);
                }
            }

            coder.setPointer(offset << 3);

            switch (compressionMethod) {
            case BI_RGB:
                decodeIDX8(coder);
                break;
            case BI_RLE8:
                decodeRLE8(coder);
                break;
            case BI_RLE4:
                decodeRLE4(coder);
                break;
            default:
                throw new DataFormatException(BAD_FORMAT);
            }
        } else {
            image = new byte[height * width * 4];

            coder.setPointer(offset << 3);

            switch (format) {
            case RGB5:
                decodeRGB5(coder);
                break;
            case RGB8:
                decodeRGB8(coder);
                break;
            case RGBA:
                decodeRGBA(coder);
                break;
            default:
                throw new DataFormatException(BAD_FORMAT);
            }
        }
    }

    /**
     * Decode the indexed image data block (IDX8).
     * @param coder the decoder containing the image data.
     */
    private void decodeIDX8(final LittleDecoder coder) {
        int bitsRead;
        int index = 0;

        for (int row = height - 1; row > 0; row--) {
            bitsRead = 0;
            index = row * width;

            for (int col = 0; col < width; col++) {
                image[index++] = (byte) coder.readBits(bitDepth, false);
                bitsRead += bitDepth;
            }

            if (bitsRead % 32 > 0) {
                coder.adjustPointer(32 - (bitsRead % 32));
            }
        }
    }

    /**
     * Decode the run length encoded image data block (RLE4).
     * @param coder the decoder containing the image data.
     */
    private void decodeRLE4(final LittleDecoder coder) {
        int row = height - 1;
        int col = 0;
        int index = 0;

        boolean hasMore = true;

        while (hasMore) {
            final int count = coder.readByte();

            if (count == 0) {
                final int code = coder.readByte();

                switch (code) {
                case 0:
                    col = 0;
                    row--;
                    break;
                case 1:
                    hasMore = false;
                    break;
                case 2:
                    col += coder.readUI16();
                    row -= coder.readUI16();
                    index = row * width + col;

                    for (int i = 0; i < code; i += 2) {
                        image[index++] = (byte) coder.readBits(4, false);
                        image[index++] = (byte) coder.readBits(4, false);
                    }

                    if ((code & 2) == 2) {
                        coder.readByte();
                    }
                    break;
                default:
                    index = row * width + col;
                    for (int i = 0; i < code; i += 2) {
                        image[index++] = (byte) coder.readBits(4, false);
                        image[index++] = (byte) coder.readBits(4, false);
                    }

                    if ((code & 2) == 2) {
                        coder.readByte();
                    }
                    break;
                }
            } else {
                final byte indexA = (byte) coder.readBits(4, false);
                final byte indexB = (byte) coder.readBits(4, false);
                index = row * width + col;

                for (int i = 0; (i < count) && (col < width); i++, col++) {
                    image[index++] = (i % 2 > 0) ? indexB : indexA;
                }
            }
        }
    }

    /**
     * Decode the run length encoded image data block (RLE8).
     * @param coder the decoder containing the image data.
     */
    private void decodeRLE8(final LittleDecoder coder) {
        int row = height - 1;
        int col = 0;
        int index = 0;

        boolean hasMore = true;

        while (hasMore) {
            final int count = coder.readByte();

            if (count == 0) {
                final int code = coder.readByte();

                switch (code) {
                case 0:
                    col = 0;
                    row--;
                    break;
                case 1:
                    hasMore = false;
                    break;
                case 2:
                    col += coder.readUI16();
                    row -= coder.readUI16();
                    index = row * width + col;
                    for (int i = 0; i < code; i++) {
                        image[index++] = (byte) coder.readByte();
                    }

                    if ((code & 1) == 1) {
                        coder.readByte();
                    }
                    break;
                default:
                    index = row * width + col;
                    for (int i = 0; i < code; i++) {
                        image[index++] = (byte) coder.readByte();
                    }

                    if ((code & 1) == 1) {
                        coder.readByte();
                    }
                    break;
                }
            } else {
                final byte value = (byte) coder.readByte();
                index = row * width + col;

                for (int i = 0; i < count; i++) {
                    image[index++] = value;
                }
            }
        }
    }

    /**
     * Decode the true colour image with each colour channel taking 5-bits.
     * @param coder the decoder containing the image data.
     */
    private void decodeRGB5(final LittleDecoder coder) {
        int bitsRead = 0;
        int index = 0;

        if (compressionMethod == BI_RGB) {
            for (int row = height - 1; row > 0; row--) {
                bitsRead = 0;

                for (int col = 0; col < width; col++, index += 4) {
                    final int colour = coder.readUI16() & 0xFFFF;

                    image[index] = (byte) ((colour & 0x7C00) >> 7);
                    image[index + 1] = (byte) ((colour & 0x03E0) >> 2);
                    image[index + 2] = (byte) ((colour & 0x001F) << 3);
                    image[index + 3] = (byte) OPAQUE;

                    bitsRead += 16;
                }
                if (bitsRead % 32 > 0) {
                    coder.adjustPointer(32 - (bitsRead % 32));
                }
            }
        } else {
            for (int row = height - 1; row > 0; row--) {
                bitsRead = 0;

                for (int col = 0; col < width; col++, index += 4) {
                    final int colour = coder.readUI16() & 0xFFFF;

                    if ((redMask == 0x7C00) && (greenMask == 0x03E0)
                            && (blueMask == 0x001F)) {
                        image[index] = (byte) ((colour & 0x7C00) >> 7);
                        image[index + 1] = (byte) ((colour & 0x03E0) >> 2);
                        image[index + 2] = (byte) ((colour & 0x001F) << 3);
                        image[index + 3] = (byte) 0xFF;
                    } else if ((redMask == 0xF800) && (greenMask == 0x07E0)
                            && (blueMask == 0x001F)) {
                        image[index] = (byte) ((colour & 0xF800) >> 8);
                        image[index + 1] = (byte) ((colour & 0x07E0) >> 3);
                        image[index + 2] = (byte) ((colour & 0x001F) << 3);
                        image[index + 3] = (byte) OPAQUE;
                    }
                    bitsRead += 16;
                }
                if (bitsRead % 32 > 0) {
                    coder.adjustPointer(32 - (bitsRead % 32));
                }
            }
        }

    }

    /**
     * Decode the true colour image with each colour channel taking 8-bits.
     * @param coder the decoder containing the image data.
     */
    private void decodeRGB8(final LittleDecoder coder) {
        int bitsRead;
        int index = 0;

        for (int row = height - 1; row > 0; row--) {
            bitsRead = 0;

            for (int col = 0; col < width; col++, index += 4) {
                image[index] = (byte) coder.readBits(bitDepth, false);
                image[index + 1] = (byte) coder.readBits(bitDepth, false);
                image[index + 2] = (byte) coder.readBits(bitDepth, false);
                image[index + 3] = (byte) OPAQUE;

                bitsRead += 24;
            }

            if (bitsRead % 32 > 0) {
                coder.adjustPointer(32 - (bitsRead % 32));
            }
        }
    }

    /**
     * Decode the true colour image with each colour channel and alpha taking
     * 8-bits.
     * @param coder the decoder containing the image data.
     */
    private void decodeRGBA(final LittleDecoder coder) {
        int index = 0;

        for (int row = height - 1; row > 0; row--) {
            for (int col = 0; col < width; col++, index += 4) {
                image[index + 2] = (byte) coder.readByte();
                image[index + 1] = (byte) coder.readByte();
                image[index] = (byte) coder.readByte();
                image[index + 3] = (byte) coder.readByte();
                image[index + 3] = (byte) OPAQUE;
            }
        }
    }

    /**
     * Reorder the image pixels from RGBA to ARGB.
     *
     * @param img the image data.
     */
    private void orderAlpha(final byte[] img) {
        byte alpha;

        for (int i = 0; i < img.length; i += 4) {
            alpha = img[i + 3];

            img[i + 3] = img[i + 2];
            img[i + 2] = img[i + 1];
            img[i + 1] = img[i];
            img[i] = alpha;
        }
    }

    /**
     * Apply the level for the alpha channel to the red, green and blue colour
     * channels for encoding the image so it can be added to a Flash movie.
     * @param img the image data.
     */
   private void applyAlpha(final byte[] img) {
        int alpha;

        for (int i = 0; i < img.length; i += 4) {
            alpha = img[i + 3] & 0xFF;

            img[i] = (byte) (((img[i] & UNSIGNED_BYTE) * alpha) / OPAQUE);
            img[i + 1] = (byte) (((img[i + 1] & UNSIGNED_BYTE) * alpha)
                    / OPAQUE);
            img[i + 2] = (byte) (((img[i + 2] & UNSIGNED_BYTE) * alpha)
                    / OPAQUE);
        }
    }

   /**
    * Concatenate the colour table and the image data together.
    * @param img the image data.
    * @param colors the colour table.
    * @return a single array containing the red, green and blue (not alpha)
    * entries from the colour table followed by the red, green, blue and
    * alpha channels from the image. The alpha defaults to 255 for an opaque
    * image.
    */
    private byte[] merge(final byte[] img, final byte[] colors) {
        final byte[] merged = new byte[(colors.length / 4) * 3 + img.length];
        int dst = 0;

        for (int i = 0; i < colors.length; i += 4) {
            merged[dst++] = colors[i + 2]; // R
            merged[dst++] = colors[i + 1]; // G
            merged[dst++] = colors[i]; // B
        }

        for (final byte element : img) {
            merged[dst++] = element;
        }

        return merged;
    }

    /**
     * Concatenate the colour table and the image data together.
     * @param img the image data.
     * @param colors the colour table.
     * @return a single array containing entries from the colour table followed
     * by the image.
     */
    private byte[] mergeAlpha(final byte[] img, final byte[] colors) {
        final byte[] merged = new byte[colors.length + img.length];
        int dst = 0;

        for (final byte element : colors) {
            merged[dst++] = element;
        }

        for (final byte element : img) {
            merged[dst++] = element;
        }
        return merged;
    }

    /**
     * Compress the image using the ZIP format.
     * @param img the image data.
     * @return the compressed image.
     */
    private byte[] zip(final byte[] img) {
        final Deflater deflater = new Deflater();
        deflater.setInput(img);
        deflater.finish();

        final byte[] compressedData = new byte[img.length * 2];
        final int bytesCompressed = deflater.deflate(compressedData);
        final byte[] newData = Arrays.copyOf(compressedData, bytesCompressed);

        return newData;
    }

    /**
     * Adjust the width of each row in an image so the data is aligned to a
     * 16-bit word boundary when loaded in memory. The additional bytes are
     * all set to zero and will not be displayed in the image.
     *
     * @param imgWidth the width of the image in pixels.
     * @param imgHeight the height of the image in pixels.
     * @param img the image data.
     * @return the image data with each row aligned to a 16-bit boundary.
     */
    private byte[] adjustScan(final int imgWidth, final int imgHeight,
            final byte[] img) {
        int src = 0;
        int dst = 0;
        int row;
        int col;

        int scan = 0;
        byte[] formattedImage = null;

        scan = (imgWidth + 3) & ~3;
        formattedImage = new byte[scan * imgHeight];

        for (row = 0; row < imgHeight; row++) {
            for (col = 0; col < imgWidth; col++) {
                formattedImage[dst++] = img[src++];
            }

            while (col++ < scan) {
                formattedImage[dst++] = 0;
            }
        }

        return formattedImage;
    }

    /**
     * Convert an image with 32-bits for the red, green, blue and alpha channels
     * to one where the channels each take 5-bits in a 16-bit word.
     * @param imgWidth the width of the image in pixels.
     * @param imgHeight the height of the image in pixels.
     * @param img the image data.
     * @return the image data with the red, green and blue channels packed into
     * 16-bit words. Alpha is discarded.
     */
    private byte[] packColours(final int imgWidth, final int imgHeight,
            final byte[] img) {
        int src = 0;
        int dst = 0;
        int row;
        int col;

        final int scan = imgWidth + (imgWidth & 1);
        final byte[] formattedImage = new byte[scan * imgHeight * 2];

        for (row = 0; row < imgHeight; row++) {
            for (col = 0; col < imgWidth; col++, src++) {
                final int red = (img[src++] & 0xF8) << 7;
                final int green = (img[src++] & 0xF8) << 2;
                final int blue = (img[src++] & 0xF8) >> 3;
                final int colour = (red | green | blue) & 0x7FFF;

                formattedImage[dst++] = (byte) (colour >> 8);
                formattedImage[dst++] = (byte) colour;
            }

            while (col < scan) {
                formattedImage[dst++] = 0;
                formattedImage[dst++] = 0;
                col++;
            }
        }
        return formattedImage;
    }

}
