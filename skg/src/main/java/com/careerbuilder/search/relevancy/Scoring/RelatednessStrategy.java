package com.careerbuilder.search.relevancy.Scoring;

public class RelatednessStrategy {

    public static double z(int fgTotal, int bgTotal, double fgCount, double bgCount) {
        double bgProb = (bgCount / bgTotal);
        double num = fgCount - fgTotal * bgProb;
        double denom = Math.sqrt(fgTotal * bgProb * (1 - bgProb));
        denom = (denom == 0) ? 1e-10 : denom;
        double z = num / denom;
        double result = 0.2*sigmoid(z, -300)
                + 0.2*sigmoid(z, -50)
                + 0.2*sigmoid(z, 0)
                + 0.2*sigmoid(z, 50)
                + 0.2*sigmoid(z, 300);
        return Math.round(result * 1e5) / 1e5;
    }

    private static double sigmoid(double x, double offset) {
        return (x+offset) / (30 + Math.abs(x+offset));
    }
}

