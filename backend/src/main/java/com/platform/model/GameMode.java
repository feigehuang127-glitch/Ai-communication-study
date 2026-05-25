package com.platform.model;

import jakarta.persistence.*;

@Entity
@Table(name = "game_modes")
public class GameMode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "JSON")
    private String rules;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRules() { return rules; }
    public void setRules(String rules) { this.rules = rules; }
}
