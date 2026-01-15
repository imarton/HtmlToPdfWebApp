package dev.martoni;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import javax.servlet.http.HttpServletResponse;
import org.xhtmlrenderer.pdf.ITextRenderer;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;

@ManagedBean
@RequestScoped
public class HelloBean implements Serializable {

    public String getCurrentTime() {
        return new Date().toString();
    }

    public void buildPdfExport() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();

        try {
            UIComponent component = facesContext.getViewRoot();

            if (component != null) {
                StringWriter stringWriter = new StringWriter();
                ResponseWriter originalWriter = facesContext.getResponseWriter();

                RenderKit renderKit = facesContext.getRenderKit();
                ResponseWriter writer = renderKit.createResponseWriter(stringWriter, "text/html", "UTF-8");
                facesContext.setResponseWriter(writer);

                component.encodeAll(facesContext);

                if (originalWriter != null) {
                    facesContext.setResponseWriter(originalWriter);
                }

                String xhtmlContent = stringWriter.toString();

                // Flying Saucernek néha szüksége van a doctype-ra és a korrekt XHTML formátumra
                // Ha a JSF kimenete nem tartalmazza a doctype-ot, hozzáadhatjuk, 
                // de a getViewRoot renderelésekor általában benne van ha a template-ben benne van.
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ITextRenderer renderer = new ITextRenderer();
                renderer.setDocumentFromString(xhtmlContent);
                renderer.layout();
                renderer.createPDF(baos);

                byte[] pdfBytes = baos.toByteArray();

                HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
                response.reset();
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=\"export.pdf\"");
                response.setContentLength(pdfBytes.length);

                OutputStream os = response.getOutputStream();
                os.write(pdfBytes);
                os.flush();
                os.close();

                facesContext.responseComplete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
