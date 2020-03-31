package com.composum.platform.htl.impl;

import org.apache.sling.scripting.api.BindingsValuesProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Component;

import javax.script.Bindings;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A {@link org.apache.sling.scripting.api.BindingsValuesProvider} that binds the outermost bindings themselves into a
 * binding, such that global variables can be added / changed.
 *
 * @author Hans-Peter Stoerr
 * @since 09/2017
 */
@Component(
        property = {
                "javax.script.name=sightly"
        }
)
public class OuterBindingsBindingsValueProvider implements BindingsValuesProvider {

    /**
     * A binding that references the {@link javax.script.Bindings} on the outermost level.
     */
    public static final String OUTER_BINDINGS = "outerBindings";


    @Override
    public void addBindings(Bindings bindings) {
        if (!bindings.containsKey(OUTER_BINDINGS) && bindings != null) {
            bindings.put(OUTER_BINDINGS, new BindingsWrapper(bindings));
        }
    }

    /**
     * Avoids a bug in sling.api:
     */
    protected class BindingsWrapper implements Bindings {

        protected final Bindings bindings;

        public BindingsWrapper(Bindings bindings) {
            this.bindings = bindings;
        }

        /**
         * This is the workaround for https://issues.apache.org/jira/browse/SLING-9312 :
         * the hashcode of the actual binding values is calculated if we don't break
         * this here. This is strictly wrong, but is only accidentially used in the Sling implementation.
         */
        @Override
        public int hashCode() {
            return bindings.keySet().hashCode();
        }

        // The rest is just delegates to bindings.

        @Override
        public Object put(String name, Object value) {
            return bindings.put(name, value);
        }

        @Override
        public void putAll(Map<? extends String, ?> toMerge) {
            bindings.putAll(toMerge);
        }

        @Override
        public boolean containsKey(Object key) {
            return bindings.containsKey(key);
        }

        @Override
        public Object get(Object key) {
            return bindings.get(key);
        }

        @Override
        public Object remove(Object key) {
            return bindings.remove(key);
        }

        @Override
        public int size() {
            return bindings.size();
        }

        @Override
        public boolean isEmpty() {
            return bindings.isEmpty();
        }

        @Override
        public boolean containsValue(Object value) {
            return bindings.containsValue(value);
        }

        @Override
        public void clear() {
            bindings.clear();
        }

        @NotNull
        @Override
        public Set<String> keySet() {
            return bindings.keySet();
        }

        @NotNull
        @Override
        public Collection<Object> values() {
            return bindings.values();
        }

        @NotNull
        @Override
        public Set<Entry<String, Object>> entrySet() {
            return bindings.entrySet();
        }

        @Override
        public boolean equals(Object o) {
            return bindings.equals(o);
        }

        @Override
        public Object getOrDefault(Object key, Object defaultValue) {
            return bindings.getOrDefault(key, defaultValue);
        }

        @Override
        public void forEach(BiConsumer<? super String, ? super Object> action) {
            bindings.forEach(action);
        }

        @Override
        public void replaceAll(BiFunction<? super String, ? super Object, ?> function) {
            bindings.replaceAll(function);
        }

        @Nullable
        @Override
        public Object putIfAbsent(String key, Object value) {
            return bindings.putIfAbsent(key, value);
        }

        @Override
        public boolean remove(Object key, Object value) {
            return bindings.remove(key, value);
        }

        @Override
        public boolean replace(String key, Object oldValue, Object newValue) {
            return bindings.replace(key, oldValue, newValue);
        }

        @Nullable
        @Override
        public Object replace(String key, Object value) {
            return bindings.replace(key, value);
        }

        @Override
        public Object computeIfAbsent(String key, Function<? super String, ?> mappingFunction) {
            return bindings.computeIfAbsent(key, mappingFunction);
        }

        @Override
        public Object computeIfPresent(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
            return bindings.computeIfPresent(key, remappingFunction);
        }

        @Override
        public Object compute(String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
            return bindings.compute(key, remappingFunction);
        }

        @Override
        public Object merge(String key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
            return bindings.merge(key, value, remappingFunction);
        }

    }
}
