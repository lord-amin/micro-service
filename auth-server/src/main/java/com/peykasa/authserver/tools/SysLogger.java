package com.peykasa.authserver.tools;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import com.cloudbees.syslog.SyslogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.CharArrayWriter;

/**
 * @author yaer sadeghi
 */
public class SysLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(SysLogger.class);

    public static void log(String log) {
        SyslogMessage syslogMessage = new SyslogMessage();
        syslogMessage.setAppName("auth-server");
        syslogMessage.setFacility(Facility.USER);
        syslogMessage.setHostname("localhost");
        syslogMessage.setSeverity(Severity.INFORMATIONAL);
        CharArrayWriter msg = new CharArrayWriter();
        msg.append(log);
        syslogMessage.setMsg(msg);
//        System.out.println("<<<<<<<<<<<<<<<<<<<<<<");
        LOGGER.info(syslogMessage.toRfc3164SyslogMessage());
    }

}
