/**
 * Copyright 2014 Groupon.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arpnetworking.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Layout;
import com.arpnetworking.logback.widgets.Widget;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tests for <code>KeyValueEncoder</code>.
 *
 * @author Gil Markham (gil at groupon dot com)
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public class KeyValueEncoderTest {

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        _context = new LoggerContext();
        _context.start();
        _baos = new ByteArrayOutputStream();
        _encoder = new KeyValueEncoder();
        _encoder.setImmediateFlush(true);
        _encoder.init(_baos);
        final PatternLayout layout = new PatternLayout();
        layout.setPattern("[%d{dd MMM yyyy HH:mm:ss.SSS,UTC}] %t - %m%n");
        layout.setContext(_context);
        layout.start();
        _encoder.setLayout(layout);
        _encoder.setContext(_context);
        Mockito.doThrow(new RuntimeException("Mocked Failure")).when(_throwingLayout).doLayout(Mockito.any(ILoggingEvent.class));
    }

    @Test
    public void testEncodeArray() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[2];
        argArray[0] = new String[] {"key1", "key2"};
        argArray[1] = new Object[] {Integer.valueOf(1234), "foo"};
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeArray.log", logOutput);
    }

    @Test
    public void testEncodeArrayThrowsException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[2];
        argArray[0] = new String[] {"key1", "key2"};
        argArray[1] = new Object[] {Integer.valueOf(1234), "foo"};
        event.setArgumentArray(argArray);
        _encoder.setLayout(_throwingLayout);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeArrayThrowsException.log", logOutput);
    }

    @Test
    public void testEncodeArrayDefaultName() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[2];
        argArray[0] = new String[] {"key1", "key2"};
        argArray[1] = new Object[] {Integer.valueOf(1234), "foo"};
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeArrayDefaultName.log", logOutput);
    }

    @Test
    public void testEncodeArrayCustomDefaultName() throws Exception {
        _encoder.setLogEventName("FooBar");
        Assert.assertEquals("FooBar", _encoder.getLogEventName());
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[2];
        argArray[0] = new String[] {"key1", "key2"};
        argArray[1] = new Object[] {Integer.valueOf(1234), "foo"};
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeArrayCustomDefaultName.log", logOutput);
    }

    @Test
    public void testEncodeArrayJson() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_JSON_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[2];
        argArray[0] = new String[] {"key1", "key2"};
        argArray[1] = new String[] {"{\"foo\":\"bar\"}", "[\"foo\":\"bar\"]"};
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeArrayJson.log", logOutput);
    }

    @Test
    public void testEncodeArrayJsonThrowsException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_JSON_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[2];
        argArray[0] = new String[] {"key1", "key2"};
        argArray[1] = new String[] {"{\"foo\":\"bar\"}", "[\"foo\":\"bar\"]"};
        event.setArgumentArray(argArray);
        _encoder.setLayout(_throwingLayout);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeArrayJsonThrowsException.log", logOutput);
    }

    @Test
    public void testEncodeArrayJsonNullValues() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_JSON_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[2];
        argArray[0] = new String[] {"key1", "key2"};
        argArray[1] = null;
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeArrayJsonNullValues.log", logOutput);
    }

    @Test
    public void testEncodeMap() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.MAP_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("key1", Integer.valueOf(1234));
        map.put("key2", "foo");
        final Object[] argArray = new Object[1];
        argArray[0] = map;
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeMap.log", logOutput);
    }

    @Test
    public void testEncodeMapThrowsException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.MAP_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("key1", Integer.valueOf(1234));
        map.put("key2", "foo");
        final Object[] argArray = new Object[1];
        argArray[0] = map;
        event.setArgumentArray(argArray);
        _encoder.setLayout(_throwingLayout);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeMapThrowsException.log", logOutput);
    }

    @Test
    public void testEncodeMapNullMap() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.MAP_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[1];
        argArray[0] = null;
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeMapNullMap.log", logOutput);
    }

    @Test
    public void testEncodeMapJson() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.MAP_JSON_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("key1", "{\"foo\":\"bar\"}");
        map.put("key2", "[\"foo\":\"bar\"]");
        final Object[] argArray = new Object[1];
        argArray[0] = map;
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeMapJson.log", logOutput);
    }

    @Test
    public void testEncodeMapJsonThrowsException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.MAP_JSON_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("key1", "{\"foo\":\"bar\"}");
        map.put("key2", "[\"foo\":\"bar\"]");
        final Object[] argArray = new Object[1];
        argArray[0] = map;
        event.setArgumentArray(argArray);
        _encoder.setLayout(_throwingLayout);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeMapJsonThrowsException.log", logOutput);
    }

    @Test
    public void testEncodeMapJsonNullMap() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.MAP_JSON_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[1];
        argArray[0] = null;
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeMapJsonNullMap.log", logOutput);
    }

    @Test
    public void testEncodeObject() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.OBJECT_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[1];
        argArray[0] = new Widget("foo");
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeObject.log", logOutput);
    }

    @Test
    public void testEncodeObjectThrowsException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.OBJECT_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[1];
        argArray[0] = new Widget("foo");
        event.setArgumentArray(argArray);
        _encoder.setLayout(_throwingLayout);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeObjectThrowsException.log", logOutput);
    }

    @Test
    public void testEncodeObjectNull() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.OBJECT_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[1];
        argArray[0] = null;
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeObjectNull.log", logOutput);
    }

    @Test
    public void testEncodeObjectJson() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.OBJECT_JSON_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[1];
        argArray[0] = "{\"key\":\"value\"}";
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeObjectJson.log", logOutput);
    }

    @Test
    public void testEncodeObjectJsonThrowsException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.OBJECT_JSON_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[1];
        argArray[0] = "{\"key\":\"value\"}";
        event.setArgumentArray(argArray);
        _encoder.setLayout(_throwingLayout);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeObjectJsonThrowsException.log", logOutput);
    }

    @Test
    public void testEncodeObjectJsonNull() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.OBJECT_JSON_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[1];
        argArray[0] = null;
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeObjectJsonNull.log", logOutput);
    }

    @Test
    public void testEncodeLists() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.LISTS_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[4];
        argArray[0] = Collections.singletonList("dataKey");
        argArray[1] = Collections.singletonList("dataValue");
        argArray[2] = Collections.singletonList("contextKey");
        argArray[3] = Collections.singletonList("contextValue");
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeLists.log", logOutput);
    }

    @Test
    public void testEncodeListsThrowsException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.LISTS_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[4];
        argArray[0] = Collections.singletonList("dataKey");
        argArray[1] = Collections.singletonList("dataValue");
        argArray[2] = Collections.singletonList("contextKey");
        argArray[3] = Collections.singletonList("contextValue");
        event.setArgumentArray(argArray);
        _encoder.setLayout(_throwingLayout);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeListsThrowsException.log", logOutput);
    }

    @Test
    public void testEncodeListsEmpty() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.LISTS_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[4];
        argArray[0] = Collections.emptyList();
        argArray[1] = Collections.emptyList();
        argArray[2] = Collections.emptyList();
        argArray[3] = Collections.emptyList();
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeListsEmpty.log", logOutput);
    }

    @Test
    public void testEncodeListsNull() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.LISTS_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[4];
        argArray[0] = null;
        argArray[1] = null;
        argArray[2] = null;
        argArray[3] = null;
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeListsNull.log", logOutput);
    }

    @Test
    public void testEncodeListsNullValues() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.LISTS_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[4];
        argArray[0] = Collections.singletonList("dataKey");
        argArray[1] = null;
        argArray[2] = Collections.singletonList("contextKey");
        argArray[3] = null;
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeListsNullValues.log", logOutput);
    }

    @Test
    public void testEncodeListsNullMismatch() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.LISTS_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[4];
        argArray[0] = Collections.singletonList("dataKey");
        argArray[1] = null;
        argArray[2] = null;
        argArray[3] = Collections.singletonList("contextValue");
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeListsNullMismatch.log", logOutput);
    }

    @Test
    public void testEncodeStandardEvent() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMessage("logEvent: foo = {}");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[2];
        argArray[0] = "bar";
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeStandardEvent.log", logOutput);
    }

    @Test
    public void testEncodeStandardEventThrowsException() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMessage("logEvent: foo = {}");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[2];
        argArray[0] = "bar";
        event.setArgumentArray(argArray);
        _encoder.setLayout(_throwingLayout);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeStandardEventThrowsException.log", logOutput);
    }

    @Test
    public void testEncodeArrayEmptyKeysAndValues() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[2];
        argArray[0] = new String[] {};
        argArray[1] = new Object[] {};
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeArrayEmptyKeysAndValues.log", logOutput);
    }

    @Test
    public void testEncodeArrayStringNullKeys() throws Exception {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setMarker(StenoMarker.ARRAY_MARKER);
        event.setMessage("logEvent");
        event.setThreadName("thread");
        event.setTimeStamp(0);
        event.setLoggerContextRemoteView(_context.getLoggerContextRemoteView());
        final Object[] argArray = new Object[2];
        argArray[0] = null;
        argArray[1] = new Object[] {Integer.valueOf(1234), "foo"};
        event.setArgumentArray(argArray);
        _encoder.doEncode(event);
        final String logOutput = _baos.toString(StandardCharsets.UTF_8.name());
        assertOutput("KeyValueEncoderTest.testEncodeArrayStringNullKeys.log", logOutput);
    }

    private static void assertOutput(final String expectedResource, final String actualOutput) {
        final String actualOutputSanitized = actualOutput.replaceAll("host=[^,\\}]+", "host=<HOST>")
                .replaceAll("processId=[^,\\}]+", "processId=<PROCESS_ID>")
                .replaceAll("threadId=[^,\\}]+", "threadId=<THREAD_ID>");
        try {
            final URL resource = KeyValueEncoderTest.class.getClassLoader().getResource("com/arpnetworking/logback/" + expectedResource);
            Assert.assertEquals(
                    // CHECKSTYLE.OFF: IllegalInstantiation - This is how you do it.
                    new String(Files.readAllBytes(Paths.get(resource.toURI())), StandardCharsets.UTF_8),
                    // CHECKSTYLE.ON: IllegalInstantiation
                    actualOutputSanitized);
        } catch (final IOException | URISyntaxException e) {
            Assert.fail("Failed with exception: " + e);
        }
    }

    private KeyValueEncoder _encoder;
    private ByteArrayOutputStream _baos;
    private LoggerContext _context;
    @Mock
    private Layout<ILoggingEvent> _throwingLayout;
}
