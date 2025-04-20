import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class ZookeeperChallenge {

    private static final String NAME_SECTION_SUFFIX = " Names:";
    private static final int MIN_PARTS = 8;
    private static final int DEFAULT_MAX_DAYS = 28;

    private static final Map<String, Habitat> habitats = new HashMap<>();
    private static final Map<String, List<String>> animalNameMap = new HashMap<>();
    private static final Map<String, Integer> speciesCount = new HashMap<>();
    private static final List<Animal> allAnimals = new ArrayList<>();

    public static void main(String[] args) {
        loadAnimalNames("animalNames.txt");
        processArrivingAnimals("arrivingAnimals.txt");
        assignNames();
        generateUniqueIDs();
        organizeIntoHabitats();
        generateZooReport("zooPopulation.txt");

        System.out.println("Zoo population report generated successfully in zooPopulation.txt");
    }

    private static void loadAnimalNames(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            String currentSpecies = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.endsWith(NAME_SECTION_SUFFIX)) {
                    currentSpecies = line.substring(0, line.length() - NAME_SECTION_SUFFIX.length()).trim();
                    animalNameMap.put(currentSpecies, new ArrayList<>());
                } else if (currentSpecies != null && !line.isEmpty()) {
                    String[] names = line.split(",");
                    List<String> nameList = animalNameMap.get(currentSpecies);
                    for (String name : names) {
                        nameList.add(name.trim());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading animal names: " + e.getMessage());
        }
    }

    private static void processArrivingAnimals(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= MIN_PARTS) {
                    try {
                        String species = parts[0].trim();
                        int age = Integer.parseInt(parts[1].trim());
                        String sex = parts[2].trim();
                        String color = parts[3].trim();
                        double weight = Double.parseDouble(parts[4].trim());

                        StringBuilder originBuilder = new StringBuilder(parts[5].trim());
                        for (int i = 6; i < parts.length - 2; i++) {
                            originBuilder.append(",").append(parts[i].trim());
                        }
                        String origin = originBuilder.toString().replaceAll("^\"|\"$", "");

                        LocalDate arrivalDate = LocalDate.parse(parts[parts.length - 2].trim(), dateFormatter);
                        String birthSeason = parts[parts.length - 1].trim();

                        Animal animal = new Animal(species, age, sex, color, weight, origin, arrivalDate, birthSeason);
                        allAnimals.add(animal);
                        speciesCount.put(species, speciesCount.getOrDefault(species, 0) + 1);
                    } catch (NumberFormatException | DateTimeParseException e) {
                        System.err.println("Error parsing line: " + line + " - " + e.getMessage());
                    }
                } else {
                    System.err.println("Skipping invalid animal data line: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error processing arriving animals: " + e.getMessage());
        }
    }

    private static void assignNames() {
        Map<String, Integer> nameIndex = new HashMap<>();
        for (Animal animal : allAnimals) {
            String species = animal.getSpecies();
            List<String> names = animalNameMap.getOrDefault(species, new ArrayList<>());
            int currentIndex = nameIndex.getOrDefault(species, 0);
            if (currentIndex < names.size()) {
                animal.setName(names.get(currentIndex));
            } else {
                animal.setName(species + " #" + (currentIndex + 1 - names.size()));
            }
            nameIndex.put(species, currentIndex + 1);
        }
    }

    private static void generateUniqueIDs() {
        Map<String, Integer> currentCount = new HashMap<>();
        for (Animal animal : allAnimals) {
            String species = animal.getSpecies();
            String speciesCode = species.substring(0, Math.min(2, species.length())).toUpperCase();
            int count = currentCount.getOrDefault(species, 0) + 1;
            animal.setUniqueID(String.format("%s%02d", speciesCode, count));
            currentCount.put(species, count);
        }
    }

    private static void organizeIntoHabitats() {
        for (Animal animal : allAnimals) {
            String species = animal.getSpecies();
            habitats.computeIfAbsent(species, Habitat::new).addAnimal(animal);
        }
    }

    private static void generateZooReport(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Habitat habitat : habitats.values()) {
                writer.write(habitat.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing zoo report: " + e.getMessage());
        }
    }
}