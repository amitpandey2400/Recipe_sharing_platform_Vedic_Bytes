package platform.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

public class Recipe implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String title;
    private String description;
    private String authorUsername;
    private List<String> ingredients;
    private List<String> steps;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private Set<String> likes; // usernames
    private List<Comment> comments;

    public Recipe(int id, String title, String description, String authorUsername,
                  List<String> ingredients, List<String> steps, List<String> tags, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.authorUsername = authorUsername;
        this.ingredients = new ArrayList<>(ingredients);
        this.steps = new ArrayList<>(steps);
        this.tags = new ArrayList<>(tags);
        this.createdAt = createdAt;
        this.lastUpdated = null;
        this.likes = new HashSet<>();
        this.comments = new ArrayList<>();
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAuthorUsername() { return authorUsername; }
    public List<String> getIngredients() { return ingredients; }
    public List<String> getSteps() { return steps; }
    public List<String> getTags() { return tags; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public Set<String> getLikes() { return likes; }
    public List<Comment> getComments() { return comments; }

    public void setTitle(String t) { this.title = t; }
    public void setDescription(String d) { this.description = d; }
    public void setIngredients(List<String> ing) { this.ingredients = new ArrayList<>(ing); }
    public void setSteps(List<String> s) { this.steps = new ArrayList<>(s); }
    public void setTags(List<String> t) { this.tags = new ArrayList<>(t); }
    public void setLastUpdated(LocalDateTime t) { this.lastUpdated = t; }

    public boolean toggleLike(String username) {
        if (likes.contains(username.toLowerCase())) {
            likes.remove(username.toLowerCase());
            return false;
        } else {
            likes.add(username.toLowerCase());
            return true;
        }
    }
}
