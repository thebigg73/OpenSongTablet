package com.garethevans.church.opensongtablet;

public class TimeSignature {
    final int numerator;
    final int denominator;

    TimeSignature(int numerator, int denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    @Override
    public String toString() {
        return numerator + "/" + denominator;
    }
}