package de.tinf15b4.ihatestau.rest.services;

import java.io.IOException;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import de.tinf15b4.ihatestau.persistence.ImageMaskEntity;
import de.tinf15b4.ihatestau.persistence.PersistenceBean;

@Path("/mask")
public class MaskImageService {
    @Inject
    private PersistenceBean persistenceBean;

    @GET
    @Path("/{id}")
    @Produces("image/png")
    public Response getImage(@PathParam("id") String id) throws IOException {
        ImageMaskEntity e = persistenceBean.selectById(ImageMaskEntity.class, id);
        if (e != null)
            return Response.ok(e.getImageData()).build();
        else
            return Response.status(404, "Not Found").build();
    }

    @POST
    @Path("/{id}")
    @Consumes("image/png")
    @RolesAllowed("admin")
    public Response setImage(@PathParam("id") String id, byte[] data) throws IOException {
        ImageMaskEntity e = new ImageMaskEntity();
        e.setId(id);
        e.setImageData(data);
        persistenceBean.merge(e);

        return Response.ok().build();
    }
}
