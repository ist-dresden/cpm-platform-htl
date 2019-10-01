package com.composum.platform.htl.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.scripting.sightly.render.RenderContext;
import org.apache.sling.scripting.sightly.use.ProviderOutcome;
import org.apache.sling.scripting.sightly.use.UseProvider;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.SourceVersion;
import javax.script.Bindings;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Stack;

/**
 * Allows reading request- or session-attributes or the {@link EmulatedPageContext} with a data-sly-use
 * statement such that the IDE knows the specific class and can provide code completion etc.
 * Since the retrieved value can be a model, the priority of this
 * needs to be higher than the models use providers. This use provider is activated whenever the {@value #PARAM_SCOPE} parameter is present. Possible scopes are defined in {@link Scope}. Usage example:
 * <pre>
 * &lt;sly data-sly-use.searchresult="${'com.composum.pages.commons.service.search.SearchService.Result' @ fromScope='request',
 * key='searchresult'}"/&gt;
 * </pre>
 * If the parameter {@value #PARAM_KEY} is not present, the identifier is used as key. This might make sense if you don't want to
 * specify the Java class of the value, which is more concise but gives your IDE less power to check:
 * <pre>
 * &lt;sly data-sly-use.searchresult="${'searchresult' @ fromScope='request'}"/&gt;
 * </pre>
 * This can also be used to give the IDE a clue on the type of some binding. If there is a binding <code>searchresult</code> and you want your IDE
 * to know about it's type to easily access it's attributes:
 * <pre>
 * &lt;sly data-sly-use.searchresult="${'com.composum.pages.commons.service.search.SearchService.Result' @ fromScope='bindings',
 *  * key='searchresult'}"/&gt;
 * </pre>
 *
 * @author Hans-Peter Stoerr
 * @since 09/2017
 */
@Component(
        service = UseProvider.class,
        configurationPid = "AttributesUseProvider",
        property = {
                Constants.SERVICE_RANKING + ":Integer=97"
        }
)
public class AttributesUseProvider implements UseProvider {

    private static final Logger LOG = LoggerFactory.getLogger(AttributesUseProvider.class);

    @interface Configuration {

        @AttributeDefinition(
                name = "Service Ranking",
                description = "The Service Ranking of the AttributesUseProvider. Should be minimally higher than " +
                        "ComposumModelsUseProvider because this has to be run first."
        )
        int service_ranking() default 97;
    }

    /**
     * Mandatory parameter that determines the scope to read from: lower case string representation of {@link Scope} .
     * If this parameter isn't present, the {@link AttributesUseProvider} is not used.
     */
    public static final String PARAM_SCOPE = "fromScope";

    /** If the identifier is a class name, this is the name of the attribute name to read from - otherwise the identifier is taken. */
    public static final String PARAM_KEY = "key";

    /** Explicitly given value; mandatory for scope Scope {@link Scope#VALUE}. */
    public static final String PARAM_VALUE = "value";

    public enum Scope {
        /** From the script bindings. */
        BINDINGS,
        /** A page context simulated by {@link EmulatedPageContext}. */
        PAGE,
        REQUEST,
        SESSION,
        /** Explicit value given with parameter {@value #PARAM_VALUE} - this corresponds to a typecast. */
        VALUE
    }

    @Override
    public ProviderOutcome provide(String identifier, RenderContext renderContext, Bindings arguments) {
        Object rawKey = arguments.get(PARAM_KEY);
        boolean haveKey = null != rawKey && rawKey instanceof String && StringUtils.isNotBlank((String) rawKey);
        Object rawScope = arguments.get(PARAM_SCOPE);
        boolean haveScope = null != rawScope && null != rawScope;
        Object rawValue = arguments.get(PARAM_VALUE);

        String scriptName = EmulatedPageContext.getScriptName(renderContext.getBindings());
        if (haveScope) {
            Scope scope;
            try {
                scope = rawScope instanceof Scope ? (Scope) rawScope :
                        Scope.valueOf(rawScope.toString().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ProviderOutcome.failure(
                        new IllegalArgumentException("Scope '" + rawScope + "' is not defined. Possible values are: " +
                                Arrays.asList(Scope.values()) + " for " + scriptName));
            }

            String key = StringUtils.defaultIfBlank(rawKey != null ? String.valueOf(rawKey) : null, identifier);
            Object value = getAttribute(renderContext, scope, key, rawValue);
            if (null == value) {
                LOG.info("Could not find key {} in {} for {}", rawKey, rawScope, scriptName);
                return ProviderOutcome.success(null);
                // other alternative would be this, but that somewhat collides with the HTL philosophy of being null-lenient:
                // return ProviderOutcome.failure( new IllegalStateException("Could not find key " + rawKey + " in " + rawScope + " for " + scriptName));
            } else {
                if (key != identifier) checkType(value, identifier, scriptName);
            }
            return ProviderOutcome.success(value);
        }
        return ProviderOutcome.failure();
    }

    protected void checkType(Object value, String identifier, String scriptName) {
        if (SourceVersion.isName(identifier) && identifier.contains(".")) {
            if (value != null) {
                Stack<Class<?>> classes = new Stack<>();
                classes.push(value.getClass());
                while (!classes.isEmpty()) {
                    Class<?> clazz = classes.pop();
                    if (clazz.getName().equals(identifier)) {
                        return;
                    }
                    if (clazz.getSuperclass() != null) {
                        classes.push(clazz.getSuperclass());
                    }
                    for (Class<?> itf : clazz.getInterfaces()) {
                        classes.push(itf);
                    }
                }
            }
            LOG.warn("Not instance of {} in {} : {}", new Object[]{identifier, scriptName, value});
        }
    }

    protected SlingHttpServletRequest getRequest(RenderContext renderContext) {
        return (SlingHttpServletRequest) renderContext.getBindings().get(SlingBindings.REQUEST);
    }

    protected Object getAttribute(RenderContext renderContext, Scope scope, String key, Object rawValue) {
        switch (scope) {
            case BINDINGS:
                return renderContext.getBindings().get(key);
            case REQUEST:
                return getRequest(renderContext).getAttribute(key);
            case SESSION:
                HttpSession session = getRequest(renderContext).getSession(false);
                return null == session ? null : session.getAttribute(key);
            case PAGE:
                return EmulatedPageContext.map(renderContext.getBindings()).get(key);
            case VALUE:
                return rawValue;
            default: // Impossible
                throw new IllegalArgumentException("Unknown scope " + scope);
        }
    }
}
