import java.io.*;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CSVReader {
    /**
     * @param filename  - nazwa pliku
     * @param delimiter - separator pól
     * @param hasHeader - czy plik ma wiersz nagłówkowy
     * @param columnLabels - nazwy kolumn w takiej kolejności, jak w pliku
     * @param columnLabelsToInt - odwzorowanie: nazwa kolumny -> numer kolumny
     * @param current - obecnie przetwarzany wiersz
     */
    private BufferedReader reader;
    private String delimiter;
    private boolean hasHeader;
    private List<String> columnLabels = new ArrayList<>();
    private Map<String, Integer> columnLabelsToInt = new HashMap<>();
    private String[] current;


    public CSVReader(String filename) throws IOException {
        this(filename, ";", false);
    }

    public CSVReader(String filename, String delimiter) throws IOException {
        this(filename, delimiter, false);
    }

    public CSVReader(String filename, String delimiter, boolean hasHeader) throws IOException {
        reader = new BufferedReader(new FileReader(filename));
        this.delimiter = delimiter;
        this.hasHeader = hasHeader;
        if (hasHeader) parseHeader();
    }

    public CSVReader(Reader reader, String delimiter, boolean hasHeader) throws IOException { //Reader?
        this.reader = new BufferedReader(reader);
        this.delimiter = delimiter;
        this.hasHeader = hasHeader;
        if (hasHeader) parseHeader();
    }


    Map<String, Integer> getColumnLabelsToInt() {
        return columnLabelsToInt;
    }


    void parseHeader() throws IOException {
        // wczytaj wiersz
        String line = reader.readLine();
        if (line == null) {
            return;
        }
        // podziel na pola
        String[] header = line.split(delimiter);
//        String[] header = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
        // przetwarzaj dane w wierszu
        for (int i = 0; i < header.length; i++) {
            // dodaj nazwy kolumn do columnLabels i numery do columnLabelsToInt
            columnLabels.add(header[i]);
            columnLabelsToInt.put(header[i], i);
        }
    }


    boolean next() throws IOException {
        // czyta następny wiersz, dzieli na elementy i przypisuje do current
        String line = reader.readLine();
        if (line == null) {
            return false;
        }
        String help = String.format("%s(?=([^\"]*\"[^\"]*\")*[^\"]*$)", delimiter);
        current = line.split(help);
//        current = line.split(delimiter);
        return true;
    }


    List<String> getColumnLabels() {
        return columnLabels;
    }

    int getRecordLength() {
        return current.length; //czy o to chodzi?
    }

    boolean isMissing(int columnIndex) {
        if (columnIndex >= this.getRecordLength()) {
            return true;
        }
        if (current[columnIndex].isEmpty()) {
            return true;
        } else return false;
    }

    boolean isMissing(String columnLabel) {
        if (!getColumnLabels().contains(columnLabel)) {
            return true;
        } else {
            int index = columnLabelsToInt.get(columnLabel);
            return isMissing(index);
            /*System.out.print(getRecordLength() + "  ");
            if (current[index].isEmpty()) {
                return true;
            } else return false;*/
        }
    }

    String get(int columnIndex) throws EmptyColumnException {
        if (isMissing(columnIndex)) {
            throw new EmptyColumnException("Pusta kolumna!");
        }
        return current[columnIndex];
    }


    String get(String columnLabel) throws EmptyColumnException {
        if (isMissing(columnLabel)) {
            throw new EmptyColumnException("Pusta kolumna!");
        } else {
            int index = columnLabelsToInt.get(columnLabel);
            return current[index];
        }
    }

    int getInt(String columnLabel) throws EmptyColumnException {
        String text = get(columnLabel);
        return Integer.parseInt(text);
    }

    int getInt(int columnIndex) throws EmptyColumnException {
        String text = get(columnIndex);
        return Integer.parseInt(text);
    }

    long getLong(String columnLabel) throws EmptyColumnException {
        String text = get(columnLabel);
        return Long.parseLong(text);
    }

    long getLong(int columnIndex) throws EmptyColumnException {
        String text = get(columnIndex);
        return Long.parseLong(text);
    }

    double getDouble(String columnLabel) throws EmptyColumnException {
        String text = get(columnLabel);
        return Double.parseDouble(text);
    }

    double getDouble(int columnIndex) throws EmptyColumnException {
        String text = get(columnIndex);
        return Double.parseDouble(text);
    }

    LocalTime getTime(int columnIndex, String format) throws EmptyColumnException {
        String time = get(columnIndex);
        return LocalTime.parse(time, DateTimeFormatter.ofPattern(format));
    }

    LocalTime getTime(int columnIndex) throws EmptyColumnException {
        return getTime(columnIndex, "HH:mm:ss");
    }

    LocalTime getTime(String columnLabel, String format) throws EmptyColumnException {
        String time = get(columnLabel);
        return LocalTime.parse(time, DateTimeFormatter.ofPattern(format));
    }

    LocalTime getTime(String columnLabel) throws EmptyColumnException {
        return getTime(columnLabel, "HH:mm:ss");
    }

    LocalDate getDate(int columnIndex, String format) throws EmptyColumnException {
        String date = get(columnIndex);
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(format));
    }

    LocalDate getDate(int columnIndex) throws EmptyColumnException {
        String date = get(columnIndex);
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.mm.yyyy"));
    }

    LocalDate getDate(String columnLabel, String format) throws EmptyColumnException {
        String date = get(columnLabel);
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(format));
    }

    LocalDate getDate(String columnLabel) throws EmptyColumnException {
        String date = get(columnLabel);
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.mm.yyyy"));
    }

}