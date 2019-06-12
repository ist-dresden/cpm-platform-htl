package com.composum.platform.models.htl.tools;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.sling.scripting.sightly.pojo.Use;

import javax.script.Bindings;
import java.util.Arrays;

/**
 * Utility to print out information about a HTL variable via Java Reflection. Not for production use.
 * Usage e.g.:
 */
public class ObjectInfo implements Use {
    private Object object;

    @Override
    public void init(Bindings bindings) {
        this.object = bindings.get("object");
    }

    public String getInfo() {
        return toString();
    }

    @Override
    public String toString() {
        if (null == object) return "<null>";
        StringBuilder buf = new StringBuilder();
        buf.append("Class: ").append(object.getClass().getName()).append("\n");
        Class superclass = object.getClass();
        while (null != (superclass = superclass.getSuperclass())) {
            buf.append("Interfaces: ").append(Arrays.asList(superclass.getInterfaces())).append("\n");
            buf.append("Superclass: ").append(superclass.getName()).append("\n");
        }
        buf.append("toString(): ").append(object).append("\n");
        buf.append(ReflectionToStringBuilder.toString(object, ToStringStyle.MULTI_LINE_STYLE, true));
        return buf.toString();
    }
}
