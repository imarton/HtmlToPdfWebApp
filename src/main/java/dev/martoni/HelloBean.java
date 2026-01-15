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
            // Mindig az index.xhtml-t akarjuk exportálni
            String viewId = "/index.xhtml";
            javax.faces.application.ViewHandler viewHandler = facesContext.getApplication().getViewHandler();
            javax.faces.component.UIViewRoot viewRoot = viewHandler.createView(facesContext, viewId);

            // Mentjük az eredeti nézetet, majd beállítjuk az újat
            javax.faces.component.UIViewRoot originalView = facesContext.getViewRoot();
            facesContext.setViewRoot(viewRoot);

            // FONTOS: A komponensfa felépítése (Build View), különben üres lesz a kimenet
            facesContext.getAttributes().put("javax.faces.IS_BUILDING_INITIAL_STATE", Boolean.TRUE);
            viewHandler.getViewDeclarationLanguage(facesContext, viewId).buildView(facesContext, viewRoot);
            facesContext.getAttributes().remove("javax.faces.IS_BUILDING_INITIAL_STATE");

            // 2. Renderelés StringWriter-be
            StringWriter stringWriter = new StringWriter();
            RenderKit renderKit = facesContext.getRenderKit();
            ResponseWriter writer = renderKit.createResponseWriter(stringWriter, "text/html", "UTF-8");
            facesContext.setResponseWriter(writer);

            try {
                viewRoot.encodeAll(facesContext);
            } finally {
                facesContext.setViewRoot(originalView); // Visszaállítjuk az eredeti nézetet
            }

            String xhtmlContent = stringWriter.toString();

            // 3. Tisztítás:
            // A Bootstrap script tagek és a nem lezárt meta tagek gyakran hibát okoznak.
            xhtmlContent = cleanForXhtml(xhtmlContent);
            //System.out.println(xhtmlContent);

            // 4. PDF generálás
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(xhtmlContent, null);
            builder.toStream(baos);
            builder.run();

            // 5. HTTP Válasz kiküldése
            OutputStream os = writePdfToResponse(baos, externalContext);
            os.close();

            facesContext.responseComplete();

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
     * Segédmetódus a JSF kimenet XHTML-kompatibilissé tételéhez.
     */
    private String cleanForXhtml(String html) {
        // Eltávolítjuk a szkripteket, mert a PDF-ben nem futnak és gyakran hibás XML-t okoznak
        String cleaned = html.replaceAll("(?s)<script.*?>.*?</script>", "");
        // Biztosítjuk a HTML5 meta charset lezárását, ha hiányozna
        cleaned = cleaned.replaceAll("<meta charset=\"utf-8\">", "<meta charset=\"utf-8\" />");
        // PrimeFaces specifikus: néha maradhatnak benne entitások, amiket a parser nem szeret
        cleaned = cleaned.replace("&nbsp;", "&#160;");
        return cleaned;
    }
}
