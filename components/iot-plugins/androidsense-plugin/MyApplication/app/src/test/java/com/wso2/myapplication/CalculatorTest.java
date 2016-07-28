package com.wso2.myapplication;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by dilan on 7/22/16.
 */
public class CalculatorTest extends TestCase {


    Calculator calculator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        calculator = new Calculator();
    }

    @Test
    public void testAdd() throws Exception {
        assertEquals(calculator.add(10f, 20f), 30f, 0);
        assertEquals(calculator.add(30f, 20f), 50f, 0);
    }

    @Test
    public void testSquare() throws Exception {
        assertEquals(calculator.square(6), 36);
    }
}