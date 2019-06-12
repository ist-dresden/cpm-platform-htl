package com.composum.platform.models.htl.tools;

import org.apache.sling.scripting.sightly.pojo.Use;

import javax.script.Bindings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Development-tool to list out all bindings in the current context. Not for production use.
 * Usage e.g.:
 */
public class BindingsHelper implements Use {

    private Bindings bindings;

    @Override
    public void init(Bindings bindings) {
        this.bindings = bindings;
    }

    public Bindings getBindings() {
        return bindings;
    }

    public List<String> getKeys() {
        ArrayList<String> keys = new ArrayList<>(bindings.keySet());
        Collections.sort(keys);
        return keys;
    }

    public String getInfo() {
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, Object> entry : bindings.entrySet()) {
            buf.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
        }
        return buf.toString();
    }

    @Override
    public String toString() {
        return bindings.toString();
    }

}
