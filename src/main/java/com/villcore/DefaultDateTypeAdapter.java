/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.villcore;

import com.villcore.visitor.Visitor;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This type adapter supports three subclasses of date: Date, Timestamp, and
 * java.sql.Date.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
final class DefaultDateTypeAdapter extends TypeAdapter<Date> {

    private static final String SIMPLE_NAME = "DefaultDateTypeAdapter";

    private final Class<? extends Date> dateType;
    private final DateFormat enUsFormat;
    private final DateFormat localFormat;

    DefaultDateTypeAdapter(Class<? extends Date> dateType) {
        this(dateType,
                DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US),
                DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT));
    }

    DefaultDateTypeAdapter(Class<? extends Date> dateType, String datePattern) {
        this(dateType, new SimpleDateFormat(datePattern, Locale.US), new SimpleDateFormat(datePattern));
    }

    DefaultDateTypeAdapter(Class<? extends Date> dateType, int style) {
        this(dateType, DateFormat.getDateInstance(style, Locale.US), DateFormat.getDateInstance(style));
    }

    public DefaultDateTypeAdapter(int dateStyle, int timeStyle) {
        this(Date.class,
                DateFormat.getDateTimeInstance(dateStyle, timeStyle, Locale.US),
                DateFormat.getDateTimeInstance(dateStyle, timeStyle));
    }

    public DefaultDateTypeAdapter(Class<? extends Date> dateType, int dateStyle, int timeStyle) {
        this(dateType,
                DateFormat.getDateTimeInstance(dateStyle, timeStyle, Locale.US),
                DateFormat.getDateTimeInstance(dateStyle, timeStyle));
    }

    DefaultDateTypeAdapter(final Class<? extends Date> dateType, DateFormat enUsFormat, DateFormat localFormat) {
        if (dateType != Date.class && dateType != java.sql.Date.class && dateType != Timestamp.class) {
            throw new IllegalArgumentException("Date type must be one of " + Date.class + ", " + Timestamp.class + ", or " + java.sql.Date.class + " but was " + dateType);
        }
        this.dateType = dateType;
        this.enUsFormat = enUsFormat;
        this.localFormat = localFormat;
    }

    // These methods need to be synchronized since JDK DateFormat classes are not thread-safe
    // See issue 162
    @Override
    public void visit(Date value, Visitor visitor) throws Exception {
        if (value == null) {
            return;
        }
        synchronized (localFormat) {
            String dateFormatAsString = enUsFormat.format(value);
            // out.value(dateFormatAsString);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SIMPLE_NAME);
        sb.append('(').append(localFormat.getClass().getSimpleName()).append(')');
        return sb.toString();
    }
}
