import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import static org.junit.Assert.*;

public class MainTest {
    @org.junit.Test
    public void CSVReaderAdminTest() throws IOException, EmptyColumnException {
        CSVReader reader = new CSVReader("admin-units.csv", ",", true);
        String test = new String();
        for (int i = 0; i < 100; i++) {
            if (reader.next()) {
                String id = reader.get(0);
                String parent = reader.get(1);
                String name = reader.get(2);
                String a_l = reader.get(3);

                String pop = new String();
                try {
                    pop = reader.get(4);
                } catch (EmptyColumnException e) {
                    pop = "---";
                }
                String area = reader.get(5);

                test += id + " " + parent + " " + name + " " + a_l + " " + pop + " " + area + "\n";
            }
        }
        System.out.print(test);
    }

    @org.junit.Test
    public void selectByNameAndListAndBoundingBoxTest() throws IOException, EmptyColumnException {
        AdminUnitList adUnLi = new AdminUnitList();
        adUnLi.read("admin-units.csv");
        System.out.println("Selected by list:");
        adUnLi.list(System.out, 12, 30);
        AdminUnitList selected1 = adUnLi.selectByName("Nowy", false);
        AdminUnitList selected2 = adUnLi.selectByName("Now.*", true);
        System.out.println("Selected without regex:");
        for(AdminUnit unit : selected1.units) {
            unit.toString(System.out);
        }
        System.out.println("Selected using regex:");
        for(AdminUnit unit : selected2.units) {
            unit.toString(System.out);
        }
    }

    @org.junit.Test
    public void fixMissingValuesTest() throws IOException, EmptyColumnException {
        AdminUnitList adUnLi = new AdminUnitList();
        adUnLi.read("admin-units.csv");
        AdminUnitList selected1 = adUnLi.selectByName("Kolonia", false);
        selected1.fixMissingValues();
        for(AdminUnit unit : selected1.units) {
            unit.toString(System.out);
        }
    }

    @org.junit.Test
    public void childrenTest() throws IOException, EmptyColumnException {
        AdminUnitList adUnLi = new AdminUnitList();
        adUnLi.read("admin-units.csv");
        AdminUnitList selected1 = adUnLi.selectByName("powiat żywiecki", false);
        for(AdminUnit unit : selected1.units) {
            for (AdminUnit child : unit.children) {
                child.toString(System.out);
            }
        }
    }

    @org.junit.Test
    public void getNeighborsTest() throws IOException, EmptyColumnException {
        AdminUnitList adUnLi = new AdminUnitList();
        adUnLi.read("admin-units.csv");
        AdminUnit zabrze = adUnLi.units.get(4351);
        System.out.print("MULTIPOLYGON (");
        zabrze.bbox.toStringMap(System.out);
        System.out.print(", ");
        double t1 = System.nanoTime()/1e6;
        AdminUnitList neighbors = adUnLi.getNeighbors(zabrze, 15);
        double t2 = System.nanoTime()/1e6;
        for (AdminUnit neighbor : neighbors.units) {
            neighbor.bbox.toStringMap(System.out);
            System.out.print(", ");
        }
        System.out.print(")\n\n");
        for (AdminUnit neighbor : neighbors.units) {
            neighbor.toString(System.out);
        }
        System.out.println("Czas wykonania zadania:  ");
        System.out.printf(Locale.US,"t2-t1=%f\n",t2-t1);
    }

    @org.junit.Test
    public void sortInPlaceTest() throws IOException, EmptyColumnException {
        AdminUnitList adUnLi = new AdminUnitList();
        adUnLi.read("admin-units.csv");
        adUnLi.sortInplaceByName();
        System.out.println("Posortowana wg nazwy lista:");
        adUnLi.list(System.out, 0, 10);

        adUnLi.sortInplaceByArea();
        System.out.println("\n\nPosortowana wg powierzchni lista:");
        adUnLi.list(System.out, 10, 10);

        adUnLi.sortInplaceByPopulation();
        System.out.println("\n\nPosortowana wg populacji lista:");
        adUnLi.list(System.out, 14000, 10);
    }

    @org.junit.Test
    public void sortCopyTest() throws IOException, EmptyColumnException {
        AdminUnitList adUnLi = new AdminUnitList();
        adUnLi.read("admin-units.csv");
        AdminUnitList sorted = adUnLi.sort((au1, au2) -> Double.compare(au1.area, au2.area));
        System.out.println("Posortowana wg powierzchni lista:");
        sorted.list(System.out, 30, 10);
        System.out.println("Nieposortowana lista:");
        adUnLi.list(System.out, 30, 10);

/*        System.out.println("\nCzy children zrobiło się deepcopy?\nPosortowana lista:");
        AdminUnit selected1 = sorted.units.get(15000);
        selected1.toString(System.out);
            for (AdminUnit child : selected1.children) {
                child.name = child.name + " changed";
                child.toString(System.out);
            }

        System.out.println("\nWyjściowa lista:");
        AdminUnitList selected2 = adUnLi.selectByName("powiat radziejowski", false);
        for(AdminUnit unit : selected2.units) {
            for (AdminUnit child : unit.children) {
                child.toString(System.out);
            }
        }

 */
    }

    @org.junit.Test
    public void filterTest() throws IOException, EmptyColumnException {
        AdminUnitList adUnLi = new AdminUnitList();
        adUnLi.read("admin-units.csv");
        AdminUnitList filtered = adUnLi.filter(a->a.name.startsWith("Ż")).sortInplaceByArea();
        System.out.println("Przefiltrowana lista 1:");
        filtered.list(System.out, 0, 10);

        AdminUnitList startingWithK = adUnLi.filter(AdminUnitList.startsWithK());
        System.out.println("\n\nZaczynające się na K:");
        startingWithK.list(System.out, 0, 10);

        AdminUnitList powParMalop = adUnLi.filter(AdminUnitList.powiat().and(AdminUnitList.parentMalop()));
        System.out.println("\n\nBędące powiatami, których parent.name to województwo małopolskie:");
        powParMalop.list(System.out, 0, 10);

        AdminUnitList denseOrLarge = adUnLi.filter(AdminUnitList.denseOrLarge(), 30, 10);
        System.out.println("\n\nGęsto zaludnione lub duże:");
        denseOrLarge.list(System.out);

        AdminUnitList highPopNotW = adUnLi.filter(AdminUnitList.highPopNotW(),10);
        System.out.println("\n\nO dużej populacji, nie zawierające litery \"w\":");
        highPopNotW.list(System.out);
    }

    @org.junit.Test
    public void adminUnitQueryTest() throws IOException, EmptyColumnException {
        AdminUnitList adUnLi = new AdminUnitList();
        adUnLi.read("admin-units.csv");
        AdminUnitQuery query = new AdminUnitQuery()
                .selectFrom(adUnLi)
                .where(a->a.area>1000)
                .or(a->a.name.startsWith("Sz"))
                .sort((a,b)->Double.compare(a.area,b.area))
                .limit(100);
        query.execute().list(System.out);
    }




}