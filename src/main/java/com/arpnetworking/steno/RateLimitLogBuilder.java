/**
 * Copyright 2015 Groupon.com
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
package com.arpnetworking.steno;

import com.arpnetworking.logback.annotations.LogValue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Limits actual log output to at most once per specified <code>Period</code>.
 * The implementation will add two data attributes <code>_skipped</code> and
 * <code>_lastLogTime</code> to the wrapped <code>LogBuilder</code> instance.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 * @since 1.9.0
 * @deprecated This class is not thread safe and easily leads to misuse. Use <code>RateLimitLogger</code>.
 */
@Deprecated
public class RateLimitLogBuilder implements LogBuilder {

    /**
     * Public constructor.
     *
     * @since 1.9.0
     *
     * @param logBuilder Instance of <code>LogBuilder</code>.
     * @param duration Minimum time between log message output.
     */
    public RateLimitLogBuilder(final LogBuilder logBuilder, final Duration duration) {
        this(logBuilder, duration, Clock.systemUTC());
    }

    /**
     * Package private constructor.
     *
     * @since 1.9.0
     *
     * @param logBuilder Instance of <code>LogBuilder</code>.
     * @param duration Minimum time between log message output.
     * @param clock Instance of <code>Clock</code>.
     */
    /* package private */ RateLimitLogBuilder(final LogBuilder logBuilder, final Duration duration, final Clock clock) {
        _logBuilder = logBuilder;
        _duration = duration;
        _clock = clock;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogBuilder setEvent(final String value) {
        _logBuilder.setEvent(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogBuilder setMessage(final String value) {
        _logBuilder.setMessage(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogBuilder setThrowable(final Throwable value) {
        _logBuilder.setThrowable(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogBuilder addData(final String name, final Object value) {
        _logBuilder.addData(name, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LogBuilder addContext(final String name, final Object value) {
        _logBuilder.addContext(name, value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void log() {
        final Instant now = _clock.instant();
        boolean shouldLog = true;
        if (_lastLogTime.isPresent()) {
            if (!_lastLogTime.get().plus(_duration).isBefore(now)) {
                shouldLog = false;
                ++_skipped;
            }
        }
        if (shouldLog) {
            _logBuilder
                    .addData("_skipped", _skipped)
                    .addData("_lastLogTime", _lastLogTime)
                    .log();
            _lastLogTime = Optional.of(now);
            _skipped = 0;
        }
    }

    /**
     * Generate a Steno log compatible representation.
     *
     * @since 1.9.0
     *
     * @return Steno log compatible representation.
     */
    @LogValue
    public Object toLogValue() {
        return LogValueMapFactory.<String, Object>builder()
                .put("logBuilder", _logBuilder)
                .put("duration", _duration)
                .put("lastLogTime", _lastLogTime)
                .put("skipped", _skipped)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toLogValue().toString();
    }

    private final LogBuilder _logBuilder;
    private final Duration _duration;
    private final Clock _clock;
    private Optional<Instant> _lastLogTime = Optional.empty();
    private int _skipped = 0;
}
