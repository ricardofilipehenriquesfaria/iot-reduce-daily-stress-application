package app.miti.com.roads_width;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ricardo on 08-09-2017.
 */

public class RoadsWidth{

    public static List<RoadsWidth> roadsWidthList = new ArrayList<>();

    private int id;
    private String toponimo;
    private String categoria;
    private String tipo_uso;
    private int extensao_via;
    private double largura_via;
    private String tipo_pavimento;
    private String estado_conservacao;

    public RoadsWidth(){
        super();
    }

    public RoadsWidth(int id, String toponimo, String categoria, String tipo_uso, int extensao_via, double largura_via, String tipo_pavimento, String estado_conservacao) {
        this.id = id;
        this.toponimo = toponimo;
        this.categoria = categoria;
        this.tipo_uso = tipo_uso;
        this.extensao_via = extensao_via;
        this.largura_via = largura_via;
        this.tipo_pavimento = tipo_pavimento;
        this.estado_conservacao = estado_conservacao;
    }

    public int getId() {
        return id;
    }

    public String getToponimo() {
        return toponimo;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getTipoUso() {
        return tipo_uso;
    }

    public int getExtensaoVia() {
        return extensao_via;
    }

    public double getLarguraVia() {
        return largura_via;
    }

    public String getTipoPavimento() {
        return tipo_pavimento;
    }

    public String getEstadoConservacao() {
        return estado_conservacao;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setToponimo(String toponimo) {
        this.toponimo = toponimo;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setTipoUso(String tipo_uso) {
        this.tipo_uso = tipo_uso;
    }

    public void setExtensaoVia(int extensao_via) {
        this.extensao_via = extensao_via;
    }

    public void setLarguraVia(double largura_via) {
        this.largura_via = largura_via;
    }

    public void setTipoPavimento(String tipo_pavimento) {
        this.tipo_pavimento = tipo_pavimento;
    }

    public void setEstadoConservacao(String estado_conservacao) {
        this.estado_conservacao = estado_conservacao;
    }

    public static void setRoadsWidthList(RoadsWidth roadsWidth){
        roadsWidthList.add(roadsWidth);
    }

    public List<RoadsWidth> getRoadsWidthList(){
        return roadsWidthList;
    }
}
