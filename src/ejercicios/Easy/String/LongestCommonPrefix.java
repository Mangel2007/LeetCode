package ejercicios.Easy.String;

public class LongestCommonPrefix {
        public String longestCommonPrefix(String[] strs) {

            // Lógica para saber que haya algo adentro
            if (strs == null || strs.length == 0){
                return("");
            }

            // Tomar la primera palabra de referencia
            String referencia = strs[0];
            String palabrasRepetidas = "";

            // Lógica para determinar si palabras se repiten
            for(int i = 0; i < referencia.length(); i++){
                char c = referencia.charAt(i);

                for(int j = 1; j < strs.length; j++){
                    if(i == strs[j].length() || strs[j].charAt(i) != c){
                        return palabrasRepetidas;
                    }
                }
                palabrasRepetidas = palabrasRepetidas + c;
            }
            return palabrasRepetidas;
    }
}

