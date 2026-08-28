package ejercicios.Easy.String;

public class FindTheIndexOfTheFirstOccurrenceInAString {

    /*
     * SOLUCIÓN 1 — Fuerza bruta (ventana deslizante). O(n * m) tiempo, O(1) espacio.
     * Es la respuesta esperada para un Easy y la que conviene defender en una entrevista.
     */
    public int strStr(String haystack, String needle) {

        // Si needle es más largo que haystack, no puede estar contenido
        if (needle.length() > haystack.length()) {
            return -1;
        }

        // Recorrer cada posición de inicio posible dentro de haystack.
        // El límite es haystack.length() - needle.length() porque más allá
        // ya no quedan caracteres suficientes para que needle quepa entero.
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

    /*
     * SOLUCIÓN 2 — KMP (Knuth-Morris-Pratt). O(n + m) tiempo, O(m) espacio.
     *
     * Idea: cuando falla una comparación tras haber acertado j caracteres, ya sabemos
     * exactamente qué acabamos de leer: es needle[0..j-1]. La fuerza bruta tira esa
     * información y reinicia; KMP la aprovecha para no retroceder nunca en haystack.
     */
    public int strStrKMP(String haystack, String needle) {

        if (needle.length() > haystack.length()) {
            return -1;
        }

        // Las restricciones garantizan needle.length >= 1, pero sin esta guarda
        // el bucle haría needle.charAt(0) sobre una cadena vacía y reventaría
        if (needle.isEmpty()) {
            return 0;
        }

        int[] lps = construirLPS(needle);

        int i = 0; // puntero en haystack: SOLO avanza, nunca retrocede
        int j = 0; // puntero en needle: retrocede usando lps al fallar

        while (i < haystack.length()) {

            if (haystack.charAt(i) == needle.charAt(j)) {
                i++;
                j++;

                // needle consumido entero: la coincidencia empezó j posiciones atrás
                if (j == needle.length()) {
                    return i - j;
                }

            } else if (j > 0) {
                // Fallo con progreso: no reiniciamos. Los últimos lps[j-1] caracteres
                // acertados también son prefijo de needle, así que ya están alineados.
                j = lps[j - 1];

            } else {
                // Fallo sin progreso (j == 0): no hay nada que reaprovechar
                i++;
            }
        }

        return -1;
    }

    /*
     * Tabla LPS (Longest Proper Prefix which is also Suffix).
     * lps[i] = longitud del prefijo propio más largo de needle[0..i] que además
     * es sufijo de needle[0..i]. "Propio" = no puede ser la cadena entera.
     *
     * Ejemplo, needle = "aabaa":
     *   "a"     -> 0
     *   "aa"    -> 1  ("a")
     *   "aab"   -> 0
     *   "aaba"  -> 1  ("a")
     *   "aabaa" -> 2  ("aa")
     */
    private int[] construirLPS(String needle) {

        int[] lps = new int[needle.length()];
        int longitud = 0; // longitud del prefijo-sufijo que llevamos confirmado

        // lps[0] siempre es 0: una cadena de un carácter no tiene prefijo propio
        for (int i = 1; i < needle.length(); i++) {

            // Mientras no cuadre, retrocedemos al siguiente mejor candidato.
            // Esto es KMP aplicado a needle contra sí mismo.
            while (longitud > 0 && needle.charAt(i) != needle.charAt(longitud)) {
                longitud = lps[longitud - 1];
            }

            if (needle.charAt(i) == needle.charAt(longitud)) {
                longitud++;
            }

            lps[i] = longitud;
        }

        return lps;
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
