package net.innig.macker.structure;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author Paul Cantrell
 */
public class ClassInfoNameComparator implements Comparator<ClassInfo>, Serializable {

    private static final long serialVersionUID = -8101657315805571521L;

    public int compare(final ClassInfo a, final ClassInfo b) {
        return a.getFullName().compareTo(b.getFullName());
    }
}
