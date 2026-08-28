package ejercicios.Easy.String;

class Solution {
    public String longestCommonPrefix(String[] strs) {
        if (strs == null || strs.length == 0) return "";

        // Creamos un objeto que este ubicada en la primera palabra
        String palabraRepetida = strs[0];

        // Se crea la lógica del programa
        for (int i = 1; i < strs.length; i++) {

            // Se identifican todos los index, y si no coinciden se le resta el último hasta que funcione
            while (strs[i].indexOf(palabraRepetida) != 0) {
                palabraRepetida = palabraRepetida.substring(0, palabraRepetida.length() - 1);

                if (palabraRepetida.isEmpty()) return "";
            }
        }
        return palabraRepetida;
    }
} 
