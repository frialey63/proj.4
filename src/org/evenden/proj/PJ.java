package org.evenden.proj;
import java.util.Properties;

class PJ {

    static class PVALUE {
        double f;
        boolean i;
        String s;

        public PVALUE(double f, boolean i, String s) {
            this.f = f;
            this.i = i;
            this.s = s;
        }
    }

    Properties params;  /* parameter list */

    boolean over;       /* over-range flag */

    boolean geoc;       /* geocentric latitude flag */

    double
        a,          /* major axis or radius if es==0 */
        e,          /* eccentricity */
        es,         /* e ^ 2 */
        ra,         /* 1/A */
        one_es,     /* 1 - e^2 */
        rone_es,    /* 1/one_es */
        lam0, phi0, /* central longitude, latitude */
        x0, y0,     /* easting and northing */
        k0,         /* general scaling factor */
        to_meter, fr_meter; /* cartesian scaling */

    public PJ(Properties params) {
        super();
        this.params = params;
    }

}
