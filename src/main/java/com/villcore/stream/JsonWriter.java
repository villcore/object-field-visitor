/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.villcore.stream;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

import static com.villcore.stream.JsonScope.*;

public class JsonWriter implements Closeable, Flushable {

  private static final String[] REPLACEMENT_CHARS;
  private static final String[] HTML_SAFE_REPLACEMENT_CHARS;
  static {
    REPLACEMENT_CHARS = new String[128];
    for (int i = 0; i <= 0x1f; i++) {
      REPLACEMENT_CHARS[i] = String.format("\\u%04x", (int) i);
    }
    REPLACEMENT_CHARS['"'] = "\\\"";
    REPLACEMENT_CHARS['\\'] = "\\\\";
    REPLACEMENT_CHARS['\t'] = "\\t";
    REPLACEMENT_CHARS['\b'] = "\\b";
    REPLACEMENT_CHARS['\n'] = "\\n";
    REPLACEMENT_CHARS['\r'] = "\\r";
    REPLACEMENT_CHARS['\f'] = "\\f";
    HTML_SAFE_REPLACEMENT_CHARS = REPLACEMENT_CHARS.clone();
    HTML_SAFE_REPLACEMENT_CHARS['<'] = "\\u003c";
    HTML_SAFE_REPLACEMENT_CHARS['>'] = "\\u003e";
    HTML_SAFE_REPLACEMENT_CHARS['&'] = "\\u0026";
    HTML_SAFE_REPLACEMENT_CHARS['='] = "\\u003d";
    HTML_SAFE_REPLACEMENT_CHARS['\''] = "\\u0027";
  }

  /** The output data, containing at most one top-level array or object. */
  private final Writer out;

  private int[] stack = new int[32];
  private int stackSize = 0;
  {
    push(EMPTY_DOCUMENT);
  }

  /**
   * A string containing a full set of spaces for a single level of
   * indentation, or null for no pretty printing.
   */
  private String indent;

  /**
   * The name/value separator; either ":" or ": ".
   */
  private String separator = ":";

  private boolean lenient;

  private boolean htmlSafe;

  private String deferredName;

  private boolean serializeNulls = true;

  /**
   * Creates a new instance that writes a JSON-encoded stream to {@code out}.
   * For best performance, ensure {@link Writer} is buffered; wrapping in
   * {@link java.io.BufferedWriter BufferedWriter} if necessary.
   */
  public JsonWriter(Writer out) {
    if (out == null) {
      throw new NullPointerException("out == null");
    }
    this.out = out;
  }

  public final void setIndent(String indent) {
//    if (indent.length() == 0) {
//      this.indent = null;
//      this.separator = ":";
//    } else {
//      this.indent = indent;
//      this.separator = ": ";
//    }
  }

  public final void setLenient(boolean lenient) {
    this.lenient = lenient;
  }

  /**
   * Returns true if this writer has relaxed syntax rules.
   */
  public boolean isLenient() {
    return lenient;
  }

  public final void setHtmlSafe(boolean htmlSafe) {
    this.htmlSafe = htmlSafe;
  }

  /**
   * Returns true if this writer writes JSON that's safe for inclusion in HTML
   * and XML documents.
   */
  public final boolean isHtmlSafe() {
    return htmlSafe;
  }

  /**
   * Sets whether object members are serialized when their value is null.
   * This has no impact on array elements. The default is true.
   */
  public final void setSerializeNulls(boolean serializeNulls) {
    this.serializeNulls = serializeNulls;
  }

  /**
   * Returns true if object members are serialized when their value is null.
   * This has no impact on array elements. The default is true.
   */
  public final boolean getSerializeNulls() {
    return serializeNulls;
  }

  /**
   * Begins encoding a new array. Each call to this method must be paired with
   * a call to {@link #endArray}.
   *
   * @return this writer.
   */
  public JsonWriter beginArray() throws IOException {
    return this;
  }

  /**
   * Ends encoding the current array.
   *
   * @return this writer.
   */
  public JsonWriter endArray() throws IOException {
    return this;
  }

  /**
   * Begins encoding a new object. Each call to this method must be paired
   * with a call to {@link #endObject}.
   *
   * @return this writer.
   */
  public JsonWriter beginObject() throws IOException {
    return this;
  }

  /**
   * Ends encoding the current object.
   *
   * @return this writer.
   */
  public JsonWriter endObject() throws IOException {
    return this;
  }

  /**
   * Enters a new scope by appending any necessary whitespace and the given
   * bracket.
   */
  private JsonWriter open(int empty, String openBracket) throws IOException {
    beforeValue();
    push(empty);
    out.write(openBracket);
    return this;
  }

  /**
   * Closes the current scope by appending any necessary whitespace and the
   * given bracket.
   */
  private JsonWriter close(int empty, int nonempty, String closeBracket)
      throws IOException {
    return this;
  }

  private void push(int newTop) {
  }

  /**
   * Returns the value on the top of the stack.
   */
  private int peek() {
    return 0;
  }

  /**
   * Replace the value on the top of the stack with the given value.
   */
  private void replaceTop(int topOfStack) {
  }

