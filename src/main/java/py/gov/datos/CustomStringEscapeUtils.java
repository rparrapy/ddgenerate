package py.gov.datos;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by rparra on 2/7/15.
 * Extraido de StringEscapeUtils para poder redefinir el caracter delimitador
 */
public class CustomStringEscapeUtils {
    /**
     * Translator object for escaping individual Comma Separated Values.
     * <p>
     * While {@link #escapeCsv(String)} is the expected method of use, this
     * object allows the CSV escaping functionality to be used
     * as the foundation for a custom translator.
     *
     * @since 3.0
     */
    public static final CharSequenceTranslator ESCAPE_CSV = new CsvEscaper();

    static class CsvEscaper extends CharSequenceTranslator {

        private static final char CSV_DELIMITER = ';';
        private static final char CSV_QUOTE = '"';
        private static final String CSV_QUOTE_STR = String.valueOf(CSV_QUOTE);
        private static final char[] CSV_SEARCH_CHARS =
                new char[]{CSV_DELIMITER, CSV_QUOTE, CharUtils.CR, CharUtils.LF};

        @Override
        public int translate(final CharSequence input, final int index, final Writer out) throws IOException {

            if (index != 0) {
                throw new IllegalStateException("CsvEscaper should never reach the [1] index");
            }


            if (StringUtils.containsNone(input.toString(), CSV_SEARCH_CHARS)) {
                out.write(input.toString());
            } else {
                String tmp;
                out.write(CSV_QUOTE);
                tmp = StringUtils.replace(input.toString(), CSV_QUOTE_STR, CSV_QUOTE_STR + CSV_QUOTE_STR);
                tmp = StringUtils.replace(tmp, "\n", " ");
                out.write(tmp);
                out.write(CSV_QUOTE);
            }
            return Character.codePointCount(input, 0, input.length());
        }
    }

    public static final String escapeCsv(final String input) {
        return ESCAPE_CSV.translate(input);
    }

    /**
     * Translator object for unescaping escaped Comma Separated Value entries.
     * <p>
     * While {@link #unescapeCsv(String)} is the expected method of use, this
     * object allows the CSV unescaping functionality to be used
     * as the foundation for a custom translator.
     *
     * @since 3.0
     */
    public static final CharSequenceTranslator UNESCAPE_CSV = new CsvUnescaper();

    static class CsvUnescaper extends CharSequenceTranslator {

        private static final char CSV_DELIMITER = ';';
        private static final char CSV_QUOTE = '"';
        private static final String CSV_QUOTE_STR = String.valueOf(CSV_QUOTE);
        private static final char[] CSV_SEARCH_CHARS =
                new char[]{CSV_DELIMITER, CSV_QUOTE, CharUtils.CR, CharUtils.LF};

        @Override
        public int translate(final CharSequence input, final int index, final Writer out) throws IOException {
            if (index != 0) {
                throw new IllegalStateException("CsvUnescaper should never reach the [1] index");
            }

            if (input.charAt(0) != CSV_QUOTE || input.charAt(input.length() - 1) != CSV_QUOTE) {
                out.write(input.toString());
                return Character.codePointCount(input, 0, input.length());
            }

            // strip quotes
            final String quoteless = input.subSequence(1, input.length() - 1).toString();

            if (StringUtils.containsAny(quoteless, CSV_SEARCH_CHARS)) {
                // deal with escaped quotes; ie) ""
                out.write(StringUtils.replace(quoteless, CSV_QUOTE_STR + CSV_QUOTE_STR, CSV_QUOTE_STR));
            } else {
                out.write(input.toString());
            }
            return Character.codePointCount(input, 0, input.length());
        }
    }

    public static final String unescapeCsv(final String input) {
        return UNESCAPE_CSV.translate(input);
    }
}
