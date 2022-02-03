package org.evenden.proj;
final class Factors {

    static final int IS_ANAL_XL_YL  = 01;       /* derivatives of lon analytic */
    static final int IS_ANAL_XP_YP  = 02;       /* derivatives of lat analytic */
    static final int IS_ANAL_HK  = 04;          /* h and k analytic */
    static final int IS_ANAL_CONV = 010;        /* convergence analytic */

    static class DERIVS {
        double x_l, x_p;    /* derivatives of x for lambda-phi */

        double y_l, y_p;    /* derivatives of y for lambda-phi */
    }

    DERIVS der;

    double h, k;            /* meridinal, parallel scales */
    double omega, thetap;   /* angular distortion, theta prime */
    double conv;            /* convergence */
    double s;               /* areal scale factor */
    double a, b;            /* max-min scale error */
    int code;               /* info as to analytics, see following */

}
