package com.illuque.tcpnumbers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NumbersCollectorTest {

    @Test
    void poll_whenNumberRepeated_NotIncluded() {
        NumbersCollector numbersCollector = NumbersCollector.create();
        numbersCollector.add(1);
        numbersCollector.add(2);
        numbersCollector.add(1);

        assertEquals(1, numbersCollector.pollNumber());
        assertEquals(2, numbersCollector.pollNumber());
        assertNull(numbersCollector.pollNumber());
    }

    @Test
    void newReportRound() {
        NumbersCollector numbersCollector = NumbersCollector.create();
        numbersCollector.add(1);
        numbersCollector.add(2);
        numbersCollector.add(1);
        numbersCollector.add(3);
        numbersCollector.add(4);

        NumbersCollector.Report report = numbersCollector.newReportRound();
        assertEquals(4, report.getUniquesInRound());
        assertEquals(1, report.getDuplicatedInRound());

        numbersCollector.add(3);
        numbersCollector.add(4);
        numbersCollector.add(5);

        report = numbersCollector.newReportRound();

        assertEquals(1, report.getUniquesInRound());
        assertEquals(2, report.getDuplicatedInRound());

        assertEquals(5, report.getTotalUniqueNumbers());
    }
}