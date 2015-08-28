package com.careerbuilder.search.relevancy.Scoring;

import java.util.function.Function;

public class RelatednessStrategy {

    private int fgTotal;
    private int bgTotal;
    private double fgCount;
    private double bgCount;

    public RelatednessStrategy(int fgTotal, int bgTotal, double fgCount, double bgCount) {
        this.fgTotal = fgTotal;
        this.bgTotal = bgTotal;
        this.fgCount = fgCount;
        this.bgCount = bgCount;
    }

    public double z() {
        double bgProb = (bgCount / bgTotal);
        double num = fgCount - fgTotal * bgProb;
        double denom = Math.sqrt(fgTotal * bgProb * (1 - bgProb));
        denom = (denom == 0) ? 1e-10 : denom;
        return 2 * sigmoid(0.2 * num / denom) - 1;
    }

    private double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }
}

