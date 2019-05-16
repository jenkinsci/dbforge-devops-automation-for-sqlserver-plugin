package io.jenkins.plugins;

import io.jenkins.plugins.Presenters.Utils;
import org.junit.Test;
import static org.junit.Assert.*;

public class UtilsTest {

    @Test
    public void testIsValidPath() {

        assertTrue(Utils.isValidPath("D:/Temp"));
        assertFalse(Utils.isValidPath("D:/D:/"));
    }

    @Test
    public void testIsValidPackageId() {

        assertTrue(Utils.isValidPackageId("test"));
        assertTrue(Utils.isValidPackageId("test."));
        assertTrue(Utils.isValidPackageId("testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttest"));
        assertFalse(Utils.isValidPackageId("test "));
        assertFalse(Utils.isValidPackageId("test-"));
        assertFalse(Utils.isValidPackageId("test~"));
        assertFalse(Utils.isValidPackageId("test<"));
        assertFalse(Utils.isValidPackageId("test!"));
        assertFalse(Utils.isValidPackageId("test@"));
        assertFalse(Utils.isValidPackageId("test/"));
        assertFalse(Utils.isValidPackageId("test$"));
        assertFalse(Utils.isValidPackageId("test%"));
        assertFalse(Utils.isValidPackageId("test^"));
        assertFalse(Utils.isValidPackageId("test&"));
        assertFalse(Utils.isValidPackageId("test#"));
        assertFalse(Utils.isValidPackageId("test№"));
        assertFalse(Utils.isValidPackageId("test("));
        assertFalse(Utils.isValidPackageId("test)"));
        assertFalse(Utils.isValidPackageId("test?"));
        assertFalse(Utils.isValidPackageId("test>"));
        assertFalse(Utils.isValidPackageId("test,"));
        assertFalse(Utils.isValidPackageId("test*"));
        assertFalse(Utils.isValidPackageId("test|"));
        assertFalse(Utils.isValidPackageId("test'"));
        assertFalse(Utils.isValidPackageId("testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttestt"));
    }
}
