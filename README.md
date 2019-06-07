# HTL integration with Composum Pages

## Scope

This is an initial approach to integrate the [Adobe HTL Expression Language](https://docs.adobe.com/content/help/en/experience-manager-htl/using/htl/expression-language.html) (formerly called Sightly) with the [Composum Pages](https://github.com/ist-dresden/composum-pages) framework.

Unfortunately HTL is quite limited in it's expressiveness, and does not (yet?) support an equivalent of JSP tag libraries. Since the [Composum Tag libraries](https://ist-software.atlassian.net/wiki/spaces/CMP/pages/50659330/Composum+Taglibs) are an important part of the Pages framework, this is a bit limited, and thus not recommended for production usage. There is a somewhat limited adapter that allows using the Composum tags within HTL, which uses one HTL template for the tag start and one for the tag end. Obviously this cannot hide or change the content of the tag, as it is possible in JSP, and it is more verbose.

This project provides the UseProviders that enable the creation of [Composum models](https://ist-software.atlassian.net/wiki/spaces/CMP/pages/65110057/Composum+Models) within HTL, and the discussed Tag adapter. As an example, the test artefact defines a search component completely with HTL. 

More details please find [here](https://ist-software.atlassian.net/wiki/spaces/CMP/pages/67567617/Composum+and+HTL).


## What is HTL?

The <a href="https://github.com/Adobe-Marketing-Cloud/htl-spec" class="external-link">HTML Template Language HTL</a>
(formerly called Sightly) is a modern replacement for JSP that provides
the dynamic behaviour by using HTML5 like data attributes and using HTML
elements as block statements, and provides enhanced security against XSS
by tightly integrating with HTML syntax and automatically escaping data
properly.

## Integration with [Composum models](https://ist-software.atlassian.net/wiki/spaces/CMP/pages/65110057/Composum+Models)

The `ComposumModelUseProvider` extends the
<a href="https://docs.adobe.com/docs/en/htl/docs/use-api/java.html" class="external-link">HTL Java Use-Api</a> such
that an object is also created if it can be created
with `BeanContext.adaptTo`, which supports the creation of
both `SlingBean`s and Pages `Model`s with or without Sling-Models. Thus,
to create a model is as easy as e.g.

``` syntaxhighlighter-pre
// no parameters
<sly data-sly-use.someobject="foo.bar.SomeClass">
// with parameters
<sly data-sly-use.someobject="${'foo.bar.SomeClass' @ parameter=resource}">
```

## Replacement for [Composum Tag libraries](https://ist-software.atlassian.net/wiki/spaces/CMP/pages/50659330/Composum+Taglibs)

Since HTL there is
<a href="http://blogs.adobe.com/experiencedelivers/experience-management/htl-intro-part-5/" class="external-link">no direct counterpart for JSP tag libraries</a>,
there is a partial emulation of the composum tag libraries as HTL
templates. Unfortunately the
<a href="https://github.com/Adobe-Marketing-Cloud/htl-spec/blob/master/SPECIFICATION.md#22102-call" class="external-link">data-sly-call</a> to
a template cannot access the elements content where the call is placed
at. Thus, the usual JSP mechanisms for wrapping some content into a
custom tag that modifies it and possibly adds variables valid in that
context cannot be easily be carried over.

As a workaround the tags that expect content have to be split into
templates, one for the start and one for the end of the tag. For
instance the [cpp:element
Tag](https://ist-software.atlassian.net/wiki/content-only/viewpage.action?pageId=50167841&iframeId=fallback-mode&user_key=ff808081452709e101452d7d30b6000b&user_id=hps&xdm_e=https://ist-software.atlassian.net/&xsm_c=fallback-mode-fake-key__00963168003513526&cp=/wiki&cv=0.0.0-fallback-mode&lic=none#ComposumPagesTaglib-cpp:...-cpp:element)
can be used as follows: 

``` syntaxhighlighter-pre
<sly data-sly-call="${cpp.startElement @ var='field', type='com.composum.pages.components.model.search.SearchField'}"/>
... content that would be within a cpp:element tag in JSP ...
<sly data-sly-call="${cpp.endElement}"/>
```

TODO: how to access the defined variables?

## Usage-Patterns

## Tips

Some of the less obvious things:

If you use of the
<a href="https://sling.apache.org/documentation/bundles/models.html#injector-specific-annotations" class="external-link">injector-specific annotations</a>,
adding `@Inject` is unneccesary. These annotations can be recognized by
carrying the @InjectAnnotation annotation.

## Links

-   <a href="https://docs.adobe.com/docs/en/htl/overview.html" class="external-link">https://docs.adobe.com/docs/en/htl/overview.html</a>
-   <a href="https://github.com/Adobe-Marketing-Cloud/htl-spec/blob/master/SPECIFICATION.md" class="external-link">HTL Specification</a>
-   <a href="http://blogs.adobe.com/experiencedelivers/experience-management/htl-intro-part-1/" class="external-link">http://blogs.adobe.com/experiencedelivers/experience-management/htl-intro-part-1/</a>,
    incl. some
    <a href="http://blogs.adobe.com/experiencedelivers/experience-management/htl-intro-part-5/" class="external-link">FAQ</a>
-   <a href="https://sling.apache.org/documentation/bundles/scripting/scripting-htl.html" class="external-link">Sling: HTL Scripting Engine</a>
-   HTL
    Repl <a href="http://localhost:9090/htl/repl.html" class="external-link">http://localhost:9090/htl/repl.html</a>
-   Slides
    about <a href="https://de.slideshare.net/GabrielWalt/component-development" class="external-link">AEM Sightly Template Language</a>
-   <a href="https://sling.apache.org/project-information.html" class="external-link">https://sling.apache.org/project-information.html</a>
-   Stackoverflow: <a href="https://stackoverflow.com/questions/tagged/sling-models" class="external-link">Tag sling-models</a>, <a href="https://stackoverflow.com/questions/tagged/sling" class="external-link">Tag sling</a>

## Limitations

-   The JSP Expression language cannot be used in the Composum Tags when
    these are used in HTL.

## Implementation concerns

### Extension points of HTL: 

-   org.apache.sling.scripting.sightly.use.UseProvider provides ways to
    instantiate the objects for data-sly-use blocks; the
    ComposumModelUseProvider is a UseProvider that instantiates
    Sling-Models also from a BeanContext.
-   org.apache.sling.scripting.sightly.extension.RuntimeExtension :
    handler for various built in functions, see RuntimeFunction. Could
    e.g. be used to extend i18n to the Composum Pages way, or extend
    uriManipulation or includeResource with additional arguments or
    functionality.

### Misc. findings

Defining a global with use within a template doesn't work - that's only
local to the template. Thus, a template cannot declare global variables.
(Idea: special composum variables as one model `composum`).  
Statements in a template library are not executed when loading - HTL
just takes the templates out of there.  
Unknown variables are taken from the bindings, but are initialized
before everything else, so we can't define additional globals.  
The bindings passed on by the Java Use-Api are freshly generated - if
it's neccesary to keep something permanently, one needs to put it into
the request attributes.

### Limitations of HTL

When trying to carry over the concept of tag libraries to HTL, there are
the following limitations that hurt the possibilities:

-   A <a href="https://github.com/Adobe-Marketing-Cloud/htl-spec/blob/master/SPECIFICATION.md#22102-call" class="external-link">data-sly-call</a>
    on an element throws away the content of the element. Thus, it is
    not possible to modify, conditionally throw away, or repeat the
    content of the element, or easily surround some content with a
    computed number of elements.
    -   Partial solution: split up the tags with content into a start
        and an end template. Problem: functionality that concerns the
        content of a tag cannot be implemented, and no variables for the
        content of the tag can be set. (Possible extension of HTL: the
        content of the data-sly-call element could be delivered to the
        template e.g. as
        a <a href="https://sling.apache.org/apidocs/sling9/org/apache/sling/scripting/sightly/java/compiler/RenderUnit.html" class="external-link">RenderUnit</a>
        as a binding variable, which could be rendered once or several
        times via a new mechanism.)
-   There seems no way to define something like functions as in a tag
    library. (Idea: Use-Provider that calls a static function and
    delivers the result? (→ no IDE support.) Special class for each
    taglib function with constructor parameters corresponding to the
    arguments of the taglib function?)
-   In a template the parameters cannot be documented. Thus, it is not
    possible to see documentation within the IDE.
-   It is not possible to modify the global variables from a template.
    Thus, it its not possible to emulate a tag
    like [cpp:defineObjects](https://ist-software.atlassian.net/wiki/content-only/viewpage.action?pageId=50167841&iframeId=fallback-mode&user_key=ff808081452709e101452d7d30b6000b&user_id=hps&xdm_e=https://ist-software.atlassian.net/&xsm_c=fallback-mode-fake-key__8775492479392046&cp=/wiki&cv=0.0.0-fallback-mode&lic=none#ComposumPagesTaglib-cpp:...-cpp:defineObjects)
    that defines several objects at once - the closest you can do is to
    create a model class that has several attributes with the objects to
    define. (Mostly OK; perhaps HTL could be extended that a template
    could also define some models, not only templates.).
-   <a href="https://github.com/Adobe-Marketing-Cloud/htl-spec/blob/master/SPECIFICATION.md#2210-template--call" class="external-link">data-sly-use for template libraries</a>
    etc. does not use the resource search path (/apps, /libs, ...)
-   To output an attribute of type URI one needs to insert .toString -
    URI are silently swallowed.

## Open points / possible extensions

-   Model that extends ResourceHandle and can provide e.g.
    inheritedValues (while setting the inheritancetype). Partial
    solution: `<sly data-sly-use.resourceHandle="com.composum.sling.core.ResourceHandle" />`
-   EL Function equivalents
-   Model that provides the defineObjects functionality
