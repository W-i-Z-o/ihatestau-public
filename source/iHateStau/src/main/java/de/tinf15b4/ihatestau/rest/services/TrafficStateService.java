package de.tinf15b4.ihatestau.rest.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.xml.XmlEscapers;

import de.tinf15b4.ihatestau.camera.api.CameraSpotTrafficEvaluationService;
import de.tinf15b4.ihatestau.persistence.CameraSpotConfigEntity;
import de.tinf15b4.ihatestau.persistence.PersistenceBean;
import de.tinf15b4.ihatestau.rest.exceptions.NoDataCachedException;

@Path("/traffic")
public class TrafficStateService {
    @Inject
    private CameraSpotTrafficEvaluationService spotTraffic;

    @Inject
    private PersistenceBean persistenceBean;

    @GET
    @Path("camera/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStateForSimpleCamera(@PathParam("id") String id) {
        CameraSpotConfigEntity c = persistenceBean.selectById(CameraSpotConfigEntity.class, id);
        if (c != null) {
            return Response.ok(spotTraffic.getJamProbabilitySmooth(c)).build();
        } else {
            return Response.status(404, "Not Found").build();
        }
    }

    @GET
    @Path("bulk")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Float> getStateBulk(@QueryParam("camera") Set<String> cameras) {
        Map<String, Float> retval = new HashMap<>();

        for (String id : cameras) {
            CameraSpotConfigEntity c = persistenceBean.selectById(CameraSpotConfigEntity.class, id);
            if (c != null) {
                retval.put(id, spotTraffic.getJamProbabilitySmooth(c));
            }
        }

        return retval;
    }

    @GET
    @Path("bulk-front-image")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, byte[]> getFrontImageBulk(@QueryParam("camera") Set<String> cameras) {
        Map<String, byte[]> retval = new HashMap<>();

        for (String id : cameras) {
            CameraSpotConfigEntity c = persistenceBean.selectById(CameraSpotConfigEntity.class, id);
            if (c != null) {
                try {
                    retval.put(id, spotTraffic.getImageFront(c));
                } catch (NoDataCachedException e) {
                }
            }
        }

        return retval;
    }

    @GET
    @Path(".html")
    @Produces("text/html; charset=utf-8")
    public String getHtml() {
        StringBuilder b = new StringBuilder();

        b.append("<!DOCTYPE html>");
        b.append("<title>Traffic State</title>");
        b.append("<ul>");
        for (CameraSpotConfigEntity c : persistenceBean.selectAll(CameraSpotConfigEntity.class)) {
            b.append("<li>");
            b.append("<a href=\"camera/");
            b.append(XmlEscapers.xmlAttributeEscaper().escape(c.getId()));
            b.append("\">");
            b.append(XmlEscapers.xmlContentEscaper().escape(c.getName()));
            b.append("</a>");
        }
        b.append("</ul>");

        return b.toString();
    }
}
