package com.careerbuilder.search.relevancy.Scoring;

public class RelatednessStrategy {

    public static double z(int fgTotal, int bgTotal, double fgCount, double bgCount) {
        double bgProb = (bgCount / bgTotal);
        double num = fgCount - fgTotal * bgProb;
        double denom = Math.sqrt(fgTotal * bgProb * (1 - bgProb));
        denom = (denom == 0) ? 1e-10 : denom;
        return Math.round(sigmoid(num / denom) * 1e5) / 1e5;
    }

    private static double sigmoid(double x) {
        return x / (10 + Math.abs(x));
    }
}

