package org.evenden.proj;

class LCC implements Projection {

    private static final String des_lcc = "Lambert Conformal Conic\n\tConic, Sph&Ell\n\tlat_1= and lat_2= or lat_0";

    private double  phi1;
    private double  phi2;
    private double  n;
    private double  rho;
    private double  rho0;
    private double  c;
    private boolean ellips;

    private PJ P;

    LCC(PJ P) {
        this.P = pj_lcc(P);
    }

    /*
     * ellipsoid & spheroid
     *
     * @see Projection#e_forward(LP)
     */
    public XY forward(LP lp) {

        XY xy = null;

        if (Math.abs(Math.abs(lp.phi) - Constants.HALFPI) < Constants.EPS10) {
            if ((lp.phi * n) <= 0.0) {
                Error.pj_errno = -20;
                return null;
            }

            rho = 0.0;
        } else {
            rho = c * (ellips ? Math.pow(PJUtils.pj_tsfn(lp.phi, Math.sin(lp.phi), P.e), n) : Math.pow(Math.tan(Constants.FORTPI + 0.5 * lp.phi), -n));
        }

        xy = new XY(P.k0 * (rho * Math.sin(lp.lam *= n)), P.k0 * (rho0 - rho * Math.cos(lp.lam)));

        return xy;
    }

    /*
     * ellipsoid & spheroid
     *
     * @see Projection#e_inverse(XY)
     */
    public LP inverse(XY xy) {
        LP lp = new LP();

        xy.x /= P.k0;
        xy.y /= P.k0;

        if ((rho = Hypot.hypot(xy.x, (xy.y = rho0 - xy.y))) != 0) {
            if (n < 0.) {
                rho = -rho;
                xy.x = -xy.x;
                xy.y = -xy.y;
            }

            if (ellips) {
                lp.phi = PJUtils.pj_phi2(Math.pow(rho / c, 1.0 / n), P.e);
                if (Error.pj_errno != 0) {
                    Error.pj_errno = -20;
                    return null;
                }
            } else {
                lp.phi = 2.0 * Math.atan(Math.pow(c / rho, 1.0 / n)) - Constants.HALFPI;
            }

            lp.lam = Math.atan2(xy.x, xy.y) / n;
        } else {
            lp.lam = 0.0;
            lp.phi = (n > 0.0) ? Constants.HALFPI : -Constants.HALFPI;
        }

        return lp;
    }

    /* (non-Javadoc)
     * @see Projection#fac(LP, FACTORS)
     */
    public Factors fac(LP lp, Factors fac) {
        if (Math.abs(Math.abs(lp.phi) - Constants.HALFPI) < Constants.EPS10) {
            if ((lp.phi * n) <= 0.0)
                return null;

            rho = 0.0;
        } else {
            rho = c * (ellips ? Math.pow(PJUtils.pj_tsfn(lp.phi, Math.sin(lp.phi), P.e), n) : Math.pow(Math.tan(Constants.FORTPI + 0.5 * lp.phi), -n));
        }

        fac.code |= Factors.IS_ANAL_HK + Factors.IS_ANAL_CONV;
        fac.k = fac.h = P.k0 * n * rho / PJUtils.pj_msfn(Math.sin(lp.phi), Math.cos(lp.phi), P.es);
        fac.conv = - n * lp.lam;

        return fac;
    }

    /* (non-Javadoc)
     * @see Projection#description()
     */
    public String description() {
        return des_lcc;
    }

    private PJ pj_lcc(PJ P) {
        double cosphi, sinphi;
        boolean secant;

        phi1 = Double.parseDouble(P.params.getProperty("lat_1", "0.0")) * Constants.DEG_TO_RAD;

        String str = P.params.getProperty("lat_2");
        if (str != null) {
            phi2 = Double.parseDouble(str) * Constants.DEG_TO_RAD;
        }
        else {
            phi2 = phi1;

            if (P.params.getProperty("lat_0") == null)
                P.phi0 = phi1;
        }

        if (Math.abs(phi1 + phi2) < Constants.EPS10) {
            Error.pj_errno = -21;
            return null;
        }

        n = sinphi = Math.sin(phi1);
        cosphi = Math.cos(phi1);
        secant = Math.abs(phi1 - phi2) >= Constants.EPS10;

        if (ellips = (P.es != 0.0)) {
            double ml1, m1;

            P.e = Math.sqrt(P.es);
            m1 = PJUtils.pj_msfn(sinphi, cosphi, P.es);
            ml1 = PJUtils.pj_tsfn(phi1, sinphi, P.e);

            if (secant) {
                /* secant cone */
                n = Math.log(m1 / PJUtils.pj_msfn(sinphi = Math.sin(phi2), Math.cos(phi2), P.es));
                n /= Math.log(ml1 / PJUtils.pj_tsfn(phi2, sinphi, P.e));
            }

            c = (rho0 = m1 * Math.pow(ml1, -n) / n);
            rho0 *= (Math.abs(Math.abs(P.phi0) - Constants.HALFPI) < Constants.EPS10) ? 0.0 : Math.pow(PJUtils.pj_tsfn(P.phi0, Math.sin(P.phi0), P.e), n);
        } else {
            if (secant)
                n = Math.log(cosphi / Math.cos(phi2)) / Math.log(Math.tan(Constants.FORTPI + 0.5 * phi2) / Math.tan(Constants.FORTPI + 0.5 * phi1));

            c = cosphi * Math.pow(Math.tan(Constants.FORTPI + .5 * phi1), n) / n;
            rho0 = (Math.abs(Math.abs(P.phi0) - Constants.HALFPI) < Constants.EPS10) ? 0.0 : c * Math.pow(Math.tan(Constants.FORTPI + 0.5 * P.phi0), -n);
        }

        return P;
    }

}
