import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Predicate;

public class AdminUnitList {
    List<AdminUnit> units = new ArrayList<>();
    Map<Long, AdminUnit> idToAdminUnit = new HashMap<>();
    Map<AdminUnit, Long> adminUnitToParentId = new HashMap<>();

    public AdminUnitList(List<AdminUnit> units, Map<Long, AdminUnit> idToAdminUnit, Map<AdminUnit, Long> adminUnitToParentId) {
        this.units = units;
        this.idToAdminUnit = idToAdminUnit;
        this.adminUnitToParentId = adminUnitToParentId;
    }

    public AdminUnitList() {}

    /**
     * Czyta rekordy pliku i dodaje do listy
     * @param filename nazwa pliku
     */
    public void read(String filename) throws IOException, EmptyColumnException {
        CSVReader reader = new CSVReader(filename, ",", true);
        while (reader.next()) {
            AdminUnit newUnit = new AdminUnit();
            Long id = reader.getLong("id");
            Long parentId;
            try {parentId = reader.getLong("parent");}
            catch(EmptyColumnException e) {parentId = (long) -1;}
            newUnit.name = reader.get(2);
            try {newUnit.adminLevel = reader.getInt(3); }
            catch (EmptyColumnException e) {newUnit.adminLevel = 0; }

            try {newUnit.population = reader.getDouble(4);}
            catch (EmptyColumnException e) { newUnit.population = -1;}

            try {newUnit.area = reader.getDouble(5);}
            catch (EmptyColumnException e) {newUnit.area = -1; }

            try {newUnit.density = reader.getDouble(6); }
            catch (EmptyColumnException e) {newUnit.density = -1;}
            newUnit.children=new ArrayList<>();
            List<Double> x = new ArrayList<>();
            List<Double> y = new ArrayList<>();
            try {x.add(reader.getDouble("x1")); }
            catch (EmptyColumnException ignored) {}
            try {x.add(reader.getDouble("x2")); }
            catch (EmptyColumnException ignored) {}
            try {x.add(reader.getDouble("x3")); }
            catch (EmptyColumnException ignored) {}
            try {x.add(reader.getDouble("x4")); }
            catch (EmptyColumnException ignored) {}
            try {y.add(reader.getDouble("y1")); }
            catch (EmptyColumnException ignored) {}
            try {y.add(reader.getDouble("y2")); }
            catch (EmptyColumnException ignored) {}
            try {y.add(reader.getDouble("y3")); }
            catch (EmptyColumnException ignored) {}
            try {y.add(reader.getDouble("y4")); }
            catch (EmptyColumnException ignored) {}
            if (!x.isEmpty() && !y.isEmpty()) newUnit.bbox.addPoint(Collections.min(x), Collections.min(y));
            if (!x.isEmpty() && !y.isEmpty()) newUnit.bbox.addPoint(Collections.max(x), Collections.max(y));

            units.add(newUnit);
            idToAdminUnit.put(id, newUnit);
            adminUnitToParentId.put(newUnit, parentId);
        }

        for (Long id : idToAdminUnit.keySet()) {
            AdminUnit unit = idToAdminUnit.get(id);
            Long parentId = adminUnitToParentId.get(unit);
            if (parentId >= 0) {
                AdminUnit parent = idToAdminUnit.get(parentId);
                unit.parent = parent;
                parent.children.add(unit);
            }
            else {unit.parent = null;}
        }
    }


    public void fixMissingValues() {
        for (AdminUnit unit : units) {
            unit.fixMissingValues();
        }
    }

    /**
     * Wypisuje zawartość korzystając z AdminUnit.toString()
     * @param out
     */
    void list(PrintStream out){
        int i=0;
        for (AdminUnit unit : units) {
            out.print(i + ". ");
            unit.toString(out);
            i++;
        }
    }
    /**
     * Wypisuje co najwyżej limit elementów począwszy od elementu o indeksie offset
     * @param out - strumień wyjsciowy
     * @param offset - od którego elementu rozpocząć wypisywanie
     * @param limit - ile (maksymalnie) elementów wypisać
     */
    void list(PrintStream out,int offset, int limit ){
        if (offset < 0) return;
        for (int i=offset; i < offset + limit; i++) {
            if (i < units.size()) units.get(i).toString(out);
        }
    }

    /**
     * Zwraca listę jednostek sąsiadujących z jendostką unit na tym samym poziomie hierarchii admin_level.
     * Czyli sąsiadami wojweództw są województwa, powiatów - powiaty, gmin - gminy, miejscowości - inne miejscowości
     * @param unit - jednostka, której sąsiedzi mają być wyznaczeni
     * @param maxdistance - parametr stosowany wyłącznie dla miejscowości, maksymalny promień odległości od środka unit,
     *                    w którym mają sie znaleźć punkty środkowe BoundingBox sąsiadów
     * @return lista wypełniona sąsiadami
     */
    AdminUnitList getNeighbors(AdminUnit unit, double maxdistance){
        AdminUnitList neighbors = new AdminUnitList();
        for (AdminUnit potNeighbor : units) {
            if (potNeighbor.adminLevel == unit.adminLevel) {
                if(!potNeighbor.name.equals(unit.name)) {
                    if (potNeighbor.bbox.intersects(unit.bbox)) {
                        neighbors.units.add(potNeighbor);
                    }
                    else if (potNeighbor.bbox.distanceTo(unit.bbox) < maxdistance)  neighbors.units.add(potNeighbor);
                }
//                if (potNeighbor.adminLevel > 8) {
//                    if (potNeighbor.bbox.distanceTo(unit.bbox) < maxdistance) neighbors.units.add(potNeighbor);
//                }
            }
        }
        return neighbors;
    }



