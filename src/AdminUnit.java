import java.io.PrintStream;
import java.util.*;

public class AdminUnit {
    String name; //w przyszłości - prywatne + konstruktor?
    int adminLevel;
    double population;
    double area;
    double density;
    AdminUnit parent;
    BoundingBox bbox = new BoundingBox();
    List<AdminUnit> children;

    public AdminUnit() {}

    //konstruktor kopiujący
    public AdminUnit(AdminUnit another){
        this.name=another.name;
        this.adminLevel=another.adminLevel;
        this.population=another.population;
        this.area=another.area;
        this.density=another.density;
        try {this.parent= new AdminUnit(another.parent);}
        catch (NullPointerException e) {}
        try {this.bbox= new BoundingBox(another.bbox); }
        catch (NullPointerException e) {}
        try {for(int i=0;i<another.children.size();i++) this.children.add(another.children.get(i)); } //this.children.add(new AdminUnit(another.children.get(i)));
        catch (NullPointerException e) {}
    }



    void toString(PrintStream out) {
        String info =  "Name: " + name;
        if (adminLevel > 0) info += "   Administrative level: " + adminLevel;
        if (population >= 0) info += "   Population: " + population;
        if (area >= 0) info += "   Area: " + area;
        if (density >= 0) info += "   Density: " + density;
        out.print(info);
        if (!bbox.isEmpty()) bbox.toString(out);
        out.print("\n");
    }

    void toStringMap(PrintStream out) {
        String info =  "Name: " + name;
        if (adminLevel > 0) info += "   Admin level: " + adminLevel;
        out.print(info + "  ");
        if (!bbox.isEmpty()) {
            System.out.printf(Locale.US,"LINESTRING(");
            bbox.toStringMap(out);
            System.out.printf(")");
        }
        out.print("\n");
    }

    public void fixMissingValues() {
        if (density < 0) {
            parent.fixMissingValues();
            density = parent.density;
        }
        if (population < 0) {
            parent.fixMissingValues();
            population = area * density;
        }
    }
}
