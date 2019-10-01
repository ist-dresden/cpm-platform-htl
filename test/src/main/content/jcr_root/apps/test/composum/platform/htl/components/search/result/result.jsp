<%@page session="false" pageEncoding="UTF-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.2" %>
<%@taglib prefix="cpn" uri="http://sling.composum.com/cpnl/1.0" %>
<%@taglib prefix="cpp" uri="http://sling.composum.com/cppl/1.0" %>
<%-- This JSP serves as a frame for the htl.html, which contains the actual rendering code for the component as HTL.
    The frame contains the Composum tags, which are more concise and better documented in their JSP form.
    The actual rendering code is however separated out into htl.html using Sightly / HTL,
    which may or may not more suitable for web designers. --%>
<cpp:defineObjects/>
<cpp:element var="model" type="com.composum.pages.components.model.search.SearchResult" scope="request">
    <sling:call script="result.htl.html"/>
</cpp:element>
