import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, EmptyColumnException {
        AdminUnitList aul = new AdminUnitList();
        aul.read("admin-units.csv");
        AdminUnitList zabrze = aul.selectByName("Zabrze", true);
        for(AdminUnit au : zabrze.units) {
            aul.getNeighbors(au, 50).list(System.out);
        }

    }
}
