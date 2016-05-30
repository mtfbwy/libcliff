package τ.typedef.io;

import java.io.IOException;
import java.io.OutputStream;

class FormatCodecBase64 {

    private static final char[] ENCODE_ALPHABET = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', '+', '/'
    };

    private static InstallmentByteBuffer encode(byte[] a, int i, int j) {
        assert a != null;
        assert i >= 0 && i < a.length;
        assert j >= i && j < a.length;
        // encode
        InstallmentByteBuffer ibb = new InstallmentByteBuffer();
        while (j - i >= 3) {
            ibb.append(encode3(a, i));
            i += 3;
        }
        switch (j - i) {
            case 2:
                ibb.append(encode3(new byte[] {
                        a[i], a[i + 1], 0
                }, i));
                ibb.append('=');
                break;
            case 1:
                ibb.append(encode3(new byte[] {
                        a[i], 0, 0
                }, i));
                ibb.append('=');
                ibb.append('=');
                break;
            case 0:
                break;
            default:
                throw new IllegalArgumentException();
        }
        return ibb;
    }

    public static byte[] encode(byte[] a, int bytesPerLine,
            int firstOffset, byte[] prefix, byte[] suffix)
            throws IOException {
        return encode(a, 0, a.length, bytesPerLine, firstOffset,
                prefix, suffix);
    }

    public static OutputStream encode(byte[] a, int bytesPerLine,
            int firstOffset, byte[] prefix, byte[] suffix, OutputStream o)
            throws IOException {
        return encode(a, 0, a.length, bytesPerLine, firstOffset,
                prefix, suffix, o);
    }

    public static byte[] encode(byte[] a, int i, int n, int bytesPerLine,
            int firstOffset, byte[] prefix, byte[] suffix)
            throws IOException {
        InstallmentByteBuffer o = new InstallmentByteBuffer();
        encode(a, i, n, bytesPerLine, firstOffset, prefix, suffix, o);
        return o.toByteArray();
    }

    /**
     * it is user who should put the prefix/suffix before/after invoke this method if necessary
     */
    public static OutputStream encode(byte[] a, int i, int n,
            int bytesPerLine, int firstOffset,
            byte[] prefix, byte[] suffix, OutputStream o)
            throws IOException {
        assert a != null;
        assert i >= 0 && i < a.length;
        assert n >= 0 && i + n <= a.length;
        assert bytesPerLine > 0;
        assert firstOffset >= 0;
        if (firstOffset > 0) {
            assert bytesPerLine >= prefix.length + suffix.length
                    + firstOffset;
        } else {
            assert bytesPerLine > prefix.length + suffix.length;
        }

        InstallmentByteBuffer.Reader ibbr = encode(a, i, i + n).reader();

        ibbr.rewind();
        int m = ibbr.size();

        // the first line
        int rest = bytesPerLine - firstOffset - suffix.length;
        if (rest > m) {
            rest = m;
        }
        for (int j = 0; j < rest; ++i) {
            o.write(ibbr.next());
        }
        m -= rest;
        if (m > 0) {
            o.write(suffix);
        }

        // middle lines
        rest = bytesPerLine - prefix.length - suffix.length;
        while (m >= rest) {
            o.write(prefix);
            while (rest-- > 0) {
                o.write(ibbr.next());
            }
            o.write(suffix);
            m -= rest;
        }

        if (m > 0) {
            rest = m;
            o.write(prefix);
            while (rest-- > 0) {
                o.write(ibbr.next());
            }
        }
        return o;
    }

    private static byte[] encode3(byte[] a, int i) {
        return encode3(a, i, new byte[4], 0);
    }

    private static byte[] encode3(byte[] a, int i, byte[] result, int offset) {
        assert a != null;
        assert i >= 0 && i + 3 < a.length;
        assert result != null;
        assert offset >= 0 && offset + 4 < result.length;
        result[0] = (byte) ENCODE_ALPHABET[a[i] >>> 2];
        result[1] = (byte) ENCODE_ALPHABET[((0x03 & a[i]) << 4)
                | (a[i + 1] >>> 4)];
        result[2] = (byte) ENCODE_ALPHABET[((0x0F & a[i + 1]) << 2)
                | (a[i + 2] >>> 6)];
        result[3] = (byte) ENCODE_ALPHABET[0x3F & a[i + 2]];
        return result;
    }
}
