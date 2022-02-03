package org.evenden.proj;
import java.util.Properties;

import org.evenden.proj.BumCallException;
import org.evenden.proj.Constants;
import org.evenden.proj.LP;
import org.evenden.proj.Proj4;
import org.evenden.proj.XY;

import junit.framework.TestCase;

public class LccTest extends TestCase {

    private static final double FWD_TOL = 1.0;

    private static final double INV_TOL = 0.0001;

    private Proj4 proj4;

    protected void setUp() throws Exception {
        super.setUp();

        final double a = 6378137.0;
        final double es = 0.0066943799901413;

        Properties start = new Properties();
        start.put("lon_0", "-90.0");
        start.put("lat_1", "20.0");
        start.put("lat_2", "60.0");

        proj4 = new Proj4(start, a, es, Proj4.LAMBERT_CONFORMAL_CONIC);
    }

    private class Ref
    {
        double lat;
        double lon;
        long x;
        long y;

        public Ref(double lat, double lon, long x, long y) {
            super();
            this.lat = lat;
            this.lon = lon;
            this.x = x;
            this.y = y;
        }
    }

    private Ref[] refs = new Ref[] {
            new Ref(0.0, -90.0, 0, 0),
            new Ref(50.0, -80.0, 680587, 5607024),
            new Ref(30.0, -80.0, 920504, 3524027),
            new Ref(50.0, -100.0, -680587, 5607024),
            new Ref(30.0, -100.0, -920504, 3524027)
    };

    /*
     * Test method for 'Proj4.pj_fwd(LP, PJ)'
     */
    public void testPj_fwd() {

        for (int i = 0; i < refs.length; i++) {
            Ref ref = refs[i];

            LP lp = new LP(ref.lon * Constants.DEG_TO_RAD, ref.lat * Constants.DEG_TO_RAD);

            try {
                XY xy = proj4.pj_fwd(lp);

                assertEquals(ref.x, xy.x, FWD_TOL);
                assertEquals(ref.y, xy.y, FWD_TOL);
            } catch (BumCallException e) {
                fail();
            }
        }
    }

    /*
     * Test method for 'Proj4.pj_inv(XY, PJ)'
     */
    public void testPj_inv() {

        for (int i = 0; i < refs.length; i++) {
            Ref ref = refs[i];

            XY xy = new XY(ref.x, ref.y);

            try {
                LP lp = proj4.pj_inv(xy);

                double lon = lp.lam * Constants.RAD_TO_DEG;
                double lat = lp.phi * Constants.RAD_TO_DEG;

                assertEquals(ref.lon, lon, INV_TOL);
                assertEquals(ref.lat, lat, INV_TOL);
            } catch (BumCallException e) {

                fail();
            }
        }
    }

}
