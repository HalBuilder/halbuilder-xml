package com.theoryinpractise.halbuilder.xml;

import com.theoryinpractise.halbuilder.DefaultRepresentationFactory;

/**
 * Created by amrk on 27/05/13.
 */
public class XmlRepresentationFactory extends DefaultRepresentationFactory {
    public XmlRepresentationFactory() {
        withRenderer(HAL_XML, XmlRepresentationWriter.class);
        withReader(HAL_XML, XmlRepresentationReader.class);
    }
}