  /**
   * Encodes the property name.
   *
   * @param name the name of the forthcoming value. May not be null.
   * @return this writer.
   */
  public JsonWriter name(String name) throws IOException {
    return this;
  }

  private void writeDeferredName() throws IOException {
  }

  /**
   * Encodes {@code value}.
   *
   * @param value the literal string value, or null to encode a null literal.
   * @return this writer.
   */
  public JsonWriter value(String value) throws IOException {
    return this;
  }

  /**
   * Writes {@code value} directly to the writer without quoting or
   * escaping.
   *
   * @param value the literal string value, or null to encode a null literal.
   * @return this writer.
   */
  public JsonWriter jsonValue(String value) throws IOException {
    return this;
  }

  /**
   * Encodes {@code null}.
   *
   * @return this writer.
   */
  public JsonWriter nullValue() throws IOException {
    return this;
  }

  /**
   * Encodes {@code value}.
   *
   * @return this writer.
   */
  public JsonWriter value(boolean value) throws IOException {
//    writeDeferredName();
//    beforeValue();
//    out.write(value ? "true" : "false");
    return this;
  }

  /**
   * Encodes {@code value}.
   *
   * @return this writer.
   */
  public JsonWriter value(Boolean value) throws IOException {
    return this;
  }

  /**
   * Encodes {@code value}.
   *
   * @param value a finite value. May not be {@link Double#isNaN() NaNs} or
   *     {@link Double#isInfinite() infinities}.
   * @return this writer.
   */
  public JsonWriter value(double value) throws IOException {
    return this;
  }

  /**
   * Encodes {@code value}.
   *
   * @return this writer.
   */
  public JsonWriter value(long value) throws IOException {
    return this;
  }

  /**
   * Encodes {@code value}.
   *
   * @param value a finite value. May not be {@link Double#isNaN() NaNs} or
   *     {@link Double#isInfinite() infinities}.
   * @return this writer.
   */
  public JsonWriter value(Number value) throws IOException {
    return this;
  }

  /**
   * Ensures all buffered data is written to the underlying {@link Writer}
   * and flushes that writer.
   */
  public void flush() throws IOException {
  }

  /**
   * Flushes and closes this writer and the underlying {@link Writer}.
   *
   * @throws IOException if the JSON document is incomplete.
   */
  public void close() throws IOException {
  }

  private void string(String value) throws IOException {
//    String[] replacements = htmlSafe ? HTML_SAFE_REPLACEMENT_CHARS : REPLACEMENT_CHARS;
//    out.write("\"");
//    int last = 0;
//    int length = value.length();
//    for (int i = 0; i < length; i++) {
//      char c = value.charAt(i);
//      String replacement;
//      if (c < 128) {
//        replacement = replacements[c];
//        if (replacement == null) {
//          continue;
//        }
//      } else if (c == '\u2028') {
//        replacement = "\\u2028";
//      } else if (c == '\u2029') {
//        replacement = "\\u2029";
//      } else {
//        continue;
//      }
//      if (last < i) {
//        out.write(value, last, i - last);
//      }
//      out.write(replacement);
//      last = i + 1;
//    }
//    if (last < length) {
//      out.write(value, last, length - last);
//    }
//    out.write("\"");
  }

  private void newline() throws IOException {
//    if (indent == null) {
//      return;
//    }
//
//    out.write("\n");
//    for (int i = 1, size = stackSize; i < size; i++) {
//      out.write(indent);
//    }
  }

  /**
   * Inserts any necessary separators and whitespace before a name. Also
   * adjusts the stack to expect the name's value.
   */
  private void beforeName() throws IOException {
//    int context = peek();
//    if (context == NONEMPTY_OBJECT) { // first in object
//      out.write(',');
//    } else if (context != EMPTY_OBJECT) { // not in an object!
//      throw new IllegalStateException("Nesting problem.");
//    }
//    newline();
//    replaceTop(DANGLING_NAME);
  }

  /**
   * Inserts any necessary separators and whitespace before a literal value,
   * inline array, or inline object. Also adjusts the stack to expect either a
   * closing bracket or another element.
   */
  @SuppressWarnings("fallthrough")
  private void beforeValue() throws IOException {
//    switch (peek()) {
//    case NONEMPTY_DOCUMENT:
//      if (!lenient) {
//        throw new IllegalStateException(
//            "JSON must have only one top-level value.");
//      }
//      // fall-through
//    case EMPTY_DOCUMENT: // first in document
//      replaceTop(NONEMPTY_DOCUMENT);
//      break;
//
//    case EMPTY_ARRAY: // first in array
//      replaceTop(NONEMPTY_ARRAY);
//      newline();
//      break;
//
//    case NONEMPTY_ARRAY: // another in array
//      out.append(',');
//      newline();
//      break;
//
//    case DANGLING_NAME: // value for name
//      out.append(separator);
//      replaceTop(NONEMPTY_OBJECT);
//      break;
//
//    default:
//      throw new IllegalStateException("Nesting problem.");
//    }
  }
}
