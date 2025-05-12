import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;


public class Main {


    public static boolean checkCapacity(int maxCapacity, List<Map<String, String>> guests) {

        TreeMap<LocalDate, Integer> events = new TreeMap<>();

        for (Map<String, String> guest : guests) {
            LocalDate checkIn = LocalDate.parse(guest.get("check-in"));
            LocalDate checkOut = LocalDate.parse(guest.get("check-out"));
            events.put(checkIn, events.getOrDefault(checkIn, 0) + 1);
            events.put(checkOut, events.getOrDefault(checkOut, 0) - 1);
        }

        int currentGuests = 0;
        for (Map.Entry<LocalDate, Integer> entry : events.entrySet()) {
            currentGuests += entry.getValue();
            if (currentGuests > maxCapacity) {
                return false;
            }
        }

        return true;
    }


    private static Map<String, String> parseJsonToMap(String json) {
        Map<String, String> map = new HashMap<>();
        json = json.substring(1, json.length() - 1);

        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            String key = keyValue[0].trim().replace("\"", "");
            String value = keyValue[1].trim().replace("\"", "");
            map.put(key, value);
        }

        return map;
    }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int maxCapacity = Integer.parseInt(scanner.nextLine());

        int n = Integer.parseInt(scanner.nextLine());


        List<Map<String, String>> guests = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            String jsonGuest = scanner.nextLine();
            // Простой парсер JSON строки в Map
            Map<String, String> guest = parseJsonToMap(jsonGuest);
            guests.add(guest);
        }

        boolean result = checkCapacity(maxCapacity, guests);
        System.out.println(result ? "True" : "False");
        scanner.close();
    }
}