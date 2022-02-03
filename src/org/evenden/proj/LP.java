package org.evenden.proj;
class LP {

    double lam; // longitude

    double phi; // latitude

    LP() {

    }

    LP(double lam, double phi) {
        this.lam = lam;
        this.phi = phi;
    }

    /** (non-Javadoc).
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("LP[");
        buffer.append("lam=").append(lam);
        buffer.append(",phi=").append(phi);
        buffer.append("]");
        return buffer.toString();
    }

}
