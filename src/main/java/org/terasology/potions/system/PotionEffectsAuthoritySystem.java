/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.potions.system;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.potions.component.PotionEffect;
import org.terasology.potions.component.PotionEffectsListComponent;
import org.terasology.registry.In;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * This authority system manages the duration updates of every potion effect modifier in every entity.
 */
@RegisterSystem(value = RegisterMode.AUTHORITY)
public class PotionEffectsAuthoritySystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    /** Integer storing when to check each effect. */
    private static final int CHECK_INTERVAL = 100;

    /** Last time the list of regen effects were checked. */
    private long lastUpdated;

    @In
    private Time time;
    @In
    private EntityManager entityManager;
    @In
    private PrefabManager prefabManager;

    /**
     * For every update, check to see if the time's been over the CHECK_INTERVAL. If so, subtract the delta time from
     * the remaining duration of each potion effect modifier.
     *
     * @param delta The time (in seconds) since the last engine update.
     */
    @Override
    public void update(float delta) {
        final long currentTime = time.getGameTimeInMs();

        // If the current time passes the CHECK_INTERVAL threshold, continue.
        if (currentTime >= lastUpdated + CHECK_INTERVAL) {
            lastUpdated = currentTime;

            // Iterate through all of the entities that have potions-based effects, and reduce the duration remaining
            // on each (as long as they have a finite amount of time).
            for (EntityRef entity : entityManager.getEntitiesWith(PotionEffectsListComponent.class)) {
                // Get the list of potion effects applied to this entity.
                final PotionEffectsListComponent effectsList = entity.getComponent(PotionEffectsListComponent.class);

                // Search through each type of potion-based AlterationEffects.
                Iterator<Entry<String, PotionEffect>> effectIter = effectsList.effects.entrySet().iterator();
                while (effectIter.hasNext()) {
                    Entry<String, PotionEffect> effect = effectIter.next();

                    effect.getValue().duration -= CHECK_INTERVAL;

                    // If this effect has no remaining time, remove it from the potion effects map.
                    if (effect.getValue().duration <= 0) {
                        effectIter.remove();
                    }
                }
            }
        }
    }
}
