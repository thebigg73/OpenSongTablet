package com.garethevans.church.opensongtablet.filemanagement;

import android.annotation.SuppressLint;
import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

class UnicodeBOMInputStream extends InputStream {

    static final class BOM {

        static final BOM NONE = new BOM(new byte[]{}, "UTF-8");

        /*static final BOM WINDOWS = new BOM(new byte[]{}, "WINDOWS-1252");
*/
        static final BOM UTF_8 = new BOM(new byte[]{(byte) 0xEF,
                (byte) 0xBB,
                (byte) 0xBF},
                "UTF-8");

        static final BOM UTF_16_LE = new BOM(new byte[]{(byte) 0xFF,
                (byte) 0xFE},
                "UTF-16LE");

        static final BOM UTF_16_BE = new BOM(new byte[]{(byte) 0xFE,
                (byte) 0xFF},
                "UTF-16BE");

        static final BOM UTF_32_LE = new BOM(new byte[]{(byte) 0xFF,
                (byte) 0xFE,
                (byte) 0x00,
                (byte) 0x00},
                "UTF-32LE");

        static final BOM UTF_32_BE = new BOM(new byte[]{(byte) 0x00,
                (byte) 0x00,
                (byte) 0xFE,
                (byte) 0xFF},
                "UTF-32BE");

        //Returns a <code>String</code> representation of this <code>BOM</code>
        @NonNull
        public final String toString() {
            return description;
        }

        //Returns the bytes corresponding to this <code>BOM</code> value.
        /*public final byte[] getBytes() {
            final int length = bytes.length;
            final byte[] result = new byte[length];

            // Make a defensive copy
            System.arraycopy(bytes, 0, result, 0, length);

            return result;
        }*/

        @SuppressLint("Assert")
        private BOM(final byte[] bom, final String description) {
            assert (bom != null) : "invalid BOM: null is not allowed";
            assert (description != null) : "invalid description: null is not allowed";
            assert (description.length() != 0) : "invalid description: empty string is not allowed";

            this.bytes = bom;
            this.description = description;
        }

        final byte[] bytes;
        private final String description;

    } // BOM

    UnicodeBOMInputStream(final InputStream inputStream) throws NullPointerException,
            IOException {
        if (inputStream == null)
            throw new NullPointerException("invalid input stream: null is not allowed");

        in = new PushbackInputStream(inputStream, 4);
        final byte[] bom = new byte[4];
        final int read = in.read(bom);

        switch (read) {
            case 4:
                if ((bom[0] == (byte) 0xFF) &&
                        (bom[1] == (byte) 0xFE) &&
                        (bom[2] == (byte) 0x00) &&
                        (bom[3] == (byte) 0x00)) {
                    this.bom = BOM.UTF_32_LE;
                    break;
                } else if ((bom[0] == (byte) 0x00) &&
                        (bom[1] == (byte) 0x00) &&
                        (bom[2] == (byte) 0xFE) &&
                        (bom[3] == (byte) 0xFF)) {
                    this.bom = BOM.UTF_32_BE;
                    break;
                }

            case 3:
                if ((bom[0] == (byte) 0xEF) &&
                        (bom[1] == (byte) 0xBB) &&
                        (bom[2] == (byte) 0xBF)) {
                    this.bom = BOM.UTF_8;
                    break;
                }

            case 2:
                if ((bom[0] == (byte) 0xFF) &&
                        (bom[1] == (byte) 0xFE)) {
                    this.bom = BOM.UTF_16_LE;
                    break;
                } else if ((bom[0] == (byte) 0xFE) &&
                        (bom[1] == (byte) 0xFF)) {
                    this.bom = BOM.UTF_16_BE;
                    break;
                }

            default:
                this.bom = BOM.NONE;
                break;
        }

        if (read > 0)
            in.unread(bom, 0, read);
    }

    /**
     * Returns the <code>BOM</code> that was detected in the wrapped
     * <code>InputStream</code> object.
     *
     * @return a <code>BOM</code> value.
     */
    final BOM getBOM() {
        // BOM type is immutable.
        return bom;
    }
/*
    *
     * Skips the <code>BOM</code> that was found in the wrapped
     * <code>InputStream</code> object.
     *
     * @return this <code>UnicodeBOMInputStream</code>.
     * @throws IOException when trying to skip the BOM from the wrapped
     *                     <code>InputStream</code> object.
    */
    /*public final synchronized UnicodeBOMInputStream skipBOM() throws IOException {
        if (!skipped) {
            in.skip(bom.bytes.length);
            skipped = true;
        }
        return this;
    }*/

    /**
     * {@inheritDoc}
     */
    public int read() throws IOException {
        return in.read();
    }

    /**
     * {@inheritDoc}
     */
    public int read(@NonNull final byte[] b) throws IOException,
            NullPointerException {
        return in.read(b, 0, b.length);
    }

    /**
     * {@inheritDoc}
     */
    public int read(@NonNull final byte[] b,
                    final int off,
                    final int len) throws IOException,
            NullPointerException {
        return in.read(b, off, len);
    }

    /**
     * {@inheritDoc}
     */
    public long skip(final long n) throws IOException {
        return in.skip(n);
    }

    /**
     * {@inheritDoc}
     */
    public int available() throws IOException {
        return in.available();
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws IOException {
        in.close();
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void mark(final int readlimit) {
        in.mark(readlimit);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void reset() throws IOException {
        in.reset();
    }

    /**
     * {@inheritDoc}
     */
    public boolean markSupported() {
        return in.markSupported();
    }

    private final PushbackInputStream in;
    private final BOM bom;
    //private boolean skipped = false;

}