package com.tucreador.turrets.ecs;

import java.util.*;

public class ECSRegistry {
    private int nextEntityId = 0;
    private final Set<Integer> activeEntities = new HashSet<>();
    private final Map<Class<?>, Map<Integer, Object>> componentStores = new HashMap<>();

    public int createEntity() {
        int id = nextEntityId++;
        activeEntities.add(id);
        return id;
    }

    public void destroyEntity(int entityId) {
        activeEntities.remove(entityId);
        for (Map<Integer, Object> store : componentStores.values()) {
            store.remove(entityId);
        }
    }

    public <T> void addComponent(int entityId, T component) {
        componentStores.computeIfAbsent(component.getClass(), k -> new HashMap<>()).put(entityId, component);
    }

    @SuppressWarnings("unchecked")
    public <T> T getComponent(int entityId, Class<T> componentClass) {
        Map<Integer, Object> store = componentStores.get(componentClass);
        if (store == null) return null;
        return (T) store.get(entityId);
    }

    public Set<Integer> getActiveEntities() {
        return Collections.unmodifiableSet(activeEntities);
    }
}
