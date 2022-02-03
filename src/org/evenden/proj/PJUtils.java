package org.evenden.proj;
final class PJUtils {

    static double adjlon(double lon) {
        while (Math.abs(lon) > Constants.SPI)
            lon += lon < 0.0 ? Constants.TWOPI : -Constants.TWOPI;

        return lon;
    }

    static double pj_tsfn(double phi, double sinphi, double e) {
        sinphi *= e;
        return (Math.tan(0.5 * (Constants.HALFPI - phi)) / Math.pow((1.0 - sinphi) / (1.0 + sinphi), 0.5 * e));
    }

    static double pj_msfn(double sinphi, double cosphi, double es) {
        return (cosphi / Math.sqrt(1.0 - es * sinphi * sinphi));
    }

    private static final int N_ITER = 15;

    private static final double TOL = 1.0e-10;

    static double pj_phi2(double ts, double e) {
        double eccnth, Phi, con, dphi;
        int i;

        eccnth = 0.5 * e;
        Phi = Constants.HALFPI - 2.0 * Math.atan(ts);

        i = N_ITER;
        do {
            con = e * Math.sin(Phi);
            dphi = Constants.HALFPI - 2.0 * Math.atan(ts * Math.pow((1.0 - con) / (1.0 + con), eccnth)) - Phi;
            Phi += dphi;
        } while (Math.abs(dphi) > TOL && (--i > 0));

        if (i <= 0)
            Error.pj_errno = -18;

        return Phi;
    }

    static boolean pj_deriv(LP lp, double h, PJ P, Projection proj, Factors.DERIVS der) {
        XY t = null;

        lp.lam += h;
        lp.phi += h;

        if (Math.abs(lp.phi) > Constants.HALFPI) 
            return true;

        h += h;
        t = proj.forward(lp);
        if (t == null) return true;

        der.x_l = t.x; der.y_p = t.y; der.x_p = -t.x; der.y_l = -t.y;
        lp.phi -= h;
        if (Math.abs(lp.phi) > Constants.HALFPI) return true;
        
        t = proj.forward(lp);
        if (t == null) 
            return true;

        der.x_l += t.x; der.y_p -= t.y; der.x_p += t.x; der.y_l -= t.y;
        lp.lam -= h;
        t = proj.forward(lp);
        if (t == null) 
            return true;

        der.x_l -= t.x; der.y_p -= t.y; der.x_p += t.x; der.y_l += t.y;
        lp.phi += h;
        t = proj.forward(lp);
        if (t == null) 
            return true;

        der.x_l -= t.x; der.y_p += t.y; der.x_p -= t.x; der.y_l += t.y;
        der.x_l /= (h += h);
        der.y_p /= h;
        der.x_p /= h;
        der.y_l /= h;

        return false;
    }

}
