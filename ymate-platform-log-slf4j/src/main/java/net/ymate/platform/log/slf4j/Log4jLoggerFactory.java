/*
 * Copyright 2007-2017 the original author or authors.
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
package net.ymate.platform.log.slf4j;

import net.ymate.platform.log.ILog;
import net.ymate.platform.log.Logs;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.AbstractLoggerAdapter;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.util.ReflectionUtil;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/4 上午11:28
 * @version 1.0
 */
public class Log4jLoggerFactory extends AbstractLoggerAdapter<Logger> implements ILoggerFactory {

    private static final String FQCN = Log4jLoggerFactory.class.getName();
    private static final String PACKAGE = "org.slf4j";

    private ILog __logs;

    @Override
    protected Logger newLogger(final String name, final LoggerContext context) {
        Logger _logger = null;
        if (context == null) {
            try {
                String _key = Logger.ROOT_LOGGER_NAME.equals(name) ? __logs.getModuleCfg().getLoggerName() : name;
                if (StringUtils.isBlank(_key)) {
                    _logger = new Log4jLogger((org.apache.logging.log4j.Logger) __logs.getLogger().getLoggerImpl(), name, __logs.getModuleCfg().allowOutputConsole());
                } else {
                    _logger = new Log4jLogger((org.apache.logging.log4j.Logger) __logs.getLogger().getLogger(_key).getLoggerImpl(), name, __logs.getModuleCfg().allowOutputConsole());
                }
            } catch (Exception e) {
                _logger = new Log4jLogger((org.apache.logging.log4j.Logger) __logs.getLogger().getLoggerImpl(), name, __logs.getModuleCfg().allowOutputConsole());
            }
        } else {
            _logger = new Log4jLogger(context.getLogger(Logger.ROOT_LOGGER_NAME.equals(name) ? LogManager.ROOT_LOGGER_NAME : name), name, false);
        }
        return _logger;
    }

    @Override
    protected LoggerContext getContext() {
        __logs = Logs.get();
        if (__logs == null || !__logs.isInited()) {
            final Class<?> anchor = ReflectionUtil.getCallerClass(FQCN, PACKAGE);
            return anchor == null ? LogManager.getContext() : getContext(ReflectionUtil.getCallerClass(anchor));
        }
        return null;
    }
}
