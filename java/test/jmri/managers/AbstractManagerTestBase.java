package jmri.managers;

import jmri.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import jmri.util.JUnitAppender;
import org.apache.log4j.Level;

import org.junit.Assert;
import org.junit.Test;

/**
 * Base for the various Abstract*MgrTestBase base classes for NamedBean Manager test classes
 * <p>
 * This is not itself a test class, e.g. should not be added to a suite.
 * Instead, this forms the base for test classes, including providing some
 * common tests.
 * <p>
 * Quite a bit of AbstractManager testing is done in InternalSensorManagerTest
 * to give it a concrete platform
 *
 * @author Bob Jacobsen Copyright (C) 2017	
 * @param <T> the class being tested
 * @param <E> the class of NamedBean handled by T
 */
public abstract class AbstractManagerTestBase<T extends Manager<E>, E extends NamedBean> {

    // Manager<E> under test - setUp() loads this
    protected T l = null;

    // check that you can add and remove listeners
    @Test
    public final void testManagerDataListenerAddAndRemove() {
        
        Manager.ManagerDataListener<E> listener = new Manager.ManagerDataListener<E>(){
            @Override public void contentsChanged(Manager.ManagerDataEvent<E> e){}
            @Override public void intervalAdded(Manager.ManagerDataEvent<E> e) {}
            @Override public void intervalRemoved(Manager.ManagerDataEvent<E> e) {}
        };
        
        l.addDataListener(listener);
        l.removeDataListener(listener);
        
        l.addDataListener(null);
        l.removeDataListener(null);
        
        l.addDataListener(null);
        l.removeDataListener(listener);
        
        l.addDataListener(listener);
        l.removeDataListener(null);
        
    }

    @Test
    public final void testPropertyChangeListenerAddAndRemove() {

        int base = l.getPropertyChangeListeners().length;
        
        PropertyChangeListener listener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                // do nothing
            }
        };
        
        Assert.assertEquals(base, l.getPropertyChangeListeners().length);
        l.addPropertyChangeListener(listener);
        Assert.assertEquals(base + 1, l.getPropertyChangeListeners().length);
        l.removePropertyChangeListener(listener);
        Assert.assertEquals(base, l.getPropertyChangeListeners().length);

        Assert.assertEquals(base, l.getPropertyChangeListeners().length);
        l.addPropertyChangeListener("property", listener);
        Assert.assertEquals(base + 1, l.getPropertyChangeListeners().length);
        Assert.assertEquals(1, l.getPropertyChangeListeners("property").length);
        l.removePropertyChangeListener("property", listener);
        Assert.assertEquals(base, l.getPropertyChangeListeners().length);

    }


    @Test
    public final void testVetoableChangeListenerAddAndRemove() {

        int base = l.getVetoableChangeListeners().length;
        
        VetoableChangeListener listener = new VetoableChangeListener() {

            @Override
            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                // do nothing
            }
        };
        
        Assert.assertEquals(base, l.getVetoableChangeListeners().length);
        l.addVetoableChangeListener(listener);
        Assert.assertEquals(base + 1, l.getVetoableChangeListeners().length);
        l.removeVetoableChangeListener(listener);
        Assert.assertEquals(base, l.getVetoableChangeListeners().length);

        Assert.assertEquals(base, l.getVetoableChangeListeners().length);
        l.addVetoableChangeListener("property", listener);
        Assert.assertEquals(base + 1, l.getVetoableChangeListeners().length);
        Assert.assertEquals(1, l.getVetoableChangeListeners("property").length);
        l.removeVetoableChangeListener("property", listener);
        Assert.assertEquals(base, l.getVetoableChangeListeners().length);

    }

    @Test
    public void testMakeSystemName() {
        String s = l.makeSystemName("1");
        Assert.assertTrue(s != null);
        Assert.assertTrue(! s.isEmpty());
    }

    @Test
    public void testRegisterDuplicateSystemName() throws PropertyVetoException {
        if (l instanceof ProvidingManager)  {
            ProvidingManager<E> m = (ProvidingManager<E>) l;
            String s = l.makeSystemName("1");
            Assert.assertTrue(s != null);
            Assert.assertTrue(! s.isEmpty());

            E e;

            try {
                e = m.provide(s);
            } catch (IllegalArgumentException | NullPointerException | ArrayIndexOutOfBoundsException ex) {
                // jmri.jmrix.openlcb.OlcbLightManagerTest gives a NullPointerException here.
                // jmri.jmrix.openlcb.OlcbSensorManagerTest gives a ArrayIndexOutOfBoundsException here.
                // Some other tests give an IllegalArgumentException here.

                // If the test is unable to provide a named bean, abort this test.
                JUnitAppender.clearBacklog(Level.WARN);
                return;
            }

            // Remove bean if it's already registered
            if (l.getBeanBySystemName(e.getSystemName()) != null) {
                l.deregister(e);
            }

            // Register the bean once. This should be OK.
            l.register(e);

            String expectedMessage = "systemName is already registered: " + e.getSystemName();
            boolean hasException = false;
            try {
                // Register bean twice. This should fail with an IllegalArgumentException.
                l.register(e);
            } catch (IllegalArgumentException ex) {
                hasException = true;
                Assert.assertTrue("exception message is correct",
                        expectedMessage.equals(ex.getMessage()));
            }
            Assert.assertTrue("exception is thrown", hasException);

            l.deregister(e);
        }
    }

}