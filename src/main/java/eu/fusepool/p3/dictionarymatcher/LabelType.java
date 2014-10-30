package eu.fusepool.p3.dictionarymatcher;

/**
 *
 * @author Gabor
 */
public enum LabelType {

    PREF("prefLabel"),
    ALT("altLabel");

    private final String name;

    private LabelType(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static LabelType getByName(String name) {
        return LabelType.valueOf(name);
    }

    public static LabelType getByValue(String value) {
        for (LabelType e : LabelType.values()) {
            if (e.name.equals(value)) {
                return e;
            }
        }
        return null;
    }

    public static boolean isPrefLabel(String label) {
        return PREF.name.equals(label);
    }

    public static boolean isPrefLabel(LabelType label) {
        return PREF.equals(label);
    }

    public static boolean isAltLabel(String label) {
        return ALT.name.equals(label);
    }

    public static boolean isAltLabel(LabelType label) {
        return ALT.equals(label);
    }

    @Override
    public String toString() {
        return name;
    }
}
