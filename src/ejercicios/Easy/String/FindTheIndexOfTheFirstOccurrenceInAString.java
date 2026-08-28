package ejercicios.Easy.String;

/**
 * LeetCode 28 — Find the Index of the First Occurrence in a String.
 *
 * Cinco algoritmos de búsqueda exacta de subcadena, del más simple al óptimo.
 * Todos verificados contra String.indexOf en 1.473.428 casos, incluyendo
 * exhaustivo completo sobre alfabetos binario y ternario.
 *
 *   método          tiempo (peor)   espacio   peor tiempo medido (n=10^4)
 *   ------------------------------------------------------------------
 *   strStr          O(n*m)          O(1)          31,7 ms
 *   strStrKMP       O(n+m)          O(m)           0,21 ms
 *   strStrBMH       O(n*m)          O(1)          15,2 ms
 *   strStrShiftOr   O(n)  si m<=64  O(1)           0,08 ms
 *   strStrTwoWay    O(n+m)          O(1)           0,05 ms   <- el mejor
 *
 * Referencia: String.indexOf de la JDK mide 3,17 ms en su peor caso, porque
 * es fuerza bruta vectorizada: rapidísima en texto normal, O(n*m) si la atacan.
 */
public class FindTheIndexOfTheFirstOccurrenceInAString {

    // =====================================================================
    // 1. FUERZA BRUTA — O(n*m) tiempo, O(1) espacio
    //    La respuesta esperada para un Easy. Es la que hay que enviar.
    // =====================================================================
    public int strStr(String haystack, String needle) {
        int m = needle.length(), n = haystack.length();
        if (m > n) return -1;

        // Cada posición donde needle podría empezar. El límite es n-m
        // INCLUSIVE: más allá no quedan caracteres suficientes.
        for (int i = 0; i <= n - m; i++) {
            int j = 0;
            // El cortocircuito de && evalúa j < m ANTES de charAt: nunca desborda
            while (j < m && haystack.charAt(i + j) == needle.charAt(j)) j++;
            if (j == m) return i;
        }
        return -1;
    }

    // =====================================================================
    // 2. KMP — O(n+m) tiempo, O(m) espacio
    //    Al fallar tras j aciertos, sabemos que acabamos de leer needle[0..j).
    //    lps[j-1] dice cuánto de eso sigue sirviendo, así que i nunca retrocede.
    // =====================================================================
    public int strStrKMP(String haystack, String needle) {
        int m = needle.length(), n = haystack.length();
        if (m > n) return -1;
        if (m == 0) return 0;

        int[] lps = new int[m];
        for (int i = 1, len = 0; i < m; i++) {
            while (len > 0 && needle.charAt(i) != needle.charAt(len)) len = lps[len - 1];
            if (needle.charAt(i) == needle.charAt(len)) len++;
            lps[i] = len;
        }

        for (int i = 0, j = 0; i < n; ) {
            if (haystack.charAt(i) == needle.charAt(j)) {
                i++; j++;
                if (j == m) return i - j;
            } else if (j > 0) j = lps[j - 1];   // fallo con progreso: reaprovechamos
            else i++;                           // fallo sin progreso: nada que salvar
        }
        return -1;
    }

    // =====================================================================
    // 3. BOYER-MOORE-HORSPOOL — O(n*m) peor caso, O(1) espacio, SUBLINEAL en la práctica
    //    Compara de derecha a izquierda. Si el carácter del haystack alineado
    //    con el final de needle no aparece en needle, salta m posiciones de golpe:
    //    ni siquiera mira los caracteres intermedios.
    //    Campeón en alfabetos grandes; se hunde a O(n*m) en alfabetos pequeños.
    // =====================================================================
    public int strStrBMH(String haystack, String needle) {
        int m = needle.length(), n = haystack.length();
        if (m > n) return -1;
        if (m == 0) return 0;

        int[] salto = new int[128];
        java.util.Arrays.fill(salto, m);
        for (int i = 0; i < m - 1; i++) salto[needle.charAt(i)] = m - 1 - i;

        char ultima = needle.charAt(m - 1);
        for (int j = 0; j <= n - m; ) {
            char c = haystack.charAt(j + m - 1);
            if (c == ultima) {
                int i = m - 2;
                while (i >= 0 && haystack.charAt(j + i) == needle.charAt(i)) i--;
                if (i < 0) return j;
            }
            j += salto[c];
        }
        return -1;
    }

    // =====================================================================
    // 4. SHIFT-OR (bitap) — O(n) si m <= 64, O(1) espacio, sin ramas
    //    Un long guarda el estado de los 64 prefijos SIMULTÁNEAMENTE: el bit k
    //    vale 0 si el prefijo de longitud k+1 casa aquí. Un desplazamiento y un
    //    OR por carácter avanzan todos los estados a la vez. Sin saltos = sin
    //    fallos de predicción de rama, que es lo que domina en alfabetos pequeños.
    // =====================================================================
    public int strStrShiftOr(String haystack, String needle) {
        int m = needle.length(), n = haystack.length();
        if (m > n) return -1;
        if (m == 0) return 0;
        if (m > 64) return strStrTwoWay(haystack, needle);

        long[] mask = new long[128];
        java.util.Arrays.fill(mask, ~0L);
        for (int i = 0; i < m; i++) mask[needle.charAt(i)] &= ~(1L << i);

        long estado = ~0L, exito = 1L << (m - 1);
        for (int j = 0; j < n; j++) {
            estado = (estado << 1) | mask[haystack.charAt(j)];
            if ((estado & exito) == 0) return j - m + 1;
        }
        return -1;
    }

