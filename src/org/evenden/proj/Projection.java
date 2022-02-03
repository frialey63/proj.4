package org.evenden.proj;

public interface Projection {

    public XY forward(LP lp);

    public LP inverse(XY xy);

    public Factors fac(LP lp, Factors fac);

    public String description();
}