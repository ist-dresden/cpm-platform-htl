# HTL integration with Composum Pages

## Scope

This is an initial approach to integrate the [Adobe HTL Expression Language](https://docs.adobe.com/content/help/en/experience-manager-htl/using/htl/expression-language.html) (formerly called Sightly) with the [Composum Pages](https://github.com/ist-dresden/composum-pages) framework.

Unfortunately HTL is quite limited in it's expressiveness, and does not (yet?) support an equivalent of JSP tag libraries. Since the [Composum Tag libraries](https://ist-software.atlassian.net/wiki/spaces/CMP/pages/50659330/Composum+Taglibs) are an important part of the Pages framework, this is a bit limited, and thus not recommended for production usage. There is a somewhat limited adapter that allows using the Composum tags within HTL, which uses one HTL template for the tag start and one for the tag end. Obviously this cannot hide or change the content of the tag, as it is possible in JSP, and it is more verbose.

This project provides the UseProviders that enable the creation of [Composum models](https://ist-software.atlassian.net/wiki/spaces/CMP/pages/65110057/Composum+Models) within HTL, and the discussed Tag adapter. As an example, the test artefact defines a search component completely with HTL. 

More details please find [here](https://ist-software.atlassian.net/wiki/spaces/CMP/pages/67567617/Composum+and+HTL).