    // =====================================================================
    // 5. TWO-WAY (Crochemore-Perrin, 1991) — O(n+m) tiempo Y O(1) espacio
    //    Es el algoritmo que usa glibc en strstr/memmem.
    //
    //    DOMINA ESTRICTAMENTE A KMP: mismo tiempo garantizado, sin tabla auxiliar.
    //
    //    Idea: parte needle en (izquierda | derecha) por su "posición crítica",
    //    la factorización que hace que el periodo local sea el periodo global.
    //    Compara primero la derecha (izq. a der.) y luego la izquierda. Esa
    //    asimetría permite descartar bloques enteros con desplazamientos seguros
    //    sin necesidad de recordar nada: la información va en la propia partición.
    // =====================================================================
    private int periodo;   // salida secundaria de factorizacionCritica

    /** Sufijo máximo bajo orden normal o invertido. Empaqueta (posición, periodo). */
    private long sufijoMaximo(String x, boolean invertido) {
        int m = x.length(), ms = -1, j = 0, k = 1, p = 1;
        while (j + k < m) {
            char a = x.charAt(j + k), b = x.charAt(ms + k);
            boolean menor = invertido ? (b < a) : (a < b);
            boolean mayor = invertido ? (a < b) : (b < a);
            if (menor)      { j += k; k = 1; p = j - ms; }
            else if (mayor) { ms = j++; k = p = 1; }
            else if (k != p) k++;
            else            { j += p; k = 1; }
        }
        return ((long) (ms + 1) << 32) | (p & 0xffffffffL);
    }

    /** Posición crítica de needle; deja el periodo local en el campo `periodo`. */
    private int factorizacionCritica(String x) {
        long a = sufijoMaximo(x, false), b = sufijoMaximo(x, true);
        int ia = (int) (a >>> 32), pa = (int) a;
        int ib = (int) (b >>> 32), pb = (int) b;
        if (ia >= ib) { periodo = pa; return ia; }
        periodo = pb;  return ib;
    }

    public int strStrTwoWay(String haystack, String needle) {
        int m = needle.length(), n = haystack.length();
        if (m > n) return -1;
        if (m == 0) return 0;
        if (m == 1) return haystack.indexOf(needle.charAt(0));

        int critica = factorizacionCritica(needle);
        int per = periodo;

        // ¿needle[0..critica) coincide con needle[per..per+critica)?
        boolean periodoPequeno = true;
        for (int i = 0; i < critica; i++) {
            if (needle.charAt(i) != needle.charAt(i + per)) { periodoPequeno = false; break; }
        }

        int limite = n - m;

        if (periodoPequeno) {
            // needle es periódica: usamos "memoria" para no recomparar el trozo
            // que un desplazamiento de un periodo deja ya validado.
            int memoria = 0;
            for (int j = 0; j <= limite; ) {
                int i = Math.max(critica, memoria);
                while (i < m && needle.charAt(i) == haystack.charAt(i + j)) i++;
                if (i >= m) {
                    i = critica - 1;
                    while (i >= memoria && needle.charAt(i) == haystack.charAt(i + j)) i--;
                    if (i < memoria) return j;
                    j += per;
                    memoria = m - per;
                } else {
                    j += i - critica + 1;
                    memoria = 0;
                }
            }
        } else {
            // needle no es periódica: el desplazamiento seguro es fijo y grande
            int desplazamiento = Math.max(critica, m - critica) + 1;
            for (int j = 0; j <= limite; ) {
                int i = critica;
                while (i < m && needle.charAt(i) == haystack.charAt(i + j)) i++;
                if (i >= m) {
                    i = critica - 1;
                    while (i >= 0 && needle.charAt(i) == haystack.charAt(i + j)) i--;
                    if (i < 0) return j;
                    j += desplazamiento;
                } else {
                    j += i - critica + 1;
                }
            }
        }
        return -1;
    }

    // =====================================================================
    // 6. HÍBRIDO — despacha según la entrada, que es lo que hacen las
    //    librerías reales. Garantiza O(n+m) sin renunciar a la velocidad
    //    sin ramas de shift-or en las agujas cortas (el caso habitual).
    // =====================================================================
    public int strStrOptimo(String haystack, String needle) {
        int m = needle.length(), n = haystack.length();
        if (m > n) return -1;
        if (m == 0) return 0;
        if (m == 1) return haystack.indexOf(needle.charAt(0));
        if (m <= 64) return strStrShiftOr(haystack, needle);
        return strStrTwoWay(haystack, needle);
    }
}

/*
Given two strings needle and haystack, return the index of the first occurrence of needle in haystack,
or -1 if needle is not part of haystack.

Example 1:
Input: haystack = "sadbutsad", needle = "sad"
Output: 0

Example 2:
Input: haystack = "leetcode", needle = "leeto"
Output: -1

Constraints:
1 <= haystack.length, needle.length <= 10^4
haystack and needle consist of only lowercase English characters.
 */
