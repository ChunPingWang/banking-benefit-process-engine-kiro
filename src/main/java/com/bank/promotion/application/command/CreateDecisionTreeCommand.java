package com.bank.promotion.application.command;

import java.util.Objects;

/**
 * 創建決策樹命令
 */
public final class CreateDecisionTreeCommand {
    
    private final String name;
    private final String description;
    
    public CreateDecisionTreeCommand(String name, String description) {
        this.name = validateName(name);
        this.description = description;
    }
    
    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Decision tree name cannot be null or empty");
        }
        return name.trim();
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateDecisionTreeCommand that = (CreateDecisionTreeCommand) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(description, that.description);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, description);
    }
    
    @Override
    public String toString() {
        return "CreateDecisionTreeCommand{" +
               "name='" + name + '\'' +
               ", description='" + description + '\'' +
               '}';
    }
}