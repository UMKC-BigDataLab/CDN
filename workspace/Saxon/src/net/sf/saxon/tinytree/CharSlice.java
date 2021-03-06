package net.sf.saxon.tinytree;

import java.io.Serializable;
import java.io.Writer;

/**
 * This is an implementation of the JDK 1.4 CharSequence interface: it implements
 * a CharSequence as a view of an array. The implementation relies on the array
 * being immutable: as a minimum, the caller is required to ensure that the array
 * contents will not change so long as the CharSlice remains in existence.
 *
 * This class should be more efficient than String because it avoids copying the
 * characters unnecessarily.
 *
 * The methods in the class don't check their arguments. Incorrect arguments will
 * generally result in exceptions from lower-level classes.
 *
 */
public final class CharSlice implements CharSequence, Serializable {

    private char[] array;
    private int offset;
    private int count;

    /**
     * Create a CharSlice that maps to the whole of a char[] array
     * @param array the char[] array
     */

    public CharSlice(char[] array) {
        this.array = array;
        offset = 0;
        count = array.length;
    }

    /**
     * Create a CharSlice that maps to a section of a char[] array
     * @param array the char[] array
     * @param start position of the first character to be included
     * @param length number of characters to be included
     */

    public CharSlice(char[] array, int start, int length) {
        this.array = array;
        offset = start;
        count = length;
        if (start + length > array.length) {
            throw new IndexOutOfBoundsException("start(" + start +
                    ") + length(" + length + ") > size(" + array.length + ')');
        }
    }

    /**
     * Returns the length of this character sequence.  The length is the number
     * of 16-bit Unicode characters in the sequence. </p>
     *
     * @return  the number of characters in this sequence
     */
    public int length() {
        return count;
    }

    /**
     * Set the length of this character sequence, without changing the array and start offset
     * to which it is bound
     * @param length the new length of the CharSlice (which must be less than the existing length,
     * though this is not enforced)
     */
    public void setLength(int length) {
        count = length;
    }

    /**
     * Returns the character at the specified index.  An index ranges from zero
     * to <tt>length() - 1</tt>.  The first character of the sequence is at
     * index zero, the next at index one, and so on, as for array
     * indexing. </p>
     *
     * @param   index   the index of the character to be returned
     * @return  the specified character
     * @throws  java.lang.IndexOutOfBoundsException
     *          if the <tt>index</tt> argument is negative or not less than
     *          <tt>length()</tt>
     */
    public char charAt(int index) {
        return array[offset+index];
    }

    /**
     * Returns a new character sequence that is a subsequence of this sequence.
     * The subsequence starts with the character at the specified index and
     * ends with the character at index <tt>end - 1</tt>.  The length of the
     * returned sequence is <tt>end - start</tt>, so if <tt>start == end</tt>
     * then an empty sequence is returned. </p>
     *
     * @param   start   the start index, inclusive
     * @param   end     the end index, exclusive
     *
     * @return  the specified subsequence
     *
     * @throws  java.lang.IndexOutOfBoundsException
     *          if <tt>start</tt> or <tt>end</tt> are negative,
     *          if <tt>end</tt> is greater than <tt>length()</tt>,
     *          or if <tt>start</tt> is greater than <tt>end</tt>
     */
    public CharSequence subSequence(int start, int end) {
        return new CharSlice(array, offset+start, end-start);
    }

    /**
     * Convert to a string
     */

    public String toString() {
        return new String(array, offset, count);
    }

    /**
     * Compare equality
     */

    public boolean equals(Object other) {
        if (other instanceof CharSlice) {
            CharSlice cs2 = (CharSlice)other;
            if (count != cs2.count) {
                return false;
            }
            int limit = offset + count;
            for (int j=offset, k=cs2.offset; j<limit; ) {
                if (array[j++] != cs2.array[k++]) {
                    return false;
                }
            }
            return true;
        } else if (other instanceof CharSequence) {
            return count == ((CharSequence)other).length() && toString().equals(other.toString());
        }
        return false;
    }

    /**
     * Generate a hash code
     */

    public int hashCode() {
        // Same algorithm as String#hashCode(), but not cached
        int end = offset+count;
        int h = 0;
        for (int i = offset; i < end; i++) {
            h = 31 * h + array[i];
        }
        return h;
    }

    /**
     * Get the index of a specific character in the sequence. Returns -1 if not found.
     * This method mimics {@link String#indexOf}
     * @param c the character to be found
     * @return the position of the first occurrence of that character, or -1 if not found.
     */

    public int indexOf(char c) {
        int end = offset+count;
        for (int i = offset; i < end; i++) {
            if (array[i] == c) {
                return i-offset;
            }
        }
        return -1;
    }

    /**
     * Returns a new character sequence that is a subsequence of this sequence.
     * Unlike subSequence, this is guaranteed to return a String.
     * @param start position of the first character to be included (relative to the
     * start of the CharSlice, not the underlying array)
     * @param end position of the first character <b>not</b> to be included (relative
     * to the start of the CharSlice)
     * @return the substring, as a String object
     */

    public String substring(int start, int end) {
        return new String(array, offset+start, end-start);
    }

    /**
     * Append the contents to another array at a given offset. The caller is responsible
     * for ensuring that sufficient space is available.
     * @param destination the array to which the characters will be copied
     * @param destOffset the offset in the target array where the copy will start
     */

    public void copyTo(char[] destination, int destOffset) {
        System.arraycopy(array, offset, destination, destOffset, count);
    }

    /**
     * Append the contents to another array at a given offset. The caller is responsible
     * for ensuring that sufficient space is available. Otherwise this behaves like String.getChars()
     * @param start offset of first character to be copied
     * @param end offset of the first character that is not copied
     * @param destination the array to which the characters will be copied
     * @param destOffset the offset in the target array where the copy will start
     */

    public void getChars(int start, int end, char[] destination, int destOffset) {
        System.arraycopy(array, offset+start, destination, destOffset, end - start);
    }    

    /**
     * Write the value to a writer
     * @param writer the writer to be written to
     */

    public void write(Writer writer) throws java.io.IOException {
        writer.write(array, offset, count);
    }

}

//
// The contents of this file are subject to the Mozilla Public License Version 1.0 (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the
// License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is Michael H. Kay
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
// Contributor(s): none
//
