package citynetwork;

public class Utils {

    public static boolean isValidCode(String _cityName, String _cityCode) {
        int codeIdx = 0, cityIdx = 0;

        // check if code is 3 long
        if (_cityCode.length() != 3) {
            return false;
        }

        // turn city name into stringbuilder for later
        _cityName.replace(" ", "");
        StringBuilder cityNameBuilder = new StringBuilder(_cityName);
        char[] codeChars = _cityCode.toCharArray();

        // if code has a space, return false, (Character.isAlphabethic doesnt catch this??)
        if (_cityCode.contains(" ")) {
            return false;
        }

        // checks if there's junk chars in code
        for (char c: codeChars) {
            if (!Character.isAlphabetic(c)) {
                return false;
            }
        }

        //checks if all chars in code are present in name
        if (_cityName.indexOf(codeChars[0]) == -1 || _cityName.indexOf(codeChars[1]) == -1 || _cityName.indexOf(codeChars[2]) == -1){
            return false;
        }

        // gets rid of everything before the first instance of the first char of the code
        cityNameBuilder.delete(0, cityNameBuilder.indexOf(codeChars[0] + ""));
        if (cityNameBuilder.indexOf(codeChars[0] + "") > cityNameBuilder.indexOf(codeChars[1] + "") + 1) {
            return false;
        } else {
            // deletes everything up to and including the first char
            // this insures LLN fails London, since the L can only be used once
            cityNameBuilder.delete(0, cityNameBuilder.indexOf(codeChars[0] + "") + 1);
        }

        // checks if 2nd char is still in the name
        // this ensures the city name has the code in the
        // correct order
        if (cityNameBuilder.indexOf(codeChars[1] + "") == -1) {
            return false;
        }


        // same story as before, but instead of 0 and 1 index, it's for 1 and 2 index
        if (cityNameBuilder.indexOf(codeChars[1] + "") > cityNameBuilder.indexOf(codeChars[2] + "")) {
            return false;
        } else {
            cityNameBuilder.delete(0, cityNameBuilder.indexOf(codeChars[1] + ""));
        }

        // checks if the last char in the code is still in the city name
        if (cityNameBuilder.indexOf(codeChars[2] + "") == -1) {
            return false;
        }
        return true;
    }

}
