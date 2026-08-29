package ejercicios.Hard.String;

/**
 * LeetCode 10 — Regular Expression Matching.
 *
 *   método        tiempo     espacio
 *   ---------------------------------
 *   isMatch       O(n + m)   O(1)      <- simulación NFA con máscaras de bits
 *   isMatchDP     O(n * m)   O(m)      <- programación dinámica clásica
 *
 * Ambos verificados contra un matcher recursivo de referencia sobre todas las
 * combinaciones de s hasta longitud 8 y patrones válidos hasta longitud 6.
 */
public class RegularExpressionMatching {

    // =====================================================================
    // SOLUCIÓN PRINCIPAL — autómata (NFA) simulado con un solo int
    //
    // El patrón se parsea en tokens: cada token es un carácter con o sin '*'.
    // "c*a*b" -> [c*, a*, b]. Con k tokens hay k+1 estados: el estado i
    // significa "ya consumí los tokens 0..i-1"; el estado k es aceptación.
    //
    // Como m <= 20, TODO el conjunto de estados activos cabe en los bits de
    // un int. En vez de recorrer estados uno a uno, avanzamos todos a la vez
    // con operaciones de bits: de ahí O(1) espacio y ~11 ops por carácter.
    // =====================================================================
    public boolean isMatch(String s, String p) {
        int m = p.length();

        // --- 1. Parsear el patrón en tokens ---
        int[] accept = new int[26];  // accept[c] = tokens que casan con la letra c
        int dotMask = 0;             // tokens '.' (casan con cualquier letra)
        int starMask = 0;            // tokens que llevan '*'
        int k = 0;                   // número de tokens

        for (int i = 0; i < m; i++) {
            char c = p.charAt(i);
            if (c == '.') dotMask |= 1 << k;
            else accept[c - 'a'] |= 1 << k;

            if (i + 1 < m && p.charAt(i + 1) == '*') {
                starMask |= 1 << k;
                i++;                 // saltar el '*', ya está absorbido en el token
            }
            k++;
        }

        // --- 2. Máscaras del cierre epsilon, por prefijo paralelo ---
        // Un token con '*' admite cero repeticiones: estado i -> i+1 sin consumir
        // nada. Esos saltos encadenan ("a*b*c*" salta del 0 al 3 de golpe), así que
        // el cierre debe propagarse por toda la racha de estados con '*'.
        // Estas 5 máscaras convierten esa propagación en 5 operaciones fijas:
        // m2 marca rachas de 2 estados con '*', m4 de 4, m8 de 8, m16 de 16.
        int m1 = starMask;
        int m2 = m1 & (m1 >>> 1);
        int m4 = m2 & (m2 >>> 2);
        int m8 = m4 & (m4 >>> 4);
        int m16 = m8 & (m8 >>> 8);

        // --- 3. Estado inicial: token 0, más todo lo alcanzable saltando '*' ---
        int state = 1;
        state |= (state & m1) << 1;
        state |= (state & m2) << 2;
        state |= (state & m4) << 4;
        state |= (state & m8) << 8;
        state |= (state & m16) << 16;

        // --- 4. Consumir s carácter a carácter ---
        int aceptacion = 1 << k;
        for (int i = 0, n = s.length(); i < n; i++) {

            // Estados activos que aceptan este carácter
            int hit = state & (accept[s.charAt(i) - 'a'] | dotMask);

            // Token con '*': consume y SE QUEDA (puede repetir).
            // Token normal: consume y AVANZA al siguiente estado.
            state = (hit & starMask) | ((hit & ~starMask) << 1);

            if (state == 0) return false;   // ningún estado vivo: imposible casar

            state |= (state & m1) << 1;
            state |= (state & m2) << 2;
            state |= (state & m4) << 4;
            state |= (state & m8) << 8;
            state |= (state & m16) << 16;
        }

        return (state & aceptacion) != 0;
    }

    // =====================================================================
    // DP clásica — O(n*m) tiempo, O(m) espacio (una sola fila rodante).
    // Es la que conviene explicar en una entrevista: más fácil de defender.
    //
    // dp[j] = ¿casa s[0..i) con p[0..j)?
    // =====================================================================
    public boolean isMatchDP(String s, String p) {
        int n = s.length(), m = p.length();
        boolean[] dp = new boolean[m + 1];

        dp[0] = true;
        // Fila i=0: qué prefijos del patrón casan con la cadena vacía (solo "x*y*...")
        for (int j = 2; j <= m; j++) {
            if (p.charAt(j - 1) == '*') dp[j] = dp[j - 2];
        }

        for (int i = 1; i <= n; i++) {
            boolean diagonal = dp[0];   // dp[i-1][j-1]
            dp[0] = false;              // s no vacío nunca casa con patrón vacío
            char sc = s.charAt(i - 1);

            for (int j = 1; j <= m; j++) {
                boolean arriba = dp[j];  // dp[i-1][j], antes de sobrescribirlo
                char pc = p.charAt(j - 1);

                if (pc == '*') {
                    char prev = p.charAt(j - 2);
                    // cero repeticiones -> dp[i][j-2]
                    // una o más      -> dp[i-1][j], si el carácter casa
                    dp[j] = dp[j - 2] || (arriba && (prev == '.' || prev == sc));
                } else {
                    dp[j] = diagonal && (pc == '.' || pc == sc);
                }
                diagonal = arriba;
            }
        }
        return dp[m];
    }
}
