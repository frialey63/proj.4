package org.evenden.proj;
class XY {

    double x;

    double y;

    XY() {
        
    }
    
    XY(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /** (non-Javadoc).
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("XY[");
        buffer.append("x=").append(x);
        buffer.append(",y=").append(y);
        buffer.append("]");
        return buffer.toString();
    }

}
