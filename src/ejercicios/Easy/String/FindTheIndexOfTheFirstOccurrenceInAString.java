package ejercicios.Easy.String;

public class FindTheIndexOfTheFirstOccurrenceInAString {

    public int strStr(String haystack, String needle) {

        // Si needle es más largo que haystack, no puede estar contenido
        if (needle.length() > haystack.length()) {
            return -1;
        }

        // Recorrer cada posición de inicio posible dentro de haystack
        for (int i = 0; i <= haystack.length() - needle.length(); i++) {

            // Comparar carácter a carácter needle con el trozo que empieza en i
            int j = 0;
            while (j < needle.length() && haystack.charAt(i + j) == needle.charAt(j)) {
                j++;
            }

            // Si se recorrió needle entero, hay coincidencia en la posición i
            if (j == needle.length()) {
                return i;
            }
        }

        // No se encontró ninguna ocurrencia
        return -1;
    }
}

/*
Given two strings needle and haystack, return the index of the first occurrence of needle in haystack,
or -1 if needle is not part of haystack.

Example 1:

Input: haystack = "sadbutsad", needle = "sad"
Output: 0
Explanation: "sad" occurs at index 0 and 6.
The first occurrence is at index 0, so we return 0.

Example 2:

Input: haystack = "leetcode", needle = "leeto"
Output: -1
Explanation: "leeto" did not occur in "leetcode", so we return -1.

Constraints:

1 <= haystack.length, needle.length <= 10^4
haystack and needle consist of only lowercase English characters.
 */
