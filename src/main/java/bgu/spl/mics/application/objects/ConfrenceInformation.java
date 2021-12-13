package bgu.spl.mics.application.objects;

import java.util.Objects;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConfrenceInformation {

    private String name;
    private int date;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfrenceInformation that = (ConfrenceInformation) o;
        return date == that.date && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, date);
    }
}
