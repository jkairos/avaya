package com.avaya.queue.email;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.avaya.queue.util.Constants;

/**
 * A wrapper around an internal properties file and JNDI, but overridable
 * programatically for testing or via the command line.
 */
public class Settings {

    protected final static Log log = LogFactory.getLog("queueMonitoring.settings");
    private static Map<String, Object> MAP = null;

    private static void setUp() {
        MAP = new HashMap<String, Object>();
        try {
            InputStream in = null;
            try{
            	in = new FileInputStream(Constants.APP_PATH+File.separator+"config.properties");
            }catch(IOException ioe){
            	in = new FileInputStream(Constants.PROJECT_PATH+File.separator+"config.properties");
            }
            if (in != null) {
                log.info("Reading properties from classpath.");
                Properties properties = new Properties();
                properties.load(in);
                in.close();
                Context context = null;
                try {
                    context = new InitialContext();
                    log.info("Found JNDI Context : " + context.getNameInNamespace());
                } catch (NoInitialContextException e) {
                    // e.g. unit tests
                    context = null;
                    log.info("No JNDI Context.");
                } catch (NamingException e) {
                    throw new RuntimeException(e);
                }
                for (String key : properties.stringPropertyNames()) {
                    MAP.put(key, properties.getProperty(key));
                    log.info("Loaded property : " + key);
                    if (context != null) {
                        try {
                            Object fromJndi = context.lookup("java:comp/env/" + key);
                            if (fromJndi != null) {
                                log.info("Also found in JNDI, overriding.");
                                MAP.put(key, fromJndi);
                            }
                        } catch (NoInitialContextException e) {
                            throw new RuntimeException(e);
                        } catch (NameNotFoundException e) {
                        } catch (NamingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.warn("Problem reading properties from classpath.", e);
        }
    }

    public static void set(String key, Object value) {
        if (MAP == null)
            setUp();
        MAP.put(key, value);
    }

    public static Object get(String key) {
        if (MAP == null)
            setUp();
        return MAP.get(key);
    }

    public static boolean isSet(String key) {
        return get(key) != null;
    }

    public static String getString(String key) {
        if (!isSet(key))
            throw new RuntimeException("missing setting : " + key);
        return (String) get(key);
    }

    public static int getInt(String key) {
        return Integer.parseInt(getString(key));
    }

    public static boolean getBoolean(String key) {
        Object value = get(key);
        if (value == null)
            return false;
        if (value instanceof Boolean)
            return ((Boolean) value).booleanValue();
        if (value instanceof String)
            return value != null && ((String) value).equals("true");
        return false;
    }

}
