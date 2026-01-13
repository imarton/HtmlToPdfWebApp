package dev.martoni;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import javax.servlet.http.HttpServletResponse;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.*;
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
            UIComponent component = facesContext.getViewRoot().findComponent("forPdfExport");
            if (component == null) {
                // Megpróbáljuk megkeresni rekurzívan ha a findComponent nem találja meg közvetlenül (pl. form-on belül van)
                component = findComponent(facesContext.getViewRoot(), "forPdfExport");
            }

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

                String htmlContent = stringWriter.toString();
                String xhtmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
                        "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                        "<head><title>PDF Export</title>\n" +
                        "<style>\n" +
                        "body { font-family: 'Arial', sans-serif; }\n" +
                        "table { width: 100%; border-collapse: collapse; }\n" +
                        "th, td { border: 1px solid black; padding: 8px; text-align: left; }\n" +
                        "th { background-color: #f2f2f2; }\n" +
                        "</style>\n" +
                        "</head><body>\n" +
                        htmlContent +
                        "</body></html>";

                // Tisztítás: PrimeFaces táblázat specifikus dolgok eltávolítása vagy javítása ha szükséges
                // (Egyszerűsítve most csak a renderelt HTML-t használjuk)

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(xhtmlContent, null);
                builder.toStream(baos);
                builder.run();

                OutputStream os = writePdfToResponse(baos, externalContext);
                os.close();

                facesContext.responseComplete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes PDF data to HTTP response stream
     */
    private static OutputStream writePdfToResponse(ByteArrayOutputStream baos, ExternalContext externalContext) throws IOException {
        byte[] pdfBytes = baos.toByteArray();

        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
        response.reset();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"export.pdf\"");
        response.setContentLength(pdfBytes.length);

        OutputStream os = response.getOutputStream();
        os.write(pdfBytes);
        os.flush();
        return os;
    }

    /**
     * Recursively searches a component tree for matching ID
     */
    private UIComponent findComponent(UIComponent parent, String id) {
        if (id.equals(parent.getId())) {
            return parent;
        }
        for (UIComponent child : parent.getChildren()) {
            UIComponent found = findComponent(child, id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
