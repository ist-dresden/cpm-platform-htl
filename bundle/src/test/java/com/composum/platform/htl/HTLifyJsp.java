package com.composum.platform.htl;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Small application that transforms the Composum tags in a JSP (read from standard input) to the approximate template
 * calls for a HTL file. This can serve as a base to convert an existing JSP in to similar HTML, but has to be checked
 * by hand.
 */
public class HTLifyJsp {

    public static void main(String[] args) throws Exception {
        String file = IOUtils.toString(new FileReader(new File("/Users/hps/dev/composum/cpm-site-composum/app/package/src/main/content/jcr_root/apps/ist/composum/components/documentation/servlet/servlet.jsp")));
        // System.out.println("Please paste the JSP content into standard input.");
        // String file = IOUtils.toString(System.in, Charsets.UTF_8);

        {
            // </cpp:editDialog>
            // to
            // <sly data-sly-call="${cpp.endEditDialog}"/>
            final Pattern endTag = Pattern.compile("</*(?<namespace>cp[a-z]):(?<tagname>\\w+)\\s*>", Pattern.MULTILINE);

            Matcher m = endTag.matcher(file);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String endTagHtl = "" +
                        "<sly data-sly-call=\"${" +
                        m.group("namespace") +
                        ".end" +
                        WordUtils.capitalize(m.group("tagname")) +
                        "}\"/>";
                m.appendReplacement(sb,
                        Matcher.quoteReplacement(
                                endTagHtl
                        ));
            }
            m.appendTail(sb);
            file = sb.toString();
        }

        {
            // <cpp:widget label="Selector" property="selector" type="textfield" i18n="false" rules="optional"
            //     hint="Sling selector which renders a found resource as search result. Default &quot;searchItem&quot;."/>
            // to
            // <sly data-sly-call="${cpp.widget @ label='Selector',  property='selector',  type='text',  i18n='false',  rules='optional',
            //    hint='Sling selector which renders a found resource as search result. Default &quot;searchItem&quot;.'}"/>
            //
            final Pattern attributeMatcher = Pattern.compile("(?<whitespace>\\s*)(?<attribute>\\w+)=\"(?<value>[^=]*)\"");

            final Pattern startTag = Pattern.compile("<(?<namespace>cp[a-z]):(?<tagname>\\w+)(?<attributes>" +
                    "((" + attributeMatcher.pattern() + ")*)" +
                    ")\\s*(?<tagend>/?>)", Pattern.MULTILINE);

            Matcher m = startTag.matcher(file);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                StringBuilder attrib = new StringBuilder();
                Matcher attr = attributeMatcher.matcher(m.group("attributes"));
                while (attr.find()) {
                    if (attrib.length() > 0) attrib.append(",");
                    attrib //
                            .append(attr.group("whitespace"))
                            .append(attr.group("attribute"))
                            .append("='")
                            .append(quoteAttribute(attr.group("value")))
                            .append("'");
                }
                if (attrib.length() > 0) attrib.insert(0, " @");
                String tagname = m.group("tagname");
                if (!m.group("tagend").contains("/>"))
                    tagname = "start" + WordUtils.capitalize(tagname);
                String startTagHtl = "" +
                        "<sly data-sly-call=\"${" +
                        m.group("namespace") +
                        "." +
                        tagname +
                        attrib +
                        "}\"/>";
                m.appendReplacement(sb,
                        Matcher.quoteReplacement(
                                startTagHtl
                        ));
            }
            m.appendTail(sb);
            file = sb.toString();
        }

        System.out.println("<!--/* Translated with " + HTLifyJsp.class.getName() + " */-->");
        System.out.println("<sly data-sly-use.cpp=\"/libs/composum/platform/htl/cppl.html\"\n" +
                "     data-sly-use.cpn=\"/libs/composum/platform/htl/cpnl.html\"\n" +
                "     data-sly-use.cpm=\"/libs/composum/platform/htl/composum.html\"></sly>\n");
        System.out.println(file);
    }

    private static String quoteAttribute(String value) {
        String result = value;
        result = StringUtils.replace(result, "'", "\\'");
        result = StringUtils.replace(result, "\"", "\\\"");
        return result;
    }
}
