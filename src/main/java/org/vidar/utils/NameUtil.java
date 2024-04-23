
package org.vidar.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class NameUtil {
    private final static char[] DICT_SPACES = new char[]{
            '\u2000', '\u2001', '\u2002', '\u2003', '\u2004',
            '\u2005', '\u2006', '\u2007', '\u2008', '\u2009',
            '\u200A', '\u200B', '\u200C', '\u200D', '\u200E',
            '\u200F'
    };
    private static final HashMap<String, Integer> packageMap = new HashMap<>();
    private static final Map<String, HashMap<String, Integer>> USED_METHOD_NAMES = new HashMap<>();
    private static final Map<String, Integer> USED_FIELD_NAMES = new HashMap<>();
    private static int localVars = Short.MAX_VALUE;
    private static final Random random = new Random();
    private static int METHODS = 0;
    private static int FIELDS = 0;

    @SuppressWarnings("SameParameterValue")
    private static int randInt(int min, int max) {
        return random.nextInt(max - min) + min;
    }

    public static void setup(final String classCharacters,
                             final String methodCharacters,
                             final String fieldCharacters,
                             boolean iL) {
        USED_METHOD_NAMES.clear();
        USED_FIELD_NAMES.clear();

    }

    public static String generateSpaceString(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

    public static String generateClassName() {
        String packageName = "";
        if (!packageMap.containsKey(packageName)) {
            packageMap.put(packageName, 0);
        }
        int id = packageMap.get(packageName);
        packageMap.put(packageName, id + 1);
        return "L" + toIl(id);
    }

    public static String crazyString(int len) {
        char[] buildString = new char[len];
        for (int i = 0; i < len; i++) {
            buildString[i] = DICT_SPACES[random.nextInt(DICT_SPACES.length)];
        }
        return new String(buildString);
    }

    public static String generateMethodName(final String className, String desc) {
        return toIl(METHODS++);
    }

    public static String generateFieldName(final String className) {
        return toIl(FIELDS++);
    }

    public static String generateLocalVariableName(final String className, final String methodName) {
        return generateLocalVariableName();
    }

    public static String generateLocalVariableName() {
        return toIl(localVars--);
    }

    public static String unicodeString(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append((char) randInt(128, 250));
        }
        return stringBuilder.toString();
    }

    public static void mapClass(String old, String newName) {
        if (USED_METHOD_NAMES.containsKey(old)) {
            USED_METHOD_NAMES.put(newName, USED_METHOD_NAMES.get(old));
        }
        if (USED_FIELD_NAMES.containsKey(old)) {
            USED_FIELD_NAMES.put(newName, USED_FIELD_NAMES.get(old));
        }
    }

    public static String toIl(int i) {
        return Integer.toBinaryString(i).replace('0', 'I').replace('1', 'l');
    }

}
