/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.rx.rm.runtime;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.sql.DataSource;

import junit.framework.TestCase;

import com.sun.xml.ws.rx.rm.runtime.LocalIDManager;
import com.sun.xml.ws.rx.rm.runtime.LocalIDManager.BoundMessage;
import com.sun.xml.ws.rx.rm.runtime.sequence.invm.InMemoryLocalIDManager;
import com.sun.xml.ws.rx.rm.runtime.sequence.persistent.DataSourceProvider;
import com.sun.xml.ws.rx.rm.runtime.sequence.persistent.EmbeddedDerbyDbInstance;
import com.sun.xml.ws.rx.rm.runtime.sequence.persistent.JDBCLocalIDManager;
import com.sun.xml.ws.rx.rm.runtime.sequence.persistent.PersistenceException;

public class LocalIDManagerTest extends TestCase {

    private class UnitTestDerbyDataSourceProvider implements DataSourceProvider {

        private final DataSource ds = new DataSource() {

            public Connection getConnection() throws SQLException {
                return dbInstance.getConnection();
            }

            public Connection getConnection(String username, String password) throws SQLException {
                return dbInstance.getConnection();
            }

            public PrintWriter getLogWriter() throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void setLogWriter(PrintWriter out) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void setLoginTimeout(int seconds) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public int getLoginTimeout() throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public <T> T unwrap(Class<T> iface) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public boolean isWrapperFor(Class<?> iface) throws SQLException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Logger getParentLogger() throws SQLFeatureNotSupportedException {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };

        public DataSource getDataSource() throws PersistenceException {
            return ds;
        }
    }

    private EmbeddedDerbyDbInstance dbInstance;

    public void setUp() {
        tearDown();

        dbInstance = EmbeddedDerbyDbInstance.start("PersistentRmJunitTestDb");

        if (dbInstance.tableExists("RM_LOCALIDS")) {
            dbInstance.execute("DROP TABLE RM_LOCALIDS");
        }
        
        dbInstance.execute(
                "CREATE TABLE RM_LOCALIDS (LOCAL_ID VARCHAR(512) NOT NULL,"+
                "SEQ_ID VARCHAR(256) NOT NULL, MSG_NUMBER BIGINT NOT NULL,"+
                "CREATE_TIME BIGINT, SEQ_TERMINATE_TIME BIGINT, PRIMARY KEY (LOCAL_ID))");
    }

    public void tearDown() {
        if (dbInstance != null) {
            dbInstance.stop();
            dbInstance = null;
        }
    }
    
    public void testJDBCLocalIDManager() throws Exception {
        runTest(new JDBCLocalIDManager(new UnitTestDerbyDataSourceProvider()));
    }
    
    public void testInMemoryLocalIDManager() throws Exception {
        runTest(InMemoryLocalIDManager.getInstance());
    }
    
    private void runTest(LocalIDManager mgr) throws Exception {
        // test createLocalID
        mgr.createLocalID("localid1", "seq1", 1);
        validateLocalID(mgr.getBoundMessage("localid1"), "seq1", 1);

        mgr.createLocalID("localid2", "seq2", 2);
        mgr.createLocalID("localid3", "seq3", 3);
        validateLocalID(mgr.getBoundMessage("localid1"), "seq1", 1);
        validateLocalID(mgr.getBoundMessage("localid2"), "seq2", 2);
        validateLocalID(mgr.getBoundMessage("localid3"), "seq3", 3);
        
        // test removeLocalIDs
        List<String> toRemove = new ArrayList<String>();
        mgr.removeLocalIDs(toRemove.iterator());
        validateLocalID(mgr.getBoundMessage("localid1"), "seq1", 1);
        validateLocalID(mgr.getBoundMessage("localid2"), "seq2", 2);
        validateLocalID(mgr.getBoundMessage("localid3"), "seq3", 3);

        toRemove.add("localid1");
        toRemove.add("localid2");
        mgr.removeLocalIDs(toRemove.iterator());
        assertNull(mgr.getBoundMessage("localid1"));
        assertNull(mgr.getBoundMessage("localid2"));
        validateLocalID(mgr.getBoundMessage("localid3"), "seq3", 3);
        
        toRemove = new ArrayList<String>();
        toRemove.add("localid3");
        mgr.removeLocalIDs(toRemove.iterator());
        assertNull(mgr.getBoundMessage("localid1"));
        assertNull(mgr.getBoundMessage("localid2"));
        assertNull(mgr.getBoundMessage("localid3"));
        
        toRemove = new ArrayList<String>();
        toRemove.add("localid4");
        mgr.removeLocalIDs(toRemove.iterator());
        assertNull(mgr.getBoundMessage("localid1"));
        assertNull(mgr.getBoundMessage("localid2"));
        assertNull(mgr.getBoundMessage("localid3"));
        
        // test markSequenceRemoval
        mgr.createLocalID("localida", "testSequence", 1);
        mgr.createLocalID("localidb", "testSequence", 2);
        validateLocalID(mgr.getBoundMessage("localida"), "testSequence", 1);
        validateLocalID(mgr.getBoundMessage("localidb"), "testSequence", 2);
        mgr.markSequenceTermination("testSequence");
        validateLocalID(mgr.getBoundMessage("localida"), "testSequence", 1, true);
        validateLocalID(mgr.getBoundMessage("localidb"), "testSequence", 2, true);
    }
    
    private void validateLocalID(BoundMessage boundMessage, 
            String expectedSequenceID, 
            long expectedMessageNumber) {
        validateLocalID(boundMessage, expectedSequenceID, expectedMessageNumber, false);
    }
    
    private void validateLocalID(BoundMessage boundMessage, 
            String expectedSequenceID, 
            long expectedMessageNumber,
            boolean terminated) {
        System.out.println(boundMessage);
        assertEquals(expectedSequenceID, boundMessage.sequenceID);
        assertEquals(expectedMessageNumber, boundMessage.messageNumber);
        assertTrue(boundMessage.createTime > 0);
        assertTrue(boundMessage.createTime <= System.currentTimeMillis());
        if (terminated) {
            assertTrue(boundMessage.seqTerminateTime > 0);
            assertTrue(boundMessage.seqTerminateTime <= System.currentTimeMillis());
        } else {
            assertEquals(0, boundMessage.seqTerminateTime);
        }
    }

}