    /**
     * Zwraca nową listę zawierającą te obiekty AdminUnit, których nazwa pasuje do wzorca
     * @param pattern - wzorzec dla nazwy
     * @param regex - jeśli regex=true, użyj finkcji String matches(); jeśli false użyj funkcji contains()
     * @return podzbiór elementów, których nazwy spełniają kryterium wyboru
     */
    AdminUnitList selectByName(String pattern, boolean regex){
        AdminUnitList ret = new AdminUnitList();
        // przeiteruj po zawartości units
        // jeżeli nazwa jednostki pasuje do wzorca dodaj do ret
        if (regex) {
            for (AdminUnit unit : units) {
                if (unit.name.matches(pattern)) ret.units.add(unit);
            }
        }

        if (!regex) {
            for (AdminUnit unit : units) {
                if (unit.name.contains(pattern)) ret.units.add(unit);
            }
        }
        return ret;
    }


    /**
     * Sortuje daną listę jednostek (in place = w miejscu)
     * @return this
     */
    AdminUnitList sortInplaceByName(){
        class SortHelp implements Comparator<AdminUnit> {

            @Override
            public int compare(AdminUnit au1, AdminUnit au2) {
                return au1.name.compareToIgnoreCase(au2.name);
            }
        }
        units.sort(new SortHelp());
        return this;
    }

    /**
     * Sortuje daną listę jednostek (in place = w miejscu)
     * @return this
     */
    AdminUnitList sortInplaceByArea(){
        units.sort(new Comparator<AdminUnit>() {
            @Override
            public int compare(AdminUnit au1, AdminUnit au2) {
                return Double.compare(au1.area, au2.area);
            }
        });
        return this;
    }

    /**
     * Sortuje daną listę jednostek (in place = w miejscu)
     * @return this
     */
    AdminUnitList sortInplaceByPopulation(){
        units.sort((au1, au2) -> Double.compare(au1.population, au2.population));
        return this;
    }

    AdminUnitList sortInplace(Comparator<AdminUnit> cmp){
        units.sort(cmp);
        return this;
    }

    AdminUnitList sort(Comparator<AdminUnit> cmp){
        AdminUnitList second = new AdminUnitList();
//        second.list(System.out);
        for(int i=0;i<units.size();i++){
            second.units.add(new AdminUnit(units.get(i)));
        }
        second.sortInplace(cmp);
        return second;
    }


    public static Predicate<AdminUnit> startsWithK() {
        return au -> au.name.startsWith("K");
    }

    public static Predicate<AdminUnit> powiat() {
        return au -> au.adminLevel == 6;
    }

    public static Predicate<AdminUnit> parentMalop() {
        return au -> au.parent.name.equals("województwo małopolskie") ;
    }

    public static Predicate<AdminUnit> denseOrLarge() {
        Predicate<AdminUnit> dense = au -> au.density > 4000;
        Predicate<AdminUnit> large = au -> au.area > 100;
        return dense.or(large);
    }

    public static Predicate<AdminUnit> highPopNotW() {
        Predicate<AdminUnit> highPop = au -> au.population > 250000;
        Predicate<AdminUnit> contW = au -> au.name.contains("w");
        return highPop.and(contW.negate());
    }

    /**
     *
     * @param pred referencja do interfejsu Predicate
     * @return nową listę, na której pozostawiono tylko te jednostki,
     * dla których metoda test() zwraca true
     */
    AdminUnitList filter(Predicate<AdminUnit> pred){
        AdminUnitList filtered  = new AdminUnitList();
        for(int i=0;i<units.size();i++){
            if(pred.test(units.get(i))) {
                filtered.units.add(new AdminUnit(units.get(i)));
            }
        }
        return filtered;
    }

    /**
     * Zwraca co najwyżej limit elementów spełniających pred począwszy od offset
     * Offest jest obliczany po przefiltrowaniu
     * @param pred - predykat
     * @param - od którego elementu
     * @param limit - maksymalna liczba elementów
     * @return nową listę
     */
    AdminUnitList filter(Predicate<AdminUnit> pred, int offset, int limit){
        AdminUnitList filtered  = new AdminUnitList();
        int i=offset;
        while(i<units.size() && limit>0){
            if(pred.test(units.get(i))) {
                filtered.units.add(new AdminUnit(units.get(i)));
                limit--;
            }
            i++;
        }
        return filtered;
    }

    /**
     * Zwraca co najwyżej limit elementów spełniających pred
     * @param pred - predykat
     * @param limit - maksymalna liczba elementów
     * @return nową listę
     */
    AdminUnitList filter(Predicate<AdminUnit> pred, int limit){
        return filter(pred, 0, limit);
    }

}
