package com.jyz.component.core.logging.slf4j;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.spi.LocationAwareLogger;

import com.jyz.component.core.logging.Log;
import com.jyz.component.core.logging.LogFactory;

/**
 * 
 * @author JoyoungZhang@gmail.com
 *
 */
class Slf4jLocationAwareLoggerImpl implements Log {
  
  private static Marker MARKER = MarkerFactory.getMarker(LogFactory.MARKER);

  private static final String FQCN = Slf4jImpl.class.getName();

  private LocationAwareLogger logger;

  Slf4jLocationAwareLoggerImpl(LocationAwareLogger logger) {
    this.logger = logger;
  }

  public boolean isDebugEnabled() {
    return logger.isDebugEnabled();
  }

  public boolean isTraceEnabled() {
    return logger.isTraceEnabled();
  }

  public void error(String s, Throwable e) {
    logger.log(MARKER, FQCN, LocationAwareLogger.ERROR_INT, s, null, e);
  }

  public void error(String s) {
    logger.log(MARKER, FQCN, LocationAwareLogger.ERROR_INT, s, null, null);
  }

  public void debug(String s) {
    logger.log(MARKER, FQCN, LocationAwareLogger.DEBUG_INT, s, null, null);
  }

  public void trace(String s) {
    logger.log(MARKER, FQCN, LocationAwareLogger.TRACE_INT, s, null, null);
  }

  public void warn(String s) {
    logger.log(MARKER, FQCN, LocationAwareLogger.WARN_INT, s, null, null);
  }

}
