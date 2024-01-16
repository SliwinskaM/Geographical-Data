import java.io.PrintStream;
import java.util.Locale;

public class BoundingBox {
    double xmin;
    double ymin;
    double xmax;
    double ymax;

    public BoundingBox() {
        xmin = Double.NaN;
        xmax = Double.NaN;
        ymin = Double.NaN;
        ymax = Double.NaN;
    }

    public BoundingBox(BoundingBox another){
        this.xmin=another.xmin;
        this.xmax=another.xmax;
        this.ymin=another.ymin;
        this.xmax=another.xmax;
    }


    /**
     * Powiększa BB tak, aby zawierał punkt (x,y)
     * @param x - współrzędna x
     * @param y - współrzędna y
     */
    void addPoint(double x, double y){
        if(isEmpty()) {
            xmin = x;
            xmax = x;
            ymin = y;
            ymax = y;
        }
        else {
            if (x < xmin) xmin = x;
            if (x > xmax) xmax = x;
            if (y < ymin) ymin = y;
            if (y > ymax) ymax = y;
        }
    }

    /**
     * Sprawdza, czy BB zawiera punkt (x,y)
     * @param x
     * @param y
     * @return
     */
    boolean contains(double x, double y){
        if (isEmpty()) return false;
        else return x >= xmin && x <= xmax && y >= ymin && y <= ymax;
    }

    /**
     * Sprawdza czy dany BB zawiera bb
     * @param bb
     * @return
     */
    boolean contains(BoundingBox bb){
        if (this.isEmpty() || bb.isEmpty()) return false;
        else return bb.xmin >= xmin && bb.xmax <= xmax && bb.ymin >= ymin && bb.ymax <= ymax;
    }

    /**
     * Sprawdza, czy dany BB przecina się z bb
     * @param bb
     * @return
     */
    boolean intersects(BoundingBox bb){
        if (this.isEmpty() || bb.isEmpty()) return false;
        else return (xmin <= bb.xmax) && (xmax >= bb.xmin) &&
                (ymin <= bb.ymax) && (ymax >= bb.ymin);
    }


    /**
     * Powiększa rozmiary tak, aby zawierał bb oraz poprzednią wersję this
     * @param bb
     * @return
     */
    BoundingBox add(BoundingBox bb){
        if (!bb.isEmpty()) {
            addPoint(bb.xmin, bb.ymin);
            addPoint(bb.xmax, bb.ymax);
        }
        return this;
    }
    /**
     * Sprawdza czy BB jest pusty
     * @return
     */
    boolean isEmpty(){
        return Double.isNaN(xmin) || Double.isNaN(xmax) || Double.isNaN(ymin) || Double.isNaN(ymax);
    }

    /**
     * Oblicza i zwraca współrzędną x środka
     * @return if !isEmpty() współrzędna x środka else wyrzuca wyjątek
     * (sam dobierz typ)
     */
    double getCenterX(){
        if (!isEmpty()) return (xmin + xmax) / 2;
        else throw new RuntimeException("Not implemented"); //może inny typ wyjątku?
    }
    /**
     * Oblicza i zwraca współrzędną y środka
     * @return if !isEmpty() współrzędna y środka else wyrzuca wyjątek
     * (sam dobierz typ)
     */
    double getCenterY(){
        if (!isEmpty()) return (ymin + ymax) / 2;
        else throw new RuntimeException("Not implemented");
    }

    /**
     * Pomocnicza dla distanceTo()
     */
    private static Double toRad(Double value) {
        return value * Math.PI / 180;
    }

    /**
     * Oblicza odległość pomiędzy środkami this bounding box oraz bbx
     * @param bbx prostokąt, do którego liczona jest odległość
     * @return if !isEmpty odległość, else wyrzuca wyjątek lub zwraca maksymalną możliwą wartość double
     * Ze względu na to, że są to współrzędne geograficzne, zamiast odległosci euklidesowej możesz użyć wzoru haversine
     * (ang. haversine formula)
     */
/*    double distanceTo(BoundingBox bbx){
        if(!isEmpty() && !bbx.isEmpty()) {
            double latDistance = getCenterX()-bbx.getCenterX();
            double dist = Math.sin(Math.toRadians(getCenterY())) * Math.sin(Math.toRadians(bbx.getCenterY()))
                    + Math.cos(Math.toRadians(getCenterY())) * Math.cos(Math.toRadians(bbx.getCenterY())) * Math.cos(Math.toRadians(latDistance));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            return dist * 60 * 1.1515;
        }
        else return Double.NaN;
    } */

    double distanceTo(BoundingBox bbx){
        if(isEmpty() || bbx.isEmpty())
            throw new IllegalStateException("Exception");

        double centerX = getCenterX();
        double centerY = getCenterY();
        double bbCenterX = bbx.getCenterX();
        double bbCenterY = bbx.getCenterY();

        return distance(
                Math.min(centerY, bbCenterY),
                Math.min(centerX, bbCenterX),
                Math.max(centerY, bbCenterY),
                Math.max(centerX, bbCenterX));
    }

    static double distance(double startLat, double startLong, double endLat, double endLong) {
        int r = 6371;
        double dLat  = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat   = Math.toRadians(endLat);

        double a = Math.pow(Math.sin(dLat/2),2) + Math.cos(startLat) * Math.cos(endLat) * Math.pow(Math.sin(dLong/2),2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return r * c; // <-- d
    }


    void toString(PrintStream out) {
        out.print("   BoundingBox: ");
        if(isEmpty()) out.print("---");
        else if (xmin == xmax && ymin == ymax) out.print("(" + xmin + "," + ymin + ")");
        else out.print("(" + xmin + "," + ymax + "), " + "(" + xmax + "," + ymax + "), " + "(" + xmax + "," + ymin + "), " + "(" + xmin + "," + ymin + ")");
    }

    void toStringMap(PrintStream out) {
        if(isEmpty()) out.print("");
        else if (xmin == xmax && ymin == ymax) out.print("(" + xmin + " " + ymin + ")");
        else out.print("(" + xmin + " " + ymax + ", " + xmax + " " + ymax + ", " + xmax + " " + ymin + ", " + xmin + " " + ymin + ", " + xmin + " " + ymax + ")");
    }
}