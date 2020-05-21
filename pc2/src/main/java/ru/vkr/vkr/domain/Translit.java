package ru.vkr.vkr.domain;

public class Translit {
    private static String cyr2lat(char ch){
        switch (ch){
            case 'А': return "A";
            case 'Б': return "B";
            case 'В': return "V";
            case 'Г': return "G";
            case 'Д': return "D";
            case 'Е': return "E";
            case 'Ё': return "JE";
            case 'Ж': return "ZH";
            case 'З': return "Z";
            case 'И': return "I";
            case 'Й': return "Y";
            case 'К': return "K";
            case 'Л': return "L";
            case 'М': return "M";
            case 'Н': return "N";
            case 'О': return "O";
            case 'П': return "P";
            case 'Р': return "R";
            case 'С': return "S";
            case 'Т': return "T";
            case 'У': return "U";
            case 'Ф': return "F";
            case 'Х': return "KH";
            case 'Ц': return "C";
            case 'Ч': return "CH";
            case 'Ш': return "SH";
            case 'Щ': return "JSH";
            case 'Ъ': return "HH";
            case 'Ы': return "IH";
            case 'Ь': return "JH";
            case 'Э': return "EH";
            case 'Ю': return "JU";
            case 'Я': return "JA";
            default: return String.valueOf(ch);
        }
    }

    public static String fio2login(String surname, String name, String middlename){
        int len = surname.length() + name.length() + middlename.length();
        surname = surname.toUpperCase();
        name = name.toUpperCase();
        middlename = middlename.toUpperCase();

        StringBuilder sb = new StringBuilder(len*2);
        boolean firstCh = true;
        for(char ch : surname.toCharArray()){
            if (firstCh) {      //Первый символ должен быть большим
                String s = cyr2lat(ch);
                sb.append(s.substring(0, 1) + s.substring(1).toLowerCase());
            } else {
                sb.append(cyr2lat(ch).toLowerCase());
            }
            firstCh = false;
        }

        sb.append(cyr2lat(name.charAt(0)) + cyr2lat(middlename.charAt(0)));

        return sb.toString();
    }
}
