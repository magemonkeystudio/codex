package com.promcteam.risecore.legacy.util.message;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Represent placeholder data, name of object and object instance.
 */
public class MessageData {
    private final String name;
    private final Object object;

    /**
     * Construct new message placeholder data, with given name and object.
     *
     * @param name   name of placeholder object.
     * @param object object instance used in placeholder.
     */
    public MessageData(String name, Object object) {
        this.name = name;
        this.object = object;
    }

    /**
     * Simpler costructor for this object. <br>
     * Construct new message placeholder data, with given name and object.
     *
     * @param name   name of placeholder object.
     * @param object object instance used in placeholder.
     * @return new instance of {@link MessageData}
     */
    public static MessageData e(String name, Object object) {
        return new MessageData(name, object);
    }

    public String getName() {
        return name;
    }

    public Object getObject() {
        return object;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString())
                .append("name", this.name)
                .append("object", this.object)
                .toString();
    }
}
