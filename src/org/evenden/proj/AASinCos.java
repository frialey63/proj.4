package org.evenden.proj;
final class AASinCos {

    private static final double ONE_TOL = 1.00000000000001;

    private static final double ATOL = 1e-50;

    static double aasin(double v) {
        double av;

        if ((av = Math.abs(v)) >= 1.0) {
            if (av > ONE_TOL)
                Error.pj_errno = -19;
            return (v < 0. ? -Constants.HALFPI : Constants.HALFPI);
        }
        
        return Math.asin(v);
    }

    static double aacos(double v) {
        double av;

        if ((av = Math.abs(v)) >= 1.0) {
            if (av > ONE_TOL)
                Error.pj_errno = -19;
            return (v < 0. ? Constants.PI : 0.);
        }
        
        return Math.acos(v);
    }

    static double asqrt(double v) {
        return ((v <= 0) ? 0.0 : Math.sqrt(v));
    }

    static double aatan2(double n, double d) {
        return ((Math.abs(n) < ATOL && Math.abs(d) < ATOL) ? 0.0 : Math.atan2(n, d));
    }

}
