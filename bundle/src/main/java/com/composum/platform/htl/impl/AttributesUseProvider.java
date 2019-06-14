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

import javax.script.Bindings;
import javax.servlet.http.HttpSession;

/**
 * Allows reading request- or session-attributes or the {@link EmulatedPageContext} with a data-sly-use
 * statement such that the IDE knows the specific class and can provide code completion etc.
 * Since the retrieved value can be a model, the priority of this
 * needs to be higher than the models use providers. This use provider is activated whenever the {@value #PARAM_SCOPE} paraeter is present. Usage example:
 * <code>
 * &lt;sly data-sly-use.searchresult="${'com.composum.pages.commons.service.search.SearchService.Result' @ fromScope='request',
 * key='searchresult'}"/&gt:
 * </code>
 * Possible scopes are defined in {@link Scope}.
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
     */
    public static final String PARAM_SCOPE = "fromScope";

    /** Parameter for the name of the attribute to read from; mandatory except for scope {@link Scope#VALUE}. */
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
        if (haveScope && (haveKey || null != rawValue)) {
            Scope scope = rawScope instanceof Scope ? (Scope) rawScope :
                    Scope.valueOf(rawScope.toString().toUpperCase());
            Object value = getAttribute(renderContext, scope, rawKey.toString(), rawValue);
            if (null == value) {
                String scriptName = EmulatedPageContext.scriptName(renderContext.getBindings());
                LOG.info("Could not find key {} in {} for {}", rawKey, rawScope, scriptName);
                return ProviderOutcome.success(null);
                // other alternative would be this, but that somewhat collides with the HTL philosophy of being null-lenient:
                // return ProviderOutcome.failure( new IllegalStateException("Could not find key " + rawKey + " in " + rawScope + " for " + scriptName));
            }
            return ProviderOutcome.success(value);
        }
        return ProviderOutcome.failure();
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
