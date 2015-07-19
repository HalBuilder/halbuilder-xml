package com.theoryinpractise.halbuilder.xml;

import com.google.common.base.Strings;
import com.google.common.collect.Ordering;
import com.theoryinpractise.halbuilder.api.*;
import com.theoryinpractise.halbuilder.api.RepresentationWriter;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.theoryinpractise.halbuilder.impl.api.Support.HREF;
import static com.theoryinpractise.halbuilder.impl.api.Support.HREFLANG;
import static com.theoryinpractise.halbuilder.impl.api.Support.LINK;
import static com.theoryinpractise.halbuilder.impl.api.Support.NAME;
import static com.theoryinpractise.halbuilder.impl.api.Support.REL;
import static com.theoryinpractise.halbuilder.impl.api.Support.SELF;
import static com.theoryinpractise.halbuilder.impl.api.Support.TEMPLATED;
import static com.theoryinpractise.halbuilder.impl.api.Support.PROFILE;
import static com.theoryinpractise.halbuilder.impl.api.Support.TITLE;
import static com.theoryinpractise.halbuilder.xml.XmlRepresentationFactory.XSI_NAMESPACE;

public class XmlRepresentationWriter implements RepresentationWriter<String> {

    public void write(ReadableRepresentation representation, Set<URI> flags, Writer writer) {
        final Element element = renderElement(flags, "self", representation, false);
        try {
            Format prettyFormat = flags.contains(RepresentationFactory.PRETTY_PRINT)
                    ? Format.getPrettyFormat()
                    : Format.getCompactFormat();

            final XMLOutputter outputter = new XMLOutputter(prettyFormat);
            outputter.output(element, writer);
        } catch (IOException e) {
            throw new RepresentationException(e);
        }
    }

    private Element renderElement(Set<URI> flags, String rel, ReadableRepresentation representation, boolean embedded) {

        final Link resourceLink = representation.getResourceLink();

        // Create the root element
        final Element resourceElement = new Element("resource");
        if (resourceLink != null) {
            resourceElement.setAttribute("href", resourceLink.getHref());
        }

        if (!rel.equals("self")) {
            resourceElement.setAttribute("rel", rel);
        }

        // Only add namespaces to non-embedded resources
        if (!embedded) {
            for (Map.Entry<String, String> entry : representation.getNamespaces().entrySet()) {
                resourceElement.addNamespaceDeclaration(
                        Namespace.getNamespace(entry.getKey(), entry.getValue()));
            }
            // Add the instance namespace if there are null properties on this
            // representation or on any embedded resources.
            if (representation.hasNullProperties()) {
                resourceElement.addNamespaceDeclaration(XSI_NAMESPACE);
            }
        }

        //add a comment
//        resourceElement.addContent(new Comment("Description of a representation"));

        // add links
        List<Link> links = representation.getLinks();
        for (Link link : links) {
            Element linkElement = new Element(LINK);
            if (!link.getRel().equals(SELF)) {
                linkElement.setAttribute(REL, link.getRel());
                linkElement.setAttribute(HREF, link.getHref());
                if (!Strings.isNullOrEmpty(link.getName())) {
                    linkElement.setAttribute(NAME, link.getName());
                }
                if (!Strings.isNullOrEmpty(link.getTitle())) {
                    linkElement.setAttribute(TITLE, link.getTitle());
                }
                if (!Strings.isNullOrEmpty(link.getHreflang())) {
                    linkElement.setAttribute(HREFLANG, link.getHreflang());
                }
                if (!Strings.isNullOrEmpty(link.getProfile())) {
                    linkElement.setAttribute(PROFILE, link.getProfile());
                }
                if (link.hasTemplate()) {
                    linkElement.setAttribute(TEMPLATED, "true");
                }
                resourceElement.addContent(linkElement);
            }
        }

        // add properties
        for (Map.Entry<String, Object> entry : representation.getProperties().entrySet()) {
            Element propertyElement = new Element(entry.getKey());
            if (entry.getValue() != null) {
                propertyElement.setContent(new Text(entry.getValue().toString()));
            } else {
                propertyElement.setAttribute("nil", "true", XSI_NAMESPACE);
            }
            resourceElement.addContent(propertyElement);
        }

        // add subresources

        Map<String, Collection<ReadableRepresentation>> resourceMap = representation.getResourceMap();

        for (Map.Entry<String, Collection<ReadableRepresentation>> resourceEntry : resourceMap.entrySet()) {

            Rel registeredRel = representation.getRels().get(resourceEntry.getKey());

            final boolean sortedRel = registeredRel != null && !registeredRel.isSingleton();

            final Collection<ReadableRepresentation> values = sortedRel
                                                              ? Ordering.from((registeredRel).comparator()).sortedCopy(resourceEntry.getValue())
                                                              : resourceEntry.getValue();

            final String collectionRel = sortedRel && !flags.contains(RepresentationFactory.SILENT_SORTING)
                                         ? resourceEntry.getKey() + " sorted:" + ((Rel.Sorted) registeredRel).id()
                                         : resourceEntry.getKey();

            for (ReadableRepresentation value : values) {
                Element subResourceElement = renderElement(flags, collectionRel, value, true);
                resourceElement.addContent(subResourceElement);
            }
        }

        return resourceElement;
    }

}
