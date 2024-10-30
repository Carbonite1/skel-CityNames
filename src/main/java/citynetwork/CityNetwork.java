package citynetwork;

import java.util.*;

public class CityNetwork {
    public final List<CityTLA> _cities = new ArrayList<>();

    public boolean checkRep() {
        for (int i = 0; i < _cities.size(); i++) {
            CityTLA city1 = _cities.get(i);
            if (!city1.checkRep()) {
                return false;
            }
            for (int j = i + 1; j < _cities.size(); j++) {
                CityTLA city2 = _cities.get(j);
                // Check if city2's code is a valid TLA for city1's name
                if (Utils.isValidCode(city1._cityName, city2._cityCode)) {
                    return false;
                }
                // Check if city1's code is a valid TLA for city2's name
                if (Utils.isValidCode(city2._cityName, city1._cityCode)) {
                    return false;
                }
                // Check if the codes are the same
                if (city1._cityCode.equals(city2._cityCode)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Builds a CityNetwork with the maximum number of cities from cityNames that can have ambiguity-free TLAs.
     *
     * @param cityNames is not null and only contains strings with upper-case alphabets and spaces
     * @return a {@code CityNetwork} instance with the maximum number of cities from {@code cityNames} that can have ambiguity-free TLAs
     */
    public static final CityNetwork buildNetwork(List<String> cityNames) {
        CityNetwork network = new CityNetwork();
        // Map from city name to set of valid TLAs
        Map<String, Set<String>> cityToTLAs = new HashMap<>();
        // Map from TLA to set of cities for which it is a valid TLA
        Map<String, Set<String>> tlaToCities = new HashMap<>();

        // Step 1: Generate all valid TLAs for each city
        for (String cityName : cityNames) {
            Set<String> validTLAs = generateValidTLAs(cityName);
            cityToTLAs.put(cityName, validTLAs);
            for (String tla : validTLAs) {
                tlaToCities.computeIfAbsent(tla, k -> new HashSet<>()).add(cityName);
            }
        }

        // Assigned TLAs and cities
        Map<String, String> assignedTLAs = new HashMap<>(); // cityName -> TLA
        Set<String> usedTLAs = new HashSet<>();

        // Step 2: Assign TLAs that are unique to cities
        boolean progress = true;
        while (progress) {
            progress = false;
            List<String> citiesToRemove = new ArrayList<>();
            for (String city : cityToTLAs.keySet()) {
                Set<String> possibleTLAs = new HashSet<>(cityToTLAs.get(city));
                // Remove TLAs that are already used
                possibleTLAs.removeAll(usedTLAs);
                // Find TLAs that are unique to this city
                Set<String> uniqueTLAs = new HashSet<>();
                for (String tla : possibleTLAs) {
                    if (tlaToCities.get(tla).size() == 1) {
                        uniqueTLAs.add(tla);
                    }
                }
                if (!uniqueTLAs.isEmpty()) {
                    // Assign any unique TLA
                    String assignedTLA = uniqueTLAs.iterator().next();
                    assignedTLAs.put(city, assignedTLA);
                    usedTLAs.add(assignedTLA);
                    // Remove city from consideration
                    citiesToRemove.add(city);
                    progress = true;
                }
            }
            // Remove assigned cities from cityToTLAs
            for (String city : citiesToRemove) {
                cityToTLAs.remove(city);
            }
        }

        // Step 3: Attempt to assign remaining cities to maximize the number of cities included
        List<String> remainingCities = new ArrayList<>(cityToTLAs.keySet());
        Map<String, String> bestAssignment = new HashMap<>(assignedTLAs);
        maximizeAssignments(remainingCities, cityToTLAs, assignedTLAs, usedTLAs, bestAssignment, cityToTLAs);

        // Build the CityNetwork with the best assignment
        for (Map.Entry<String, String> entry : bestAssignment.entrySet()) {
            CityTLA cityTLA = new CityTLA(entry.getKey(), entry.getValue());
            network._cities.add(cityTLA);
        }
        return network;
    }

    // Recursive method to maximize the number of cities included
    private static void maximizeAssignments(
            List<String> cities,
            Map<String, Set<String>> cityToTLAs,
            Map<String, String> assignedTLAs,
            Set<String> usedTLAs,
            Map<String, String> bestAssignment,
            Map<String, Set<String>> allCityToTLAs
    ) {
        if (cities.isEmpty()) {
            if (assignedTLAs.size() > bestAssignment.size()) {
                bestAssignment.clear();
                bestAssignment.putAll(assignedTLAs);
            }
            return;
        }

        String city = cities.get(0);
        List<String> remainingCities = new ArrayList<>(cities);
        remainingCities.remove(0);

        Set<String> possibleTLAs = new HashSet<>(cityToTLAs.get(city));
        // Remove TLAs that are already used
        possibleTLAs.removeAll(usedTLAs);

        // Try each possible TLA
        for (String tla : possibleTLAs) {
            // Check if TLA is valid for any other city (assigned or unassigned), excluding the current city
            boolean validForOthers = false;
            for (Map.Entry<String, Set<String>> entry : allCityToTLAs.entrySet()) {
                String otherCity = entry.getKey();
                if (!otherCity.equals(city) && entry.getValue().contains(tla)) {
                    validForOthers = true;
                    break;
                }
            }
            if (!validForOthers) {
                // Assign TLA to city
                assignedTLAs.put(city, tla);
                usedTLAs.add(tla);
                maximizeAssignments(remainingCities, cityToTLAs, assignedTLAs, usedTLAs, bestAssignment, allCityToTLAs);
                // Backtrack
                assignedTLAs.remove(city);
                usedTLAs.remove(tla);
            }
        }

        // Try not assigning this city to possibly get a better assignment
        maximizeAssignments(remainingCities, cityToTLAs, assignedTLAs, usedTLAs, bestAssignment, allCityToTLAs);
    }

    // Method to generate all valid TLAs for a given city name
    private static Set<String> generateValidTLAs(String cityName) {
        Set<String> result = new HashSet<>();
        String name = cityName.replaceAll(" ", "");
        generateTLAsHelper(name, 0, new StringBuilder(), result);
        return result;
    }

    // Helper method for recursive generation of TLAs
    private static void generateTLAsHelper(String cityName, int index, StringBuilder current, Set<String> result) {
        if (current.length() == 3) {
            result.add(current.toString());
            return;
        }
        if (index >= cityName.length()) {
            return;
        }
        // Include current character
        current.append(cityName.charAt(index));
        generateTLAsHelper(cityName, index + 1, current, result);
        current.deleteCharAt(current.length() - 1);
        // Exclude current character
        generateTLAsHelper(cityName, index + 1, current, result);
    }
}
