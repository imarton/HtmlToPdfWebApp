package dev.martoni;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean
@ViewScoped
public class PcBean implements Serializable {

    private List<PcElement> pcElements;

    @PostConstruct
    public void init() {
        pcElements = new ArrayList<>();
        pcElements.add(new PcElement("AMD Ryzen 5 5600X", "Processzor", 65000));
        pcElements.add(new PcElement("NVIDIA RTX 3060 Ti", "Videokártya", 120000));
        pcElements.add(new PcElement("Kingston FURY Beast 16GB RAM", "Memória", 18000));
        pcElements.add(new PcElement("Samsung 980 Pro 1TB SSD", "Háttértár", 35000));
        pcElements.add(new PcElement("ASUS ROG STRIX B550-F", "Alaplap", 55000));
        pcElements.add(new PcElement("Corsair RM750x", "Tápegység", 45000));
    }

    public List<PcElement> getPcElements() {
        return pcElements;
    }

    public void setPcElements(List<PcElement> pcElements) {
        this.pcElements = pcElements;
    }
}
