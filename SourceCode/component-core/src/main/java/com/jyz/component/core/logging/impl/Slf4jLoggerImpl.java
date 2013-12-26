package com.jyz.component.core.logging.impl;

import org.slf4j.Logger;

import com.jyz.component.core.logging.Log;

/**
 * 
 * @author JoyoungZhang@gmail.com
 * 
 */
class Slf4jLoggerImpl implements Log {

	private Logger log;

	public Slf4jLoggerImpl(Logger logger) {
		log = logger;
	}

	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}

	public boolean isTraceEnabled() {
		return log.isTraceEnabled();
	}

	public void error(String s, Throwable e) {
		log.error(s, e);
	}

	public void error(String s) {
		log.error(s);
	}

	public void info(String s) {
		log.info(s);
	}

	public void debug(String s) {
		log.debug(s);
	}

	public void trace(String s) {
		log.trace(s);
	}

	public void warn(String s) {
		log.warn(s);
	}

}
