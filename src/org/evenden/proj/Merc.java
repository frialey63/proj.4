package org.evenden.proj;
public class Merc implements Projection {

    private static final String des_merc = "Mercator\n\tCyl, Sph&Ell\n\tlat_ts=";

    private boolean ellips;

    private PJ P;

    public Merc(PJ P) {
        this.P = pj_merc(P);
    }

    /**
     * ellipsoid
     *
     * @param lp
     * @return
     */
    private XY e_forward(LP lp) {
        XY xy = null;

        if (Math.abs(Math.abs(lp.phi) - Constants.HALFPI) <= Constants.EPS10) {
            Error.pj_errno = -20;
            return null;
        }

        xy = new XY(P.k0 * lp.lam, -P.k0 * Math.log(PJUtils.pj_tsfn(lp.phi, Math.sin(lp.phi), P.e)));

        return xy;
    }

    /**
     * spheroid
     *
     * @param lp
     * @return
     */
    private XY s_forward(LP lp) {
        XY xy = null;

        if (Math.abs(Math.abs(lp.phi) - Constants.HALFPI) <= Constants.EPS10) {
            Error.pj_errno = -20;
            return null;
        }

        xy = new XY(P.k0 * lp.lam, P.k0 * Math.log(Math.tan(Constants.FORTPI + .5 * lp.phi)));

        return xy;
    }

    /**
     * ellipsoid
     *
     * @param xy
     * @return
     */
    private LP e_inverse(XY xy) {
        LP lp = new LP();

        lp.phi = PJUtils.pj_phi2(Math.exp(-xy.y / P.k0), P.e);
        if (Error.pj_errno != 0) {
            Error.pj_errno = -20;
            return null;
        }

        lp.lam = xy.x / P.k0;

        return lp;
    }

    /**
     * spheroid
     *
     * @param xy
     * @return
     */
    private LP s_inverse(XY xy) {
        LP lp = new LP();

        lp.phi = Constants.HALFPI - 2.0 * Math.atan(Math.exp(-xy.y / P.k0));
        lp.lam = xy.x / P.k0;

        return lp;
    }

    private PJ pj_merc(PJ P) {

        double phits = 0.0;
        boolean is_phits;

        String str = P.params.getProperty("lat_ts");
        if (is_phits = (str != null)) {
            phits = Math.abs(Double.parseDouble(str) * Constants.DEG_TO_RAD);

            if (phits >= Constants.HALFPI) {
                Error.pj_errno = -24;
                return null;
            }
        }

        if (P.es != 0.0) {
            /* ellipsoid */
            if (is_phits)
                P.k0 = PJUtils.pj_msfn(Math.sin(phits), Math.cos(phits), P.es);

            ellips = true;
        } else {
            /* sphere */
            if (is_phits)
                P.k0 = Math.cos(phits);
        }

        return P;
    }

    /* (non-Javadoc)
     * @see Projection#forward(LP)
     */
    public XY forward(LP lp) {
        return ellips ? e_forward(lp) : s_forward(lp);
    }

    /* (non-Javadoc)
     * @see Projection#inverse(XY)
     */
    public LP inverse(XY xy) {
        return ellips ? e_inverse(xy) : s_inverse(xy);
    }

    /* (non-Javadoc)
     * @see Projection#fac(LP, FACTORS)
     */
    public Factors fac(LP lp, Factors fac) {
        return null;
    }

    /* (non-Javadoc)
     * @see Projection#description()
     */
    public String description() {
        return des_merc;
    }

}
