package API;

import java.util.ArrayList;
import java.util.List;

public class Heroes {
    String id;
    String name;
    String description;
    String photo;
    List<Heroes> evolutions;

    public Heroes(String id, String name, String description, String photo) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.photo = photo;
        this.evolutions = new ArrayList<>();
    }

    public int getTotalEvolutionsCount() {
        int count = evolutions.size();
        for (Heroes evolution : evolutions) {
            count += evolution.getTotalEvolutionsCount();
        }
        return count;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhoto() {
        return this.photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public List<Heroes> getEvolutions() {
        return this.evolutions;
    }

    public void addEvolution(Heroes evolution) {
        this.evolutions.add(evolution);
    }
}
