package org.evenden.proj;
import java.util.Properties;

/**
 * PROJ.4 - Cartographic Projections Library
 *
 * This web page relates to the PROJ.4 Cartographic Projections library
 * originally written by Gerald Evenden then of the USGS.
 *
 * The primary version of this web page can be found at
 * http://www.remotesensing.org/proj, and mirrored at http://proj.maptools.org.
 *
 * @author paulpa (Electronic Data Systems)
 * @since 1.0, Apr 1, 2006
 */
public class Proj4 {

    public static final int LAMBERT_CONFORMAL_CONIC = 0;

    public static final int MERCATOR = 1;

    private Projection proj;

    private PJ PIN;

    public Proj4(Properties start, double a, double es, int projType) throws BumCallException {

        pj_init(start, a, es);

        if (Error.pj_errno != 0)
            throw new BumCallException(Error.pj_errno);

        switch(projType) {
            case LAMBERT_CONFORMAL_CONIC:
                proj = new LCC(PIN);
                break;
            case MERCATOR:
                proj = new Merc(PIN);
                break;
            default:
                Error.pj_errno = -5;
                break;
        }

        if (Error.pj_errno != 0)
            throw new BumCallException(Error.pj_errno);
    }

    private void pj_init(Properties start, double a, double es) {

        PIN = new PJ(start);

        /* set ellipsoid/sphere parameters */
        PIN.a = a;                                      // if (pj_ell_set(start, &PIN.a, &PIN.es)) goto bum_call;
        PIN.es = es;
        PIN.e = Math.sqrt(PIN.es);
        PIN.ra = 1.0 / PIN.a;
        PIN.one_es = 1.0 - PIN.es;
        if (PIN.one_es == 0.) {
            Error.pj_errno = -6;
            return;
        }
        PIN.rone_es = 1.0 / PIN.one_es;

        /* set PIN.geoc coordinate system */
        PIN.geoc = Boolean.valueOf(start.getProperty("geoc", "false")).booleanValue();                  // (PIN.es && pj_param(start, "bgeoc").i);

        /* over-ranging flag */
        PIN.over = Boolean.valueOf(start.getProperty("over", "false")).booleanValue();                  // pj_param(start, "bover").i;

        /* central meridian */
        PIN.lam0 = Double.parseDouble(start.getProperty("lon_0", "0.0")) * Constants.DEG_TO_RAD;        // pj_param(start, "rlon_0").f;

        /* central latitude */
        PIN.phi0 = Double.parseDouble(start.getProperty("lat_0", "0.0")) * Constants.DEG_TO_RAD;        // pj_param(start, "rlat_0").f;

        /* false easting and northing */
        PIN.x0 = Double.parseDouble(start.getProperty("x_0", "0.0"));                                   // pj_param(start, "dx_0").f;
        PIN.y0 = Double.parseDouble(start.getProperty("y_0", "0.0"));                                   // pj_param(start, "dy_0").f;

        /* general scaling factor */
        PIN.k0 = Double.parseDouble(start.getProperty("k_0", "1.0"));
        if (PIN.k0 <= 0.0) {
            Error.pj_errno = -31;
            return;
        }

        /* set units */
        PIN.to_meter = PIN.fr_meter = 1.0;
    }

    /* general forward projection */
    public XY pj_fwd(LP lp) throws BumCallException {
        XY xy = null;
        double t;

        /* check for forward and latitude or longitude overange */
        if ((t = Math.abs(lp.phi) - Constants.HALFPI) > Constants.EPS || Math.abs(lp.lam) > 10.0) {
            throw new BumCallException(Error.pj_errno = -14);
        } else { /* proceed with projection */
            Error.pj_errno = 0;

            if (Math.abs(t) <= Constants.EPS)
                lp.phi = lp.phi < 0.0 ? -Constants.HALFPI : Constants.HALFPI;
            else if (PIN.geoc)
                lp.phi = Math.atan(PIN.rone_es * Math.tan(lp.phi));

            lp.lam -= PIN.lam0; /* compute del lp.lam */

            if (!PIN.over)
                lp.lam = PJUtils.adjlon(lp.lam); /* adjust del longitude */

            xy = proj.forward(lp); /* project */

            if (Error.pj_errno != 0) {
                throw new BumCallException(Error.pj_errno);
            } else {
                /* adjust for major axis and easting/northings */
                xy.x = PIN.fr_meter * (PIN.a * xy.x + PIN.x0);
                xy.y = PIN.fr_meter * (PIN.a * xy.y + PIN.y0);
            }
        }

        return xy;
    }

    /* general inverse projection */
    public LP pj_inv(XY xy) throws BumCallException {
        LP lp = null;

        /* can't do as much preliminary checking as with forward */

        Error.pj_errno = 0;

        xy.x = (xy.x * PIN.to_meter - PIN.x0) * PIN.ra; /* descale and de-offset */
        xy.y = (xy.y * PIN.to_meter - PIN.y0) * PIN.ra;

        lp = proj.inverse(xy); /* inverse project */

        if (Error.pj_errno != 0) {
            throw new BumCallException(Error.pj_errno);
        }
        else {
            lp.lam += PIN.lam0; /* reduce from del lp.lam */
            if (!PIN.over)
                lp.lam = PJUtils.adjlon(lp.lam); /* adjust longitude to CM */
            if (PIN.geoc && Math.abs(Math.abs(lp.phi) - Constants.HALFPI) > Constants.EPS)
                lp.phi = Math.atan(PIN.one_es * Math.tan(lp.phi));
        }

        return lp;
    }

    /* projection scale factors */
    public boolean pj_factors(LP lp, double h, Factors fac) throws BumCallException {
        Factors.DERIVS der = new Factors.DERIVS();
        double cosphi, t, n, r;

        /* check for forward and latitude or longitude overange */
        if ((t = Math.abs(lp.phi) - Constants.HALFPI) > Constants.EPS || Math.abs(lp.lam) > 10.0) {
            Error.pj_errno = -14;
            return true;
        } else { /* proceed */
            Error.pj_errno = 0;

            if (Math.abs(t) <= Constants.EPS) /* adjust to pi/2 */
                lp.phi = lp.phi < 0.0 ? -Constants.HALFPI : Constants.HALFPI;
            else if (PIN.geoc)
                lp.phi = Math.atan(PIN.rone_es * Math.tan(lp.phi));

            lp.lam -= PIN.lam0; /* compute del lp.lam */
            if (!PIN.over)
                lp.lam = PJUtils.adjlon(lp.lam); /* adjust del longitude */

            if (h <= 0.0)
                h = Constants.DEFAULT_H;

            if (proj != null) /* get what projection analytic values */
                fac = proj.fac(lp, fac);

            if (((fac.code & (Factors.IS_ANAL_XL_YL + Factors.IS_ANAL_XP_YP)) != (Factors.IS_ANAL_XL_YL + Factors.IS_ANAL_XP_YP)) && PJUtils.pj_deriv(lp, h, PIN, proj, der))
                return true;

            if ((fac.code & Factors.IS_ANAL_XL_YL) == 0) {
                fac.der.x_l = der.x_l;
                fac.der.y_l = der.y_l;
            }

            if ((fac.code & Factors.IS_ANAL_XP_YP) == 0) {
                fac.der.x_p = der.x_p;
                fac.der.y_p = der.y_p;
            }

            cosphi = Math.cos(lp.phi);

            if ((fac.code & Factors.IS_ANAL_HK) == 0) {
                fac.h = Hypot.hypot(fac.der.x_p, fac.der.y_p);
                fac.k = Hypot.hypot(fac.der.x_l, fac.der.y_l) / cosphi;
                if (PIN.es != 0.0) {
                    t = Math.sin(lp.phi);
                    t = 1.0 - PIN.es * t * t;
                    n = Math.sqrt(t);
                    fac.h *= t * n / PIN.one_es;
                    fac.k *= n;
                    r = t * t / PIN.one_es;
                } else
                    r = 1.0;
            } else if (PIN.es != 0.0) {
                r = Math.sin(lp.phi);
                r = 1.0 - PIN.es * r * r;
                r = r * r / PIN.one_es;
            } else
                r = 1.0;

            /* convergence */
            if ((fac.code & Factors.IS_ANAL_CONV) == 0) {
                fac.conv = -Math.atan2(fac.der.y_l, fac.der.x_l);
                if ((fac.code & Factors.IS_ANAL_XL_YL) != 0)
                    fac.code |= Factors.IS_ANAL_CONV;
            }

            /* areal scale factor */
            fac.s = (fac.der.y_p * fac.der.x_l - fac.der.x_p * fac.der.y_l) * r / cosphi;

            /* meridian-parallel angle theta prime */
            fac.thetap = AASinCos.aasin(fac.s / (fac.h * fac.k));

            /* Tissot ellips axis */
            t = fac.k * fac.k + fac.h * fac.h;
            fac.a = Math.sqrt(t + 2. * fac.s);
            t = (t = t - 2.0 * fac.s) <= 0. ? 0. : Math.sqrt(t);
            fac.b = 0.5 * (fac.a - t);
            fac.a = 0.5 * (fac.a + t);

            /* omega */
            fac.omega = 2.0 * AASinCos.aasin((fac.a - fac.b) / (fac.a + fac.b));
        }

        return false;
    }
}
